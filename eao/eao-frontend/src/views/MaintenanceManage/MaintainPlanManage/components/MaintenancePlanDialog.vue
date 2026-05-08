<script setup>
import { computed, reactive, watch } from 'vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    required: true
  },
  form: {
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
  mode: {
    type: String,
    default: 'make'
  },
  saving: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'save', 'approve', 'rollback'])

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const localForm = reactive({
  id: '',
  productionLineCode: '',
  productionLineName: '',
  planCode: '系统生成',
  maintenanceCategory: '',
  maintenanceStartTime: '',
  maintenanceEndTime: '',
  status: '',
  creatorUsername: '',
  creatorName: '',
  createdAt: ''
})

watch(
  () => props.form,
  (value) => {
    Object.assign(localForm, {
      id: value?.id || '',
      productionLineCode: value?.productionLineCode || '',
      productionLineName: value?.productionLineName || '',
      planCode: value?.planCode || '系统生成',
      maintenanceCategory: value?.maintenanceCategory || '',
      maintenanceStartTime: value?.maintenanceStartTime || '',
      maintenanceEndTime: value?.maintenanceEndTime || '',
      status: value?.status || '',
      creatorUsername: value?.creatorUsername || '',
      creatorName: value?.creatorName || '',
      createdAt: value?.createdAt || ''
    })
  },
  { immediate: true, deep: true }
)

const isApproveMode = computed(() => props.mode === 'approve')
</script>

<template>
  <el-dialog
    v-model="visible"
    :title="isApproveMode ? '检修计划审核' : '检修计划编制'"
    width="760px"
  >
    <el-form :model="localForm" label-width="110px" class="plan-form">
      <div class="form-grid">
        <el-form-item label="生产线代码">
          <el-input v-model="localForm.productionLineCode" disabled />
        </el-form-item>
        <el-form-item label="生产线名称">
          <el-input v-model="localForm.productionLineName" disabled />
        </el-form-item>
        <el-form-item label="计划编号">
          <el-input v-model="localForm.planCode" disabled />
        </el-form-item>
        <el-form-item label="检修分类">
          <el-select
            v-model="localForm.maintenanceCategory"
            :disabled="isApproveMode"
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
        </el-form-item>
        <el-form-item label="检修开始时间">
          <el-date-picker
            v-model="localForm.maintenanceStartTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="请选择检修开始时间"
            :disabled="isApproveMode"
          />
        </el-form-item>
        <el-form-item label="检修结束时间">
          <el-date-picker
            v-model="localForm.maintenanceEndTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="请选择检修结束时间"
            :disabled="isApproveMode"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="localForm.status" disabled filterable allow-create>
            <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="创建人">
          <el-input v-model="localForm.creatorUsername" disabled />
        </el-form-item>
        <el-form-item label="创建人姓名">
          <el-input v-model="localForm.creatorName" disabled />
        </el-form-item>
        <el-form-item label="创建时间">
          <el-input v-model="localForm.createdAt" disabled />
        </el-form-item>
      </div>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="visible = false">关闭</el-button>
        <template v-if="isApproveMode">
          <el-button type="warning" :loading="saving" @click="emit('rollback', { ...localForm })">
            回退
          </el-button>
          <el-button type="primary" :loading="saving" @click="emit('approve', { ...localForm })">
            同意
          </el-button>
        </template>
        <template v-else>
          <el-button type="primary" :loading="saving" @click="emit('save', { ...localForm })">
            保存
          </el-button>
        </template>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  column-gap: 14px;
}
.plan-form :deep(.el-select),
.plan-form :deep(.el-date-editor) {
  width: 100%;
}
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
