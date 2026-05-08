<script setup>
import { reactive, watch } from 'vue'

const props = defineProps({
  modelValue: {
    type: Object,
    required: true
  },
  statusOptions: {
    type: Array,
    default: () => []
  },
  failureTypeOptions: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue', 'search', 'reset'])

const localFilters = reactive({
  failureCode: '',
  devicePartCode: '',
  status: '',
  failureType: '',
  failureName: '',
  failureStartTime: ''
})

watch(
  () => props.modelValue,
  (value) => {
    Object.assign(localFilters, {
      failureCode: value?.failureCode || '',
      devicePartCode: value?.devicePartCode || '',
      status: value?.status || '',
      failureType: value?.failureType || '',
      failureName: value?.failureName || '',
      failureStartTime: value?.failureStartTime || ''
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
    failureCode: '',
    devicePartCode: '',
    status: '',
    failureType: '',
    failureName: '',
    failureStartTime: ''
  })
  emit('update:modelValue', { ...localFilters })
  emit('reset')
}
</script>

<template>
  <div class="search-panel">
    <div class="search-grid">
      <div class="search-item">
        <span class="search-label">故障编号</span>
        <el-input v-model="localFilters.failureCode" clearable placeholder="请输入故障编号" />
      </div>
      <div class="search-item">
        <span class="search-label">设备部位编码</span>
        <el-input
          v-model="localFilters.devicePartCode"
          clearable
          placeholder="请输入设备部位编码"
        />
      </div>
      <div class="search-item">
        <span class="search-label">状态</span>
        <el-select v-model="localFilters.status" clearable placeholder="请选择状态">
          <el-option
            v-for="item in statusOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </div>
      <div class="search-item">
        <span class="search-label">故障分类</span>
        <el-select v-model="localFilters.failureType" clearable placeholder="请选择故障分类">
          <el-option
            v-for="item in failureTypeOptions"
            :key="item.value || 'empty'"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </div>
      <div class="search-item">
        <span class="search-label">故障名称</span>
        <el-input v-model="localFilters.failureName" clearable placeholder="请输入故障名称" />
      </div>
      <div class="search-item">
        <span class="search-label">故障开始时间</span>
        <el-date-picker
          v-model="localFilters.failureStartTime"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="故障开始时间"
          clearable
          class="search-date"
        />
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
  min-width: 90px;
  color: #4b5d73;
  font-size: 13px;
}
.search-date {
  width: 100%;
}
.search-actions {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
