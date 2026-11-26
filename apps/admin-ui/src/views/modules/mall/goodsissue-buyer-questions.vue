<template>
    <el-alert v-if="error" :title="error" type="error" :closable="false" />
    <el-table v-loading="loading" :data="rows" :empty-text="error ? '—' : '暂无用户提问'">
        <el-table-column prop="name" label="商品" min-width="140" />
        <el-table-column prop="question" label="问题" min-width="180" />
        <el-table-column prop="answer" label="商家回复" min-width="180" />
        <el-table-column label="状态" width="100">
            <template #default="{row}">{{ statusLabels[row.status] }}</template>
        </el-table-column>
        <el-table-column prop="create_time" label="提问时间" min-width="160" />
        <el-table-column v-if="isAuth('mall:goodsissue:update')" label="操作" width="150">
            <template #default="{row}">
                <el-button link type="primary" @click="open(row)">{{ row.answer ? '修改回复' : '回复并公开' }}</el-button>
                <el-button v-if="row.status !== 'HIDDEN'" link type="danger" @click="hide(row)">隐藏</el-button>
            </template>
        </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="page" :page-size="10" :total="total" layout="total, prev, pager, next" @current-change="load" />
    <el-dialog v-model="dialog" title="回复商品提问" width="540px">
        <p>{{ selected?.question }}</p>
        <el-input v-model="answer" type="textarea" :rows="5" maxlength="1000" show-word-limit placeholder="填写公开回复" />
        <template #footer>
            <el-button :disabled="saving" @click="dialog = false">取消</el-button>
            <el-button type="primary" :loading="saving" :disabled="!answer.trim()" @click="review(selected, 'PUBLISH')">回复并公开</el-button>
        </template>
    </el-dialog>
</template>

<script>
export default {
    /** 初始化独立于通用常见问题的审核状态. */
    data() {
        return {
            rows: [], page: 1, total: 0, error: '', loading: false,
            dialog: false, selected: null, answer: '', saving: false,
            statusLabels: {PENDING: '待审核', PUBLISHED: '已公开', HIDDEN: '已隐藏'},
        };
    },
    /** 查询本账号有权读取的商品问题. */
    mounted() {
        this.load();
    },
    methods: {
        /** 读取当前页, 不回填迟到的旧分页响应. */
        async load() {
            const page = this.page;
            this.loading = true;
            this.error = '';
            try {
                const {data} = await this.$http({url: '/mall/goodsissue/buyer-questions', method: 'get', params: {page}});
                if (page !== this.page) return;
                if (data.code !== 0) throw new Error(data.msg || '用户提问加载失败');
                this.rows = data.data.records;
                this.total = data.data.total;
            } catch (error) {
                if (page === this.page) this.error = error.message || '用户提问加载失败';
            } finally {
                if (page === this.page) this.loading = false;
            }
        },
        /** 复用已有回复内容, 保存前仅修改本地表单. */
        open(row) {
            this.selected = row;
            this.answer = row.answer || '';
            this.dialog = true;
        },
        /** 用户明确确认后再隐藏公开内容. */
        async hide(row) {
            try {
                await this.$confirm('隐藏后此问答将在商品详情页下架。', '隐藏问答', {type: 'warning'});
            } catch {
                return;
            }
            await this.review(row, 'HIDE');
        },
        /** 提交审核动作, 请求失败保留回复正文. */
        async review(row, action) {
            if (!row || this.saving) return;
            this.saving = true;
            try {
                const {data} = await this.$http({
                    url: '/mall/goodsissue/buyer-questions/review', method: 'post',
                    data: {id: row.id, action, answer: action === 'PUBLISH' ? this.answer : null},
                });
                if (data.code !== 0) throw new Error(data.msg || '审核操作失败');
                this.dialog = false;
                await this.load();
            } catch (error) {
                this.$message.error(error.message || '审核操作失败');
            } finally {
                this.saving = false;
            }
        },
    },
};
</script>
