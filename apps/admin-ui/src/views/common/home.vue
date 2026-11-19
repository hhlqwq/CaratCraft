<template>
    <main class="merchant-home">
        <header class="workbench-heading">
            <div>
                <h1>商家工作台</h1>
            </div>
            <div class="heading-actions">
                <el-button
                    v-if="isAuth('sys:monitor:server')"
                    @click="monitorVisible = !monitorVisible"
                    >{{ monitorVisible ? '返回业务待办' : '系统监控' }}</el-button
                >
                <el-button v-if="!monitorVisible" :loading="loading" @click="load"
                    >刷新待办</el-button
                >
            </div>
        </header>
        <server-monitor v-if="monitorVisible" />
        <section v-else>
            <el-alert v-if="error" :title="error" type="error" :closable="false" />
            <div class="task-grid">
                <button
                    v-for="card in visibleCards"
                    :key="card.key"
                    class="task-card"
                    @click="go(card)"
                >
                    <span>{{ card.label }}</span>
                    <strong v-if="typeof summary[card.key] === 'number'">{{
                        summary[card.key]
                    }}</strong>
                </button>
            </div>
            <p v-if="!loading && !error && !visibleCards.length" class="empty-note">
                当前账号暂无业务查看权限, 请联系管理员配置角色。
            </p>
        </section>
    </main>
</template>

<script>
import ServerMonitor from '../modules/sys/server-monitor';

export default {
    components: {ServerMonitor},
    /** 初始化只读业务统计, 不填充虚构经营数据. */
    data() {
        return {
            summary: {},
            loading: false,
            error: '',
            monitorVisible: false,
            cards: [
                {key: 'goods', label: '商品管理', route: 'mall-goods'},
                {
                    key: 'stockOut',
                    label: '库存异常',
                    route: 'custom-component-stock',
                    todo: 'stockOut',
                },
                {key: 'diy', label: 'DIY 材料维护', route: 'custom-configuration'},
                {
                    key: 'workReview',
                    label: '待处理工单',
                    route: 'custom-work-order',
                    todo: 'PENDING_REVIEW',
                },
                {key: 'workMaking', label: '待制作', route: 'custom-work-order', todo: 'MAKING'},
                {
                    key: 'workQuality',
                    label: '待质检',
                    route: 'custom-work-order',
                    todo: 'QUALITY_CHECK',
                },
                {key: 'toShip', label: '待发货', route: 'mall-order', todo: 'toShip'},
                {
                    key: 'supportPending',
                    label: '待处理售后',
                    route: 'mall-aftersale',
                    todo: 'supportPending',
                },
                {key: 'feedback', label: '客户反馈', route: 'mall-feedback'},
                {
                    key: 'feedbackPending',
                    label: '待回复事项',
                    route: 'mall-feedback',
                    todo: 'feedbackPending',
                },
                {
                    key: 'refundReview',
                    label: '待审核退款',
                    route: 'mall-refund',
                    todo: 'refundReview',
                },
                {
                    key: 'refundExecute',
                    label: '待执行退款',
                    route: 'mall-refund',
                    todo: 'refundExecute',
                },
                {
                    key: 'refundRetry',
                    label: '待重试退款',
                    route: 'mall-refund',
                    todo: 'refundRetry',
                },
                {
                    key: 'refundAttention',
                    label: '待对账退款',
                    route: 'mall-refund',
                    todo: 'refundAttention',
                },
                {
                    key: 'communityReview',
                    label: '待审核社区内容',
                    route: 'community-posts',
                    todo: 'communityReview',
                },
                {key: 'employees', label: '员工账号管理', route: 'sys-user'},
                {key: 'roles', label: '角色管理', route: 'sys-role'},
                {key: 'permissions', label: '权限配置', route: 'sys-menu'},
                {key: 'audit', label: '系统审计', route: 'sys-log'},
            ],
        };
    },
    computed: {
        /** 只呈现服务端按当前权限返回的指标. */
        visibleCards() {
            return this.cards.filter((card) =>
                Object.prototype.hasOwnProperty.call(this.summary, card.key)
            );
        },
    },
    /** 每次进入读取最新数据, 不创建后台定时器. */
    activated() {
        this.load();
    },
    methods: {
        /** 加载业务数据并清除过期显示. */
        async load() {
            if (this.loading) {
                return;
            }
            this.loading = true;
            this.error = '';
            try {
                const {data} = await this.$http({url: '/mall/dashboard/summary', method: 'get'});
                if (data.code !== 0) {
                    throw new Error(data.msg);
                }
                this.summary = data.data || {};
            } catch (error) {
                this.summary = {};
                this.error = error.message || '业务待办读取失败';
            } finally {
                this.loading = false;
            }
        },
        /** 使用已有菜单路由进入实际业务页面. */
        go(card) {
            this.$router
                .push({name: card.route, query: card.todo ? {todo: card.todo} : {}})
                .catch(() => {});
        },
    },
};
</script>

<style scoped>
.merchant-home {
    max-width: 1440px;
    margin: 0 auto;
    padding: 12px;
    color: #283e32;
}
.workbench-heading {
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    gap: 20px;
    padding: 12px 0 24px;
}
h1 {
    margin: 10px 0;
    font-size: clamp(25px, 3vw, 36px);
}
p {
    color: #67766c;
    line-height: 1.8;
}
.heading-actions {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
}
.task-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(min(100%, 230px), 1fr));
    gap: 20px;
}
.task-card {
    min-width: 0;
    min-height: 168px;
    padding: 24px;
    text-align: left;
    border: 1px solid #e0e6de;
    border-radius: 16px;
    background: #fff;
    color: #344d3e;
    cursor: pointer;
    transition:
        box-shadow 0.2s,
        transform 0.2s;
}
.task-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px #25443012;
}
.task-card:focus-visible {
    outline: 3px solid #9fb6a2;
    outline-offset: 3px;
}
.task-card span,
.task-card small {
    display: block;
    overflow-wrap: anywhere;
}
.task-card strong {
    display: block;
    font-size: 38px;
    margin: 18px 0;
    font-weight: 600;
}
.task-card small {
    color: #738174;
}
.workbench-guide {
    margin-top: 28px;
    padding: 8px 0;
}
h2 {
    font-size: 18px;
}
@media (max-width: 600px) {
    .merchant-home {
        padding: 0;
    }
    .task-grid {
        gap: 12px;
    }
    .task-card {
        padding: 20px;
    }
}
</style>
