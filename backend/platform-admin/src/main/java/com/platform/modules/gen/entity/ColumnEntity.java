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
package com.platform.modules.gen.entity;

/**
 * 列的属性
 *
 * @author hehailong
 * @email 939961241@qq.com
 * @since 2016年12月20日 上午12:01:45
 */
public class ColumnEntity {
    /** 列名 */
    private String columnName;

    /** 列名类型 */
    private String dataType;

    /** 列名备注 */
    private String comments;

    /** 属性名称(第一个字母大写)，如：user_name => UserName */
    private String attrName;

    /** 属性名称(第一个字母小写)，如：user_name => userName */
    private String attrname;

    private String columnKey;

    /** 属性类型 */
    private String attrType;

    /** auto_increment */
    private String extra;

    /** 属性的get方法 */
    private String getMethod;

    /** 读取当前对象的GetMethod属性. */
    public String getGetMethod() {
        return getMethod;
    }

    /** 更新当前对象的GetMethod属性. */
    public void setGetMethod(String getMethod) {
        this.getMethod = getMethod;
    }

    /** 读取当前对象的ColumnName属性. */
    public String getColumnName() {
        return columnName;
    }

    /** 更新当前对象的ColumnName属性. */
    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    /** 读取当前对象的DataType属性. */
    public String getDataType() {
        return dataType;
    }

    /** 更新当前对象的DataType属性. */
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    /** 读取当前对象的Comments属性. */
    public String getComments() {
        return comments;
    }

    /** 更新当前对象的Comments属性. */
    public void setComments(String comments) {
        this.comments = comments;
    }

    /** 读取当前对象的Attrname属性. */
    public String getAttrname() {
        return attrname;
    }

    /** 更新当前对象的Attrname属性. */
    public void setAttrname(String attrname) {
        this.attrname = attrname;
    }

    /** 读取当前对象的AttrName属性. */
    public String getAttrName() {
        return attrName;
    }

    /** 更新当前对象的AttrName属性. */
    public void setAttrName(String attrName) {
        this.attrName = attrName;
    }

    /** 读取当前对象的AttrType属性. */
    public String getAttrType() {
        return attrType;
    }

    /** 更新当前对象的AttrType属性. */
    public void setAttrType(String attrType) {
        this.attrType = attrType;
    }

    /** 读取当前对象的Extra属性. */
    public String getExtra() {
        return extra;
    }

    /** 更新当前对象的Extra属性. */
    public void setExtra(String extra) {
        this.extra = extra;
    }

    /** 读取当前对象的ColumnKey属性. */
    public String getColumnKey() {
        return columnKey;
    }

    /** 更新当前对象的ColumnKey属性. */
    public void setColumnKey(String columnKey) {
        this.columnKey = columnKey;
    }
}
