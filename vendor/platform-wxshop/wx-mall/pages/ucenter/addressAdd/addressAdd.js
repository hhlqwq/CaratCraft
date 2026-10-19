const util = require('../../../utils/util.js');
const api = require('../../../config/api.js');

Page({
  data: {
    address: {
      id: 0,
      provinceId: 0,
      cityId: 0,
      districtId: 0,
      address: '',
      fullRegion: '',
      userName: '',
      telNumber: '',
      isDefault: 0,
    },
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
    const address = this.data.address;
    address.telNumber = event.detail.value;
    this.setData({
      address: address,
    });
  },
  /** 同步收件人姓名输入. */
  bindinputName(event) {
    const address = this.data.address;
    address.userName = event.detail.value;
    this.setData({
      address: address,
    });
  },
  /** 同步收货地址详细地址输入. */
  bindinputAddress(event) {
    const address = this.data.address;
    address.detailInfo = event.detail.value;
    this.setData({
      address: address,
    });
  },
  /** 切换当前地址的默认地址标记. */
  bindIsDefault() {
    const address = this.data.address;
    address.isDefault = !address.isDefault;
    this.setData({
      address: address,
    });
  },
  /** 读取本人指定地址并恢复地址编辑表单. */
  getAddressDetail() {
    const that = this;
    util.request(api.AddressDetail, {id: that.data.addressId}).then(function (res) {
      if (res.code === 0) {
        if (res.data) {
          that.setData({
            address: res.data,
          });
        }
      }
    });
  },
  /** 按省市区选择进度更新确认按钮状态. */
  setRegionDoneStatus() {
    const that = this;
    const doneStatus = that.data.selectRegionList.every((item) => {
      return item.id != 0;
    });

    that.setData({
      selectRegionDone: doneStatus,
    });
  },
  /** 打开地区选择器并恢复已有省市区选择. */
  chooseRegion() {
    this.setData({
      openSelectRegion: !this.data.openSelectRegion,
    });

    //设置区域选择数据
    const address = this.data.address;
    if (address.provinceId > 0 && address.cityId > 0 && address.districtId > 0) {
      const selectRegionList = this.data.selectRegionList;
      selectRegionList[0].id = address.provinceId;
      selectRegionList[0].name = address.province_name;
      selectRegionList[0].parentId = 1;

      selectRegionList[1].id = address.cityId;
      selectRegionList[1].name = address.city_name;
      selectRegionList[1].parentId = address.provinceId;

      selectRegionList[2].id = address.districtId;
      selectRegionList[2].name = address.district_name;
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
  onLoad: function (options) {
    if (options.id) {
      this.setData({
        addressId: options.id,
      });
      this.getAddressDetail();
    }

    this.getRegionList(1);
  },

  /** 切换省、市或区的当前选择层级. */
  selectRegionType(event) {
    const that = this;
    const regionTypeIndex = event.target.dataset.regionTypeIndex;
    const selectRegionList = that.data.selectRegionList;

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
    const that = this;
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

    that.setData({
      regionList: that.data.regionList.map((item) => {
        //标记已选择的
        if (
          that.data.regionType == item.type &&
          that.data.selectRegionList[that.data.regionType - 1].id == item.id
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
    address.province_name = selectRegionList[0].name;
    address.city_name = selectRegionList[1].name;
    address.district_name = selectRegionList[2].name;
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
    const that = this;
    const regionType = that.data.regionType;
    util.request(api.RegionList, {parentId: regionId}).then(function (res) {
      if (res.code === 0) {
        that.setData({
          regionList: res.data.map((item) => {
            //标记已选择的
            if (
              regionType == item.type &&
              that.data.selectRegionList[regionType - 1].id == item.id
            ) {
              item.selected = true;
            } else {
              item.selected = false;
            }

            return item;
          }),
        });
      }
    });
  },
  /** 放弃当前地址编辑并返回上一页. */
  cancelAddress() {
    wx.navigateBack({
      url: '/pages/ucenter/address/address',
    });
  },
  /** 校验收货信息并保存当前地址. */
  saveAddress() {
    const address = this.data.address;

    if (address.userName == '') {
      util.showErrorToast('请输入姓名');

      return false;
    }

    if (address.telNumber == '') {
      util.showErrorToast('请输入手机号码');
      return false;
    }

    if (address.districtId == 0) {
      util.showErrorToast('请输入省市区');
      return false;
    }

    if (address.detailInfo == '') {
      util.showErrorToast('请输入详细地址');
      return false;
    }

    util
      .request(
        api.AddressSave,
        {
          id: address.id,
          userName: address.userName,
          telNumber: address.telNumber,
          provinceId: address.provinceId,
          cityId: address.cityId,
          districtId: address.districtId,
          isDefault: address.isDefault,
          provinceName: address.province_name,
          cityName: address.city_name,
          countyName: address.district_name,
          detailInfo: address.detailInfo,
        },
        'POST',
        'application/json'
      )
      .then(function (res) {
        if (res.code === 0) {
          wx.navigateBack({
            url: '/pages/ucenter/address/address',
          });
        }
      });
  },
});
