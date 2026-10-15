package com.platform.modules.sys.service;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.software.os.OSFileStore;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

/** 直接采集系统信息, 保持管理端现有JSON字段契约. */
@Service
public class HostMonitorService {
  private final SystemInfo systemInfo = new SystemInfo();
  private long[] previousCpuTicks = systemInfo.getHardware().getProcessor().getSystemCpuLoadTicks();

  /** 采集一次快照, CPU使用率按相邻请求的计数差计算, 不阻塞等待采样. */
  public synchronized Map<String, Object> collect() {
    CentralProcessor processor = systemInfo.getHardware().getProcessor();
    GlobalMemory memory = systemInfo.getHardware().getMemory();
    OperatingSystem operatingSystem = systemInfo.getOperatingSystem();
    Map<String, Object> server = new HashMap<>();
    double cpuLoad = processor.getSystemCpuLoadBetweenTicks(previousCpuTicks);
    previousCpuTicks = processor.getSystemCpuLoadTicks();
    server.put("cupInfo", Map.of(
        "physicalProcessorCount", processor.getPhysicalProcessorCount(),
        "logicalProcessorCount", processor.getLogicalProcessorCount(),
        "usePercent", Math.max(0, Math.min(1, cpuLoad))));
    server.put("centralProcessor", processor.getProcessorIdentifier());
    server.put("sysInfo", systemDescription());
    server.put("memoryInfo", Map.of(
        "total", formatBytes(memory.getTotal()),
        "free", formatBytes(memory.getAvailable()),
        "used", formatBytes(memory.getTotal() - memory.getAvailable()),
        "usePercent", ratio(memory.getTotal() - memory.getAvailable(), memory.getTotal())));
    server.put("jvmInfo", jvmDescription());
    List<Map<String, Object>> disks = disks(operatingSystem);
    server.put("diskInfos", disks);
    long total = 0;
    long available = 0;
    for (Map<String, Object> disk : disks) {
      total += (Long) disk.get("totalSpace");
      available += (Long) disk.get("usableSpace");
    }
    server.put("diskUsePercent", ratio(total - available, total) * 100);
    server.put("processList", processes(operatingSystem));
    return server;
  }

  /** 生成服务器描述, 无法解析主机名时仍返回可用快照. */
  private Map<String, Object> systemDescription() {
    String name = "unknown";
    String ip = "unknown";
    try {
      InetAddress address = InetAddress.getLocalHost();
      name = address.getHostName();
      ip = address.getHostAddress();
    } catch (UnknownHostException ignored) {
      // 主机解析失败不影响CPU、内存和磁盘采集.
    }
    return Map.of("name", name, "ip", ip,
        "osName", System.getProperty("os.name", "unknown"),
        "osArch", System.getProperty("os.arch", "unknown"),
        "userDir", System.getProperty("user.dir", ""));
  }

  /** 读取当前JVM内存和启动时间, 百分比统一使用0到1的比例. */
  private Map<String, Object> jvmDescription() {
    Runtime runtime = Runtime.getRuntime();
    long used = runtime.totalMemory() - runtime.freeMemory();
    return Map.of("jdkName", System.getProperty("java.vm.name", "unknown"),
        "jdkVersion", System.getProperty("java.version", "unknown"),
        "jdkHome", System.getProperty("java.home", ""),
        "maxMemory", formatBytes(runtime.maxMemory()),
        "usedMemory", formatBytes(used),
        "freeMemory", formatBytes(runtime.maxMemory() - used),
        "usePercent", ratio(used, runtime.maxMemory()),
        "startTime", ManagementFactory.getRuntimeMXBean().getStartTime(),
        "uptime", ManagementFactory.getRuntimeMXBean().getUptime());
  }

  /** 枚举文件系统, 同一底层卷的重复挂载只统计一次. */
  private List<Map<String, Object>> disks(OperatingSystem operatingSystem) {
    List<Map<String, Object>> result = new ArrayList<>();
    Set<String> devices = new HashSet<>();
    for (OSFileStore store : operatingSystem.getFileSystem().getFileStores()) {
      if (store.getTotalSpace() <= 0) {
        continue;
      }
      String key = store.getUUID().isBlank()
          ? store.getVolume() + ":" + store.getType() : store.getUUID();
      if (!devices.add(key)) {
        continue;
      }
      long total = store.getTotalSpace();
      long available = Math.max(0, Math.min(total, store.getUsableSpace()));
      Map<String, Object> disk = new HashMap<>();
      disk.put("uuid", key);
      disk.put("name", store.getName());
      disk.put("volume", store.getVolume());
      disk.put("label", store.getLabel());
      disk.put("mount", store.getMount());
      disk.put("type", store.getType());
      disk.put("size", formatBytes(total));
      disk.put("used", formatBytes(total - available));
      disk.put("avail", formatBytes(available));
      disk.put("totalSpace", total);
      disk.put("usableSpace", available);
      disk.put("usePercent", ratio(total - available, total));
      result.add(disk);
    }
    return result;
  }

  /** 返回CPU占用排序的前10个进程, 不包含命令行和环境秘密. */
  private List<Map<String, Object>> processes(OperatingSystem operatingSystem) {
    List<Map<String, Object>> result = new ArrayList<>();
    for (OSProcess process : operatingSystem.getProcesses(
        null, OperatingSystem.ProcessSorting.CPU_DESC, 10)) {
      Map<String, Object> item = new HashMap<>();
      item.put("pid", process.getProcessID());
      item.put("cpu", process.getProcessCpuLoadCumulative());
      item.put("name", process.getName());
      item.put("state", process.getState().name());
      item.put("threadCount", process.getThreadCount());
      item.put("virtualSize", process.getVirtualSize());
      item.put("residentSetSize", process.getResidentSetSize());
      item.put("kernelTime", process.getKernelTime());
      item.put("userTime", process.getUserTime());
      item.put("startTime", process.getStartTime());
      item.put("upTime", process.getUpTime());
      item.put("bytesRead", process.getBytesRead());
      item.put("bytesWritten", process.getBytesWritten());
      item.put("openFiles", process.getOpenFiles());
      result.add(item);
    }
    return result;
  }

  /** 避免空容量产生NaN或无限值. */
  private double ratio(long used, long total) {
    return total > 0 ? Math.max(0, Math.min(1, (double) used / total)) : 0;
  }

  /** 以GiB格式输出容量, 不依赖系统区域设置. */
  private String formatBytes(long value) {
    return String.format(Locale.ROOT, "%.2f GiB", value / (1024.0 * 1024 * 1024));
  }
}
