package com.platform.modules.oss.cloud;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.platform.common.exception.BusinessException;
import com.platform.common.utils.Constant;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;

/** 本地存储边界回归, 只在测试运行器指定的独立临时目录写入. */
class DiskCloudStorageServiceTest {
  @TempDir(cleanup = CleanupMode.NEVER)
  Path directory;

  /** 视频多级目录必须可创建, 返回的公开地址与实际文件路径一致. */
  @Test
  void supportsNestedMediaDirectories() throws Exception {
    var service = storage();
    byte[] bytes = {1, 2, 3, 4};
    assertEquals(
        "/upload/video/20261019/owned.mp4", service.upload(bytes, "video/20261019/owned.mp4"));
    assertArrayEquals(
        bytes, Files.readAllBytes(directory.resolve("storage/video/20261019/owned.mp4")));
  }

  /** 即使命名发生碰撞也不能覆盖已有媒体. */
  @Test
  void refusesToOverwriteExistingFiles() throws Exception {
    var service = storage();
    byte[] original = {1, 2, 3, 4};
    service.upload(original, "video/existing.mp4");
    assertThrows(
        BusinessException.class,
        () -> service.upload(new byte[] {5, 6, 7, 8}, "video/existing.mp4"));
    assertArrayEquals(
        original, Files.readAllBytes(directory.resolve("storage/video/existing.mp4")));
  }

  /** 规范化后的路径必须仍位于配置根目录内. */
  @Test
  void rejectsDirectoryTraversal() {
    assertThrows(
        BusinessException.class, () -> storage().upload(new byte[] {1, 2, 3}, "../escape.mp4"));
    assertFalse(Files.exists(directory.resolve("escape.mp4")));
  }

  /** 视频保留商家云前缀, 媒体MIME与已验证输出后缀一致. */
  @Test
  void retainsVideoPrefixAndCorrectMediaTypes() {
    var config = new CloudStorageConfig();
    config.setType(Constant.CloudService.ALIYUN.getValue());
    config.setAliyunPrefix("merchant");
    var service = new DiskCloudStorageService(config);
    assertTrue(service.getVideoPath().startsWith("merchant/video/"));
    assertTrue(service.getVideoPath().endsWith(".mp4"));
    assertEquals("video/mp4", service.contentType("video/file.MP4"));
    assertEquals("image/png", service.contentType("file.png"));
  }

  /** 每个测试使用独立存储根目录, 不读取或修改商家的存储配置. */
  private DiskCloudStorageService storage() {
    var config = new CloudStorageConfig();
    config.setDiskPath(directory.resolve("storage").toString());
    config.setProxyServer("/upload");
    return new DiskCloudStorageService(config);
  }
}
