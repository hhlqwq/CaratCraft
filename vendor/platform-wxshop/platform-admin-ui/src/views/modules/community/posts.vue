<template>
  <div class="community-admin">
    <el-alert title="所有分享默认待审核。请核对文字、图片与公开设计材料, 确认适合公开后再通过; 社区不开放评论或好友。" type="info" :closable="false" />
    <el-form :inline="true" class="filters">
      <el-form-item label="审核状态"><el-select v-model="status" style="width: 190px" @change="filter"><el-option v-for="(label, key) in statuses" :key="key" :value="key" :label="label" /></el-select></el-form-item>
      <el-form-item><el-button @click="load">刷新</el-button></el-form-item>
    </el-form>
    <el-alert v-if="error" :title="error" type="error" :closable="false" />
    <el-table v-loading="loading" :data="records" border class="desktop-list">
      <el-table-column prop="id" label="编号" width="85" />
      <el-table-column label="分享内容" min-width="300"><template #default="scope">
        <div class="post-title">{{ scope.row.title }}</div><div class="post-content">{{ scope.row.content }}</div>
        <div class="images"><el-image v-for="url in scope.row.images" :key="url" :src="url" :preview-src-list="scope.row.images" preview-teleported fit="cover" /></div>
        <details v-if="scope.row.arrangement.length"><summary>公开设计排列 ({{ scope.row.arrangement.length }}个实例)</summary><div v-for="(item, index) in scope.row.arrangement" :key="index">{{ index + 1 }}. {{ item.name }} · {{ item.sizeMm || '尺寸未配置' }} {{ item.sizeMm ? 'mm' : '' }}</div></details>
      </template></el-table-column>
      <el-table-column label="类型" width="110"><template #default="scope">{{ kinds[scope.row.kind] }}</template></el-table-column>
      <el-table-column prop="createTime" label="提交时间" min-width="160" />
      <el-table-column prop="moderationReason" label="审核说明" min-width="150" />
      <el-table-column label="操作" width="190" fixed="right"><template #default="scope">
        <template v-if="isAuth('community:post:moderate') && scope.row.status !== 'DELETED'">
          <el-button v-if="scope.row.status !== 'PUBLISHED'" type="text" :disabled="busy || loading" @click="moderate(scope.row, 'PUBLISHED')">通过</el-button>
          <el-button v-if="scope.row.status !== 'HIDDEN'" type="text" :disabled="busy || loading" @click="moderate(scope.row, 'HIDDEN')">隐藏</el-button>
          <el-button type="text" :disabled="busy || loading" @click="moderate(scope.row, 'DELETED')">删除</el-button>
        </template>
        <el-button type="text" @click="showLogs(scope.row.id)">日志</el-button>
      </template></el-table-column>
    </el-table>
    <div v-loading="loading" class="compact-posts">
      <p v-if="!records.length">当前状态没有分享</p>
      <article v-for="row in records" :key="row.id" class="compact-post">
        <div class="compact-meta">#{{ row.id }} · {{ kinds[row.kind] }} · {{ row.createTime }}</div>
        <div class="post-title">{{ row.title }}</div><div class="post-content">{{ row.content }}</div>
        <div class="images"><el-image v-for="url in row.images" :key="url" :src="url" :preview-src-list="row.images" preview-teleported fit="cover" /></div>
        <details v-if="row.arrangement.length"><summary>公开设计排列 ({{ row.arrangement.length }}个实例)</summary><div v-for="(item, index) in row.arrangement" :key="index">{{ index + 1 }}. {{ item.name }} · {{ item.sizeMm || '尺寸未配置' }} {{ item.sizeMm ? 'mm' : '' }}</div></details>
        <p v-if="row.moderationReason">审核说明: {{ row.moderationReason }}</p>
        <div class="compact-actions"><template v-if="isAuth('community:post:moderate') && row.status !== 'DELETED'"><el-button v-if="row.status !== 'PUBLISHED'" size="small" :disabled="busy || loading" @click="moderate(row, 'PUBLISHED')">通过</el-button><el-button v-if="row.status !== 'HIDDEN'" size="small" :disabled="busy || loading" @click="moderate(row, 'HIDDEN')">隐藏</el-button><el-button size="small" :disabled="busy || loading" @click="moderate(row, 'DELETED')">删除</el-button></template><el-button size="small" @click="showLogs(row.id)">日志</el-button></div>
      </article>
    </div>
    <el-pagination :current-page="page" :page-size="20" :total="total" layout="total, prev, pager, next" @current-change="changePage" />
    <el-dialog v-model="logsVisible" title="社区操作记录" width="min(94vw, 900px)"><el-table :data="logs"><el-table-column prop="createTime" label="时间" min-width="155" /><el-table-column prop="operatorName" label="操作人" min-width="130" /><el-table-column prop="fromStatus" label="原状态" /><el-table-column prop="toStatus" label="新状态" /><el-table-column prop="reason" label="原因" min-width="180" /></el-table></el-dialog>
  </div>
</template>

<script>
export default {
  compatConfig: {MODE: 2},
  /** 社区列表只保存当前页和显式审核状态. */
  data () {
    return {status: 'PENDING', page: 1, total: 0, records: [], loading: false, busy: false, error: '', logs: [], logsVisible: false,
      statuses: {PENDING: '待审核', PUBLISHED: '已公开', HIDDEN: '已隐藏', DELETED: '已删除'},
      kinds: {DESIGN: '设计分享', FINISHED: '成品分享', SUGGESTION: '改进建议'}}
  },
  /** 进入运营页面时重新获取服务端审核状态. */
  activated () { this.load() },
  methods: {
    /** 切换审核状态时重置页码. */
    filter () { this.page = 1; this.load() },
    /** 翻页时保留所选审核状态. */
    changePage (page) { this.page = page; this.load() },
    /** JSON字段解析失败时显示空值, 不执行用户提供的富文本. */
    parse (text, fallback) { try { return JSON.parse(text || 'null') || fallback } catch (error) { return fallback } },
    /** 使用序号隔离快速切换筛选时的迟到响应. */
    async load () {
      const seq = this._seq = (this._seq || 0) + 1
      this.loading = true
      this.error = ''
      try {
        const {data} = await this.$http({url: '/community/posts/list', method: 'get', params: {status: this.status, page: this.page, limit: 20}})
        if (!data || data.code !== 0) throw new Error((data && data.msg) || '社区列表加载失败')
        if (seq !== this._seq) return
        this.records = data.data.records.map(row => Object.assign({}, row, {images: this.parse(row.imagesJson, []), arrangement: this.parse(row.designJson, {}).arrangement || []}))
        this.total = data.data.total
      } catch (error) { if (seq === this._seq) this.error = error.message } finally { if (seq === this._seq) this.loading = false }
    },
    /** 所有审核动作必须填写原因, 服务端再次校验当前版本. */
    async moderate (row, status) {
      try {
        const {value} = await this.$prompt(`确认将此分享设为“${this.statuses[status]}”, 请填写审核原因`, '社区审核', {inputType: 'textarea', inputValidator: value => value && value.trim().length > 0 && value.length <= 300 ? true : '请填写1到300字原因'})
        this.busy = true
        const {data} = await this.$http({url: '/community/posts/moderate', method: 'post', params: {id: row.id, version: row.version, status, reason: value}})
        if (!data || data.code !== 0) throw new Error((data && data.msg) || '审核失败')
        this.$message.success('审核操作已保存')
        await this.load()
      } catch (error) { if (error !== 'cancel' && error !== 'close') this.$message.error(error.message || '审核失败') } finally { this.busy = false }
    },
    /** 日志由服务端返回操作人和前后状态, 不在浏览器拼造记录. */
    async showLogs (id) {
      const {data} = await this.$http({url: '/community/posts/logs', method: 'get', params: {id}})
      if (data && data.code === 0) { this.logs = data.data; this.logsVisible = true }
    }
  }
}
</script>

<style scoped>
.filters { margin-top: 24px; }
.post-title { font-size: 16px; font-weight: 600; margin-bottom: 8px; overflow-wrap: anywhere; }
.post-content { white-space: pre-wrap; max-height: 240px; overflow: auto; line-height: 1.8; overflow-wrap: anywhere; }
.images { display: flex; flex-wrap: wrap; gap: 8px; margin: 12px 0; }
.images .el-image { width: 88px; height: 88px; border-radius: 8px; }
details { margin: 14px 0; line-height: 1.9; }
.compact-posts { display: none; }
.compact-post { padding: 18px 12px; border: 1px solid #ebe6dc; border-radius: 10px; margin-bottom: 16px; }
.compact-meta { color: #8d8272; font-size: 12px; margin-bottom: 10px; overflow-wrap: anywhere; }
.compact-actions { display: flex; flex-wrap: wrap; gap: 10px; }
.compact-actions .el-button { margin-left: 0; }
@media (max-width: 760px) {
  .desktop-list { display: none; }
  .compact-posts { display: block; }
}
</style>
