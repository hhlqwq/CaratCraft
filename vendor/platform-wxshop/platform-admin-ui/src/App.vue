<template>
    <router-view v-slot="{Component}"
        ><transition name="fade"><component :is="Component" /></transition
    ></router-view>
</template>
<script></script>

<style>
.table-expand {
    font-size: 0;
}

.table-expand label {
    width: 90px;
    color: #99a9bf;
}

.table-expand .el-form-item {
    margin-right: 0;
    margin-bottom: 0;
    width: 48%;
}
</style>
