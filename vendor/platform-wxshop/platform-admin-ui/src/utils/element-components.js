import {defineAsyncComponent} from 'vue';
import {
    ElAlert,
    ElAvatar,
    ElBadge,
    ElButton,
    ElCard,
    ElCol,
    ElDialog,
    ElDivider,
    ElDropdown,
    ElDropdownItem,
    ElDropdownMenu,
    ElEmpty,
    ElForm,
    ElFormItem,
    ElImage,
    ElInput,
    ElInputNumber,
    ElLink,
    ElMenu,
    ElMenuItem,
    ElOption,
    ElPagination,
    ElPopover,
    ElProgress,
    ElRadio,
    ElRadioButton,
    ElRadioGroup,
    ElRow,
    ElSelect,
    ElSubMenu,
    ElSwitch,
    ElTabPane,
    ElTabs,
    ElTag,
    ElTooltip,
    ElLoading,
    ElMessage,
    ElMessageBox,
    ElNotification,
    provideGlobalConfig,
} from 'element-plus';
import zhCn from 'element-plus/es/locale/lang/zh-cn';
import ElDescriptionsItem from 'element-plus/es/components/descriptions/src/description-item.mjs';
import ElTableColumn from 'element-plus/es/components/table/src/tableColumn.mjs';

/** 按需加载重型控件, 描述子项与表格列保留真实类型供父组件识别. */
export function installElementComponents(app) {
    app.component('ElDescriptionsItem', ElDescriptionsItem);
    app.component('ElTableColumn', ElTableColumn);
    for (const component of [
        ElAlert,
        ElAvatar,
        ElBadge,
        ElButton,
        ElCard,
        ElCol,
        ElDialog,
        ElDivider,
        ElDropdown,
        ElDropdownItem,
        ElDropdownMenu,
        ElEmpty,
        ElForm,
        ElFormItem,
        ElImage,
        ElInput,
        ElInputNumber,
        ElLink,
        ElMenu,
        ElMenuItem,
        ElOption,
        ElPagination,
        ElPopover,
        ElProgress,
        ElRadio,
        ElRadioButton,
        ElRadioGroup,
        ElRow,
        ElSelect,
        ElSubMenu,
        ElSwitch,
        ElTabPane,
        ElTabs,
        ElTag,
        ElTooltip,
        ElLoading,
        ElMessage,
        ElMessageBox,
        ElNotification,
    ]) {
        app.use(component);
    }
    app.component(
        'ElDatePicker',
        defineAsyncComponent(() =>
            import('element-plus/es/components/date-picker/index.mjs').then(
                (module) => module.ElDatePicker
            )
        )
    );
    app.component(
        'ElTimePicker',
        defineAsyncComponent(() =>
            import('element-plus/es/components/time-picker/index.mjs').then(
                (module) => module.ElTimePicker
            )
        )
    );
    app.component(
        'ElTable',
        defineAsyncComponent(() =>
            import('element-plus/es/components/table/index.mjs').then((module) => module.ElTable)
        )
    );
    app.component(
        'ElTree',
        defineAsyncComponent(() =>
            import('element-plus/es/components/tree/index.mjs').then((module) => module.ElTree)
        )
    );
    app.component(
        'ElUpload',
        defineAsyncComponent(() =>
            import('element-plus/es/components/upload/index.mjs').then((module) => module.ElUpload)
        )
    );
    app.component(
        'ElDescriptions',
        defineAsyncComponent(() =>
            import('element-plus/es/components/descriptions/index.mjs').then(
                (module) => module.ElDescriptions
            )
        )
    );
    provideGlobalConfig({locale: zhCn, size: 'default'}, app, true);
}
