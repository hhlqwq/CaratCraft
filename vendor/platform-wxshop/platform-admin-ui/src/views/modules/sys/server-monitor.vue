<template>
    <section class="server-monitor">
        <header>
            <h2>系统监控</h2>
            <el-button :loading="loading" @click="refresh">刷新</el-button>
        </header>
        <el-alert v-if="error" :title="error" type="error" :closable="false" />
        <div v-if="data" class="monitor-grid">
            <article>
                <h3>CPU 使用率</h3>
                <el-progress :percentage="percent(data.cupInfo.usePercent)" />
                <p>
                    物理核心 {{ data.cupInfo.physicalProcessorCount }} · 逻辑处理器
                    {{ data.cupInfo.logicalProcessorCount }}
                </p>
            </article>
            <article>
                <h3>内存使用率</h3>
                <el-progress :percentage="percent(data.memoryInfo.usePercent)" />
                <p>
                    已用 {{ data.memoryInfo.used }} · 可用 {{ data.memoryInfo.free }} · 总计
                    {{ data.memoryInfo.total }}
                </p>
            </article>
            <article>
                <h3>JVM 内存使用率</h3>
                <el-progress :percentage="percent(data.jvmInfo.usePercent)" />
                <p>已用 {{ data.jvmInfo.usedMemory }} · 可用 {{ data.jvmInfo.freeMemory }}</p>
            </article>
        </div>
        <el-table v-if="data" :data="data.diskInfos" border>
            <el-table-column prop="mount" label="挂载位置" min-width="130" />
            <el-table-column prop="size" label="总空间" min-width="110" />
            <el-table-column prop="used" label="已用空间" min-width="110" />
            <el-table-column prop="avail" label="可用空间" min-width="110" />
            <el-table-column label="使用率" min-width="160"
                ><template #default="scope"
                    ><el-progress :percentage="percent(scope.row.usePercent)" /></template
            ></el-table-column>
        </el-table>
        <p class="monitor-note">
            监控范围为应用当前运行环境,
            容器中不代表整台宿主机。同一底层卷的重复挂载只展示一项。业务待办请返回商家工作台。
        </p>
    </section>
</template>

<script>
export default {
    /** 初始化监控状态. */
    data() {
        return {data: null, loading: false, error: ''};
    },
    /** 打开时读取, 不在后台无限轮询. */
    mounted() {
        this.refresh();
    },
    methods: {
        /** 将后端0到1比例转换为百分数, 防止异常值破坏布局. */
        percent(ratio) {
            const value = Number(ratio);
            return Number.isFinite(value) ? Math.max(0, Math.min(100, Math.round(value * 100))) : 0;
        },
        /** 拉取受权限保护的监控数据并释放加载状态. */
        async refresh() {
            if (this.loading) {
                return;
            }
            this.loading = true;
            try {
                const {data} = await this.$http({url: '/sys/monitor/server', method: 'post'});
                if (data.code !== 0) {
                    throw new Error(data.msg);
                }
                this.data = data.data;
                this.error = '';
            } catch (error) {
                this.error = error.message || '监控读取失败';
            } finally {
                this.loading = false;
            }
        },
    },
};
</script>

<style scoped>
header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 16px;
}
.monitor-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(min(100%, 260px), 1fr));
    gap: 18px;
    margin: 20px 0;
}
article {
    min-width: 0;
    padding: 20px;
    border: 1px solid #e2e8e2;
    border-radius: 12px;
    background: #fff;
}
p {
    color: #66756c;
    line-height: 1.8;
    overflow-wrap: anywhere;
}
</style>
