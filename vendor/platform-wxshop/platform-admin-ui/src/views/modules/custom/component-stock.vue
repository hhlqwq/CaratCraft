<template>
  <div class="mod-custom-component-stock">
    <el-form :inline="true" @keyup.enter.native="loadStock()">
      <el-form-item>
        <el-input v-model="goodsId" placeholder="商品 ID" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadStock()">查询</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="stockList" border style="width: 100%;">
      <el-table-column prop="optionId" label="选项 ID" width="90"></el-table-column>
      <el-table-column prop="goodsId" label="商品 ID" width="100"></el-table-column>
      <el-table-column prop="componentType" label="组件类型" width="120"></el-table-column>
      <el-table-column prop="optionCode" label="选项代码" min-width="150"></el-table-column>
      <el-table-column prop="name" label="选项名称" min-width="140"></el-table-column>
      <el-table-column prop="priceDelta" label="加价(¥)" width="100"></el-table-column>
      <el-table-column prop="stock" label="库存" width="100">
        <template slot-scope="scope">
          <el-tag :type="scope.row.stock > 0 ? 'success' : 'danger'">{{ scope.row.stock }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column fixed="right" label="操作" width="120">
        <template slot-scope="scope">
          <el-button v-if="isAuth('custom:stock:update')" type="text" size="small"
                     @click="openEdit(scope.row)">修改库存</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog title="修改组件库存" :visible.sync="editVisible" width="360px">
      <el-form label-width="90px">
        <el-form-item label="选项">
          <span>{{ editRow && editRow.name }}（{{ editRow && editRow.componentType }}）</span>
        </el-form-item>
        <el-form-item label="库存">
          <el-input-number v-model="editStock" :min="0" :max="999999" :step="1"></el-input-number>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="saveStock()">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  data () {
    return {
      goodsId: '',
      stockList: [],
      editVisible: false,
      editRow: null,
      editStock: 0
    }
  },
  activated () {
    this.loadStock()
  },
  methods: {
    // 加载组件选项库存 / Loads component option stock.
    loadStock () {
      this.$http({
        url: '/custom/component-stock/list',
        method: 'get',
        params: {goodsId: this.goodsId || undefined}
      }).then(({data}) => {
        this.stockList = data && data.code === 0 ? data.data : []
      })
    },
    // 打开库存编辑弹窗 / Opens the stock edit dialog.
    openEdit (row) {
      this.editRow = row
      this.editStock = row.stock
      this.editVisible = true
    },
    // 保存库存修改 / Saves the stock change.
    saveStock () {
      this.$http({
        url: '/custom/component-stock/update',
        method: 'post',
        params: {optionId: this.editRow.optionId, stock: this.editStock}
      }).then(({data}) => {
        if (data && data.code === 0) {
          this.$message.success('库存已更新')
          this.editVisible = false
          this.loadStock()
        } else {
          this.$message.error(data.msg || '更新失败')
        }
      })
    }
  }
}
</script>
