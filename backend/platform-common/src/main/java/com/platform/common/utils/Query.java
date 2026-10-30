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
package com.platform.common.utils;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 查询参数
 *
 * @author hehailong
 */
public class Query<T> extends LinkedHashMap<String, Object> {
    private static final long serialVersionUID = 1L;

    /** mybatis-plus分页参数 */
    private Page<T> page;

    /** 当前页码 */
    private int currPage = 1;

    /** 每页条数 */
    private int limit = 10;

    private static final String ASC = "asc";

    /** 解析分页及排序参数并构建当前查询上下文. */
    public Query(Map<String, Object> params) {

        String strPage = "page";
        String strLimit = "limit";
        this.putAll(params);

        // 分页参数
        if (params.get(strPage) != null) {
            currPage = Integer.parseInt(params.get("page").toString());
        }
        if (params.get(strLimit) != null) {
            limit = Integer.parseInt(params.get("limit").toString());
        }

        this.put("offset", (currPage - 1) * limit);
        this.put("page", currPage);
        this.put("limit", limit);

        String sidx = (String) params.get("sidx");
        // 默认升序
        Boolean asc = true;
        if (!StringUtils.isNullOrEmpty(params.get(ASC))) {
            asc = (Boolean) params.get("asc");
        }
        // mybatis-plus分页
        this.page = new Page<>(currPage, limit);

        // 排序
        if (StringUtils.isNotBlank(sidx)) {
            if (asc) {
                this.page.addOrder(OrderItem.asc(sidx));
            } else {
                this.page.addOrder(OrderItem.desc(sidx));
            }
        }
    }

    /** 读取当前对象的Page属性. */
    public Page<T> getPage() {
        return page;
    }

    /** 读取当前对象的CurrPage属性. */
    public int getCurrPage() {
        return currPage;
    }

    /** 读取当前对象的Limit属性. */
    public int getLimit() {
        return limit;
    }
}
