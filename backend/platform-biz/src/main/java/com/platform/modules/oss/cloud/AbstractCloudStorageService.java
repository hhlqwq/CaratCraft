/*
 *
 *      Copyright (c) 2018-2099, hehailong All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 *  this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in the
 *  documentation and/or other materials provided with the distribution.
 *  Neither the name of the openwtai.com developer nor the names of its
 *  contributors may be used to endorse or promote products derived from
 *  this software without specific prior written permission.
 *  Author: hehailong(845367567@qq.com)
 *
 */
package com.platform.modules.oss.cloud;

import com.platform.common.utils.Constant;
import com.platform.common.utils.DateUtils;

import org.apache.commons.lang.StringUtils;

import java.io.InputStream;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

/**
 * 云存储(支持七牛、阿里云、腾讯云、MINIO、华为云)
 *
 * @author hehailong
 * @since 2019-01-17 16:21:01
 */
public abstract class AbstractCloudStorageService implements AutoCloseable {
    /** 云存储配置信息 */
    CloudStorageConfig config;

    /** 默认没有独占客户端资源, 持有独立连接池的实现负责重写并释放. */
    @Override
    public void close() {}

    /**
     * 文件路径
     *
     * @param prefix 前缀
     * @param suffix 后缀
     * @return 返回上传路径
     */
    public String getPath(String prefix, String suffix) {
        // 生成uuid
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        // 文件路径
        String path = DateUtils.format(new Date(), "yyyyMMdd") + "/" + uuid;

        if (StringUtils.isNotBlank(prefix)) {
            path = prefix + "/" + path;
        }

        return path + suffix;
    }

    /** 视频保留商家配置的云存储前缀, 在其下增加独立video目录. */
    public String getVideoPath() {
        String prefix = "";
        Integer type = config.getType();
        if (Integer.valueOf(Constant.CloudService.QINIU.getValue()).equals(type)) {
            prefix = config.getQiniuPrefix();
        } else if (Integer.valueOf(Constant.CloudService.ALIYUN.getValue()).equals(type)) {
            prefix = config.getAliyunPrefix();
        } else if (Integer.valueOf(Constant.CloudService.QCLOUD.getValue()).equals(type)) {
            prefix = config.getQcloudPrefix();
        } else if (Integer.valueOf(Constant.CloudService.HUAWEI.getValue()).equals(type)) {
            prefix = config.getHuaweiPrefix();
        }
        return getPath(StringUtils.isBlank(prefix) ? "video" : prefix + "/video", ".mp4");
    }

    /** 已验证媒体按实际输出后缀设置MIME, 不再把视频或PNG硬编码成JPEG. */
    protected String contentType(String path) {
        String lower = path.toLowerCase(Locale.ROOT);
        return switch (lower.substring(lower.lastIndexOf('.') + 1)) {
            case "mp4" -> "video/mp4";
            case "png" -> "image/png";
            case "jpg", "jpeg" -> "image/jpeg";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            default -> "application/octet-stream";
        };
    }

    /**
     * 文件上传
     *
     * @param data 文件字节数组
     * @param path 文件路径，包含文件名
     * @return 返回http地址
     */
    public abstract String upload(byte[] data, String path);

    /**
     * 文件上传
     *
     * @param data 文件字节数组
     * @param suffix 后缀
     * @return 返回http地址
     */
    public abstract String uploadSuffix(byte[] data, String suffix);

    /**
     * 文件上传
     *
     * @param inputStream 字节流
     * @param path 文件路径，包含文件名
     * @return 返回http地址
     */
    public abstract String upload(InputStream inputStream, String path);

    /**
     * 文件上传
     *
     * @param inputStream 字节流
     * @param suffix 后缀
     * @return 返回http地址
     */
    public abstract String uploadSuffix(InputStream inputStream, String suffix);
}
