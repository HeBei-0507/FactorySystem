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
    title="选择检修标准"
    width="72%"
    @update:model-value="(v) => emit('update:modelValue', v)"
  >
    <el-table :data="rows" border @selection-change="(rows) => emit('selection-change', rows)">
      <el-table-column type="selection" width="48" />
      <el-table-column prop="standardCode" label="标准项目编码" min-width="140" />
      <el-table-column prop="partCode" label="设备部位编码" min-width="120" />
      <el-table-column prop="partName" label="设备部位名称" min-width="120" />
      <el-table-column prop="itemName" label="项目名称" min-width="120" />
      <el-table-column prop="profession" label="专业" min-width="100" />
      <el-table-column prop="workCategory" label="工事分类" min-width="140" />
      <el-table-column prop="acceptanceLevel" label="验收等级" min-width="120" />
      <el-table-column prop="maintenanceCategory" label="检修分类" min-width="120" />
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
</style>
