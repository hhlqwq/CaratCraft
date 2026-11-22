<template>
    <main v-loading="loading">
        <h1>商家管理员</h1>
        <el-button type="primary" @click="openCreate">创建商家管理员</el-button>
        <el-alert v-if="error" :title="error" type="error" :closable="false" />
        <el-table :data="accounts" empty-text="暂无商家管理员" style="margin-top: 16px">
            <el-table-column prop="userName" label="登录账号" />
            <el-table-column prop="realName" label="姓名" />
            <el-table-column prop="orgNo" label="工作室" />
            <el-table-column label="状态">
                <template #default="{row}">{{ row.status === 1 ? '已启用' : '已停用' }}</template>
            </el-table-column>
            <el-table-column label="操作" min-width="180">
                <template #default="{row}">
                    <el-button link type="primary" :disabled="busy" @click="changeStatus(row)">
                        {{ row.status === 1 ? '停用' : '恢复' }}
                    </el-button>
                    <el-button link type="primary" :disabled="busy" @click="reset(row)"
                        >重置密码</el-button
                    >
                </template>
            </el-table-column>
        </el-table>
        <el-pagination
            v-model:current-page="page"
            :page-size="20"
            :total="total"
            @current-change="load"
        />
        <el-dialog v-model="creating" title="创建商家管理员" width="min(90vw, 480px)">
            <el-form :model="form" label-width="85px" @submit.prevent="create">
                <el-form-item label="登录账号"
                    ><el-input v-model.trim="form.userName" maxlength="50"
                /></el-form-item>
                <el-form-item label="姓名"
                    ><el-input v-model.trim="form.realName" maxlength="64"
                /></el-form-item>
                <el-form-item label="手机号"><el-input v-model.trim="form.mobile" /></el-form-item>
                <el-form-item label="邮箱"><el-input v-model.trim="form.email" /></el-form-item>
                <el-alert v-if="formError" :title="formError" type="error" :closable="false" />
                <p>填写手机号或邮箱。账号自动关联默认工作室。</p>
                <el-button native-type="submit" type="primary" :loading="busy">创建</el-button>
            </el-form>
        </el-dialog>
        <el-dialog
            v-model="showCredential"
            title="临时密码"
            width="min(90vw, 450px)"
            @closed="clearCredential"
        >
            <p>只显示一次，24 小时内可用。请单独交给对应商家管理员，首次登录时须修改。</p>
            <el-input :model-value="credential" readonly />
        </el-dialog>
    </main>
</template>

<script>
export default {
    /** 初始化平台账号列表、开户及临时凭据面板. */
    data() {
        return {
            accounts: [],
            page: 1,
            total: 0,
            loading: false,
            busy: false,
            error: '',
            formError: '',
            creating: false,
            form: {},
            showCredential: false,
            credential: '',
        };
    },
    /** 进入平台工作区后加载账号列表. */
    mounted() {
        this.load();
    },
    methods: {
        /** 通过专用平台接口执行账号操作. */
        async request(action, payload) {
            const {data} = await this.$http({
                url: '/platform/merchant/' + action,
                method: payload ? 'post' : 'get',
                params: payload ? undefined : {page: this.page},
                data: payload,
                showError: false,
            });
            if (data?.code !== 0) {
                throw new Error(data?.msg || '商家管理员操作失败');
            }
            return data.data;
        },
        /** 刷新账号数据及分页总数. */
        async load() {
            this.loading = true;
            this.error = '';
            try {
                const result = await this.request('list');
                this.accounts = result.records || [];
                this.total = result.total || 0;
            } catch (error) {
                this.error = error.response?.data?.msg || error.message;
            } finally {
                this.loading = false;
            }
        },
        /** 打开一份清空的开户表单. */
        openCreate() {
            this.form = {userName: '', realName: '', mobile: '', email: ''};
            this.formError = '';
            this.creating = true;
        },
        /** 提交开户资料并展示后端签发的一次性响应. */
        async create() {
            if (this.busy) {
                return;
            }
            this.busy = true;
            this.formError = '';
            try {
                if (
                    !this.form.userName ||
                    !this.form.realName ||
                    (!this.form.mobile && !this.form.email)
                ) {
                    throw new Error('请填写登录账号、姓名和至少一种联系方式');
                }
                const result = await this.request('create', this.form);
                this.creating = false;
                this.credential = result.temporaryPassword;
                this.showCredential = true;
                await this.load();
            } catch (error) {
                this.formError = error.response?.data?.msg || error.message;
            } finally {
                this.busy = false;
            }
        },
        /** 对单个商家管理员切换启停状态. */
        async changeStatus(row) {
            this.busy = true;
            try {
                await this.request('status', {userId: row.userId, active: row.status !== 1});
                await this.load();
            } catch (error) {
                this.error = error.response?.data?.msg || error.message;
            } finally {
                this.busy = false;
            }
        },
        /** 经确认撤销旧凭据并显示新临时密码. */
        async reset(row) {
            try {
                await this.$confirm('重置后会撤销当前会话，是否继续？', '重置密码');
            } catch {
                return;
            }
            this.busy = true;
            try {
                const result = await this.request('reset', {userId: row.userId});
                this.credential = result.temporaryPassword;
                this.showCredential = true;
            } catch (error) {
                this.error = error.response?.data?.msg || error.message;
            } finally {
                this.busy = false;
            }
        },
        /** 关闭凭据面板后从页面内存清除密码. */
        clearCredential() {
            this.credential = '';
        },
    },
};
</script>
