<template>
    <main v-loading="loading && !data">
        <el-alert v-if="error" :title="error" type="error" :closable="false" />
        <template v-if="data">
            <el-descriptions title="平台运行状态" :column="3" border>
                <el-descriptions-item label="实际版本">{{
                    data.release.version || '待登记'
                }}</el-descriptions-item>
                <el-descriptions-item label="环境">{{ data.environment }}</el-descriptions-item>
                <el-descriptions-item label="后台运行"
                    >{{ Math.floor(data.uptimeMillis / 60000) }} 分钟</el-descriptions-item
                >
                <el-descriptions-item label="商家管理员"
                    >{{ data.merchants.total }} 个</el-descriptions-item
                >
                <el-descriptions-item label="已启用"
                    >{{ data.merchants.active }} 个</el-descriptions-item
                >
                <el-descriptions-item label="已停用"
                    >{{ data.merchants.disabled }} 个</el-descriptions-item
                >
            </el-descriptions>
            <h2>核心服务</h2>
            <el-table :data="data.monitor.services || []" empty-text="正在采集服务状态">
                <el-table-column prop="name" label="服务" />
                <el-table-column prop="status" label="运行状态" />
                <el-table-column prop="health" label="健康状态" />
                <el-table-column label="CPU"
                    ><template #default="{row}"
                        >{{ row.cpuPercent ?? '待采样' }}%</template
                    ></el-table-column
                >
                <el-table-column label="内存"
                    ><template #default="{row}">{{
                        bytes(row.memoryUsed)
                    }}</template></el-table-column
                >
            </el-table>
            <p v-if="data.monitor.host">
                {{ data.monitor.host.originLabel }} · CPU
                {{ data.monitor.host.cpuPercent ?? '待采样' }}% · 内存
                {{ bytes(data.monitor.host.memoryUsed) }} · 存储
                {{ data.monitor.host.storage?.percent ?? '未获取' }}%
            </p>
            <h2>最近维护任务</h2>
            <el-table :data="data.jobs.slice(0, 3)" empty-text="尚无升级任务">
                <el-table-column prop="version" label="目标版本" />
                <el-table-column prop="stage" label="当前阶段" />
                <el-table-column prop="status" label="结果" />
            </el-table>
            <h2>重要事件</h2>
            <el-table :data="data.events" empty-text="暂无平台重要事件">
                <el-table-column prop="actor" label="操作账号" />
                <el-table-column prop="operation" label="操作" />
                <el-table-column prop="result" label="结果" />
                <el-table-column label="时间"
                    ><template #default="{row}">{{ time(row.time) }}</template></el-table-column
                >
            </el-table>
        </template>
        <el-button @click="load" :loading="loading">刷新状态</el-button>
    </main>
</template>
<script setup>
import {usePlatformSnapshot, bytes, time} from '@/utils/platform-console';
const {data, error, loading, load} = usePlatformSnapshot('overview', 15000);
</script>
