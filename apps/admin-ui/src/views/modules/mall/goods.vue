<template>
    <div class="mod-goods">
        <el-form :inline="true" :model="searchForm" @keyup.enter="getDataList(1)">
            <el-form-item>
                <el-input v-model="searchForm.name" placeholder="商品名称" clearable></el-input>
            </el-form-item>
            <el-form-item>
                <el-button @click="getDataList(1)">查询</el-button>
                <el-button
                    v-if="isAuth('mall:goods:save')"
                    type="primary"
                    @click="addOrUpdateHandle()"
                    >新建商品</el-button
                >
            </el-form-item>
        </el-form>

        <el-table :data="dataList" border style="width: 100%">
            <el-table-column label="主图" width="105" align="center">
                <template #default="{row}">
                    <el-image
                        v-if="row.primaryPicUrl || row.listPicUrl"
                        class="product-thumb"
                        :src="row.primaryPicUrl || row.listPicUrl"
                        fit="cover"
                    >
                        <template #error><span class="image-pending">待上传图片</span></template>
                    </el-image>
                    <span v-else class="image-pending">待上传图片</span>
                </template>
            </el-table-column>
            <el-table-column
                prop="name"
                header-align="center"
                align="left"
                label="商品名称"
                min-width="220"
            ></el-table-column>
            <el-table-column label="品类" min-width="105">
                <template #default="{row}">{{ categoryLabel(row.jewelryType) }}</template>
            </el-table-column>
            <el-table-column
                prop="retailPrice"
                header-align="center"
                align="center"
                label="零售价"
                width="110"
            ></el-table-column>
            <el-table-column
                prop="goodsNumber"
                header-align="center"
                align="center"
                label="库存"
                width="90"
            ></el-table-column>
            <el-table-column
                prop="isOnSale"
                header-align="center"
                align="center"
                label="销售状态"
                width="90"
            >
                <template #default="scope">
                    <el-tag :type="scope.row.isOnSale === 1 ? 'success' : 'info'" size="small">
                        {{ scope.row.isOnSale === 1 ? '已上架' : '草稿／下架' }}
                    </el-tag>
                </template>
            </el-table-column>
            <el-table-column fixed="right" align="center" width="240" label="操作">
                <template #default="{row}">
                    <div class="product-actions">
                        <el-button
                            v-if="isAuth('mall:goods:info')"
                            link
                            type="primary"
                            @click="showDetails(row.id)"
                            >查看</el-button
                        >
                        <el-button
                            v-if="isAuth('mall:goods:update') && isAuth('mall:goods:info')"
                            link
                            type="primary"
                            @click="addOrUpdateHandle(row.id)"
                            >编辑</el-button
                        >
                        <el-button
                            v-if="isAuth('mall:goods:update')"
                            plain
                            size="small"
                            :disabled="salePending !== null"
                            @click="changeSale(row)"
                        >
                            {{ row.isOnSale === 1 ? '下架' : '上架' }}
                        </el-button>
                        <el-dropdown v-if="isAuth('mall:goods:delete')" trigger="click">
                            <el-button link aria-label="更多商品操作">更多</el-button>
                            <template #dropdown>
                                <el-dropdown-menu>
                                    <el-dropdown-item @click="deleteHandle(row.id)">
                                        移入回收站
                                    </el-dropdown-item>
                                </el-dropdown-menu>
                            </template>
                        </el-dropdown>
                    </div>
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
    </div>
</template>

<script>
export default {
    /** 初始化当前组件的响应式界面和表单状态. */
    data() {
        return {
            searchForm: {
                name: '',
            },
            dataList: [],
            pageIndex: 1,
            pageSize: 10,
            totalPage: 0,
            salePending: null,
        };
    },
    /** 页面激活时刷新当前业务数据. */
    activated() {
        const query = this.$route.query;
        this.searchForm.name = query.name || '';
        this.pageSize = [10, 20, 50, 100].includes(Number(query.limit)) ? Number(query.limit) : 10;
        this.getDataList(Math.max(1, Number.parseInt(query.page, 10) || 1));
    },
    methods: {
        /** 按当前筛选和分页条件读取业务列表. */
        getDataList(page) {
            if (page) {
                this.pageIndex = page;
            }
            this.$http({
                url: '/mall/goods/list',
                method: 'get',
                params: {
                    page: this.pageIndex,
                    limit: this.pageSize,
                    name: this.searchForm.name,
                },
            }).then(({data}) => {
                if (data && data.code === 0) {
                    this.dataList = data.data.records;
                    this.totalPage = data.data.total;
                    if (this.$route.name === 'mall-goods') {
                        this.$router.replace({
                            name: 'mall-goods',
                            query: {
                                name: this.searchForm.name || undefined,
                                page: this.pageIndex > 1 ? String(this.pageIndex) : undefined,
                                limit: this.pageSize === 10 ? undefined : String(this.pageSize),
                            },
                        });
                    }
                } else {
                    this.dataList = [];
                    this.totalPage = 0;
                }
            });
        },
        /** 调整每页数量并重新读取列表. */
        sizeChangeHandle(val) {
            this.pageSize = val;
            this.pageIndex = 1;
            this.getDataList();
        },
        /** 切换当前页码并读取对应列表. */
        currentChangeHandle(val) {
            this.pageIndex = val;
            this.getDataList();
        },
        /** 显示现有成品品类, 历史空值留待商家补齐. */
        categoryLabel(type) {
            return (
                {NECKLACE: '项链', EARRING: '耳饰', BRACELET: '手链', RING: '戒指'}[type] ||
                '待填写'
            );
        },
        /** 上架或下架只请求修改商品销售状态. */
        async changeSale(row) {
            if (this.salePending !== null) return;
            this.salePending = row.id;
            try {
                const onSale = row.isOnSale !== 1;
                const {data} = await this.$http({
                    url: '/mall/goods/sale',
                    method: 'post',
                    data: {id: row.id, onSale},
                    showError: false,
                });
                if (data?.code !== 0) throw new Error(data?.msg || '操作失败，请重试');
                this.$message.success(onSale ? '商品已上架' : '商品已下架');
                this.getDataList();
            } catch (error) {
                this.$message.error(error.response?.data?.msg || error.message);
            } finally {
                this.salePending = null;
            }
        },
        /** 路由进入商品详情, 保留名称筛选和分页. */
        showDetails(id) {
            return this.$router.push({
                name: 'mall-goods-detail',
                params: {id},
                query: this.$route.query,
            });
        },
        /** 在独立路径编辑或新增商品, 从列表带入可恢复查询. */
        addOrUpdateHandle(id) {
            return this.$router.push(
                id
                    ? {name: 'mall-goods-edit', params: {id}, query: this.$route.query}
                    : {name: 'mall-goods-new', query: this.$route.query}
            );
        },
        /** 二次确认后将指定商品移入回收站. */
        deleteHandle(id) {
            const ids = [id];
            this.$confirm(
                '商品将从商城下架并移入回收站，三十天内可恢复。是否继续？',
                '移入回收站',
                {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning',
                }
            )
                .then(() => {
                    this.$http({
                        url: '/mall/goods/delete',
                        method: 'post',
                        data: ids,
                    }).then(({data}) => {
                        if (data && data.code === 0) {
                            this.$message({
                                message: data.msg || '商品已移入回收站',
                                type: 'success',
                                duration: 1500,
                            });
                            this.getDataList();
                        }
                    });
                })
                .catch(() => {});
        },
    },
};
</script>

<style scoped>
.product-actions {
    display: flex;
    gap: 10px;
    align-items: center;
    justify-content: center;
    white-space: nowrap;
}
.product-actions :deep(.el-button) {
    margin-left: 0;
}
.product-thumb,
.image-pending {
    width: 72px;
    height: 56px;
    border-radius: 4px;
}
.image-pending {
    display: inline-grid;
    place-items: center;
    color: #52615a;
    background: #f0eee8;
    font-size: 12px;
}
</style>
