const sections = [
    {
        title: '商品与材料',
        icon: 'goods',
        urls: [
            'mall/goods',
            'mall/goods-recycle',
            'mall/category',
            'mall/brand',
            'mall/attribute',
            'mall/attributecategory',
            'mall/specification',
            'mall/goodsissue',
            'custom/configuration',
            'custom/component-stock',
        ],
    },
    {
        title: '订单与制作',
        icon: 'orders',
        urls: ['mall/order', 'custom/work-order', 'mall/shipping'],
    },
    {
        title: '客服与售后',
        icon: 'feedback',
        urls: [
            'mall/aftersale',
            'mall/refund',
            'mall/feedback',
            'merchant/contact',
            'mall/helptype',
            'mall/helpissue',
        ],
    },
    {title: '社区', icon: 'community', urls: ['community/posts', 'mall/comment']},
    {
        title: '系统设置',
        icon: 'setting',
        urls: [
            'sys/user',
            'sys/role',
            'sys/log',
            'sys/menu',
            'sys/config',
            'sys/dict',
            'sys/dictgroup',
            'sys/smslog',
            'merchant/legal',
            'oss/oss',
        ],
    },
];

const labels = {'sys/user': '员工账号', 'mall/order': '订单列表'};

/** 展开服务端已授权菜单, 保留原路由标识和来源. */
export function authorizedPages(menus) {
    return menus.flatMap((menu) => {
        const own = menu.url ? [{...menu, url: menu.url.replace(/^\//, '')}] : [];
        return own.concat(authorizedPages(menu.list || []));
    });
}

/** 为已授权页面提供名称和分组, 隐藏入口仍能按原权限直达. */
export function pagePresentation(page) {
    return {
        title: labels[page.url] || page.name,
        group: sections.find((section) => section.urls.includes(page.url))?.title,
    };
}

/** 从当前账号的菜单生成工作室导航, 去掉空分组. */
export function workspaceGroups(menus) {
    const pages = new Map(authorizedPages(menus).map((page) => [page.url, page]));
    return sections
        .map((section) => ({
            ...section,
            pages: section.urls
                .filter((url) => pages.has(url))
                .map((url) => ({
                    name: url.replace('/', '-'),
                    title: pagePresentation(pages.get(url)).title,
                })),
        }))
        .filter((section) => section.pages.length);
}
