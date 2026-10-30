const {readPage, withPageReads} = require('../../../services/page-read.js');
const util = require('../../../utils/util.js');
const api = require('../../../config/api.js');
const {performPageAction} = require('../../../services/page-action.js');

Page(
    withPageReads({
        data: {
            addressList: [],
        },

        /** 页面再次展示时刷新收货地址管理状态. */
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
                    '/pages/ucenter/addressAdd/addressAdd?id=' +
                    event.currentTarget.dataset.addressId,
            });
        },
        /** 确认后删除本人指定的收货地址并刷新列表. */
        deleteAddress(event) {
            const addressId = event.currentTarget.dataset.addressId;
            return performPageAction(this, {
                confirmation: '确定要删除地址？',
                canRun: () =>
                    this.data.addressList.some((item) => String(item.id) === String(addressId)),
                request: () =>
                    util.request(api.AddressDelete, {id: addressId}, 'POST', 'application/json'),
                onSuccess: () => this.getAddressList(),
            });
        },
    })
);
