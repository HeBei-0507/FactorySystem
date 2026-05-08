<script setup>
const props = defineProps({
  modelValue: Boolean,
  rows: { type: Array, default: () => [] }
})
const emit = defineEmits(['update:modelValue', 'confirm', 'selection-change'])
</script>

<template>
  <el-dialog
    :model-value="modelValue"
    title="选择物料"
    width="70%"
    @update:model-value="(v) => emit('update:modelValue', v)"
  >
    <el-table :data="rows" border @selection-change="(rows) => emit('selection-change', rows)">
      <el-table-column type="selection" width="48" />
      <el-table-column prop="materialCode" label="物料代码" />
      <el-table-column prop="materialName" label="物料名称" />
      <el-table-column prop="materialSubCategory" label="物料分类" />
      <el-table-column prop="modelSpecification" label="规格型号" />
      <el-table-column prop="inboundNo" label="入库单号" />
    </el-table>
    <template #footer>
      <el-button @click="emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" @click="emit('confirm')">确定</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
:deep(.el-dialog__header) {
  border-bottom: 1px solid #e6edf6;
  margin-right: 0;
  padding-bottom: 14px;
}
:deep(.el-table__header th) {
  background: #e7f0ff;
  color: #365078;
  font-size: 12px;
  font-weight: 700;
}
:deep(.el-table__body td) {
  font-size: 12px;
}
</style>
