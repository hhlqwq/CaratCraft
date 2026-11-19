const contacts = require('../../services/merchant-contact.js');

Component({
    data: {visible: false, loading: false, contact: {}, error: '', configured: false},
    lifetimes: {
        /** 记录组件存活状态, 防止迟到请求更新已经卸载的页面组件. */
        attached() {
            this.isAttached = true;
        },
        /** 卸载只使本组件失效, 不取消其他页面共享的公开配置请求. */
        detached() {
            this.isAttached = false;
        },
    },
    methods: {
        /** 用户主动打开时才请求客服配置, 不为匿名浏览建立个人账号. */
        async open() {
            if (this.data.loading) {
                this.setData({visible: true});
                return;
            }
            this.setData({visible: true, loading: true, error: ''});
            try {
                const contact = await contacts.load();
                if (!this.isAttached) {
                    return;
                }
                this.setData({
                    contact,
                    configured: !!(
                        contact.phone ||
                        contact.wechatId ||
                        contact.qrUrl ||
                        contact.wxChatEnabled
                    ),
                });
            } catch (error) {
                if (this.isAttached) {
                    this.setData({error: '客服资料暂时不可用, 请稍后重试'});
                }
            } finally {
                if (this.isAttached) {
                    this.setData({loading: false});
                }
            }
        },
        /** 关闭联系面板, 不清除共享的短期公开配置缓存. */
        close() {
            this.setData({visible: false});
        },
        /** catchtap在面板处截断冒泡, 避免点击内容误触遮罩关闭. */
        stopPropagation() {
            return false;
        },
        /** 只复制经营者已配置的微信号, 添加好友由消费者自行确认. */
        copyWechat() {
            if (!this.data.contact.wechatId) {
                return;
            }
            wx.setClipboardData({
                data: this.data.contact.wechatId,
                fail() {
                    wx.showToast({title: '复制未完成, 可长按微信号记录', icon: 'none'});
                },
            });
        },
        /** 电话调用由消费者明确点击发起, 未配置号码时不触发微信接口. */
        call() {
            if (this.data.contact.phone) {
                wx.makePhoneCall({phoneNumber: this.data.contact.phone.replace(/[()\s-]/g, '')});
            }
        },
        /** 预览本商家上传的客服二维码, 不使用参考商家的图片. */
        previewQr() {
            const url = this.data.contact.qrUrl;
            if (url) {
                wx.previewImage({current: url, urls: [url]});
            }
        },
    },
});
