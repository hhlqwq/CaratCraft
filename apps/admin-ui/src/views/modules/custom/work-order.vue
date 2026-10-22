<template>
    <div class="mod-custom-work-order">
        <el-form :inline="true" @keyup.enter="loadWorkOrders(1)">
            <el-form-item>
                <el-select v-model="status" placeholder="工单状态" clearable>
                    <el-option
                        v-for="item in statusOptions"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value"
                    ></el-option>
                </el-select>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" :loading="loading" @click="loadWorkOrders(1)"
                    >查询</el-button
                >
            </el-form-item>
        </el-form>

        <el-alert v-if="listError" :title="listError" type="error" :closable="false" />
        <el-table v-loading="loading" :data="workOrders" border style="width: 100%">
            <el-table-column prop="workOrderNo" label="工单号" min-width="150"></el-table-column>
            <el-table-column prop="orderId" label="订单 ID" width="100"></el-table-column>
            <el-table-column prop="snapshotId" label="设计快照 ID" width="120"></el-table-column>
            <el-table-column label="状态" width="120">
                <template #default="scope">
                    <el-tag>{{ statusText(scope.row.status) }}</el-tag>
                </template>
            </el-table-column>
            <el-table-column label="制作内容" min-width="280">
                <template #default="scope">
                    <div
                        v-for="(line, index) in craftSummary(scope.row)"
                        :key="index"
                        class="craft-line"
                    >
                        {{ line }}
                    </div>
                    <details v-if="arrangementLines(scope.row).length">
                        <summary>查看冻结排列顺序</summary>
                        <div
                            v-for="(line, index) in arrangementLines(scope.row)"
                            :key="index"
                            class="craft-line"
                        >
                            {{ line }}
                        </div>
                    </details>
                </template>
            </el-table-column>
            <el-table-column label="设计预览" min-width="130"
                ><template #default="scope">
                    <span v-if="arrangementLines(scope.row).length">逐项顺序见制作内容</span>
                    <div v-else class="work-preview">
                        <img
                            v-for="layer in previewLayers(scope.row)"
                            :key="layer.optionId"
                            :src="layer.assetUrl"
                            alt="设计图层"
                        />
                    </div> </template
            ></el-table-column>
            <el-table-column prop="createTime" label="创建时间" min-width="160"></el-table-column>
            <el-table-column fixed="right" label="操作" width="200">
                <template #default="scope">
                    <el-dropdown
                        :disabled="updating || loading || !!listError"
                        v-if="
                            isAuth('custom:work-order:update') && allowedActions(scope.row).length
                        "
                        @command="updateStatus(scope.row.id, $event)"
                    >
                        <el-button type="text"
                            >流转状态<i class="el-icon-arrow-down el-icon--right"></i
                        ></el-button>
                        <template #dropdown
                            ><el-dropdown-menu>
                                <el-dropdown-item
                                    v-for="item in allowedActions(scope.row)"
                                    :key="item.value"
                                    :command="item.value"
                                    >{{ item.label }}</el-dropdown-item
                                >
                            </el-dropdown-menu></template
                        >
                    </el-dropdown>
                    <el-button
                        v-if="isAuth('custom:work-order:list')"
                        type="text"
                        size="small"
                        @click="showLogs(scope.row)"
                        >日志</el-button
                    >
                </template>
            </el-table-column>
        </el-table>

        <el-pagination
            :current-page="page"
            :page-size="20"
            :total="total"
            layout="total, prev, pager, next"
            @current-change="changePage"
        />

        <el-dialog title="工单操作日志" v-model="logVisible" width="min(94vw, 900px)">
            <el-alert v-if="logError" :title="logError" type="error" :closable="false">
                <el-button @click="showLogs({id: logWorkOrderId})">重新加载</el-button>
            </el-alert>
            <el-table v-loading="logsLoading" :data="logs" border size="small">
                <el-table-column prop="createTime" label="时间" min-width="150"></el-table-column>
                <el-table-column prop="operatorName" label="操作人" width="110"></el-table-column>
                <el-table-column label="状态流转" min-width="180">
                    <template #default="scope">
                        {{ statusText(scope.row.fromStatus) || '—' }} →
                        {{ statusText(scope.row.toStatus) }}
                    </template>
                </el-table-column>
                <el-table-column
                    prop="remark"
                    label="备注"
                    min-width="140"
                    show-overflow-tooltip
                ></el-table-column>
            </el-table>
        </el-dialog>
    </div>
</template>

<script>
export default {
    data() {
        return {
            pageActive: true,
            pageEpoch: 0,
            loading: false,
            updating: false,
            listError: '',
            logsLoading: false,
            logError: '',
            logWorkOrderId: null,
            status: '',
            workOrders: [],
            page: 1,
            total: 0,
            statusOptions: [
                {value: 'PENDING_REVIEW', label: '待审核'},
                {value: 'REVIEWED', label: '已审核'},
                {value: 'PRODUCING', label: '制作中'},
                {value: 'QUALITY_CHECK', label: '质检中'},
                {value: 'COMPLETED', label: '已完成'},
                {value: 'CANCELLED', label: '已取消'},
            ],
            logVisible: false,
            logs: [],
        };
    },
    activated() {
        this.pageActive = true;
        this.pageEpoch++;
        this.loadWorkOrders();
    },
    /** 离页时废弃在途读取, 关闭旧日志弹窗. */
    deactivated() {
        this.pageActive = false;
        this.pageEpoch++;
        this.logVisible = false;
    },
    /** 销毁后不允许异步回调再更新页面. */
    beforeUnmount() {
        this.pageActive = false;
        this.pageEpoch++;
    },
    methods: {
        // 显示下单时逐个材料的冻结顺序和尺寸, 不按当前配置重建.
        arrangementLines(row) {
            try {
                return (JSON.parse(row.craftJson || '{}').arrangement || []).map(
                    (item, index) =>
                        `${index + 1}. ${item.name} · ${item.sizeMm || '尺寸未配置'}${item.sizeMm ? 'mm' : ''}`
                );
            } catch (error) {
                return [];
            }
        },
        // 切换有限页数据.
        changePage(page) {
            return this.loadWorkOrders(page);
        },
        // 加载生产工单 / Loads production work orders.
        async loadWorkOrders(page = this.page) {
            const version = (this._listVersion = (this._listVersion || 0) + 1);
            const epoch = this.pageEpoch;
            this.loading = true;
            this.listError = '';
            try {
                const {data} = await this.$http({
                    url: '/custom/work-order/list',
                    method: 'get',
                    params: {status: this.status, page, limit: 20},
                    showError: false,
                });
                if (!this.pageActive || epoch !== this.pageEpoch || version !== this._listVersion) {
                    return;
                }
                if (!data || data.code !== 0 || !Array.isArray(data.data?.records)) {
                    throw new Error(data?.msg || '工单读取失败, 请重试查询');
                }
                this.workOrders = data.data.records;
                this.total = data.data.total;
                this.page = page;
            } catch (error) {
                if (this.pageActive && epoch === this.pageEpoch && version === this._listVersion) {
                    this.listError = error.message || '工单读取失败, 请重试查询';
                }
            } finally {
                if (epoch === this.pageEpoch && version === this._listVersion) {
                    this.loading = false;
                }
            }
        },
        // 只展示下一步合法动作, 取消必须由订单售后流程处理.
        allowedActions(row) {
            return this.statusOptions.filter((item) =>
                (row.allowedActions || []).includes(item.value)
            );
        },
        // 以可读制作参数展示快照, 历史快照缺字段时明确提示人工核验.
        craftSummary(row) {
            try {
                const craft = JSON.parse(row.craftJson || '{}');
                const lines = Array.isArray(craft.components)
                    ? craft.components.map(
                          (item) =>
                              `${item.name || item.optionCode} × ${item.quantity} ${item.unit || '件'}`
                      )
                    : [
                          '历史选项: ' + (craft.optionIds || []).join('、'),
                          '历史参数需结合设计详情核验',
                      ];
                if (craft.sizing) {
                    const sizing = craft.sizing;
                    lines.push('冻结实测颈围: ' + sizing.neckCircumferenceMm + 'mm');
                    lines.push(
                        '目标成品长度: ' +
                            sizing.targetLengthMm +
                            'mm / 工艺估算: ' +
                            sizing.estimatedFinishedLengthMm +
                            'mm'
                    );
                    if (sizing.rules) {
                        lines.push(
                            '冻结长度工艺: ' +
                                sizing.rules.mode +
                                ', 扣件 ' +
                                sizing.rules.claspMm +
                                'mm, 连接 ' +
                                sizing.rules.connectionMm +
                                'mm, 加工缩短 ' +
                                sizing.rules.lossMm +
                                'mm, 容差 ' +
                                sizing.rules.toleranceMm +
                                'mm'
                        );
                    }
                }
                if (craft.engraving) {
                    lines.push('刻字: ' + craft.engraving);
                }
                return lines;
            } catch (error) {
                return ['生产参数无法读取, 请核对设计快照后再操作'];
            }
        },
        // 使用订单冻结图层预览, 历史缺图时不伪造商品图.
        previewLayers(row) {
            try {
                return (JSON.parse(row.craftJson || '{}').components || [])
                    .filter(
                        (line) =>
                            line.assetUrl &&
                            (/^https:\/\//i.test(line.assetUrl) || /^\/(?!\/)/.test(line.assetUrl))
                    )
                    .sort((a, b) => Number(a.zIndex || 0) - Number(b.zIndex || 0));
            } catch (error) {
                return [];
            }
        },
        // 每次审核、开工和质检必须录入操作备注并确认.
        async updateStatus(id, status) {
            if (this.updating || this.loading || this.listError || !this.pageActive) {
                return;
            }
            const row = this.workOrders.find((item) => item.id === id);
            if (!row || !this.allowedActions(row).some((item) => item.value === status)) {
                return;
            }
            const epoch = this.pageEpoch;
            this.updating = true;
            try {
                const {value} = await this.$prompt(
                    `确认执行“${this.statusText(status)}”, 请填写审核、制作或质检记录`,
                    '工单流转',
                    {
                        inputType: 'textarea',
                        inputValidator: (value) =>
                            value && value.trim().length > 0 && value.length <= 500
                                ? true
                                : '请填写1到500字的备注',
                    }
                );
                if (!this.pageActive || epoch !== this.pageEpoch) {
                    return;
                }
                const current = this.workOrders.find((item) => item.id === id);
                if (
                    !current ||
                    !this.allowedActions(current).some((item) => item.value === status)
                ) {
                    throw new Error('工单列表已更新, 请重新选择流转操作');
                }
                const {data} = await this.$http({
                    url: '/custom/work-order/status',
                    method: 'post',
                    params: {id: id, status: status, remark: value},
                    showError: false,
                });
                if (!this.pageActive || epoch !== this.pageEpoch) {
                    return;
                }
                if (data && data.code === 0) {
                    this.$message.success('工单状态已更新');
                    await this.loadWorkOrders();
                } else {
                    throw new Error(data?.msg || '工单流转失败');
                }
            } catch (error) {
                if (
                    this.pageActive &&
                    epoch === this.pageEpoch &&
                    error !== 'cancel' &&
                    error !== 'close'
                ) {
                    this.$message.error(error.message || '工单流转失败');
                }
            } finally {
                this.updating = false;
            }
        },
        // 获取状态中文名称 / Gets a localized status label.
        statusText(status) {
            const option = this.statusOptions.find((item) => item.value === status);
            return option ? option.label : status;
        },
        // 查看工单操作日志 / Shows the work order operation logs.
        async showLogs(row) {
            const version = (this._logVersion = (this._logVersion || 0) + 1);
            const epoch = this.pageEpoch;
            this.logWorkOrderId = row.id;
            this.logVisible = true;
            this.logs = [];
            this.logError = '';
            this.logsLoading = true;
            try {
                const {data} = await this.$http({
                    url: `/custom/work-order/log/${row.id}`,
                    method: 'get',
                    showError: false,
                });
                if (
                    !this.pageActive ||
                    epoch !== this.pageEpoch ||
                    version !== this._logVersion ||
                    !this.logVisible
                ) {
                    return;
                }
                if (!data || data.code !== 0 || !Array.isArray(data.data)) {
                    throw new Error(data?.msg || '日志查询失败');
                }
                this.logs = data.data;
            } catch (error) {
                if (
                    this.pageActive &&
                    epoch === this.pageEpoch &&
                    version === this._logVersion &&
                    this.logVisible
                ) {
                    this.logError = error.message || '日志查询失败';
                }
            } finally {
                if (epoch === this.pageEpoch && version === this._logVersion) {
                    this.logsLoading = false;
                }
            }
        },
    },
};
</script>

<style scoped>
.work-preview {
    position: relative;
    width: 100px;
    height: 100px;
    background: #f5f6f2;
}
.work-preview img {
    position: absolute;
    inset: 0;
    width: 100%;
    height: 100%;
    object-fit: contain;
}
.craft-line {
    line-height: 1.8;
    overflow-wrap: anywhere;
}
</style>
