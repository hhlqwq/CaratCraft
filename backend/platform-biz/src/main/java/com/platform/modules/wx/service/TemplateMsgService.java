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
package com.platform.modules.wx.service;

import com.platform.modules.wx.form.TemplateMsgBatchForm;

import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;

/**
 * @author 何海龙
 */
public interface TemplateMsgService {
    /**
     * 发送微信模版消息
     *
     * @param msg
     */
    void sendTemplateMsg(WxMpTemplateMessage msg);

    /**
     * 批量消息发送
     *
     * @param form
     */
    void sendMsgBatch(TemplateMsgBatchForm form);
}
