import {pagePresentation, workspaceGroups} from '@/router/workspace-navigation';

const subjects = {
    'mall/order': '订单',
    'mall/specification': '规格名称',
    'mall/goods-recycle': '商品回收站',
    'mall/refund': '退款',
    'mall/aftersale': '售后',
    'mall/feedback': '客户反馈',
    'merchant/contact': '客服资料',
    'mall/specification': '商品规格',
    'custom/component-stock': '材料库存',
    'community/posts': '社区内容',
    'oss/oss': '上传文件',
};

const actionNames = {
    新增: '新增',
    修改: '编辑',
    删除: '删除',
    查看: '查看',
};

const permissionNames = {
    'mall:goods:recycle:delete': '永久删除商品',
    'mall:order:ship': '订单发货',
    'mall:refund:reconcile': '对账并更新退款状态',
    'custom:work-order:update': '更新生产工单状态',
    'custom:stock:update': '编辑材料库存',
    'community:post:moderate': '审核与下架社区内容',
};

const retiredActions = new Set(['mall:order:save', 'mall:order:update', 'mall:order:delete']);

/** 沿用侧栏业务分类, 将服务端授权目录转换为可勾选的中文权限. */
export function rolePermissionGroups(catalog) {
    const pages = new Map(
        catalog
            .filter((item) => item.url)
            .map((item) => [item.menuId, {...item, url: item.url.replace(/^\//, '')}])
            .filter(
                ([, page]) =>
                    /^(?:mall|custom|community|merchant)\//.test(page.url) || page.url === 'oss/oss'
            )
    );
    const groups = new Map(workspaceGroups(catalog).map(({title}) => [title, []]));
    for (const item of catalog) {
        if (!item.perms || retiredActions.has(item.perms)) {
            continue;
        }
        const page = pages.get(item.url ? item.menuId : item.parentId);
        if (!page || (!item.url && item.perms === page.perms)) {
            continue;
        }
        const {title, group} = pagePresentation(page);
        const subject = subjects[page.url] || title.replace(/管理$/, '');
        const label = item.url
            ? '查看' + subject
            : permissionNames[item.perms] ||
              (actionNames[item.name] ? actionNames[item.name] + subject : item.name);
        if (groups.has(group) && /[\u3400-\u9fff]/.test(label) && !/\w+:\w+:/.test(label)) {
            groups.get(group).push({
                menuId: item.menuId,
                label,
                actions: item.perms.split(',').map((action) => action.trim()),
            });
        }
    }
    return Array.from(groups, ([title, items]) => ({title, items})).filter(({items}) => items.length);
}
