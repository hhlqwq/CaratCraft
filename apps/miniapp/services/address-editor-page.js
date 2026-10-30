const util = require('../utils/util.js');
const api = require('../config/api.js');

/** 创建地区选择的独立初始值, 不在页面间共享可变对象. */
function emptyRegions() {
    return ['省份', '城市', '区县'].map((name, index) => ({
        id: 0,
        name,
        parentId: index === 0 ? 1 : 0,
        type: index + 1,
    }));
}

/** 两个地址编辑入口统一使用服务端名称契约和异步生命周期. */
function createAddressEditorPage() {
    return {
        data: {
            address: {
                id: 0,
                userName: '',
                telNumber: '',
                provinceName: '',
                cityName: '',
                countyName: '',
                detailInfo: '',
                fullRegion: '',
                isDefault: false,
            },
            addressId: 0,
            loading: false,
            loadError: '',
            saving: false,
            saveUncertain: false,
            openSelectRegion: false,
            selectRegionList: emptyRegions(),
            regionType: 1,
            regionList: [],
            selectRegionDone: false,
            regionLoading: false,
            regionError: '',
        },

        /** 固定编辑账号和目标地址, 新建地址不读取id为0的详情. */
        onLoad(options) {
            this.editorActive = true;
            this.editorToken = wx.getStorageSync('token');
            this.editorGeneration = 1;
            this.regionGeneration = 0;
            const id = Number(options.id || 0);
            if (!Number.isSafeInteger(id) || id < 0) {
                this.setData({loadError: '地址标识无效'});
                return;
            }
            this.setData({addressId: id});
            if (id > 0) {
                this.getAddressDetail();
            }
        },

        /** 离开编辑页时废弃全部读取及写入结果. */
        onHide() {
            this.editorInterruptedRead = this.data.loading;
            this.editorInterruptedSave = this.data.saving;
            this.editorActive = false;
            this.editorGeneration++;
            this.regionGeneration++;
        },

        /** 返回编辑页时只恢复原账号的交互, 不应用离页前的响应. */
        onShow() {
            if (this.editorToken === wx.getStorageSync('token')) {
                this.editorActive = true;
                this.setData({saving: false, openSelectRegion: false, regionLoading: false});
                if (this.editorInterruptedSave) {
                    this.setData({saveUncertain: true});
                    this.editorInterruptedSave = false;
                }
                if (this.editorInterruptedRead) {
                    this.editorInterruptedRead = false;
                    this.getAddressDetail();
                }
            }
        },

        /** 卸载采用同一回调失效规则. */
        onUnload() {
            this.onHide();
        },

        /** 核对页面生命周期和原账号身份. */
        editorCurrent(generation = this.editorGeneration) {
            return (
                this.editorActive &&
                generation === this.editorGeneration &&
                !!this.editorToken &&
                this.editorToken === wx.getStorageSync('token')
            );
        },

        /** 输入只更新当前可编辑表单, 避免读取和保存期间覆盖内容. */
        updateAddressField(field, value) {
            if (this.editorCurrent() && !this.data.loading && !this.data.saving) {
                this.setData({address: {...this.data.address, [field]: value}});
            }
        },

        /** 同步收件人姓名. */
        bindinputName(event) {
            this.updateAddressField('userName', event.detail.value);
        },
        /** 同步手机号码. */
        bindinputMobile(event) {
            this.updateAddressField('telNumber', event.detail.value);
        },
        /** 同步详细收货地址. */
        bindinputAddress(event) {
            this.updateAddressField('detailInfo', event.detail.value);
        },
        /** 用明确布尔值提交默认地址状态. */
        bindIsDefault() {
            this.updateAddressField('isDefault', !this.data.address.isDefault);
        },

        /** 读取本人详情, 采用服务端provinceName等名称字段并保留失败重试. */
        async getAddressDetail() {
            if (!this.editorCurrent() || !this.data.addressId || this.data.saving) {
                return;
            }
            const generation = ++this.editorGeneration;
            this.setData({loading: true, loadError: ''});
            try {
                const response = await util.request(api.AddressDetail, {id: this.data.addressId});
                if (!this.editorCurrent(generation)) {
                    return;
                }
                if (
                    response.code !== 0 ||
                    !response.data ||
                    Number(response.data.id) !== this.data.addressId
                ) {
                    throw new Error('地址读取失败');
                }
                const address = {...response.data, isDefault: response.data.isDefault === 1};
                address.fullRegion = [
                    address.provinceName,
                    address.cityName,
                    address.countyName,
                ].join('');
                this.setData({address});
            } catch (error) {
                if (this.editorCurrent(generation)) {
                    this.setData({loadError: '地址加载失败, 请点击重试'});
                }
            } finally {
                if (this.editorCurrent(generation)) {
                    this.setData({loading: false});
                }
            }
        },

        /** 每次重新选择地区从省开始, 原地址保留到完整确认后才更新. */
        chooseRegion() {
            if (
                !this.editorCurrent() ||
                this.data.loading ||
                this.data.loadError ||
                this.data.saving
            ) {
                return;
            }
            this.setData({
                openSelectRegion: true,
                selectRegionList: emptyRegions(),
                regionType: 1,
                selectRegionDone: false,
            });
            this.getRegionList(1);
        },

        /** 按当前层级读取地区, 拒绝旧父级和已关闭选择器的响应. */
        async getRegionList(parentId) {
            if (!this.editorCurrent() || !this.data.openSelectRegion) {
                return;
            }
            this.regionParentId = parentId;
            const generation = ++this.regionGeneration;
            const editorGeneration = this.editorGeneration;
            const type = this.data.regionType;
            this.setData({regionLoading: true, regionError: '', regionList: []});
            try {
                const response = await util.request(api.RegionList, {parentId});
                if (!this.editorCurrent(editorGeneration) || generation !== this.regionGeneration) {
                    return;
                }
                if (response.code !== 0 || !Array.isArray(response.data)) {
                    throw new Error('地区读取失败');
                }
                this.setData({
                    regionList: response.data.filter((item) => Number(item.type) === type),
                });
            } catch (error) {
                if (this.editorCurrent(editorGeneration) && generation === this.regionGeneration) {
                    this.setData({regionError: '地区加载失败, 请点击重试'});
                }
            } finally {
                if (this.editorCurrent(editorGeneration) && generation === this.regionGeneration) {
                    this.setData({regionLoading: false});
                }
            }
        },

        /** 仅重试当前父级的只读地区请求. */
        retryRegions() {
            this.getRegionList(this.regionParentId);
        },

        /** 切换到已有父级支持的地区层级. */
        selectRegionType(event) {
            const index = Number(event.currentTarget.dataset.regionTypeIndex);
            if (
                !this.editorCurrent() ||
                !this.data.openSelectRegion ||
                !Number.isInteger(index) ||
                index < 0 ||
                index > 2 ||
                (index > 0 && !this.data.selectRegionList[index - 1].id)
            ) {
                return;
            }
            this.setData({regionType: index + 1});
            this.getRegionList(index === 0 ? 1 : this.data.selectRegionList[index - 1].id);
        },

        /** 选择当前层级地区并清空其全部下级选择. */
        selectRegion(event) {
            if (
                !this.editorCurrent() ||
                !this.data.openSelectRegion ||
                this.data.regionLoading ||
                this.data.regionError
            ) {
                return;
            }
            const item = this.data.regionList[Number(event.currentTarget.dataset.regionIndex)];
            if (!item || Number(item.type) !== this.data.regionType) {
                return;
            }
            const index = this.data.regionType - 1;
            const regions = this.data.selectRegionList.map((region, position) =>
                position === index
                    ? {...item}
                    : position > index
                      ? emptyRegions()[position]
                      : region
            );
            this.setData({
                selectRegionList: regions,
                selectRegionDone: regions.every((region) => !!region.id),
            });
            if (index < 2) {
                this.setData({regionType: index + 2});
                this.getRegionList(item.id);
            }
        },

        /** 完整确认后一次应用地区名称, 不向服务端提交虚构地区ID. */
        doneSelectRegion() {
            if (
                !this.editorCurrent() ||
                !this.data.openSelectRegion ||
                !this.data.selectRegionDone
            ) {
                return;
            }
            const regions = this.data.selectRegionList;
            this.updateAddressField('provinceName', regions[0].name);
            this.updateAddressField('cityName', regions[1].name);
            this.updateAddressField('countyName', regions[2].name);
            this.updateAddressField('fullRegion', regions.map((region) => region.name).join(''));
            this.cancelSelectRegion();
        },

        /** 关闭选择器并废弃在途地区请求, 保留已保存地址. */
        cancelSelectRegion() {
            this.regionGeneration++;
            this.setData({openSelectRegion: false, regionLoading: false});
        },

        /** 返回地址列表核对已保存结果. */
        cancelAddress() {
            wx.navigateBack({delta: 1});
        },

        /** 校验名称契约并从校验阶段防重, 网络结果不明时禁止重复创建. */
        async saveAddress() {
            if (
                !this.editorCurrent() ||
                this.data.loading ||
                this.data.loadError ||
                this.data.saving ||
                this.data.saveUncertain
            ) {
                return;
            }
            this.setData({saving: true});
            const generation = this.editorGeneration;
            const address = this.data.address;
            const payload = {id: address.id, isDefault: !!address.isDefault};
            for (const field of [
                'userName',
                'telNumber',
                'provinceName',
                'cityName',
                'countyName',
                'detailInfo',
            ]) {
                payload[field] = String(address[field] || '').trim();
            }
            if (
                !payload.userName ||
                payload.userName.length > 50 ||
                !/^1[3-9][0-9]{9}$/.test(payload.telNumber) ||
                !payload.provinceName ||
                !payload.cityName ||
                !payload.countyName ||
                !payload.detailInfo ||
                ['provinceName', 'cityName', 'countyName'].some(
                    (field) => payload[field].length > 50
                ) ||
                payload.detailInfo.length > 120
            ) {
                util.showErrorToast('请填写完整地址、收货人和有效手机号');
                this.setData({saving: false});
                return;
            }
            try {
                const response = await util.request(
                    api.AddressSave,
                    payload,
                    'POST',
                    'application/json'
                );
                if (!this.editorCurrent(generation)) {
                    return;
                }
                if (response.code === 0) {
                    wx.navigateBack({delta: 1});
                } else {
                    util.showErrorToast(response.msg || '保存失败, 请核对输入');
                }
            } catch (error) {
                if (this.editorCurrent(generation)) {
                    this.setData({saveUncertain: true});
                    util.showErrorToast('保存结果未确认, 请返回地址列表核对');
                }
            } finally {
                if (this.editorCurrent(generation)) {
                    this.setData({saving: false});
                }
            }
        },
    };
}

module.exports = {createAddressEditorPage};
