<template>
  <el-dialog title="退款与售后审核" v-model="visible" width="94%" :close-on-click-modal="false">
    <p>审核通过后由支付查单确认结果。已发货订单须先核实退货或免退货安排, 不自动恢复库存。</p>
    <el-button v-if="isAuth('mall:order:update')" :loading="busy" @click="reconcile">执行对账</el-button>
    <el-table v-loading="busy" :data="rows" border style="width: 100%; margin-top: 16px">
      <el-table-column prop="order_sn" label="订单号" min-width="180" />
      <el-table-column label="申请金额" min-width="110"><template #default="scope">¥{{ (scope.row.amount_fen / 100).toFixed(2) }}</template></el-table-column>
      <el-table-column prop="reason" label="申请理由" min-width="180" />
      <el-table-column label="处理状态" min-width="130"><template #default="scope">{{ statusName(scope.row.status) }}</template></el-table-column>
      <el-table-column prop="decision_note" label="审核说明" min-width="180" />
      <el-table-column label="操作" min-width="160"><template #default="scope">
        <template v-if="scope.row.status === 'PENDING_REVIEW' && isAuth('mall:order:update')">
          <el-button type="text" @click="decide(scope.row, true)">通过</el-button>
          <el-button type="text" @click="decide(scope.row, false)">拒绝</el-button>
        </template>
        <span v-else>{{ scope.row.status === 'MANUAL_REVIEW' ? '请在微信商户平台处理异常' : '等待对账或已完成' }}</span>
      </template></el-table-column>
    </el-table>
    <el-pagination :current-page="page" :page-size="20" :total="total" layout="total, prev, pager, next" @current-change="load" />
  </el-dialog>
</template>

<script>
export default {
  compatConfig: {MODE: 2},
  /** 初始化分页及请求状态. */
  data () {
    return {visible: false, rows: [], page: 1, total: 0, busy: false}
  },
  methods: {
    /** 打开审核列表. */
    open () {
      this.visible = true
      this.load(1)
    },
    /** 读取有限页数据并处理失败. */
    async load (page) {
      this.busy = true
      try {
        const {data} = await this.$http({url: '/mall/refund/list', method: 'get', params: {page, limit: 20}})
        if (data.code !== 0) {
          throw new Error(data.msg)
        }
        this.rows = data.data.records
        this.total = data.data.total
        this.page = page
      } catch (error) {
        this.$message.error(error.message || '售后列表读取失败')
      } finally {
        this.busy = false
      }
    },
    /** 审核必须填写理由, 不将审核通过显示成退款成功. */
    async decide (row, approved) {
      try {
        const {value} = await this.$prompt(approved ? '请确认退货/免退货安排并填写审核说明' : '请填写拒绝理由', '售后审核', {
          inputValidator: value => Boolean(value && value.trim() && value.length <= 200) || '请填写1到200字说明'
        })
        const {data} = await this.$http({url: '/mall/refund/decide', method: 'post', data: {orderId: row.order_id, approved, note: value}})
        if (data.code !== 0) {
          throw new Error(data.msg)
        }
        this.$message.success('审核已保存')
        this.load(this.page)
      } catch (error) {
        if (error !== 'cancel' && error !== 'close') {
          this.$message.error(error.message || '审核失败')
        }
      }
    },
    /** 手动处理到期退款租约, 显示实际处理数量. */
    async reconcile () {
      this.busy = true
      try {
        const {data} = await this.$http({url: '/mall/refund/reconcile', method: 'post'})
        if (data.code !== 0) {
          throw new Error(data.msg)
        }
        this.$message.success('本次处理 ' + data.data.processed + ' 项')
        await this.load(this.page)
      } catch (error) {
        this.$message.error(error.message || '对账失败')
      } finally {
        this.busy = false
      }
    },
    /** 使用商家可读状态名称. */
    statusName (status) {
      return {PENDING_REVIEW: '待审核', APPROVED: '待提交退款', PROCESSING: '退款处理中', RETRY: '等待补偿重试', SUCCEEDED: '退款已完成', REJECTED: '审核未通过', MANUAL_REVIEW: '需人工处理'}[status] || status
    }
  }
}
</script>
