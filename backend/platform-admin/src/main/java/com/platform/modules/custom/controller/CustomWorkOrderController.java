package com.platform.modules.custom.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.platform.common.utils.RestResponse;
import com.platform.modules.custom.dao.ProductionWorkOrderDao;
import com.platform.modules.custom.dao.WorkOrderLogDao;
import com.platform.modules.custom.entity.ProductionWorkOrderEntity;
import com.platform.modules.custom.entity.WorkOrderLogEntity;
import com.platform.modules.sys.controller.AbstractController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 定制生产工单管理 / Custom production work order administration. */
@RestController
@RequiredArgsConstructor
@RequestMapping("custom/work-order")
@Tag(name = "CustomWorkOrderController|定制生产工单")
public class CustomWorkOrderController extends AbstractController {
    private final ProductionWorkOrderDao workOrderDao;
    private final WorkOrderLogDao workOrderLogDao;
    private final com.platform.modules.custom.service.ProductionWorkflowService workflowService;

    /** 查询工单列表 / Lists production work orders. */
    @Operation(summary = "查询定制生产工单")
    @GetMapping("list")
    @RequiresPermissions("custom:work-order:list")
    public RestResponse<Object> list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        LambdaQueryWrapper<ProductionWorkOrderEntity> wrapper =
                new LambdaQueryWrapper<ProductionWorkOrderEntity>()
                        .eq(
                                status != null && !status.isBlank(),
                                ProductionWorkOrderEntity::getStatus,
                                status)
                        .orderByDesc(ProductionWorkOrderEntity::getCreateTime);
        if (page < 1 || limit < 1 || limit > 100) {
            throw new IllegalArgumentException("分页参数不正确");
        }
        var result =
                workOrderDao.selectPage(
                        new com.baomidou.mybatisplus.extension.plugins.pagination.Page<
                                ProductionWorkOrderEntity>(page, limit),
                        wrapper);
        workflowService.describeAllowedActions(result.getRecords());
        return RestResponse.ok(result);
    }

    /** 查询工单操作日志 / Lists work order operation logs. */
    @Operation(summary = "查询工单操作日志")
    @GetMapping("log/{workOrderId}")
    @RequiresPermissions("custom:work-order:list")
    public RestResponse<Object> log(
            @PathVariable Long workOrderId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        LambdaQueryWrapper<WorkOrderLogEntity> wrapper =
                new LambdaQueryWrapper<WorkOrderLogEntity>()
                        .eq(WorkOrderLogEntity::getWorkOrderId, workOrderId)
                        .orderByDesc(WorkOrderLogEntity::getCreateTime)
                        .orderByDesc(WorkOrderLogEntity::getId);
        if (page < 1 || page > 100000 || size < 1 || size > 100) {
            throw new IllegalArgumentException("分页参数不正确");
        }
        var result =
                workOrderLogDao.selectPage(
                        new com.baomidou.mybatisplus.extension.plugins.pagination.Page<
                                WorkOrderLogEntity>(page, size),
                        wrapper);
        return RestResponse.ok(
                java.util.Map.of(
                        "records",
                        result.getRecords(),
                        "total",
                        result.getTotal(),
                        "current",
                        result.getCurrent(),
                        "size",
                        result.getSize()));
    }

    /** 更新工单状态 / Updates a production work order status. */
    @Operation(summary = "更新定制生产工单状态")
    @PostMapping("status")
    @RequiresPermissions("custom:work-order:update")
    public RestResponse<String> updateStatus(Long id, String status, String remark) {
        workflowService.transition(
                id, status, remark, NumberUtils.toLong(getUserId()), getUser().getRealName());
        return RestResponse.ok("更新成功");
    }
}
