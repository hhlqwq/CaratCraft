package com.platform.modules.sys.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** 验证替代监控服务的页面契约与有效数值, 不输出机器标识或环境秘密. */
class HostMonitorServiceTest {
  /** 真正采集系统信息, 验证管理端依赖的字段和百分比边界. */
  @Test
  void collectReturnsCompatibleSnapshot() {
    Map<String, Object> snapshot = new HostMonitorService().collect();
    assertTrue(
        snapshot
            .keySet()
            .containsAll(
                List.of(
                    "cupInfo", "memoryInfo", "sysInfo", "jvmInfo", "diskInfos", "processList")));
    Map<?, ?> cpu = (Map<?, ?>) snapshot.get("cupInfo");
    double load = ((Number) cpu.get("usePercent")).doubleValue();
    assertTrue(Double.isFinite(load) && load >= 0 && load <= 1);
    Map<?, ?> jvm = (Map<?, ?>) snapshot.get("jvmInfo");
    assertTrue(((Number) jvm.get("startTime")).longValue() > 0);
  }

  /** 连续请求仍产生有限数值, 不需要停顿或长时间CPU采样. */
  @Test
  void consecutiveSamplesRemainFinite() {
    HostMonitorService service = new HostMonitorService();
    service.collect();
    Map<String, Object> snapshot = service.collect();
    assertTrue(Double.isFinite(((Number) snapshot.get("diskUsePercent")).doubleValue()));
    assertFalse(((List<?>) snapshot.get("processList")).size() > 10);
  }

  /** 验证磁盘列表去重且容量没有越界. */
  @Test
  void disksHaveUniqueKeysAndValidCapacity() {
    List<?> disks = (List<?>) new HostMonitorService().collect().get("diskInfos");
    HashSet<Object> keys = new HashSet<>();
    for (Object entry : disks) {
      Map<?, ?> disk = (Map<?, ?>) entry;
      keys.add(disk.get("uuid"));
      long total = ((Number) disk.get("totalSpace")).longValue();
      long available = ((Number) disk.get("usableSpace")).longValue();
      long used = ((Number) disk.get("usedSpace")).longValue();
      assertEquals(total, used + available);
      assertTrue(total > 0 && available >= 0 && available <= total);
    }
    assertEquals(disks.size(), keys.size());
  }
}
