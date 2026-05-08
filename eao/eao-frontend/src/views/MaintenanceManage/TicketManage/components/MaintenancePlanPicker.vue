<script setup>
import { reactive } from 'vue'

const props = defineProps({
  modelValue: Boolean,
  rows: { type: Array, default: () => [] }
})
const emit = defineEmits(['update:modelValue', 'confirm', 'selection-change'])

const q = reactive({ planCode: '', productionLineCode: '', maintenanceCategory: '' })
</script>

<template>
  <el-dialog
    :model-value="modelValue"
    title="选择检修计划"
    width="72%"
    @update:model-value="(v) => emit('update:modelValue', v)"
  >
    <div class="search-grid">
      <el-input v-model="q.planCode" placeholder="计划编号" />
      <el-input v-model="q.productionLineCode" placeholder="生产线代码" />
      <el-select v-model="q.maintenanceCategory" clearable placeholder="检修分类">
        <el-option label="01-日修" value="01-日修" />
        <el-option label="02-定修" value="02-定修" />
        <el-option label="03-年修" value="03-年修" />
        <el-option label="04-抢修" value="04-抢修" />
      </el-select>
    </div>
    <el-table :data="rows" border @selection-change="(rows) => emit('selection-change', rows)">
      <el-table-column type="selection" width="48" />
      <el-table-column prop="planCode" label="计划编号" min-width="140" />
      <el-table-column prop="productionLineCode" label="生产线代码" min-width="120" />
      <el-table-column prop="productionLineName" label="生产线名称" min-width="120" />
      <el-table-column prop="maintenanceCategory" label="检修分类" min-width="120" />
      <el-table-column prop="maintenanceStartTime" label="检修开始时间" min-width="160" />
      <el-table-column prop="maintenanceEndTime" label="检修结束时间" min-width="160" />
      <el-table-column prop="status" label="状态" min-width="100" />
    </el-table>
    <template #footer>
      <el-button @click="emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" @click="emit('confirm')">确定</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.search-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(180px, 1fr));
  gap: 12px;
  margin-bottom: 12px;
}
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
