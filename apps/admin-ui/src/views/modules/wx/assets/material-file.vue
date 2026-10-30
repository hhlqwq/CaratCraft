<template>
    <div class="mod-menu">
        <el-form :inline="true" :model="dataForm">
            <el-form-item v-show="!selectMode">
                <el-button
                    size="small"
                    v-if="isAuth('wx:wxassets:save')"
                    type="primary"
                    @click="addOrUpdateHandle()"
                    >新增
                </el-button>
            </el-form-item>
        </el-form>
        <div v-loading="dataListLoading">
            <div class="card" v-for="item in dataList" :key="item.mediaId" @click="onSelect(item)">
                <el-image
                    v-if="fileType === 'image'"
                    class="card-image"
                    :src="item.url"
                    fit="contain"
                    lazy
                ></el-image>
                <div v-else class="card-preview">
                    <div
                        v-if="fileType === 'voice'"
                        class="card-preview-icon el-icon-microphone"
                    ></div>
                    <div
                        v-if="fileType === 'video'"
                        class="card-preview-icon el-icon-video-camera-solid"
                    ></div>
                    <div class="card-preview-text">管理后台不支持预览<br />微信中可正常播放</div>
                </div>
                <div class="card-footer">
                    <div class="text-cut-name">{{ item.name }}</div>
                    <div>{{ item.updateTime }}</div>
                    <div class="flex justify-between align-center" v-show="!selectMode">
                        <el-button
                            size="small"
                            type="text"
                            icon="el-icon-copy-document"
                            v-clipboard:copy="item.mediaId"
                            v-clipboard:success="onCopySuccess"
                            v-clipboard:error="onCopyError"
                            >复制media_id
                        </el-button>
                        <el-button
                            size="small"
                            type="text"
                            icon="el-icon-delete"
                            @click="deleteHandle(item.mediaId)"
                            >删除
                        </el-button>
                    </div>
                </div>
            </div>
        </div>
        <el-pagination
            @current-change="currentChangeHandle"
            :current-page="pageIndex"
            :page-sizes="[20]"
            :page-size="20"
            :total="totalCount"
            layout="total, prev,pager, next, jumper"
        >
        </el-pagination>
        <!-- 弹窗, 新增 / 修改 -->
        <add-or-update
            v-if="addOrUpdateVisible"
            ref="addOrUpdate"
            @refreshDataList="onChange"
        ></add-or-update>
    </div>
</template>
<script>
import AddOrUpdate from './material-file-add-or-update';

export default {
    name: 'material-file',
    props: {
        fileType: {
            // image、voice、video
            type: String,
            default: 'image',
        },
        selectMode: {
            // 是否选择模式，选择模式下点击素材选中，不可新增和删除
            type: Boolean,
            default: false,
        },
    },
    components: {
        AddOrUpdate,
    },
    /** 初始化当前组件的响应式界面和表单状态. */
    data() {
        return {
            dataForm: {},
            addOrUpdateVisible: false,
            dataList: [],
            pageIndex: 1,
            pageSize: 20,
            totalCount: 0,
            dataListLoading: false,
        };
    },
    /** 挂载组件后初始化控件并读取所需数据. */
    mounted() {
        this.init();
    },
    methods: {
        /** 按传入上下文初始化当前界面的数据和选择状态. */
        init() {
            if (!this.dataList.length) {
                this.getDataList();
            }
        },
        /** 按当前筛选和分页条件读取业务列表. */
        getDataList() {
            if (this.dataListLoading) {
                return;
            }
            this.dataListLoading = true;
            this.$http({
                url: '/manage/wxAssets/materialFileBatchGet',
                params: {
                    page: this.pageIndex,
                    type: this.fileType,
                },
            }).then(({data}) => {
                if (data && data.code === 0) {
                    this.dataList = data.data.items;
                    this.totalCount = data.data.totalCount;
                } else {
                    this.$message.error(data.msg);
                }
                this.dataListLoading = false;
            });
        },
        // 新增 / 修改
        addOrUpdateHandle() {
            this.addOrUpdateVisible = true;
            this.$nextTick(() => {
                this.$refs.addOrUpdate.init(this.fileType);
            });
        },
        /** 应用当前选择结果并同步到所属表单. */
        onSelect(itemInfo) {
            if (!this.selectMode) {
                return;
            }
            this.$emit('selected', itemInfo);
        },
        // 删除
        deleteHandle(id) {
            this.$confirm(`确定对[mediaId=${id}]进行删除操作?`, '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning',
            }).then(() => {
                this.$http({
                    url: '/manage/wxAssets/materialDelete',
                    method: 'post',
                    data: {
                        mediaId: id,
                    },
                }).then(({data}) => {
                    if (data && data.code === 0) {
                        this.$message({
                            message: '操作成功',
                            type: 'success',
                            duration: 1500,
                        });
                        this.onChange();
                    }
                });
            });
        },
        // 当前页
        currentChangeHandle(val) {
            this.pageIndex = val;
            this.getDataList();
        },
        /** 提示素材地址已复制成功. */
        onCopySuccess() {
            this.$message.success('已复制');
        },
        /** 提示当前浏览器未能完成复制操作. */
        onCopyError() {
            this.$message.error('复制失败,可能是此浏览器不支持复制');
        },
        /** 从第一页重新读取素材列表并通知父组件内容已变更. */
        onChange() {
            this.pageIndex = 1;
            this.getDataList();
            this.$emit('change');
        },
    },
};
</script>
<style scoped>
.card {
    width: 170px;
    display: inline-block;
    background: #ffffff;
    border: 1px solid #ebeef5;
    box-shadow: 1px 1px 20px 0 rgba(0, 0, 0, 0.1);
    margin: 0 10px 10px 0;
    vertical-align: top;
    border-radius: 5px;
    box-sizing: border-box;
}

.card:hover {
    border: 2px solid #66b1ff;
    margin-bottom: 6px;
}

.card-image {
    line-height: 170px;
    max-height: 170px;
    width: 100%;
}

.card-preview {
    padding: 20px 0;
    color: #d9d9d9;
    display: flex;
    justify-content: center;
    align-items: center;
}

.card-preview-icon {
    font-size: 30px;
    margin-right: 5px;
}

.card-preview-text {
    font-size: 12px;
}

.card-footer {
    color: #ccc;
    font-size: 12px;
    padding: 15px 10px;
}
</style>
