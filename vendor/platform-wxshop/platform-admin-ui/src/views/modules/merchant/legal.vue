<template>
  <div v-loading="busy" class="legal-admin">
    <el-alert
      title="正文由经营者审定. 保存只生成草稿, 发布后消费者才能查看; 已发布内容保留原版本."
      type="info"
      :closable="false"
    />
    <el-alert v-if="error" :title="error" type="error" :closable="false" />
    <el-select v-model="kind" :disabled="busy" aria-label="协议类别" @change="load">
      <el-option label="用户协议" value="TERMS" /><el-option label="隐私政策" value="PRIVACY" />
    </el-select>
    <el-button :disabled="busy" @click="load">重新加载</el-button>
    <p>最新修订 {{ head.revision }} · 当前发布版本 {{ head.published_version || '尚未发布' }}</p>
    <el-form label-position="top" @submit.prevent="save">
      <el-form-item label="标题"
        ><el-input
          v-model="form.title"
          maxlength="120"
          :disabled="!loaded || busy || !isAuth('merchant:legal:update')"
      /></el-form-item>
      <el-form-item label="正文(纯文本)"
        ><el-input
          v-model="form.content"
          type="textarea"
          :rows="16"
          maxlength="50000"
          show-word-limit
          :disabled="!loaded || busy || !isAuth('merchant:legal:update')"
      /></el-form-item>
      <el-button
        v-if="isAuth('merchant:legal:update')"
        type="primary"
        :disabled="!loaded || busy"
        @click="save"
        >保存新草稿</el-button
      >
      <el-button
        v-if="isAuth('merchant:legal:publish')"
        type="success"
        :disabled="!canPublish || busy"
        @click="publish"
        >发布已保存草稿</el-button
      >
    </el-form>
    <el-table :data="versions" class="history">
      <el-table-column prop="version" label="版本" width="80" /><el-table-column
        prop="status"
        label="状态"
        width="120"
      />
      <el-table-column prop="title" label="标题" /><el-table-column
        prop="published_at"
        label="发布时间"
      />
      <el-table-column label="核对正文" width="100"
        ><template #default="scope"
          ><el-button link @click="preview = scope.row">查看</el-button></template
        ></el-table-column
      >
    </el-table>
    <el-dialog v-model="previewVisible" title="版本正文" width="min(760px, 94vw)"
      ><h3>{{ preview && preview.title }}</h3>
      <pre>{{ preview && preview.content }}</pre>
    </el-dialog>
  </div>
</template>
<script>
export default {
  name: 'MerchantLegalDocuments',
  /** 独立保存读取修订号和表单, 发布时核对已保存正文未被本地修改. */
  data() {
    return {
      kind: 'TERMS',
      head: {revision: 0, published_version: 0},
      versions: [],
      form: {title: '', content: ''},
      loaded: false,
      busy: false,
      error: '',
      preview: null,
    };
  },
  computed: {
    /** 仅最新草稿且表单与持久化正文一致时允许发布. */
    canPublish() {
      const latest = this.versions[0];
      return (
        this.loaded &&
        latest &&
        latest.status === 'DRAFT' &&
        latest.title === this.form.title &&
        latest.content === this.form.content
      );
    },
    previewVisible: {
      /** 有待核对版本时打开正文对话框. */
      get() {
        return !!this.preview;
      },
      /** 关闭预览时释放当前历史正文. */
      set(value) {
        if (!value) {
          this.preview = null;
        }
      },
    },
  },
  /** 进入管理页面时加载默认协议类别. */
  mounted() {
    this.load();
  },
  methods: {
    /** 统一检查业务响应, 失败不清空当前编辑内容. */
    async request(url, method = 'get', data) {
      const response = await this.$http({url, method, data, loading: false, showError: false});
      if (!response.data || response.data.code !== 0) {
        throw new Error((response.data && response.data.msg) || '协议操作失败');
      }
      return response.data.data;
    },
    /** 应用完整服务端版本, 不推测发布是否成功. */
    apply(result) {
      this.head = result.head;
      this.versions = result.versions;
      const latest = result.versions[0] || {};
      this.form = {title: latest.title || '', content: latest.content || ''};
      this.loaded = true;
    },
    /** 类别切换后重新读取, 请求期间阻止重复保存或切换. */
    async load() {
      if (this.busy) {
        return;
      }
      this.busy = true;
      this.loaded = false;
      this.error = '';
      try {
        this.apply(await this.request('/merchant/legal/' + this.kind));
      } catch (error) {
        this.error = error.message;
      } finally {
        this.busy = false;
      }
    },
    /** 保存追加修订, 版本冲突由服务端拒绝并保留本地正文. */
    async save() {
      if (this.busy || !this.loaded) {
        return;
      }
      this.busy = true;
      this.error = '';
      try {
        this.apply(
          await this.request('/merchant/legal', 'post', {
            kind: this.kind,
            revision: this.head.revision,
            ...this.form,
          })
        );
        this.$message.success('新草稿已保存');
      } catch (error) {
        this.error = error.message;
      } finally {
        this.busy = false;
      }
    },
    /** 明确核对具体修订后发布, 对话框期间锁定表单及类别. */
    async publish() {
      if (this.busy || !this.canPublish) {
        return;
      }
      this.busy = true;
      this.error = '';
      try {
        await this.$confirm(
          '确认正文已由经营者审定并发布版本 ' + this.head.revision + '?',
          '发布协议'
        );
        this.apply(
          await this.request(
            '/merchant/legal/' + this.kind + '/' + this.head.revision + '/publish',
            'post'
          )
        );
        this.$message.success('协议版本已发布');
      } catch (error) {
        if (error !== 'cancel' && error !== 'close') {
          this.error = error.message || '发布失败';
        }
      } finally {
        this.busy = false;
      }
    },
  },
};
</script>
<style scoped>
.legal-admin {
  max-width: 1000px;
}
.el-alert,
.el-select,
.history {
  margin-bottom: 20px;
}
pre {
  white-space: pre-wrap;
  overflow-wrap: anywhere;
  line-height: 1.8;
}
</style>
