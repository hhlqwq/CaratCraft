<template>
    <main>
        <el-form inline @submit.prevent="search">
            <el-form-item label="操作账号"
                ><el-input v-model.trim="filters.account" clearable
            /></el-form-item>
            <el-form-item label="模块"
                ><el-select v-model="filters.module" clearable style="width: 150px"
                    ><el-option
                        v-for="module in ['账户与权限', '身份认证', '系统操作', '系统维护']"
                        :key="module"
                        :label="module"
                        :value="module" /></el-select
            ></el-form-item>
            <el-form-item label="结果"
                ><el-select v-model="filters.result" clearable style="width: 120px"
                    ><el-option label="成功" value="SUCCESS" /><el-option
                        label="失败"
                        value="FAILED" /></el-select
            ></el-form-item>
            <el-form-item label="操作类型"
                ><el-select v-model="filters.type" clearable style="width: 130px"
                    ><el-option label="登录" value="LOGIN" /><el-option
                        label="变更"
                        value="CHANGE" /><el-option label="版本维护" value="UPDATE" /></el-select
            ></el-form-item>
            <el-form-item label="起止日期"
                ><el-date-picker v-model="dates" type="daterange" value-format="YYYY-MM-DD"
            /></el-form-item>
            <el-button type="primary" native-type="submit">查询</el-button>
        </el-form>
        <el-alert v-if="error" :title="error" type="error" :closable="false" />
        <el-table :data="records" v-loading="loading" empty-text="暂无符合条件的平台日志">
            <el-table-column prop="userName" label="账号" />
            <el-table-column prop="actorType" label="身份" />
            <el-table-column prop="scopeOrgNo" label="工作室" />
            <el-table-column prop="module" label="模块" />
            <el-table-column prop="operation" label="操作" min-width="160" />
            <el-table-column prop="targetId" label="目标" show-overflow-tooltip />
            <el-table-column label="结果"
                ><template #default="{row}">{{
                    row.result === 'SUCCESS'
                        ? '成功'
                        : row.result === 'FAILED'
                          ? '失败'
                          : '历史未分类'
                }}</template></el-table-column
            >
            <el-table-column
                prop="summary"
                label="安全摘要"
                min-width="170"
                show-overflow-tooltip
            />
            <el-table-column prop="ip" label="来源" />
            <el-table-column label="时间" min-width="175"
                ><template #default="{row}">{{ time(row.createTime) }}</template></el-table-column
            >
        </el-table>
        <el-pagination
            v-model:current-page="page"
            :page-size="20"
            :total="total"
            @current-change="load"
        />
    </main>
</template>
<script setup>
import {ref, reactive, onMounted} from 'vue';
import {platformRequest, time} from '@/utils/platform-console';
const filters = reactive({account: '', module: '', result: '', type: ''});
const dates = ref([]);
const page = ref(1);
const total = ref(0);
const records = ref([]);
const error = ref('');
const loading = ref(false);
/** 按筛选项读取后端已经脱敏且只读的平台审计. */
async function load() {
    loading.value = true;
    try {
        const result = await platformRequest('logs', undefined, {
            params: {...filters, page: page.value, start: dates.value?.[0], end: dates.value?.[1]},
        });
        records.value = result.records;
        total.value = result.total;
        error.value = '';
    } catch (failure) {
        error.value = failure.response?.data?.msg || failure.message;
    } finally {
        loading.value = false;
    }
}
/** 查询条件变化后从第一页读取. */
function search() {
    page.value = 1;
    load();
}
onMounted(load);
</script>
