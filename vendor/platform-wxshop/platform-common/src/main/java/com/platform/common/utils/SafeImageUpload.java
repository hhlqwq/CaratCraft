package com.platform.common.utils;

import com.platform.common.exception.BusinessException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.springframework.web.multipart.MultipartFile;

/** 上传图片的内容校验和重新编码, 不信任扩展名或客户端MIME. */
public final class SafeImageUpload {
  private static final long MAX_BYTES = 5L * 1024 * 1024;
  private static final long MAX_PIXELS = 20_000_000L;

  /** 工具类不允许实例化. */
  private SafeImageUpload() {}

  /** 校验大小及解码尺寸, 输出不带原始元数据或活动内容的PNG. */
  public static byte[] readPng(MultipartFile file) throws IOException {
    if (file == null || file.isEmpty() || file.getSize() > MAX_BYTES) {
      throw new BusinessException("请上传不超过5MB的图片");
    }
    try (ImageInputStream input = ImageIO.createImageInputStream(file.getInputStream())) {
      if (input == null) {
        throw new BusinessException("图片无法识别");
      }
      Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
      if (!readers.hasNext()) {
        throw new BusinessException("仅支持JPEG、PNG和GIF图片");
      }
      ImageReader reader = readers.next();
      try {
        String format = reader.getFormatName();
        if (!format.equalsIgnoreCase("jpeg") && !format.equalsIgnoreCase("png")
            && !format.equalsIgnoreCase("gif")) {
          throw new BusinessException("图片格式不支持");
        }
        reader.setInput(input, true, true);
        int width = reader.getWidth(0);
        int height = reader.getHeight(0);
        if (width <= 0 || height <= 0 || (long) width * height > MAX_PIXELS) {
          throw new BusinessException("图片尺寸超过2000万像素");
        }
        BufferedImage image = reader.read(0);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        if (image == null || !ImageIO.write(image, "png", output)
            || output.size() > MAX_BYTES) {
          throw new BusinessException("图片转换失败或转换后超过5MB");
        }
        return output.toByteArray();
      } finally {
        reader.dispose();
      }
    }
  }
}
