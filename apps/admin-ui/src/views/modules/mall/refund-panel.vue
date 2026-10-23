<template>
    <el-dialog title="退款与售后审核" v-model="visible" width="94%" :close-on-click-modal="false">
        <p>审核通过后由支付查单确认结果。已发货订单须先核实退货或免退货安排, 不自动恢复库存。</p>
        <el-alert
            v-if="listError || writeError"
            :title="listError || writeError"
            type="error"
            :closable="false"
        >
            <el-button @click="load(requestedPage)">重新读取售后列表</el-button>
        </el-alert>
        <el-button
            v-if="isAuth('mall:order:update')"
            :loading="writing"
            :disabled="loading || needsReload || Boolean(listError)"
            @click="reconcile"
            >执行对账</el-button
        >
        <el-table v-loading="loading" :data="rows" border style="width: 100%; margin-top: 16px">
            <el-table-column prop="order_sn" label="订单号" min-width="180" />
            <el-table-column label="申请金额" min-width="110"
                ><template #default="scope"
                    >¥{{ (scope.row.amount_fen / 100).toFixed(2) }}</template
                ></el-table-column
            >
            <el-table-column prop="reason" label="申请理由" min-width="180" />
            <el-table-column label="处理状态" min-width="130"
                ><template #default="scope">{{
                    statusName(scope.row.status)
                }}</template></el-table-column
            >
            <el-table-column prop="decision_note" label="审核说明" min-width="180" />
            <el-table-column label="操作" min-width="160"
                ><template #default="scope">
                    <template
                        v-if="scope.row.status === 'PENDING_REVIEW' && isAuth('mall:order:update')"
                    >
                        <el-button
                            type="text"
                            :disabled="writing || loading || needsReload || Boolean(listError)"
                            @click="decide(scope.row, true)"
                            >通过</el-button
                        >
                        <el-button
                            type="text"
                            :disabled="writing || loading || needsReload || Boolean(listError)"
                            @click="decide(scope.row, false)"
                            >拒绝</el-button
                        >
                    </template>
                    <span v-else>{{
                        scope.row.status === 'MANUAL_REVIEW'
                            ? '请在微信商户平台处理异常'
                            : '等待对账或已完成'
                    }}</span>
                </template></el-table-column
            >
        </el-table>
        <el-pagination
            :current-page="page"
            :page-size="20"
            :total="total"
            layout="total, prev, pager, next"
            @current-change="load"
        />
    </el-dialog>
</template>

<script>
export default {
    /** 读取与写入各自持有状态, 弹窗每次打开使用独立生命周期. */
    data() {
        return {
            visible: false,
            rows: [],
            page: 1,
            requestedPage: 1,
            total: 0,
            panelEpoch: 0,
            loading: false,
            writing: false,
            listError: '',
            writeError: '',
            needsReload: false,
            decisionDraft: {orderId: null, note: ''},
        };
    },
    /** 离开订单页面时关闭售后弹窗. */
    deactivated() {
        this.close();
    },
    /** 销毁后不允许旧读取或确认继续更新页面. */
    beforeUnmount() {
        this.close();
    },
    methods: {
        /** 新打开只显示本次读取结果, 写入锁保留到原请求结束. */
        open() {
            this.panelEpoch++;
            this.visible = true;
            this.rows = [];
            this.listError = '';
            this.writeError = '';
            return this.load(1);
        },
        /** 显式废弃弹窗读取和确认上下文. */
        close() {
            this.visible = false;
            this.panelEpoch++;
            this.loading = false;
        },
        /** 最新分页成功后才替换已展示数据, 失败保留原页供重试. */
        async load(page = this.page) {
            const version = (this._readVersion = (this._readVersion || 0) + 1);
            const epoch = this.panelEpoch;
            this.requestedPage = page;
            this.loading = true;
            this.listError = '';
            try {
                const {data} = await this.$http({
                    url: '/mall/refund/list',
                    method: 'get',
                    params: {page, limit: 20},
                    showError: false,
                });
                if (!this.visible || epoch !== this.panelEpoch || version !== this._readVersion) {
                    return;
                }
                if (!data || data.code !== 0 || !Array.isArray(data.data?.records)) {
                    throw new Error(data?.msg || '售后列表读取失败');
                }
                this.rows = data.data.records;
                this.total = data.data.total;
                this.page = page;
                this.needsReload = false;
                this.writeError = '';
            } catch (error) {
                if (this.visible && epoch === this.panelEpoch && version === this._readVersion) {
                    this.listError = error.message || '售后列表读取失败';
                }
            } finally {
                if (epoch === this.panelEpoch && version === this._readVersion) {
                    this.loading = false;
                }
            }
        },
        /** 写入结果不确定时先重查, 之前在途的读取不能解除保护. */
        requireReload(text) {
            this._readVersion = (this._readVersion || 0) + 1;
            this.loading = false;
            this.needsReload = true;
            this.writeError = text + ', 请重新读取并核对状态';
        },
        /** 确认前防重, 确认后复核申请仍待审核, 不将审核通过当作退款成功. */
        async decide(row, approved) {
            if (
                this.writing ||
                this.loading ||
                this.listError ||
                this.needsReload ||
                !this.visible ||
                row.status !== 'PENDING_REVIEW'
            ) {
                return;
            }
            const epoch = this.panelEpoch;
            const orderId = row.order_id;
            this.writing = true;
            let submitted = false;
            try {
                const {value} = await this.$prompt(
                    approved ? '请确认退货/免退货安排并填写审核说明' : '请填写拒绝理由',
                    '售后审核',
                    {
                        inputValue:
                            this.decisionDraft.orderId === orderId ? this.decisionDraft.note : '',
                        inputValidator: (value) =>
                            Boolean(value && value.trim() && value.length <= 200) ||
                            '请填写1到200字说明',
                    }
                );
                if (!this.visible || epoch !== this.panelEpoch) {
                    return;
                }
                this.decisionDraft = {orderId, note: value};
                if (!value?.trim() || value.length > 200) {
                    throw new Error('请填写1到200字说明');
                }
                const current = this.rows.find((item) => item.order_id === orderId);
                if (
                    this.loading ||
                    this.listError ||
                    this.needsReload ||
                    !current ||
                    current.status !== 'PENDING_REVIEW'
                ) {
                    throw new Error('申请状态已变化, 请重新读取后审核');
                }
                submitted = true;
                const {data} = await this.$http({
                    url: '/mall/refund/decide',
                    method: 'post',
                    showError: false,
                    data: {orderId, approved, note: value.trim()},
                });
                if (!this.visible || epoch !== this.panelEpoch) {
                    return;
                }
                if (!data || data.code !== 0) {
                    throw new Error(data?.msg || '审核失败');
                }
                this.decisionDraft = {orderId: null, note: ''};
                this.$message.success('审核已保存, 退款结果以对账为准');
                await this.load();
            } catch (error) {
                if (
                    this.visible &&
                    epoch === this.panelEpoch &&
                    error !== 'cancel' &&
                    error !== 'close'
                ) {
                    const text = error.message || '审核结果未确认';
                    if (submitted) {
                        this.requireReload(text);
                    }
                    this.$message.error(text);
                }
            } finally {
                this.writing = false;
            }
        },
        /** 限制一次对账请求, 成功后展示服务端状态和实际处理数. */
        async reconcile() {
            if (
                this.writing ||
                this.loading ||
                this.listError ||
                this.needsReload ||
                !this.visible
            ) {
                return;
            }
            const epoch = this.panelEpoch;
            this.writing = true;
            try {
                const {data} = await this.$http({
                    url: '/mall/refund/reconcile',
                    method: 'post',
                    showError: false,
                });
                if (!this.visible || epoch !== this.panelEpoch) {
                    return;
                }
                if (
                    !data ||
                    data.code !== 0 ||
                    !Number.isInteger(data.data?.processed) ||
                    data.data.processed < 0
                ) {
                    throw new Error(data?.msg || '对账结果未确认');
                }
                this.$message.success(
                    '本次处理 ' + data.data.processed + ' 项, 退款结果请核对实际状态'
                );
                await this.load();
            } catch (error) {
                if (this.visible && epoch === this.panelEpoch) {
                    const text = error.message || '对账结果未确认';
                    this.requireReload(text);
                    this.$message.error(text);
                }
            } finally {
                this.writing = false;
            }
        },
        /** 使用商家可读状态名称. */
        statusName(status) {
            return (
                {
                    PENDING_REVIEW: '待审核',
                    APPROVED: '待提交退款',
                    PROCESSING: '退款处理中',
                    RETRY: '等待补偿重试',
                    SUCCEEDED: '退款已完成',
                    REJECTED: '审核未通过',
                    MANUAL_REVIEW: '需人工处理',
                }[status] || status
            );
        },
    },
};
</script>
