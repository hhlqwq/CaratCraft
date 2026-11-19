<template>
    <div class="mod-custom-configuration">
        <el-alert
            type="info"
            :closable="false"
            show-icon
            title="商家可在“商品管理 → 商品列表”网页新增或导入完整商品，再在本页维护该商品的 DIY 配件和二维图层。"
        ></el-alert>
        <el-alert
            type="warning"
            :closable="false"
            title="DIY仅使用珍珠、尾扣、配饰、链条四类. 客户112项候选默认停用, 价格、实测尺寸、图片及库存须自行核对; 14项配饰为暂定拆分名称. 旧分类仅保留历史记录, 请停用后新建四类材料."
        ></el-alert>
        <el-alert
            v-if="readErrors.goods || readErrors.products || readErrors.options"
            type="error"
            :closable="false"
            :title="readErrors.goods || readErrors.products || readErrors.options"
        >
            <el-button @click="retryReads">重新加载</el-button>
        </el-alert>
        <div class="toolbar">
            <el-upload
                v-if="isAuth('custom:config:update')"
                action=""
                accept=".json"
                :auto-upload="false"
                :show-file-list="false"
                :on-change="importConfiguration"
                :disabled="writing"
            >
                <el-button type="success">导入商品配件 JSON</el-button>
            </el-upload>
            <el-button type="text" :loading="catalogDownloading" @click="downloadTemplate"
                >下载客户112项候选模板</el-button
            >
        </div>

        <el-card class="card" shadow="never">
            <template #header><div>定制商品</div></template>
            <el-form :inert="writing" label-width="110px">
                <el-row :gutter="18">
                    <el-col :xs="24" :sm="24" :lg="12">
                        <el-form-item label="商城商品">
                            <el-select
                                v-model="product.goodsId"
                                filterable
                                remote
                                :remote-method="searchGoods"
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
                            <el-pagination
                                small
                                layout="prev, pager, next"
                                :total="pagination.goods.total"
                                :page-size="pagination.goods.size"
                                :current-page="pagination.goods.page"
                                @current-change="changePage('goods', $event)"
                            />
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
                                <label
                                    v-for="field in lengthRuleFields"
                                    :key="field.key"
                                    class="length-rule-field"
                                >
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
                            加工缩短量从材料长度扣减; 吊坠等悬挂件不计周长.
                            规则未完整时不能发布此配置, 未配置规则的设计只可保存草稿.
                        </p>
                    </div>
                </el-form-item>
                <el-button
                    v-if="isAuth('custom:config:update')"
                    type="primary"
                    :loading="writing"
                    @click="saveProduct"
                    >保存商品配置</el-button
                >
                <el-button @click="resetProduct">新建配置</el-button>
            </el-form>
            <el-table :data="products" border class="table-space">
                <el-table-column prop="goodsId" label="商品 ID" width="100"></el-table-column>
                <el-table-column label="商品名称" min-width="180"
                    ><template #default="scope">{{
                        goodsName(scope.row.goodsId)
                    }}</template></el-table-column
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
            <el-pagination
                layout="total, prev, pager, next"
                :total="pagination.products.total"
                :page-size="pagination.products.size"
                :current-page="pagination.products.page"
                @current-change="changePage('products', $event)"
            />
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
            <el-form :inert="writing" label-width="105px">
                <el-row :gutter="18">
                    <el-col :xs="24" :sm="12" :lg="12">
                        <el-form-item label="组件类型">
                            <el-select
                                v-model="option.componentType"
                                filterable
                                default-first-option
                                placeholder="请选择四类材料"
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
                            ><el-input
                                v-model="option.name"
                                placeholder="18K 金链"
                            ></el-input></el-form-item
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
                        ><el-form-item label="每实例加价"
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
                    <el-col v-if="option.componentType === 'accessory'" :xs="24" :sm="12" :lg="12">
                        <el-form-item label="配饰计长位置">
                            <el-select
                                v-model="ruleForm.lengthRole"
                                clearable
                                placeholder="请按实际工艺确认"
                            >
                                <el-option value="INLINE" label="串接, 按实测跨度计长" />
                                <el-option value="HANGING" label="悬挂, 不占项链周长" />
                            </el-select>
                        </el-form-item>
                    </el-col>
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
                    :disabled="!selectedGoodsId || reading.options || !!readErrors.options"
                    :loading="writing"
                    @click="saveOption"
                    >保存配件</el-button
                >
                <el-button @click="resetOption">新建配件</el-button>
            </el-form>
            <el-input
                v-model="optionKeyword"
                placeholder="搜索材料名称或代码"
                clearable
                @change="searchOptions"
            />
            <el-table :data="options" border class="table-space">
                <el-table-column prop="componentType" label="类型" width="110"></el-table-column>
                <el-table-column prop="optionCode" label="代码" min-width="130"></el-table-column>
                <el-table-column prop="name" label="名称" min-width="130"></el-table-column>
                <el-table-column prop="priceDelta" label="每实例加价" width="90"></el-table-column>
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
                            @click="disableOption(scope.row)"
                            >停用</el-button
                        >
                    </template>
                </el-table-column>
            </el-table>
            <el-pagination
                layout="total, prev, pager, next"
                :total="pagination.options.total"
                :page-size="pagination.options.size"
                :current-page="pagination.options.page"
                @current-change="changePage('options', $event)"
            />
        </el-card>
    </div>
</template>

<script>
/** 创建未发布的独立商品配置, 制作规则保持未填写状态. */
const createProduct = () => ({
    goodsId: null,
    templateCode: 'necklace-v1',
    previewWidth: 750,
    previewHeight: 750,
    requiredComponentTypes: 'pearl,chain,clasp',
    enabled: 0,
    lengthRules: '',
});
/** 创建独立配件表单, 不继承上一个商品的选项身份. */
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
    /** 初始化当前组件的响应式界面和表单状态. */
    data() {
        return {
            pageActive: true,
            pageEpoch: 0,
            writing: false,
            catalogDownloading: false,
            reading: {goods: false, products: false, options: false},
            readErrors: {goods: '', products: '', options: ''},
            pagination: {
                goods: {page: 1, size: 20, total: 0},
                products: {page: 1, size: 20, total: 0},
                options: {page: 1, size: 20, total: 0},
            },
            goodsKeyword: '',
            optionKeyword: '',
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
                lengthRole: null,
                shape: '近正圆',
                luster: '极光',
                blemish: '微瑕',
            },
            componentTypes: [
                {value: 'pearl', label: '珍珠（pearl）'},
                {value: 'clasp', label: '尾扣（clasp）'},
                {value: 'accessory', label: '配饰（accessory）'},
                {value: 'chain', label: '链条（chain）'},
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
        /** 显示当前选中商品名称和标识, 未选择时保持提示状态. */
        selectedGoodsName() {
            return this.selectedGoodsId
                ? `${this.goodsName(this.selectedGoodsId)}（ID: ${this.selectedGoodsId}）`
                : '未选择';
        },
    },
    /** 页面激活时刷新当前业务数据. */
    activated() {
        this.pageActive = true;
        this.pageEpoch++;
        this.loadGoods();
        this.loadProducts();
        this.loadOptions();
    },
    /** 离页后废弃当前所有读取和写入回调. */
    deactivated() {
        this.pageActive = false;
        this.pageEpoch++;
    },
    /** 销毁时隔离尚未完成的接口响应. */
    beforeUnmount() {
        this.pageActive = false;
        this.pageEpoch++;
    },
    methods: {
        /** 独立翻页并继续复用读取代次隔离. */
        changePage(name, page) {
            this.pagination[name].page = page;
            if (name === 'goods') {
                return this.loadGoods();
            }
            if (name === 'products') {
                return this.loadProducts();
            }
            return this.loadOptions();
        },
        /** 商品名称由服务端搜索, 不限于本页已加载选项. */
        searchGoods(keyword) {
            this.goodsKeyword = keyword;
            return this.changePage('goods', 1);
        },
        /** 材料搜索从第一页开始并保留当前编辑表单. */
        searchOptions() {
            return this.changePage('options', 1);
        },
        /** 捕获当前商品编辑上下文, 不让回调切换或重置其他商品. */
        configurationContext(option) {
            return {
                epoch: this.pageEpoch,
                product: this.product,
                goodsId: this.product.goodsId,
                selectedGoodsId: this.selectedGoodsId,
                option,
            };
        },
        /** 同一商品同一页面代次才可接收写入结果. */
        isConfigurationCurrent(context) {
            return (
                this.pageActive &&
                context.epoch === this.pageEpoch &&
                context.product === this.product &&
                context.goodsId === this.product.goodsId &&
                context.selectedGoodsId === this.selectedGoodsId &&
                (!context.option || context.option === this.option)
            );
        },
        /** 写操作串行化, 失败保留编辑内容, 不自动重放请求. */
        async writeConfiguration(url, payload, context, message, afterSave, params) {
            if (this.writing || !this.isConfigurationCurrent(context)) {
                return;
            }
            this.writing = true;
            try {
                const {data} = await this.$http({
                    url,
                    method: 'post',
                    data: payload,
                    params,
                    showError: false,
                });
                if (!this.isConfigurationCurrent(context)) {
                    return;
                }
                if (!data || data.code !== 0) {
                    throw new Error(data?.msg || '配置操作失败');
                }
                this.$message.success(typeof message === 'function' ? message(data) : message);
                afterSave();
            } catch (error) {
                if (this.isConfigurationCurrent(context)) {
                    this.$message.error(error.message || '配置操作失败, 请重试');
                }
            } finally {
                this.writing = false;
            }
        },
        /** 按读取类型隔离乱序响应, 配件列表还须核对商品归属. */
        async readConfiguration(name, field, params) {
            this._readVersions = this._readVersions || {};
            const version = (this._readVersions[name] = (this._readVersions[name] || 0) + 1);
            const epoch = this.pageEpoch;
            const goodsId = this.selectedGoodsId;
            const current = () =>
                this.pageActive &&
                epoch === this.pageEpoch &&
                version === this._readVersions[name] &&
                (name !== 'options' || goodsId === this.selectedGoodsId);
            this.reading[name] = true;
            this.readErrors[name] = '';
            try {
                const {data} = await this.$http({
                    url: `/custom/configuration/${name}`,
                    method: 'get',
                    params: {
                        ...params,
                        page: this.pagination[name].page,
                        size: this.pagination[name].size,
                    },
                });
                if (!current()) {
                    return;
                }
                if (!data || data.code !== 0 || !Array.isArray(data.data?.records)) {
                    throw new Error('配置读取失败');
                }
                this[field] = data.data.records;
                this.pagination[name].total = data.data.total;
            } catch (error) {
                if (current()) {
                    this[field] = [];
                    this.readErrors[name] = '配置读取失败, 请重新加载';
                }
            } finally {
                if (current()) {
                    this.reading[name] = false;
                }
            }
        },
        /** 只重试失败的读取, 保留尚未提交的表单. */
        retryReads() {
            if (this.readErrors.goods) {
                this.loadGoods();
            }
            if (this.readErrors.products) {
                this.loadProducts();
            }
            if (this.readErrors.options) {
                this.loadOptions();
            }
        },
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
            if (
                !/^\d+(\.\d{1,3})?$/.test(text) ||
                !Number.isFinite(size) ||
                size <= 0 ||
                size > 3000
            ) {
                throw new Error('尺寸须为0.001到3000mm, 最多三位小数, 未知请留空');
            }
            return size;
        },
        // 加载商城商品 / Loads mall products.
        loadGoods() {
            return this.readConfiguration('goods', 'goods', {keyword: this.goodsKeyword});
        },
        // 加载定制商品配置 / Loads custom product configurations.
        loadProducts() {
            return this.readConfiguration('products', 'products');
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
            if (!this.product.goodsId || this.readErrors.goods || this.readErrors.products) {
                return this.$message.error('请选择实际商品, 读取失败时请先重新加载');
            }
            const payload = {...this.product};
            return this.writeConfiguration(
                '/custom/configuration/product/save',
                payload,
                this.configurationContext(),
                '商品配置已保存',
                () => {
                    this.selectedGoodsId = payload.goodsId;
                    this.loadProducts();
                    this.loadOptions();
                }
            );
        },
        // 重置商品编辑表单 / Resets the product editor.
        resetProduct() {
            this.product = createProduct();
            this.selectedGoodsId = null;
            this.options = [];
            this.reading.options = false;
            this.readErrors.options = '';
            this.resetOption();
        },
        // 选择待配置商品 / Selects a product for component configuration.
        selectProduct(row) {
            this.options = [];
            this.pagination.options.page = 1;
            this.optionKeyword = '';
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
            return this.readConfiguration('options', 'options', {
                goodsId: this.selectedGoodsId,
                keyword: this.optionKeyword,
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
                lengthRole: null,
                shape: '近正圆',
                luster: '极光',
                blemish: '微瑕',
            };
        },
        // 编辑配件 / Edits a component option.
        editOption(row) {
            if (!this.componentTypes.some((type) => type.value === row.componentType)) {
                this.$message.error('旧分类仅可停用, 请按四类材料新建配置');
                return;
            }
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
                    lengthRole: rules.lengthRole || null,
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
            if (!this.selectedGoodsId || this.reading.options || this.readErrors.options) {
                return;
            }
            let payload;
            try {
                const rules = Object.assign({}, JSON.parse(this.option.materialParams || '{}'), {
                    stockQuantity: this.ruleForm.quantity,
                    stockUnit: this.ruleForm.unit,
                    diameterMm: this.measuredSize(this.ruleForm.diameterMm),
                    spanMm: this.measuredSize(this.ruleForm.spanMm),
                    lengthMm: this.measuredSize(this.ruleForm.lengthMm),
                    lengthRole: this.ruleForm.lengthRole || null,
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
            return this.writeConfiguration(
                '/custom/configuration/option/save',
                payload,
                this.configurationContext(this.option),
                '配件已保存',
                () => {
                    this.resetOption();
                    this.loadOptions();
                }
            );
        },
        /** 停用配件并保留历史库存. */
        disableOption(row) {
            if (this.writing) {
                return;
            }
            const context = this.configurationContext();
            return this.$confirm(
                `确定停用“${row.name}”吗？材料将停用, 历史订单和库存会保留。`,
                '停用配件',
                {
                    type: 'warning',
                }
            )
                .then(() =>
                    this.writeConfiguration(
                        '/custom/configuration/option/disable',
                        undefined,
                        context,
                        '配件已停用',
                        () => {
                            if (this.option.id === row.id) {
                                this.resetOption();
                            }
                            this.loadOptions();
                        },
                        {id: row.id}
                    )
                )
                .catch((error) => {
                    if (
                        this.isConfigurationCurrent(context) &&
                        error !== 'cancel' &&
                        error !== 'close'
                    ) {
                        this.$message.error(`停用失败: ${error.message || error}`);
                    }
                });
        },
        // 导入完整定制商品配件配置 / Imports a complete customization configuration.
        importConfiguration(file) {
            if (this.writing) {
                return;
            }
            const context = this.configurationContext();
            const reader = new FileReader();
            reader.onload = (event) => {
                if (!this.isConfigurationCurrent(context)) {
                    return;
                }
                try {
                    const payload = JSON.parse(event.target.result);
                    if (
                        !payload.product ||
                        !payload.product.goodsId ||
                        !Array.isArray(payload.options)
                    ) {
                        throw new Error('JSON 必须包含 product.goodsId 和 options 数组');
                    }
                    this.$confirm(
                        payload.replaceOptions === false
                            ? '导入会更新模板及同代码材料, 保留其他材料和已有库存; 候选须核对后单独启用。是否继续？'
                            : '导入将停用未出现的旧配件并更新同代码材料, 保留已有库存。是否继续？',
                        '导入确认',
                        {
                            type: 'warning',
                        }
                    )
                        .then(() =>
                            this.writeConfiguration(
                                '/custom/configuration/import',
                                payload,
                                context,
                                (data) => `导入成功，共 ${data.data.optionCount} 个配件`,
                                () => {
                                    this.selectedGoodsId = payload.product.goodsId;
                                    this.product = Object.assign(createProduct(), payload.product);
                                    this.resetOption();
                                    this.options = [];
                                    this.loadProducts();
                                    this.loadOptions();
                                }
                            )
                        )
                        .catch((error) => {
                            if (
                                this.isConfigurationCurrent(context) &&
                                error !== 'cancel' &&
                                error !== 'close'
                            ) {
                                this.$message.error(`导入失败：${error.message || error}`);
                            }
                        });
                } catch (error) {
                    this.$message.error(`JSON 解析失败：${error.message}`);
                }
            };
            reader.onerror = () => {
                if (this.isConfigurationCurrent(context)) {
                    this.$message.error('无法读取所选 JSON 文件');
                }
            };
            reader.readAsText(file.raw, 'UTF-8');
        },
        /** 下载单一客户目录的完整停用模板, 商品切换或离页后不触发旧下载. */
        async downloadTemplate() {
            if (!this.selectedGoodsId || this.catalogDownloading) {
                return;
            }
            const context = this.configurationContext();
            this.catalogDownloading = true;
            try {
                const {data} = await this.$http({
                    url: '/custom/configuration/catalog-template',
                    method: 'get',
                    params: {goodsId: this.selectedGoodsId},
                });
                if (!this.isConfigurationCurrent(context)) {
                    return;
                }
                if (!data || data.code !== 0 || !Array.isArray(data.data?.options)) {
                    throw new Error('客户候选目录读取失败');
                }
                const blob = new Blob([JSON.stringify(data.data, null, 4)], {
                    type: 'application/json;charset=utf-8',
                });
                const link = document.createElement('a');
                const url = URL.createObjectURL(blob);
                try {
                    link.href = url;
                    link.download = '客户材料候选导入模板.json';
                    link.click();
                } finally {
                    URL.revokeObjectURL(url);
                }
            } catch (error) {
                if (this.isConfigurationCurrent(context)) {
                    this.$message.error(error.message || '客户候选目录读取失败, 请重试');
                }
            } finally {
                this.catalogDownloading = false;
            }
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
