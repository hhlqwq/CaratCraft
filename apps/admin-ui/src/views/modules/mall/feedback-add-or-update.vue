<template>
    <el-dialog title="查看" :close-on-click-modal="false" :lock-scroll="false" v-model="visible">
        <el-descriptions :column="3" border>
            <el-descriptions-item>
                <template #label> 手机 </template>
                {{ dataForm.mobile }}
            </el-descriptions-item>
            <el-descriptions-item>
                <template #label> 反馈时间 </template>
                {{ dataForm.addTime }}
            </el-descriptions-item>
            <el-descriptions-item>
                <template #label> 反馈类型 </template>
                <el-dict code="FEED_TYPE" disabled v-model="dataForm.feedType"></el-dict>
            </el-descriptions-item>
            <el-descriptions-item>
                <template #label> 详细内容 </template>
                {{ dataForm.content }}
            </el-descriptions-item>
        </el-descriptions>
        <template #footer
            ><span class="dialog-footer">
                <el-button @click="visible = false">关闭</el-button>
                <el-button
                    v-if="isAuth('mall:feedback:update')"
                    :disabled="!dataForm.msgId || !dataForm.staffReply"
                    :loading="saving"
                    type="primary"
                    @click="saveReply"
                    >保存并标为已处理</el-button
                >
            </span></template
        >
    </el-dialog>
</template>

<script>
export default {
    /** 初始化当前组件的响应式界面和表单状态. */
    data() {
        return {
            visible: false,
            saving: false,
            dataForm: {
                id: '',
                userId: '',
                mobile: '',
                feedType: '',
                content: '',
                status: '',
                addTime: '',
            },
        };
    },
    methods: {
        /** 由独立反馈处理权限保存回复, 只提交反馈编号和处理说明. */
        async saveReply() {
            if (this.saving) {
                return;
            }
            this.saving = true;
            try {
                const {data} = await this.$http({
                    url: '/mall/feedback/update',
                    method: 'post',
                    data: {msgId: this.dataForm.msgId, staffReply: this.dataForm.staffReply},
                });
                if (data.code === 0) {
                    this.visible = false;
                    this.$emit('refreshDataList');
                }
            } finally {
                this.saving = false;
            }
        },
        /** 按传入上下文初始化当前界面的数据和选择状态. */
        init(id) {
            this.dataForm.id = id || '';
            this.visible = true;
            this.$nextTick(() => {
                if (this.dataForm.id) {
                    this.$http({
                        url: `/mall/feedback/info/${this.dataForm.id}`,
                        method: 'get',
                    }).then(({data}) => {
                        if (data && data.code === 0) {
                            this.dataForm = data.data;
                        }
                    });
                }
            });
        },
    },
};
</script>
