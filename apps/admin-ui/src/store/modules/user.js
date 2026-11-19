export default {
    namespaced: true,
    state: {
        id: 0,
        name: '',
    },
    mutations: {
        /** 同步当前登录用户标识到共享状态. */
        updateId(state, id) {
            state.id = id;
        },
        /** 同步当前登录用户名称到共享状态. */
        updateName(state, name) {
            state.name = name;
        },
    },
};
