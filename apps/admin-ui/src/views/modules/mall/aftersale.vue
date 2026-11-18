<template>
    <section>
        <el-button v-if="isAuth('mall:aftersale:register')" type="primary" @click="register">
            登记售后
        </el-button>
        <el-button @click="load(1)">刷新</el-button>
        <el-table v-loading="loading" :data="rows">
            <el-table-column prop="order_sn" label="订单编号" min-width="150" />
            <el-table-column prop="customer_id" label="客户编号" min-width="100" />
            <el-table-column prop="mobile_hint" label="手机尾号" min-width="90" />
            <el-table-column prop="reason" label="售后原因" min-width="160" />
            <el-table-column prop="support_note" label="客服处理记录" min-width="160" />
            <el-table-column label="操作">
                <template #default="{row}">
                    <el-button v-if="isAuth('mall:aftersale:handle')" link @click="handle(row)"
                        >记录处理</el-button
                    >
                </template>
            </el-table-column>
        </el-table>
        <el-pagination :current-page="page" :page-size="20" :total="total" @current-change="load" />
        <el-dialog v-model="registrationVisible" title="登记售后">
            <el-form label-width="95px">
                <el-form-item label="关联订单">
                    <el-select v-model="orderId" filterable>
                        <el-option
                            v-for="order in eligibleOrders"
                            :key="order.id"
                            :label="order.orderSn"
                            :value="order.id"
                        />
                    </el-select>
                </el-form-item>
                <el-form-item label="售后原因">
                    <el-input v-model.trim="reason" maxlength="200" type="textarea" />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button
                    type="primary"
                    :disabled="!orderId || !reason"
                    :loading="writing"
                    @click="submitRegistration"
                    >登记</el-button
                >
            </template>
        </el-dialog>
    </section>
</template>

<script>
export default {
    /** 保存客服仅可见的售后记录和当前分页. */
    data() {
        return {
            rows: [],
            total: 0,
            page: 1,
            loading: false,
            writing: false,
            registrationVisible: false,
            eligibleOrders: [],
            orderId: null,
            reason: '',
        };
    },
    /** 进入客服入口时读取当前授权的队列. */
    activated() {
        this.load(1);
    },
    methods: {
        /** 只展示服务端按机构筛选的客服列表. */
        async load(page) {
            this.loading = true;
            try {
                const {data} = await this.$http({
                    url: '/mall/aftersale/list',
                    method: 'get',
                    params: {page, todo: this.$route.query.todo},
                });
                this.rows = data.code === 0 ? data.data.records : [];
                this.total = data.code === 0 ? data.data.total : 0;
                this.page = page;
            } finally {
                this.loading = false;
            }
        },
        /** 查看本账号有权登记售后的关联订单. */
        async register() {
            const {data} = await this.$http({url: '/mall/aftersale/orders', method: 'get'});
            if (data.code === 0) {
                this.eligibleOrders = data.data;
                this.orderId = null;
                this.reason = '';
                this.registrationVisible = true;
            }
        },
        /** 用服务端识别的订单建立待审核售后. */
        async submitRegistration() {
            if (await this.write('/mall/aftersale/register', this.orderId, this.reason)) {
                this.registrationVisible = false;
            }
        },
        /** 保存客服处理说明, 保持退款进度由资金岗位管理. */
        async handle(row) {
            let note;
            try {
                const {value} = await this.$prompt('请输入客服处理说明', '处理售后', {
                    inputValue: row.support_note || '',
                });
                note = value?.trim();
            } catch {
                return;
            }
            if (note) {
                await this.write('/mall/aftersale/handle', row.order_id, note);
            }
        },
        /** 写入期间禁止重复提交, 成功后重新加载列表. */
        async write(url, orderId, note) {
            if (this.writing) {
                return false;
            }
            this.writing = true;
            try {
                const {data} = await this.$http({url, method: 'post', data: {orderId, note}});
                if (data.code !== 0) {
                    return false;
                }
                await this.load(this.page);
                return true;
            } finally {
                this.writing = false;
            }
        },
    },
};
</script>
