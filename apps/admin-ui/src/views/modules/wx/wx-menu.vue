<template>
    <div>
        <div id="app-menu">
            <!-- 预览窗 -->
            <div class="weixin-preview">
                <div class="weixin-bd">
                    <div class="weixin-header">
                        <span>公众号名称</span>
                    </div>
                    <ul class="weixin-menu" id="weixin-menu">
                        <li class="menu-li"></li>
                        <li
                            v-for="(btn, i) in menu.buttons"
                            :key="i"
                            class="menu-item"
                            :class="{current: selectedMenuIndex === i && selectedMenuLevel === 1}"
                            @click="selectMenu(i)"
                        >
                            <div class="menu-item-title">
                                <span>{{ btn.name }}</span>
                            </div>
                            <ul class="weixin-sub-menu">
                                <li
                                    v-for="(sub, i2) in btn.subButtons"
                                    :key="i2"
                                    class="menu-sub-item"
                                    :class="{
                                        current:
                                            selectedMenuIndex === i &&
                                            selectedSubMenuIndex === i2 &&
                                            selectedMenuLevel === 2,
                                        'on-drag-over': onDragOverMenu === i + '_' + i2,
                                    }"
                                    @click.stop="selectSubMenu(i, i2)"
                                    draggable="true"
                                    @dragstart="selectSubMenu(i, i2)"
                                    @dragover.prevent="onDragOverMenu = i + '_' + i2"
                                    @drop="onDrop(i, i2)"
                                >
                                    <div class="menu-item-title">
                                        <span>{{ sub.name }}</span>
                                    </div>
                                </li>
                                <li
                                    v-if="btn.subButtons.length < 5"
                                    class="menu-sub-item"
                                    :class="{
                                        'on-drag-over':
                                            onDragOverMenu === i + '_' + btn.subButtons.length,
                                    }"
                                    @click.stop="addMenu(2, i)"
                                    @dragover.prevent="
                                        onDragOverMenu = i + '_' + btn.subButtons.length
                                    "
                                    @drop="onDrop(i, btn.subButtons.length)"
                                >
                                    <div class="menu-item-title">
                                        <i class="el-icon-plus"></i>
                                    </div>
                                </li>
                                <i class="menu-arrow arrow_out"></i>
                                <i class="menu-arrow arrow_in"></i>
                            </ul>
                        </li>
                        <li class="menu-item" v-if="menu.buttons.length < 3" @click="addMenu(1)">
                            <i class="el-icon-plus"></i>
                        </li>
                    </ul>
                </div>
            </div>
            <!-- 菜单编辑器 -->
            <div class="weixin-menu-detail" v-if="selectedMenuLevel > 0">
                <wx-menu-button-editor
                    :button="selectedButton"
                    :selectedMenuLevel="selectedMenuLevel"
                    @delMenu="delMenu"
                    @change="updateSelectedButton"
                ></wx-menu-button-editor>
            </div>
        </div>
        <div class="weixin-btn-group" v-if="isAuth('wx:menu:save')">
            <el-button type="success" icon="el-icon-upload" @click="updateWxMenu">发布</el-button>
            <el-button type="warning" icon="el-icon-delete" @click="delMenu">删除</el-button>
        </div>
    </div>
</template>
<script>
import WxMenuButtonEditor from './wx-menu-button-editor';
export default {
    components: {
        WxMenuButtonEditor,
    },
    /** 初始化当前组件的响应式界面和表单状态. */
    data() {
        return {
            // 当前菜单
            menu: {buttons: []},
            // 当前选中菜单索引
            selectedMenuIndex: '',
            // 当前选中子菜单索引
            selectedSubMenuIndex: '',
            // 选中菜单级别
            selectedMenuLevel: 0,
            // 选中的菜单按钮
            selectedButton: '',
            // 当前鼠标拖动到的位置
            onDragOverMenu: '',
        };
    },
    /** 挂载组件后初始化控件并读取所需数据. */
    mounted() {
        this.getWxMenu();
    },
    methods: {
        /** 将编辑结果提交到当前主菜单或子菜单, 保持数据归属清晰. */
        updateSelectedButton(button) {
            if (this.selectedMenuLevel === 1) {
                this.menu.buttons[this.selectedMenuIndex] = button;
            } else {
                this.menu.buttons[this.selectedMenuIndex].subButtons[this.selectedSubMenuIndex] =
                    button;
            }
            this.selectedButton = button;
        },
        /** 读取公众号当前菜单并恢复编辑状态. */
        getWxMenu() {
            this.$http({
                url: '/manage/wxMenu/getMenu',
            }).then(({data}) => {
                if (data.code === 0 && data.data) {
                    this.menu = data.data.menu;
                }
            });
        },
        // 选中主菜单
        selectMenu(i) {
            this.selectedMenuLevel = 1;
            this.selectedSubMenuIndex = '';
            this.selectedMenuIndex = i;
            this.selectedButton = this.menu.buttons[i];
        },
        // 选中子菜单
        selectSubMenu(i, i2) {
            this.selectedMenuLevel = 2;
            this.selectedMenuIndex = i;
            this.selectedSubMenuIndex = i2;
            this.selectedButton = this.menu.buttons[i].subButtons[i2];
        },
        // 添加菜单
        addMenu(level, i) {
            if (level === 1 && this.menu.buttons.length < 3) {
                this.menu.buttons.push({
                    type: 'view',
                    name: '菜单名称',
                    subButtons: [],
                    url: '',
                });
                this.selectMenu(this.menu.buttons.length - 1);
            }
            if (level === 2 && this.menu.buttons[i].subButtons.length < 5) {
                this.menu.buttons[i].subButtons.push({
                    type: 'view',
                    name: '子菜单名称',
                    url: '',
                });
                this.selectSubMenu(i, this.menu.buttons[i].subButtons.length - 1);
            }
        },
        // 删除菜单
        delMenu() {
            if (this.selectedMenuLevel === 1 && window.confirm('删除后菜单下设置的内容将被删除')) {
                this.menu.buttons.splice(this.selectedMenuIndex, 1);
                this.unSelectMenu();
            } else if (this.selectedMenuLevel === 2) {
                this.menu.buttons[this.selectedMenuIndex].subButtons.splice(
                    this.selectedSubMenuIndex,
                    1
                );
                this.unSelectMenu();
            }
        },
        /** 清除当前公众号菜单节点的选中状态. */
        unSelectMenu() {
            // 不选中任何菜单
            this.selectedMenuLevel = 0;
            this.selectedMenuIndex = '';
            this.selectedSubMenuIndex = '';
            this.selectedButton = '';
        },
        /** 将当前菜单编辑结果提交到公众号菜单接口. */
        updateWxMenu() {
            this.$http({
                url: '/manage/wxMenu/updateMenu',
                data: this.menu,
                method: 'post',
            }).then(({data}) => {
                if (data.code === 0) {
                    this.$message.success('操作成功');
                }
            });
        },
        /** 根据拖放目标调整公众号菜单节点的位置. */
        onDrop(i, i2) {
            // 拖拽移动位置
            this.onDragOverMenu = '';
            // 拖拽到了原位置
            if (i === this.selectedMenuIndex && i2 === this.selectedSubMenuIndex) {
                return;
            }
            if (i !== this.selectedMenuIndex && this.menu.buttons[i].subButtons.length >= 5) {
                this.$message.error('目标组已满');
                return;
            }
            this.menu.buttons[i].subButtons.splice(i2, 0, this.selectedButton);
            let delSubIndex = this.selectedSubMenuIndex;
            if (i === this.selectedMenuIndex && i2 < this.selectedSubMenuIndex) {
                delSubIndex++;
            }
            this.menu.buttons[this.selectedMenuIndex].subButtons.splice(delSubIndex, 1);
            this.unSelectMenu();
        },
    },
};
</script>
<style src="@/assets/css/wx-menu.css"></style>
