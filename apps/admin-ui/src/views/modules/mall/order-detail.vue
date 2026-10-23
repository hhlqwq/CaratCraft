<template>
    <el-dialog
        title="订单详情"
        :close-on-click-modal="false"
        :lock-scroll="false"
        v-model="visible"
        width="980px"
        class="order-detail-dialog"
    >
        <el-alert v-if="readError" :title="readError" type="error" :closable="false">
            <el-button @click="init(currentId)">重新读取</el-button>
        </el-alert>
        <div v-loading="loading" class="order-hero">
            <div class="hero-main">
                <div class="hero-label">订单编号</div>
                <div class="hero-sn">{{ dataForm.orderSn || '-' }}</div>
            </div>
            <div class="hero-tags">
                <el-tag size="small" :type="orderStatusType(dataForm.orderStatus)"
                    >{{ orderStatusText(dataForm.orderStatus) }}
                </el-tag>
                <el-tag size="small" :type="payStatusType(dataForm.payStatus)">{{
                    payStatusText(dataForm.payStatus)
                }}</el-tag>
                <el-tag size="small" :type="shippingStatusType(dataForm.shippingStatus)">
                    {{ shippingStatusText(dataForm.shippingStatus) }}
                </el-tag>
            </div>
        </div>

        <el-form :model="dataForm" :rules="dataRule" ref="dataForm" label-width="110px">
            <section class="section-card">
                <h3 class="section-title">基础信息</h3>
                <div class="form-grid">
                    <el-form-item label="订单编号" prop="orderSn" class="full">
                        <el-input v-model="dataForm.orderSn" :disabled="disabled"></el-input>
                    </el-form-item>
                    <el-form-item label="用户昵称">
                        <el-input v-model="dataForm.userNickname" disabled></el-input>
                    </el-form-item>
                    <el-form-item label="订单类型">
                        <el-input v-model="dataForm.orderType" :disabled="disabled"></el-input>
                    </el-form-item>
                </div>
            </section>

            <section class="section-card">
                <h3 class="section-title">收货信息</h3>
                <div class="form-grid">
                    <el-form-item label="收货人" prop="consignee">
                        <el-input v-model="dataForm.consignee" :disabled="disabled"></el-input>
                    </el-form-item>
                    <el-form-item label="手机号" prop="mobile">
                        <el-input v-model="dataForm.mobile" :disabled="disabled"></el-input>
                    </el-form-item>
                    <el-form-item label="国家">
                        <el-input v-model="dataForm.country" :disabled="disabled"></el-input>
                    </el-form-item>
                    <el-form-item label="省">
                        <el-input v-model="dataForm.province" :disabled="disabled"></el-input>
                    </el-form-item>
                    <el-form-item label="市">
                        <el-input v-model="dataForm.city" :disabled="disabled"></el-input>
                    </el-form-item>
                    <el-form-item label="区/县">
                        <el-input v-model="dataForm.district" :disabled="disabled"></el-input>
                    </el-form-item>
                    <el-form-item label="详细地址" class="full">
                        <el-input v-model="dataForm.address" :disabled="disabled"></el-input>
                    </el-form-item>
                    <el-form-item label="买家留言" class="full">
                        <el-input
                            type="textarea"
                            :rows="2"
                            v-model="dataForm.postscript"
                            :disabled="disabled"
                        ></el-input>
                    </el-form-item>
                </div>
            </section>

            <section class="section-card">
                <h3 class="section-title">物流与支付</h3>
                <div class="form-grid">
                    <el-form-item label="物流方式">
                        <el-input v-model="dataForm.shippingName" :disabled="disabled"></el-input>
                    </el-form-item>
                    <el-form-item label="物流单号">
                        <el-input v-model="dataForm.shippingNo" :disabled="disabled"></el-input>
                    </el-form-item>
                    <el-form-item label="支付方式">
                        <el-input v-model="dataForm.payName" :disabled="disabled"></el-input>
                    </el-form-item>
                    <el-form-item label="运费">
                        <el-input-number
                            v-model="dataForm.freightPrice"
                            :disabled="disabled"
                            :min="0"
                            controls-position="right"
                        ></el-input-number>
                    </el-form-item>
                    <el-form-item label="物流费用">
                        <el-input-number
                            v-model="dataForm.shippingFee"
                            :disabled="disabled"
                            :precision="2"
                            :min="0"
                            controls-position="right"
                        ></el-input-number>
                    </el-form-item>
                </div>
            </section>

            <section class="section-card">
                <h3 class="section-title">金额信息</h3>
                <div class="form-grid">
                    <el-form-item label="实付金额">
                        <el-input-number
                            v-model="dataForm.actualPrice"
                            :disabled="disabled"
                            :precision="2"
                            :min="0"
                            controls-position="right"
                        ></el-input-number>
                    </el-form-item>
                    <el-form-item label="订单总价">
                        <el-input-number
                            v-model="dataForm.orderPrice"
                            :disabled="disabled"
                            :precision="2"
                            :min="0"
                            controls-position="right"
                        ></el-input-number>
                    </el-form-item>
                    <el-form-item label="商品总价">
                        <el-input-number
                            v-model="dataForm.goodsPrice"
                            :disabled="disabled"
                            :precision="2"
                            :min="0"
                            controls-position="right"
                        ></el-input-number>
                    </el-form-item>
                    <el-form-item label="优惠金额">
                        <el-input-number
                            v-model="dataForm.couponPrice"
                            :disabled="disabled"
                            :precision="2"
                            :min="0"
                            controls-position="right"
                        ></el-input-number>
                    </el-form-item>
                    <el-form-item label="满减金额">
                        <el-input-number
                            v-model="dataForm.fullCutPrice"
                            :disabled="disabled"
                            :precision="2"
                            :min="0"
                            controls-position="right"
                        ></el-input-number>
                    </el-form-item>
                    <el-form-item label="优惠券ID">
                        <el-input-number
                            v-model="dataForm.couponId"
                            :disabled="disabled"
                            :min="0"
                            controls-position="right"
                        ></el-input-number>
                    </el-form-item>
                    <el-form-item label="积分">
                        <el-input-number
                            v-model="dataForm.integral"
                            :disabled="disabled"
                            :min="0"
                            controls-position="right"
                        ></el-input-number>
                    </el-form-item>
                    <el-form-item label="积分抵扣">
                        <el-input-number
                            v-model="dataForm.integralMoney"
                            :disabled="disabled"
                            :precision="2"
                            :min="0"
                            controls-position="right"
                        ></el-input-number>
                    </el-form-item>
                </div>
            </section>

            <section class="section-card">
                <h3 class="section-title">时间信息</h3>
                <div class="form-grid">
                    <el-form-item label="下单时间">
                        <el-date-picker
                            v-model="dataForm.addTime"
                            type="datetime"
                            :disabled="disabled"
                        ></el-date-picker>
                    </el-form-item>
                    <el-form-item label="确认时间">
                        <el-date-picker
                            v-model="dataForm.confirmTime"
                            type="datetime"
                            :disabled="disabled"
                        ></el-date-picker>
                    </el-form-item>
                    <el-form-item label="支付时间">
                        <el-date-picker
                            v-model="dataForm.payTime"
                            type="datetime"
                            :disabled="disabled"
                        ></el-date-picker>
                    </el-form-item>
                </div>
            </section>
        </el-form>

        <el-divider v-if="dataForm.id">订单商品明细</el-divider>
        <el-table
            v-if="dataForm.id"
            :data="orderGoodsList"
            border
            size="small"
            style="width: 100%; margin-top: 12px"
        >
            <el-table-column prop="id" label="ID" width="80" align="center"></el-table-column>
            <el-table-column
                prop="goodsId"
                label="商品ID"
                width="100"
                align="center"
            ></el-table-column>
            <el-table-column
                prop="goodsName"
                label="商品名称"
                min-width="220"
                show-overflow-tooltip
            ></el-table-column>
            <el-table-column prop="number" label="数量" width="80" align="center"></el-table-column>
            <el-table-column
                prop="retailPrice"
                label="零售价"
                width="100"
                align="center"
            ></el-table-column>
            <el-table-column label="图片" width="100" align="center">
                <template #default="scope">
                    <el-image
                        v-if="scope.row.listPicUrl"
                        :src="scope.row.listPicUrl"
                        :preview-src-list="
                            orderGoodsList.map((item) => item.listPicUrl).filter(Boolean)
                        "
                        fit="cover"
                        style="width: 40px; height: 40px; border-radius: 4px"
                    >
                    </el-image>
                    <span v-else>无</span>
                </template>
            </el-table-column>
        </el-table>

        <template #footer
            ><span class="dialog-footer">
                <el-button @click="visible = false">取消</el-button>
            </span></template
        >
    </el-dialog>
</template>

<script>
export default {
    /** 详情只读, 打开新订单前清空旧内容. */
    data() {
        return {
            disabled: true,
            currentId: null,
            loading: false,
            readError: '',
            visible: false,
            orderGoodsList: [],
            dataForm: {
                id: '',
                orderSn: '',
                userId: 0,
                userNickname: '',
                orderStatus: 0,
                shippingStatus: 0,
                payStatus: 0,
                consignee: '',
                country: '',
                province: '',
                city: '',
                district: '',
                address: '',
                mobile: '',
                postscript: '',
                shippingId: 0,
                shippingName: '',
                payId: '',
                payName: '',
                shippingFee: 0,
                actualPrice: 0,
                integral: 0,
                integralMoney: 0,
                orderPrice: 0,
                goodsPrice: 0,
                addTime: '',
                confirmTime: '',
                payTime: '',
                freightPrice: 0,
                couponId: 0,
                parentId: 0,
                couponPrice: 0,
                callbackStatus: 'true',
                shippingNo: '',
                fullCutPrice: 0,
                orderType: '',
            },
            dataRule: {
                orderSn: [{required: true, message: '订单编号不能为空', trigger: 'blur'}],
            },
        };
    },
    /** 缓存页离开时关闭详情并废弃在途读取. */
    deactivated() {
        this.close();
    },
    /** 销毁时不允许旧请求继续回填. */
    beforeUnmount() {
        this.close();
    },
    methods: {
        /** 明确关闭当前详情, 新打开操作使用新的请求代次. */
        close() {
            this.visible = false;
            this._readVersion = (this._readVersion || 0) + 1;
            this.loading = false;
        },
        /** 同一次打开并行读取详情与商品, 只有全部成功才展示订单. */
        async init(id) {
            const version = (this._readVersion = (this._readVersion || 0) + 1);
            this.currentId = id;
            this.visible = true;
            this.loading = true;
            this.readError = '';
            this.dataForm = {};
            this.orderGoodsList = [];
            try {
                const [detail, goods] = await Promise.all([
                    this.$http({url: `/mall/order/info/${id}`, method: 'get', showError: false}),
                    this.$http({url: `/mall/order/goods/${id}`, method: 'get', showError: false}),
                ]);
                if (!this.visible || version !== this._readVersion) {
                    return;
                }
                if (
                    detail.data?.code !== 0 ||
                    !detail.data.data ||
                    goods.data?.code !== 0 ||
                    !Array.isArray(goods.data.data)
                ) {
                    throw new Error(detail.data?.msg || goods.data?.msg || '订单详情读取失败');
                }
                this.dataForm = detail.data.data;
                this.orderGoodsList = goods.data.data;
            } catch (error) {
                if (this.visible && version === this._readVersion) {
                    this.readError = error.message || '订单详情读取失败';
                }
            } finally {
                if (version === this._readVersion) {
                    this.loading = false;
                }
            }
        },
        /** 展示服务端订单状态. */
        orderStatusText(status) {
            const map = {
                0: '待付款',
                1: '待发货',
                2: '待收货',
                3: '已完成',
                4: '已取消',
                101: '已取消',
                102: '已删除',
                200: '已支付',
                201: '待发货',
                300: '待收货',
                301: '已完成',
                401: '已退款',
                402: '已退货',
            };
            return map[status] || `状态${status}`;
        },
        /** 按订单状态选择提示色. */
        orderStatusType(status) {
            const map = {
                0: 'warning',
                1: '',
                2: 'success',
                3: 'success',
                4: 'info',
                101: 'info',
                102: 'info',
                200: '',
                201: '',
                300: 'success',
                301: 'success',
                401: 'danger',
                402: 'danger',
            };
            return map[status] || 'info';
        },
        /** 展示实际支付状态. */
        payStatusText(status) {
            const map = {0: '未支付', 1: '预支付已创建', 2: '已支付', 3: '退款中', 4: '已退款'};
            return map[status] || `状态${status}`;
        },
        /** 按支付状态选择提示色. */
        payStatusType(status) {
            const map = {0: 'warning', 1: 'warning', 2: 'success', 3: 'danger', 4: 'danger'};
            return map[status] || 'info';
        },
        /** 展示实际发货状态. */
        shippingStatusText(status) {
            const map = {0: '未发货', 1: '已发货', 2: '已收货'};
            return map[status] || `状态${status}`;
        },
        /** 按发货状态选择提示色. */
        shippingStatusType(status) {
            const map = {0: 'info', 1: 'success', 2: 'success'};
            return map[status] || 'info';
        },
    },
};
</script>

<style scoped>
.order-hero {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 14px;
    padding: 14px 16px;
    border-radius: 10px;
    background: linear-gradient(135deg, #f7fbff 0%, #eef5ff 100%);
    border: 1px solid #e4ecf7;
}

.hero-label {
    font-size: 12px;
    color: #7c8aa5;
    margin-bottom: 3px;
}

.hero-sn {
    font-size: 16px;
    font-weight: 600;
    color: #2a3550;
    letter-spacing: 0.3px;
}

.hero-tags {
    display: flex;
    gap: 8px;
}

.section-card {
    margin-bottom: 12px;
    padding: 12px 12px 6px;
    border: 1px solid #ebeef5;
    border-radius: 8px;
    background: #fff;
}

.section-title {
    margin: 0 0 10px;
    font-size: 13px;
    font-weight: 600;
    color: #324057;
}

.form-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    column-gap: 14px;
}

.form-grid .full {
    grid-column: 1 / -1;
}

@media (max-width: 920px) {
    .order-hero {
        flex-direction: column;
        align-items: flex-start;
        gap: 8px;
    }

    .form-grid {
        grid-template-columns: 1fr;
    }
}
</style>
