/** 新旧部署资源切换时提示重新加载, 不自动丢弃商家正在编辑的内容. */
export function watchBuildUpdates(messageBox, target = window) {
    let prompting = false;
    target.addEventListener('vite:preloadError', (event) => {
        event.preventDefault();
        if (prompting) {
            return;
        }
        prompting = true;
        messageBox
            .confirm(
                '页面组件暂时无法加载, 可能是版本更新或网络中断。请先复制未保存内容, 再重新加载页面。',
                '页面需要重新加载',
                {
                    confirmButtonText: '重新加载',
                    cancelButtonText: '稍后处理',
                    type: 'warning',
                }
            )
            .then(() => target.location.reload())
            .catch(() => {})
            .finally(() => {
                prompting = false;
            });
    });
}
