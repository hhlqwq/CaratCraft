import http from './httpRequest';

/** 读取服务端公开策略, 前端不能通过环境变量决定生产密码强度. */
export async function loadPasswordPolicy() {
    const {data} = await http({url: '/sys/password-policy', method: 'get', loading: false});
    if (data?.code !== 0) {
        throw new Error('无法加载密码要求，请稍后重试');
    }
    return data.data;
}

/** 只做与已加载服务端规则一致的表单预检, 最终仍由后端校验. */
export function validPassword(value, policy) {
    if (
        !policy ||
        !/^[!-~]+$/.test(value) ||
        value.length < policy.minLength ||
        value.length > policy.maxLength
    ) {
        return false;
    }
    if (policy.local) {
        return true;
    }
    return (
        [/[A-Z]/, /[a-z]/, /[0-9]/, /[^A-Za-z0-9]/].filter((pattern) => pattern.test(value))
            .length >= 3
    );
}
