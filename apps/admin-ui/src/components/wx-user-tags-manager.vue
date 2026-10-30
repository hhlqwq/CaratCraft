<template>
    <el-dialog
        title="公众号用户标签管理"
        :close-on-click-modal="false"
        :lock-scroll="false"
        v-model="dialogVisible"
    >
        <div class="panel flex flex-wrap" v-loading="submitting">
            <el-tag
                v-for="tag in wxUserTags"
                closable
                @click="editTag(tag.id, tag.name)"
                @close="deleteTag(tag.id)"
                :disable-transitions="false"
                :key="tag.id"
            >
                ID：{{ tag.id }}；名称：{{ tag.name }}
            </el-tag>
            <el-input
                class="input-new-tag"
                v-if="inputVisible"
                placeholder="回车确认"
                v-model="inputValue"
                ref="saveTagInput"
                size="small"
                @keyup.enter="addTag"
            >
            </el-input>
            <el-button v-else class="button-new-tag" size="small" @click="showInput"
                >+ 添加</el-button
            >
        </div>
        <template #footer
            ><span class="dialog-footer">
                <el-button @click="dialogVisible = false">关闭</el-button>
            </span></template
        >
    </el-dialog>
</template>
<script>
import {mapState} from 'vuex';

export default {
    name: 'wx-user-tags-manager',
    props: {
        visible: {
            type: Boolean,
            default: true,
        },
    },
    /** 初始化当前组件的响应式界面和表单状态. */
    data() {
        return {
            dialogVisible: false,
            inputVisible: false,
            inputValue: '',
            submitting: false,
        };
    },
    computed: mapState({
        wxUserTags: (state) => state.wxUserTags.tags,
    }),
    /** 挂载组件后初始化控件并读取所需数据. */
    mounted() {
        this.getWxUserTags();
    },
    methods: {
        /** 显示用户标签管理弹窗. */
        show() {
            this.dialogVisible = true;
        },
        /** 读取公众号用户标签供当前界面选择或维护. */
        getWxUserTags() {
            this.$http({
                url: '/manage/wxUserTags/list',
                method: 'get',
            }).then(({data}) => {
                if (data && data.code === 0) {
                    this.$store.commit('wxUserTags/updateTags', data.data);
                }
            });
        },
        /** 确认后删除指定的公众号用户标签. */
        deleteTag(tagid) {
            if (this.submitting) {
                return;
            }
            this.$confirm(`确定删除标签?`, '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning',
            }).then(() => {
                this.submitting = true;
                this.$http({
                    url: '/manage/wxUserTags/delete/' + tagid,
                    method: 'post',
                }).then(({data}) => {
                    if (data && data.code === 0) {
                        this.getWxUserTags();
                        this.$emit('change');
                    }
                    this.submitting = false;
                });
            });
        },
        /** 显示标签输入框并在下一帧聚焦输入控件. */
        showInput() {
            this.inputVisible = true;
            this.$nextTick((_) => {
                this.$refs.saveTagInput.$refs.input.focus();
            });
        },
        /** 提交新标签并关闭新增标签输入框. */
        addTag() {
            const newTagName = this.inputValue;
            this.saveTag(newTagName);
            this.inputVisible = false;
            this.inputValue = '';
        },
        /** 提示输入指定标签的新名称并提交修改. */
        editTag(tagid, orignName = '') {
            this.$prompt('请输入新标签名称', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                inputValue: orignName,
                inputPattern: /^.{1,30}$/,
                inputErrorMessage: '名称1-30字符',
            })
                .then(({value}) => {
                    this.saveTag(value, tagid);
                })
                .catch(() => {});
        },
        /** 提交公众号用户标签的新增或改名请求. */
        saveTag(name, tagid) {
            if (this.submitting) {
                return;
            }
            this.submitting = true;
            this.$http({
                url: '/manage/wxUserTags/save',
                method: 'post',
                data: {
                    // id: tagid ? tagid : undefined,
                    id: tagid,
                    name: name,
                },
            }).then(({data}) => {
                if (data && data.code === 0) {
                    this.getWxUserTags();
                    this.$emit('change');
                }
                this.submitting = false;
            });
        },
    },
};
</script>
<style scoped>
.panel {
    flex: 1;
}

.el-tag,
.button-new-tag {
    margin: 5px;
}

.input-new-tag {
    width: inherit;
}
</style>
