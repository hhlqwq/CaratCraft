<template>
    <el-dialog
        :title="requiredChange ? '修改临时密码' : '修改密码'"
        :lock-scroll="false"
        v-model="visible"
        :append-to-body="true"
        :show-close="!requiredChange"
        :close-on-click-modal="!requiredChange"
        :close-on-press-escape="!requiredChange"
    >
        <el-alert
            v-if="requiredChange"
            title="修改临时密码后，请使用新密码重新登录。"
            type="info"
            :closable="false"
        />
        <el-form
            :model="dataForm"
            :rules="dataRule"
            ref="dataForm"
            @keyup.enter="dataFormSubmit()"
            label-width="80px"
        >
            <el-form-item v-if="userName" label="账号">
                <span>{{ userName }}</span>
            </el-form-item>
            <el-form-item label="原密码" prop="password">
                <el-input type="password" v-model="dataForm.password"></el-input>
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
                <el-input type="password" v-model="dataForm.newPassword"></el-input>
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
                <el-input type="password" v-model="dataForm.confirmPassword"></el-input>
            </el-form-item>
        </el-form>
        <template #footer
            ><span class="dialog-footer">
                <el-button v-if="requiredChange" @click="exitChange">返回登录</el-button>
                <el-button v-else @click="visible = false">取消</el-button>
                <el-button type="primary" @click="dataFormSubmit()">确定</el-button>
            </span></template
        >
    </el-dialog>
</template>

<script>
import {clearLoginInfo} from '@/utils';

export default {
    props: {
        requiredChange: {type: Boolean, default: false},
    },
    /** 强制改密入口直接打开改密表单. */
    mounted() {
        if (this.requiredChange) {
            this.init();
        }
    },
    /** 初始化当前组件的响应式界面和表单状态. */
    data() {
        const validateConfirmPassword = (rule, value, callback) => {
            if (this.dataForm.newPassword !== value) {
                callback(new Error('确认密码与新密码不一致'));
            } else {
                callback();
            }
        };
        return {
            visible: false,
            dataForm: {
                password: '',
                newPassword: '',
                confirmPassword: '',
            },
            dataRule: {
                password: [
                    {
                        required: true,
                        message: '原密码不能为空',
                        trigger: 'blur',
                    },
                ],
                newPassword: [
                    {
                        required: true,
                        pattern: /^(?!123456$)[!-~]{6,32}$/,
                        message: '请输入6到32位其他密码，不含空格',
                        trigger: 'blur',
                    },
                ],
                confirmPassword: [
                    {
                        required: true,
                        message: '确认密码不能为空',
                        trigger: 'blur',
                    },
                    {
                        validator: validateConfirmPassword,
                        trigger: 'blur',
                    },
                ],
            },
        };
    },
    computed: {
        userName: {
            /** 读取当前控件或页面的响应式绑定值. */
            get() {
                return this.$store.state.user.name;
            },
        },
    },
    methods: {
        // 初始化
        init() {
            this.visible = true;
            this.$nextTick(() => {
                this.$refs['dataForm'].resetFields();
            });
        },
        /** 退出当前临时会话并回到登录. */
        async exitChange() {
            try {
                await this.$http({url: '/sys/logout', method: 'post', showError: false});
            } catch {
                // 本地会话始终退出, 后端过期令牌会自动失效.
            } finally {
                clearLoginInfo();
                this.$router.replace({name: 'login'});
            }
        },
        // 表单提交
        dataFormSubmit() {
            this.$refs['dataForm'].validate((valid) => {
                if (valid) {
                    this.$http({
                        url: '/sys/user/password',
                        method: 'post',
                        data: {
                            password: this.dataForm.password,
                            newPassword: this.dataForm.newPassword,
                        },
                    }).then(({data}) => {
                        if (data && data.code === 0) {
                            this.$message({
                                message: '操作成功',
                                type: 'success',
                                duration: 1500,
                            });
                            this.visible = false;
                            this.$nextTick(() => {
                                clearLoginInfo();
                                this.$router.replace({name: 'login'});
                            });
                        }
                    });
                }
            });
        },
    },
};
</script>
