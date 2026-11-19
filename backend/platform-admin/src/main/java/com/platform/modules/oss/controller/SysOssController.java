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
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.platform.common.annotation.SysLog;
import com.platform.common.exception.BusinessException;
import com.platform.common.utils.Constant;
import com.platform.common.utils.RestResponse;
import com.platform.common.utils.SafeImageUpload;
import com.platform.common.validator.ValidatorUtils;
import com.platform.common.validator.group.AliyunGroup;
import com.platform.common.validator.group.DiskGroup;
import com.platform.common.validator.group.HuaweiGroup;
import com.platform.common.validator.group.MinioGroup;
import com.platform.common.validator.group.QcloudGroup;
import com.platform.common.validator.group.QiniuGroup;
import com.platform.datascope.StaffAuthority;
import com.platform.modules.oss.cloud.CloudStorageConfig;
import com.platform.modules.oss.cloud.UploadFactory;
import com.platform.modules.oss.entity.SysOssEntity;
import com.platform.modules.oss.service.SysOssService;
import com.platform.modules.sys.controller.AbstractController;
import com.platform.modules.sys.entity.SysUserEntity;
import com.platform.modules.sys.service.OrgActionScope;
import com.platform.modules.sys.service.SysConfigService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件上传
 *
 * @author hehailong
 * @since 2019-01-17 16:21:01
 */
@Tag(name = "SysOssController|文件上传")
@RestController
@RequestMapping("sys/oss")
@RequiredArgsConstructor
public class SysOssController extends AbstractController {

    private final SysOssService sysOssService;
    private final SysConfigService sysConfigService;
    private final StaffAuthority authority;

    /**
     * 分页查询
     *
     * @param params 查询参数
     * @return RestResponse
     */
    @Operation(summary = "分页查询", description = "分页查询")
    @GetMapping("/list")
    @RequiresPermissions(
            value = {"sys:oss:list", "mall:goods:list"},
            logical = Logical.OR)
    public RestResponse<Page<SysOssEntity>> list(@RequestParam Map<String, Object> params) {
        var scope = assetScope("sys:oss:list", "mall:goods:list");
        params.remove("dataScope");
        params.put("ossOrgNos", scope.emergency() ? null : scope.organizations());
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
    @RequiresPermissions(
            value = {"sys:oss:list", "mall:goods:save", "mall:goods:update"},
            logical = Logical.OR)
    public RestResponse<Map<String, Object>> upload(
            @RequestParam(value = "file", required = false) MultipartFile file) throws Exception {
        if (null == file || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }

        SysUserEntity user = getUser();
        var scope = assetScope("sys:oss:list", "mall:goods:save", "mall:goods:update");
        String organization = scope.permits(user.getOrgNo()) ? user.getOrgNo() : null;
        if (!scope.emergency() && organization == null) {
            if (scope.organizations().size() != 1) {
                throw new UnauthorizedException("无法确定图片上传的业务机构");
            }
            organization = scope.organizations().get(0);
        }
        String url = UploadFactory.uploadPng(SafeImageUpload.readPng(file));
        SysOssEntity ossEntity = new SysOssEntity();
        ossEntity.setUrl(url);
        ossEntity.setCreateDate(new Date());
        ossEntity.setCreateUserId(user.getUserId());
        ossEntity.setCreateUserOrgNo(organization);
        sysOssService.save(ossEntity);
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
        var scope = authority.scope("sys:oss:delete");
        List<String> requested = Arrays.stream(ids).distinct().toList();
        if (!scope.emergency()
                && !requested.isEmpty()
                && sysOssService.count(
                                new QueryWrapper<SysOssEntity>()
                                        .in("ID", requested)
                                        .apply(
                                                scope.mybatisCondition("CREATE_USER_ORG_NO"),
                                                scope.organizations().toArray()))
                        != requested.size()) {
            throw new UnauthorizedException("图片记录超出当前操作范围");
        }
        sysOssService.removeByIds(Arrays.asList(ids));

        return RestResponse.ok();
    }

    /** 组合可执行同一图片动作的权限, 每个来源均独立核对机构. */
    private OrgActionScope assetScope(String... actions) {
        return new OrgActionScope(
                authority.emergency(),
                Arrays.stream(actions)
                        .flatMap(action -> authority.scope(action).organizations().stream())
                        .distinct()
                        .toList());
    }
}
