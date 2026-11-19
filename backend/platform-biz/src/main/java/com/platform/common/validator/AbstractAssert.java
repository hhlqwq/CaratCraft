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
package com.platform.common.validator;

import com.platform.common.exception.BusinessException;

import org.apache.commons.lang.StringUtils;

/**
 * 数据校验
 *
 * @author hehailong
 */
public abstract class AbstractAssert {

    /** 判断文本是否为空或仅包含空白. */
    public static void isBlank(String str, String message) {
        if (StringUtils.isBlank(str)) {
            throw new BusinessException(message);
        }
    }

    /** 判断当前值是否为空或符合此工具定义的空值条件. */
    public static void isNull(Object object, String message) {
        if (object == null) {
            throw new BusinessException(message);
        }
    }
}
