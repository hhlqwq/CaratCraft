package com.platform.modules.mall.controller;

import com.platform.common.utils.ProductVideoUpload;
import com.platform.common.utils.RestResponse;
import com.platform.modules.mall.service.ProductVideoService;
import com.platform.modules.oss.cloud.UploadFactory;
import com.platform.modules.sys.controller.AbstractController;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Semaphore;
import lombok.RequiredArgsConstructor;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** 商品视频使用独立权限、配额、类型检查与存储目录, 不放宽图片上传规则. */
@RestController
@RequestMapping("mall/product-video")
@RequiredArgsConstructor
public class ProductVideoController extends AbstractController {
  private static final Semaphore UPLOAD_SLOTS = new Semaphore(2);
  private final ProductVideoService videos;

  /** 预留配额后限制实际读取, 只有校验和存储均成功才允许商品引用媒体记录. */
  @PostMapping("upload")
  @RequiresPermissions(
      value = {"mall:goods:save", "mall:goods:update"},
      logical = Logical.OR)
  public RestResponse<Map<String, Object>> upload(@RequestParam("file") MultipartFile file)
      throws IOException {
    String operator = getUserId();
    if (!UPLOAD_SLOTS.tryAcquire()) {
      throw new IllegalArgumentException("服务器正在处理其他视频, 请稍后重试");
    }
    try {
      long id = videos.reserve(operator);
      ProductVideoUpload.Video video = ProductVideoUpload.read(file);
      try (var storage = UploadFactory.build()) {
        String url = storage.upload(video.bytes(), storage.getVideoPath());
        return RestResponse.ok(videos.complete(operator, id, url, video));
      }
    } finally {
      UPLOAD_SLOTS.release();
    }
  }
}
