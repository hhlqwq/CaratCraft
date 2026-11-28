<template>
    <main>
        <el-alert
            v-if="error || message"
            :title="error || message"
            :type="error ? 'warning' : 'info'"
            :closable="false"
        />
        <el-descriptions v-if="release" title="当前安装" :column="2" border>
            <el-descriptions-item label="软件版本">{{
                release.version || '待登记'
            }}</el-descriptions-item>
            <el-descriptions-item label="构建时间">{{
                time(release.builtAt)
            }}</el-descriptions-item>
            <el-descriptions-item label="首次安装">{{
                time(release.installedAt)
            }}</el-descriptions-item>
            <el-descriptions-item label="最近更新">{{
                time(release.updatedAt)
            }}</el-descriptions-item>
        </el-descriptions>
        <h2>离线升级</h2>
        <p>
            仅接受与本机架构匹配、由预置信任公钥签名的发行包。升级会短暂停止应用，任务在独立维护服务中持续执行。
        </p>
        <p>微信小程序发布独立进行，本入口只更新服务器应用。</p>
        <input
            type="file"
            accept=".zip"
            :disabled="busy || active"
            @change="choose"
            aria-label="选择签名离线发行包"
        />
        <el-button type="primary" :disabled="!file || active" :loading="busy" @click="upload"
            >上传并校验</el-button
        >
        <el-progress v-if="busy" :percentage="progress" />
        <el-button
            v-if="validated"
            type="primary"
            :disabled="busy || active"
            @click="confirm(validated.id)"
            >确认升级至 {{ validated.version }}</el-button
        >
        <h2>持久任务记录</h2>
        <el-table :data="jobs || []" empty-text="尚无升级记录">
            <el-table-column prop="version" label="目标版本" />
            <el-table-column prop="status" label="状态" min-width="155" />
            <el-table-column prop="stage" label="阶段" min-width="170" />
            <el-table-column label="最近变化" min-width="175"
                ><template #default="{row}">{{ time(row.updatedAt) }}</template></el-table-column
            >
            <el-table-column label="操作"
                ><template #default="{row}"
                    ><el-button link @click="selected = row">查看进度</el-button
                    ><el-button
                        v-if="row.status === 'VALIDATED'"
                        link
                        :disabled="active"
                        @click="confirm(row.id)"
                        >确认升级</el-button
                    ><el-button v-if="row.recoverable" link type="danger" @click="recover(row.id)"
                        >恢复备份</el-button
                    ></template
                ></el-table-column
            >
        </el-table>
        <el-dialog :model-value="!!selected" title="实际执行进度" @close="selected = null">
            <el-timeline
                ><el-timeline-item
                    v-for="(event, index) in selected?.events || []"
                    :key="index"
                    :timestamp="time(event.time)"
                    >{{ event.stage }} · {{ event.message }}</el-timeline-item
                ></el-timeline
            >
        </el-dialog>
    </main>
</template>
<script setup>
import {ref, computed} from 'vue';
import {ElMessageBox} from 'element-plus';
import {platformRequest, usePlatformSnapshot, time} from '@/utils/platform-console';
const {data: jobs, error, load} = usePlatformSnapshot('jobs', 5000);
const {data: release, load: loadRelease} = usePlatformSnapshot('release', 15000);
const file = ref(null);
const validated = ref(null);
const busy = ref(false);
const progress = ref(0);
const message = ref('');
const selectedId = ref(null);
const selected = computed({
    /** 当前任务从轮询的持久状态中读取. */
    get() {
        return jobs.value?.find((job) => job.id === selectedId.value);
    },
    /** 面板只保留任务标识, 不保存旧进度副本. */
    set(value) {
        selectedId.value = value?.id;
    },
});
const active = computed(() =>
    jobs.value?.some(
        (job) =>
            !['SUCCEEDED', 'FAILED_RECOVERED', 'FAILED', 'RESTORED', 'VALIDATED'].includes(
                job.status
            )
    )
);
/** 选择发行文件时清空上次尚未确认的展示状态. */
function choose(event) {
    file.value = event.target.files?.[0] || null;
    validated.value = null;
    message.value = '';
}
/** 真实上传字节进度与后续校验阶段分别展示. */
async function upload() {
    if (!file.value || busy.value) {
        return;
    }
    busy.value = true;
    error.value = '';
    progress.value = 0;
    try {
        validated.value = await platformRequest('packages', file.value, {
            headers: {'Content-Type': 'application/octet-stream'},
            timeout: 900000,
            onUploadProgress(event) {
                progress.value = Math.min(100, Math.floor((event.loaded * 100) / file.value.size));
            },
        });
        message.value = '签名、文件、架构与服务校验通过，请确认升级。';
        await load();
    } catch (failure) {
        error.value = failure.response?.data?.msg || failure.message;
    } finally {
        busy.value = false;
    }
}
/** 明确确认停机更新后提交任务, 页面关闭不会中断工作容器. */
async function confirm(id) {
    try {
        await ElMessageBox.confirm('将备份数据并短暂停止应用，确认开始升级？', '确认离线升级');
        await platformRequest('confirm', {id});
        validated.value = null;
        message.value = '任务已提交。连接中断时请等待维护服务恢复，勿重复启动部署。';
        selectedId.value = id;
        await load();
        await loadRelease();
    } catch (failure) {
        if (failure instanceof Error) {
            error.value = failure.response?.data?.msg || failure.message;
        }
    }
}
/** 只对维护中失败任务恢复数据库、上传文件及原镜像的一致备份. */
async function recover(id) {
    try {
        await ElMessageBox.confirm(
            '恢复停机前的数据库、上传文件和应用镜像，确认继续？',
            '恢复失败升级'
        );
        await platformRequest('restore', {id});
        await load();
    } catch (failure) {
        if (failure instanceof Error) {
            error.value = failure.response?.data?.msg || failure.message;
        }
    }
}
</script>
