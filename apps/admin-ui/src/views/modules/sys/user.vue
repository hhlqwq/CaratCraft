<template>
    <div class="mod-user">
        <el-form :inline="true" :model="searchForm" @keyup.enter="getDataList(1)">
            <el-form-item>
                <el-input v-model="searchForm.userName" placeholder="用户名" clearable></el-input>
            </el-form-item>
            <el-form-item>
                <el-input v-model="searchForm.realName" placeholder="真实姓名" clearable></el-input>
            </el-form-item>
            <el-form-item>
                <el-input v-model="searchForm.mobile" placeholder="手机号" clearable></el-input>
            </el-form-item>
            <el-form-item>
                <el-select
                    v-model="searchForm.status"
                    placeholder="员工状态"
                    @change="getDataList(1)"
                    clearable
                >
                    <el-option key="0" label="停用" value="0"> </el-option>
                    <el-option key="1" label="启用" value="1"> </el-option>
                </el-select>
            </el-form-item>
            <el-form-item>
                <el-button @click="getDataList(1)">查询</el-button>
                <el-button
                    v-if="isAuth('sys:user:save')"
                    type="primary"
                    @click="addOrUpdateHandle()"
                    >新增</el-button
                >
                <el-button
                    v-if="isAuth('sys:user:delete')"
                    type="danger"
                    @click="deleteHandle()"
                    :disabled="dataListSelections.length <= 0"
                    >批量删除
                </el-button>
                <el-button
                    v-if="isAuth('sys:user:resetPw')"
                    @click="resetHandle()"
                    :disabled="dataListSelections.length <= 0"
                    >重置密码
                </el-button>
            </el-form-item>
        </el-form>
        <el-alert v-if="listError" :title="listError" type="error" :closable="false" />
        <el-table
            v-loading="loading"
            :empty-text="listError ? '—' : '暂无员工账号'"
            :data="dataList"
            border
            @selection-change="selectionChangeHandle"
            style="width: 100%"
        >
            <el-table-column type="selection" header-align="center" align="center" width="50">
            </el-table-column>
            <el-table-column prop="userName" header-align="center" align="center" label="用户名">
            </el-table-column>
            <el-table-column prop="realName" header-align="center" align="center" label="真实姓名">
            </el-table-column>
            <el-table-column
                prop="email"
                min-width="165"
                show-overflow-tooltip
                class-name="studio-nowrap"
                label="邮箱"
            >
            </el-table-column>
            <el-table-column
                prop="mobile"
                min-width="126"
                class-name="studio-nowrap"
                label="手机号"
            >
            </el-table-column>
            <el-table-column prop="status" header-align="center" align="center" label="状态">
                <template #default="scope">
                    <el-tag v-if="scope.row.status === 0" size="small" type="danger">停用</el-tag>
                    <el-tag v-else-if="scope.row.status === 1" size="small" type="success"
                        >启用</el-tag
                    >
                </template>
            </el-table-column>
            <el-table-column
                fixed="right"
                header-align="center"
                align="center"
                width="96"
                label="操作"
            >
                <template #default="scope">
                    <el-button
                        v-if="isAuth('sys:user:update') && isAuth('sys:user:info')"
                        type="text"
                        size="small"
                        @click="addOrUpdateHandle(scope.row.userId)"
                        >编辑
                    </el-button>
                    <el-button
                        v-if="isAuth('sys:user:delete')"
                        type="text"
                        size="small"
                        @click="deleteHandle(scope.row.userId)"
                    >
                        删除
                    </el-button>
                </template>
            </el-table-column>
            <el-table-column
                prop="lastLoginAt"
                label="最近登录"
                width="168"
                class-name="studio-nowrap"
            />
            <el-table-column label="岗位" prop="roleNames" min-width="140" show-overflow-tooltip>
                <template #default="{row}">{{ row.roleNames || '未分配岗位' }}</template>
            </el-table-column>
            <el-table-column label="权限来源" width="100">
                <template #default="scope">
                    <el-button
                        v-if="isAuth('sys:user:info')"
                        link
                        @click="showPermissions(scope.row.userId)"
                        >查看来源</el-button
                    >
                </template>
            </el-table-column>
        </el-table>
        <el-dialog v-model="permissionsVisible" title="员工角色与权限来源">
            <el-table :data="permissionRows">
                <el-table-column prop="roleName" label="角色" />
                <el-table-column prop="permissionLabel" label="功能权限" />
            </el-table>
        </el-dialog>
        <el-pagination
            @size-change="sizeChangeHandle"
            @current-change="currentChangeHandle"
            :current-page="pageIndex"
            :page-sizes="[10, 20, 50, 100]"
            :page-size="pageSize"
            :total="totalPage"
            layout="total, sizes, prev, pager, next, jumper"
        >
        </el-pagination>
        <!-- 弹窗, 新增 / 修改 -->
    </div>
</template>

<script>
import {rolePermissionGroups} from './role-permissions';
export default {
    /** 初始化当前组件的响应式界面和表单状态. */
    data() {
        return {
            searchForm: {
                userName: '',
                realName: '',
                mobile: '',
                status: '',
            },
            dataList: [],
            loading: false,
            listError: '',
            pageIndex: 1,
            pageSize: 10,
            totalPage: 0,
            dataListSelections: [],
            permissionsVisible: false,
            permissionRows: [],
        };
    },
    /** 页面激活时刷新当前业务数据. */
    activated() {
        const query = this.$route.query;
        this.pageSize = [10, 20, 50, 100].includes(Number(query.limit)) ? Number(query.limit) : 10;
        for (const field of Object.keys(this.searchForm)) {
            this.searchForm[field] = query[field] || '';
        }
        this.getDataList(Math.max(1, Number.parseInt(query.page, 10) || 1));
    },
    methods: {
        /** 读取服务端确认的人员角色、机构及授权来源. */
        async showPermissions(userId) {
            const [details, catalog] = await Promise.all([
                this.$http({url: '/sys/user/permissions/' + userId, method: 'get'}),
                this.$http({url: '/sys/menu/list', method: 'get'}),
            ]);
            if (details.data?.code === 0 && catalog.data?.code === 0) {
                const labels = new Map();
                for (const {items} of rolePermissionGroups(catalog.data.data || [])) {
                    for (const {label, actions} of items) {
                        actions.forEach((action) => labels.set(action, label));
                    }
                }
                this.permissionRows = details.data.data.map((row) => ({
                    roleName: row.roleName,
                    permissionLabel: Array.from(
                        new Set(
                            row.permissions
                                .split(',')
                                .map((action) => labels.get(action) || '历史经营功能')
                        )
                    ).join('、'),
                }));
                this.permissionsVisible = true;
            }
        },
        // 获取数据列表
        getDataList(page) {
            if (page) {
                this.pageIndex = page;
            }
            this.loading = true;
            this.listError = '';
            this.$http({
                url: '/sys/user/list',
                method: 'get',
                showError: false,
                params: {
                    page: this.pageIndex,
                    limit: this.pageSize,
                    userName: this.searchForm.userName,
                    realName: this.searchForm.realName,
                    mobile: this.searchForm.mobile,
                    status: this.searchForm.status,
                },
            })
                .then(({data}) => {
                    if (data && data.code === 0) {
                        this.dataList = data.data.records;
                        this.totalPage = data.data.total;
                        if (this.$route.name === 'sys-user') {
                            const query = {
                                page: this.pageIndex > 1 ? String(this.pageIndex) : undefined,
                                limit: this.pageSize === 10 ? undefined : String(this.pageSize),
                            };
                            for (const [key, value] of Object.entries(this.searchForm)) {
                                query[key] =
                                    value === '' || value == null ? undefined : String(value);
                            }
                            this.$router.replace({name: 'sys-user', query});
                        }
                    } else {
                        this.listError =
                            data?.code === 403
                                ? '没有权限查看员工账号'
                                : '员工账号加载失败, 请重试';
                    }
                })
                .catch(() => {
                    this.listError = '员工账号加载失败, 请重试';
                })
                .finally(() => {
                    this.loading = false;
                });
        },
        // 每页数
        sizeChangeHandle(val) {
            this.pageSize = val;
            this.pageIndex = 1;
            this.getDataList();
        },
        // 当前页
        currentChangeHandle(val) {
            this.pageIndex = val;
            this.getDataList();
        },
        // 多选
        selectionChangeHandle(val) {
            this.dataListSelections = val;
        },
        // 新增 / 修改
        addOrUpdateHandle(id) {
            return this.$router.push(
                id
                    ? {name: 'sys-user-edit', params: {id}, query: this.$route.query}
                    : {name: 'sys-user-new', query: this.$route.query}
            );
        },
        /** 清空筛选或编辑条件并恢复初始查询状态. */
        resetHandle() {
            if (this.dataListSelections.length !== 1) {
                this.$message.warning('请选择一个员工单独重置密码');
                return;
            }
            const userId = this.dataListSelections[0].userId;
            this.$confirm('重置后员工须重新登录并修改临时密码，确认继续？', '重置员工密码')
                .then(async () => {
                    const {data} = await this.$http({
                        url: '/sys/user/resetPw',
                        method: 'post',
                        data: {userId},
                    });
                    if (data?.code === 0) {
                        await this.$alert(data.data.temporaryPassword, '员工临时密码，请单独交付', {
                            closeOnClickModal: false,
                        });
                        this.getDataList();
                    }
                })
                .catch(() => {});
        },
        // 删除
        deleteHandle(id) {
            const userIds = id
                ? [id]
                : this.dataListSelections.map((item) => {
                      return item.userId;
                  });
            this.$confirm('确定对所选项进行[删除]操作?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning',
            })
                .then(() => {
                    this.$http({
                        url: '/sys/user/delete',
                        method: 'post',
                        data: userIds,
                    }).then(({data}) => {
                        if (data && data.code === 0) {
                            this.$message({
                                message: '操作成功',
                                type: 'success',
                                duration: 1500,
                            });
                            this.getDataList();
                        }
                    });
                })
                .catch(() => {});
        },
    },
};
</script>
