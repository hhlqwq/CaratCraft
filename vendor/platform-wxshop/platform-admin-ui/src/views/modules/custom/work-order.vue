<template>
  <div class="mod-custom-work-order">
    <el-form :inline="true" @keyup.enter.native="loadWorkOrders">
      <el-form-item>
        <el-select v-model="status" placeholder="工单状态" clearable>
          <el-option v-for="item in statusOptions" :key="item.value"
                     :label="item.label" :value="item.value"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadWorkOrders">查询</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="workOrders" border style="width: 100%;">
      <el-table-column prop="workOrderNo" label="工单号" min-width="150"></el-table-column>
      <el-table-column prop="orderId" label="订单 ID" width="100"></el-table-column>
      <el-table-column prop="snapshotId" label="设计快照 ID" width="120"></el-table-column>
      <el-table-column label="状态" width="120">
        <template slot-scope="scope">
          <el-tag>{{ statusText(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="制作内容" min-width="280">
        <template slot-scope="scope">
          <div v-for="(line, index) in craftSummary(scope.row)" :key="index" class="craft-line">{{ line }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" min-width="160"></el-table-column>
      <el-table-column fixed="right" label="操作" width="200">
        <template slot-scope="scope">
          <el-dropdown v-if="isAuth('custom:work-order:update') && allowedActions(scope.row).length" @command="updateStatus(scope.row.id, $event)">
            <el-button type="text">流转状态<i class="el-icon-arrow-down el-icon--right"></i></el-button>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item v-for="item in allowedActions(scope.row)" :key="item.value"
                                :command="item.value">{{ item.label }}</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
          <el-button v-if="isAuth('custom:work-order:list')" type="text" size="small"
                     @click="showLogs(scope.row)">日志</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog title="工单操作日志" :visible.sync="logVisible" width="min(94vw, 900px)">
      <el-table :data="logs" border size="small">
        <el-table-column prop="createTime" label="时间" min-width="150"></el-table-column>
        <el-table-column prop="operatorName" label="操作人" width="110"></el-table-column>
        <el-table-column label="状态流转" min-width="180">
          <template slot-scope="scope">
            {{ statusText(scope.row.fromStatus) || '—' }} → {{ statusText(scope.row.toStatus) }}
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip></el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script>
export default {
  data () {
    return {
      status: '',
      workOrders: [],
      statusOptions: [
        {value: 'PENDING_REVIEW', label: '待审核'},
        {value: 'REVIEWED', label: '已审核'},
        {value: 'PRODUCING', label: '制作中'},
        {value: 'QUALITY_CHECK', label: '质检中'},
        {value: 'COMPLETED', label: '已完成'},
        {value: 'CANCELLED', label: '已取消'}
      ],
      logVisible: false,
      logs: []
    }
  },
  activated () {
    this.loadWorkOrders()
  },
  methods: {
    // 加载生产工单 / Loads production work orders.
    loadWorkOrders () {
      this.$http({
        url: '/custom/work-order/list',
        method: 'get',
        params: {status: this.status}
      }).then(({data}) => {
        this.workOrders = data && data.code === 0 ? data.data : []
      })
    },
    // 只展示下一步合法动作, 取消必须由订单售后流程处理.
    allowedActions (row) {
      const transitions = {PENDING_REVIEW: ['REVIEWED'], REVIEWED: ['PRODUCING'], PRODUCING: ['QUALITY_CHECK'], QUALITY_CHECK: ['COMPLETED']}
      return this.statusOptions.filter(item => (transitions[row.status] || []).includes(item.value))
    },
    // 以可读制作参数展示快照, 历史快照缺字段时明确提示人工核验.
    craftSummary (row) {
      try {
        const craft = JSON.parse(row.craftJson || '{}')
        const lines = Array.isArray(craft.components)
          ? craft.components.map(item => `${item.name || item.optionCode} × ${item.quantity}`)
          : ['历史选项: ' + (craft.optionIds || []).join('、'), '历史参数需结合设计详情核验']
        if (craft.engraving) {
          lines.push('刻字: ' + craft.engraving)
        }
        return lines
      } catch (error) {
        return ['生产参数无法读取, 请核对设计快照后再操作']
      }
    },
    // 每次审核、开工和质检必须录入操作备注并确认.
    updateStatus (id, status) {
      this.$prompt(`确认执行“${this.statusText(status)}”, 请填写审核、制作或质检记录`, '工单流转', {
        inputType: 'textarea',
        inputValidator: value => value && value.trim().length > 0 && value.length <= 500
          ? true : '请填写1到500字的备注'
      }).then(({value}) => this.$http({
        url: '/custom/work-order/status',
        method: 'post',
        params: {id: id, status: status, remark: value}
      })).then(({data}) => {
        if (data && data.code === 0) {
          this.$message.success('工单状态已更新')
          this.loadWorkOrders()
        }
      }).catch(() => {})
    },
    // 获取状态中文名称 / Gets a localized status label.
    statusText (status) {
      const option = this.statusOptions.find(item => item.value === status)
      return option ? option.label : status
    },
    // 查看工单操作日志 / Shows the work order operation logs.
    showLogs (row) {
      this.$http({
        url: `/custom/work-order/log/${row.id}`,
        method: 'get'
      }).then(({data}) => {
        if (data && data.code === 0) {
          this.logs = data.data || []
          this.logVisible = true
        } else {
          this.$message.error(data.msg || '日志查询失败')
        }
      })
    }
  }
}
</script>

<style scoped>
.craft-line { line-height: 1.8; overflow-wrap: anywhere; }
</style>
