<template>
  <div class="panel flex flex-wrap">
    <el-tag
      v-for="tag in dynamicTags"
      :closable="!disabled"
      @close="handleClose(tag)"
      :disable-transitions="false"
      :key="tag"
    >
      {{ tag }}
    </el-tag>
    <el-input
      class="input-new-tag"
      v-if="inputVisible"
      v-model="inputValue"
      ref="saveTagInput"
      size="small"
      :disabled="disabled"
      @keyup.enter="handleInputConfirm"
      @blur="handleInputConfirm"
    >
    </el-input>
    <el-button v-else class="button-new-tag" size="small" @click="showInput" :disabled="disabled"
      >+ 添加</el-button
    >
  </div>
</template>
<script>
/**
 * 标签编辑器
 */
export default {
  compatConfig: {MODE: 2},
  name: 'tags-editor',
  props: {
    value: {
      type: String,
      required: true,
      default: '',
    },
    disabled: {
      type: Boolean,
      default: false,
    },
    size: {
      // 标签大小：[small:小,large:大]
      type: String,
      default: 'small',
    },
  },
  data() {
    return {
      inputVisible: false,
      inputValue: '',
    };
  },
  computed: {
    dynamicTags() {
      if (this.value !== '') {
        return this.value.split(',');
      }
      return [];
    },
  },
  methods: {
    handleClose(tag) {
      const newTags = this.dynamicTags;
      newTags.splice(newTags.indexOf(tag), 1);
      this.$emit('input', newTags.join(','));
    },
    showInput() {
      this.inputVisible = true;
      this.$nextTick((_) => {
        this.$refs.saveTagInput.$refs.input.focus();
      });
    },
    handleInputConfirm() {
      const inputValue = this.inputValue;
      const newTags = this.dynamicTags;
      if (inputValue && newTags.indexOf(inputValue) < 0) {
        newTags.push(inputValue);
      }
      this.inputVisible = false;
      this.inputValue = '';
      this.$emit('input', newTags.join(','));
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
