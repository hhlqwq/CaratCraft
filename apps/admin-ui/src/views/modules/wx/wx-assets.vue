<template>
    <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <el-tab-pane :label="'图片素材（' + assetsCount.imageCount + ')'" name="image" lazy>
            <material-file
                fileType="image"
                ref="imagePanel"
                @change="materialCount"
            ></material-file>
        </el-tab-pane>
        <el-tab-pane :label="'语音素材（' + assetsCount.voiceCount + ')'" name="voice" lazy>
            <material-file
                fileType="voice"
                ref="voicePanel"
                @change="materialCount"
            ></material-file>
        </el-tab-pane>
        <el-tab-pane :label="'视频素材（' + assetsCount.videoCount + ')'" name="video" lazy>
            <material-file
                fileType="video"
                ref="videoPanel"
                @change="materialCount"
            ></material-file>
        </el-tab-pane>
        <el-tab-pane :label="'图文素材（' + assetsCount.newsCount + ')'" name="news" lazy>
            <material-news ref="newsPanel" @change="materialCount"></material-news>
        </el-tab-pane>
    </el-tabs>
</template>
<script>
export default {
    /** 初始化当前组件的响应式界面和表单状态. */
    data() {
        return {
            activeTab: 'image',
            assetsCount: {
                imageCount: '..',
                videoCount: '..',
                voiceCount: '..',
                newsCount: '..',
            },
        };
    },
    components: {
        MaterialFile: () => import('./assets/material-file'),
        MaterialNews: () => import('./assets/material-news'),
    },
    /** 挂载组件后初始化控件并读取所需数据. */
    mounted() {
        this.materialCount();
    },
    methods: {
        /** 切换素材标签页后初始化对应素材面板. */
        handleTabClick(tab, event) {
            this.$nextTick(() => {
                this.$refs[tab.name + 'Panel'].init();
            });
        },
        /** 读取公众号各类素材的数量统计. */
        materialCount() {
            this.$http({
                url: '/manage/wxAssets/materialCount',
            }).then(({data}) => {
                if (data && data.code === 0) {
                    this.assetsCount = data.data;
                } else {
                    this.$message.error(data.msg);
                }
            });
        },
    },
};
</script>
