package com.platform.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.platform.common.exception.BusinessException;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import javax.imageio.ImageIO;

/** 图片上传必须基于实际内容, 并丢弃原始文件携带的数据. */
class SafeImageUploadTest {
    /** 假冒图片的HTML与SVG即使使用PNG文件名也应拒绝. */
    @Test
    void activeContentIsRejected() {
        for (String content :
                new String[] {
                    "<html><script>alert(1)</script></html>",
                    "<svg xmlns='http://www.w3.org/2000/svg' onload='alert(1)'/>"
                }) {
            assertThrows(
                    BusinessException.class,
                    () ->
                            SafeImageUpload.readPng(
                                    new MockMultipartFile(
                                            "file",
                                            "fake.png",
                                            "image/png",
                                            content.getBytes(StandardCharsets.UTF_8))));
        }
    }

    /** 有效图片后的脚本字节不会出现在重新编码的图片中. */
    @Test
    void reencodingStripsAppendedPayload() throws Exception {
        ByteArrayOutputStream source = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "png", source);
        source.write("<script>payload</script>".getBytes(StandardCharsets.UTF_8));
        byte[] result =
                SafeImageUpload.readPng(
                        new MockMultipartFile(
                                "file", "image.png", "image/png", source.toByteArray()));
        assertFalse(new String(result, StandardCharsets.ISO_8859_1).contains("payload"));
        assertEquals(2, ImageIO.read(new java.io.ByteArrayInputStream(result)).getWidth());
    }

    /** 超过5MB的文件在解码前拒绝. */
    @Test
    void oversizedImageIsRejected() {
        assertThrows(
                BusinessException.class,
                () ->
                        SafeImageUpload.readPng(
                                new MockMultipartFile("file", new byte[5 * 1024 * 1024 + 1])));
    }
}
