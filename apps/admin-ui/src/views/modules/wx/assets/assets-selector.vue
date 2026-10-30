<template>
    <el-dialog
        title="选择素材"
        v-model="dataVisible"
        :modal="true"
        :lock-scroll="false"
        append-to-body
        @close="onClose"
    >
        <material-news v-if="selectType === 'news'" @selected="onSelect" selectMode></material-news>
        <material-file
            v-else
            :fileType="selectType"
            @selected="onSelect"
            selectMode
        ></material-file>
    </el-dialog>
</template>
<script>
import MaterialFile from './material-file';
import MaterialNews from './material-news';
export default {
    name: 'assets-selector',
    /** 初始化当前组件的响应式界面和表单状态. */
    data: function () {
        return {
            dataVisible: this.visible,
        };
    },
    components: {
        MaterialFile,
        MaterialNews,
    },
    props: {
        selectType: {
            // image、voice、video、news
            type: String,
            default: 'image',
        },
        visible: {
            type: Boolean,
            default: false,
        },
    },
    methods: {
        /** 应用当前选择结果并同步到所属表单. */
        onSelect(itemInfo) {
            this.$emit('selected', itemInfo);
        },
        /** 关闭当前素材编辑界面并通知父组件. */
        onClose() {
            this.$emit('onClose');
        },
    },
};
</script>
