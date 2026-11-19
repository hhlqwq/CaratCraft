<template>
    <section class="staff-editor">
        <el-button @click="returnToList">返回列表</el-button>
        <h1>{{ dataForm.id ? '编辑员工' : '新建员工' }}</h1>
        <el-alert v-if="error" :title="error" type="error" :closable="false" />
        <el-form
            :model="dataForm"
            v-loading="loading"
            :rules="dataRule"
            ref="dataForm"
            @submit.prevent="dataFormSubmit"
            label-width="80px"
        >
            <el-form-item label="用户名" prop="userName">
                <el-input
                    v-model="dataForm.userName"
                    placeholder="登录帐号"
                    class="width200"
                ></el-input>
            </el-form-item>
            <el-form-item v-if="!dataForm.id" label="初始密码" prop="initialPassword">
                <el-input
                    v-model="dataForm.initialPassword"
                    type="password"
                    show-password
                    autocomplete="new-password"
                    placeholder="为员工单独设置，12到128位"
                    class="width200"
                />
            </el-form-item>
            <el-form-item label="状态" prop="status">
                <el-radio-group v-model="dataForm.status" class="width200">
                    <el-radio-button :label="0">禁用</el-radio-button>
                    <el-radio-button :label="1">正常</el-radio-button>
                </el-radio-group>
            </el-form-item>
            <el-form-item label="性别" prop="sex">
                <el-dict code="SEX" v-model="dataForm.sex"></el-dict>
            </el-form-item>
            <el-form-item label="真实姓名" prop="realName">
                <el-input
                    v-model="dataForm.realName"
                    placeholder="真实姓名"
                    class="width200"
                ></el-input>
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
                <el-input v-model="dataForm.email" placeholder="邮箱" class="width200"></el-input>
            </el-form-item>
            <el-form-item label="手机号" prop="mobile">
                <el-input
                    v-model="dataForm.mobile"
                    placeholder="手机号"
                    class="width200"
                ></el-input>
            </el-form-item>
            <el-form-item label="岗位" prop="roleIdList">
                <el-select
                    v-model="dataForm.roleIdList"
                    multiple
                    clearable
                    filterable
                    placeholder="选择员工岗位"
                >
                    <el-option
                        v-for="role in roleList"
                        :key="role.roleId"
                        :label="role.roleName"
                        :value="role.roleId"
                    >
                    </el-option>
                </el-select>
            </el-form-item>
        </el-form>
        <div class="staff-actions">
            <el-button @click="returnToList">取消</el-button>
            <el-button
                type="primary"
                :loading="saving"
                :disabled="loading || !loaded"
                @click="dataFormSubmit"
                >保存</el-button
            >
        </div>
    </section>
</template>

<script>
export default {
    /** 新员工归属由服务端决定, 表单仅维护员工资料和岗位. */
    data() {
        return {
            loading: true,
            loaded: false,
            saving: false,
            error: '',
            original: '',
            roleList: [],
            dataForm: {
                id: this.$route.params.id || '',
                userName: '',
                sex: '',
                realName: '',
                initialPassword: '',
                email: '',
                mobile: '',
                roleIdList: [],
                status: 1,
            },
            dataRule: {
                initialPassword: [
                    {
                        required: true,
                        pattern: /^[!-~]{12,128}$/,
                        message: '请单独设置12到128位员工密码',
                        trigger: 'blur',
                    },
                ],
                userName: [
                    {
                        required: true,
                        message: '用户名不能为空',
                        trigger: 'blur',
                    },
                ],
                sex: [
                    {
                        required: true,
                        message: '性别不能为空',
                        trigger: 'blur',
                    },
                ],
                realName: [
                    {
                        required: true,
                        message: '真实姓名不能为空',
                        trigger: 'blur',
                    },
                ],
            },
        };
    },
    computed: {
        /** 只有成功加载后发生的表单变更才需要离开确认. */
        dirty() {
            return this.loaded && this.original !== JSON.stringify(this.dataForm);
        },
    },
    /** 加载岗位和可编辑资料, 注册浏览器刷新时的未保存保护. */
    mounted() {
        this.load();
        window.addEventListener('beforeunload', this.warnBeforeUnload);
    },
    /** 页面销毁后移除浏览器监听. */
    beforeUnmount() {
        window.removeEventListener('beforeunload', this.warnBeforeUnload);
    },
    /** 取消离开时保留脏表单, 普通无修改页面直接切换. */
    async beforeRouteLeave(to) {
        if (!this.dirty || to.name === 'login') {
            return true;
        }
        try {
            await this.$confirm('员工资料有未保存的修改，是否离开？', '未保存的修改', {
                confirmButtonText: '离开',
                cancelButtonText: '继续编辑',
                type: 'warning',
            });
            return true;
        } catch {
            return false;
        }
    },
    methods: {
        /** 浏览器刷新和关闭仅在表单有修改时触发原生提示. */
        warnBeforeUnload(event) {
            if (this.dirty) {
                event.preventDefault();
                event.returnValue = '';
            }
        },
        /** 同时获取可分配岗位及已有员工的原始资料. */
        async load() {
            try {
                const requests = [this.$http({url: '/sys/role/select', method: 'get'})];
                if (this.dataForm.id) {
                    requests.push(
                        this.$http({url: `/sys/user/info/${this.dataForm.id}`, method: 'get'})
                    );
                }
                const [roles, detail] = await Promise.all(requests);
                if (roles.data?.code !== 0 || (detail && detail.data?.code !== 0)) {
                    throw new Error(detail?.data?.msg || roles.data?.msg || '员工资料加载失败');
                }
                this.roleList = roles.data.data || [];
                if (detail) {
                    const employee = detail.data.data;
                    this.dataForm = {
                        ...this.dataForm,
                        userName: employee.userName,
                        sex: String(employee.sex),
                        realName: employee.realName,
                        email: employee.email || '',
                        mobile: employee.mobile || '',
                        roleIdList: employee.roleIdList || [],
                        status: employee.status,
                    };
                }
                this.original = JSON.stringify(this.dataForm);
                this.loaded = true;
            } catch (error) {
                this.error = error.response?.data?.msg || error.message || '员工资料加载失败';
            } finally {
                this.loading = false;
            }
        },
        /** 提交员工资料及独立初始密码, 不发送机构信息. */
        async dataFormSubmit() {
            if (this.saving || !this.loaded) {
                return;
            }
            this.saving = true;
            this.error = '';
            try {
                await this.$refs.dataForm.validate();
                const {id, initialPassword, ...profile} = this.dataForm;
                const {data} = await this.$http({
                    url: `/sys/user/${id ? 'update' : 'save'}`,
                    method: 'post',
                    showError: false,
                    data: {
                        ...profile,
                        userId: id || undefined,
                        password: id ? undefined : initialPassword,
                    },
                });
                if (data?.code !== 0) {
                    throw new Error(data?.msg || '员工资料保存失败');
                }
                this.dataForm.initialPassword = '';
                this.original = JSON.stringify(this.dataForm);
                this.$message.success(
                    id ? '员工资料已保存' : '员工已创建，请将用户名和初始密码单独交给本人'
                );
                await this.returnToList();
            } catch (error) {
                this.error = error.response?.data?.msg || error.message || '请核对员工资料';
            } finally {
                this.saving = false;
            }
        },
        /** 返回员工列表并保留已应用的筛选和分页. */
        returnToList() {
            return this.$router.push({name: 'sys-user', query: this.$route.query});
        },
    },
};
</script>

<style scoped>
.staff-editor {
    max-width: 540px;
}
.staff-editor h1 {
    margin: 20px 0;
    font-size: 24px;
}
.staff-editor :deep(.el-input),
.staff-editor :deep(.el-select),
.staff-editor :deep(.width200.el-input) {
    width: 100%;
}
.staff-actions {
    display: flex;
    justify-content: flex-end;
}
</style>
