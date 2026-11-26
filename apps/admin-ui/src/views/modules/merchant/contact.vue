<template>
    <div v-loading="loading || saving" class="merchant-contact-admin">
        <el-alert
            title="请填写本商家的客服资料, 不使用参考图中的品牌或二维码. 保存后小程序最长5分钟刷新."
            type="info"
            :closable="false"
        />
        <el-alert v-if="error" class="section" :title="error" type="error" :closable="false" />
        <el-form v-if="loaded" label-position="top" class="contact-form" @submit.prevent="save">
            <el-row :gutter="24">
                <el-col :xs="24" :md="12"
                    ><el-form-item label="商家显示名称"
                        ><el-input
                            v-model="form.merchantName"
                            maxlength="120"
                            placeholder="未填写时显示商家客服"
                            :disabled="!canEdit || loading || saving" /></el-form-item
                ></el-col>
                <el-col :xs="24" :md="12"
                    ><el-form-item label="客服电话"
                        ><el-input
                            v-model="form.phone"
                            maxlength="30"
                            placeholder="可留空, 使用本商家的有效电话号码"
                            :disabled="!canEdit || loading || saving" /></el-form-item
                ></el-col>
                <el-col :xs="24" :md="12"
                    ><el-form-item label="客服微信号"
                        ><el-input
                            v-model="form.wechatId"
                            maxlength="64"
                            placeholder="消费者可复制后自行添加"
                            :disabled="!canEdit || loading || saving" /></el-form-item
                ></el-col>
            </el-row>
            <el-form-item label="客服二维码">
                <el-img
                    v-if="canEdit && isAuth('sys:oss:list')"
                    v-model="form.qrUrl"
                    :disabled="!canEdit || loading || saving"
                />
                <el-image
                    v-else-if="form.qrUrl"
                    :src="form.qrUrl"
                    style="width: 160px; height: 160px"
                    fit="contain"
                />
                <el-button
                    v-if="form.qrUrl && canEdit"
                    :disabled="loading || saving"
                    class="section"
                    @click="form.qrUrl = ''"
                    >移除二维码</el-button
                >
                <div class="hint">
                    必须由当前管理员通过后台安全图片入口上传PNG. 需要同时具备文件上传权限,
                    不会自动复用示例二维码.
                </div>
            </el-form-item>
            <div class="actions">
                <el-button
                    v-if="canEdit"
                    type="primary"
                    :loading="saving"
                    :disabled="loading"
                    @click="save"
                    >保存客服资料</el-button
                ><el-button :disabled="loading || saving" @click="load">重新加载</el-button
                ><span class="hint">当前版本 {{ form.version }}</span>
            </div>
        </el-form>
        <el-empty v-else-if="!loading" description="客服资料加载失败, 可重新加载" />
        <el-button v-if="!loaded && !loading" @click="load">重新加载</el-button>
        <h3 class="history-title">最近修改记录</h3>
        <el-table :data="history" border empty-text="尚无修改记录">
            <el-table-column prop="version" label="版本" width="85" />
            <el-table-column prop="operator_id" label="操作者" min-width="150" />
            <el-table-column prop="create_time" label="时间" min-width="180" />
            <el-table-column label="配置快照" min-width="240"
                ><template #default="scope"
                    ><div class="snapshot">{{ summary(scope.row.snapshot_json) }}</div></template
                ></el-table-column
            >
        </el-table>
    </div>
</template>

<script>
export default {
    /** 表单仅保留客服公开字段及乐观锁版本, 不加载通用系统配置. */
    data() {
        return {form: {}, history: [], loaded: false, loading: false, saving: false, error: ''};
    },
    computed: {
        /** 只读角色可以查看现有配置及修改记录, 不能提交变更. */
        canEdit() {
            return this.isAuth('merchant:contact:update');
        },
    },
    /** 进入页面后读取当前配置和最近修改记录. */
    mounted() {
        this.load();
    },
    methods: {
        /** 刷新同时更新版本, 加载失败时保留已有表单供查看但不改变服务器资料. */
        async load() {
            if (this.loading || this.saving) {
                return;
            }
            this.loading = true;
            this.error = '';
            try {
                const responses = await Promise.all([
                    this.$http({
                        url: '/merchant/contact',
                        method: 'get',
                        showError: false,
                        loading: false,
                    }),
                    this.$http({
                        url: '/merchant/contact/history',
                        method: 'get',
                        showError: false,
                        loading: false,
                    }),
                ]);
                if (responses.some(({data}) => !data || data.code !== 0)) {
                    throw new Error('无法加载客服资料或修改记录');
                }
                this.form = responses[0].data.data;
                this.history = responses[1].data.data;
                this.loaded = true;
            } catch (error) {
                this.error = error.message || '客服资料加载失败, 请重试';
            } finally {
                this.loading = false;
            }
        },
        /** 按原版本提交, 冲突时提示重新加载而不自动覆盖别人的编辑. */
        async save() {
            if (!this.loaded || !this.canEdit || this.saving || this.loading) {
                return;
            }
            this.saving = true;
            this.error = '';
            try {
                const {data} = await this.$http({
                    url: '/merchant/contact',
                    method: 'post',
                    loading: false,
                    data: this.form,
                    showError: false,
                });
                if (!data || data.code !== 0) {
                    throw new Error((data && data.msg) || '保存失败');
                }
                this.form = data.data;
                this.$message.success('客服资料已保存');
                const response = await this.$http({
                    url: '/merchant/contact/history',
                    method: 'get',
                    showError: false,
                    loading: false,
                });
                if (response.data && response.data.code === 0) {
                    this.history = response.data.data;
                }
            } catch (error) {
                this.error =
                    (error.response && error.response.data && error.response.data.msg) ||
                    error.message ||
                    '保存失败';
            } finally {
                this.saving = false;
            }
        },
        /** 历史快照只转成纯文本, 不执行管理员输入的HTML. */
        summary(text) {
            try {
                const data = JSON.parse(text || '{}');
                return [
                    data.merchantName || '未填名称',
                    data.phone || '未填电话',
                    data.wechatId || '未填微信号',
                    data.qrUrl ? '有二维码' : '无二维码',
                ].join(' / ');
            } catch (error) {
                return '历史记录格式异常';
            }
        },
    },
};
</script>

<style scoped>
.merchant-contact-admin {
    max-width: 1200px;
}
.contact-form {
    margin-top: 24px;
    max-width: 900px;
}
.section {
    margin-top: 16px;
}
.hint {
    color: #777;
    font-size: 13px;
    line-height: 1.8;
}
.actions {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 12px;
}
.history-title {
    margin: 32px 0 16px;
    font-size: 18px;
}
.snapshot {
    overflow-wrap: anywhere;
    line-height: 1.8;
}
</style>
