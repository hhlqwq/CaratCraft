<template>
    <div class="mod-log">
        <el-form :inline="true" :model="searchForm" @keyup.enter="getDataList(1)">
            <el-form-item>
                <el-input
                    v-model="searchForm.key"
                    placeholder="用户名／用户操作"
                    clearable
                ></el-input>
            </el-form-item>
            <el-form-item>
                <el-input v-model="searchForm.ip" placeholder="所属IP" clearable></el-input>
            </el-form-item>
            <el-form-item label="操作结果"
                ><el-select v-model="searchForm.result" clearable style="width: 120px"
                    ><el-option label="成功" value="SUCCESS" /><el-option
                        label="失败"
                        value="FAILED" /></el-select
            ></el-form-item>
            <el-form-item label="操作类型"
                ><el-select v-model="searchForm.type" clearable style="width: 120px"
                    ><el-option label="登录" value="LOGIN" /><el-option
                        label="变更"
                        value="CHANGE" /></el-select
            ></el-form-item>
            <el-form-item label="模块"
                ><el-input v-model.trim="searchForm.module" clearable
            /></el-form-item>
            <el-form-item label="起始日期"
                ><el-date-picker v-model="searchForm.start" value-format="YYYY-MM-DD"
            /></el-form-item>
            <el-form-item label="结束日期"
                ><el-date-picker v-model="searchForm.end" value-format="YYYY-MM-DD"
            /></el-form-item>
            <el-form-item>
                <el-button @click="getDataList(1)">查询</el-button>
            </el-form-item>
        </el-form>
        <el-table :data="dataList" border style="width: 100%">
            <el-table-column prop="userName" header-align="center" align="center" label="用户名">
            </el-table-column>
            <el-table-column prop="operation" header-align="center" align="center" label="用户操作">
            </el-table-column>
            <el-table-column
                prop="module"
                header-align="center"
                align="center"
                width="150"
                show-tooltip-when-overflow
                label="操作模块"
            >
            </el-table-column>
            <el-table-column
                prop="summary"
                header-align="center"
                align="center"
                width="150"
                show-tooltip-when-overflow
                label="安全摘要"
            >
            </el-table-column>
            <el-table-column prop="result" label="结果" /><el-table-column
                prop="targetId"
                label="操作对象"
                show-overflow-tooltip
            />
            <el-table-column
                prop="time"
                header-align="center"
                align="center"
                label="执行时长(毫秒)"
            >
            </el-table-column>
            <el-table-column
                prop="ip"
                header-align="center"
                align="center"
                width="150"
                label="所属IP"
            >
            </el-table-column>
            <el-table-column
                prop="createTime"
                header-align="center"
                align="center"
                label="创建时间"
            >
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
                key: '',
                ip: '',
                result: '',
                type: '',
                module: '',
                start: '',
                end: '',
            },
            dataList: [],
            pageIndex: 1,
            pageSize: 10,
            totalPage: 0,
            selectionDataList: [],
        };
    },
    /** 创建组件时准备所需的依赖数据. */
    created() {
        this.getDataList();
    },
    methods: {
        // 获取数据列表
        getDataList(page) {
            if (page) {
                this.pageIndex = page;
            }
            this.$http({
                url: '/sys/log/list',
                method: 'get',
                params: {
                    page: this.pageIndex,
                    limit: this.pageSize,
                    key: this.searchForm.key,
                    ip: this.searchForm.ip,
                    result: this.searchForm.result,
                    type: this.searchForm.type,
                    module: this.searchForm.module,
                    start: this.searchForm.start,
                    end: this.searchForm.end,
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
        // 每页数
        sizeChangeHandle(val) {
            this.pageSize = val;
            this.pageIndex = 1;
            this.getDataList();
        },
        // 当前页
        currentChangeHandle(val) {
            this.pageIndex = val;
            this.getDataList();
        },
    },
};
</script>
