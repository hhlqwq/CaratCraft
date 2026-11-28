import {ref, onMounted, onBeforeUnmount} from 'vue';
import http from './httpRequest';

/** 平台页面仅调用固定后端前缀, 错误由当前页面明确展示. */
export async function platformRequest(resource, payload, options = {}) {
    const {data} = await http({
        url: '/platform/' + resource,
        method: payload === undefined ? 'get' : 'post',
        data: payload,
        loading: false,
        showError: false,
        ...options,
    });
    if (data?.code !== 0) {
        throw new Error(data?.msg || '平台请求未完成');
    }
    return data.data;
}

/** 可见页面定时读取真实快照, 离开即释放定时器, 不制造历史样本. */
export function usePlatformSnapshot(resource, interval = 0) {
    const data = ref(null);
    const error = ref('');
    const loading = ref(false);
    let timer;
    let active = true;
    /** 一次只读取一个快照, 维护停机期间保留上次结果并显示连接状态. */
    async function load() {
        if (loading.value || !active) {
            return;
        }
        loading.value = true;
        try {
            const result = await platformRequest(resource);
            if (active) {
                data.value = result;
                error.value = '';
            }
        } catch (failure) {
            if (active) {
                error.value =
                    failure.response?.data?.msg || failure.message || '维护中或连接暂不可用';
            }
        } finally {
            loading.value = false;
        }
    }
    onMounted(() => {
        load();
        if (interval) {
            timer = setInterval(() => {
                if (!document.hidden) {
                    load();
                }
            }, interval);
        }
    });
    onBeforeUnmount(() => {
        active = false;
        clearInterval(timer);
    });
    return {data, error, loading, load};
}

/** 缺失容量保留未知, 不将不支持的指标显示成零. */
export function bytes(value) {
    return typeof value === 'number' ? (value / 1024 ** 3).toFixed(2) + ' GiB' : '未获取';
}

/** 时间统一按当前浏览器本地时区显示. */
export function time(value) {
    return value ? new Date(value).toLocaleString('zh-CN', {hour12: false}) : '未获取';
}
