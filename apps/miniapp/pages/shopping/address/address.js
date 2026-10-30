const {readPage, withPageReads} = require('../../../services/page-read.js');
const util = require('../../../utils/util.js');
const api = require('../../../config/api.js');

Page(
    withPageReads({
        data: {
            addressList: [],
        },

        /** 页面再次展示时刷新结算收货地址状态. */
        onShow() {
            this.getAddressList();
        },
        /** 读取本人保存的收货地址列表. */
        getAddressList() {
            readPage(
                this,
                'AddressList',
                () => util.request(api.AddressList),
                (res) => {
                    if (res.code === 0) {
                        this.setData({
                            addressList: res.data,
                        });
                    }
                }
            );
        },
        /** 携带地址标识进入新增或编辑页面. */
        addressAddOrUpdate(event) {
            wx.navigateTo({
                url:
                    '/pages/shopping/addressAdd/addressAdd?id=' +
                    event.currentTarget.dataset.addressId,
            });
        },
        /** 记录当前选择的收货地址并返回结算流程. */
        selectAddress(event) {
            const id = event.currentTarget.dataset.addressId;
            if (
                this.data.readLoading ||
                this.data.readError ||
                (wx.getStorageSync('token') || '') !== this._readToken ||
                !this.data.addressList.some((item) => String(item.id) === String(id))
            ) {
                return;
            }
            try {
                wx.setStorageSync('addressId', id);
            } catch (e) {
                util.showErrorToast('无法保存收货地址, 请重试');
                return;
            }

            //选择该收货地址
            wx.navigateBack({});
        },
    })
);
