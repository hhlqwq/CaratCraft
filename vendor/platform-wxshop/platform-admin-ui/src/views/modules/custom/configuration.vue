<template>
  <div class="mod-custom-configuration">
    <el-alert type="info" :closable="false" show-icon
              title="商家可在“商品管理 → 商品列表”网页新增或导入完整商品，再在本页维护该商品的 DIY 配件、图层和模型。"></el-alert>
    <div class="toolbar">
      <el-upload v-if="isAuth('custom:config:update')" action="" accept=".json" :auto-upload="false"
                 :show-file-list="false" :on-change="importConfiguration">
        <el-button type="success">导入商品配件 JSON</el-button>
      </el-upload>
      <el-button type="text" @click="downloadTemplate">下载配件导入模板</el-button>
    </div>

    <el-card class="card" shadow="never">
      <div slot="header">定制商品</div>
      <el-form label-width="110px">
        <el-row :gutter="18">
          <el-col :span="12">
            <el-form-item label="商城商品">
              <el-select v-model="product.goodsId" filterable placeholder="请选择商品" class="full-width">
                <el-option v-for="item in goods" :key="item.id" :value="item.id"
                           :label="`${item.name}（ID: ${item.id}）`"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6"><el-form-item label="模板代码"><el-input v-model="product.templateCode" placeholder="necklace-v1"></el-input></el-form-item></el-col>
          <el-col :span="6"><el-form-item label="启用"><el-switch v-model="product.enabled" :active-value="1" :inactive-value="0"></el-switch></el-form-item></el-col>
        </el-row>
        <el-row :gutter="18">
          <el-col :span="6"><el-form-item label="预览宽度"><el-input-number v-model="product.previewWidth" :min="1" class="full-width"></el-input-number></el-form-item></el-col>
          <el-col :span="6"><el-form-item label="预览高度"><el-input-number v-model="product.previewHeight" :min="1" class="full-width"></el-input-number></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="必选组件"><el-input v-model="product.requiredComponentTypes" placeholder="material,length,style,pearl"></el-input></el-form-item></el-col>
        </el-row>
        <el-form-item label="商品模型 URL"><el-input v-model="product.modelUrl" placeholder="可填写 GLB/GLTF 模型地址"></el-input></el-form-item>
        <el-button v-if="isAuth('custom:config:update')" type="primary" @click="saveProduct">保存商品配置</el-button>
        <el-button @click="resetProduct">新建配置</el-button>
      </el-form>
      <el-table :data="products" border class="table-space">
        <el-table-column prop="goodsId" label="商品 ID" width="100"></el-table-column>
        <el-table-column label="商品名称" min-width="180"><template slot-scope="scope">{{ goodsName(scope.row.goodsId) }}</template></el-table-column>
        <el-table-column prop="templateCode" label="模板" min-width="130"></el-table-column>
        <el-table-column prop="requiredComponentTypes" label="必选组件" min-width="180"></el-table-column>
        <el-table-column label="预览尺寸" width="120"><template slot-scope="scope">{{ scope.row.previewWidth }} × {{ scope.row.previewHeight }}</template></el-table-column>
        <el-table-column label="状态" width="80"><template slot-scope="scope"><el-tag :type="scope.row.enabled ? 'success' : 'info'">{{ scope.row.enabled ? '启用' : '停用' }}</el-tag></template></el-table-column>
        <el-table-column label="操作" width="140"><template slot-scope="scope"><el-button type="text" @click="selectProduct(scope.row)">编辑商品与配件</el-button></template></el-table-column>
      </el-table>
    </el-card>

    <el-card class="card" shadow="never">
      <div slot="header">DIY 配件（当前商品：{{ selectedGoodsName }}）</div>
      <el-alert v-if="!selectedGoodsId" title="请先从上方列表选择一个已保存的定制商品。" type="warning" :closable="false"></el-alert>
      <el-form label-width="105px">
        <el-row :gutter="18">
          <el-col :span="6">
            <el-form-item label="组件类型">
              <el-select v-model="option.componentType" filterable allow-create default-first-option placeholder="请选择或输入类型" class="full-width">
                <el-option v-for="item in componentTypes" :key="item.value" :label="item.label" :value="item.value"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6"><el-form-item label="选项代码"><el-input v-model="option.optionCode" placeholder="chain-18k"></el-input></el-form-item></el-col>
          <el-col :span="6"><el-form-item label="显示名称"><el-input v-model="option.name" placeholder="18K 金链"></el-input></el-form-item></el-col>
          <el-col :span="6"><el-form-item label="启用"><el-switch v-model="option.enabled" :active-value="1" :inactive-value="0"></el-switch></el-form-item></el-col>
        </el-row>
        <el-row :gutter="18">
          <el-col :span="6"><el-form-item label="加价"><el-input-number v-model="option.priceDelta" :precision="2" :min="0" class="full-width"></el-input-number></el-form-item></el-col>
          <el-col :span="6"><el-form-item label="图层顺序"><el-input-number v-model="option.zIndex" :min="0" class="full-width"></el-input-number></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="2D 图层 URL"><el-input v-model="option.assetUrl" placeholder="PNG/SVG 图片地址"></el-input></el-form-item></el-col>
        </el-row>
        <el-form-item label="3D 模型 URL"><el-input v-model="option.modelUrl" placeholder="GLB/GLTF 模型地址"></el-input></el-form-item>
        <el-form-item label="材质参数 JSON"><el-input v-model="option.materialParams" type="textarea" :rows="3" placeholder='例如：{"metalness":0.8,"color":"#D4AF37"}'></el-input></el-form-item>
        <el-button v-if="isAuth('custom:config:update')" type="primary" :disabled="!selectedGoodsId" @click="saveOption">保存配件</el-button>
        <el-button @click="resetOption">新建配件</el-button>
      </el-form>
      <el-table :data="options" border class="table-space">
        <el-table-column prop="componentType" label="类型" width="110"></el-table-column>
        <el-table-column prop="optionCode" label="代码" min-width="130"></el-table-column>
        <el-table-column prop="name" label="名称" min-width="130"></el-table-column>
        <el-table-column prop="priceDelta" label="加价" width="90"></el-table-column>
        <el-table-column prop="assetUrl" label="2D 图层" min-width="210" show-overflow-tooltip></el-table-column>
        <el-table-column prop="modelUrl" label="3D 模型" min-width="180" show-overflow-tooltip></el-table-column>
        <el-table-column prop="zIndex" label="图层" width="70"></el-table-column>
        <el-table-column label="操作" width="130">
          <template slot-scope="scope">
            <el-button type="text" @click="editOption(scope.row)">编辑</el-button>
            <el-button v-if="isAuth('custom:config:update')" type="text" class="danger-action" @click="deleteOption(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
const createProduct = () => ({
  goodsId: null,
  templateCode: 'necklace-v1',
  previewWidth: 750,
  previewHeight: 750,
  modelUrl: '',
  requiredComponentTypes: 'material,length,style,pearl',
  enabled: 1
})
const createOption = () => ({
  id: null,
  componentType: '',
  optionCode: '',
  name: '',
  priceDelta: 0,
  assetUrl: '',
  modelUrl: '',
  materialParams: '{}',
  zIndex: 0,
  enabled: 1
})

export default {
  data () {
    return {
      goods: [],
      products: [],
      options: [],
      selectedGoodsId: null,
      product: createProduct(),
      option: createOption(),
      componentTypes: [
        {value: 'material', label: '材质（material）'},
        {value: 'length', label: '长度（length）'},
        {value: 'style', label: '款式（style）'},
        {value: 'pearl', label: '珍珠种类（pearl）'}
      ]
    }
  },
  computed: {
    selectedGoodsName () {
      return this.selectedGoodsId ? `${this.goodsName(this.selectedGoodsId)}（ID: ${this.selectedGoodsId}）` : '未选择'
    }
  },
  activated () {
    this.loadGoods()
    this.loadProducts()
  },
  methods: {
    // 加载商城商品 / Loads mall products.
    loadGoods () {
      this.$http({url: '/custom/configuration/goods', method: 'get'}).then(({data}) => {
        this.goods = data && data.code === 0 ? data.data : []
      })
    },
    // 加载定制商品配置 / Loads custom product configurations.
    loadProducts () {
      this.$http({url: '/custom/configuration/products', method: 'get'}).then(({data}) => {
        this.products = data && data.code === 0 ? data.data : []
      })
    },
    // 获取商品显示名称 / Resolves a product display name.
    goodsName (goodsId) {
      const item = this.goods.find(goods => goods.id === goodsId)
      return item ? item.name : `商品 ${goodsId}`
    },
    // 保存定制商品配置 / Saves the custom product configuration.
    saveProduct () {
      this.$http({url: '/custom/configuration/product/save', method: 'post', data: this.product, showError: false}).then(({data}) => {
        if (!data || data.code !== 0) {
          this.$message.error((data && data.msg) || '商品配置保存失败')
          return
        }
        this.$message.success('商品配置已保存')
        this.selectedGoodsId = this.product.goodsId
        this.loadProducts()
        this.loadOptions()
      })
    },
    // 重置商品编辑表单 / Resets the product editor.
    resetProduct () {
      this.product = createProduct()
      this.selectedGoodsId = null
      this.options = []
      this.resetOption()
    },
    // 选择待配置商品 / Selects a product for component configuration.
    selectProduct (row) {
      this.selectedGoodsId = row.goodsId
      this.product = Object.assign(createProduct(), row)
      this.resetOption()
      this.loadOptions()
    },
    // 加载组件选项 / Loads component options.
    loadOptions () {
      if (!this.selectedGoodsId) {
        return
      }
      this.$http({url: '/custom/configuration/options', method: 'get', params: {goodsId: this.selectedGoodsId}}).then(({data}) => {
        this.options = data && data.code === 0 ? data.data : []
      })
    },
    // 重置配件编辑表单 / Resets the component editor.
    resetOption () {
      this.option = createOption()
    },
    // 编辑配件 / Edits a component option.
    editOption (row) {
      this.option = Object.assign(createOption(), row)
    },
    // 保存配件 / Saves a component option.
    saveOption () {
      const payload = Object.assign({goodsId: this.selectedGoodsId}, this.option)
      this.$http({url: '/custom/configuration/option/save', method: 'post', data: payload, showError: false}).then(({data}) => {
        if (!data || data.code !== 0) {
          this.$message.error((data && data.msg) || '配件保存失败')
          return
        }
        this.$message.success('配件已保存')
        this.resetOption()
        this.loadOptions()
      })
    },
    // 删除配件及其库存 / Deletes a component option and its stock.
    deleteOption (row) {
      this.$confirm(`确定删除“${row.name}”吗？对应组件库存也会一并删除。`, '删除配件', {type: 'warning'})
        .then(() => this.$http({
          url: '/custom/configuration/option/delete',
          method: 'post',
          params: {id: row.id},
          showError: false
        }))
        .then(({data}) => {
          if (!data || data.code !== 0) {
            throw new Error((data && data.msg) || '删除失败')
          }
          this.$message.success('配件已删除')
          if (this.option.id === row.id) {
            this.resetOption()
          }
          this.loadOptions()
        }).catch(error => {
          if (error !== 'cancel' && error !== 'close') {
            this.$message.error(`删除失败：${error.message || error}`)
          }
        })
    },
    // 导入完整定制商品配件配置 / Imports a complete customization configuration.
    importConfiguration (file) {
      const reader = new FileReader()
      reader.onload = event => {
        try {
          const payload = JSON.parse(event.target.result)
          if (!payload.product || !payload.product.goodsId || !Array.isArray(payload.options)) {
            throw new Error('JSON 必须包含 product.goodsId 和 options 数组')
          }
          this.$confirm('导入将覆盖该商品现有配件，是否继续？', '导入确认', {type: 'warning'})
            .then(() => this.$http({url: '/custom/configuration/import', method: 'post', data: payload, showError: false}))
            .then(({data}) => {
              if (!data || data.code !== 0) {
                throw new Error((data && data.msg) || '导入失败')
              }
              this.$message.success(`导入成功，共 ${data.data.optionCount} 个配件`)
              this.selectedGoodsId = payload.product.goodsId
              this.product = Object.assign(createProduct(), payload.product)
              this.loadProducts()
              this.loadOptions()
            }).catch(error => {
              if (error !== 'cancel' && error !== 'close') {
                this.$message.error(`导入失败：${error.message || error}`)
              }
            })
        } catch (error) {
          this.$message.error(`JSON 解析失败：${error.message}`)
        }
      }
      reader.onerror = () => this.$message.error('无法读取所选 JSON 文件')
      reader.readAsText(file.raw, 'UTF-8')
    },
    // 下载配件导入模板 / Downloads the component import template.
    downloadTemplate () {
      const payload = {
        product: Object.assign(createProduct(), {goodsId: 1006002}),
        replaceOptions: true,
        options: [
          Object.assign(createOption(), {
            componentType: 'material',
            optionCode: 'material-18k',
            name: '演示 · 18K 黄金',
            assetUrl: '/static/images/designer/chain-gold.svg',
            materialParams: '{"metalness":0.8}'
          }),
          Object.assign(createOption(), {
            componentType: 'length',
            optionCode: 'length-45',
            name: '演示 · 45cm'
          }),
          Object.assign(createOption(), {
            componentType: 'style',
            optionCode: 'style-classic',
            name: '演示 · 经典单珠',
            assetUrl: '/static/images/designer/pendant-solitaire.svg'
          }),
          Object.assign(createOption(), {
            componentType: 'pearl',
            optionCode: 'pearl-akoya',
            name: '演示 · Akoya 海水珍珠',
            assetUrl: '/static/images/designer/pearl-akoya.svg'
          })
        ]
      }
      const blob = new Blob([JSON.stringify(payload, null, 2)], {type: 'application/json;charset=utf-8'})
      const link = document.createElement('a')
      link.href = URL.createObjectURL(blob)
      link.download = '定制商品配件导入模板.json'
      link.click()
      URL.revokeObjectURL(link.href)
    }
  }
}
</script>

<style scoped>
.toolbar { display: flex; align-items: center; gap: 10px; margin-top: 16px; }
.card { margin-top: 16px; }
.table-space { margin-top: 18px; }
.full-width { width: 100%; }
.danger-action { color: #c45656; }
</style>
