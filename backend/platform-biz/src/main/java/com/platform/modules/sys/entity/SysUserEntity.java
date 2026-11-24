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
package com.platform.modules.sys.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.platform.common.validator.group.AddGroup;
import com.platform.common.validator.group.UpdateGroup;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 系统用户
 *
 * @author hehailong
 */
@Data
@TableName("SYS_USER")
public class SysUserEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 用户ID */
    @TableId private String userId;

    /** 用户性别 */
    private Integer sex;

    /** 用户真实姓名 */
    @NotBlank(
            message = "真实姓名不能为空",
            groups = {AddGroup.class, UpdateGroup.class})
    private String realName;

    /** 用户名 */
    @NotBlank(
            message = "用户名不能为空",
            groups = {AddGroup.class, UpdateGroup.class})
    private String userName;

    /** 机构编码 */
    private String orgNo;

    /** 账号层级仅由服务端开户流程赋值. */
    @com.fasterxml.jackson.annotation.JsonProperty(
            access = com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY)
    private String accountType;

    /** 密码 */
    @com.fasterxml.jackson.annotation.JsonProperty(
            access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
    private String password;

    /** 盐 */
    @com.fasterxml.jackson.annotation.JsonIgnore private String salt;

    /** 邮箱服务器地址 */
    private String emailHost;

    /** 邮箱 */
    @Email(
            message = "邮箱格式不正确",
            groups = {AddGroup.class, UpdateGroup.class})
    private String email;

    /** 邮箱密码 */
    @com.fasterxml.jackson.annotation.JsonIgnore private String emailPw;

    /** 手机号 */
    private String mobile;

    /** 状态 0：禁用 1：正常 */
    private Integer status;

    /** 最近一次成功建立管理端会话的时间. */
    private Date lastLoginAt;

    /** 初始或管理员重置密码必须由员工本人修改. */
    @com.fasterxml.jackson.annotation.JsonIgnore private Boolean passwordChangeRequired;

    /** 平台引导和商家管理员临时密码的有效期. */
    @com.fasterxml.jackson.annotation.JsonIgnore private Date temporaryPasswordExpiresAt;

    /** 为指定员工单独选择的经营权限菜单节点. */
    @TableField(exist = false)
    private List<String> permissionMenuIds;

    /** 角色ID列表 */
    @TableField(exist = false)
    private List<String> roleIdList;

    /** 属于当前工作室的可分配岗位显示名. */
    @TableField(exist = false)
    private String roleNames;

    /** 创建者ID */
    private String createUserId;

    /** 创建者所属机构 */
    private String createUserOrgNo;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
}
