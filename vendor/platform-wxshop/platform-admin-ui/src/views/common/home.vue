<template>
  <main class="merchant-home">
    <header class="workbench-heading">
      <div>
        <p class="eyebrow">CARAT CRAFT</p>
        <h1>商家工作台</h1>
        <p>从订单、制作和库存开始处理今天的工作。</p>
      </div>
      <div class="heading-actions">
        <el-button v-if="isAuth('sys:monitor:server')" @click="monitorVisible = !monitorVisible">{{
          monitorVisible ? '返回业务待办' : '系统监控'
        }}</el-button>
        <el-button v-if="!monitorVisible" :loading="loading" @click="load">刷新待办</el-button>
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
          @click="go(card.route)"
        >
          <span>{{ card.label }}</span
          ><strong>{{ summary[card.key] }}</strong
          ><small>{{ card.help }} →</small>
        </button>
      </div>
      <p v-if="!loading && !error && !visibleCards.length" class="empty-note">
        当前账号暂无业务查看权限, 请联系管理员配置角色。
      </p>
      <section class="workbench-guide">
        <h2>处理顺序</h2>
        <p>
          先审核售后和待制作工单, 再核对材料库存。成品通过质检后安排发货, 退款完成以支付侧查单为准。
        </p>
      </section>
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
        {key: 'unpaid', label: '待付款订单', help: '查看订单', route: 'mall-order'},
        {key: 'toShip', label: '待发货订单', help: '安排发货', route: 'mall-order'},
        {key: 'refundReview', label: '待审核售后', help: '处理售后', route: 'mall-order'},
        {key: 'refundAttention', label: '退款需关注', help: '核对退款记录', route: 'mall-order'},
        {key: 'workReview', label: '待审核工单', help: '核对制作要求', route: 'custom-work-order'},
        {key: 'workMaking', label: '制作与质检', help: '查看生产进度', route: 'custom-work-order'},
        {key: 'stockOut', label: '缺货材料', help: '检查可用库存', route: 'custom-component-stock'},
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
    go(route) {
      this.$router.push({name: route}).catch(() => {});
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
.eyebrow {
  color: #8a7953;
  letter-spacing: 3px;
  font-size: 12px;
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
