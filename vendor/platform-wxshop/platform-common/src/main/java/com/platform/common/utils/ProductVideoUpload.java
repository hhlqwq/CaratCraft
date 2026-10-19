package com.platform.common.utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.web.multipart.MultipartFile;

/** 有界检查商品MP4容器及编码声明, 不调用系统命令或引入转码组件. */
public final class ProductVideoUpload {
  public static final int MAX_BYTES = 30 * 1024 * 1024;
  private static final Set<String> CONTAINERS =
      Set.of("moov", "trak", "mdia", "minf", "stbl", "dinf");

  /** 工具类不创建实例. */
  private ProductVideoUpload() {}

  /** 返回经过容器边界校验的原始字节及服务端读取的媒体参数. */
  public record Video(byte[] bytes, int durationMs, int width, int height) {}

  /** 先限制实际读取量再解析, 不相信扩展名、客户端时长或声明大小. */
  public static Video read(MultipartFile file) throws IOException {
    if (file == null || file.isEmpty() || file.getSize() > MAX_BYTES) {
      throw new IllegalArgumentException("请选择不超过30MB的MP4商品视频");
    }
    String name = file.getOriginalFilename();
    if (name == null || !name.toLowerCase(Locale.ROOT).endsWith(".mp4")) {
      throw new IllegalArgumentException("仅支持MP4视频, 请使用H.264编码导出");
    }
    byte[] data;
    try (var input = file.getInputStream()) {
      data = input.readNBytes(MAX_BYTES + 1);
    }
    return inspect(data);
  }

  /** 严格限制容器规模及公开播放参数, 不等同于解码检测、转码或内容审核. */
  public static Video inspect(byte[] data) {
    if (data == null || data.length < 32 || data.length > MAX_BYTES) {
      throw invalid();
    }
    Inspector inspector = new Inspector(data);
    List<Box> top = inspector.boxes(0, data.length);
    if (top.isEmpty() || !top.get(0).type().equals("ftyp")) {
      throw invalid();
    }
    int movies = 0;
    boolean media = false;
    for (Box box : top) {
      switch (box.type()) {
        case "ftyp" -> {
          inspector.require(box, 8);
          String brand = inspector.text(box.start());
          if (!Set.of("isom", "iso2", "mp41", "mp42", "avc1").contains(brand)) {
            throw invalid();
          }
        }
        case "moov" -> {
          movies++;
          inspector.visit(box, 0);
        }
        case "mdat" -> media |= box.end() > box.start();
        case "free", "skip", "wide" -> {}
        default -> throw invalid();
      }
    }
    if (movies != 1
        || !media
        || inspector.durationMs == 0
        || inspector.videoTracks != 1
        || inspector.audioTracks > 1
        || inspector.width == 0
        || inspector.references == 0) {
      throw invalid();
    }
    return new Video(data, inspector.durationMs, inspector.width, inspector.height);
  }

  /** 统一拒绝不支持或畸形的媒体, 不向客户端输出底层解析位置. */
  private static IllegalArgumentException invalid() {
    return new IllegalArgumentException("视频无效或不受支持: 需为60秒以内、1080p以内的非分片H.264 MP4, 音频可选AAC-LC");
  }

  private record Box(String type, int start, int end) {}

  /** 单次上传独立维护解析预算, 嵌套和盒子数量均设置硬上限. */
  private static final class Inspector {
    private final ByteBuffer bytes;
    private int budget = 10000;
    private int durationMs;
    private int width;
    private int height;
    private int videoTracks;
    private int audioTracks;
    private int references;

    /** 字节已限制为30MB以内, 解析期间不再复制文件数据. */
    private Inspector(byte[] data) {
      bytes = ByteBuffer.wrap(data);
    }

    /** 将32位无符号长度提升为long, 防止加法溢出绕过边界检查. */
    private long unsigned(int offset) {
      return Integer.toUnsignedLong(bytes.getInt(offset));
    }

    /** 四字符编码始终按ASCII读取, 不使用平台默认字符集. */
    private String text(int offset) {
      return new String(bytes.array(), offset, 4, StandardCharsets.US_ASCII);
    }

    /** 每个容器必须被完整且有序地覆盖, 不接受越界、零长或扩展长度盒子. */
    private List<Box> boxes(int start, int end) {
      List<Box> result = new ArrayList<>();
      for (int offset = start; offset < end; ) {
        if (--budget < 0 || end - offset < 8) {
          throw invalid();
        }
        long size = unsigned(offset);
        if (size < 8 || size > end - offset) {
          throw invalid();
        }
        int next = offset + (int) size;
        result.add(new Box(text(offset + 4), offset + 8, next));
        offset = next;
      }
      return result;
    }

    /** 在读取字段前验证对应盒子的最小负载长度. */
    private void require(Box box, int size) {
      if (box.end() - box.start() < size) {
        throw invalid();
      }
    }

    /** 仅递归已知容器和描述表, 避免将压缩媒体数据当作元数据执行解析. */
    private void visit(Box box, int depth) {
      if (depth > 10) {
        throw invalid();
      }
      int offset = box.start();
      if (CONTAINERS.contains(box.type())) {
        for (Box child : boxes(offset, box.end())) {
          visit(child, depth + 1);
        }
        return;
      }
      switch (box.type()) {
        case "mvhd", "mdhd" -> readDuration(box);
        case "stsd" -> {
          require(box, 8);
          List<Box> entries = boxes(offset + 8, box.end());
          if (unsigned(offset + 4) != 1 || entries.size() != 1) {
            throw invalid();
          }
          if (!Set.of("avc1", "mp4a").contains(entries.get(0).type())) {
            throw invalid();
          }
          visit(entries.get(0), depth + 1);
        }
        case "avc1" -> {
          require(box, 78);
          if (Short.toUnsignedInt(bytes.getShort(offset + 6)) != 1) {
            throw invalid();
          }
          width = Short.toUnsignedInt(bytes.getShort(offset + 24));
          height = Short.toUnsignedInt(bytes.getShort(offset + 26));
          videoTracks++;
          if (width == 0
              || height == 0
              || width > 1920
              || height > 1920
              || Math.min(width, height) > 1080
              || (long) width * height > 1920L * 1080) {
            throw invalid();
          }
          List<Box> configuration = boxes(offset + 78, box.end());
          Box avc =
              configuration.stream()
                  .filter(value -> value.type().equals("avcC"))
                  .findFirst()
                  .orElseThrow(ProductVideoUpload::invalid);
          readAvcConfiguration(avc);
        }
        case "mp4a" -> {
          require(box, 28);
          if (Short.toUnsignedInt(bytes.getShort(offset + 6)) != 1
              || bytes.getShort(offset + 8) != 0) {
            throw invalid();
          }
          List<Box> configuration = boxes(offset + 28, box.end());
          Box esds =
              configuration.stream()
                  .filter(value -> value.type().equals("esds"))
                  .findFirst()
                  .orElseThrow(ProductVideoUpload::invalid);
          readAudioConfiguration(esds);
          audioTracks++;
        }
        case "dref" -> {
          require(box, 8);
          List<Box> entries = boxes(offset + 8, box.end());
          if (unsigned(offset + 4) != 1 || entries.size() != 1) {
            throw invalid();
          }
          Box reference = entries.get(0);
          if (!reference.type().equals("url ")
              || reference.end() - reference.start() != 4
              || unsigned(reference.start()) != 1) {
            throw invalid();
          }
          references++;
        }
        case "mvex", "moof", "encv", "enca", "hvc1", "hev1", "av01" -> throw invalid();
        default -> {}
      }
    }

    /** 要求H.264配置包含有界SPS和PPS, 不把截断的编码配置当成可用媒体. */
    private void readAvcConfiguration(Box box) {
      require(box, 7);
      int offset = box.start();
      int lengthFlags = Byte.toUnsignedInt(bytes.get(offset + 4));
      int countFlags = Byte.toUnsignedInt(bytes.get(offset + 5));
      if (bytes.get(offset) != 1
          || (lengthFlags & 252) != 252
          || (lengthFlags & 3) == 2
          || (countFlags & 224) != 224
          || (countFlags & 31) == 0) {
        throw invalid();
      }
      offset += 6;
      for (int count = 0; count < (countFlags & 31); count++) {
        offset = parameterSet(offset, box.end(), 7);
      }
      if (offset >= box.end()) {
        throw invalid();
      }
      int count = Byte.toUnsignedInt(bytes.get(offset++));
      if (count == 0) {
        throw invalid();
      }
      for (int index = 0; index < count; index++) {
        offset = parameterSet(offset, box.end(), 8);
      }
    }

    /** 逐个检查NAL参数集长度和类型, 不解码视频帧或声称完成码流审核. */
    private int parameterSet(int offset, int end, int type) {
      if (end - offset < 3) {
        throw invalid();
      }
      int length = Short.toUnsignedInt(bytes.getShort(offset));
      offset += 2;
      if (length == 0 || length > end - offset || (bytes.get(offset) & 31) != type) {
        throw invalid();
      }
      return offset + length;
    }

    /** 读取AAC-LC解码声明, 拒绝仅把任意音频条目命名为mp4a的伪装. */
    private void readAudioConfiguration(Box box) {
      require(box, 6);
      if (unsigned(box.start()) != 0) {
        throw invalid();
      }
      Box stream = descriptor(box.start() + 4, box.end(), 3);
      require(stream, 3);
      if (bytes.get(stream.start() + 2) != 0) {
        throw invalid();
      }
      Box decoder = descriptor(stream.start() + 3, stream.end(), 4);
      require(decoder, 13);
      if (Byte.toUnsignedInt(bytes.get(decoder.start())) != 0x40
          || ((Byte.toUnsignedInt(bytes.get(decoder.start() + 1)) >> 2) & 63) != 5) {
        throw invalid();
      }
      Box specific = descriptor(decoder.start() + 13, decoder.end(), 5);
      require(specific, 2);
      int first = Byte.toUnsignedInt(bytes.get(specific.start()));
      int second = Byte.toUnsignedInt(bytes.get(specific.start() + 1));
      int frequency = ((first & 7) << 1) | (second >> 7);
      int channels = (second >> 3) & 15;
      if ((first >> 3) != 2 || frequency > 12 || channels < 1 || channels > 2) {
        throw invalid();
      }
    }

    /** MPEG描述符长度最多四字节, 在读取内容前限制在所属描述符的边界内. */
    private Box descriptor(int offset, int end, int tag) {
      if (end - offset < 2 || Byte.toUnsignedInt(bytes.get(offset++)) != tag) {
        throw invalid();
      }
      int length = 0;
      for (int index = 0; index < 4 && offset < end; index++) {
        int value = Byte.toUnsignedInt(bytes.get(offset++));
        length = (length << 7) | (value & 127);
        if ((value & 128) == 0) {
          if (length > end - offset) {
            throw invalid();
          }
          return new Box("descriptor", offset, offset + length);
        }
      }
      throw invalid();
    }

    /** 同时检查电影和轨道时长, 拒绝未知时长及超过60秒的隐藏长轨道. */
    private void readDuration(Box box) {
      require(box, 20);
      int offset = box.start();
      int version = Byte.toUnsignedInt(bytes.get(offset));
      long scale;
      long duration;
      if (version == 0) {
        scale = unsigned(offset + 12);
        duration = unsigned(offset + 16);
      } else if (version == 1) {
        require(box, 32);
        scale = unsigned(offset + 20);
        duration = bytes.getLong(offset + 24);
      } else {
        throw invalid();
      }
      if (scale == 0 || duration <= 0 || duration > scale * 60L) {
        throw invalid();
      }
      if (box.type().equals("mvhd")) {
        durationMs = (int) ((duration * 1000L + scale - 1) / scale);
      }
    }
  }
}
