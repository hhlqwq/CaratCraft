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

import com.platform.common.exception.BusinessException;
import com.platform.common.utils.Constant;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

/**
 * 服务器存储
 *
 * @author 何海龙
 * @since 2019-01-17 16:21:01
 */
public class DiskCloudStorageService extends AbstractCloudStorageService {

    /** 读取商家文件存储配置, 目录在实际上传时创建. */
    public DiskCloudStorageService(CloudStorageConfig config) {
        this.config = config;
    }

    /** 将随机命名文件写入配置目录内, 支持视频子目录且不覆盖现有文件. */
    @Override
    public String upload(byte[] data, String path) {
        if (data == null || data.length < Constant.THREE || path == null || path.isEmpty()) {
            throw new BusinessException("上传文件为空");
        }
        // 本地存储必需要以"/"开头
        if (!path.startsWith(Constant.SLASH)) {
            path = "/" + path;
        }
        try {
            File root = new File(config.getDiskPath()).getCanonicalFile();
            File target = new File(root, path.substring(1)).getCanonicalFile();
            if (!target.toPath().startsWith(root.toPath()) || target.equals(root)) {
                throw new BusinessException("上传路径越界");
            }
            Files.createDirectories(target.toPath().getParent());
            Files.write(
                    target.toPath(), data, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        } catch (IOException error) {
            throw new BusinessException("上传文件失败", error);
        }

        return config.getProxyServer() + path;
    }

    @Override
    public String upload(InputStream inputStream, String path) {
        try {
            byte[] data = IOUtils.toByteArray(inputStream);
            return this.upload(data, path);
        } catch (IOException e) {
            throw new BusinessException("上传文件失败", e);
        }
    }

    @Override
    public String uploadSuffix(byte[] data, String suffix) {
        return upload(data, getPath("", suffix));
    }

    @Override
    public String uploadSuffix(InputStream inputStream, String suffix) {
        return upload(inputStream, getPath("", suffix));
    }
}
