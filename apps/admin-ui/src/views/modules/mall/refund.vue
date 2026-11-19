<template>
    <el-button type="primary" @click="$refs.panel.open()">打开退款管理</el-button>
    <refund-panel ref="panel" />
</template>

<script>
import RefundPanel from './refund-panel';

export default {
    components: {RefundPanel},
    /** 从工作台或菜单进入时读取本岗位的退款队列. */
    activated() {
        this.$refs.panel.open();
    },
};
</script>
