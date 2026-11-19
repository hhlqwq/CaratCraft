package com.platform.modules.mall.service;

import com.platform.common.utils.ProductVideoUpload;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.Map;

/** 将上传配额和已完成媒体记录分开处理, 失败上传也占用本小时配额. */
@Service
public class ProductVideoService {
    private final JdbcTemplate jdbc;

    /** 注入媒体台账, 存储实现由控制器按现有商家配置选择. */
    public ProductVideoService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** 锁定当前操作者以串行计算配额, 每小时最多发起20次商品视频上传. */
    @Transactional(rollbackFor = Exception.class)
    public long reserve(String operator) {
        if (jdbc.queryForList("SELECT USER_ID FROM SYS_USER WHERE USER_ID=? FOR UPDATE", operator)
                .isEmpty()) {
            throw new IllegalArgumentException("当前操作者不存在");
        }
        Integer count =
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM CC_PRODUCT_VIDEO WHERE operator_id=? AND"
                                + " create_time>=DATE_SUB(NOW(),INTERVAL 1 HOUR)",
                        Integer.class,
                        operator);
        if (count != null && count >= 20) {
            throw new IllegalArgumentException("本小时视频上传次数已达上限, 请稍后再试");
        }
        jdbc.update("INSERT INTO CC_PRODUCT_VIDEO(operator_id) VALUES(?)", operator);
        Long id = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        if (id == null) {
            throw new IllegalStateException("未生成视频上传标识");
        }
        return id;
    }

    /** 只有本次操作者可以完成自己的预留记录, 地址与参数不接受客户端回填. */
    public Map<String, Object> complete(
            String operator, long id, String url, ProductVideoUpload.Video video) {
        validateUrl(url);
        int updated =
                jdbc.update(
                        "UPDATE CC_PRODUCT_VIDEO SET"
                                + " url=?,duration_ms=?,width=?,height=?,byte_size=? WHERE id=? AND"
                                + " operator_id=? AND url=''",
                        url,
                        video.durationMs(),
                        video.width(),
                        video.height(),
                        video.bytes().length,
                        id,
                        operator);
        if (updated != 1) {
            throw new IllegalStateException("视频上传记录状态已变化");
        }
        return Map.of(
                "id",
                id,
                "url",
                url,
                "durationMs",
                video.durationMs(),
                "width",
                video.width(),
                "height",
                video.height());
    }

    /** 只发布静态MP4路径或明确的HTTP媒体地址, 不允许活动协议和超长存储地址. */
    private static void validateUrl(String value) {
        if (value == null || value.length() > 1000) {
            throw new IllegalArgumentException("视频存储地址无效");
        }
        URI uri = URI.create(value);
        boolean local = uri.getScheme() == null && value.startsWith("/upload/video/");
        boolean remote =
                ("https".equalsIgnoreCase(uri.getScheme())
                                || "http".equalsIgnoreCase(uri.getScheme()))
                        && uri.getHost() != null
                        && uri.getUserInfo() == null;
        if ((!local && !remote)
                || uri.getPath() == null
                || !uri.getPath().endsWith(".mp4")
                || uri.getFragment() != null) {
            throw new IllegalArgumentException("视频存储地址无效");
        }
    }
}
