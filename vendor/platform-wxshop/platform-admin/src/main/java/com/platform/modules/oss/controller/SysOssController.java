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
package com.platform.modules.oss.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.platform.common.annotation.SysLog;
import com.platform.common.exception.BusinessException;
import com.platform.common.utils.Constant;
import com.platform.common.utils.RestResponse;
import com.platform.common.utils.SafeImageUpload;
import com.platform.common.validator.ValidatorUtils;
import com.platform.common.validator.group.*;
import com.platform.modules.oss.cloud.CloudStorageConfig;
import com.platform.modules.oss.cloud.UploadFactory;
import com.platform.modules.oss.entity.SysOssEntity;
import com.platform.modules.oss.service.SysOssService;
import com.platform.modules.sys.controller.AbstractController;
import com.platform.modules.sys.entity.SysUserEntity;
import com.platform.modules.sys.service.SysConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传
 *
 * @author 何海龙
 * @since 2019-01-17 16:21:01
 */
@Tag(name = "SysOssController|文件上传")
@RestController
@RequestMapping("sys/oss")
@RequiredArgsConstructor
public class SysOssController extends AbstractController {

  private final SysOssService sysOssService;
  private final SysConfigService sysConfigService;

  /**
   * 分页查询
   *
   * @param params 查询参数
   * @return RestResponse
   */
  @Operation(summary = "分页查询", description = "分页查询")
  @GetMapping("/list")
  public RestResponse<Page<SysOssEntity>> list(@RequestParam Map<String, Object> params) {
    // 如需数据权限，在参数中添加DataScope
    params.put("dataScope", getDataScope());
    Page<SysOssEntity> page = sysOssService.queryPage(params);

    return RestResponse.ok(page);
  }

  /**
   * 云存储配置信息
   *
   * @return RestResponse
   */
  @Operation(summary = "云存储配置信息", description = "云存储配置信息")
  @GetMapping("/config")
  @RequiresPermissions("sys:oss:config")
  public RestResponse<CloudStorageConfig> config() {
    CloudStorageConfig config =
        sysConfigService.getConfigObject(
            Constant.CLOUD_STORAGE_CONFIG_KEY, CloudStorageConfig.class);

    return RestResponse.ok(config);
  }

  /**
   * 修改云存储配置信息
   *
   * @param config config
   * @return RestResponse
   */
  @Operation(summary = "修改云存储配置信息", description = "修改云存储配置信息")
  @SysLog("修改云存储配置信息")
  @PostMapping("/saveConfig")
  @RequiresPermissions("sys:oss:config")
  public RestResponse<String> saveConfig(@RequestBody CloudStorageConfig config) {
    // 校验类型
    ValidatorUtils.validateEntity(config);

    if (config.getType() == Constant.CloudService.QINIU.getValue()) {
      // 校验七牛数据
      ValidatorUtils.validateEntity(config, QiniuGroup.class);
    } else if (config.getType() == Constant.CloudService.ALIYUN.getValue()) {
      // 校验阿里云数据
      ValidatorUtils.validateEntity(config, AliyunGroup.class);
    } else if (config.getType() == Constant.CloudService.QCLOUD.getValue()) {
      // 校验腾讯云数据
      ValidatorUtils.validateEntity(config, QcloudGroup.class);
    } else if (config.getType() == Constant.CloudService.DISCK.getValue()) {
      // 校验腾讯云数据
      ValidatorUtils.validateEntity(config, DiskGroup.class);
    } else if (config.getType() == Constant.CloudService.MINIO.getValue()) {
      // 校验MINIO数据
      ValidatorUtils.validateEntity(config, MinioGroup.class);
    } else if (config.getType() == Constant.CloudService.HUAWEI.getValue()) {
      // 华为云OBS数据
      ValidatorUtils.validateEntity(config, HuaweiGroup.class);
    }

    sysConfigService.updateValueByKey(
        Constant.CLOUD_STORAGE_CONFIG_KEY, JSONObject.toJSONString(config));

    return RestResponse.ok();
  }

  /**
   * 上传文件
   *
   * @param file file
   * @return RestResponse
   */
  @Operation(summary = "上传文件", description = "上传文件")
  @PostMapping("/upload")
  @RequiresPermissions("sys:oss:list")
  public RestResponse<Map<String, Object>> upload(
      @RequestParam(value = "file", required = false) MultipartFile file) throws Exception {
    if (null == file || file.isEmpty()) {
      throw new BusinessException("上传文件不能为空");
    }

    // 上传文件
    String suffix = ".png";
    String url =
        Objects.requireNonNull(UploadFactory.build())
            .uploadSuffix(SafeImageUpload.readPng(file), suffix);

    String suffixLowerCase = suffix.toLowerCase();
    String p12 = ".p12";
    if (!suffixLowerCase.contains(p12)) {
      // 保存文件信息
      SysUserEntity user = getUser();
      SysOssEntity ossEntity = new SysOssEntity();
      ossEntity.setUrl(url);
      ossEntity.setCreateDate(new Date());
      ossEntity.setCreateUserId(user.getUserId());
      ossEntity.setCreateUserOrgNo(user.getOrgNo());
      sysOssService.save(ossEntity);
    }
    // 返回上传结果, 保留既有客户端字段.
    Map<String, Object> result = new HashMap<>(8);
    result.put("url", url);
    result.put("state", "SUCCESS");
    result.put("title", url);
    result.put("original", url);
    return RestResponse.ok(result);
  }

  /**
   * 删除文件上传记录
   *
   * @param ids ids
   * @return RestResponse
   */
  @Operation(summary = "删除文件上传记录", description = "删除文件上传记录")
  @SysLog("删除文件上传记录")
  @PostMapping("/delete")
  @RequiresPermissions("sys:oss:delete")
  public RestResponse<String> delete(@RequestBody String[] ids) {
    sysOssService.removeByIds(Arrays.asList(ids));

    return RestResponse.ok();
  }
}
