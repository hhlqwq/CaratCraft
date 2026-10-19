package com.platform.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

/** 使用自建容器元数据检查上传边界, 不将模拟媒体称为已完成真实解码验证. */
class ProductVideoUploadTest {
  /** 检查服务端读取时长和分辨率, 不依赖客户端上报参数. */
  @Test
  void readsContainerMetadata() {
    var video = ProductVideoUpload.inspect(movie(30000, 640, "avc1", 1));
    assertEquals(30000, video.durationMs());
    assertEquals(640, video.width());
    assertEquals(480, video.height());
  }

  /** 超长、高分辨率及不支持编码均应在存储之前拒绝. */
  @Test
  void rejectsUnsupportedMediaDeclarations() {
    assertThrows(
        IllegalArgumentException.class,
        () -> ProductVideoUpload.inspect(movie(60001, 640, "avc1", 1)));
    assertThrows(
        IllegalArgumentException.class,
        () -> ProductVideoUpload.inspect(movie(30000, 4096, "avc1", 1)));
    assertThrows(
        IllegalArgumentException.class,
        () -> ProductVideoUpload.inspect(movie(30000, 640, "hvc1", 1)));
  }

  /** 自包含引用之外的媒体不能让播放器加载外部资源. */
  @Test
  void rejectsExternalReferences() {
    assertThrows(
        IllegalArgumentException.class,
        () -> ProductVideoUpload.inspect(movie(30000, 640, "avc1", 0)));
  }

  /** 截断、超大长度和分片盒子不能导致越界读取或被当成正常商品视频. */
  @Test
  void rejectsTruncationOverflowAndFragments() {
    byte[] data = movie(30000, 640, "avc1", 1);
    assertThrows(
        IllegalArgumentException.class,
        () -> ProductVideoUpload.inspect(java.util.Arrays.copyOf(data, data.length - 1)));
    ByteBuffer.wrap(data).putInt(-1);
    assertThrows(IllegalArgumentException.class, () -> ProductVideoUpload.inspect(data));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            ProductVideoUpload.inspect(
                join(movie(30000, 640, "avc1", 1), box("moof", new byte[0]))));
  }

  /** 盒子数量有上限, 大量空元数据不能造成无限解析. */
  @Test
  void limitsParserBudget() {
    byte[][] free = new byte[10002][];
    free[0] = box("ftyp", join("isom".getBytes(StandardCharsets.US_ASCII), new byte[4]));
    for (int index = 1; index < free.length; index++) {
      free[index] = box("free", new byte[0]);
    }
    assertThrows(IllegalArgumentException.class, () -> ProductVideoUpload.inspect(join(free)));
  }

  /** AAC-LC声明可以与视频同时存在, 其他音频对象不能仅靠mp4a名称通过. */
  @Test
  void verifiesAudioDecoderDeclaration() {
    assertEquals(640, ProductVideoUpload.inspect(withAudio(0x12)).width());
    assertThrows(IllegalArgumentException.class, () -> ProductVideoUpload.inspect(withAudio(0x2a)));
  }

  /** 编码配置版本损坏时即使其余容器完整也应拒绝. */
  @Test
  void rejectsMalformedCodecConfiguration() {
    byte[] data = movie(30000, 640, "avc1", 1);
    int offset = new String(data, StandardCharsets.ISO_8859_1).indexOf("avcC") + 4;
    data[offset] = 0;
    assertThrows(IllegalArgumentException.class, () -> ProductVideoUpload.inspect(data));
  }

  /** 在最小模拟电影中增加自包含音轨, 用于测试编码声明而非实际音频解码. */
  private static byte[] withAudio(int audioConfiguration) {
    byte[] video = movie(30000, 640, "avc1", 1);
    int movieOffset = ByteBuffer.wrap(video).getInt(0);
    int movieSize = ByteBuffer.wrap(video).getInt(movieOffset);
    byte[] original = java.util.Arrays.copyOfRange(video, movieOffset + 8, movieOffset + movieSize);
    byte[] specific = {5, 2, (byte) audioConfiguration, 0x10};
    byte[] decoder = join(new byte[] {0x40, 0x15}, new byte[11], specific);
    byte[] stream = join(new byte[] {0, 2, 0, 4, (byte) decoder.length}, decoder);
    byte[] esds = box("esds", join(new byte[4], new byte[] {3, (byte) stream.length}, stream));
    byte[] sample = new byte[28];
    ByteBuffer.wrap(sample).putShort(6, (short) 1);
    byte[] stsd =
        box(
            "stsd",
            join(
                ByteBuffer.allocate(8).putInt(0).putInt(1).array(),
                box("mp4a", join(sample, esds))));
    byte[] reference =
        box(
            "dref",
            join(
                ByteBuffer.allocate(8).putInt(0).putInt(1).array(),
                box("url ", new byte[] {0, 0, 0, 1})));
    byte[] track =
        box("trak", box("mdia", box("minf", join(box("dinf", reference), box("stbl", stsd)))));
    return join(
        java.util.Arrays.copyOf(video, movieOffset),
        box("moov", join(original, track)),
        box("mdat", new byte[] {1}));
  }

  /** 构造只用于容器解析的最小声明, 不包含真实编码的视频内容. */
  private static byte[] movie(int duration, int width, String codec, int referenceFlags) {
    byte[] header = new byte[20];
    ByteBuffer.wrap(header).putInt(12, 1000).putInt(16, duration);
    byte[] sample = new byte[78];
    ByteBuffer.wrap(sample)
        .putShort(6, (short) 1)
        .putShort(24, (short) width)
        .putShort(26, (short) 480);
    byte[] avc =
        box(
            codec,
            join(
                sample,
                box("avcC", new byte[] {1, 66, 0, 30, -1, -31, 0, 2, 0x67, 0, 1, 0, 2, 0x68, 0})));
    byte[] stsd = box("stsd", join(ByteBuffer.allocate(8).putInt(0).putInt(1).array(), avc));
    byte[] url = box("url ", ByteBuffer.allocate(4).putInt(referenceFlags).array());
    byte[] dref = box("dref", join(ByteBuffer.allocate(8).putInt(0).putInt(1).array(), url));
    byte[] minf = box("minf", join(box("dinf", dref), box("stbl", stsd)));
    byte[] movie = box("moov", join(box("mvhd", header), box("trak", box("mdia", minf))));
    return join(
        box("ftyp", join("isom".getBytes(StandardCharsets.US_ASCII), new byte[4])),
        movie,
        box("mdat", new byte[] {1}));
  }

  /** 每个测试盒子显式编码大端长度, 用于构造合法边界及故意损坏的输入. */
  private static byte[] box(String type, byte[] payload) {
    return join(
        ByteBuffer.allocate(4).putInt(payload.length + 8).array(),
        type.getBytes(StandardCharsets.US_ASCII),
        payload);
  }

  /** 在内存拼接小型测试数据, 不写文件也不下载第三方媒体. */
  private static byte[] join(byte[]... values) {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    for (byte[] value : values) {
      output.writeBytes(value);
    }
    return output.toByteArray();
  }
}
