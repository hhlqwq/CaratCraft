package com.platform.modules.oss.cloud;

import com.obs.services.ObsClient;
import com.obs.services.model.ObjectMetadata;
import com.obs.services.model.PutObjectResult;
import com.platform.common.exception.BusinessException;
import com.platform.common.utils.Constant;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 华为OBS存储
 *
 * @author 何海龙
 * @email 939961241@qq.com
 * @date 2022-03-06 16:21:01
 */
@Slf4j
public class HuaWeiCloudStorageService extends AbstractCloudStorageService {

    /** 释放本次上传创建的OBS连接, 关闭失败记录日志但不掩盖原始上传结果. */
    @Override
    public void close() {
        try {
            obsClient.close();
        } catch (IOException error) {
            log.warn("关闭OBS上传连接失败", error);
        }
    }

    private ObsClient obsClient;

    public HuaWeiCloudStorageService(CloudStorageConfig config) {
        this.config = config;
        // 初始化
        init();
    }

    private void init() {
        obsClient =
                new ObsClient(
                        config.getHuaweiAccessKey(),
                        config.getHuaweiSecretKey(),
                        config.getHuaweiEndPoint());
    }

    @Override
    public String upload(byte[] data, String path) {
        if (data.length < Constant.THREE || Constant.BLANK.equals(path)) {
            throw new BusinessException("上传文件为空");
        }

        try {

            ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(contentType(path));
            PutObjectResult result =
                    obsClient.putObject(config.getHuaweiBucketName(), path, inputStream, metadata);

            return "https://"
                    + config.getHuaweiBucketName()
                    + "."
                    + config.getHuaweiEndPoint()
                    + "/"
                    + result.getObjectKey();
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException("上传文件失败", e);
        }
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
        return upload(data, getPath(config.getHuaweiPrefix(), suffix));
    }

    @Override
    public String uploadSuffix(InputStream inputStream, String suffix) {
        return upload(inputStream, getPath(config.getHuaweiPrefix(), suffix));
    }
}
