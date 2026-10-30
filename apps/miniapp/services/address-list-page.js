const util = require('../utils/util.js');
const api = require('../config/api.js');

/** 为管理和结算入口提供同一地址读取、归属隔离及删除规则. */
function createAddressListPage(checkout) {
    return {
        data: {addressList: [], loading: false, loadError: '', deleting: false},

        /** 展示时绑定当前账号并重新读取, 不沿用此前账号的地址. */
        onShow() {
            this.addressActive = true;
            this.addressToken = wx.getStorageSync('token');
            this.addressGeneration = (this.addressGeneration || 0) + 1;
            this.setData({addressList: [], deleting: false});
            this.getAddressList();
        },

        /** 离开页面时废弃读取、确认及写入回调. */
        onHide() {
            this.addressActive = false;
            this.addressGeneration = (this.addressGeneration || 0) + 1;
        },

        /** 卸载时复用离页隔离规则. */
        onUnload() {
            this.onHide();
        },

        /** 只允许当前可见页面及原认证账号继续操作. */
        addressContextCurrent(generation = this.addressGeneration) {
            return (
                this.addressActive &&
                generation === this.addressGeneration &&
                !!this.addressToken &&
                this.addressToken === wx.getStorageSync('token')
            );
        },

        /** 读取最新列表, 失败清空旧地址并提供显式重试. */
        async getAddressList() {
            if (!this.addressContextCurrent() || this.data.deleting) {
                return;
            }
            const generation = ++this.addressGeneration;
            this.setData({loading: true, loadError: '', addressList: []});
            try {
                const response = await util.request(api.AddressList);
                if (!this.addressContextCurrent(generation)) {
                    return;
                }
                if (response.code !== 0 || !Array.isArray(response.data)) {
                    throw new Error('地址读取失败');
                }
                this.setData({addressList: response.data});
            } catch (error) {
                if (this.addressContextCurrent(generation)) {
                    this.setData({loadError: '地址加载失败, 请点击重试'});
                }
            } finally {
                if (this.addressContextCurrent(generation)) {
                    this.setData({loading: false});
                }
            }
        },

        /** 核对当前列表中的地址, 拒绝过期节点或其他账号选择. */
        addressAvailable(id) {
            return (
                this.addressContextCurrent() &&
                !this.data.loading &&
                !this.data.loadError &&
                !this.data.deleting &&
                this.data.addressList.some((address) => String(address.id) === String(id))
            );
        },

        /** 携带已核对的地址标识进入对应编辑入口. */
        addressAddOrUpdate(event) {
            const id = event.currentTarget.dataset.addressId;
            if (
                !this.addressContextCurrent() ||
                (String(id) !== '0' && !this.addressAvailable(id))
            ) {
                return;
            }
            wx.navigateTo({
                url: `/pages/${checkout ? 'shopping' : 'ucenter'}/addressAdd/addressAdd?id=${encodeURIComponent(id)}`,
            });
        },

        /** 只保存当前账号已成功加载的地址并返回结算. */
        selectAddress(event) {
            const id = event.currentTarget.dataset.addressId;
            if (!checkout || !this.addressAvailable(id)) {
                return;
            }
            try {
                wx.setStorageSync('addressId', id);
            } catch (error) {
                util.showErrorToast('无法保存收货地址, 请重试');
                return;
            }
            wx.navigateBack({delta: 1});
        },

        /** 从确认阶段持锁, 写入不自动重放且完成后重新核对列表. */
        deleteAddress(event) {
            const id = event.currentTarget.dataset.addressId;
            if (checkout || !this.addressAvailable(id)) {
                return;
            }
            const generation = ++this.addressGeneration;
            this.setData({deleting: true});
            wx.showModal({
                title: '',
                content: '确定要删除地址？',
                success: async (result) => {
                    if (!this.addressContextCurrent(generation)) {
                        return;
                    }
                    if (!result.confirm) {
                        this.setData({deleting: false});
                        return;
                    }
                    try {
                        const response = await util.request(
                            api.AddressDelete,
                            {id},
                            'POST',
                            'application/json'
                        );
                        if (this.addressContextCurrent(generation) && response.code !== 0) {
                            util.showErrorToast(response.msg || '删除失败, 请重新核对地址');
                        }
                    } catch (error) {
                        if (this.addressContextCurrent(generation)) {
                            util.showErrorToast('删除结果未确认, 请重新核对地址');
                        }
                    } finally {
                        if (this.addressContextCurrent(generation)) {
                            this.setData({deleting: false});
                            this.getAddressList();
                        }
                    }
                },
                fail: () => {
                    if (this.addressContextCurrent(generation)) {
                        this.setData({deleting: false});
                        util.showErrorToast('无法打开确认框, 请重试');
                    }
                },
            });
        },
    };
}

module.exports = {createAddressListPage};
