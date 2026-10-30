<template>
    <el-dialog append-to-body :lock-scroll="false" width="30%" center v-model="visible">
        <el-form>
            <h2>布局设置</h2>
            <el-form-item label="导航条类型">
                <el-radio-group v-model="navbarLayoutType">
                    <el-radio label="default" border>default</el-radio>
                    <el-radio label="inverse" border>inverse</el-radio>
                </el-radio-group>
            </el-form-item>
            <el-form-item label="侧边栏皮肤">
                <el-radio-group v-model="sidebarLayoutSkin">
                    <el-radio label="light" border>light</el-radio>
                    <el-radio label="dark" border>dark</el-radio>
                </el-radio-group>
            </el-form-item>
        </el-form>
    </el-dialog>
</template>
<script>
export default {
    /** 初始化当前组件的响应式界面和表单状态. */
    data() {
        return {
            visible: false,
        };
    },
    methods: {
        /** 按传入上下文初始化当前界面的数据和选择状态. */
        init() {
            this.visible = true;
        },
    },
    computed: {
        navbarLayoutType: {
            /** 读取当前控件或页面的响应式绑定值. */
            get() {
                return this.$store.state.common.navbarLayoutType;
            },
            /** 将新值同步到当前控件或页面的绑定状态. */
            set(val) {
                this.$store.commit('common/updateNavbarLayoutType', val);
            },
        },
        sidebarLayoutSkin: {
            /** 读取当前控件或页面的响应式绑定值. */
            get() {
                return this.$store.state.common.sidebarLayoutSkin;
            },
            /** 将新值同步到当前控件或页面的绑定状态. */
            set(val) {
                this.$store.commit('common/updateSidebarLayoutSkin', val);
            },
        },
    },
};
</script>
