<template>
    <el-dialog title="查看" :close-on-click-modal="false" :lock-scroll="false" v-model="visible">
        <el-form :model="dataForm" ref="dataForm" label-width="80px">
            <el-form-item label="商品id" prop="goodsId">
                <el-input
                    v-model="dataForm.goodsId"
                    :disabled="disabled"
                    placeholder="商品id"
                ></el-input>
            </el-form-item>
            <el-form-item label="图片" prop="imgUrl">
                <el-input
                    v-model="dataForm.imgUrl"
                    :disabled="disabled"
                    placeholder="图片"
                ></el-input>
            </el-form-item>
            <el-form-item label="描述" prop="imgDesc">
                <el-input
                    v-model="dataForm.imgDesc"
                    :disabled="disabled"
                    placeholder="描述"
                ></el-input>
            </el-form-item>
            <el-form-item label="排序" prop="sortOrder">
                <el-input
                    v-model="dataForm.sortOrder"
                    :disabled="disabled"
                    placeholder="排序"
                ></el-input>
            </el-form-item>
        </el-form>
        <template #footer
            ><span class="dialog-footer">
                <el-button @click="visible = false">关闭</el-button>
            </span></template
        >
    </el-dialog>
</template>

<script>
export default {
    /** 初始化当前组件的响应式界面和表单状态. */
    data() {
        return {
            disabled: false,
            visible: false,
            dataForm: {
                id: '',
                goodsId: '',
                imgUrl: '',
                imgDesc: '',
                sortOrder: '',
            },
            dataRule: {
                name: [{required: true, message: '名称不能为空', trigger: 'blur'}],
            },
        };
    },
    methods: {
        /** 按传入上下文初始化当前界面的数据和选择状态. */
        init(id, disabled) {
            this.disabled = true;
            this.dataForm.id = id || '';
            this.visible = true;
            this.$nextTick(() => {
                this.$refs['dataForm'].resetFields();
                if (this.dataForm.id) {
                    this.$http({
                        url: `/mall/goodsgallery/info/${this.dataForm.id}`,
                        method: 'get',
                    }).then(({data}) => {
                        if (data && data.code === 0) {
                            this.dataForm = data.data;
                        }
                    });
                }
            });
        },
        /** 校验当前表单并提交新增或修改请求. */
        dataFormSubmit() {},
    },
};
</script>
