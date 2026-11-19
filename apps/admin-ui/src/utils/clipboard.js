const states = new WeakMap();

/** 使用用户点击触发系统剪贴板, 不依赖Vue2插件或外部脚本. */
async function copy(event) {
    const state = states.get(event.currentTarget);
    try {
        if (!navigator.clipboard || !window.isSecureContext) {
            throw new Error('请使用HTTPS连接后复制');
        }
        await navigator.clipboard.writeText(String(state.copy || ''));
        if (state.success) {
            state.success({text: state.copy});
        }
    } catch (error) {
        if (state.error) {
            state.error(error);
        } else {
            state.instance.$message.error(error.message || '复制失败');
        }
    }
}

/** 多个参数共用一次点击监听, 更新时替换当前值. */
function bind(element, binding) {
    if (!states.has(element)) {
        states.set(element, {instance: binding.instance});
        element.addEventListener('click', copy);
    }
    states.get(element)[binding.arg] = binding.value;
}

export default {
    mounted: bind,
    updated: bind,
    /** 销毁组件时释放监听和关联状态. */
    unmounted(element) {
        element.removeEventListener('click', copy);
        states.delete(element);
    },
};
