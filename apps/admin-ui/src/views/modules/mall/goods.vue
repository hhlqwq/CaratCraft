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
                    >新增</el-button
                >
                <el-upload
                    v-if="isAuth('mall:goods:save')"
                    class="inline-upload"
                    action=""
                    accept=".json"
                    :auto-upload="false"
                    :show-file-list="false"
                    :on-change="importGoodsJson"
                >
                    <el-button type="success">导入完整商品 JSON</el-button>
                </el-upload>
                <el-button type="text" @click="downloadGoodsTemplate">下载导入模板</el-button>
                <el-button
                    v-if="isAuth('mall:goods:delete')"
                    type="danger"
                    @click="deleteHandle()"
                    :disabled="dataListSelections.length <= 0"
                    >批量删除
                </el-button>
            </el-form-item>
        </el-form>

        <el-table
            :data="dataList"
            border
            @selection-change="selectionChangeHandle"
            style="width: 100%"
        >
            <el-table-column
                type="selection"
                header-align="center"
                align="center"
                width="50"
            ></el-table-column>
            <el-table-column
                prop="id"
                header-align="center"
                align="center"
                label="ID"
                width="80"
            ></el-table-column>
            <el-table-column
                prop="name"
                header-align="center"
                align="left"
                label="商品名称"
                min-width="220"
            ></el-table-column>
            <el-table-column
                prop="goodsSn"
                header-align="center"
                align="center"
                label="商品编码"
                min-width="160"
            ></el-table-column>
            <el-table-column
                prop="retailPrice"
                header-align="center"
                align="center"
                label="零售价"
                width="110"
            ></el-table-column>
            <el-table-column
                prop="marketPrice"
                header-align="center"
                align="center"
                label="市场价"
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
                label="上架"
                width="90"
            >
                <template #default="scope">
                    <el-tag :type="scope.row.isOnSale === 1 ? 'success' : 'info'" size="small">
                        {{ scope.row.isOnSale === 1 ? '是' : '否' }}
                    </el-tag>
                </template>
            </el-table-column>
            <el-table-column
                fixed="right"
                header-align="center"
                align="center"
                width="180"
                label="操作"
            >
                <template #default="scope">
                    <el-button
                        v-if="isAuth('mall:goods:info')"
                        type="text"
                        size="small"
                        @click="showDetails(scope.row.id)"
                        >查看
                    </el-button>
                    <el-button
                        v-if="isAuth('mall:goods:update')"
                        type="text"
                        size="small"
                        @click="addOrUpdateHandle(scope.row.id)"
                        >编辑
                    </el-button>
                    <el-button
                        v-if="isAuth('mall:goods:delete')"
                        type="text"
                        size="small"
                        @click="deleteHandle(scope.row.id)"
                    >
                        删除
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

        <add-or-update
            v-if="addOrUpdateVisible"
            ref="addOrUpdate"
            @refreshDataList="getDataList"
        ></add-or-update>
    </div>
</template>

<script>
import AddOrUpdate from './goods-add-or-update';

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
            dataListSelections: [],
            addOrUpdateVisible: false,
        };
    },
    components: {
        AddOrUpdate,
    },
    /** 页面激活时刷新当前业务数据. */
    activated() {
        this.getDataList();
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
        /** 保存表格当前勾选项以供后续批量操作. */
        selectionChangeHandle(val) {
            this.dataListSelections = val;
        },
        /** 以只读方式打开指定记录的详情. */
        showDetails(id) {
            this.addOrUpdateVisible = true;
            this.$nextTick(() => {
                this.$refs.addOrUpdate.init(id, true);
            });
        },
        /** 打开新增或编辑弹窗并传入目标记录. */
        addOrUpdateHandle(id) {
            this.addOrUpdateVisible = true;
            this.$nextTick(() => {
                this.$refs.addOrUpdate.init(id);
            });
        },
        /** 确认后删除指定或勾选的业务记录. */
        deleteHandle(id) {
            const ids = id ? [id] : this.dataListSelections.map((item) => item.id);
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
        // 导入商品、相册、参数、规格和 SKU / Imports a complete product aggregate from JSON.
        importGoodsJson(file) {
            const reader = new FileReader();
            reader.onload = (event) => {
                try {
                    const aggregate = JSON.parse(event.target.result);
                    if (!aggregate.goods || !aggregate.goods.name || !aggregate.goods.categoryId) {
                        throw new Error('JSON 必须包含 goods.name 和 goods.categoryId');
                    }
                    aggregate.goods.id = null;
                    this.$confirm(
                        `确认导入商品“${aggregate.goods.name}”及其相册、参数、规格和 SKU？`,
                        '导入确认',
                        {
                            type: 'warning',
                        }
                    )
                        .then(() =>
                            this.$http({
                                url: '/mall/goods/aggregate/save',
                                method: 'post',
                                data: aggregate,
                                showError: false,
                            })
                        )
                        .then(({data}) => {
                            if (!data || data.code !== 0) {
                                throw new Error((data && data.msg) || '导入失败');
                            }
                            this.$message.success(`商品导入成功，商品 ID：${data.data}`);
                            this.getDataList(1);
                        })
                        .catch((error) => {
                            if (error !== 'cancel' && error !== 'close') {
                                this.$message.error(`导入失败：${error.message || error}`);
                            }
                        });
                } catch (error) {
                    this.$message.error(`JSON 解析失败：${error.message}`);
                }
            };
            reader.onerror = () => this.$message.error('无法读取所选 JSON 文件');
            reader.readAsText(file.raw, 'UTF-8');
        },
        // 下载完整商品导入模板 / Downloads a complete product import template.
        downloadGoodsTemplate() {
            const template = {
                goods: {
                    name: '演示商品名称',
                    categoryId: 1999001,
                    pearlSource: '',
                    jewelryType: '',
                    pearlVariety: '',
                    pearlOrigin: '',
                    pearlShape: '',
                    pearlLuster: '',
                    pearlBlemish: '',
                    pearlDiameterMin: null,
                    pearlDiameterMax: null,
                    goodsSn: 'DEMO-IMPORT-001',
                    goodsNumber: 10,
                    retailPrice: 999,
                    marketPrice: 1299,
                    isOnSale: 0,
                    isNew: 1,
                    isHot: 0,
                    goodsBrief: '商品简介',
                    goodsDesc: '<p>商品详情</p>',
                    primaryPicUrl: '',
                    listPicUrl: '',
                    goodsUnit: '件',
                },
                attributes: [],
                gallery: [{imgUrl: '', imgDesc: '主图', sortOrder: 1}],
                specs: [],
                skuList: [
                    {
                        goodsSn: 'DEMO-IMPORT-001-STD',
                        goodsNumber: 10,
                        retailPrice: 999,
                        marketPrice: 1299,
                    },
                ],
            };
            this.downloadJson('完整商品导入模板.json', template);
        },
        // 下载 JSON 文件 / Downloads a JSON file.
        downloadJson(filename, data) {
            const blob = new Blob([JSON.stringify(data, null, 2)], {
                type: 'application/json;charset=utf-8',
            });
            const link = document.createElement('a');
            link.href = URL.createObjectURL(blob);
            link.download = filename;
            link.click();
            URL.revokeObjectURL(link.href);
        },
    },
};
</script>

<style scoped>
.inline-upload {
    display: inline-block;
    margin-left: 10px;
}
</style>
