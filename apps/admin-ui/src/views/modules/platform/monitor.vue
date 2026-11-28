<template>
    <main>
        <el-alert v-if="error" :title="error" type="error" :closable="false" />
        <p>每15秒采集。仅保留实际样本，首次采样的CPU占用暂不显示。</p>
        <template v-if="data?.host">
            <el-alert
                :title="data.host.originLabel"
                :description="data.host.physicalHost.reason"
                type="info"
                :closable="false"
            />
            <el-descriptions :column="2" border>
                <el-descriptions-item label="处理器"
                    >{{
                        typeof data.host.cpuModel === 'string' ? data.host.cpuModel : '型号未公开'
                    }}
                    · {{ data.host.cores }} 核</el-descriptions-item
                >
                <el-descriptions-item label="系统及架构"
                    >{{ data.host.os }} · {{ data.host.architecture }}</el-descriptions-item
                >
                <el-descriptions-item label="CPU占用"
                    >{{ data.host.cpuPercent ?? '待采样' }}%</el-descriptions-item
                >
                <el-descriptions-item label="内存"
                    >{{ bytes(data.host.memoryUsed) }} /
                    {{ bytes(data.host.memoryTotal) }}</el-descriptions-item
                >
                <el-descriptions-item label="存储"
                    >{{ bytes(data.host.storage.available) }} 可用 /
                    {{ bytes(data.host.storage.total) }} ·
                    {{ data.host.storage.origin }}</el-descriptions-item
                >
                <el-descriptions-item label="磁盘I/O">{{
                    data.host.storage.io.reason
                }}</el-descriptions-item>
                <el-descriptions-item label="GPU">{{ data.host.gpu.reason }}</el-descriptions-item>
                <el-descriptions-item label="温度">{{
                    data.host.temperature.supported
                        ? data.host.temperature.readingsCelsius.join(' / ') + '℃'
                        : data.host.temperature.reason
                }}</el-descriptions-item>
                <el-descriptions-item label="内核运行时间"
                    >{{
                        Math.floor((data.host.uptimeSeconds || 0) / 3600)
                    }}
                    小时</el-descriptions-item
                >
                <el-descriptions-item label="采样时间">{{
                    time(data.sampledAt)
                }}</el-descriptions-item>
            </el-descriptions>
            <h2>核心容器</h2>
            <el-table :data="data.services">
                <el-table-column prop="name" label="服务" min-width="170" />
                <el-table-column prop="health" label="健康" />
                <el-table-column prop="status" label="运行状态" />
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
                <el-table-column label="实际运行镜像" min-width="230" show-overflow-tooltip
                    ><template #default="{row}"
                        >{{ row.image }} · {{ row.version || '镜像自身版本' }} ·
                        {{ row.imageId }}</template
                    ></el-table-column
                >
                <el-table-column prop="restartCount" label="重启次数" />
                <el-table-column label="启动时间" min-width="170"
                    ><template #default="{row}">{{
                        time(row.startedAt)
                    }}</template></el-table-column
                >
            </el-table>
            <h2 v-if="data.jvm">{{ data.jvm.origin }}</h2>
            <p v-if="data.jvm">
                JVM {{ data.jvm.metrics.jdkVersion }} · 已用 {{ data.jvm.metrics.usedMemory }} /
                上限 {{ data.jvm.metrics.maxMemory }} · 运行
                {{ Math.floor(data.jvm.metrics.uptime / 60000) }} 分钟
            </p>
            <h2>网络累计与速率</h2>
            <el-table :data="data.host.networks || []" empty-text="当前环境无法读取宿主网络">
                <el-table-column prop="name" label="接口" />
                <el-table-column prop="receivedBytes" label="接收字节" />
                <el-table-column prop="sentBytes" label="发送字节" />
                <el-table-column label="接收速率"
                    ><template #default="{row}">{{
                        row.receiveBytesPerSecond == null
                            ? '待采样'
                            : Math.round(row.receiveBytesPerSecond) + ' B/s'
                    }}</template></el-table-column
                >
            </el-table>
            <h2>最近实际采样</h2>
            <el-table :data="data.history.slice(-20).reverse()" max-height="340">
                <el-table-column label="时间"
                    ><template #default="{row}">{{ time(row.time) }}</template></el-table-column
                >
                <el-table-column prop="cpuPercent" label="宿主CPU占用(%)" />
                <el-table-column prop="memoryPercent" label="宿主内存占用(%)" />
            </el-table>
        </template>
        <el-empty v-else :description="data?.message || '等待首个真实样本'" />
        <el-button :loading="loading" @click="load">刷新状态</el-button>
    </main>
</template>
<script setup>
import {usePlatformSnapshot, bytes, time} from '@/utils/platform-console';
const {data, error, loading, load} = usePlatformSnapshot('monitor', 15000);
</script>
