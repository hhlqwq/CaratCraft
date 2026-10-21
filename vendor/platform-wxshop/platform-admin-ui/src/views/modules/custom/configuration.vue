<template>
  <div class="mod-custom-configuration">
    <el-alert
      type="info"
      :closable="false"
      show-icon
      title="商家可在“商品管理 → 商品列表”网页新增或导入完整商品，再在本页维护该商品的 DIY 配件和二维图层。"
    ></el-alert>
    <div class="toolbar">
      <el-upload
        v-if="isAuth('custom:config:update')"
        action=""
        accept=".json"
        :auto-upload="false"
        :show-file-list="false"
        :on-change="importConfiguration"
      >
        <el-button type="success">导入商品配件 JSON</el-button>
      </el-upload>
      <el-button type="text" @click="downloadTemplate">下载配件导入模板</el-button>
    </div>

    <el-card class="card" shadow="never">
      <template #header><div>定制商品</div></template>
      <el-form label-width="110px">
        <el-row :gutter="18">
          <el-col :xs="24" :sm="24" :lg="12">
            <el-form-item label="商城商品">
              <el-select
                v-model="product.goodsId"
                filterable
                placeholder="请选择商品"
                class="full-width"
              >
                <el-option
                  v-for="item in goods"
                  :key="item.id"
                  :value="item.id"
                  :label="`${item.name}（ID: ${item.id}）`"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :lg="12"
            ><el-form-item label="模板代码"
              ><el-input
                v-model="product.templateCode"
                placeholder="necklace-v1"
              ></el-input></el-form-item
          ></el-col>
          <el-col :xs="24" :sm="12" :lg="12"
            ><el-form-item label="启用"
              ><el-switch
                v-model="product.enabled"
                :active-value="1"
                :inactive-value="0"
              ></el-switch></el-form-item
          ></el-col>
        </el-row>
        <el-row :gutter="18">
          <el-col :xs="24" :sm="12" :lg="12"
            ><el-form-item label="预览宽度"
              ><el-input-number
                v-model="product.previewWidth"
                :min="100"
                :max="4000"
                class="full-width"
              ></el-input-number></el-form-item
          ></el-col>
          <el-col :xs="24" :sm="12" :lg="12"
            ><el-form-item label="预览高度"
              ><el-input-number
                v-model="product.previewHeight"
                :min="100"
                :max="4000"
                class="full-width"
              ></el-input-number></el-form-item
          ></el-col>
          <el-col :xs="24" :sm="24" :lg="12"
            ><el-form-item label="必选组件"
              ><el-input
                v-model="product.requiredComponentTypes"
                placeholder="pearl,chain,clasp"
              ></el-input></el-form-item
          ></el-col>
        </el-row>
        <el-form-item label="成品长度规则">
          <div class="length-rule-editor">
            <el-switch
              :model-value="Boolean(product.lengthRules)"
              @update:model-value="toggleLengthRules"
              active-text="配置制作规则"
              inactive-text="只允许草稿"
            />
            <el-alert
              v-if="lengthSettings.invalid"
              title="已有规则格式无效, 请先关闭再重新配置, 不会自动覆盖原内容"
              type="error"
              :closable="false"
            />
            <template v-else-if="product.lengthRules">
              <el-select
                class="length-rule-mode"
                placeholder="请选择实际制作方式"
                :model-value="lengthSettings.mode"
                @update:model-value="changeLengthRule('mode', $event)"
              >
                <el-option value="SUM" label="串珠与链段相加" />
                <el-option value="OVERLAY_MAX" label="链材承载串珠, 取较长部分" />
              </el-select>
              <div class="length-rule-grid">
                <label v-for="field in lengthRuleFields" :key="field.key" class="length-rule-field">
                  <span>{{ field.label }} (mm)</span>
                  <el-input-number
                    :model-value="lengthSettings[field.key]"
                    :min="field.zero ? 0 : 0.001"
                    :max="3000"
                    :precision="3"
                    :controls="false"
                    class="full-width"
                    @update:model-value="changeLengthRule(field.key, $event)"
                  />
                </label>
              </div>
            </template>
            <p class="length-rule-hint">
              所有字段由商家明确填写, 空值表示尚未配置. 扣件及连接统一计入规则,
              加工缩短量从材料长度扣减; 吊坠等悬挂件不计周长. 规则未完整时不能发布此配置,
              未配置规则的设计只可保存草稿.
            </p>
          </div>
        </el-form-item>
        <el-button v-if="isAuth('custom:config:update')" type="primary" @click="saveProduct"
          >保存商品配置</el-button
        >
        <el-button @click="resetProduct">新建配置</el-button>
      </el-form>
      <el-table :data="products" border class="table-space">
        <el-table-column prop="goodsId" label="商品 ID" width="100"></el-table-column>
        <el-table-column label="商品名称" min-width="180"
          ><template #default="scope">{{ goodsName(scope.row.goodsId) }}</template></el-table-column
        >
        <el-table-column prop="templateCode" label="模板" min-width="130"></el-table-column>
        <el-table-column
          prop="requiredComponentTypes"
          label="必选组件"
          min-width="180"
        ></el-table-column>
        <el-table-column label="预览尺寸" width="120"
          ><template #default="scope"
            >{{ scope.row.previewWidth }} × {{ scope.row.previewHeight }}</template
          ></el-table-column
        >
        <el-table-column label="状态" width="80"
          ><template #default="scope"
            ><el-tag :type="scope.row.enabled ? 'success' : 'info'">{{
              scope.row.enabled ? '启用' : '停用'
            }}</el-tag></template
          ></el-table-column
        >
        <el-table-column label="操作" width="140"
          ><template #default="scope"
            ><el-button type="text" @click="selectProduct(scope.row)"
              >编辑商品与配件</el-button
            ></template
          ></el-table-column
        >
      </el-table>
    </el-card>

    <el-card class="card" shadow="never">
      <template #header
        ><div>DIY 配件（当前商品：{{ selectedGoodsName }}）</div></template
      >
      <el-alert
        v-if="!selectedGoodsId"
        title="请先从上方列表选择一个已保存的定制商品。"
        type="warning"
        :closable="false"
      ></el-alert>
      <el-form label-width="105px">
        <el-row :gutter="18">
          <el-col :xs="24" :sm="12" :lg="12">
            <el-form-item label="组件类型">
              <el-select
                v-model="option.componentType"
                filterable
                allow-create
                default-first-option
                placeholder="请选择或输入类型"
                class="full-width"
              >
                <el-option
                  v-for="item in componentTypes"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :lg="12"
            ><el-form-item label="选项代码"
              ><el-input
                v-model="option.optionCode"
                placeholder="chain-18k"
              ></el-input></el-form-item
          ></el-col>
          <el-col :xs="24" :sm="12" :lg="12"
            ><el-form-item label="显示名称"
              ><el-input v-model="option.name" placeholder="18K 金链"></el-input></el-form-item
          ></el-col>
          <el-col :xs="24" :sm="12" :lg="12"
            ><el-form-item label="启用"
              ><el-switch
                v-model="option.enabled"
                :active-value="1"
                :inactive-value="0"
              ></el-switch></el-form-item
          ></el-col>
        </el-row>
        <el-row :gutter="18">
          <el-col :xs="24" :sm="12" :lg="12"
            ><el-form-item label="加价"
              ><el-input-number
                v-model="option.priceDelta"
                :precision="2"
                :min="0"
                class="full-width"
              ></el-input-number></el-form-item
          ></el-col>
          <el-col :xs="24" :sm="12" :lg="12"
            ><el-form-item label="图层顺序"
              ><el-input-number
                v-model="option.zIndex"
                :min="0"
                class="full-width"
              ></el-input-number></el-form-item
          ></el-col>
          <el-col :xs="24" :sm="24" :lg="12"
            ><el-form-item label="2D 图层 URL"
              ><el-input
                v-model="option.assetUrl"
                placeholder="PNG/SVG 图片地址"
              ></el-input></el-form-item
          ></el-col>
        </el-row>
        <el-row :gutter="18">
          <el-col :xs="24" :sm="12" :lg="12"
            ><el-form-item label="单件耗用量"
              ><el-input-number
                v-model="ruleForm.quantity"
                :min="0"
                :max="10000"
                :precision="0" /></el-form-item
          ></el-col>
          <el-col :xs="24" :sm="12" :lg="12"
            ><el-form-item label="库存计量单位"
              ><el-input
                v-model="ruleForm.unit"
                maxlength="10"
                placeholder="颗、件或毫米" /></el-form-item
          ></el-col>
          <el-col :xs="24" :sm="24" :lg="12"
            ><el-form-item label="不能搭配"
              ><el-input
                v-model="ruleForm.incompatible"
                placeholder="组件类型:代码, 多项用英文逗号分隔" /></el-form-item
          ></el-col>
          <el-col :xs="24" :sm="24" :lg="12"
            ><el-form-item label="必须搭配"
              ><el-input
                v-model="ruleForm.required"
                placeholder="组件类型:代码, 未提供规则可留空" /></el-form-item
          ></el-col>
        </el-row>
        <el-alert
          title="排列中每添加一次材料, 收取一次配件价格并扣减一次单件耗用量。尺寸填写实测值, 不取规格区间中点; 未配置尺寸不显示完整长度。"
          type="info"
          :closable="false"
        />
        <el-row :gutter="18">
          <el-col :xs="24" :sm="12" :lg="12"
            ><el-form-item label="珍珠直径mm"
              ><el-input
                v-model="ruleForm.diameterMm"
                inputmode="decimal"
                clearable
                placeholder="实测尺寸, 未知留空" /></el-form-item
          ></el-col>
          <el-col :xs="24" :sm="12" :lg="12"
            ><el-form-item label="配件占长mm"
              ><el-input
                v-model="ruleForm.spanMm"
                inputmode="decimal"
                clearable
                placeholder="实测尺寸, 未知留空" /></el-form-item
          ></el-col>
          <el-col :xs="24" :sm="12" :lg="12"
            ><el-form-item label="链材长度mm"
              ><el-input
                v-model="ruleForm.lengthMm"
                inputmode="decimal"
                clearable
                placeholder="实测尺寸, 未知留空" /></el-form-item
          ></el-col>
          <el-col :xs="24" :sm="12" :lg="12"
            ><el-form-item label="形状"
              ><el-input
                v-model="ruleForm.shape"
                maxlength="30"
                placeholder="近正圆; 异形珍珠请明确填写" /></el-form-item
          ></el-col>
          <el-col :xs="24" :sm="12" :lg="12"
            ><el-form-item label="光泽"
              ><el-input
                v-model="ruleForm.luster"
                maxlength="30"
                placeholder="极光" /></el-form-item
          ></el-col>
          <el-col :xs="24" :sm="12" :lg="12"
            ><el-form-item label="瑕疵"
              ><el-input
                v-model="ruleForm.blemish"
                maxlength="30"
                placeholder="微瑕" /></el-form-item
          ></el-col>
        </el-row>
        <el-button
          v-if="isAuth('custom:config:update')"
          type="primary"
          :disabled="!selectedGoodsId"
          @click="saveOption"
          >保存配件</el-button
        >
        <el-button @click="resetOption">新建配件</el-button>
      </el-form>
      <el-table :data="options" border class="table-space">
        <el-table-column prop="componentType" label="类型" width="110"></el-table-column>
        <el-table-column prop="optionCode" label="代码" min-width="130"></el-table-column>
        <el-table-column prop="name" label="名称" min-width="130"></el-table-column>
        <el-table-column prop="priceDelta" label="加价" width="90"></el-table-column>
        <el-table-column
          prop="assetUrl"
          label="2D 图层"
          min-width="210"
          show-overflow-tooltip
        ></el-table-column>
        <el-table-column prop="zIndex" label="图层" width="70"></el-table-column>
        <el-table-column label="操作" width="130">
          <template #default="scope">
            <el-button type="text" @click="editOption(scope.row)">编辑</el-button>
            <el-button
              v-if="isAuth('custom:config:update')"
              type="text"
              class="danger-action"
              @click="deleteOption(scope.row)"
              >停用</el-button
            >
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
  requiredComponentTypes: 'pearl,chain,clasp',
  enabled: 0,
  lengthRules: '',
});
const createOption = () => ({
  id: null,
  componentType: '',
  optionCode: '',
  name: '',
  priceDelta: 0,
  assetUrl: '',
  zIndex: 0,
  enabled: 1,
});

export default {
  data() {
    return {
      goods: [],
      products: [],
      options: [],
      selectedGoodsId: null,
      product: createProduct(),
      lengthRuleFields: [
        {key: 'claspMm', label: '扣件长度', zero: true},
        {key: 'connectionMm', label: '连接长度', zero: true},
        {key: 'lossMm', label: '加工缩短量', zero: true},
        {key: 'toleranceMm', label: '允许误差', zero: true},
        {key: 'minLengthMm', label: '最小成品长度', zero: false},
        {key: 'maxLengthMm', label: '最大成品长度', zero: false},
      ],
      option: createOption(),
      ruleForm: {
        quantity: 1,
        unit: '件',
        incompatible: '',
        required: '',
        diameterMm: null,
        spanMm: null,
        lengthMm: null,
        shape: '近正圆',
        luster: '极光',
        blemish: '微瑕',
      },
      componentTypes: [
        {value: 'clasp', label: '尾扣（clasp）'},
        {value: 'accessory', label: '配饰（accessory）'},
        {value: 'pearl', label: '珍珠种类与规格（pearl）'},
        {value: 'material', label: '贵金属材质（material）'},
        {value: 'gem', label: '钻石与宝石（gem）'},
        {value: 'fitting', label: '搭配配件（fitting）'},
        {value: 'length', label: '长度（length）'},
        {value: 'style', label: '款式（style）'},
        {value: 'chain', label: '链条材质（chain）'},
        {value: 'pendant', label: '吊坠款式（pendant）'},
      ],
    };
  },
  computed: {
    /** 从已保存规则读取表单, 无法解析时明确提示而不静默丢弃原配置. */
    lengthSettings() {
      if (!this.product.lengthRules) {
        return {};
      }
      try {
        const rules = JSON.parse(this.product.lengthRules);
        return rules && typeof rules === 'object' && !Array.isArray(rules)
          ? rules
          : {invalid: true};
      } catch (error) {
        return {invalid: true};
      }
    },
    selectedGoodsName() {
      return this.selectedGoodsId
        ? `${this.goodsName(this.selectedGoodsId)}（ID: ${this.selectedGoodsId}）`
        : '未选择';
    },
  },
  activated() {
    this.loadGoods();
    this.loadProducts();
  },
  methods: {
    /** 显式开启时所有毫米参数保持未知, 不用零值替代商家真实数据. */
    toggleLengthRules(enabled) {
      this.product.lengthRules = enabled
        ? JSON.stringify({
            mode: null,
            claspMm: null,
            connectionMm: null,
            lossMm: null,
            toleranceMm: null,
            minLengthMm: null,
            maxLengthMm: null,
          })
        : '';
    },
    /** 每次字段变化写回完整规则, 编辑旧商品时仍保留未修改参数. */
    changeLengthRule(field, value) {
      this.product.lengthRules = JSON.stringify({
        ...this.lengthSettings,
        [field]: value === undefined ? null : value,
      });
    },
    // 空尺寸保持未知, 不让数字控件自动补成最小值或将空串转成零.
    measuredSize(value) {
      if (value === null || value === undefined || String(value).trim() === '') {
        return null;
      }
      const text = String(value).trim();
      const size = Number(text);
      if (!/^\d+(\.\d{1,3})?$/.test(text) || !Number.isFinite(size) || size <= 0 || size > 3000) {
        throw new Error('尺寸须为0.001到3000mm, 最多三位小数, 未知请留空');
      }
      return size;
    },
    // 加载商城商品 / Loads mall products.
    loadGoods() {
      this.$http({url: '/custom/configuration/goods', method: 'get'}).then(({data}) => {
        this.goods = data && data.code === 0 ? data.data : [];
      });
    },
    // 加载定制商品配置 / Loads custom product configurations.
    loadProducts() {
      this.$http({url: '/custom/configuration/products', method: 'get'}).then(({data}) => {
        this.products = data && data.code === 0 ? data.data : [];
      });
    },
    // 获取商品显示名称 / Resolves a product display name.
    goodsName(goodsId) {
      const item = this.goods.find((goods) => goods.id === goodsId);
      return item ? item.name : `商品 ${goodsId}`;
    },
    // 保存定制商品配置 / Saves the custom product configuration.
    saveProduct() {
      if (
        this.product.lengthRules &&
        (this.lengthSettings.invalid ||
          !['SUM', 'OVERLAY_MAX'].includes(this.lengthSettings.mode) ||
          this.lengthRuleFields.some(
            (field) =>
              this.lengthSettings[field.key] === null ||
              this.lengthSettings[field.key] === undefined
          ) ||
          this.lengthSettings.minLengthMm > this.lengthSettings.maxLengthMm)
      ) {
        this.$message.error('请完整填写制作规则, 最小长度不能大于最大长度');
        return;
      }
      this.$http({
        url: '/custom/configuration/product/save',
        method: 'post',
        data: this.product,
        showError: false,
      }).then(({data}) => {
        if (!data || data.code !== 0) {
          this.$message.error((data && data.msg) || '商品配置保存失败');
          return;
        }
        this.$message.success('商品配置已保存');
        this.selectedGoodsId = this.product.goodsId;
        this.loadProducts();
        this.loadOptions();
      });
    },
    // 重置商品编辑表单 / Resets the product editor.
    resetProduct() {
      this.product = createProduct();
      this.selectedGoodsId = null;
      this.options = [];
      this.resetOption();
    },
    // 选择待配置商品 / Selects a product for component configuration.
    selectProduct(row) {
      this.selectedGoodsId = row.goodsId;
      this.product = Object.assign(createProduct(), row);
      this.resetOption();
      this.loadOptions();
    },
    // 加载组件选项 / Loads component options.
    loadOptions() {
      if (!this.selectedGoodsId) {
        return;
      }
      this.$http({
        url: '/custom/configuration/options',
        method: 'get',
        params: {goodsId: this.selectedGoodsId},
      }).then(({data}) => {
        this.options = data && data.code === 0 ? data.data : [];
      });
    },
    // 重置配件编辑表单 / Resets the component editor.
    resetOption() {
      this.option = createOption();
      this.ruleForm = {
        quantity: 1,
        unit: '件',
        incompatible: '',
        required: '',
        diameterMm: null,
        spanMm: null,
        lengthMm: null,
        shape: '近正圆',
        luster: '极光',
        blemish: '微瑕',
      };
    },
    // 编辑配件 / Edits a component option.
    editOption(row) {
      this.option = Object.assign(createOption(), row);
      try {
        const rules = JSON.parse(row.materialParams || '{}');
        this.ruleForm = {
          quantity: rules.stockQuantity === undefined ? 1 : rules.stockQuantity,
          unit: rules.stockUnit || '件',
          incompatible: (rules.incompatibleWith || []).join(','),
          required: (rules.requires || []).join(','),
          diameterMm: rules.diameterMm || null,
          spanMm: rules.spanMm || null,
          lengthMm: rules.lengthMm || null,
          shape: rules.shape || '近正圆',
          luster: rules.luster || '极光',
          blemish: rules.blemish || '微瑕',
        };
      } catch (error) {
        this.$message.error('原材料规则无法读取, 请联系管理员核对');
      }
    },
    // 保存配件 / Saves a component option.
    saveOption() {
      let payload;
      try {
        const rules = Object.assign({}, JSON.parse(this.option.materialParams || '{}'), {
          stockQuantity: this.ruleForm.quantity,
          stockUnit: this.ruleForm.unit,
          diameterMm: this.measuredSize(this.ruleForm.diameterMm),
          spanMm: this.measuredSize(this.ruleForm.spanMm),
          lengthMm: this.measuredSize(this.ruleForm.lengthMm),
          shape: this.ruleForm.shape,
          luster: this.ruleForm.luster,
          blemish: this.ruleForm.blemish,
          incompatibleWith: this.ruleForm.incompatible
            .split(',')
            .map((value) => value.trim())
            .filter(Boolean),
          requires: this.ruleForm.required
            .split(',')
            .map((value) => value.trim())
            .filter(Boolean),
        });
        payload = Object.assign({goodsId: this.selectedGoodsId}, this.option, {
          materialParams: JSON.stringify(rules),
        });
      } catch (error) {
        return this.$message.error(error.message || '材料规则格式无法读取');
      }
      this.$http({
        url: '/custom/configuration/option/save',
        method: 'post',
        data: payload,
        showError: false,
      }).then(({data}) => {
        if (!data || data.code !== 0) {
          this.$message.error((data && data.msg) || '配件保存失败');
          return;
        }
        this.$message.success('配件已保存');
        this.resetOption();
        this.loadOptions();
      });
    },
    // 停用配件并保留历史库存 / Deletes a component option and its stock.
    deleteOption(row) {
      this.$confirm(`确定删除“${row.name}”吗？材料将停用, 历史订单和库存会保留。`, '删除配件', {
        type: 'warning',
      })
        .then(() =>
          this.$http({
            url: '/custom/configuration/option/delete',
            method: 'post',
            params: {id: row.id},
            showError: false,
          })
        )
        .then(({data}) => {
          if (!data || data.code !== 0) {
            throw new Error((data && data.msg) || '删除失败');
          }
          this.$message.success('配件已停用');
          if (this.option.id === row.id) {
            this.resetOption();
          }
          this.loadOptions();
        })
        .catch((error) => {
          if (error !== 'cancel' && error !== 'close') {
            this.$message.error(`删除失败：${error.message || error}`);
          }
        });
    },
    // 导入完整定制商品配件配置 / Imports a complete customization configuration.
    importConfiguration(file) {
      const reader = new FileReader();
      reader.onload = (event) => {
        try {
          const payload = JSON.parse(event.target.result);
          if (!payload.product || !payload.product.goodsId || !Array.isArray(payload.options)) {
            throw new Error('JSON 必须包含 product.goodsId 和 options 数组');
          }
          this.$confirm('导入将覆盖该商品现有配件，是否继续？', '导入确认', {type: 'warning'})
            .then(() =>
              this.$http({
                url: '/custom/configuration/import',
                method: 'post',
                data: payload,
                showError: false,
              })
            )
            .then(({data}) => {
              if (!data || data.code !== 0) {
                throw new Error((data && data.msg) || '导入失败');
              }
              this.$message.success(`导入成功，共 ${data.data.optionCount} 个配件`);
              this.selectedGoodsId = payload.product.goodsId;
              this.product = Object.assign(createProduct(), payload.product);
              this.loadProducts();
              this.loadOptions();
            })
            .catch((error) => {
              if (error !== 'cancel' && error !== 'close') {
                this.$message.error(`导入失败：${error.message || error}`);
              }
            });
        } catch (error) {
          this.$message.error(`JSON 解析失败：${error.message}`);
        }
      };
      reader.onerror = () => this.$message.error('无法读取所选 JSON 文件');
      reader.readAsText(file.raw, 'UTF-8');
    },
    // 下载配件导入模板 / Downloads the component import template.
    downloadTemplate() {
      if (!this.selectedGoodsId) {
        return this.$message.warning('请先选择实际商城商品再下载模板');
      }
      const payload = {
        product: Object.assign(createProduct(), {
          goodsId: this.selectedGoodsId,
          enabled: 0,
          requiredComponentTypes: 'pearl,chain,clasp',
        }),
        replaceOptions: true,
        options: [
          Object.assign(createOption(), {
            componentType: 'pearl',
            optionCode: 'sample-pearl',
            name: '示例珍珠, 请替换实物规格',
            materialParams: JSON.stringify({
              stockQuantity: 1,
              stockUnit: '颗',
              shape: '近正圆',
              luster: '极光',
              blemish: '微瑕',
            }),
          }),
          Object.assign(createOption(), {
            componentType: 'chain',
            optionCode: 'sample-chain',
            name: '示例链材, 请填写材质和用量',
            materialParams: JSON.stringify({stockQuantity: 400, stockUnit: '毫米'}),
          }),
          Object.assign(createOption(), {
            componentType: 'clasp',
            optionCode: 'sample-clasp',
            name: '示例尾扣, 请填写实物规格',
            materialParams: JSON.stringify({stockQuantity: 1, stockUnit: '件'}),
          }),
        ],
      };
      const blob = new Blob([JSON.stringify(payload, null, 2)], {
        type: 'application/json;charset=utf-8',
      });
      const link = document.createElement('a');
      link.href = URL.createObjectURL(blob);
      link.download = '定制商品配件导入模板.json';
      link.click();
      URL.revokeObjectURL(link.href);
    },
  },
};
</script>

<style scoped>
.length-rule-editor {
  width: 100%;
  min-width: 0;
}
.length-rule-mode {
  width: 100%;
  margin: 12px 0;
}
.length-rule-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px 16px;
}
.length-rule-field span {
  display: block;
  margin-bottom: 6px;
  line-height: 22px;
}
.length-rule-hint {
  margin: 12px 0 0;
  line-height: 1.7;
  color: #606266;
}
@media (max-width: 1200px) {
  .length-rule-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
@media (max-width: 768px) {
  .length-rule-grid {
    grid-template-columns: minmax(0, 1fr);
  }
}
.toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 16px;
}
.card {
  margin-top: 16px;
}
.table-space {
  margin-top: 18px;
}
.full-width {
  width: 100%;
}
.danger-action {
  color: #c45656;
}
</style>
