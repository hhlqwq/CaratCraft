package com.platform.modules.community.controller;

import com.platform.common.utils.RestResponse;
import com.platform.datascope.StaffAuthority;
import com.platform.datascope.StaffAuthority.Resource;
import com.platform.modules.community.CommunityService;
import com.platform.modules.sys.controller.AbstractController;

import lombok.RequiredArgsConstructor;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 社区运营权限与商城评价权限独立, 审核必须记录操作者和原因. */
@RestController
@RequiredArgsConstructor
@RequestMapping("community/posts")
public class CommunityController extends AbstractController {
    private final CommunityService community;
    private final StaffAuthority authority;

    /** 按审核状态分页查询待处理内容. */
    @GetMapping("list")
    @RequiresPermissions("community:post:list")
    public RestResponse<Object> list(
            @RequestParam(defaultValue = "PENDING") String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String todo) {
        var scope = authority.scope("community:post:list");
        if ("communityReview".equals(todo)) {
            scope = scope.intersect(authority.scope("community:post:moderate"));
            status = "PENDING";
        }
        return RestResponse.ok(community.adminPage(status, page, limit, scope));
    }

    /** 审核、隐藏及删除统一执行版本检查. */
    @PostMapping("moderate")
    @RequiresPermissions("community:post:moderate")
    public RestResponse<Object> moderate(
            @RequestParam Long id,
            @RequestParam Long version,
            @RequestParam String status,
            @RequestParam String reason) {
        authority.require("community:post:moderate", Resource.COMMUNITY, id);
        community.moderate(
                id, version, status, reason, getUserId() + ":" + getUser().getRealName());
        return RestResponse.ok();
    }

    /** 查看审核日志需要社区查看权限. */
    @GetMapping("logs")
    @RequiresPermissions("community:post:list")
    public RestResponse<Object> logs(@RequestParam Long id) {
        authority.require("community:post:list", Resource.COMMUNITY, id);
        return RestResponse.ok(community.logs(id));
    }
}
