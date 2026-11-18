<template>
    <div class="mod-order">
        <el-button v-if="isAuth('mall:order:list')" @click="$refs.refunds.open()"
            >退款售后</el-button
        >
        <refund-panel ref="refunds" />
        <el-form :inline="true" :model="searchForm" @keyup.enter="getDataList(1)">
            <el-form-item>
                <el-input v-model="searchForm.orderSn" placeholder="订单编号" clearable></el-input>
            </el-form-item>
            <el-form-item>
                <el-input v-model="searchForm.nickname" placeholder="用户昵称" clearable></el-input>
            </el-form-item>
            <el-form-item>
                <el-select v-model="searchForm.orderStatus" placeholder="订单状态" clearable>
                    <el-option label="待付款" :value="0"></el-option>
                    <el-option label="待发货" :value="201"></el-option>
                    <el-option label="待收货" :value="300"></el-option>
                    <el-option label="已完成" :value="301"></el-option>
                    <el-option label="已取消" :value="101"></el-option>
                </el-select>
            </el-form-item>
            <el-form-item>
                <el-button @click="getDataList(1)">查询</el-button>
                <el-tag type="info">交易记录保留用于对账, 不支持手工新增或删除</el-tag>
            </el-form-item>
        </el-form>

        <el-alert
            v-if="listError || writeError"
            :title="listError || writeError"
            type="error"
            :closable="false"
        >
            <el-button @click="getDataList(requestedPage)">重新查询</el-button>
        </el-alert>
        <el-table v-loading="loading" :data="dataList" border style="width: 100%">
            <el-table-column
                prop="id"
                header-align="center"
                align="center"
                label="ID"
                width="80"
            ></el-table-column>
            <el-table-column
                prop="orderSn"
                header-align="center"
                align="center"
                label="订单编号"
                min-width="180"
            ></el-table-column>
            <el-table-column
                prop="userNickname"
                header-align="center"
                align="center"
                label="用户昵称"
                width="120"
            ></el-table-column>
            <el-table-column
                prop="consignee"
                header-align="center"
                align="center"
                label="收货人"
                width="100"
            ></el-table-column>
            <el-table-column
                prop="mobile"
                header-align="center"
                align="center"
                label="手机号"
                width="130"
            ></el-table-column>
            <el-table-column
                prop="actualPrice"
                header-align="center"
                align="center"
                label="实付金额"
                width="100"
            ></el-table-column>
            <el-table-column header-align="center" align="center" label="订单状态" width="100">
                <template #default="scope">
                    <el-tag size="small" :type="orderStatusType(scope.row.orderStatus)"
                        >{{ orderStatusText(scope.row.orderStatus) }}
                    </el-tag>
                </template>
            </el-table-column>
            <el-table-column header-align="center" align="center" label="支付状态" width="100">
                <template #default="scope">
                    <el-tag size="small" :type="payStatusType(scope.row.payStatus)"
                        >{{ payStatusText(scope.row.payStatus) }}
                    </el-tag>
                </template>
            </el-table-column>
            <el-table-column header-align="center" align="center" label="发货状态" width="100">
                <template #default="scope">
                    <el-tag size="small" :type="shippingStatusType(scope.row.shippingStatus)">
                        {{ shippingStatusText(scope.row.shippingStatus) }}
                    </el-tag>
                </template>
            </el-table-column>
            <el-table-column
                prop="addTime"
                header-align="center"
                align="center"
                label="下单时间"
                min-width="160"
            ></el-table-column>
            <el-table-column
                fixed="right"
                header-align="center"
                align="center"
                width="220"
                label="操作"
            >
                <template #default="scope">
                    <el-button
                        v-if="isAuth('mall:order:info')"
                        type="text"
                        size="small"
                        @click="showDetails(scope.row.id)"
                        >查看
                    </el-button>
                    <el-button
                        v-if="isAuth('mall:order:update') && canAdjustPrice(scope.row)"
                        type="text"
                        size="small"
                        :disabled="writing || loading || needsReload || Boolean(listError)"
                        @click="adjustPrice(scope.row)"
                        >改价
                    </el-button>
                    <el-button
                        v-if="isAuth('mall:order:ship') && canShip(scope.row)"
                        type="text"
                        size="small"
                        :disabled="writing || loading || needsReload || Boolean(listError)"
                        @click="shipOrder(scope.row)"
                        >发货
                    </el-button>
                </template>
            </el-table-column>
        </el-table>

        <el-pagination
            @size-change="sizeChangeHandle"
            @current-change="currentChangeHandle"
            :current-page="pageIndex"
            :page-sizes="[10, 20, 50, 100]"
            :page-size="pageSize"
            :total="totalPage"
            layout="total, sizes, prev, pager, next, jumper"
        >
        </el-pagination>

        <order-detail v-if="detailVisible" ref="orderDetail"></order-detail>

        <el-dialog
            title="订单发货"
            v-model="shipDialogVisible"
            width="420px"
            :close-on-click-modal="false"
        >
            <el-alert v-if="companyError" :title="companyError" type="error" :closable="false">
                <el-button @click="loadShippingCompanies">重新读取快递公司</el-button>
            </el-alert>
            <el-form
                :disabled="writing"
                :model="shipForm"
                :rules="shipRule"
                ref="shipForm"
                label-width="90px"
            >
                <el-form-item label="快递公司" prop="shippingName">
                    <el-select
                        v-model="shipForm.shippingName"
                        placeholder="请选择快递公司"
                        filterable
                        style="width: 100%"
                    >
                        <el-option
                            v-for="item in shippingCompanyOptions"
                            :key="item.id"
                            :label="item.name"
                            :value="item.name"
                        >
                        </el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="快递单号" prop="shippingNo">
                    <el-input
                        v-model.trim="shipForm.shippingNo"
                        placeholder="请输入快递单号"
                    ></el-input>
                </el-form-item>
            </el-form>
            <template #footer
                ><span class="dialog-footer">
                    <el-button @click="shipDialogVisible = false">取消</el-button>
                    <el-button
                        type="primary"
                        :loading="writing"
                        :disabled="loading || needsReload || Boolean(listError)"
                        @click="submitShip()"
                        >确认发货</el-button
                    >
                </span></template
            >
        </el-dialog>
    </div>
</template>

<script>
import OrderDetail from './order-detail';
import RefundPanel from './refund-panel';

export default {
    components: {OrderDetail, RefundPanel},
    /** 保存分页及独立读写状态, 不保留通用订单新增删除入口. */
    data() {
        return {
            pageActive: true,
            pageEpoch: 0,
            loading: false,
            writing: false,
            listError: '',
            writeError: '',
            companyError: '',
            needsReload: false,
            requestedPage: 1,
            pageIndex: 1,
            pageSize: 10,
            loadedPageSize: 10,
            totalPage: 0,
            searchForm: {orderSn: '', nickname: '', orderStatus: ''},
            dataList: [],
            detailVisible: false,
            shipDialogVisible: false,
            shipForm: {id: '', shippingName: '', shippingNo: ''},
            priceDraft: {id: null, price: '', reason: ''},
            shippingCompanyOptions: [],
            shipRule: {
                shippingName: [
                    {required: true, message: '请选择快递公司', trigger: 'change'},
                    {max: 100, message: '快递公司最多100字', trigger: 'change'},
                ],
                shippingNo: [
                    {required: true, message: '请输入快递单号', trigger: 'blur'},
                    {max: 32, message: '快递单号最多32字', trigger: 'blur'},
                ],
            },
        };
    },
    /** 每次激活重新读取实际订单与快递公司. */
    activated() {
        this.pageActive = true;
        this.pageEpoch++;
        this.loadShippingCompanies();
        this.getDataList();
    },
    /** 离页后关闭子弹窗并废弃旧请求. */
    deactivated() {
        this.leavePage();
    },
    /** 销毁时结束所有页面交互上下文. */
    beforeUnmount() {
        this.leavePage();
    },
    methods: {
        /** 关闭旧弹窗, 不允许迟到确认或写入结果影响重新进入的页面. */
        leavePage() {
            this.pageActive = false;
            this.pageEpoch++;
            this.loading = false;
            this.shipDialogVisible = false;
            this.$refs.orderDetail?.close?.();
            this.$refs.refunds?.close?.();
        },
        /** 快递字典读取失败提供独立重试, 不影响订单列表分页. */
        async loadShippingCompanies() {
            const epoch = this.pageEpoch;
            const version = (this._companyVersion = (this._companyVersion || 0) + 1);
            this.companyError = '';
            try {
                const {data} = await this.$http({
                    url: '/mall/shipping/queryAll',
                    method: 'get',
                    showError: false,
                });
                if (
                    !this.pageActive ||
                    epoch !== this.pageEpoch ||
                    version !== this._companyVersion
                ) {
                    return;
                }
                if (!data || data.code !== 0 || !Array.isArray(data.data)) {
                    throw new Error(data?.msg || '快递公司读取失败');
                }
                this.shippingCompanyOptions = data.data;
            } catch (error) {
                if (
                    this.pageActive &&
                    epoch === this.pageEpoch &&
                    version === this._companyVersion
                ) {
                    this.companyError = error.message || '快递公司读取失败';
                }
            }
        },
        /** 最新查询成功后才切换页码, 失败时保留上次已确认的数据. */
        async getDataList(page = this.pageIndex) {
            const version = (this._listVersion = (this._listVersion || 0) + 1);
            const epoch = this.pageEpoch;
            const limit = this.pageSize;
            this.loading = true;
            this.listError = '';
            this.requestedPage = page;
            try {
                const {data} = await this.$http({
                    url: '/mall/order/list',
                    method: 'get',
                    showError: false,
                    params: {page, limit, ...this.searchForm},
                });
                if (!this.pageActive || epoch !== this.pageEpoch || version !== this._listVersion) {
                    return;
                }
                if (!data || data.code !== 0 || !Array.isArray(data.data?.records)) {
                    throw new Error(data?.msg || '订单读取失败');
                }
                this.dataList = data.data.records;
                this.totalPage = data.data.total;
                this.pageIndex = page;
                this.loadedPageSize = limit;
                this.needsReload = false;
                this.writeError = '';
            } catch (error) {
                if (this.pageActive && epoch === this.pageEpoch && version === this._listVersion) {
                    this.listError = error.message || '订单读取失败';
                    this.pageSize = this.loadedPageSize;
                }
            } finally {
                if (epoch === this.pageEpoch && version === this._listVersion) {
                    this.loading = false;
                }
            }
        },
        /** 调整每页数量后从第一页读取, 不预先覆盖已展示页码. */
        sizeChangeHandle(value) {
            this.pageSize = value;
            return this.getDataList(1);
        },
        /** 分页切换复用同一读取代次. */
        currentChangeHandle(value) {
            return this.getDataList(value);
        },
        /** 打开只读订单详情, 下一帧执行前复核页面生命周期. */
        showDetails(id) {
            const epoch = this.pageEpoch;
            const version = (this._detailVersion = (this._detailVersion || 0) + 1);
            this.detailVisible = true;
            this.$nextTick(() => {
                if (
                    this.pageActive &&
                    epoch === this.pageEpoch &&
                    version === this._detailVersion
                ) {
                    this.$refs.orderDetail.init(id, true);
                }
            });
        },
        /** 显示服务端订单状态. */
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
        /** 显示支付状态而非推测到账结果. */
        payStatusText(status) {
            const map = {0: '未支付', 1: '预支付已创建', 2: '已支付', 3: '退款中', 4: '已退款'};
            return map[status] || `状态${status}`;
        },
        /** 按支付状态选择提示色. */
        payStatusType(status) {
            const map = {0: 'warning', 1: 'warning', 2: 'success', 3: 'danger', 4: 'danger'};
            return map[status] || 'info';
        },
        /** 显示服务端发货状态. */
        shippingStatusText(status) {
            const map = {0: '未发货', 1: '已发货', 2: '已收货'};
            return map[status] || `状态${status}`;
        },
        /** 按发货状态选择提示色. */
        shippingStatusType(status) {
            const map = {0: 'info', 1: 'success', 2: 'success'};
            return map[status] || 'info';
        },
        /** 仅尚未预支付的待付款订单可改价. */
        canAdjustPrice(row) {
            return (
                Number(row.payStatus) === 0 &&
                Number(row.orderStatus) === 0 &&
                (!row.payId || row.payId === '0')
            );
        },
        /** 仅已支付且待发货订单可申请发货. */
        canShip(row) {
            return (
                Number(row.payStatus) === 2 &&
                Number(row.orderStatus) === 201 &&
                Number(row.shippingStatus) === 0
            );
        },
        /** 写入失败后废弃先前发出的列表读取, 只接受用户明确重查的结果. */
        requireReload(text) {
            this._listVersion = (this._listVersion || 0) + 1;
            this.loading = false;
            this.needsReload = true;
            this.writeError = text + ', 请重新查询核对';
        },
        /** 从金额确认起防重, 两次确认后再复核当前订单支付状态. */
        async adjustPrice(row) {
            if (
                this.writing ||
                this.loading ||
                this.listError ||
                this.needsReload ||
                !this.pageActive ||
                !this.canAdjustPrice(row)
            ) {
                return;
            }
            const epoch = this.pageEpoch;
            const id = row.id;
            this.writing = true;
            let submitted = false;
            try {
                const {value} = await this.$prompt(
                    '请输入新的实付金额, 最多两位小数',
                    '支付前改价',
                    {
                        inputValue:
                            this.priceDraft.id === id ? this.priceDraft.price : row.actualPrice,
                        inputValidator: (value) =>
                            (/^(0|[1-9]\d{0,6})(\.\d{1,2})?$/.test(value) &&
                                Number(value) >= 0.01) ||
                            '金额须大于零且最多两位小数',
                    }
                );
                if (!this.pageActive || epoch !== this.pageEpoch) {
                    return;
                }
                if (!/^(0|[1-9]\d{0,6})(\.\d{1,2})?$/.test(value) || Number(value) < 0.01) {
                    throw new Error('金额须为0.01到9999999.99且最多两位小数');
                }
                this.priceDraft = {
                    id,
                    price: value,
                    reason: this.priceDraft.id === id ? this.priceDraft.reason : '',
                };
                const result = await this.$prompt('请填写改价理由, 最多200字', '记录改价理由', {
                    inputValue: this.priceDraft.reason,
                    inputValidator: (reason) =>
                        Boolean(reason && reason.trim() && reason.length <= 200) ||
                        '请填写1到200字的理由',
                });
                if (!this.pageActive || epoch !== this.pageEpoch) {
                    return;
                }
                this.priceDraft.reason = result.value;
                if (!result.value?.trim() || result.value.length > 200) {
                    throw new Error('请填写1到200字的理由');
                }
                const current = this.dataList.find((item) => item.id === id);
                if (
                    this.loading ||
                    this.listError ||
                    this.needsReload ||
                    !current ||
                    !this.canAdjustPrice(current)
                ) {
                    throw new Error('订单状态已变化, 请重新查询后操作');
                }
                submitted = true;
                const {data} = await this.$http({
                    url: '/mall/order/adjustPrice',
                    method: 'post',
                    showError: false,
                    data: {id, actualPrice: value, reason: result.value.trim()},
                });
                if (!this.pageActive || epoch !== this.pageEpoch) {
                    return;
                }
                if (!data || data.code !== 0) {
                    throw new Error(data?.msg || '改价失败');
                }
                this.priceDraft = {id: null, price: '', reason: ''};
                this.$message.success('改价成功, 已记录变更流水');
                await this.getDataList();
            } catch (error) {
                if (
                    this.pageActive &&
                    epoch === this.pageEpoch &&
                    error !== 'cancel' &&
                    error !== 'close'
                ) {
                    const text = error.message || '改价结果未确认';
                    if (submitted) {
                        this.requireReload(text);
                    }
                    this.$message.error(text);
                }
            } finally {
                this.writing = false;
            }
        },
        /** 复制选中订单的发货输入, 每次打开都废弃旧弹窗回调. */
        shipOrder(row) {
            if (
                this.loading ||
                this.listError ||
                this.needsReload ||
                !this.pageActive ||
                !this.canShip(row)
            ) {
                return;
            }
            this._shipVersion = (this._shipVersion || 0) + 1;
            this.shipForm = {
                id: row.id,
                shippingName: row.shippingName || '',
                shippingNo: row.shippingNo || '',
            };
            this.shipDialogVisible = true;
            const version = this._shipVersion;
            this.$nextTick(() => {
                if (this.shipDialogVisible && version === this._shipVersion) {
                    this.$refs.shipForm?.clearValidate();
                }
            });
        },
        /** 校验期间即持锁, 请求绑定当前订单并处理网络及业务失败. */
        async submitShip() {
            if (
                this.writing ||
                this.loading ||
                this.listError ||
                this.needsReload ||
                !this.pageActive ||
                !this.shipDialogVisible
            ) {
                return;
            }
            const epoch = this.pageEpoch;
            const version = this._shipVersion;
            const id = this.shipForm.id;
            this.writing = true;
            let submitted = false;
            try {
                const valid = await this.$refs.shipForm.validate().catch(() => false);
                if (
                    !valid ||
                    !this.pageActive ||
                    epoch !== this.pageEpoch ||
                    !this.shipDialogVisible ||
                    version !== this._shipVersion
                ) {
                    return;
                }
                const current = this.dataList.find((item) => item.id === id);
                if (
                    this.loading ||
                    this.listError ||
                    this.needsReload ||
                    !current ||
                    !this.canShip(current)
                ) {
                    throw new Error('订单状态已变化, 请重新查询后发货');
                }
                const shippingName = this.shipForm.shippingName.trim();
                const shippingNo = this.shipForm.shippingNo.trim();
                if (
                    !shippingName ||
                    shippingName.length > 100 ||
                    !shippingNo ||
                    shippingNo.length > 32
                ) {
                    throw new Error('请填写有效的快递公司及32字以内单号');
                }
                submitted = true;
                const {data} = await this.$http({
                    url: '/mall/order/ship',
                    method: 'post',
                    showError: false,
                    data: {id, shippingName, shippingNo},
                });
                if (
                    !this.pageActive ||
                    epoch !== this.pageEpoch ||
                    !this.shipDialogVisible ||
                    version !== this._shipVersion
                ) {
                    return;
                }
                if (!data || data.code !== 0) {
                    throw new Error(data?.msg || '发货失败');
                }
                this.$message.success('发货成功');
                this.shipDialogVisible = false;
                await this.getDataList();
            } catch (error) {
                if (
                    this.pageActive &&
                    epoch === this.pageEpoch &&
                    this.shipDialogVisible &&
                    version === this._shipVersion
                ) {
                    const text = error.message || '发货结果未确认';
                    if (submitted) {
                        this.requireReload(text);
                    }
                    this.$message.error(text);
                }
            } finally {
                this.writing = false;
            }
        },
    },
};
</script>
