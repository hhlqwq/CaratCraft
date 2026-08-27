package com.platform.modules.custom.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.platform.common.utils.RestResponse;
import com.platform.modules.custom.dao.ProductionWorkOrderDao;
import com.platform.modules.custom.dao.WorkOrderLogDao;
import com.platform.modules.custom.entity.ProductionWorkOrderEntity;
import com.platform.modules.custom.entity.WorkOrderLogEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 定制生产工单管理 / Custom production work order administration.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("custom/work-order")
@Tag(name = "CustomWorkOrderController|定制生产工单")
public class CustomWorkOrderController {
    /** 工单合法状态列表 / All supported work order statuses. */
    private static final List<String> ALLOWED_STATUSES = Arrays.asList(
            "PENDING_REVIEW", "REVIEWED", "PRODUCING", "QUALITY_CHECK", "COMPLETED", "CANCELLED");

    /**
     * 服务端状态机, 限定每个状态的合法流转目标 /
     * Server-side state machine restricting transitions per status.
     */
    private static final Map<String, List<String>> ALLOWED_TRANSITIONS = Map.of(
            "PENDING_REVIEW", Arrays.asList("REVIEWED", "CANCELLED"),
            "REVIEWED", Arrays.asList("PRODUCING", "CANCELLED"),
            "PRODUCING", List.of("QUALITY_CHECK"),
            "QUALITY_CHECK", List.of("COMPLETED"),
            "COMPLETED", List.of(),
            "CANCELLED", List.of());

    private final ProductionWorkOrderDao workOrderDao;
    private final WorkOrderLogDao workOrderLogDao;

    /** 查询工单列表 / Lists production work orders. */
    @Operation(summary = "查询定制生产工单")
    @GetMapping("list")
    @RequiresPermissions("custom:work-order:list")
    public RestResponse<List<ProductionWorkOrderEntity>> list(@RequestParam(required = false) String status) {
        LambdaQueryWrapper<ProductionWorkOrderEntity> wrapper =
                new LambdaQueryWrapper<ProductionWorkOrderEntity>()
                        .eq(status != null && !status.isBlank(), ProductionWorkOrderEntity::getStatus, status)
                        .orderByDesc(ProductionWorkOrderEntity::getCreateTime);
        return RestResponse.ok(workOrderDao.selectList(wrapper));
    }

    /** 查询工单操作日志 / Lists work order operation logs. */
    @Operation(summary = "查询工单操作日志")
    @GetMapping("log/{workOrderId}")
    @RequiresPermissions("custom:work-order:list")
    public RestResponse<List<WorkOrderLogEntity>> log(@PathVariable Long workOrderId) {
        LambdaQueryWrapper<WorkOrderLogEntity> wrapper =
                new LambdaQueryWrapper<WorkOrderLogEntity>()
                        .eq(WorkOrderLogEntity::getWorkOrderId, workOrderId)
                        .orderByAsc(WorkOrderLogEntity::getCreateTime);
        return RestResponse.ok(workOrderLogDao.selectList(wrapper));
    }

    /** 更新工单状态 / Updates a production work order status. */
    @Operation(summary = "更新定制生产工单状态")
    @PostMapping("status")
    @RequiresPermissions("custom:work-order:update")
    public RestResponse<String> updateStatus(Long id, String status, String remark) {
        if (!ALLOWED_STATUSES.contains(status)) {
            return RestResponse.fail("不支持的工单状态");
        }
        ProductionWorkOrderEntity workOrder = workOrderDao.selectById(id);
        if (workOrder == null) {
            return RestResponse.fail("工单不存在");
        }
        String fromStatus = workOrder.getStatus();
        // 按状态机校验流转合法性 / Validates the transition against the state machine.
        List<String> allowedTargets = ALLOWED_TRANSITIONS.getOrDefault(fromStatus, List.of());
        if (!allowedTargets.contains(status)) {
            return RestResponse.fail("不允许从 " + fromStatus + " 流转到 " + status);
        }
        workOrder.setStatus(status);
        workOrder.setUpdateTime(new Date());
        workOrderDao.updateById(workOrder);
        // 记录操作日志 / Record operation log.
        WorkOrderLogEntity log = new WorkOrderLogEntity();
        log.setWorkOrderId(id);
        log.setFromStatus(fromStatus);
        log.setToStatus(status);
        log.setRemark(remark);
        log.setCreateTime(new Date());
        workOrderLogDao.insert(log);
        return RestResponse.ok("更新成功");
    }
}
