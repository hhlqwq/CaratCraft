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
      <el-table-column prop="craftJson" label="生产参数" min-width="280" show-overflow-tooltip></el-table-column>
      <el-table-column prop="createTime" label="创建时间" min-width="160"></el-table-column>
      <el-table-column fixed="right" label="操作" width="150">
        <template slot-scope="scope">
          <el-dropdown v-if="isAuth('custom:work-order:update')" @command="updateStatus(scope.row.id, $event)">
            <el-button type="text">流转状态<i class="el-icon-arrow-down el-icon--right"></i></el-button>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item v-for="item in statusOptions" :key="item.value"
                                :command="item.value">{{ item.label }}</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
export default {
  data () {
    return {
      status: '',
      workOrders: [],
      statusOptions: [
        {value: 'PENDING', label: '待审核'},
        {value: 'REVIEWED', label: '已审核'},
        {value: 'PRODUCING', label: '制作中'},
        {value: 'QUALITY_CHECK', label: '质检中'},
        {value: 'COMPLETED', label: '已完成'},
        {value: 'CANCELLED', label: '已取消'}
      ]
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
    // 更新工单状态 / Updates a work order status.
    updateStatus (id, status) {
      this.$http({
        url: '/custom/work-order/status',
        method: 'post',
        params: {id: id, status: status}
      }).then(({data}) => {
        if (data && data.code === 0) {
          this.$message.success('工单状态已更新')
          this.loadWorkOrders()
        } else {
          this.$message.error(data.msg || '更新失败')
        }
      })
    },
    // 获取状态中文名称 / Gets a localized status label.
    statusText (status) {
      const option = this.statusOptions.find(item => item.value === status)
      return option ? option.label : status
    }
  }
}
</script>
