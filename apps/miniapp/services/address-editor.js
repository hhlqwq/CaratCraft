const util = require('../utils/util.js');
const api = require('../config/api.js');

/** 两个地址编辑入口共用状态、字段契约和请求隔离, 每次创建独立数据. */
function createAddressEditor() {
    return {
        data: {
            address: {
                id: 0,
                provinceId: 0,
                cityId: 0,
                districtId: 0,
                detailInfo: '',
                provinceName: '',
                cityName: '',
                countyName: '',
                fullRegion: '',
                userName: '',
                telNumber: '',
                isDefault: 0,
            },
            saving: false,
            detailLoading: false,
            detailError: '',
            saveError: '',
            regionError: '',
            addressId: 0,
            openSelectRegion: false,
            selectRegionList: [
                {id: 0, name: '省份', parentId: 1, type: 1},
                {id: 0, name: '城市', parentId: 1, type: 2},
                {id: 0, name: '区县', parentId: 1, type: 3},
            ],
            regionType: 1,
            regionList: [],
            selectRegionDone: false,
        },
        /** 同步收件人手机号输入. */
        bindinputMobile(event) {
            if (this.data.saving || this.data.detailLoading) {
                return;
            }
            const address = this.data.address;
            address.telNumber = event.detail.value;
            this.setData({
                address: address,
            });
        },
        /** 同步收件人姓名输入. */
        bindinputName(event) {
            if (this.data.saving || this.data.detailLoading) {
                return;
            }
            const address = this.data.address;
            address.userName = event.detail.value;
            this.setData({
                address: address,
            });
        },
        /** 同步收货地址详细地址输入. */
        bindinputAddress(event) {
            if (this.data.saving || this.data.detailLoading) {
                return;
            }
            const address = this.data.address;
            address.detailInfo = event.detail.value;
            this.setData({
                address: address,
            });
        },
        /** 切换当前地址的默认地址标记. */
        bindIsDefault() {
            if (this.data.saving || this.data.detailLoading) {
                return;
            }
            const address = this.data.address;
            address.isDefault = !address.isDefault;
            this.setData({
                address: address,
            });
        },
        /** 读取本人指定地址并恢复地址编辑表单. */
        getAddressDetail() {
            const id = this.data.addressId;
            const token = wx.getStorageSync('token') || '';
            const version = (this._detailVersion = (this._detailVersion || 0) + 1);
            const current = () =>
                this._active !== false &&
                version === this._detailVersion &&
                id === this.data.addressId &&
                token === (wx.getStorageSync('token') || '');
            this.setData({detailLoading: true, detailError: ''});
            return util
                .request(api.AddressDetail, {id})
                .then((res) => {
                    if (!current()) {
                        return;
                    }
                    if (res.code !== 0 || !res.data || res.data.id !== id) {
                        throw new Error(res.msg || '地址读取失败');
                    }
                    this.setData({
                        address: {
                            ...this.data.address,
                            ...res.data,
                            isDefault: res.data.isDefault === 1,
                        },
                        detailLoading: false,
                    });
                })
                .catch((error) => {
                    if (current()) {
                        this.setData({
                            detailLoading: false,
                            detailError: error.message || '地址读取失败, 请重试',
                        });
                    }
                });
        },
        /** 按省市区选择进度更新确认按钮状态. */
        setRegionDoneStatus() {
            const doneStatus = this.data.selectRegionList.every((item) => {
                return item.id != 0;
            });

            this.setData({
                selectRegionDone: doneStatus,
            });
        },
        /** 打开地区选择器并恢复已有省市区选择. */
        chooseRegion() {
            if (this.data.saving || this.data.detailLoading) {
                return;
            }
            this.setData({
                openSelectRegion: !this.data.openSelectRegion,
            });

            //设置区域选择数据
            const address = this.data.address;
            if (address.provinceId > 0 && address.cityId > 0 && address.districtId > 0) {
                const selectRegionList = this.data.selectRegionList;
                selectRegionList[0].id = address.provinceId;
                selectRegionList[0].name = address.provinceName;
                selectRegionList[0].parentId = 1;

                selectRegionList[1].id = address.cityId;
                selectRegionList[1].name = address.cityName;
                selectRegionList[1].parentId = address.provinceId;

                selectRegionList[2].id = address.districtId;
                selectRegionList[2].name = address.countyName;
                selectRegionList[2].parentId = address.cityId;

                this.setData({
                    selectRegionList: selectRegionList,
                    regionType: 3,
                });

                this.getRegionList(address.cityId);
            } else {
                this.setData({
                    selectRegionList: [
                        {id: 0, name: '省份', parentId: 1, type: 1},
                        {id: 0, name: '城市', parentId: 1, type: 2},
                        {id: 0, name: '区县', parentId: 1, type: 3},
                    ],
                    regionType: 1,
                });
                this.getRegionList(1);
            }

            this.setRegionDoneStatus();
        },
        /** 初始化收货地址编辑状态并读取页面所需数据. */
        onLoad(options = {}) {
            this._active = true;
            this._sessionToken = wx.getStorageSync('token') || '';
            this._saveVersion = (this._saveVersion || 0) + 1;
            const id = Number(options.id || 0);
            this.setData({addressId: Number.isSafeInteger(id) && id > 0 ? id : 0});
            if (this.data.addressId) {
                this.getAddressDetail();
            }
            this.getRegionList(1);
        },
        /** 账号切换后清除私人地址和在途请求, 不让旧输入跨账号保存. */
        onShow() {
            const token = wx.getStorageSync('token') || '';
            if (this._sessionToken !== undefined && token !== this._sessionToken) {
                this._detailVersion = (this._detailVersion || 0) + 1;
                this._regionVersion = (this._regionVersion || 0) + 1;
                this.setData(createAddressEditor().data);
                this.onLoad({});
            }
            this._sessionToken = token;
        },
        /** 离页后使读取和保存回调失效, 不自动重放写入. */
        onUnload() {
            this._active = false;
            this._detailVersion = (this._detailVersion || 0) + 1;
            this._regionVersion = (this._regionVersion || 0) + 1;
            this._saveVersion = (this._saveVersion || 0) + 1;
        },
        /** 切换省、市或区的当前选择层级. */
        selectRegionType(event) {
            const regionTypeIndex = event.target.dataset.regionTypeIndex;
            const selectRegionList = this.data.selectRegionList;

            //判断是否可点击
            if (
                regionTypeIndex + 1 == this.data.regionType ||
                (regionTypeIndex - 1 >= 0 && selectRegionList[regionTypeIndex - 1].id <= 0)
            ) {
                return false;
            }

            this.setData({
                regionType: regionTypeIndex + 1,
            });

            const selectRegionItem = selectRegionList[regionTypeIndex];

            this.getRegionList(selectRegionItem.parentId);

            this.setRegionDoneStatus();
        },
        /** 应用点击的地区并加载下一层级选项. */
        selectRegion(event) {
            const regionIndex = event.target.dataset.regionIndex;
            const regionItem = this.data.regionList[regionIndex];
            const regionType = regionItem.type;
            const selectRegionList = this.data.selectRegionList;
            selectRegionList[regionType - 1] = regionItem;

            if (regionType != 3) {
                this.setData({
                    selectRegionList: selectRegionList,
                    regionType: regionType + 1,
                });
                this.getRegionList(regionItem.id);
            } else {
                this.setData({
                    selectRegionList: selectRegionList,
                });
            }

            //重置下级区域为空
            selectRegionList.map((item, index) => {
                if (index > regionType - 1) {
                    item.id = 0;
                    item.name = index == 1 ? '城市' : '区县';
                    item.parentId = 0;
                }
                return item;
            });

            this.setData({
                selectRegionList: selectRegionList,
            });

            this.setData({
                regionList: this.data.regionList.map((item) => {
                    //标记已选择的
                    if (
                        this.data.regionType == item.type &&
                        this.data.selectRegionList[this.data.regionType - 1].id == item.id
                    ) {
                        item.selected = true;
                    } else {
                        item.selected = false;
                    }

                    return item;
                }),
            });

            this.setRegionDoneStatus();
        },
        /** 确认已选省市区并关闭地区选择器. */
        doneSelectRegion() {
            if (this.data.selectRegionDone === false) {
                return false;
            }

            const address = this.data.address;
            const selectRegionList = this.data.selectRegionList;
            address.provinceId = selectRegionList[0].id;
            address.cityId = selectRegionList[1].id;
            address.districtId = selectRegionList[2].id;
            address.provinceName = selectRegionList[0].name;
            address.cityName = selectRegionList[1].name;
            address.countyName = selectRegionList[2].name;
            address.fullRegion = selectRegionList
                .map((item) => {
                    return item.name;
                })
                .join('');

            this.setData({
                address: address,
                openSelectRegion: false,
            });
        },
        /** 取消地区选择并关闭选择器. */
        cancelSelectRegion() {
            this.setData({
                openSelectRegion: false,
                regionType: this.data.regionDoneStatus ? 3 : 1,
            });
        },
        /** 根据上级地区标识读取下一级地区列表. */
        getRegionList(regionId) {
            const type = this.data.regionType;
            const token = wx.getStorageSync('token') || '';
            const version = (this._regionVersion = (this._regionVersion || 0) + 1);
            this._regionParent = regionId;
            const current = () =>
                this._active !== false &&
                version === this._regionVersion &&
                token === (wx.getStorageSync('token') || '');
            this.setData({regionError: ''});
            return util
                .request(api.RegionList, {parentId: regionId})
                .then((res) => {
                    if (!current()) {
                        return;
                    }
                    if (res.code !== 0 || !Array.isArray(res.data)) {
                        throw new Error(res.msg || '地区读取失败');
                    }
                    this.setData({
                        regionList: res.data.map((item) => ({
                            ...item,
                            selected:
                                type === item.type &&
                                this.data.selectRegionList[type - 1].id === item.id,
                        })),
                    });
                })
                .catch((error) => {
                    if (current()) {
                        this.setData({
                            regionList: [],
                            regionError: error.message || '地区读取失败, 请重试',
                        });
                    }
                });
        },
        /** 明确重试当前地区层级, 不保存或推断未知地区. */
        retryRegion() {
            return this.getRegionList(this._regionParent || 1);
        },
        /** 放弃当前地址编辑并返回上一页. */
        cancelAddress() {
            if (!this.data.saving) {
                wx.navigateBack({delta: 1});
            }
        },
        /** 校验收货信息并保存当前地址. */
        saveAddress() {
            if (this.data.saving || this.data.detailLoading || this.data.detailError) {
                return Promise.resolve(false);
            }
            const address = this.data.address;
            const names = ['provinceName', 'cityName', 'countyName'];
            if (
                !address.userName ||
                !address.userName.trim() ||
                address.userName.length > 50 ||
                !/^1[3-9][0-9]{9}$/.test(address.telNumber) ||
                names.some(
                    (key) => !address[key] || !address[key].trim() || address[key].length > 50
                ) ||
                !address.detailInfo ||
                !address.detailInfo.trim() ||
                address.detailInfo.length > 120
            ) {
                this.setData({
                    saveError: '请填写完整省市区、50字以内收货人、有效手机号及120字以内详细地址',
                });
                return Promise.resolve(false);
            }
            const token = wx.getStorageSync('token') || '';
            const version = (this._saveVersion = (this._saveVersion || 0) + 1);
            const current = () =>
                this._active !== false &&
                version === this._saveVersion &&
                token === (wx.getStorageSync('token') || '');
            const body = {
                id: address.id || 0,
                userName: address.userName,
                telNumber: address.telNumber,
                provinceName: address.provinceName,
                cityName: address.cityName,
                countyName: address.countyName,
                isDefault: address.isDefault === true || address.isDefault === 1,
                detailInfo: address.detailInfo,
            };
            this.setData({saving: true, saveError: ''});
            return util
                .request(api.AddressSave, body, 'POST', 'application/json')
                .then((res) => {
                    if (!current()) {
                        return false;
                    }
                    if (res.code !== 0) {
                        throw new Error(res.msg || '地址保存失败');
                    }
                    wx.navigateBack({delta: 1});
                    return true;
                })
                .catch((error) => {
                    if (current()) {
                        this.setData({saveError: error.message || '保存失败, 请重试'});
                    }
                    return false;
                })
                .finally(() => {
                    if (current()) {
                        this.setData({saving: false});
                    }
                });
        },
    };
}
module.exports = {createAddressEditor};
