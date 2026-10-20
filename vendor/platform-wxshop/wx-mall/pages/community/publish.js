const community = require('../../services/community.js');
const util = require('../../utils/util.js');
const api = require('../../config/api.js');

/** 发布流程逐张上传并保留失败重试上下文, 提交后由运营审核. */
Page({
  data: {
    kindIndex: 0,
    kinds: ['设计分享', '成品分享', '改进建议'],
    title: '',
    content: '',
    images: [],
    designs: [],
    designLabels: [],
    designIndex: -1,
    consent: false,
    saving: false,
    uploading: false,
    uploadProgress: '',
    error: '',
    designError: '',
    loadingDesigns: false,
  },
  /** 加载本人可分享设计, 不获取其他人的草稿. */
  onLoad() {
    this._requestKey = community.requestKey();
    this.loadDesigns();
  },
  /** 获取本人设计供显式选择, 只分享已经保存的排列. */
  loadDesigns() {
    const that = this;
    this.setData({loadingDesigns: true, designError: ''});
    util
      .request(api.CustomizationList, {page: 1, limit: 100})
      .then(function (res) {
        if (res.code !== 0) {
          throw new Error(res.msg || '设计加载失败');
        }
        const designs = res.data.records.filter(function (row) {
          try {
            return (JSON.parse(row.priceBreakdownJson || '{}').arrangement || []).length > 0;
          } catch (error) {
            return false;
          }
        });
        that.setData({
          designs: designs,
          designLabels: designs.map(function (row) {
            return '设计 #' + row.id + ' · ' + row.updateTime;
          }),
        });
      })
      .catch(function (error) {
        that.setData({designError: error.message});
      })
      .finally(function () {
        that.setData({loadingDesigns: false});
      });
  },
  /** 改动内容后使用新发布键, 未修改的网络重试沿用原键. */
  changed() {
    this._requestKey = community.requestKey();
    this.setData({error: ''});
  },
  /** 标题和正文始终按纯文本提交. */
  input(event) {
    const field = event.currentTarget.dataset.field;
    if (['title', 'content'].indexOf(field) < 0 || this.data.saving) {
      return;
    }
    const update = {};
    update[field] = event.detail.value;
    this.setData(update);
    this.changed();
  },
  /** 切换分享类型时不默认授权设计公开. */
  kind(event) {
    this.setData({kindIndex: Number(event.detail.value), consent: false});
    this.changed();
  },
  /** 明确选择本人草稿后再次确认公开. */
  selectDesign(event) {
    this.setData({designIndex: Number(event.detail.value), consent: false});
    this.changed();
  },
  /** 公开设计只在用户主动勾选后发送. */
  consent(event) {
    this.setData({consent: event.detail.value.indexOf('yes') >= 0});
    this.changed();
  },
  /** 按顺序上传安全压缩图片, 每张显示进度, 失败保留已成功图片. */
  chooseImages() {
    if (this.data.uploading || this.data.saving || this.data.images.length >= 6) {
      return;
    }
    const that = this;
    wx.chooseMedia({
      count: 6 - this.data.images.length,
      mediaType: ['image'],
      sizeType: ['compressed'],
      success(result) {
        that.setData({uploading: true, error: ''});
        let chain = Promise.resolve();
        result.tempFiles.forEach(function (file, index) {
          chain = chain.then(function () {
            if (file.size > 5 * 1024 * 1024) {
              throw new Error('单张图片不能超过5MB, 请压缩后重试');
            }
            that.setData({
              uploadProgress: '正在上传 ' + (index + 1) + '/' + result.tempFiles.length,
            });
            return community.upload(file.tempFilePath).then(function (image) {
              image.previewUrl = /^\/upload\//.test(image.url)
                ? api.ApiOrigin + image.url
                : image.url;
              that.setData({images: that.data.images.concat([image])});
              that.changed();
            });
          });
        });
        chain
          .catch(function (error) {
            that.setData({error: error.message});
          })
          .finally(function () {
            that.setData({uploading: false, uploadProgress: ''});
          });
      },
    });
  },
  /** 删除尚未提交的图片不影响已发布分享. */
  removeImage(event) {
    if (this.data.uploading || this.data.saving) {
      return;
    }
    this.setData({
      images: this.data.images.filter(function (image) {
        return image.id !== Number(event.currentTarget.dataset.id);
      }),
    });
    this.changed();
  },
  /** 提交期间冻结编辑, 失败保留正文、图片和幂等键. */
  submit() {
    if (this.data.saving || this.data.uploading || !community.loggedIn()) {
      return;
    }
    if (!this.data.title.trim() || !this.data.content.trim()) {
      this.setData({error: '请填写标题和正文'});
      return;
    }
    const design = this.data.designs[this.data.designIndex];
    if (this.data.kindIndex === 0 && (!design || !this.data.consent)) {
      this.setData({error: '请选择本人设计并同意公开材料排列'});
      return;
    }
    const that = this;
    this.setData({saving: true, error: ''});
    community
      .request(
        'publish',
        {
          requestKey: this._requestKey,
          kind: ['DESIGN', 'FINISHED', 'SUGGESTION'][this.data.kindIndex],
          title: this.data.title,
          content: this.data.content,
          mediaIds: this.data.images.map(function (image) {
            return image.id;
          }),
          snapshotId: this.data.kindIndex === 0 ? design.id : null,
          designConsent: this.data.kindIndex === 0 && this.data.consent,
        },
        'POST',
        true
      )
      .then(function () {
        that._submitted = true;
        wx.showToast({title: '已提交审核', icon: 'success'});
        wx.navigateBack();
      })
      .catch(function (error) {
        that.setData({error: error.message});
      })
      .finally(function () {
        that.setData({saving: false});
      });
  },
});
