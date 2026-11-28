<template>
    <main>
        <el-tabs v-model="tab">
            <el-tab-pane label="商家管理员" name="accounts">
                <el-form inline @submit.prevent="search">
                    <el-form-item label="登录账号"
                        ><el-input v-model.trim="filters.account" clearable
                    /></el-form-item>
                    <el-form-item label="姓名"
                        ><el-input v-model.trim="filters.name" clearable
                    /></el-form-item>
                    <el-form-item label="账号状态"
                        ><el-select v-model="filters.status" clearable style="width: 130px"
                            ><el-option :value="1" label="已启用" /><el-option
                                :value="0"
                                label="已停用" /></el-select
                    ></el-form-item>
                    <el-button native-type="submit">查询</el-button>
                    <el-button type="primary" @click="open()">创建商家管理员</el-button>
                </el-form>
                <el-alert v-if="error" :title="error" type="error" :closable="false" />
                <el-table :data="accounts" v-loading="loading" empty-text="暂无商家管理员">
                    <el-table-column prop="userName" label="登录账号" />
                    <el-table-column prop="realName" label="姓名" />
                    <el-table-column prop="mobile" label="手机号" min-width="130" />
                    <el-table-column
                        prop="email"
                        label="邮箱"
                        min-width="165"
                        show-overflow-tooltip
                    />
                    <el-table-column label="状态"
                        ><template #default="{row}">{{
                            row.status === 1 ? '已启用' : '已停用'
                        }}</template></el-table-column
                    >
                    <el-table-column label="创建时间" min-width="175"
                        ><template #default="{row}">{{
                            time(row.createTime)
                        }}</template></el-table-column
                    >
                    <el-table-column label="最后登录" min-width="175"
                        ><template #default="{row}">{{
                            row.lastLoginAt ? time(row.lastLoginAt) : '尚未登录'
                        }}</template></el-table-column
                    >
                    <el-table-column label="操作" min-width="220"
                        ><template #default="{row}">
                            <el-button link :disabled="busy" @click="open(row)"
                                >查看与编辑</el-button
                            >
                            <el-button link :disabled="busy" @click="status(row)">{{
                                row.status === 1 ? '停用' : '启用'
                            }}</el-button>
                            <el-button link :disabled="busy" @click="reset(row)"
                                >重置密码</el-button
                            >
                        </template></el-table-column
                    >
                </el-table>
                <el-pagination
                    v-model:current-page="page"
                    :page-size="20"
                    :total="total"
                    @current-change="load"
                />
            </el-tab-pane>
            <el-tab-pane label="工作室基本信息" name="workspace">
                <el-form label-width="100px" @submit.prevent="rename">
                    <el-form-item label="工作室名称"
                        ><el-input
                            v-model.trim="workspace.name"
                            maxlength="50"
                            style="max-width: 360px"
                    /></el-form-item>
                    <el-form-item
                        ><el-button native-type="submit" type="primary" :loading="busy"
                            >保存名称</el-button
                        ></el-form-item
                    >
                </el-form>
                <p>名称变更沿用当前工作室，既有账号与历史业务归属保持一致。</p>
            </el-tab-pane>
        </el-tabs>
        <el-dialog
            v-model="editing"
            :title="form.userId ? '商家管理员资料' : '创建商家管理员'"
            width="min(90vw, 500px)"
        >
            <el-form :model="form" label-width="90px" @submit.prevent="save">
                <el-form-item label="登录账号" required
                    ><el-input
                        v-model.trim="form.userName"
                        :disabled="!!form.userId"
                        maxlength="50"
                /></el-form-item>
                <el-form-item label="姓名" required
                    ><el-input v-model.trim="form.realName" maxlength="64"
                /></el-form-item>
                <el-form-item label="手机号"><el-input v-model.trim="form.mobile" /></el-form-item>
                <el-form-item label="邮箱"><el-input v-model.trim="form.email" /></el-form-item>
                <p>联系方式选填。账号自动关联当前工作室，首次登录须改密。</p>
                <el-alert v-if="formError" :title="formError" type="error" :closable="false" />
                <el-button @click="editing = false">取消</el-button
                ><el-button native-type="submit" type="primary" :loading="busy">保存</el-button>
            </el-form>
        </el-dialog>
        <el-dialog
            v-model="showCredential"
            title="临时密码"
            width="min(90vw, 450px)"
            @closed="credential = ''"
        >
            <p>只显示本次响应，24小时内可用，请单独交给对应商家管理员。首次登录须修改。</p>
            <el-input :model-value="credential" type="password" show-password readonly />
        </el-dialog>
    </main>
</template>
<script setup>
import {ref, reactive, onMounted} from 'vue';
import {ElMessageBox} from 'element-plus';
import {platformRequest, time} from '@/utils/platform-console';
const tab = ref('accounts');
const filters = reactive({account: '', name: '', status: ''});
const page = ref(1);
const total = ref(0);
const accounts = ref([]);
const workspace = reactive({name: ''});
const loading = ref(false);
const busy = ref(false);
const error = ref('');
const formError = ref('');
const editing = ref(false);
const form = ref({});
const credential = ref('');
const showCredential = ref(false);
/** 查询只取账号资料和分页, 不混入商家经营数据. */
async function load() {
    loading.value = true;
    try {
        const result = await platformRequest('merchant/list', undefined, {
            params: {
                ...filters,
                status: filters.status === '' ? undefined : filters.status,
                page: page.value,
            },
        });
        accounts.value = result.records;
        total.value = result.total;
        error.value = '';
    } catch (failure) {
        error.value = failure.response?.data?.msg || failure.message;
    } finally {
        loading.value = false;
    }
}
/** 更换筛选项后回到第一页. */
function search() {
    page.value = 1;
    load();
}
/** 表单只包含允许编辑的基本资料. */
function open(row) {
    form.value = row
        ? {
              userId: row.userId,
              userName: row.userName,
              realName: row.realName,
              mobile: row.mobile || '',
              email: row.email || '',
          }
        : {userName: '', realName: '', mobile: '', email: ''};
    formError.value = '';
    editing.value = true;
}
/** 新建交付临时凭据, 编辑不触碰密码或账号层级. */
async function save() {
    busy.value = true;
    try {
        if (!form.value.userName || !form.value.realName) {
            throw new Error('请填写登录账号和姓名');
        }
        const update = {
            userId: form.value.userId,
            realName: form.value.realName,
            mobile: form.value.mobile,
            email: form.value.email,
        };
        const result = await platformRequest(
            'merchant/' + (form.value.userId ? 'update' : 'create'),
            form.value.userId ? update : form.value
        );
        editing.value = false;
        if (result?.temporaryPassword) {
            credential.value = result.temporaryPassword;
            showCredential.value = true;
        }
        await load();
    } catch (failure) {
        formError.value = failure.response?.data?.msg || failure.message;
    } finally {
        busy.value = false;
    }
}
/** 停用会撤销现有会话, 操作前明确确认. */
async function status(row) {
    try {
        await ElMessageBox.confirm(
            (row.status === 1 ? '停用并撤销当前会话' : '重新启用') + '此商家管理员？',
            '账号状态'
        );
        await platformRequest('merchant/status', {userId: row.userId, active: row.status !== 1});
        await load();
    } catch (failure) {
        if (failure instanceof Error) {
            error.value = failure.message;
        }
    }
}
/** 重置不展示旧密码, 只接收新的限时临时凭据. */
async function reset(row) {
    try {
        await ElMessageBox.confirm('重置后撤销当前会话，确认继续？', '重置密码');
        const result = await platformRequest('merchant/reset', {userId: row.userId});
        credential.value = result.temporaryPassword;
        showCredential.value = true;
    } catch (failure) {
        if (failure instanceof Error) {
            error.value = failure.message;
        }
    }
}
/** 更新工作室名称, 不提交归属或机构编码. */
async function rename() {
    busy.value = true;
    try {
        await platformRequest('workspace', {name: workspace.name});
    } catch (failure) {
        error.value = failure.message;
        tab.value = 'accounts';
    } finally {
        busy.value = false;
    }
}
onMounted(async () => {
    await load();
    try {
        Object.assign(workspace, await platformRequest('workspace'));
    } catch (failure) {
        error.value = failure.message;
    }
});
</script>
