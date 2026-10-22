<template>
    <div>
        <el-select
            v-model="selectValue"
            autocomplete
            clearable
            :disabled="disabled"
            @change="changeHandle"
            :placeholder="placeholder"
            filterable
        >
            <el-option
                v-for="item in options"
                :key="item.key"
                :label="item.label"
                :value="item.value"
            >
            </el-option>
        </el-select>
    </div>
</template>

<script>
export default {
    emits: ['update:modelValue', 'change'],
    name: 'el-dict',
    componentName: 'ElDict',
    data() {
        return {
            placeholder: '请选择',
            options: [],
            selectValue: '',
        };
    },
    methods: {
        changeHandle() {
            this.$emit('change');
        },
    },
    props: {
        disabled: {
            type: Boolean,
            default: false,
        },
        // 导入的url地址
        code: {
            type: String,
        },
        // Vue3双向绑定使用modelValue及update:modelValue事件.
        modelValue: {},
    },
    watch: {
        // 判断下拉框的值是否有改变
        selectValue(val, oldVal) {
            if (val !== oldVal) {
                this.$emit('update:modelValue', this.selectValue);
            }
        },
        /** 父组件修改或重置字典值时同步当前选择, 数字沿用字典字符串编码. */
        modelValue(val) {
            if (typeof val === 'number') {
                this.selectValue = val.toString();
            } else {
                this.selectValue = val;
            }
        },
    },
    /** 首次显示立即回显父值, 字典选项随后按配置代码加载. */
    mounted() {
        this.selectValue = this.modelValue == null ? '' : String(this.modelValue);
        // 远程请求回来的数据
        this.$http('/sys/dict/queryByCode?code=' + this.code).then((response) => {
            if (response.data.data.type) {
                this.placeholder = response.data.data.type;
            }
            for (let i = 0; i < response.data.data.list.length; i++) {
                this.options[i] = {};
                this.options[i].label = response.data.data.list[i]['name'];
                this.options[i].value = response.data.data.list[i]['value'];
                this.options[i].key = i;
            }
        });
    },
};
</script>
