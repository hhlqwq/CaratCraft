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
import com.platform.common.utils.SpringContextUtils;
import com.platform.modules.sys.service.SysConfigService;

/**
 * 文件上传Factory
 *
 * @author 何海龙
 * @since 2019-01-17 16:21:01
 */
public final class UploadFactory {
    private static SysConfigService sysConfigService;

    static {
        UploadFactory.sysConfigService =
                (SysConfigService) SpringContextUtils.getBean("sysConfigService");
    }

    /** 按商家配置创建独立存储访问对象, 调用方负责在完成后关闭. */
    public static AbstractCloudStorageService build() {
        // 获取云存储配置信息
        CloudStorageConfig config =
                sysConfigService.getConfigObject(
                        Constant.CLOUD_STORAGE_CONFIG_KEY, CloudStorageConfig.class);

        if (config == null || config.getType() == null) {
            throw new BusinessException("请先配置文件存储类型");
        }

        if (config.getType() == Constant.CloudService.QINIU.getValue()) {
            return new QiniuCloudStorageService(config);
        } else if (config.getType() == Constant.CloudService.ALIYUN.getValue()) {
            return new AliyunCloudStorageService(config);
        } else if (config.getType() == Constant.CloudService.QCLOUD.getValue()) {
            return new QcloudCloudStorageService(config);
        } else if (config.getType() == Constant.CloudService.DISCK.getValue()) {
            return new DiskCloudStorageService(config);
        } else if (config.getType() == Constant.CloudService.MINIO.getValue()) {
            return new MinioCloudStorageService(config);
        } else if (config.getType() == Constant.CloudService.HUAWEI.getValue()) {
            return new HuaWeiCloudStorageService(config);
        }
        throw new BusinessException("文件存储类型不受支持");
    }

    /** 保存已完成安全重编码的PNG, 无论成功或失败都释放独占存储客户端. */
    public static String uploadPng(byte[] png) {
        try (var storage = build()) {
            return storage.uploadSuffix(png, ".png");
        }
    }
}
