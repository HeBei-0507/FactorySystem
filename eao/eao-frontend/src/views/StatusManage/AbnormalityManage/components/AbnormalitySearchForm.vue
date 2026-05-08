// 界面上方筛选框
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
  abnormalTypeOptions: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue', 'search', 'reset'])

const localFilters = reactive({
  abnormalCode: '',
  deviceUnitCode: '',
  status: '',
  abnormalType: '',
  reporter: '',
  reportDate: ''
})

watch(
  () => props.modelValue,
  (value) => {
    Object.assign(localFilters, {
      abnormalCode: value?.abnormalCode || '',
      deviceUnitCode: value?.deviceUnitCode || '',
      status: value?.status || '',
      abnormalType: value?.abnormalType || '',
      reporter: value?.reporter || '',
      reportDate: value?.reportDate || ''
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
    abnormalCode: '',
    deviceUnitCode: '',
    status: '',
    abnormalType: '',
    reporter: '',
    reportDate: ''
  })
  emit('update:modelValue', { ...localFilters })
  emit('reset')
}
</script>

<template>
  <div class="search-panel">
    <div class="search-grid">
      <div class="search-item">
        <span class="search-label">异常单编号</span>
        <el-input v-model="localFilters.abnormalCode" clearable placeholder="请输入异常单编号" />
      </div>
      <div class="search-item">
        <span class="search-label">设备部位编码</span>
        <el-input
          v-model="localFilters.deviceUnitCode"
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
        <span class="search-label">异常类型</span>
        <el-select v-model="localFilters.abnormalType" clearable placeholder="请选择异常类型">
          <el-option
            v-for="item in abnormalTypeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </div>
      <div class="search-item">
        <span class="search-label">报出人</span>
        <el-input v-model="localFilters.reporter" clearable placeholder="请输入报出人" />
      </div>
      <div class="search-item">
        <span class="search-label">报出日期</span>
        <el-date-picker
          v-model="localFilters.reportDate"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="报出日期"
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
  min-width: 78px;
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
