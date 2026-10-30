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

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.model.ObjectMetadata;
import com.platform.common.exception.BusinessException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * 阿里云存储
 *
 * @author hehailong
 * @since 2019-01-17 16:21:01
 */
public class AliyunCloudStorageService extends AbstractCloudStorageService {

    /** 释放本次上传创建的OSS连接池, 不保留闲置客户端. */
    @Override
    public void close() {
        client.shutdown();
    }

    private OSSClient client;

    /** 按传入的存储配置初始化当前云存储客户端. */
    public AliyunCloudStorageService(CloudStorageConfig config) {
        this.config = config;

        // 初始化
        init();
    }

    /** 初始化当前组件的运行配置或依赖资源. */
    private void init() {
        client =
                new OSSClient(
                        config.getAliyunEndPoint(),
                        new DefaultCredentialProvider(
                                config.getAliyunAccessKeyId(), config.getAliyunAccessKeySecret()),
                        null);
    }

    @Override
    public String upload(byte[] data, String path) {
        return upload(new ByteArrayInputStream(data), path);
    }

    @Override
    public String upload(InputStream inputStream, String path) {
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(contentType(path));
            client.putObject(config.getAliyunBucketName(), path, inputStream, metadata);
        } catch (Exception e) {
            throw new BusinessException("上传文件失败，请检查配置信息", e);
        }

        return config.getAliyunDomain() + "/" + path;
    }

    @Override
    public String uploadSuffix(byte[] data, String suffix) {
        return upload(data, getPath(config.getAliyunPrefix(), suffix));
    }

    @Override
    public String uploadSuffix(InputStream inputStream, String suffix) {
        return upload(inputStream, getPath(config.getAliyunPrefix(), suffix));
    }
}
