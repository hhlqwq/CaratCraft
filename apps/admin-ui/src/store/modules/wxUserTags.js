export default {
    namespaced: true,
    state: {
        tags: [],
    },
    mutations: {
        /** 更新缓存中的公众号用户标签列表. */
        updateTags(state, tags) {
            state.tags = tags;
        },
    },
};
