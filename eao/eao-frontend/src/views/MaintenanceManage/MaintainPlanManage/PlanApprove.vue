<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Document, Search } from '@element-plus/icons-vue'
import {
  getMaintenancePlanById,
  getMaintenancePlanPage,
  approveMaintenancePlan,
  rollbackMaintenancePlan
} from '@/api/maintenancePlan'
import { extractPageData } from '@/util/apiData'
import MaintenancePlanSearchForm from './components/MaintenancePlanSearchForm.vue'
import MaintenancePlanDialog from './components/MaintenancePlanDialog.vue'

const CATEGORY_OPTIONS = [
  { value: '01-日修', label: '01-日修' },
  { value: '02-定修', label: '02-定修' },
  { value: '03-年修', label: '03-年修' }
]

const STATUS_OPTIONS = [
  { value: '10-已送审', label: '10-已送审' },
  { value: '20-已通过', label: '20-已通过' },
  { value: '30-已申请延期', label: '30-已申请延期' },
  { value: '40-延期已送审', label: '40-延期已送审' },
  { value: '50-延期通过', label: '50-延期通过' }
]

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const rows = ref([])
const selectedIds = ref([])

const filters = reactive({
  planCode: '',
  productionLineCode: '',
  maintenanceCategory: '',
  status: '10-已送审'
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const form = reactive({
  id: '',
  productionLineCode: '',
  productionLineName: '',
  planCode: '',
  maintenanceCategory: '',
  maintenanceStartTime: '',
  maintenanceEndTime: '',
  status: '',
  creatorUsername: '',
  creatorName: '',
  createdAt: ''
})

const currentPageCount = ref(0)

function mapRow(item = {}) {
  return {
    id: item.id,
    planCode: item.planCode || '',
    productionLineCode: item.productionLineCode || '',
    productionLineName: item.productionLineName || '',
    maintenanceCategory: item.maintenanceCategory || '',
    maintenanceStartTime: item.maintenanceStartTime || '',
    maintenanceEndTime: item.maintenanceEndTime || '',
    status: item.status || '',
    creatorUsername: item.creatorUsername || '',
    creatorName: item.creatorName || '',
    createdAt: item.createdAt || ''
  }
}

async function loadPage() {
  loading.value = true
  try {
    const res = await getMaintenancePlanPage({
      current: pagination.current,
      size: pagination.size,
      planCode: filters.planCode,
      productionLineCode: filters.productionLineCode,
      maintenanceCategory: filters.maintenanceCategory,
      status: filters.status,
      excludePending: true
    })
    const { records, total } = extractPageData(res)
    rows.value = (records || []).map(mapRow)
    pagination.total = total || 0
    currentPageCount.value = rows.value.length
  } catch (e) {
    if (!e?.elMessageNotified) ElMessage.error('获取检修计划审核列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.current = 1
  loadPage()
}

function handleReset() {
  filters.status = '10-已送审'
  pagination.current = 1
  loadPage()
}

function handleSelectionChange(selection) {
  selectedIds.value = selection.map((item) => item.id)
}

function handleSizeChange(size) {
  pagination.size = size
  pagination.current = 1
  loadPage()
}

function handleCurrentChange(page) {
  pagination.current = page
  loadPage()
}

async function openDialog(row) {
  try {
    const res = await getMaintenancePlanById(row.id)
    Object.assign(form, mapRow(res?.data || {}))
    dialogVisible.value = true
  } catch (e) {
    if (!e?.elMessageNotified) ElMessage.error('获取检修计划详情失败')
  }
}

async function handleApprove(target) {
  const ids = target?.id ? [target.id] : selectedIds.value
  if (!ids.length) return ElMessage.warning('请选择要同意的检修计划')
  saving.value = true
  try {
    await approveMaintenancePlan(ids)
    ElMessage.success('同意成功')
    dialogVisible.value = false
    selectedIds.value = []
    await loadPage()
  } catch (e) {
    if (!e?.elMessageNotified) ElMessage.error('同意失败')
  } finally {
    saving.value = false
  }
}

async function handleRollback(target) {
  const ids = target?.id ? [target.id] : selectedIds.value
  if (!ids.length) return ElMessage.warning('请选择要回退的检修计划')
  saving.value = true
  try {
    await rollbackMaintenancePlan(ids)
    ElMessage.success('回退成功')
    dialogVisible.value = false
    selectedIds.value = []
    await loadPage()
  } catch (e) {
    if (!e?.elMessageNotified) ElMessage.error('回退失败')
  } finally {
    saving.value = false
  }
}

onMounted(loadPage)
</script>

<template>
  <div class="plan-page">
    <div class="page-title">检修计划审核</div>
    <MaintenancePlanSearchForm
      v-model="filters"
      :category-options="CATEGORY_OPTIONS"
      :status-options="STATUS_OPTIONS"
      @search="handleSearch"
      @reset="handleReset"
    />

    <div class="table-card">
      <div class="table-title">结果集</div>
      <div class="toolbar">
        <div class="toolbar-left">
          <el-button type="primary" @click="handleApprove()">同意</el-button>
          <el-button type="warning" @click="handleRollback()">回退</el-button>
        </div>
        <div class="toolbar-right">
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <span class="page-count">当前页共 {{ currentPageCount }} 条</span>
          <el-pagination
            small
            background
            layout="sizes, prev, pager, next"
            :current-page="pagination.current"
            :page-size="pagination.size"
            :page-sizes="[10, 20, 50, 100]"
            :total="pagination.total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>

      <el-table
        v-loading="loading"
        :data="rows"
        border
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="48" align="center" />
        <el-table-column label="操作" width="70" align="center">
          <template #default="{ row }">
            <el-button type="primary" link :icon="Document" @click="openDialog(row)" />
          </template>
        </el-table-column>
        <el-table-column prop="productionLineCode" label="生产线代码" min-width="120" />
        <el-table-column prop="productionLineName" label="生产线名称" min-width="140" />
        <el-table-column prop="planCode" label="计划编号" min-width="150" />
        <el-table-column prop="maintenanceCategory" label="检修分类" min-width="110" />
        <el-table-column prop="maintenanceStartTime" label="检修开始时间" min-width="170" />
        <el-table-column prop="maintenanceEndTime" label="检修结束时间" min-width="170" />
        <el-table-column prop="status" label="状态" min-width="110" />
        <el-table-column prop="creatorName" label="创建人姓名" min-width="110" />
        <el-table-column prop="createdAt" label="创建时间" min-width="170" />
      </el-table>
    </div>

    <MaintenancePlanDialog
      v-model="dialogVisible"
      :form="form"
      :category-options="CATEGORY_OPTIONS"
      :status-options="STATUS_OPTIONS"
      :saving="saving"
      mode="approve"
      @approve="handleApprove"
      @rollback="handleRollback"
    />
  </div>
</template>

<style scoped>
.plan-page {
  min-height: calc(100vh - 36px);
  padding: 10px;
  background: #f5f7fa;
}
.page-title {
  margin-bottom: 8px;
  color: #2f3b52;
  font-size: 14px;
  font-weight: 700;
}
.table-card {
  margin-top: 8px;
  padding: 12px;
  border: 1px solid #d9e2ef;
  background: #fff;
}
.table-title {
  margin-bottom: 10px;
  color: #33507b;
  font-size: 13px;
  font-weight: 700;
}
.toolbar {
  margin-bottom: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
.toolbar-left,
.toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.page-count {
  color: #4b5d73;
  font-size: 13px;
}
</style>
