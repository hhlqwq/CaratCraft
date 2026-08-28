<template>
  <div class="mod-custom-configuration">
    <el-alert title="先配置定制商品，再维护该商品的组件选项；新增选项后请到“组件库存”设置可售库存。" type="info" :closable="false" show-icon></el-alert>
    <el-card class="card" shadow="never">
      <div slot="header">定制商品</div>
      <el-form :inline="true" label-width="90px">
        <el-form-item label="商品 ID"><el-input v-model.number="product.goodsId" type="number"></el-input></el-form-item>
        <el-form-item label="模板代码"><el-input v-model="product.templateCode" placeholder="necklace-v1"></el-input></el-form-item>
        <el-form-item label="必选组件"><el-input v-model="product.requiredComponentTypes" placeholder="chain,pendant"></el-input></el-form-item>
        <el-form-item label="启用"><el-switch v-model="product.enabled" :active-value="1" :inactive-value="0"></el-switch></el-form-item>
        <el-button v-if="isAuth('custom:config:update')" type="primary" @click="saveProduct">保存商品配置</el-button>
      </el-form>
      <el-table :data="products" border><el-table-column prop="goodsId" label="商品 ID" width="100"></el-table-column><el-table-column prop="templateCode" label="模板"></el-table-column><el-table-column prop="requiredComponentTypes" label="必选组件"></el-table-column><el-table-column prop="enabled" label="状态" width="90"><template slot-scope="scope"><el-tag :type="scope.row.enabled ? 'success' : 'info'">{{ scope.row.enabled ? '启用' : '停用' }}</el-tag></template></el-table-column><el-table-column label="操作" width="130"><template slot-scope="scope"><el-button type="text" @click="selectProduct(scope.row)">编辑选项</el-button></template></el-table-column></el-table>
    </el-card>
    <el-card class="card" shadow="never">
      <div slot="header">组件选项（当前商品：{{ selectedGoodsId || '未选择' }}）</div>
      <el-form :inline="true" label-width="80px">
        <el-form-item label="类型"><el-input v-model="option.componentType" placeholder="chain"></el-input></el-form-item>
        <el-form-item label="代码"><el-input v-model="option.optionCode"></el-input></el-form-item>
        <el-form-item label="名称"><el-input v-model="option.name"></el-input></el-form-item>
        <el-form-item label="加价"><el-input-number v-model="option.priceDelta" :precision="2" :step="10"></el-input-number></el-form-item>
        <el-form-item label="图层"><el-input-number v-model="option.zIndex" :step="1"></el-input-number></el-form-item>
        <el-form-item label="启用"><el-switch v-model="option.enabled" :active-value="1" :inactive-value="0"></el-switch></el-form-item>
        <el-button v-if="isAuth('custom:config:update')" type="primary" :disabled="!selectedGoodsId" @click="saveOption">保存选项</el-button>
        <el-button @click="resetOption">新建选项</el-button>
      </el-form>
      <el-table :data="options" border><el-table-column prop="id" label="ID" width="80"></el-table-column><el-table-column prop="componentType" label="类型"></el-table-column><el-table-column prop="optionCode" label="代码"></el-table-column><el-table-column prop="name" label="名称"></el-table-column><el-table-column prop="priceDelta" label="加价"></el-table-column><el-table-column prop="zIndex" label="图层"></el-table-column><el-table-column label="操作" width="90"><template slot-scope="scope"><el-button type="text" @click="editOption(scope.row)">编辑</el-button></template></el-table-column></el-table>
    </el-card>
  </div>
</template>

<script>
export default {
  data () { return { products: [], options: [], selectedGoodsId: null, product: { goodsId: null, templateCode: 'necklace-v1', requiredComponentTypes: '', enabled: 1 }, option: { id: null, componentType: '', optionCode: '', name: '', priceDelta: 0, zIndex: 0, enabled: 1 } } },
  activated () { this.loadProducts() },
  methods: {
    // 加载定制商品配置 / Loads custom product configurations.
    loadProducts () { this.$http({ url: '/custom/configuration/products', method: 'get' }).then(({data}) => { this.products = data && data.code === 0 ? data.data : [] }) },
    // 保存定制商品配置 / Saves the custom product configuration.
    saveProduct () { this.$http({ url: '/custom/configuration/product/save', method: 'post', data: this.product }).then(({data}) => { if (data && data.code === 0) { this.$message.success('商品配置已保存'); this.loadProducts() } else this.$message.error(data.msg || '保存失败') }) },
    // 选择待配置的商品 / Selects the product for option configuration.
    selectProduct (row) { this.selectedGoodsId = row.goodsId; this.product = Object.assign({}, row); this.resetOption(); this.loadOptions() },
    // 加载组件选项 / Loads component options.
    loadOptions () { this.$http({ url: '/custom/configuration/options', method: 'get', params: { goodsId: this.selectedGoodsId } }).then(({data}) => { this.options = data && data.code === 0 ? data.data : [] }) },
    // 重置选项编辑表单 / Resets the option editor.
    resetOption () { this.option = { id: null, componentType: '', optionCode: '', name: '', priceDelta: 0, zIndex: 0, enabled: 1 } },
    // 编辑组件选项 / Edits a component option.
    editOption (row) { this.option = Object.assign({}, row) },
    // 保存组件选项 / Saves a component option.
    saveOption () { const payload = Object.assign({ goodsId: this.selectedGoodsId }, this.option); this.$http({ url: '/custom/configuration/option/save', method: 'post', data: payload }).then(({data}) => { if (data && data.code === 0) { this.$message.success('组件选项已保存'); this.resetOption(); this.loadOptions() } else this.$message.error(data.msg || '保存失败') }) }
  }
}
</script>

<style scoped>
.card { margin-top: 16px; }
</style>
