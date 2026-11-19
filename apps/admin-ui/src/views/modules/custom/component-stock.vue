<template>
    <div class="mod-custom-component-stock">
        <el-form :inline="true" @keyup.enter="loadStock(1)">
            <el-form-item>
                <el-input v-model="goodsId" placeholder="商品 ID" clearable></el-input>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" @click="loadStock(1)">查询</el-button>
            </el-form-item>
        </el-form>

        <el-alert v-if="listError" :title="listError" type="error" :closable="false">
            <el-button @click="loadStock(requestedPage)">重新加载</el-button>
        </el-alert>
        <el-table v-loading="loading" :data="stockList" border style="width: 100%">
            <el-table-column prop="optionId" label="选项 ID" width="90"></el-table-column>
            <el-table-column prop="goodsId" label="商品 ID" width="100"></el-table-column>
            <el-table-column prop="componentType" label="组件类型" width="120"></el-table-column>
            <el-table-column prop="optionCode" label="选项代码" min-width="150"></el-table-column>
            <el-table-column prop="name" label="选项名称" min-width="140"></el-table-column>
            <el-table-column prop="priceDelta" label="加价(¥)" width="100"></el-table-column>
            <el-table-column prop="stock" label="库存" width="100">
                <template #default="scope">
                    <el-tag :type="scope.row.stock > 0 ? 'success' : 'danger'">{{
                        scope.row.stock
                    }}</el-tag>
                </template>
            </el-table-column>
            <el-table-column fixed="right" label="操作" width="120">
                <template #default="scope">
                    <el-button
                        v-if="isAuth('custom:stock:update')"
                        type="text"
                        size="small"
                        :disabled="loading || Boolean(listError)"
                        @click="openEdit(scope.row)"
                        >修改库存</el-button
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

        <el-dialog title="修改组件库存" v-model="editVisible" width="min(94vw, 420px)">
            <el-alert v-if="editError" :title="editError" type="error" :closable="false">
                <el-button :disabled="saving || loading" @click="reloadEdit"
                    >重新读取库存</el-button
                >
            </el-alert>
            <p>读取时库存: {{ editRow && editRow.stock }}. 保存失败后请重新读取并核对调整值.</p>
            <el-form label-width="90px">
                <el-form-item label="调整理由"
                    ><el-input v-model="editReason" maxlength="200" placeholder="补货、盘点等原因"
                /></el-form-item>
                <el-form-item label="选项">
                    <span
                        >{{ editRow && editRow.name }}（{{
                            editRow && editRow.componentType
                        }}）</span
                    >
                </el-form-item>
                <el-form-item label="库存">
                    <el-input-number
                        v-model="editStock"
                        :min="0"
                        :max="1000000000"
                        :step="1"
                    ></el-input-number>
                </el-form-item>
            </el-form>
            <template #footer
                ><span>
                    <el-button @click="editVisible = false">取消</el-button>
                    <el-button
                        type="primary"
                        :loading="saving"
                        :disabled="loading || editNeedsReload"
                        @click="saveStock()"
                        >确定</el-button
                    >
                </span></template
            >
        </el-dialog>
    </div>
</template>

<script>
export default {
    /** 保存列表与编辑上下文, 写入锁独立于读取状态. */
    data() {
        return {
            pageActive: true,
            pageEpoch: 0,
            loading: false,
            saving: false,
            listError: '',
            requestedPage: 1,
            editError: '',
            editNeedsReload: false,
            page: 1,
            total: 0,
            editReason: '',
            goodsId: '',
            stockList: [],
            editVisible: false,
            editRow: null,
            editStock: 0,
        };
    },
    /** 激活后重新读取库存, 不复用离页前的请求. */
    activated() {
        this.pageActive = true;
        this.pageEpoch++;
        this.loadStock();
    },
    /** 离页关闭编辑并废弃在途响应. */
    deactivated() {
        this.leavePage();
    },
    /** 销毁时释放当前页面状态. */
    beforeUnmount() {
        this.leavePage();
    },
    methods: {
        /** 结束页面生命周期, 避免旧回调影响重新进入后的操作. */
        leavePage() {
            this.pageActive = false;
            this.pageEpoch++;
            this.loading = false;
            this.editVisible = false;
        },
        /** 只展示最新查询成功页, 失败保留数据并允许明确重试. */
        async loadStock(page = this.page) {
            const version = (this._listVersion = (this._listVersion || 0) + 1);
            const epoch = this.pageEpoch;
            this.requestedPage = page;
            this.loading = true;
            this.listError = '';
            try {
                const {data} = await this.$http({
                    url: '/custom/component-stock/list',
                    method: 'get',
                    params: {
                        goodsId: this.goodsId || undefined,
                        page,
                        limit: 20,
                        todo: this.$route?.query?.todo,
                    },
                    showError: false,
                });
                if (!this.pageActive || epoch !== this.pageEpoch || version !== this._listVersion) {
                    return false;
                }
                if (!data || data.code !== 0 || !Array.isArray(data.data?.records)) {
                    throw new Error(data?.msg || '库存列表读取失败');
                }
                this.stockList = data.data.records;
                this.total = data.data.total;
                this.page = page;
                return true;
            } catch (error) {
                if (this.pageActive && epoch === this.pageEpoch && version === this._listVersion) {
                    this.listError = error.message || '库存列表读取失败';
                }
                return false;
            } finally {
                if (epoch === this.pageEpoch && version === this._listVersion) {
                    this.loading = false;
                }
            }
        },
        // 切换到指定库存页.
        changePage(page) {
            return this.loadStock(page);
        },
        // 打开库存编辑弹窗 / Opens the stock edit dialog.
        openEdit(row) {
            if (!this.pageActive || this.loading || this.listError) {
                return;
            }
            this._editVersion = (this._editVersion || 0) + 1;
            this.editError = '';
            this.editNeedsReload = false;
            this.editReason = '';
            this.editRow = {...row};
            this.editStock = row.stock;
            this.editVisible = true;
        },
        /** 明确重读后才更新并发基线, 不覆盖用户的目标库存及理由. */
        async reloadEdit() {
            if (this.saving || this.loading || !this.editVisible || !this.pageActive) {
                return;
            }
            const version = this._editVersion;
            const epoch = this.pageEpoch;
            const optionId = this.editRow.optionId;
            if (
                !(await this.loadStock()) ||
                !this.editVisible ||
                version !== this._editVersion ||
                epoch !== this.pageEpoch ||
                !this.pageActive
            ) {
                return;
            }
            const row = this.stockList.find((item) => item.optionId === optionId);
            if (!row) {
                this.editError = '当前页已无该选项, 请关闭弹窗后重新查询';
                return;
            }
            this.editRow = {...row};
            this.editNeedsReload = false;
            this.editError = '';
        },
        /** 校验并锁定一次库存调整, 失败必须重新读取后再决定是否提交. */
        async saveStock() {
            if (
                this.saving ||
                this.loading ||
                this.editNeedsReload ||
                !this.editVisible ||
                !this.pageActive ||
                !this.editRow
            ) {
                return;
            }
            if (
                !Number.isInteger(this.editStock) ||
                this.editStock < 0 ||
                this.editStock > 1000000000 ||
                !Number.isInteger(this.editRow.stock) ||
                this.editRow.stock < 0 ||
                !this.editReason.trim() ||
                this.editReason.length > 200
            ) {
                this.editError = '库存须为0到1000000000的整数, 理由须为1到200字';
                return;
            }
            const version = this._editVersion;
            const epoch = this.pageEpoch;
            this.saving = true;
            this.editError = '';
            try {
                const {data} = await this.$http({
                    url: '/custom/component-stock/update',
                    method: 'post',
                    showError: false,
                    params: {
                        optionId: this.editRow.optionId,
                        stock: this.editStock,
                        expectedStock: this.editRow.stock,
                        reason: this.editReason.trim(),
                    },
                });
                if (
                    !this.pageActive ||
                    epoch !== this.pageEpoch ||
                    !this.editVisible ||
                    version !== this._editVersion
                ) {
                    return;
                }
                if (!data || data.code !== 0) {
                    throw new Error(data?.msg || '更新失败');
                }
                this.$message.success('库存已更新');
                this.editVisible = false;
                await this.loadStock();
            } catch (error) {
                if (
                    this.pageActive &&
                    epoch === this.pageEpoch &&
                    this.editVisible &&
                    version === this._editVersion
                ) {
                    this.editError = (error.message || '更新结果未确认') + ', 请重新读取库存后核对';
                    this.editNeedsReload = true;
                }
            } finally {
                this.saving = false;
            }
        },
    },
};
</script>
