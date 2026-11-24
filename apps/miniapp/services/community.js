const util = require('../utils/util.js');
const api = require('../config/api.js');

/** 检查会话后导航登录, 公共浏览不强制登录. */
function loggedIn() {
    if (wx.getStorageSync('token')) {
        return true;
    }
    wx.navigateTo({url: '/pages/auth/btnAuth/btnAuth'});
    return false;
}

/** 统一社区业务失败, 避免错误响应被当作空数据成功处理. */
function request(path, data, method, json) {
    return util
        .request(
            api.CommunityBase + path,
            data || {},
            method || 'GET',
            json ? 'application/json' : 'application/x-www-form-urlencoded'
        )
        .then(function (res) {
            if (res.code !== 0) {
                throw new Error(res.msg || '社区请求失败');
            }
            return res.data;
        });
}

/** 将JSON字段转为仅包含展示数据的对象, 图片只允许已审核站内或HTTPS地址. */
function present(item, includeScene = false) {
    let images = [];
    let design = {};
    try {
        images = JSON.parse(item.imagesJson || '[]');
    } catch (error) {
        /* 旧数据缺图仍可阅读正文. */
    }
    try {
        design = JSON.parse(item.designJson || '{}');
    } catch (error) {
        /* 不显示损坏的设计副本. */
    }
    const parts = Array.isArray(design.arrangement) ? design.arrangement : [];
    const options = includeScene ? parts.map(function (part, index) {
        return {
            id: index + 1,
            componentType: part.componentType,
            name: part.name,
            materialParams: JSON.stringify({
                diameterMm: part.sizeMm,
                spanMm: part.sizeMm,
                lengthMm: part.sizeMm,
                lengthRole: part.lengthRole,
                axialMm: part.axialMm,
                mountSpanMm: part.mountSpanMm,
                claspRole: part.claspRole,
                chainStyle: part.chainStyle,
                shape: part.shape,
                color: part.color,
            }),
        };
    }) : [];
    return Object.assign({}, item, {
        kindText:
            {DESIGN: '设计分享', FINISHED: '成品分享', SUGGESTION: '改进建议'}[item.kind] || '分享',
        statusText:
            {PENDING: '待审核', PUBLISHED: '已公开', HIDDEN: '未公开', DELETED: '已删除'}[
                item.status
            ] || '',
        images: (Array.isArray(images) ? images : [])
            .filter(function (url) {
                return (
                    typeof url === 'string' && (/^https:\/\//i.test(url) || /^\/upload\//.test(url))
                );
            })
            .map(function (url) {
                return /^\/upload\//.test(url) ? api.ApiOrigin + url : url;
            }),
        arrangement: parts,
        publicOptionIds: options.map((option) => option.id),
        publicGroups: options.length ? [{options}] : [],
    });
}

/** 生成会话内发布幂等键, 网络失败重试沿用原键. */
function requestKey() {
    return (
        'post_' +
        Date.now().toString(36) +
        '_' +
        Math.random().toString(36).slice(2) +
        Math.random().toString(36).slice(2)
    );
}

/** 上传进度由调用页显示, 服务端解码并验证图片后返回归属ID. */
function upload(filePath) {
    return new Promise(function (resolve, reject) {
        wx.uploadFile({
            url: api.CommunityBase + 'image',
            filePath: filePath,
            name: 'file',
            header: {token: wx.getStorageSync('token') || ''},
            success(res) {
                try {
                    const body = JSON.parse(res.data);
                    if (res.statusCode !== 200 || body.code !== 0) {
                        throw new Error(body.msg || '图片上传失败');
                    }
                    resolve(body.data);
                } catch (error) {
                    reject(error);
                }
            },
            fail() {
                reject(new Error('图片上传失败, 请检查网络'));
            },
        });
    });
}

module.exports = {
    loggedIn: loggedIn,
    request: request,
    present: present,
    requestKey: requestKey,
    upload: upload,
};
