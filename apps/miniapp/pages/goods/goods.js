const util = require('../../utils/util.js');
const api = require('../../config/api.js');
const pearlCatalog = require('../../services/pearl-catalog.js');
const sku = require('../../services/goods-sku.js');

Page({
    data: {
        id: 0,
        goodsDetailHtml: '',
        goods: {},
        goodsError: '',
        relatedError: '',
        videoError: false,
        loading: true,
        submitting: false,
        collecting: false,
        gallery: [],
        attribute: [],
        issueList: [],
        comment: {},
        brand: {},
        specificationList: [],
        productList: [],
        relatedGoods: [],
        selectedProduct: null,
        selectedStock: 0,
        cartGoodsCount: 0,
        userHasCollect: 0,
        number: 1,
        checkedSpecText: '请选择规格数量',
        openAttr: false,
        isCustom: false,
        noCollectImage: '/static/images/icon_collect.png',
        hasCollectImage: '/static/images/icon_collect_checked.png',
        collectBackImage: '/static/images/icon_collect.png',
    },
    /** 捕获当前页面及账号, 异步完成时只允许原页面和原账号接收结果. */
    requestContext() {
        return {epoch: this._epoch, token: wx.getStorageSync('token') || ''};
    },
    /** 页面卸载或账号切换后的成功和失败响应均不能修改当前界面. */
    isCurrent(context) {
        return (
            !this._unloaded &&
            context.epoch === this._epoch &&
            context.token === (wx.getStorageSync('token') || '')
        );
    },
    /** 初始化合法商品编号, 不向接口发送NaN或不完整编号. */
    onLoad(options) {
        this._epoch = 1;
        this._unloaded = false;
        this._token = wx.getStorageSync('token') || '';
        const id = Number(options.id);
        if (!Number.isSafeInteger(id) || id < 1) {
            this.setData({goodsError: '商品编号无效', loading: false});
            return;
        }
        this.setData({id});
        this.refreshPage();
    },
    /** 返回时更新购物车数量, 账号变化则清除个人状态并重新读取详情. */
    onShow() {
        const token = wx.getStorageSync('token') || '';
        if (this._token !== token) {
            this._token = token;
            this._epoch++;
            this.setData({
                cartGoodsCount: 0,
                userHasCollect: 0,
                collecting: false,
                submitting: false,
                openAttr: false,
                collectBackImage: this.data.noCollectImage,
            });
            this.refreshPage();
        } else if (this._shown) {
            this.loadCartCount();
        }
        this._shown = true;
    },
    /** 使所有尚未完成的读取和提交失效, 不取消服务端已接收的操作. */
    onUnload() {
        this._unloaded = true;
        this._epoch++;
    },
    /** 详情与可选辅助数据分别收尾, 辅助失败不遮挡商品正文. */
    refreshPage() {
        if (!this.data.id) {
            return;
        }
        this.getGoodsInfo();
        this.loadCustomization();
        this.loadCartCount();
    },
    /** 读取详情及规格, 重试产生新代次以防旧响应覆盖最新结果. */
    async getGoodsInfo() {
        if (!this.data.id) {
            return;
        }
        const context = this.requestContext();
        const version = (this._detailVersion = (this._detailVersion || 0) + 1);
        this.setData({loading: true, goodsError: '', videoError: false});
        try {
            const res = await util.request(api.GoodsDetail, {id: this.data.id});
            if (!this.isCurrent(context) || version !== this._detailVersion) {
                return;
            }
            if (res.code !== 0 || !res.data || !res.data.info) {
                throw new Error(res.msg || '商品暂时不可查看');
            }
            const detail = res.data;
            this.setData({
                goods: detail.info,
                gallery: detail.gallery || [],
                attribute: pearlCatalog.parameters(detail.info).concat(detail.attribute || []),
                issueList: detail.issue || [],
                comment: detail.comment || {},
                brand: detail.brand || {},
                productList: detail.productList || [],
                userHasCollect: detail.userHasCollect === 1 ? 1 : 0,
                goodsDetailHtml: detail.info.goodsDesc || '',
                number: 1,
            });
            this.setDefSpecInfo(detail.specificationList || []);
            this.updateCollectImage();
            this.getGoodsRelated();
        } catch (error) {
            if (this.isCurrent(context) && version === this._detailVersion) {
                this.setData({goodsError: error.message || '商品暂时不可查看, 请重试'});
            }
        } finally {
            if (this.isCurrent(context) && version === this._detailVersion) {
                this.setData({loading: false});
            }
        }
    },
    /** 游客不请求私人购物车, 数量读取失败可在下次进入页面时重试. */
    async loadCartCount() {
        const context = this.requestContext();
        if (!context.token) {
            this.setData({cartGoodsCount: 0});
            return;
        }
        const version = (this._cartVersion = (this._cartVersion || 0) + 1);
        try {
            const res = await util.request(api.CartGoodsCount);
            if (this.isCurrent(context) && version === this._cartVersion && res.code === 0) {
                this.setData({cartGoodsCount: res.data.cartTotal.goodsCount});
            }
        } catch (error) {
            // 辅助统计失败不改变购物车内容, 不阻断当前商品浏览.
        }
    },
    /** 定制配置不可用时隐藏入口, 普通商品仍可正常购买. */
    async loadCustomization() {
        const context = this.requestContext();
        this.setData({isCustom: false});
        try {
            const res = await util.request(api.CustomizationConfig, {goodsId: this.data.id}, 'GET');
            if (this.isCurrent(context)) {
                this.setData({isCustom: res.code === 0});
            }
        } catch (error) {
            // 配置请求失败保持入口隐藏, 刷新页面后可重新读取.
        }
    },
    /** 推荐有独立失败提示和重试, 不影响商品正文及购买. */
    async getGoodsRelated() {
        const context = this.requestContext();
        const version = (this._relatedVersion = (this._relatedVersion || 0) + 1);
        this.setData({relatedError: ''});
        try {
            const res = await util.request(api.GoodsRelated, {id: this.data.id});
            if (!this.isCurrent(context) || version !== this._relatedVersion) {
                return;
            }
            if (res.code !== 0) {
                throw new Error(res.msg || '推荐商品暂时不可查看');
            }
            this.setData({relatedGoods: res.data.goodsList || []});
        } catch (error) {
            if (this.isCurrent(context) && version === this._relatedVersion) {
                this.setData({relatedError: error.message || '推荐商品加载失败'});
            }
        }
    },
    /** 视频失败仅影响视频区域, 保留图片与购买入口. */
    handleVideoError() {
        this.setData({videoError: true});
    },
    /** 商品图片加载失败时替换为本地占位图. */
    handleImageError(event) {
        const type = event.currentTarget.dataset.imageType;
        const index = Number(event.currentTarget.dataset.index || 0);
        const paths = {
            gallery: `gallery[${index}].imgUrl`,
            related: `relatedGoods[${index}].listPicUrl`,
            goods: 'goods.listPicUrl',
        };
        if (paths[type]) {
            this.setData({[paths[type]]: '/static/images/logo.png'});
        }
    },
    /** 只自动选择单值规格, 多值规格保持用户主动选择. */
    setDefSpecInfo(specifications) {
        this._skuIndex = sku.prepare(this.data.productList, specifications);
        this.setData({
            specificationList: specifications.map((group) => ({
                ...group,
                valueList: group.valueList.map((value) => ({
                    ...value,
                    checked: group.valueList.length === 1,
                })),
            })),
        });
        this.changeSpecInfo();
    },
    /** 切换可用规格, 已选项始终允许取消, 提交过程中冻结购买参数. */
    clickSkuValue(event) {
        if (this.data.submitting) {
            return;
        }
        const {nameId, valueId} = event.currentTarget.dataset;
        const group = this.data.specificationList.find(
            (item) => String(item.specificationId) === String(nameId)
        );
        const value = group && group.valueList.find((item) => String(item.id) === String(valueId));
        if (!value || (value.disabled && !value.checked)) {
            return;
        }
        const checked = !value.checked;
        group.valueList.forEach((item) => {
            item.checked = item === value && checked;
        });
        this.changeSpecInfo();
    },
    /** 同步候选可用性、选中价格库存及数量, 未知SKU不可购买. */
    changeSpecInfo() {
        const specifications = sku.updateAvailability(this.data.specificationList, this._skuIndex);
        const selected = sku.selections(specifications);
        const product = selected.every(Boolean) ? sku.findProduct(this._skuIndex, selected) : null;
        const stock = sku.availableStock(product);
        const labels = specifications.flatMap((group) =>
            group.valueList.filter((value) => value.checked).map((value) => value.value)
        );
        this.setData({
            specificationList: specifications,
            selectedProduct: product,
            selectedStock: stock,
            number: Math.max(1, Math.min(this.data.number, stock || 1)),
            checkedSpecText:
                labels.join(' / ') || (specifications.length ? '请选择规格数量' : '默认规格'),
        });
    },
    /** 使用当前收藏及面板状态更新图标. */
    updateCollectImage() {
        this.setData({
            collectBackImage: this.data.openAttr
                ? '/static/images/detail_back.png'
                : this.data.userHasCollect
                  ? this.data.hasCollectImage
                  : this.data.noCollectImage,
        });
    },
    /** 初次点击购买只打开规格面板, 不隐式提交. */
    switchAttrPop() {
        if (!this.data.loading && !this.data.goodsError && !this.data.submitting) {
            this.setData({openAttr: true});
            this.updateCollectImage();
        }
    },
    /** 关闭面板或串行切换收藏, 失败保留原状态供重试. */
    async closeAttrOrCollect() {
        if (this.data.submitting) {
            return;
        }
        if (this.data.openAttr) {
            this.setData({openAttr: false});
            this.updateCollectImage();
            return;
        }
        if (this.data.collecting || this.data.loading || this.data.goodsError) {
            return;
        }
        const context = this.requestContext();
        this.setData({collecting: true});
        try {
            const res = await util.request(
                api.CollectAddOrDelete,
                {typeId: 0, valueId: this.data.id},
                'POST',
                'application/json'
            );
            if (!this.isCurrent(context)) {
                return;
            }
            if (res.code !== 0) {
                throw new Error(res.msg || '收藏操作失败');
            }
            this.setData({userHasCollect: res.data.type === 'add' ? 1 : 0});
            this.updateCollectImage();
        } catch (error) {
            if (this.isCurrent(context)) {
                util.showErrorToast(error.message || '收藏操作失败, 请重试');
            }
        } finally {
            if (this.isCurrent(context)) {
                this.setData({collecting: false});
            }
        }
    },
    /** 两种入口共用完整规格和数量校验, 最终价格库存仍由服务器复核. */
    async submitPurchase(buyNow) {
        if (this.data.loading || this.data.goodsError || this.data.submitting) {
            return;
        }
        if (!this.data.openAttr) {
            this.switchAttrPop();
            return;
        }
        const selected = sku.selections(this.data.specificationList);
        const product = selected.every(Boolean) ? sku.findProduct(this._skuIndex, selected) : null;
        if (!selected.every(Boolean)) {
            util.showErrorToast('请选择完整规格');
            return;
        }
        if (!product || sku.availableStock(product) < this.data.number) {
            util.showErrorToast('该规格库存不足, 请重新选择');
            return;
        }
        if (!Number.isSafeInteger(this.data.number) || this.data.number < 1) {
            util.showErrorToast('购买数量须为1到9999');
            return;
        }
        const context = this.requestContext();
        this._cartVersion = (this._cartVersion || 0) + 1;
        this.setData({submitting: true});
        try {
            const res = await util.request(
                buyNow ? api.BuyAdd : api.CartAdd,
                {goodsId: this.data.goods.id, number: this.data.number, productId: product.id},
                'POST',
                'application/json'
            );
            if (!this.isCurrent(context)) {
                return;
            }
            if (res.code !== 0) {
                throw new Error(res.msg || '提交失败');
            }
            if (buyNow) {
                if (!res.data.checkoutId) {
                    throw new Error('购买信息不可用, 请重试');
                }
                await new Promise((resolve, reject) =>
                    wx.navigateTo({
                        url:
                            '/pages/shopping/checkout/checkout?isBuy=true&checkoutId=' +
                            encodeURIComponent(res.data.checkoutId),
                        success: resolve,
                        fail: () => reject(new Error('结算页面打开失败, 请重试')),
                    })
                );
            } else {
                this.setData({cartGoodsCount: res.data.cartTotal.goodsCount});
                util.showSuccessToast('添加成功');
            }
            if (this.isCurrent(context)) {
                this.setData({openAttr: false});
                this.updateCollectImage();
            }
        } catch (error) {
            if (this.isCurrent(context)) {
                util.showErrorToast(error.message || '提交失败, 请重试');
            }
        } finally {
            if (this.isCurrent(context)) {
                this.setData({submitting: false});
            }
        }
    },
    /** 立即购买只创建本人结算上下文, 商品页不直接发起支付. */
    buyGoods() {
        return this.submitPurchase(true);
    },
    /** 加购物车失败不自动重试, 用户确认后可再次提交. */
    addToCart() {
        return this.submitPurchase(false);
    },
    /** 数量不低于一件, 提交时不改变购买数量. */
    cutNumber() {
        if (!this.data.submitting) {
            this.setData({number: Math.max(1, this.data.number - 1)});
        }
    },
    /** 选中在库SKU后允许增加数量, 同时限制服务端数量上限. */
    addNumber() {
        if (this.data.submitting) {
            return;
        }
        if (this.data.number >= this.data.selectedStock) {
            util.showErrorToast(
                this.data.selectedProduct ? '已达该规格可购买数量' : '请先选择完整规格'
            );
            return;
        }
        this.setData({number: this.data.number + 1});
    },
    /** 跳转到独立购物车页面. */
    openCartPage() {
        wx.navigateTo({url: '/pages/cart/cart'});
    },
    /** 将明确商品编号传给常驻DIY页签, 由设计页负责读取和校验. */
    openDesigner() {
        wx.setStorageSync('designerGoodsId', this.data.id);
        wx.switchTab({url: '/pages/designer/designer'});
    },
});
