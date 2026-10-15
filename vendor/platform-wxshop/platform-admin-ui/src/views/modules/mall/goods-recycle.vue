<template>
  <div class="goods-recycle-page">
    <section class="recycle-banner">
      <div>
        <p class="eyebrow">CATALOG SAFETY</p>
        <h2>商品回收站</h2>
        <p>删除后的商品会立即从管理列表和小程序中隐藏，并在这里保留三十天。</p>
      </div>
      <div class="retention-badge">30 天可恢复</div>
    </section>

    <el-form :inline="true" :model="searchForm" class="filter-bar" @keyup.enter.native="getDataList(1)">
      <el-form-item>
        <el-input v-model="searchForm.name" placeholder="搜索已删除商品" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-button @click="getDataList(1)">查询</el-button>
        <el-button v-if="isAuth('mall:goods:recycle:restore')" type="primary"
                   :disabled="dataListSelections.length === 0" @click="restoreHandle()">批量恢复</el-button>
        <el-button v-if="isAuth('mall:goods:recycle:delete')" type="danger" plain
                   :disabled="dataListSelections.length === 0" @click="deleteHandle()">批量永久删除</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="dataList" border @selection-change="selectionChangeHandle">
      <el-table-column type="selection" width="50" align="center"></el-table-column>
      <el-table-column prop="id" label="ID" width="90" align="center"></el-table-column>
      <el-table-column prop="name" label="商品名称" min-width="220"></el-table-column>
      <el-table-column prop="goodsSn" label="商品编码" min-width="150" align="center"></el-table-column>
      <el-table-column prop="retailPrice" label="零售价" width="110" align="center"></el-table-column>
      <el-table-column label="删除时间" min-width="170" align="center">
        <template slot-scope="scope">{{ formatDate(scope.row.deleteTime) }}</template>
      </el-table-column>
      <el-table-column label="剩余时间" width="120" align="center">
        <template slot-scope="scope">
          <el-tag :type="remainingDays(scope.row.deleteTime) <= 3 ? 'danger' : 'warning'" size="small">
            {{ remainingDays(scope.row.deleteTime) }} 天
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column fixed="right" label="操作" width="180" align="center">
        <template slot-scope="scope">
          <el-button v-if="isAuth('mall:goods:recycle:restore')" type="text" size="small"
                     @click="restoreHandle(scope.row.id)">恢复</el-button>
          <el-button v-if="isAuth('mall:goods:recycle:delete')" type="text" size="small"
                     class="danger-action" @click="deleteHandle(scope.row.id)">永久删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination :current-page="pageIndex" :page-sizes="[10, 20, 50, 100]" :page-size="pageSize"
                   :total="totalPage" layout="total, sizes, prev, pager, next, jumper"
                   @size-change="sizeChangeHandle" @current-change="currentChangeHandle"></el-pagination>
  </div>
</template>

<script>
export default {
  data () {
    return {
      searchForm: {name: ''},
      dataList: [],
      dataListSelections: [],
      pageIndex: 1,
      pageSize: 10,
      totalPage: 0
    }
  },
  activated () {
    this.getDataList()
  },
  methods: {
    // 查询回收站分页数据。
    getDataList (page) {
      if (page) {
        this.pageIndex = page
      }
      this.$http({
        url: '/mall/goods/recycle/list',
        method: 'get',
        params: {page: this.pageIndex, limit: this.pageSize, name: this.searchForm.name}
      }).then(({data}) => {
        if (data && data.code === 0) {
          this.dataList = data.data.records
          this.totalPage = data.data.total
          return
        }
        this.dataList = []
        this.totalPage = 0
        this.$message.error((data && data.msg) || '回收站加载失败')
      })
    },
    // 恢复所选商品，恢复后保持下架。
    restoreHandle (id) {
      const ids = id ? [id] : this.dataListSelections.map(item => item.id)
      this.$confirm('恢复后商品仍保持下架，需要检查内容后手动上架。是否继续？', '恢复商品', {
        type: 'warning'
      }).then(() => this.submit('/mall/goods/recycle/restore', ids, '商品已恢复'))
        .catch(() => {})
    },
    // 永久删除所选商品。
    deleteHandle (id) {
      const ids = id ? [id] : this.dataListSelections.map(item => item.id)
      this.$confirm('永久删除后无法恢复，但历史订单和定制快照仍会保留。是否继续？', '高风险操作', {
        confirmButtonText: '永久删除',
        cancelButtonText: '取消',
        type: 'error'
      }).then(() => this.submit('/mall/goods/recycle/delete', ids, '商品已永久删除'))
        .catch(() => {})
    },
    // 提交回收站批量操作。
    submit (url, ids, fallbackMessage) {
      return this.$http({url, method: 'post', data: ids}).then(({data}) => {
        if (!data || data.code !== 0) {
          this.$message.error((data && data.msg) || '操作失败，请稍后重试')
          return
        }
        this.$message.success(data.msg || fallbackMessage)
        this.getDataList()
      })
    },
    // 格式化删除时间。
    formatDate (value) {
      if (!value) {
        return '迁移前删除'
      }
      return new Date(value).toLocaleString('zh-CN', {hour12: false})
    },
    // 计算自动清理前剩余天数。
    remainingDays (value) {
      if (!value) {
        return 30
      }
      const elapsed = Date.now() - new Date(value).getTime()
      return Math.max(0, 30 - Math.floor(elapsed / 86400000))
    },
    selectionChangeHandle (val) {
      this.dataListSelections = val
    },
    sizeChangeHandle (val) {
      this.pageSize = val
      this.getDataList(1)
    },
    currentChangeHandle (val) {
      this.pageIndex = val
      this.getDataList()
    }
  }
}
</script>

<style scoped>
.goods-recycle-page {
  padding: 4px;
}
.recycle-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 22px;
  padding: 26px 32px;
  color: #f5efe4;
  background: linear-gradient(120deg, #10241e, #27483c);
  border-left: 4px solid #c49a59;
}
.recycle-banner h2 {
  margin: 5px 0 8px;
  font-size: 24px;
  font-weight: 500;
}
.recycle-banner p {
  margin: 0;
}
.eyebrow {
  color: #d8b578;
  font-size: 11px;
  letter-spacing: 4px;
}
.retention-badge {
  padding: 12px 18px;
  color: #253a31;
  background: #f1e3c8;
  border-radius: 2px;
}
.filter-bar {
  padding: 18px 20px 0;
  background: #fff;
  border: 1px solid #e5ded2;
}
.danger-action {
  color: #bd4d4d;
}
</style>
