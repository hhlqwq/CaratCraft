<template>
    <el-dialog
        v-model="visible"
        :title="role.id ? '编辑岗位' : '新增岗位'"
        :close-on-click-modal="false"
    >
        <el-form v-loading="loading" :model="role" label-width="90px" @submit.prevent="save">
            <el-form-item label="岗位名称">
                <el-input v-model.trim="role.name" maxlength="100" />
            </el-form-item>
            <el-form-item label="备注">
                <el-input v-model.trim="role.remark" type="textarea" maxlength="200" />
            </el-form-item>
            <el-form-item label="功能权限">
                <el-select
                    v-model="role.menuIds"
                    multiple
                    filterable
                    clearable
                    placeholder="选择岗位需要的功能"
                >
                    <el-option
                        v-for="item in permissions"
                        :key="item.menuId"
                        :label="item.name + ' · ' + item.perms"
                        :value="item.menuId"
                    />
                </el-select>
            </el-form-item>
            <el-alert v-if="error" :title="error" type="error" :closable="false" />
            <el-button @click="visible = false">取消</el-button>
            <el-button
                native-type="submit"
                type="primary"
                :loading="saving"
                :disabled="loading || !ready"
                >保存</el-button
            >
        </el-form>
    </el-dialog>
</template>

<script>
export default {
    /** 初始化岗位弹窗及后端权限选择. */
    data() {
        return {
            visible: false,
            loading: false,
            ready: false,
            saving: false,
            error: '',
            permissions: [],
            role: {id: '', name: '', remark: '', menuIds: []},
        };
    },
    methods: {
        /** 加载可授予的经营功能和岗位已有权限. */
        async init(id) {
            this.visible = true;
            this.loading = true;
            this.ready = false;
            this.error = '';
            this.role = {id: id || '', name: '', remark: '', menuIds: []};
            try {
                const requests = [this.$http({url: '/sys/menu/list', method: 'get'})];
                if (id) {
                    requests.push(this.$http({url: '/sys/role/info/' + id, method: 'get'}));
                }
                const [catalog, detail] = await Promise.all(requests);
                if (catalog.data?.code !== 0 || (detail && detail.data?.code !== 0)) {
                    throw new Error(detail?.data?.msg || catalog.data?.msg || '岗位加载失败');
                }
                this.permissions = catalog.data.data || [];
                if (detail) {
                    const current = detail.data.data;
                    this.role = {
                        id,
                        name: current.roleName,
                        remark: current.remark,
                        menuIds: current.menuIdList || [],
                    };
                }
                this.ready = true;
            } catch (error) {
                this.error = error.response?.data?.msg || error.message;
            } finally {
                this.loading = false;
            }
        },
        /** 保存工作室岗位, 机构关联与可委派属性由后端维护. */
        async save() {
            if (this.saving || !this.ready) {
                return;
            }
            if (!this.role.name) {
                this.error = '请输入岗位名称';
                return;
            }
            this.saving = true;
            this.error = '';
            try {
                const {data} = await this.$http({
                    url: this.role.id ? '/sys/role/update' : '/sys/role/save',
                    method: 'post',
                    showError: false,
                    data: {
                        roleId: this.role.id || undefined,
                        roleName: this.role.name,
                        remark: this.role.remark,
                        menuIdList: this.role.menuIds,
                    },
                });
                if (data?.code !== 0) {
                    throw new Error(data?.msg || '岗位保存失败');
                }
                this.visible = false;
                this.$emit('refreshDataList');
            } catch (error) {
                this.error = error.response?.data?.msg || error.message;
            } finally {
                this.saving = false;
            }
        },
    },
};
</script>
