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
package com.platform.modules.wx.service.impl;

import com.platform.modules.wx.service.WxUserService;
import com.platform.modules.wx.service.WxUserTagsService;
import com.platform.config.WxMpProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.tag.WxUserTag;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Collections;

/**
 * @author 何海龙
 */
@Service
@CacheConfig(cacheNames = {"wxUserTagsServiceCache"})
@Slf4j
@RequiredArgsConstructor
public class WxUserTagsServiceImpl implements WxUserTagsService {

    private final WxMpService wxMpService;
    private final WxUserService wxUserService;
    private final WxMpProperties wxMpProperties;

    public static final String CACHE_KEY = "'WX_USER_TAGS'";

    @Override
    @Cacheable(key = CACHE_KEY)
    public List<WxUserTag> getWxTags() throws WxErrorException {
        if (!isMpConfigured()) {
            // 未启用公众号时保持后台可登录 / Keep admin login available without MP settings.
            return Collections.emptyList();
        }
        log.info("拉取公众号用户标签");
        return wxMpService.getUserTagService().tagGet();
    }

    /**
     * 判断公众号 AppId 与密钥是否已配置。
     *
     * @return 已配置返回 true
     */
    private boolean isMpConfigured() {
        return wxMpProperties.getAppId() != null && !wxMpProperties.getAppId().trim().isEmpty()
                && wxMpProperties.getSecret() != null && !wxMpProperties.getSecret().trim().isEmpty();
    }

    @Override
    @CacheEvict(key = CACHE_KEY)
    public void creatTag(String name) throws WxErrorException {
        wxMpService.getUserTagService().tagCreate(name);
    }

    @Override
    @CacheEvict(key = CACHE_KEY)
    public void updateTag(Long tagid, String name) throws WxErrorException {
        wxMpService.getUserTagService().tagUpdate(tagid, name);
    }

    @Override
    @CacheEvict(key = CACHE_KEY)
    public void deleteTag(Long tagid) throws WxErrorException {
        wxMpService.getUserTagService().tagDelete(tagid);
    }

    @Override
    public void batchTagging(Long tagid, String[] openidList) throws WxErrorException {
        wxMpService.getUserTagService().batchTagging(tagid, openidList);
        //标签更新后更新对应用户信息
        wxUserService.refreshUserInfoAsync(openidList);
    }

    @Override
    public void batchUnTagging(Long tagid, String[] openidList) throws WxErrorException {
        wxMpService.getUserTagService().batchUntagging(tagid, openidList);
        //标签更新后更新对应用户信息
        wxUserService.refreshUserInfoAsync(openidList);
    }

    @Override
    public void tagging(Long tagid, String openid) throws WxErrorException {
        wxMpService.getUserTagService().batchTagging(tagid, new String[]{openid});
        wxUserService.refreshUserInfo(openid);
    }

    @Override
    public void untagging(Long tagid, String openid) throws WxErrorException {
        wxMpService.getUserTagService().batchUntagging(tagid, new String[]{openid});
        wxUserService.refreshUserInfo(openid);
    }

}
