<script setup>
import { reactive, watch } from 'vue'

const props = defineProps({
  modelValue: {
    type: Object,
    required: true
  },
  categoryOptions: {
    type: Array,
    default: () => []
  },
  statusOptions: {
    type: Array,
    default: () => []
  },
  showStatus: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['update:modelValue', 'search', 'reset'])

const localFilters = reactive({
  planCode: '',
  productionLineCode: '',
  maintenanceCategory: '',
  status: ''
})

watch(
  () => props.modelValue,
  (value) => {
    Object.assign(localFilters, {
      planCode: value?.planCode || '',
      productionLineCode: value?.productionLineCode || '',
      maintenanceCategory: value?.maintenanceCategory || '',
      status: value?.status || ''
    })
  },
  { immediate: true, deep: true }
)

watch(
  localFilters,
  (value) => {
    emit('update:modelValue', { ...value })
  },
  { deep: true }
)

function onReset() {
  Object.assign(localFilters, {
    planCode: '',
    productionLineCode: '',
    maintenanceCategory: '',
    status: ''
  })
  emit('update:modelValue', { ...localFilters })
  emit('reset')
}
</script>

<template>
  <div class="search-panel">
    <div class="search-grid">
      <div class="search-item">
        <span class="search-label">计划编号</span>
        <el-input v-model="localFilters.planCode" clearable placeholder="请输入计划编号" />
      </div>
      <div class="search-item">
        <span class="search-label">生产线代码</span>
        <el-input
          v-model="localFilters.productionLineCode"
          clearable
          placeholder="请输入生产线代码"
        />
      </div>
      <div class="search-item">
        <span class="search-label">检修分类</span>
        <el-select
          v-model="localFilters.maintenanceCategory"
          clearable
          filterable
          allow-create
          default-first-option
          placeholder="请选择检修分类"
        >
          <el-option
            v-for="item in categoryOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </div>
      <div v-if="showStatus" class="search-item">
        <span class="search-label">状态</span>
        <el-select
          v-model="localFilters.status"
          clearable
          filterable
          allow-create
          default-first-option
          placeholder="请选择状态"
        >
          <el-option
            v-for="item in statusOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </div>
    </div>
    <div class="search-actions">
      <el-button type="primary" @click="$emit('search')">查询</el-button>
      <el-button @click="onReset">重置</el-button>
    </div>
  </div>
</template>

<style scoped>
.search-panel {
  padding: 12px;
  border: 1px solid #d9e2ef;
  background: #fff;
}
.search-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(220px, 1fr));
  gap: 14px 18px;
}
.search-item {
  display: flex;
  align-items: center;
  gap: 10px;
}
.search-label {
  min-width: 78px;
  color: #4b5d73;
  font-size: 13px;
}
.search-actions {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
