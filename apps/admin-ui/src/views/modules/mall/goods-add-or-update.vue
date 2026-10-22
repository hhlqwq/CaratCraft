<template>
    <el-dialog
        :title="!form.goods.id ? '新增商品' : !disabled ? '编辑商品' : '查看商品'"
        :close-on-click-modal="false"
        :lock-scroll="false"
        v-model="visible"
        width="90%"
    >
        <el-alert
            v-if="loadError || optionsError"
            :title="loadError || optionsError"
            type="error"
            :closable="false"
        >
            <el-button :disabled="loading || optionsLoading" @click="retryLoad">重新加载</el-button>
        </el-alert>
        <el-tabs
            v-loading="loading"
            :inert="saving || loading"
            v-model="activeTab"
            type="border-card"
        >
            <el-tab-pane label="基础信息" name="base">
                <el-form :model="form.goods" :rules="dataRule" ref="dataForm" label-width="120px">
                    <el-row :gutter="20">
                        <el-col :xs="24" :sm="12" :lg="8">
                            <el-form-item label="商品名称" prop="name">
                                <el-input
                                    v-model="form.goods.name"
                                    :disabled="disabled"
                                    placeholder="商品名称"
                                ></el-input>
                            </el-form-item>
                        </el-col>
                        <el-col :xs="24" :sm="12" :lg="8">
                            <el-form-item label="商品分类" prop="categoryId">
                                <el-select
                                    v-model="form.goods.categoryId"
                                    :disabled="disabled"
                                    filterable
                                    placeholder="请选择"
                                >
                                    <el-option
                                        v-for="item in categoryOptions"
                                        :key="item.id"
                                        :label="item.name"
                                        :value="item.id"
                                    ></el-option>
                                </el-select>
                            </el-form-item>
                        </el-col>
                        <el-col :xs="24" :sm="12" :lg="8">
                            <el-form-item label="品牌" prop="brandId">
                                <el-select
                                    v-model="form.goods.brandId"
                                    :disabled="disabled"
                                    filterable
                                    clearable
                                    placeholder="请选择"
                                >
                                    <el-option
                                        v-for="item in brandOptions"
                                        :key="item.id"
                                        :label="item.name"
                                        :value="item.id"
                                    ></el-option>
                                </el-select>
                            </el-form-item>
                        </el-col>
                    </el-row>
                    <el-row :gutter="20">
                        <el-col :xs="24" :sm="12" :lg="8">
                            <el-form-item label="商品编码" prop="goodsSn">
                                <el-input
                                    v-model="form.goods.goodsSn"
                                    :disabled="disabled"
                                    placeholder="商品编码"
                                ></el-input>
                            </el-form-item>
                        </el-col>
                        <el-col :xs="24" :sm="12" :lg="8">
                            <el-form-item label="库存" prop="goodsNumber">
                                <el-input-number
                                    v-model="form.goods.goodsNumber"
                                    :disabled="disabled"
                                    :min="0"
                                    controls-position="right"
                                ></el-input-number>
                            </el-form-item>
                        </el-col>
                        <el-col :xs="24" :sm="12" :lg="8">
                            <el-form-item label="零售价格" prop="retailPrice">
                                <el-input-number
                                    v-model="form.goods.retailPrice"
                                    :disabled="disabled"
                                    :min="0"
                                    :precision="2"
                                    controls-position="right"
                                ></el-input-number>
                            </el-form-item>
                        </el-col>
                    </el-row>
                    <el-row :gutter="20">
                        <el-col :xs="24" :sm="12" :lg="8">
                            <el-form-item label="市场价" prop="marketPrice">
                                <el-input-number
                                    v-model="form.goods.marketPrice"
                                    :disabled="disabled"
                                    :min="0"
                                    :precision="2"
                                    controls-position="right"
                                ></el-input-number>
                            </el-form-item>
                        </el-col>
                        <el-col :xs="24" :sm="12" :lg="8">
                            <el-form-item label="上架状态" prop="isOnSale">
                                <el-radio-group v-model="form.goods.isOnSale" :disabled="disabled">
                                    <el-radio-button :label="1">上架</el-radio-button>
                                    <el-radio-button :label="0">下架</el-radio-button>
                                </el-radio-group>
                            </el-form-item>
                        </el-col>
                        <el-col :xs="24" :sm="12" :lg="8">
                            <el-form-item label="新品" prop="isNew">
                                <el-radio-group v-model="form.goods.isNew" :disabled="disabled">
                                    <el-radio-button :label="1">是</el-radio-button>
                                    <el-radio-button :label="0">否</el-radio-button>
                                </el-radio-group>
                            </el-form-item>
                        </el-col>
                    </el-row>
                    <el-row :gutter="20">
                        <el-col :span="12">
                            <el-form-item label="主图" prop="primaryPicUrl">
                                <el-img
                                    v-model="form.goods.primaryPicUrl"
                                    :disabled="disabled"
                                ></el-img>
                            </el-form-item>
                        </el-col>
                        <el-col :span="12">
                            <el-form-item label="列表图" prop="listPicUrl">
                                <el-img
                                    v-model="form.goods.listPicUrl"
                                    :disabled="disabled"
                                ></el-img>
                            </el-form-item>
                        </el-col>
                    </el-row>
                    <el-form-item label="商品简介" prop="goodsBrief">
                        <el-input
                            v-model="form.goods.goodsBrief"
                            :disabled="disabled"
                            placeholder="商品简介"
                        ></el-input>
                    </el-form-item>
                    <el-form-item label="商品详情" prop="goodsDesc">
                        <rich-text-editor
                            v-model="form.goods.goodsDesc"
                            :disabled="disabled"
                            placeholder="商品详情"
                        ></rich-text-editor>
                        <el-button type="success" @click="openDetail">预览详情</el-button>
                    </el-form-item>
                </el-form>
            </el-tab-pane>

            <el-tab-pane label="珍珠参数" name="pearl">
                <el-alert
                    title="仅填写商家确认的商品事实. 来源和品类将决定商城筛选结果, 未填写的参数不会对消费者展示."
                    type="info"
                    :closable="false"
                />
                <el-form label-width="110px" class="mt10">
                    <el-row :gutter="20">
                        <el-col :xs="24" :sm="12"
                            ><el-form-item label="珍珠来源"
                                ><el-select
                                    v-model="form.goods.pearlSource"
                                    :disabled="disabled"
                                    clearable
                                    placeholder="请选择来源"
                                    ><el-option label="淡水珍珠" value="FRESHWATER" /><el-option
                                        label="海水珍珠"
                                        value="SEAWATER" /></el-select></el-form-item
                        ></el-col>
                        <el-col :xs="24" :sm="12"
                            ><el-form-item label="成品品类"
                                ><el-select
                                    v-model="form.goods.jewelryType"
                                    :disabled="disabled"
                                    clearable
                                    placeholder="请选择品类"
                                    ><el-option
                                        v-for="item in jewelryTypes"
                                        :key="item.id"
                                        :label="item.name"
                                        :value="item.id" /></el-select></el-form-item
                        ></el-col>
                        <el-col :xs="24" :sm="12"
                            ><el-form-item label="珍珠品种"
                                ><el-input
                                    v-model="form.goods.pearlVariety"
                                    :disabled="disabled"
                                    maxlength="60"
                                    placeholder="填写实际品种, 未知可留空" /></el-form-item
                        ></el-col>
                        <el-col :xs="24" :sm="12"
                            ><el-form-item label="产地"
                                ><el-input
                                    v-model="form.goods.pearlOrigin"
                                    :disabled="disabled"
                                    maxlength="100"
                                    placeholder="填写已确认产地, 未知可留空" /></el-form-item
                        ></el-col>
                        <el-col :xs="24" :sm="12"
                            ><el-form-item label="直径下限(mm)"
                                ><el-input
                                    v-model="form.goods.pearlDiameterMin"
                                    :disabled="disabled"
                                    placeholder="最多三位小数, 未知留空" /></el-form-item
                        ></el-col>
                        <el-col :xs="24" :sm="12"
                            ><el-form-item label="直径上限(mm)"
                                ><el-input
                                    v-model="form.goods.pearlDiameterMax"
                                    :disabled="disabled"
                                    placeholder="与下限同时填写或同时留空" /></el-form-item
                        ></el-col>
                        <el-col v-for="item in pearlTextFields" :key="item.key" :xs="24" :sm="12"
                            ><el-form-item :label="item.name"
                                ><el-input
                                    v-model="form.goods[item.key]"
                                    :disabled="disabled"
                                    maxlength="30"
                                    placeholder="以实际商品为准" /></el-form-item
                        ></el-col>
                    </el-row>
                </el-form>
            </el-tab-pane>

            <el-tab-pane label="商品视频" name="video">
                <el-alert
                    title="支持30MB以内、60秒以内、1080p以内的H.264 MP4, 可带单声道或双声道AAC-LC音轨. 上传后仍需保存商品. 不支持HEVC和分片MP4, 请先导出兼容格式."
                    type="info"
                    :closable="false"
                />
                <div class="mt10">
                    <input
                        v-if="!disabled"
                        type="file"
                        accept="video/mp4,.mp4"
                        :disabled="videoUploading"
                        @change="uploadVideo"
                        aria-label="选择商品MP4视频"
                    />
                </div>
                <el-progress v-if="videoUploading" class="mt10" :percentage="videoProgress" />
                <p v-if="videoUploading">
                    {{
                        videoProgress < 100
                            ? '正在上传视频...'
                            : '上传完成, 正在校验视频并写入存储...'
                    }}
                </p>
                <video
                    v-if="form.goods.videoUrl"
                    class="product-video"
                    :src="form.goods.videoUrl"
                    controls
                    preload="metadata"
                />
                <p v-else>尚未设置商品视频</p>
                <el-button
                    v-if="!disabled && form.goods.videoUrl"
                    :disabled="videoUploading"
                    @click="removeVideo"
                    >移除商品视频</el-button
                >
            </el-tab-pane>

            <el-tab-pane label="商品参数" name="attr">
                <div class="mb10">
                    <el-button v-if="!disabled" type="primary" size="small" @click="addAttribute"
                        >新增参数</el-button
                    >
                </div>
                <el-table :data="form.attributes" border>
                    <el-table-column label="属性" width="240">
                        <template #default="scope">
                            <el-select
                                v-model="scope.row.attributeId"
                                :disabled="disabled"
                                filterable
                                placeholder="请选择属性"
                                @change="onAttributeChange(scope.row)"
                            >
                                <el-option
                                    v-for="item in attributeOptions"
                                    :key="item.id"
                                    :label="item.name"
                                    :value="item.id"
                                ></el-option>
                            </el-select>
                        </template>
                    </el-table-column>
                    <el-table-column label="属性值" min-width="280">
                        <template #default="scope">
                            <el-input
                                v-model="scope.row.value"
                                :disabled="disabled"
                                placeholder="请输入属性值"
                            ></el-input>
                        </template>
                    </el-table-column>
                    <el-table-column v-if="!disabled" label="操作" width="100" align="center">
                        <template #default="scope">
                            <el-button type="text" @click="removeAttribute(scope.$index)"
                                >删除</el-button
                            >
                        </template>
                    </el-table-column>
                </el-table>
            </el-tab-pane>

            <el-tab-pane label="商品相册" name="gallery">
                <div class="mb10">
                    <el-button v-if="!disabled" type="primary" size="small" @click="addGallery"
                        >新增图片</el-button
                    >
                </div>
                <el-table :data="form.gallery" border>
                    <el-table-column label="图片" width="260">
                        <template #default="scope">
                            <el-img v-model="scope.row.imgUrl" :disabled="disabled"></el-img>
                        </template>
                    </el-table-column>
                    <el-table-column label="描述">
                        <template #default="scope">
                            <el-input
                                v-model="scope.row.imgDesc"
                                :disabled="disabled"
                                placeholder="描述"
                            ></el-input>
                        </template>
                    </el-table-column>
                    <el-table-column label="排序" width="240" align="center">
                        <template #default="scope">
                            <el-input-number
                                v-model="scope.row.sortOrder"
                                :disabled="disabled"
                                :min="1"
                                controls-position="right"
                            ></el-input-number>
                        </template>
                    </el-table-column>
                    <el-table-column v-if="!disabled" label="操作" width="120" align="center">
                        <template #default="scope">
                            <el-button type="text" @click="removeGallery(scope.$index)"
                                >删除</el-button
                            >
                        </template>
                    </el-table-column>
                </el-table>
            </el-tab-pane>

            <el-tab-pane label="规格与规格值" name="spec">
                <div class="mb10">
                    <el-button v-if="!disabled" type="primary" size="small" @click="addSpecGroup"
                        >新增规格</el-button
                    >
                    <el-button v-if="!disabled" size="small" @click="rebuildSkuList"
                        >重建SKU</el-button
                    >
                </div>
                <el-table :data="form.specs" border>
                    <el-table-column label="规格名" width="260">
                        <template #default="scope">
                            <el-select
                                v-model="scope.row.specificationId"
                                :disabled="disabled"
                                filterable
                                placeholder="请选择规格"
                                @change="onSpecificationChange(scope.row)"
                            >
                                <el-option
                                    v-for="item in specificationOptions"
                                    :key="item.id"
                                    :label="item.name"
                                    :value="item.id"
                                ></el-option>
                            </el-select>
                        </template>
                    </el-table-column>
                    <el-table-column label="规格值">
                        <template #default="scope">
                            <div
                                v-for="(value, valueIndex) in scope.row.values"
                                :key="valueIndex"
                                class="spec-value-row"
                            >
                                <el-input
                                    v-model="value.value"
                                    :disabled="disabled"
                                    placeholder="规格值"
                                    class="spec-value-input"
                                ></el-input>
                                <el-img v-model="value.picUrl" :disabled="disabled"></el-img>
                                <el-button
                                    v-if="!disabled"
                                    type="text"
                                    @click="removeSpecValue(scope.row, valueIndex)"
                                    >删除</el-button
                                >
                            </div>
                            <el-button v-if="!disabled" type="text" @click="addSpecValue(scope.row)"
                                >+ 添加规格值</el-button
                            >
                        </template>
                    </el-table-column>
                    <el-table-column v-if="!disabled" label="操作" width="100" align="center">
                        <template #default="scope">
                            <el-button type="text" @click="removeSpecGroup(scope.$index)"
                                >删除</el-button
                            >
                        </template>
                    </el-table-column>
                </el-table>
            </el-tab-pane>

            <el-tab-pane label="SKU 列表" name="sku">
                <el-alert
                    type="info"
                    :closable="false"
                    show-icon
                    title="规格变更后请点击“重建SKU”，系统会按规格组合生成 SKU。"
                ></el-alert>
                <el-table :data="form.skuList" border class="mt10">
                    <el-table-column label="规格组合" min-width="260">
                        <template #default="scope">
                            <el-tag size="small">{{ scope.row.specLabel || '默认规格' }}</el-tag>
                        </template>
                    </el-table-column>
                    <el-table-column label="SKU编码" width="180">
                        <template #default="scope">
                            <el-input
                                v-model="scope.row.goodsSn"
                                :disabled="disabled"
                                placeholder="SKU编码"
                            ></el-input>
                        </template>
                    </el-table-column>
                    <el-table-column label="库存" width="240" align="center">
                        <template #default="scope">
                            <el-input-number
                                v-model="scope.row.goodsNumber"
                                :disabled="disabled"
                                :min="0"
                                controls-position="right"
                            ></el-input-number>
                        </template>
                    </el-table-column>
                    <el-table-column label="零售价" width="240" align="center">
                        <template #default="scope">
                            <el-input-number
                                v-model="scope.row.retailPrice"
                                :disabled="disabled"
                                :min="0"
                                :precision="2"
                                controls-position="right"
                            ></el-input-number>
                        </template>
                    </el-table-column>
                    <el-table-column label="市场价" width="240" align="center">
                        <template #default="scope">
                            <el-input-number
                                v-model="scope.row.marketPrice"
                                :disabled="disabled"
                                :min="0"
                                :precision="2"
                                controls-position="right"
                            ></el-input-number>
                        </template>
                    </el-table-column>
                </el-table>
            </el-tab-pane>
        </el-tabs>

        <template #footer
            ><span class="dialog-footer">
                <el-button @click="visible = false">取消</el-button>
                <el-button
                    v-if="!disabled"
                    type="primary"
                    :disabled="
                        videoUploading || loading || optionsLoading || !!loadError || !!optionsError
                    "
                    :loading="saving"
                    @click="dataFormSubmit"
                    >保存</el-button
                >
            </span></template
        >
    </el-dialog>
</template>

<script>
import {h} from 'vue';
import DOMPurify from '@/third-party/dompurify/purify.es.mjs';

export default {
    data() {
        return {
            disabled: false,
            loading: false,
            optionsLoading: false,
            loadError: '',
            optionsError: '',
            saving: false,
            videoUploading: false,
            videoProgress: 0,
            visible: false,
            activeTab: 'base',
            form: this.getDefaultForm(),
            jewelryTypes: [
                {id: 'NECKLACE', name: '项链'},
                {id: 'EARRINGS', name: '耳饰'},
                {id: 'BRACELET', name: '手链'},
                {id: 'RING', name: '戒指'},
            ],
            pearlTextFields: [
                {key: 'pearlShape', name: '形状'},
                {key: 'pearlLuster', name: '光泽'},
                {key: 'pearlBlemish', name: '瑕疵说明'},
            ],
            brandOptions: [],
            categoryOptions: [],
            attributeOptions: [],
            specificationOptions: [],
            dataRule: {
                name: [{required: true, message: '商品名称不能为空', trigger: 'blur'}],
                categoryId: [{required: true, message: '商品分类不能为空', trigger: 'change'}],
                retailPrice: [{required: true, message: '零售价格不能为空', trigger: 'blur'}],
            },
        };
    },
    /** 卸载时使所有读取及提交回调失效. */
    beforeUnmount() {
        this._editorVersion = (this._editorVersion || 0) + 1;
    },
    methods: {
        /** 为每次打开创建独立表单, 保留空值语义. */
        getDefaultForm() {
            return {
                goods: {
                    id: '',
                    categoryId: '',
                    pearlSource: '',
                    jewelryType: '',
                    pearlVariety: '',
                    pearlOrigin: '',
                    pearlShape: '',
                    pearlLuster: '',
                    pearlBlemish: '',
                    pearlDiameterMin: null,
                    pearlDiameterMax: null,
                    videoMediaId: 0,
                    videoUrl: '',
                    goodsSn: '',
                    name: '',
                    brandId: '',
                    goodsNumber: 0,
                    keywords: '',
                    goodsBrief: '',
                    goodsDesc: '',
                    isOnSale: 1,
                    sortOrder: 100,
                    isDelete: 0,
                    counterPrice: 0,
                    extraPrice: 0,
                    isNew: 0,
                    goodsUnit: '件',
                    primaryPicUrl: '',
                    listPicUrl: '',
                    retailPrice: 0,
                    sellVolume: 0,
                    unitPrice: 0,
                    appExclusivePrice: 0,
                    isAppExclusive: 0,
                    isLimited: 0,
                    isHot: 0,
                    marketPrice: 0,
                },
                attributes: [],
                gallery: [],
                specs: [],
                skuList: [],
            };
        },
        /** 每次打开隔离旧商品的读取和保存结果. */
        init(id, disabled) {
            const version = (this._editorVersion = (this._editorVersion || 0) + 1);
            this.loading = !!id;
            this.loadError = '';
            this.optionsError = '';
            this.saving = false;
            this.disabled = !!disabled;
            this.visible = true;
            this.activeTab = 'base';
            this.form = this.getDefaultForm();
            this.videoUploading = false;
            this.videoProgress = 0;
            this.form.goods.id = id || '';

            this.$nextTick(() => {
                if (!this.isEditorCurrent(version)) {
                    return;
                }
                this.$refs.dataForm && this.$refs.dataForm.resetFields();
                this.loadOptions(version);
                if (id) {
                    this.loadAggregate(id, version);
                }
            });
        },
        /** 使用Vue3公开渲染接口预览清洗后的商品详情. */
        openDetail() {
            const content = h('div', {
                style: {maxHeight: '65vh', overflow: 'auto', overflowWrap: 'anywhere'},
                innerHTML: DOMPurify.sanitize(this.form.goods.goodsDesc || ''),
            });
            this.$alert(content, this.form.goods.name, {closeOnClickModal: true});
        },
        /** 仅当前可见编辑会话接收异步结果. */
        isEditorCurrent(version) {
            return this.visible && version === this._editorVersion;
        },
        /** 读取失败可原地重试, 选项失败不清空已填写的商品正文. */
        retryLoad() {
            if (this.loading || this.optionsLoading || this.saving) {
                return;
            }
            if (this.loadError) {
                this.loadAggregate(this.form.goods.id);
            }
            if (this.optionsError) {
                this.loadOptions();
            }
        },
        /** 完整读取商品选项, 失败时阻止在缺少选项的情况下保存. */
        async loadOptions(version = this._editorVersion) {
            this.optionsLoading = true;
            this.optionsError = '';
            const options = [
                ['category', 'categoryOptions'],
                ['brand', 'brandOptions'],
                ['attribute', 'attributeOptions'],
                ['specification', 'specificationOptions'],
            ];
            try {
                const responses = await Promise.all(
                    options.map(([name]) =>
                        this.$http({url: `/mall/${name}/queryAll`, method: 'get'})
                    )
                );
                if (!this.isEditorCurrent(version)) {
                    return;
                }
                if (responses.some(({data}) => !data || data.code !== 0)) {
                    throw new Error('商品选项加载失败');
                }
                options.forEach(([, field], index) => {
                    this[field] = responses[index].data.data || [];
                });
            } catch (error) {
                if (this.isEditorCurrent(version)) {
                    this.optionsError = '商品选项加载失败, 请重新加载后再保存';
                }
            } finally {
                if (this.isEditorCurrent(version)) {
                    this.optionsLoading = false;
                }
            }
        },
        /** 读取完整聚合, 失败时禁止用默认空白表单覆盖已有商品. */
        async loadAggregate(id, version = this._editorVersion) {
            this.loading = true;
            this.loadError = '';
            try {
                const {data} = await this.$http({
                    url: `/mall/goods/aggregate/${id}`,
                    method: 'get',
                });
                if (!this.isEditorCurrent(version)) {
                    return;
                }
                if (
                    data &&
                    data.code === 0 &&
                    data.data &&
                    String(data.data.goods?.id) === String(id)
                ) {
                    this.form = Object.assign(this.getDefaultForm(), data.data);
                    this.form.goods = Object.assign(
                        this.getDefaultForm().goods,
                        data.data.goods || {}
                    );
                    this.form.attributes = data.data.attributes || [];
                    this.form.gallery = (data.data.gallery || []).map((item, index) =>
                        Object.assign({sortOrder: index + 1}, item)
                    );
                    this.form.specs = data.data.specs || [];
                    this.form.skuList = (data.data.skuList || []).map((item) => ({
                        ...item,
                        specLabel: item.goodsSpecificationIds,
                    }));
                } else {
                    throw new Error('商品详情加载失败');
                }
            } catch (error) {
                if (this.isEditorCurrent(version)) {
                    this.loadError = '商品详情加载失败, 请重新加载后再保存';
                }
            } finally {
                if (this.isEditorCurrent(version)) {
                    this.loading = false;
                }
            }
        },
        addAttribute() {
            this.form.attributes.push({attributeId: '', attributeName: '', value: ''});
        },
        removeAttribute(index) {
            this.form.attributes.splice(index, 1);
        },
        onAttributeChange(row) {
            const hit = this.attributeOptions.find((item) => item.id === row.attributeId);
            row.attributeName = hit ? hit.name : '';
        },
        addGallery() {
            this.form.gallery.push({
                imgUrl: '',
                imgDesc: '',
                sortOrder: this.form.gallery.length + 1,
            });
        },
        removeGallery(index) {
            this.form.gallery.splice(index, 1);
        },
        addSpecGroup() {
            this.form.specs.push({
                specificationId: '',
                specificationName: '',
                values: [{value: '', picUrl: ''}],
            });
        },
        removeSpecGroup(index) {
            this.form.specs.splice(index, 1);
            this.rebuildSkuList();
        },
        onSpecificationChange(row) {
            const hit = this.specificationOptions.find((item) => item.id === row.specificationId);
            row.specificationName = hit ? hit.name : '';
            this.rebuildSkuList();
        },
        addSpecValue(row) {
            row.values.push({value: '', picUrl: ''});
        },
        removeSpecValue(row, valueIndex) {
            row.values.splice(valueIndex, 1);
            this.rebuildSkuList();
        },
        getValidSpecs() {
            return this.form.specs
                .map((spec) => ({
                    specificationId: spec.specificationId,
                    specificationName: spec.specificationName,
                    values: (spec.values || []).filter((value) => value.value),
                }))
                .filter((spec) => spec.specificationId && spec.values.length > 0)
                .sort((left, right) => left.specificationId - right.specificationId);
        },
        /** 先限制组合数量再展开, 避免编辑页创建无限笛卡尔积. */
        buildSpecCombinations(specs) {
            let total = 1;
            for (const spec of specs) {
                if (total > 512 / spec.values.length) {
                    this.$message.error('SKU组合数量不能超过512, 请减少规格值');
                    return [];
                }
                total *= spec.values.length;
            }
            if (!specs.length) {
                return [{key: '', label: '默认规格'}];
            }
            let combos = [{key: '', label: ''}];
            specs.forEach((spec) => {
                const next = [];
                combos.forEach((combo) => {
                    spec.values.forEach((value) => {
                        const keyPart = `${spec.specificationId}:${value.value}`;
                        const labelPart = `${spec.specificationName || spec.specificationId}:${value.value}`;
                        next.push({
                            key: combo.key ? `${combo.key}|${keyPart}` : keyPart,
                            label: combo.label ? `${combo.label} / ${labelPart}` : labelPart,
                        });
                    });
                });
                combos = next;
            });
            return combos;
        },
        rebuildSkuList() {
            const specs = this.getValidSpecs();
            const combinations = this.buildSpecCombinations(specs);
            const cache = (this.form.skuList || []).reduce((map, item) => {
                map[item.goodsSpecificationIds] = item;
                return map;
            }, {});
            this.form.skuList = combinations.map((combo) => {
                const hit = cache[combo.key] || {};
                return {
                    goodsSpecificationIds: combo.key,
                    specLabel: combo.label,
                    goodsSn: hit.goodsSn || this.form.goods.goodsSn || '',
                    goodsNumber:
                        hit.goodsNumber != null
                            ? hit.goodsNumber
                            : this.form.goods.goodsNumber || 0,
                    retailPrice:
                        hit.retailPrice != null
                            ? hit.retailPrice
                            : this.form.goods.retailPrice || 0,
                    marketPrice:
                        hit.marketPrice != null
                            ? hit.marketPrice
                            : this.form.goods.marketPrice || 0,
                };
            });
        },
        /** 上传结果只写入发起请求时的商品表单, 切换商品后忽略迟到响应. */
        async uploadVideo(event) {
            const file = event.target.files && event.target.files[0];
            event.target.value = '';
            if (!file || this.videoUploading || this.disabled) {
                return;
            }
            if (!/\.mp4$/i.test(file.name) || file.size > 30 * 1024 * 1024) {
                this.$message.error('请选择30MB以内的MP4视频');
                return;
            }
            const form = this.form;
            this.videoUploading = true;
            this.videoProgress = 0;
            try {
                const body = new FormData();
                body.append('file', file);
                const {data} = await this.$http({
                    url: '/mall/product-video/upload',
                    method: 'post',
                    data: body,
                    headers: {'Content-Type': 'multipart/form-data'},
                    timeout: 120000,
                    showError: false,
                    loading: false,
                    onUploadProgress: (progress) => {
                        if (this.form === form && progress.total) {
                            this.videoProgress = Math.min(
                                100,
                                Math.round((progress.loaded * 100) / progress.total)
                            );
                        }
                    },
                });
                if (this.form !== form) {
                    return;
                }
                if (!data || data.code !== 0) {
                    throw new Error((data && data.msg) || '视频上传失败');
                }
                form.goods.videoMediaId = data.data.id;
                form.goods.videoUrl = data.data.url;
                this.$message.success('视频上传成功, 保存商品后生效');
            } catch (error) {
                if (this.form === form) {
                    this.$message.error(error.message || '视频上传失败, 请重试');
                }
            } finally {
                if (this.form === form) {
                    this.videoUploading = false;
                }
            }
        },
        /** 仅清除商品对媒体的引用, 不删除可能被其他商品引用的存储文件. */
        removeVideo() {
            this.form.goods.videoMediaId = 0;
            this.form.goods.videoUrl = '';
        },
        /** 将可空测量文本转换为数值, 不用最小值补造未知尺寸. */
        measuredDiameter(value) {
            const text = value == null ? '' : String(value).trim();
            if (!text) {
                return null;
            }
            if (!/^\d+(?:\.\d{1,3})?$/.test(text) || Number(text) <= 0 || Number(text) > 3000) {
                throw new Error('珍珠直径需为大于0且不超过3000的毫米数值, 最多三位小数');
            }
            return Number(text);
        },
        /** 整理聚合请求并校验专属分类和尺寸区间, 不修改原始编辑表单. */
        sanitizePayload() {
            const payload = JSON.parse(JSON.stringify(this.form));
            const goods = payload.goods;
            const specs = this.getValidSpecs();
            const groups = new Set();
            for (const spec of specs) {
                if (
                    groups.has(spec.specificationId) ||
                    spec.values.some(
                        (item) => item.value.includes('|') || item.value !== item.value.trim()
                    ) ||
                    new Set(spec.values.map((item) => item.value)).size !== spec.values.length
                ) {
                    throw new Error('规格组和值须唯一, 不含首尾空格或分隔符|');
                }
                groups.add(spec.specificationId);
            }
            if (goods.isOnSale === 1 && (!goods.pearlSource || !goods.jewelryType)) {
                throw new Error('上架前请在珍珠参数中填写珍珠来源和成品品类');
            }
            goods.pearlDiameterMin = this.measuredDiameter(goods.pearlDiameterMin);
            goods.pearlDiameterMax = this.measuredDiameter(goods.pearlDiameterMax);
            if (
                (goods.pearlDiameterMin === null) !== (goods.pearlDiameterMax === null) ||
                (goods.pearlDiameterMin !== null && goods.pearlDiameterMax < goods.pearlDiameterMin)
            ) {
                throw new Error('直径上下限需同时填写且上限不小于下限, 或同时留空');
            }
            payload.attributes = (payload.attributes || []).filter(
                (item) => item.attributeId && item.value
            );
            payload.gallery = (payload.gallery || []).filter((item) => item.imgUrl);
            payload.specs = this.getValidSpecs();
            payload.skuList = (payload.skuList || []).map((item) => ({
                goodsSpecificationIds: item.goodsSpecificationIds || '',
                goodsSn: item.goodsSn,
                goodsNumber: item.goodsNumber,
                retailPrice: item.retailPrice,
                marketPrice: item.marketPrice,
            }));
            return payload;
        },
        /** 从校验开始锁定提交, 失败保留表单, 旧会话结果不得关闭新商品. */
        async dataFormSubmit() {
            if (
                this.disabled ||
                this.videoUploading ||
                this.loading ||
                this.optionsLoading ||
                this.loadError ||
                this.optionsError ||
                this.saving
            ) {
                return;
            }
            const version = this._editorVersion;
            this.saving = true;
            try {
                const valid = await this.$refs.dataForm.validate().catch(() => false);
                if (!valid || !this.isEditorCurrent(version)) {
                    return;
                }
                let payload;
                try {
                    payload = this.sanitizePayload();
                } catch (error) {
                    this.activeTab = 'pearl';
                    this.$message.error(error.message);
                    return;
                }
                const hasSpecs = payload.specs && payload.specs.length > 0;
                if (hasSpecs && (!payload.skuList || payload.skuList.length === 0)) {
                    this.$message.error('存在规格时必须生成SKU');
                    this.activeTab = 'sku';
                    return;
                }
                const {data} = await this.$http({
                    url: `/mall/goods/aggregate/${!payload.goods.id ? 'save' : 'update'}`,
                    method: 'post',
                    data: payload,
                });
                if (!this.isEditorCurrent(version)) {
                    return;
                }
                if (!data || data.code !== 0) {
                    throw new Error(data?.msg || '商品保存失败, 请重试');
                }
                this.$message({message: '操作成功', type: 'success', duration: 1500});
                this.visible = false;
                this.$emit('refreshDataList');
            } catch (error) {
                if (this.isEditorCurrent(version)) {
                    this.$message.error(error.message || '商品保存失败, 请重试');
                }
            } finally {
                if (version === this._editorVersion) {
                    this.saving = false;
                }
            }
        },
    },
};
</script>

<style scoped>
.product-video {
    width: 100%;
    max-width: 640px;
    max-height: 60vh;
    margin-top: 20px;
    background: #17231f;
}
.mb10 {
    margin-bottom: 10px;
}

.mt10 {
    margin-top: 10px;
}

.spec-value-row {
    display: flex;
    align-items: center;
    margin-bottom: 8px;
}

.spec-value-input {
    width: 180px;
    margin-right: 8px;
}

.spec-pic-input {
    width: 260px;
    margin-right: 8px;
}
</style>
