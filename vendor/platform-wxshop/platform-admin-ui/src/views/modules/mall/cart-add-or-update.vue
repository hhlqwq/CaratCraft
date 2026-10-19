<template>
  <el-dialog title="查看" :close-on-click-modal="false" :lock-scroll="false" v-model="visible">
    <el-descriptions :column="3" border>
      <el-descriptions-item>
        <template #label> 所属店铺 </template>
        {{ dataForm.shopsName }}
      </el-descriptions-item>
      <el-descriptions-item>
        <template #label> 商品名称 </template>
        {{ dataForm.goodsName }}
      </el-descriptions-item>
      <el-descriptions-item>
        <template #label> 商品编码 </template>
        {{ dataForm.goodsSn }}
      </el-descriptions-item>
      <el-descriptions-item>
        <template #label> 规格属性 </template>
        {{ dataForm.goodsSpecifitionNameValue }}
      </el-descriptions-item>
      <el-descriptions-item>
        <template #label> 市场价 </template>
        {{ dataForm.marketPrice }}
      </el-descriptions-item>
      <el-descriptions-item>
        <template #label> 零售价格 </template>
        {{ dataForm.retailPrice }}
      </el-descriptions-item>
      <el-descriptions-item>
        <template #label> 数量 </template>
        {{ dataForm.number }}
      </el-descriptions-item>
      <el-descriptions-item>
        <template #label> 商品图片 </template>
        <el-img v-model="dataForm.listPicUrl" disabled> </el-img>
      </el-descriptions-item>
      <el-descriptions-item>
        <template #label> 是否选中 </template>
        <el-tag v-if="dataForm.checked === 0" size="small" type="danger">否</el-tag>
        <el-tag v-else-if="dataForm.checked === 1" size="small" type="success">是</el-tag>
      </el-descriptions-item>
    </el-descriptions>
    <template #footer
      ><span class="dialog-footer">
        <el-button @click="visible = false">取消</el-button>
      </span></template
    >
  </el-dialog>
</template>

<script>
export default {
  compatConfig: {MODE: 2},
  data() {
    return {
      visible: false,
      dataForm: {
        id: '',
        userId: '',
        goodsId: '',
        goodsSn: '',
        goodsName: '',
        skuId: '',
        marketPrice: '',
        retailPrice: '',
        number: '',
        goodsSpecifitionNameValue: '',
        checked: '',
        listPicUrl: '',
        shopsId: '',
        shopsName: '',
      },
    };
  },
  methods: {
    init(id, shopsName) {
      this.dataForm.id = id || '';
      this.visible = true;
      this.$nextTick(() => {
        if (this.dataForm.id) {
          this.$http({
            url: `/mall/cart/info/${this.dataForm.id}`,
            method: 'get',
          }).then(({data}) => {
            if (data && data.code === 0) {
              this.dataForm = data.data;
              this.dataForm.shopsName = shopsName || '';
            }
          });
        }
      });
    },
  },
};
</script>
