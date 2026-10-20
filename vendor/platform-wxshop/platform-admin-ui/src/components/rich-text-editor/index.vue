<template>
  <div class="rich-text-editor">
    <div v-if="!disabled" class="editor-toolbar" role="toolbar" aria-label="内容排版">
      <button
        v-for="item in commands"
        :key="item.command"
        type="button"
        :disabled="sourceMode"
        @mousedown.prevent="runCommand(item.command)"
      >
        {{ item.label }}
      </button>
      <button type="button" :disabled="sourceMode" @mousedown.prevent="insertLink">链接</button>
      <button type="button" :disabled="sourceMode" @mousedown.prevent="insertTable">表格</button>
      <button type="button" :disabled="sourceMode" @mousedown.prevent="openImagePicker">
        图片
      </button>
      <button type="button" :aria-pressed="String(sourceMode)" @click="toggleSource">
        {{ sourceMode ? '可视编辑' : 'HTML源码' }}
      </button>
    </div>
    <textarea
      v-if="sourceMode"
      v-model="sourceHtml"
      class="editor-source"
      :readonly="disabled"
      aria-label="HTML源码"
      @input="emitSource"
    ></textarea>
    <div
      v-show="!sourceMode"
      ref="content"
      class="editor-content"
      :class="{'is-readonly': disabled}"
      :contenteditable="String(!disabled)"
      role="textbox"
      aria-multiline="true"
      :aria-readonly="String(disabled)"
      :aria-label="placeholder"
      :data-placeholder="placeholder"
      @input="emitContent"
      @paste="handlePaste"
      @blur="rememberSelection"
    ></div>
    <el-dialog title="插入图片" v-model="imageDialog" width="440px" append-to-body>
      <el-img v-if="imageDialog" v-model="imageUrl"></el-img>
      <p class="editor-tip">请使用你有权发布的图片, 商品演示图应明确标注用途.</p>
      <template #footer
        ><span>
          <el-button @click="imageDialog = false">取消</el-button>
          <el-button type="primary" :disabled="!imageUrl" @click="insertImage">插入</el-button>
        </span></template
      >
    </el-dialog>
  </div>
</template>

<script>
import DOMPurify from '../../third-party/dompurify/purify.es.mjs';

/** 过滤HTML中的执行内容, 保留商品详情的排版和表格. */
export function sanitizeHtml(html) {
  return DOMPurify.sanitize(String(html || ''), {
    USE_PROFILES: {html: true},
    FORBID_TAGS: ['iframe', 'object', 'embed', 'form', 'input', 'button', 'textarea'],
    FORBID_ATTR: ['srcset'],
  });
}

export default {
  emits: ['update:modelValue', 'change'],
  name: 'RichTextEditor',
  props: {
    modelValue: {type: String, default: ''},
    disabled: {type: Boolean, default: false},
    placeholder: {type: String, default: '输入内容'},
  },
  /** 创建独立编辑状态, 不加载旧编辑器脚本或插件. */
  data() {
    return {
      sourceMode: false,
      sourceHtml: '',
      lastHtml: '',
      savedRange: null,
      imageDialog: false,
      imageUrl: '',
      commands: [
        {command: 'bold', label: '加粗'},
        {command: 'italic', label: '斜体'},
        {command: 'underline', label: '下划线'},
        {command: 'insertOrderedList', label: '有序列表'},
        {command: 'insertUnorderedList', label: '无序列表'},
        {command: 'justifyLeft', label: '左对齐'},
        {command: 'justifyCenter', label: '居中'},
        {command: 'justifyRight', label: '右对齐'},
        {command: 'removeFormat', label: '清除格式'},
        {command: 'undo', label: '撤销'},
        {command: 'redo', label: '重做'},
      ],
    };
  },
  watch: {
    /** 回显外部HTML, 避免父组件同步时重置当前光标. */
    modelValue(value) {
      const clean = sanitizeHtml(value);
      if (clean !== this.lastHtml) {
        this.setContent(clean);
      }
    },
    /** 切换只读时关闭编辑弹窗, 防止延迟操作写入. */
    disabled(value) {
      if (value) {
        this.imageDialog = false;
        this.savedRange = null;
      }
    },
  },
  /** 初次挂载时显示经过过滤的原HTML. */
  mounted() {
    this.setContent(sanitizeHtml(this.modelValue));
  },
  methods: {
    /** 更新可视区域和源码区域, 不触发保存请求. */
    setContent(html) {
      this.lastHtml = html;
      this.sourceHtml = html;
      if (this.$refs.content) {
        this.$refs.content.innerHTML = html;
      }
    },
    /** 向父组件同步经过过滤的HTML. */
    emitHtml(html) {
      if (this.disabled) {
        return;
      }
      const clean = sanitizeHtml(html);
      this.lastHtml = clean;
      this.$emit('update:modelValue', clean);
      this.$emit('change', clean);
    },
    /** 同步可视输入, 过滤会执行的HTML并保留正常光标. */
    emitContent() {
      const html = this.$refs.content.innerHTML;
      const clean = sanitizeHtml(html);
      if (html !== clean) {
        this.$refs.content.innerHTML = clean;
      }
      this.emitHtml(clean);
    },
    /** 源码以文本显示, 输出前过滤执行标签. */
    emitSource() {
      this.emitHtml(this.sourceHtml);
    },
    /** 切换模式时同步内容, 防止丢失表格和原有HTML. */
    toggleSource() {
      if (this.disabled) {
        return;
      }
      if (this.sourceMode) {
        const clean = sanitizeHtml(this.sourceHtml);
        this.setContent(clean);
        this.emitHtml(clean);
      } else {
        this.sourceHtml = sanitizeHtml(this.$refs.content.innerHTML);
      }
      this.sourceMode = !this.sourceMode;
    },
    /** 保存当前编辑选区, 弹窗关闭后恢复插入位置. */
    rememberSelection() {
      const selection = window.getSelection();
      if (selection && selection.rangeCount && this.$refs.content.contains(selection.anchorNode)) {
        this.savedRange = selection.getRangeAt(0).cloneRange();
      }
    },
    /** 恢复有效选区, 无选区时将光标放在内容末尾. */
    restoreSelection() {
      this.$refs.content.focus();
      const selection = window.getSelection();
      const range = this.savedRange;
      selection.removeAllRanges();
      if (range && this.$refs.content.contains(range.commonAncestorContainer)) {
        selection.addRange(range);
      } else {
        const end = document.createRange();
        end.selectNodeContents(this.$refs.content);
        end.collapse(false);
        selection.addRange(end);
      }
    },
    /** 执行浏览器排版命令, 只作用于当前编辑区域. */
    runCommand(command, value = null) {
      if (this.disabled || this.sourceMode) {
        return;
      }
      this.rememberSelection();
      this.restoreSelection();
      document.execCommand(command, false, value);
      this.rememberSelection();
      this.emitContent();
    },
    /** 只接受HTTP或HTTPS链接, 阻止执行协议. */
    insertLink() {
      this.rememberSelection();
      this.$prompt('请输入完整的HTTP或HTTPS链接', '插入链接', {
        inputPattern: /^https?:\/\//i,
        inputErrorMessage: '链接必须以http://或https://开头',
      })
        .then(({value}) => {
          // 弹窗返回后重新检查只读状态.
          this.runCommand('createLink', value);
        })
        .catch(() => {
          // 用户取消时保留原内容.
        });
    },
    /** 插入可继续编辑的两行两列表格. */
    insertTable() {
      this.runCommand(
        'insertHTML',
        '<table border="1"><tbody>' +
          '<tr><td>内容</td><td>内容</td></tr>' +
          '<tr><td>内容</td><td>内容</td></tr></tbody></table><p><br></p>'
      );
    },
    /** 打开既有图片库, 复用管理端上传和选择能力. */
    openImagePicker() {
      this.rememberSelection();
      this.imageUrl = '';
      this.imageDialog = true;
    },
    /** 使用DOM构造图片标签并再次过滤, 不拼接原始属性. */
    insertImage() {
      if (this.disabled || !this.imageUrl) {
        return;
      }
      const image = document.createElement('img');
      image.src = this.imageUrl;
      image.alt = '内容图片';
      image.style.maxWidth = '100%';
      this.imageDialog = false;
      this.runCommand('insertHTML', sanitizeHtml(image.outerHTML));
    },
    /** 粘贴内容在插入前过滤, 纯文本使用浏览器文本命令. */
    handlePaste(event) {
      event.preventDefault();
      if (this.disabled || !event.clipboardData) {
        return;
      }
      const html = event.clipboardData.getData('text/html');
      if (html) {
        this.runCommand('insertHTML', sanitizeHtml(html));
      } else {
        this.runCommand('insertText', event.clipboardData.getData('text/plain'));
      }
    },
  },
};
</script>

<style scoped>
.rich-text-editor {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
}
.editor-toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  padding: 10px;
  background: #f5f7fa;
}
.editor-toolbar button {
  padding: 6px 9px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: #fff;
  cursor: pointer;
}
.editor-toolbar button:disabled {
  opacity: 0.5;
  cursor: default;
}
.editor-content,
.editor-source {
  min-height: 240px;
  max-height: 600px;
  overflow: auto;
  padding: 14px;
  line-height: 1.7;
  outline: none;
  text-align: left;
}
.editor-content:empty::before {
  content: attr(data-placeholder);
  color: #909399;
}
.editor-source {
  display: block;
  width: 100%;
  border: 0;
  resize: vertical;
  font-family: monospace;
}
.is-readonly {
  background: #fafafa;
}
.editor-tip {
  color: #606266;
  line-height: 1.6;
}
.editor-content :deep(img) {
  max-width: 100%;
}
.editor-content :deep(table) {
  border-collapse: collapse;
  max-width: 100%;
}
.editor-content :deep(td),
.editor-content :deep(th) {
  border: 1px solid #dcdfe6;
  padding: 6px;
}
</style>
