const util = require('../../utils/util.js');
const api = require('../../config/api.js');

/** 每次进入私人页面都从空状态开始, 不复用上一账号的商品或删除选择. */
function initialState() {
    return {
        cartGoods: [],
        cartTotal: {goodsCount: 0, goodsAmount: 0, checkedGoodsCount: 0, checkedGoodsAmount: 0},
        isEditCart: false,
        checkedAllStatus: false,
        selectedCount: 0,
        loading: false,
        saving: false,
        error: '',
    };
}

Page({
    data: initialState(),

    /** 展示时重新读取服务端购物车, 请求代次用于隔离离页后的迟到响应. */
    onShow() {
        this.active = true;
        this.requestVersion = (this.requestVersion || 0) + 1;
        this.setData(initialState());
        return this.getCartList();
    },

    /** 离开时立即清除私人数据, 避免返回或切换账号时闪现旧购物车. */
    onHide() {
        this.active = false;
        this.requestVersion++;
        this.setData(initialState());
    },

    /** 卸载后只废弃请求, 不再对已销毁页面调用setData. */
    onUnload() {
        this.active = false;
        this.requestVersion++;
    },

    /** 列表请求使用表单参数, 与只接受JSON请求体的修改接口区分. */
    getCartList() {
        return this.requestCart(api.CartList, {}, false);
    },

    /** 统一处理购物车响应, 写请求串行发送且仅在服务端成功后更新数量和金额. */
    async requestCart(url, payload, mutation) {
        if (!this.active || this.data.loading || this.data.saving) {
            return false;
        }
        const version = this.requestVersion;
        const token = wx.getStorageSync('token') || '';
        const pendingKey = mutation ? 'saving' : 'loading';
        this.setData({[pendingKey]: true, error: ''});
        try {
            const response = await util.request(
                url,
                payload,
                'POST',
                mutation ? 'application/json' : 'application/x-www-form-urlencoded'
            );
            if (!this.isCurrentRequest(version, token)) {
                return false;
            }
            if (response.code !== 0) {
                throw new Error(response.msg || '购物车操作失败, 请重试');
            }
            const cart = response.data;
            if (!cart || !Array.isArray(cart.cartList) || !cart.cartTotal) {
                throw new Error('购物车资料格式无效, 请重试');
            }
            this.applyCart(cart);
            return true;
        } catch (error) {
            if (this.isCurrentRequest(version, token)) {
                this.setData({error: error.message || '购物车暂不可用, 请重试'});
            }
            return false;
        } finally {
            if (this.isCurrentRequest(version, token)) {
                this.setData({[pendingKey]: false});
            }
        }
    },

    /** 页面代次和登录令牌均匹配才接收响应, 旧账号结果不能覆盖当前账号. */
    isCurrentRequest(version, token) {
        return (
            this.active &&
            version === this.requestVersion &&
            token === (wx.getStorageSync('token') || '')
        );
    },

    /** 服务端勾选用于结算, 编辑勾选只用于删除, 数量更新保留本地删除选择. */
    applyCart(cart) {
        const selectedIds = new Set(
            this.data.cartGoods.filter((item) => item.editChecked).map((item) => item.id)
        );
        const cartGoods = cart.cartList.map((item) => ({
            ...item,
            checked: item.checked === true || item.checked === 1,
            editChecked: selectedIds.has(item.id),
        }));
        this.setData({cartTotal: cart.cartTotal});
        this.applySelection(cartGoods, this.data.isEditCart);
    },

    /** 从同一份新列表计算选择状态, 不依赖setData何时刷新视图. */
    applySelection(cartGoods, isEditCart) {
        const selected = cartGoods.filter((item) => (isEditCart ? item.editChecked : item.checked));
        this.setData({
            cartGoods,
            isEditCart,
            checkedAllStatus: cartGoods.length > 0 && selected.length === cartGoods.length,
            selectedCount: selected.reduce((total, item) => total + item.number, 0),
        });
    },

    /** 从绑定控件读取商品索引, 非法或过期事件不触发请求. */
    itemFromEvent(event) {
        const index = Number(event.currentTarget.dataset.itemIndex);
        return Number.isInteger(index) && index >= 0 ? this.data.cartGoods[index] : null;
    },

    /** 普通勾选同步服务端, 编辑勾选只修改本地删除目标. */
    checkedItem(event) {
        const item = this.itemFromEvent(event);
        if (!item || this.data.loading || this.data.saving) {
            return;
        }
        if (this.data.isEditCart) {
            const rows = this.data.cartGoods.map((row) =>
                row.id === item.id ? {...row, editChecked: !row.editChecked} : row
            );
            this.applySelection(rows, true);
            return;
        }
        return this.requestCart(
            api.CartChecked,
            {productIds: String(item.productId), isChecked: item.checked ? 0 : 1},
            true
        );
    },

    /** 全选遵循当前模式, 不将待删除商品误写成服务端结算选择. */
    checkedAll() {
        if (!this.data.cartGoods.length || this.data.loading || this.data.saving) {
            return;
        }
        const selected = !this.data.checkedAllStatus;
        if (this.data.isEditCart) {
            this.applySelection(
                this.data.cartGoods.map((item) => ({...item, editChecked: selected})),
                true
            );
            return;
        }
        return this.requestCart(
            api.CartChecked,
            {
                productIds: this.data.cartGoods.map((item) => item.productId).join(','),
                isChecked: selected ? 1 : 0,
            },
            true
        );
    },

    /** 进入编辑时清空删除选择, 退出时恢复服务端已有的结算选择. */
    editCart() {
        if (this.data.loading || this.data.saving) {
            return;
        }
        this.applySelection(
            this.data.cartGoods.map((item) => ({...item, editChecked: false})),
            !this.data.isEditCart
        );
    },

    /** 返回商城首页. */
    toIndexPage() {
        wx.switchTab({url: '/pages/index/index'});
    },

    /** 数量限制与服务端一致, 库存失败时保留原数量并展示失败原因. */
    updateQuantity(event, difference) {
        const item = this.itemFromEvent(event);
        if (!item || !this.data.isEditCart) {
            return;
        }
        const number = item.number + difference;
        if (number < 1 || number > 9999) {
            return;
        }
        return this.requestCart(
            api.CartUpdate,
            {
                productId: item.productId,
                goodsId: item.goodsId,
                number,
                id: item.id,
            },
            true
        );
    },

    /** 每次减少一件, 达到最小数量后不发送无效请求. */
    cutNumber(event) {
        return this.updateQuantity(event, -1);
    },

    /** 每次增加一件, 最终库存和金额以服务端成功响应为准. */
    addNumber(event) {
        return this.updateQuantity(event, 1);
    },

    /** 编辑或请求进行中不能下单, 正常状态只提交服务端已勾选的购物车. */
    checkoutOrder() {
        if (
            this.data.isEditCart ||
            this.data.loading ||
            this.data.saving ||
            !this.data.cartGoods.some((item) => item.checked)
        ) {
            return;
        }
        wx.navigateTo({url: '../shopping/checkout/checkout'});
    },

    /** 仅删除编辑状态中明确勾选的商品, 成功后使用服务端返回的剩余列表. */
    deleteCart() {
        if (!this.data.isEditCart) {
            return;
        }
        const productIds = this.data.cartGoods
            .filter((item) => item.editChecked)
            .map((item) => item.productId);
        if (!productIds.length) {
            return;
        }
        return this.requestCart(api.CartDelete, {productIds: productIds.join(',')}, true);
    },
});
