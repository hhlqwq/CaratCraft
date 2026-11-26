const community = require('../../services/community.js');
const util = require('../../utils/util.js');
const api = require('../../config/api.js');

/** 发布流程逐张上传并保留失败重试上下文, 提交后由运营审核. */
Page({
    data: {
        kindIndex: 0,
        kinds: ['设计分享', '成品分享', '公开交流'],
        title: '',
        content: '',
        images: [],
        designs: [],

        selectedDesignId: '',
        coverScene: {},
        consent: false,
        saving: false,
        uploading: false,
        uploadProgress: '',
        error: '',
        designError: '',
        loadingDesigns: false,
    },
    /** 加载本人可分享设计, 不获取其他人的草稿. */
    onLoad(query = {}) {
        if (/^[1-9]\d*$/.test(query.designId || '')) {
            this.setData({selectedDesignId: String(query.designId)});
        }
        this._requestKey = community.requestKey();
        this.loadDesigns();
    },
    /** 获取本人设计供显式选择, 只分享已经保存的排列. */
    loadDesigns() {
        if (this.data.loadingDesigns) return;
        this.setData({loadingDesigns: true, designError: ''});
        const requestedId = this.data.selectedDesignId;
        return util.request(api.CustomizationList, {page: 1, limit: 100}, 'POST')
            .then(async (res) => {
                if (res.code !== 0 || !Array.isArray(res.data?.records)) {
                    throw new Error(res.msg || '设计加载失败');
                }
                const records = res.data.records.slice();
                if (requestedId && !records.some((row) => String(row.id) === requestedId)) {
                    const detail = await util.request(api.CustomizationDetail, {snapshotId: requestedId}, 'POST');
                    if (detail.code === 0 && detail.data?.status !== 'ARCHIVED' &&
                        String(detail.data?.id) === requestedId) {
                        records.unshift(detail.data);
                    }
                }
                const designs = records.filter(function (row) {
                    try {
                        return (
                            (JSON.parse(row.priceBreakdownJson || '{}').arrangement || []).some((part) => part.componentType === 'pearl')
                        );
                    } catch (error) {
                        return false;
                    }
                });
                this.setData({
                    designs: designs.map((row) => ({
                        ...row,
                        key: String(row.id),
                        label: row.goodsName || '珍珠项链',
                        cover: /^\/upload\//.test(row.previewUrl || '')
                            ? api.ApiOrigin + row.previewUrl
                            : (/^https:\/\//i.test(row.previewUrl || '') ? row.previewUrl : ''),
                    })),
                    selectedDesignId: designs.some((row) => String(row.id) === requestedId) ? requestedId : '',
                    designError: requestedId && !designs.some((row) => String(row.id) === requestedId)
                        ? '原设计无法分享，请选择其他已保存的珍珠设计。' : '',
                });
            })
            .then(() => this.updateCoverScene())
            .catch((error) => {
                this.setData({designError: error.message});
            })
            .finally(() => {
                this.setData({loadingDesigns: false});
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
        this.setData({kindIndex: Number(event.detail.value), consent: false, images: []});
        this.changed();
    },
    /** 明确选择本人草稿后再次确认公开. */
    selectDesign(event) {
        const selectedDesignId = String(event.currentTarget.dataset.id);
        if (this.data.saving || this.data.selectedDesignId === selectedDesignId ||
            !this.data.designs.some((row) => row.key === selectedDesignId)) return;
        this.setData({selectedDesignId, consent: false, designError: ''});
        this.updateCoverScene();
        this.changed();
    },
    /** 将保存的材料排列投影为公开封面, 不传入私人尺寸、刻字或用户资料. */
    updateCoverScene() {
        const selected = this.data.designs.find((row) => row.key === this.data.selectedDesignId);
        if (!selected) {
            this.setData({coverScene: {}});
            return;
        }
        const quote = JSON.parse(selected.priceBreakdownJson || '{}');
        if (!Array.isArray(quote.arrangement) || !quote.arrangement.length) {
            this.setData({coverScene: {}});
            return;
        }
        const scene = community.present({designJson: JSON.stringify({arrangement: quote.arrangement})}, true);
        this.setData({coverScene: {
            publicOptionIds: scene.publicOptionIds,
            publicGroups: scene.publicGroups,
            spacingMm: quote.sizing?.spacingMm || 0,
            offsetMm: quote.sizing?.layoutOffsetMm || 0,
        }});
    },

    /** 只在用户授权公开后生成封面, 同一设计重试提交复用已上传的图片. */
    async coverId(design) {
        const key = design.key + ':' + design.version;
        if (this._uploadedCover?.key === key) return this._uploadedCover.id;
        const preview = this.selectComponent('#publish-cover');
        if (!preview?.captureCover) throw new Error('请等 3D 封面加载后重试');
        const file = await preview.captureCover();
        const image = await community.upload(file);
        this._uploadedCover = {key, id: image.id};
        return image.id;
    },

    /** 公开设计只在用户主动勾选后发送. */
    consent(event) {
        if (this.data.saving || !this.data.selectedDesignId) return;
        this.setData({consent: event.detail.value.includes('yes')});
        this.changed();
    },
    /** 按顺序上传安全压缩图片, 每张显示进度, 失败保留已成功图片. */
    chooseImages() {
        if (this.data.uploading || this.data.saving || this.data.images.length >= 6) {
            return;
        }

        wx.chooseMedia({
            count: 6 - this.data.images.length,
            mediaType: ['image'],
            sizeType: ['compressed'],
            success: (result) => {
                this.setData({uploading: true, error: ''});
                let chain = Promise.resolve();
                result.tempFiles.forEach((file, index) => {
                    chain = chain.then(() => {
                        if (file.size > 5 * 1024 * 1024) {
                            throw new Error('单张图片不能超过5MB, 请压缩后重试');
                        }
                        this.setData({
                            uploadProgress:
                                '正在上传 ' + (index + 1) + '/' + result.tempFiles.length,
                        });
                        return community.upload(file.tempFilePath).then((image) => {
                            image.previewUrl = /^\/upload\//.test(image.url)
                                ? api.ApiOrigin + image.url
                                : image.url;
                            this.setData({images: this.data.images.concat([image])});
                            this.changed();
                        });
                    });
                });
                chain
                    .catch((error) => {
                        this.setData({error: error.message});
                    })
                    .finally(() => {
                        this.setData({uploading: false, uploadProgress: ''});
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
        if (this._submitted || this.data.saving || this.data.uploading || !community.loggedIn()) {
            return;
        }
        if (!this.data.title.trim() || (this.data.kindIndex !== 0 && !this.data.content.trim())) {
            this.setData({error: '请填写标题和正文'});
            return;
        }
        const design = this.data.designs.find((row) => row.key === this.data.selectedDesignId);
        if (this.data.kindIndex === 0) {
            if (!design || this.data.loadingDesigns) {
                this.setData({error: this.data.loadingDesigns ? '正在读取设计，请稍候' : '请选择一份已保存的珍珠设计'});
                return;
            }
            if (!this.data.consent) {
                this.setData({error: '请确认同意公开所选设计的材料排列'});
                return;
            }
        }

        this.setData({saving: true, error: ''});
        const designShare = this.data.kindIndex === 0;
        const media = designShare ? this.coverId(design).then((id) => [id]) :
            Promise.resolve(this.data.images.map((image) => image.id));
        return media.then((mediaIds) => community.request(
                'publish',
                {
                    requestKey: this._requestKey,
                    kind: ['DESIGN', 'FINISHED', 'SUGGESTION'][this.data.kindIndex],
                    title: this.data.title,
                    content: this.data.content,
                    mediaIds,
                    snapshotId: this.data.kindIndex === 0 ? design.id : null,
                    designConsent: this.data.kindIndex === 0 && this.data.consent,
                },
                'POST',
                true
            ))
            .then(() => {
                this._submitted = true;
                wx.showToast({title: '已提交审核', icon: 'success'});
                wx.navigateBack();
            })
            .catch((error) => {
                this.setData({error: error.message});
            })
            .finally(() => {
                this.setData({saving: false});
            });
    },
});
