<template>
    <el-dialog
        v-model="visible"
        :title="role.id ? '编辑岗位' : '新增岗位'"
        :close-on-click-modal="false"
        width="min(760px, calc(100vw - 32px))"
    >
        <el-form v-loading="loading" :model="role" label-width="90px" @submit.prevent="save">
            <el-form-item label="岗位名称">
                <el-input v-model.trim="role.name" maxlength="100" />
            </el-form-item>
            <el-form-item label="备注">
                <el-input v-model.trim="role.remark" type="textarea" maxlength="200" />
            </el-form-item>
            <el-form-item label="功能权限">
                <div v-if="ready" class="role-permission-picker">
                    <el-checkbox-group
                        v-if="permissionGroups.length"
                        v-model="role.menuIds"
                        aria-label="岗位功能权限"
                    >
                        <el-collapse v-model="openGroups" class="role-permission-picker__groups">
                            <el-collapse-item
                                v-for="group in permissionGroups"
                                :key="group.title"
                                :name="group.title"
                            >
                                <template #title>
                                    <span>{{ group.title }}</span>
                                    <span class="role-permission-picker__count">
                                        已选 {{ group.selected.length }} 项
                                    </span>
                                </template>
                                <div class="role-permission-picker__options">
                                    <el-checkbox
                                        v-for="item in group.items"
                                        :key="item.menuId"
                                        :value="item.menuId"
                                    >
                                        {{ item.label }}
                                    </el-checkbox>
                                </div>
                            </el-collapse-item>
                        </el-collapse>
                    </el-checkbox-group>
                    <p v-else>暂无可分配功能</p>
                    <div class="role-permission-picker__summary" aria-live="polite">
                        <strong>已选权限（{{ selectedCount }} 项）</strong>
                        <p v-if="!selectedCount">暂未勾选</p>
                        <p v-for="group in selectedGroups" :key="group.title">
                            {{ group.title }} · {{ group.selected.map((item) => item.label).join('、') }}
                        </p>
                        <p v-if="retainedCount">另保留 {{ retainedCount }} 项已有授权</p>
                    </div>
                </div>
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
import {ElCheckbox, ElCheckboxGroup, ElCollapse, ElCollapseItem} from 'element-plus';
import {rolePermissionGroups} from './role-permissions';

export default {
    components: {ElCheckbox, ElCheckboxGroup, ElCollapse, ElCollapseItem},
    /** 初始化岗位弹窗及后端权限选择. */
    data() {
        return {
            visible: false,
            loading: false,
            ready: false,
            saving: false,
            error: '',
            groups: [],
            openGroups: [],
            role: {id: '', name: '', remark: '', menuIds: []},
        };
    },
    computed: {
        /** 勾选状态仅影响当前目录内的展示, 原始菜单ID保持在表单中. */
        permissionGroups() {
            const selected = new Set(this.role.menuIds);
            return this.groups.map((group) => ({
                ...group,
                selected: group.items.filter((item) => selected.has(item.menuId)),
            }));
        },
        /** 摘要只列出有勾选的分类. */
        selectedGroups() {
            return this.permissionGroups.filter((group) => group.selected.length);
        },
        /** 统计可见授权条目. */
        selectedCount() {
            return this.selectedGroups.reduce((count, group) => count + group.selected.length, 0);
        },
        /** 旧入口的既有授权仍随岗位保存. */
        retainedCount() {
            return this.role.menuIds.length - this.selectedCount;
        },
    },
    methods: {
        /** 加载可授予的经营功能和岗位已有权限. */
        async init(id) {
            this.visible = true;
            this.loading = true;
            this.ready = false;
            this.error = '';
            this.groups = [];
            this.openGroups = [];
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
                this.groups = rolePermissionGroups(catalog.data.data || []);
                if (detail) {
                    const current = detail.data.data;
                    this.role = {
                        id,
                        name: current.roleName,
                        remark: current.remark,
                        menuIds: current.menuIdList || [],
                    };
                }
                this.openGroups = this.selectedGroups.length
                    ? this.selectedGroups.map((group) => group.title)
                    : this.groups.slice(0, 1).map((group) => group.title);
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

<style scoped>
.role-permission-picker {
    width: 100%;
}

.role-permission-picker__groups {
    max-height: min(44vh, 380px);
    overflow-y: auto;
}

.role-permission-picker__count {
    margin-inline-start: 12px;
    color: var(--el-text-color-secondary);
    font-weight: 400;
}

.role-permission-picker__options {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(min(100%, 166px), 1fr));
    gap: 2px 12px;
    padding-inline: 6px;
}

.role-permission-picker__options :deep(.el-checkbox) {
    margin-inline-end: 0;
    white-space: normal;
}

.role-permission-picker__summary {
    max-height: 112px;
    overflow-y: auto;
    margin-block-start: 12px;
    padding: 10px 12px;
    background: var(--el-fill-color-light);
    line-height: 1.6;
}

.role-permission-picker__summary p {
    margin: 4px 0 0;
    color: var(--el-text-color-secondary);
    overflow-wrap: anywhere;
}
</style>
