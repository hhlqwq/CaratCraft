const util = require('../../utils/util.js');
const api = require('../../config/api.js');

Component({
    data: {documents: [], checked: false, loading: false, error: ''},
    pageLifetimes: {
        /** 从正文页返回时重新读取发布版本, 保证勾选对应当前内容. */
        show() {
            if (this.alive) {
                this.load();
            }
        },
    },
    lifetimes: {
        /** 进入授权界面时读取当前发布版本, 不从本地缓存恢复勾选. */
        attached() {
            this.alive = true;
            this.load();
        },
        /** 组件卸载后废弃响应, 不再向离开的页面传递确认. */
        detached() {
            this.alive = false;
        },
    },
    methods: {
        /** 失败可主动重试, 两份正文均已发布才显示有效勾选项. */
        async load() {
            if (this.data.loading) {
                return;
            }
            this.setData({loading: true, checked: false, error: ''});
            this.triggerEvent('change', null);
            try {
                const response = await util.request(api.LegalDocuments, {}, 'GET');
                if (response.code !== 0 || !Array.isArray(response.data)) {
                    throw new Error(response.msg || '协议加载失败');
                }
                const documents = response.data;
                if (
                    !documents.some((item) => item.kind === 'TERMS') ||
                    !documents.some((item) => item.kind === 'PRIVACY')
                ) {
                    throw new Error('商家尚未发布完整协议, 暂不能授权登录');
                }
                if (this.alive) {
                    this.setData({documents});
                }
            } catch (error) {
                if (this.alive) {
                    this.setData({error: error.message || '协议加载失败, 请重试'});
                }
            } finally {
                if (this.alive) {
                    this.setData({loading: false});
                }
            }
        },
        /** 用户主动勾选后传递精确版本, 取消勾选立即撤销本次授权凭据. */
        change(event) {
            const checked = event.detail.value.includes('agree');
            this.setData({checked});
            const versions = {};
            for (const document of this.data.documents) {
                versions[document.kind === 'TERMS' ? 'termsVersion' : 'privacyVersion'] =
                    document.version;
            }
            this.triggerEvent('change', checked ? versions : null);
        },
    },
});
