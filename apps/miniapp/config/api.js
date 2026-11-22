const environment = require('./environment.js');

/** 根据微信发布环境选择独立服务地址, 不读取曾经保存在设备上的旧地址. */
function resolveBaseUrl() {
    let version = 'release';
    if (typeof wx.getAccountInfoSync === 'function') {
        version = wx.getAccountInfoSync().miniProgram.envVersion || 'release';
    }
    const value = String(environment[version + 'BaseUrl'] || '')
        .trim()
        .replace(/\/*$/, '/');
    if (!/^https?:\/\/[^\s?#]+\/$/.test(value)) {
        throw new Error(`小程序 ${version} 缺少有效 API 地址, 请检查 deploy/.env 并重新生成`);
    }
    if (
        version !== 'develop' &&
        (!/^https:\/\/[a-zA-Z0-9.-]+\.[a-zA-Z0-9.-]+(?::\d+)?\//.test(value) ||
            /^https:\/\/(localhost|127\.|0\.0\.0\.0)/.test(value))
    ) {
        throw new Error('体验版和正式版必须使用已配置合法域名的HTTPS地址');
    }
    return value;
}

const API_BASE_URL = resolveBaseUrl();

module.exports = {
    LegalDocuments: API_BASE_URL + 'legal',
    ApiOrigin: API_BASE_URL.match(/^https?:\/\/[^/]+/)[0],
    IndexUrlNewGoods: API_BASE_URL + 'index/newGoods', //新品首发
    IndexUrlHotGoods: API_BASE_URL + 'index/hotGoods', //热卖商品
    IndexUrlTopic: API_BASE_URL + 'index/topic', //专题精选
    IndexUrlBrand: API_BASE_URL + 'index/brand', //品牌制造商
    IndexUrlCategory: API_BASE_URL + 'index/category', //首页底部的分类及商品列表
    IndexUrlBanner: API_BASE_URL + 'index/banner', //首页banner
    IndexUrlChannel: API_BASE_URL + 'index/channel', //banner下的分类
    Catalog: API_BASE_URL + 'catalog/', //分类数据接口
    CatalogList: API_BASE_URL + 'catalog/index', //分类目录全部分类数据接口
    CatalogCurrent: API_BASE_URL + 'catalog/current', //分类目录当前分类数据接口

    AuthLoginByWeixin: API_BASE_URL + 'auth/LoginByMa', //微信登录

    GoodsCount: API_BASE_URL + 'goods/count', //统计商品总数
    GoodsList: API_BASE_URL + 'goods/list', //获得商品列表
    GoodsCategory: API_BASE_URL + 'goods/category', //获得分类数据
    GoodsDetail: API_BASE_URL + 'goods/detail', //获得商品的详情
    GoodsNew: API_BASE_URL + 'goods/new', //新品介绍
    GoodsHot: API_BASE_URL + 'goods/hot', //人气推荐
    GoodsRelated: API_BASE_URL + 'goods/related', //商品详情页的关联商品（大家都在看）
    CustomizationConfig: API_BASE_URL + 'customization/config', //获取定制器配置
    CustomizationReference: API_BASE_URL + 'customization/reference', //获取首版规则参考
    CustomizationQuote: API_BASE_URL + 'customization/quote', //服务端定制报价
    CustomizationSave: API_BASE_URL + 'customization/save', //保存定制设计
    CustomizationArchive: API_BASE_URL + 'customization/archive', //归档本人草稿
    CustomizationList: API_BASE_URL + 'customization/list', //我的定制设计
    CustomizationDetail: API_BASE_URL + 'customization/detail', //获取本人设计详情
    CustomizationCheckoutQuote: API_BASE_URL + 'customization/checkout-quote', //结算前最新报价

    BrandList: API_BASE_URL + 'brand/list', //品牌列表
    BrandDetail: API_BASE_URL + 'brand/detail', //品牌详情

    CartList: API_BASE_URL + 'cart/index', //获取购物车的数据
    CartAdd: API_BASE_URL + 'cart/add', // 添加商品到购物车
    BuyAdd: API_BASE_URL + 'buy/add', // 直接购买
    CartUpdate: API_BASE_URL + 'cart/update', // 更新购物车的商品
    CartDelete: API_BASE_URL + 'cart/delete', // 删除购物车的商品
    CartChecked: API_BASE_URL + 'cart/checked', // 选择或取消选择商品
    CartGoodsCount: API_BASE_URL + 'cart/goodscount', // 获取购物车商品件数
    CartCheckout: API_BASE_URL + 'cart/checkout', // 下单前信息确认

    OrderSubmit: API_BASE_URL + 'order/submit', // 提交订单
    PayPrepayId: API_BASE_URL + 'pay/prepay', //获取微信统一下单prepay_id

    CollectList: API_BASE_URL + 'collect/list', //收藏列表
    CollectAddOrDelete: API_BASE_URL + 'collect/addordelete', //添加或取消收藏

    CommentList: API_BASE_URL + 'comment/list', //评论列表
    CommentCount: API_BASE_URL + 'comment/count', //评论总数
    CommentPost: API_BASE_URL + 'comment/post', //发表评论

    CommunityBase: API_BASE_URL + 'community/', // 社区发布和点赞

    TopicList: API_BASE_URL + 'topic/list', //专题列表
    TopicDetail: API_BASE_URL + 'topic/detail', //专题详情
    TopicRelated: API_BASE_URL + 'topic/related', //相关专题

    SearchIndex: API_BASE_URL + 'search/index', //搜索页面数据
    SearchHelper: API_BASE_URL + 'search/helper', //搜索帮助
    SearchClearHistory: API_BASE_URL + 'search/clearhistory', //搜索帮助

    AddressList: API_BASE_URL + 'address/list', //收货地址列表
    AddressDetail: API_BASE_URL + 'address/detail', //收货地址详情
    AddressSave: API_BASE_URL + 'address/save', //保存收货地址
    AddressDelete: API_BASE_URL + 'address/delete', //删除收货地址

    RegionList: API_BASE_URL + 'region/list', //获取区域列表

    OrderList: API_BASE_URL + 'order/list', //订单列表
    OrderDetail: API_BASE_URL + 'order/detail', //订单详情
    OrderCancel: API_BASE_URL + 'order/cancelOrder', //取消订单
    OrderConfirm: API_BASE_URL + 'order/confirmOrder', //确认收货
    RefundRequest: API_BASE_URL + 'refund/request', //申请售后审核
    RefundStatus: API_BASE_URL + 'refund/status', //本人退款进度

    FootprintList: API_BASE_URL + 'footprint/list', //足迹列表
    FootprintDelete: API_BASE_URL + 'footprint/delete', //删除足迹

    FeedbackAdd: API_BASE_URL + 'feedback/save', //添加反馈
    SmsCode: API_BASE_URL + 'user/smscode', //发送短信
    BindMobile: API_BASE_URL + 'user/bindMobile', //绑定手机
    UserProfile: API_BASE_URL + 'user/profile', //获取个人资料
    UpdateUserProfile: API_BASE_URL + 'user/profile/update', //更新个人资料
    UploadFile: API_BASE_URL + 'upload/upload', //上传个人头像
    Code: API_BASE_URL + 'auth/', //静默登录
    CouponList: API_BASE_URL + 'coupon/list', // 优惠券列表
    GoodsCouponList: API_BASE_URL + 'coupon/listByGoods', // 商品优惠券列表
    OrderQuery: API_BASE_URL + 'pay/query', //微信查询订单状态

    HelpTypeList: API_BASE_URL + 'helpissue/typeList', //查看帮助类型列表
    PearlCatalog: API_BASE_URL + 'pearl-catalog/list', //按珍珠来源及成品品类组合筛选
    MerchantContact: API_BASE_URL + 'merchant/contact', //商家公开客服资料
    RefundList: API_BASE_URL + 'refund/list', //本人售后中心列表
    HelpIssueList: API_BASE_URL + 'helpissue/issueList', //查看问题列表
};
