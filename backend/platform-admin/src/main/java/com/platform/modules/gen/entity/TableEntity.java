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

import java.util.List;

/**
 * 表数据
 *
 * @author hehailong
 * @email 939961241@qq.com
 * @since 2016年12月20日 上午12:02:55
 */
public class TableEntity {
    /** 表的名称 */
    private String tableName;

    /** 表的备注 */
    private String comments;

    /** 表的主键 */
    private ColumnEntity pk;

    /** 表的列名(不包含主键) */
    private List<ColumnEntity> columns;

    /** 类名(第一个字母大写)，如：sys_user => SysUser */
    private String className;

    /** 类名(第一个字母小写)，如：sys_user => sysUser */
    private String classname;

    /** 读取当前对象的TableName属性. */
    public String getTableName() {
        return tableName;
    }

    /** 更新当前对象的TableName属性. */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    /** 读取当前对象的Comments属性. */
    public String getComments() {
        return comments;
    }

    /** 更新当前对象的Comments属性. */
    public void setComments(String comments) {
        this.comments = comments;
    }

    /** 读取当前对象的Pk属性. */
    public ColumnEntity getPk() {
        return pk;
    }

    /** 更新当前对象的Pk属性. */
    public void setPk(ColumnEntity pk) {
        this.pk = pk;
    }

    /** 读取当前对象的Columns属性. */
    public List<ColumnEntity> getColumns() {
        return columns;
    }

    /** 更新当前对象的Columns属性. */
    public void setColumns(List<ColumnEntity> columns) {
        this.columns = columns;
    }

    /** 读取当前对象的ClassName属性. */
    public String getClassName() {
        return className;
    }

    /** 更新当前对象的ClassName属性. */
    public void setClassName(String className) {
        this.className = className;
    }

    /** 读取当前对象的Classname属性. */
    public String getClassname() {
        return classname;
    }

    /** 更新当前对象的Classname属性. */
    public void setClassname(String classname) {
        this.classname = classname;
    }
}
