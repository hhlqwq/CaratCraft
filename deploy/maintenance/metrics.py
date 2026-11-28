"""采集有明确来源的Docker宿主及五服务指标, 历史只保留实际采样."""

from collections import deque
from concurrent.futures import ThreadPoolExecutor
from datetime import datetime, timezone
import json
import os
from pathlib import Path
import platform
import threading
import time

import docker_api

STATE = Path(os.environ.get("CARATCRAFT_MAINTENANCE_STATE", "/state"))
LOCK = threading.Lock()
SNAPSHOT = {"status": "COLLECTING", "message": "正在采集首个真实样本", "history": []}
PREVIOUS = {}
HISTORY = deque(maxlen=240)


def unavailable(reason="当前环境无法读取"):
    """缺失指标显式携带原因, 不填充伪造的零值."""
    return {"supported": False, "reason": reason}


def ratio(used, total):
    """将真实计数转换为有界百分比, 缺少分母时保留未知."""
    return round(max(0, min(100, used * 100 / total)), 2) if total else None


def service_snapshot(service):
    """读取单服务的真实状态及累计计数, 首次CPU样本保持未知."""
    try:
        metadata = docker_api.inspect(service)
        state = metadata["State"]
        result = {"service": service, "name": docker_api.NAMES[service], "status": state["Status"],
                  "health": state.get("Health", {}).get("Status", "未配置"), "startedAt": state.get("StartedAt"),
                  "version": metadata["Config"].get("Labels", {}).get("org.opencontainers.image.version"),
                  "restartCount": metadata.get("RestartCount", 0), "imageId": metadata["Image"],
                  "image": metadata["Config"]["Image"], "sourceDigest": metadata["Config"].get("Labels", {}).get("io.caratcraft.source-digest")}
        if state["Running"]:
            stats = docker_api.request("/containers/" + metadata["Id"] + "/stats?stream=false&one-shot=true")
            cpu = stats.get("cpu_stats", {})
            memory = stats.get("memory_stats", {})
            total = cpu.get("cpu_usage", {}).get("total_usage", 0)
            system = cpu.get("system_cpu_usage", 0)
            previous = PREVIOUS.get(service)
            percent = None
            if previous and system > previous[1] and total >= previous[0]:
                percent = round((total - previous[0]) * cpu.get("online_cpus", 1) * 100 / (system - previous[1]), 2)
            PREVIOUS[service] = (total, system)
            cache = memory.get("stats", {}).get("inactive_file", 0)
            used = max(0, memory.get("usage", 0) - cache)
            result.update(cpuPercent=percent, memoryUsed=used, memoryLimit=memory.get("limit"),
                          memoryPercent=ratio(used, memory.get("limit", 0)))
        return result
    except (OSError, ValueError, RuntimeError, KeyError) as error:
        return {"service": service, "name": docker_api.NAMES[service], "status": "UNAVAILABLE", "message": str(error)}


def host_snapshot(info):
    """宿主内核数据注明Docker虚拟机或Linux宿主, Mac物理硬件保持不可用."""
    desktop = "desktop" in info.get("OperatingSystem", "").lower()
    result = {"origin": "DOCKER_VM" if desktop else "DOCKER_HOST_KERNEL",
              "originLabel": "Docker Desktop Linux虚拟机" if desktop else "Docker宿主Linux内核",
              "physicalHost": unavailable("容器无法读取Mac物理硬件" if desktop else "内核可能运行于虚拟机, 无法确认物理硬件"),
              "os": info.get("OperatingSystem"), "architecture": info.get("Architecture", platform.machine()),
              "cores": info.get("NCPU"), "temperature": unavailable(), "gpu": unavailable("未配置受支持的GPU驱动采集"),
              "memoryTotal": info.get("MemTotal"), "cpuPercent": None}
    try:
        proc = Path("/host/proc")
        memory = {}
        for line in (proc / "meminfo").read_text().splitlines():
            key, value = line.split(":", 1)
            memory[key] = int(value.strip().split()[0]) * 1024
        total, free = memory["MemTotal"], memory["MemAvailable"]
        result.update(memoryTotal=total, memoryAvailable=free, memoryUsed=total-free, memoryPercent=ratio(total-free, total))
        result["uptimeSeconds"] = float((proc / "uptime").read_text().split()[0])
        result["load"] = [float(value) for value in (proc / "loadavg").read_text().split()[:3]]
        cpu_values = [int(value) for value in (proc / "stat").read_text().splitlines()[0].split()[1:9]]
        ticks, idle = sum(cpu_values), sum(cpu_values[3:5])
        previous = PREVIOUS.get("host-cpu")
        if previous and ticks > previous[0]:
            result["cpuPercent"] = ratio(ticks-previous[0]-(idle-previous[1]), ticks-previous[0])
        PREVIOUS["host-cpu"] = (ticks, idle)
        model = next((line.split(":", 1)[1].strip() for line in (proc / "cpuinfo").read_text().splitlines()
                      if line.startswith(("model name", "Hardware"))), None)
        result["cpuModel"] = model or unavailable("宿主未公开CPU型号")
        now = time.monotonic()
        networks = []
        for line in (proc / "1/net/dev").read_text().splitlines()[2:]:
            interface, values = line.split(":", 1)
            values = values.split()
            receive, transmit = int(values[0]), int(values[8])
            previous = PREVIOUS.get("net-" + interface)
            elapsed = now - previous[2] if previous else 0
            networks.append({"name": interface.strip(), "receivedBytes": receive, "sentBytes": transmit,
                             "receiveBytesPerSecond": max(0, (receive-previous[0])/elapsed) if elapsed else None,
                             "sendBytesPerSecond": max(0, (transmit-previous[1])/elapsed) if elapsed else None})
            PREVIOUS["net-" + interface] = (receive, transmit, now)
        result["networks"] = networks
        temperatures = []
        for sensor in Path("/host/sys/class/thermal").glob("thermal_zone*/temp"):
            try:
                value = int(sensor.read_text().strip()) / 1000
                if -20 < value < 150:
                    temperatures.append(value)
            except (OSError, ValueError):
                continue
        if temperatures:
            result["temperature"] = {"supported": True, "readingsCelsius": temperatures}
    except (OSError, ValueError, KeyError):
        result["kernelDetails"] = unavailable("只读宿主内核挂载不可用")
    disk = os.statvfs(STATE)
    result["storage"] = {"origin": "维护持久卷所在文件系统", "total": disk.f_blocks * disk.f_frsize,
                         "available": disk.f_bavail * disk.f_frsize,
                         "percent": ratio(disk.f_blocks-disk.f_bfree, disk.f_blocks),
                         "io": unavailable("当前挂载无法唯一映射到物理块设备")}
    return result


def collect_loop():
    """每15秒采样并保留最近一小时真实趋势, 服务退出前不阻塞业务请求."""
    global SNAPSHOT
    STATE.mkdir(parents=True, exist_ok=True)
    path = STATE / "metrics-history.json"
    try:
        records = json.loads(path.read_text())
        HISTORY.extend(records[-240:])
    except (OSError, ValueError, TypeError):
        pass
    while True:
        start = time.monotonic()
        try:
            info = docker_api.request("/info")
            host = host_snapshot(info)
            with ThreadPoolExecutor(max_workers=5) as executor:
                services = list(executor.map(service_snapshot, docker_api.NAMES))
            sampled = datetime.now(timezone.utc).isoformat()
            HISTORY.append({"time": sampled, "cpuPercent": host.get("cpuPercent"), "memoryPercent": host.get("memoryPercent"),
                            "services": [{key: item.get(key) for key in ("name", "cpuPercent", "memoryUsed", "status")} for item in services]})
            snapshot = {"status": "OK", "sampledAt": sampled, "intervalSeconds": 15, "host": host,
                        "services": services, "history": list(HISTORY)}
            temporary = path.with_suffix(".tmp")
            temporary.write_text(json.dumps(list(HISTORY)))
            temporary.replace(path)
            with LOCK:
                SNAPSHOT = snapshot
        except (OSError, ValueError, RuntimeError) as error:
            with LOCK:
                SNAPSHOT = {"status": "UNAVAILABLE", "message": str(error), "history": list(HISTORY)}
        time.sleep(max(1, 15 - (time.monotonic()-start)))


def current():
    """返回已经采集的真实快照, 不由网页请求触发高频采集."""
    with LOCK:
        return SNAPSHOT
