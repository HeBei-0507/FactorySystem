<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, Plus, Search } from '@element-plus/icons-vue'
import { getProductionlineList } from '@/api/prodectionLine'
import {
  addMaintenancePlan,
  batchDeleteMaintenancePlan,
  getMaintenancePlanById,
  getMaintenancePlanPage,
  submitMaintenancePlan,
  updateMaintenancePlan
} from '@/api/maintenancePlan'
import { extractPageData, extractRecordsArray } from '@/util/apiData'
import { getUserProfile } from '@/util/authStorage'
import MaintenancePlanSearchForm from './components/MaintenancePlanSearchForm.vue'
import MaintenancePlanDialog from './components/MaintenancePlanDialog.vue'
import MaintenancePlanLineTree from './components/MaintenancePlanLineTree.vue'

const CATEGORY_OPTIONS = [
  { value: '01-日修', label: '01-日修' },
  { value: '02-定修', label: '02-定修' },
  { value: '03-年修', label: '03-年修' }
]

const STATUS_OPTIONS = [
  { value: '00-待送审', label: '00-待送审' },
  { value: '10-已送审', label: '10-已送审' },
  { value: '20-已通过', label: '20-已通过' },
  { value: '30-已申请延期', label: '30-已申请延期' },
  { value: '40-延期已送审', label: '40-延期已送审' },
  { value: '50-延期通过', label: '50-延期通过' }
]

const treeLoading = ref(false)
const tableLoading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const lines = ref([])
const rows = ref([])
const selectedIds = ref([])
const activeLineId = ref('')

const filters = reactive({
  planCode: '',
  productionLineCode: '',
  maintenanceCategory: '',
  status: ''
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
  planCode: '系统生成',
  maintenanceCategory: '',
  maintenanceStartTime: '',
  maintenanceEndTime: '',
  status: '00-待送审',
  creatorUsername: '',
  creatorName: '',
  createdAt: ''
})

const activeLine = computed(
  () => lines.value.find((item) => item.id === activeLineId.value) || null
)
const currentPageCount = computed(() => rows.value.length)

function resetForm() {
  const profile = getUserProfile() || {}
  Object.assign(form, {
    id: '',
    productionLineCode: activeLine.value?.lineCode || '',
    productionLineName: activeLine.value?.lineName || '',
    planCode: '系统生成',
    maintenanceCategory: '',
    maintenanceStartTime: '',
    maintenanceEndTime: '',
    status: '00-待送审',
    creatorUsername: profile?.username || '',
    creatorName: profile?.realName || '',
    createdAt: ''
  })
}

function mapLine(item = {}) {
  return {
    id: item.id,
    lineCode: item.lineCode || '',
    lineName: item.lineName || ''
  }
}

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

async function loadLines() {
  treeLoading.value = true
  try {
    const res = await getProductionlineList()
    lines.value = extractRecordsArray(res?.data).map(mapLine)
    if (!activeLineId.value && lines.value.length) {
      activeLineId.value = lines.value[0].id
    }
    if (!filters.productionLineCode && activeLine.value?.lineCode) {
      filters.productionLineCode = activeLine.value.lineCode
    }
  } catch (e) {
    if (!e?.elMessageNotified) ElMessage.error('获取生产线目录失败')
  } finally {
    treeLoading.value = false
  }
}

async function loadPage() {
  tableLoading.value = true
  try {
    const res = await getMaintenancePlanPage({
      current: pagination.current,
      size: pagination.size,
      planCode: filters.planCode,
      productionLineCode: filters.productionLineCode,
      maintenanceCategory: filters.maintenanceCategory,
      status: filters.status
    })
    const { records, total } = extractPageData(res)
    rows.value = (records || []).map(mapRow)
    pagination.total = total || 0
  } catch (e) {
    if (!e?.elMessageNotified) ElMessage.error('获取检修计划列表失败')
  } finally {
    tableLoading.value = false
  }
}

function handleLineSelect(line) {
  activeLineId.value = line.id
  filters.productionLineCode = line.lineCode || ''
  pagination.current = 1
  loadPage()
}

function handleSearch() {
  pagination.current = 1
  loadPage()
}

function handleReset() {
  filters.productionLineCode = activeLine.value?.lineCode || ''
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

function openAddDialog() {
  if (!activeLine.value) return ElMessage.warning('请先在左侧选择生产线')
  resetForm()
  dialogVisible.value = true
}

async function openEditDialog(row) {
  try {
    const res = await getMaintenancePlanById(row.id)
    Object.assign(form, mapRow(res?.data || {}))
    dialogVisible.value = true
  } catch (e) {
    if (!e?.elMessageNotified) ElMessage.error('获取检修计划详情失败')
  }
}

async function handleSave(payload) {
  if (!payload.productionLineCode) return ElMessage.warning('生产线代码不能为空')
  if (!payload.maintenanceCategory) return ElMessage.warning('请选择检修分类')
  if (!payload.maintenanceStartTime) return ElMessage.warning('请选择检修开始时间')
  if (!payload.maintenanceEndTime) return ElMessage.warning('请选择检修结束时间')
  saving.value = true
  try {
    const req = {
      id: payload.id || undefined,
      productionLineCode: payload.productionLineCode,
      productionLineName: payload.productionLineName,
      maintenanceCategory: payload.maintenanceCategory,
      maintenanceStartTime: payload.maintenanceStartTime,
      maintenanceEndTime: payload.maintenanceEndTime,
      status: payload.status || '00-待送审'
    }
    if (payload.id) {
      await updateMaintenancePlan(req)
      ElMessage.success('修改成功')
    } else {
      await addMaintenancePlan(req)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    await loadPage()
  } catch (e) {
    if (!e?.elMessageNotified) ElMessage.error(payload.id ? '修改失败' : '新增失败')
  } finally {
    saving.value = false
  }
}

async function handleDelete() {
  if (!selectedIds.value.length) return ElMessage.warning('请选择要删除的检修计划')
  try {
    await ElMessageBox.confirm('确认删除所选检修计划吗？仅待送审状态允许删除。', '提示', {
      type: 'warning'
    })
    await batchDeleteMaintenancePlan(selectedIds.value)
    ElMessage.success('删除成功')
    selectedIds.value = []
    await loadPage()
  } catch (e) {
    if (e === 'cancel') return
    if (!e?.elMessageNotified) ElMessage.error('删除失败')
  }
}

async function handleSubmit() {
  if (!selectedIds.value.length) return ElMessage.warning('请选择要送审的检修计划')
  try {
    await submitMaintenancePlan(selectedIds.value)
    ElMessage.success('送审成功')
    selectedIds.value = []
    await loadPage()
  } catch (e) {
    if (!e?.elMessageNotified) ElMessage.error('送审失败')
  }
}

onMounted(async () => {
  await loadLines()
  await loadPage()
})
</script>

<template>
  <div class="plan-page">
    <div class="page-title">检修计划编制</div>
    <div class="page-main">
      <MaintenancePlanLineTree
        :lines="lines"
        :active-line-id="activeLineId"
        :loading="treeLoading"
        @select="handleLineSelect"
      />

      <section class="content-panel">
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
              <el-button type="primary" :icon="Plus" @click="openAddDialog">新增</el-button>
              <el-button type="danger" @click="handleDelete">删除</el-button>
              <el-button type="primary" @click="handleSubmit">送审</el-button>
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
            v-loading="tableLoading"
            :data="rows"
            border
            stripe
            @selection-change="handleSelectionChange"
          >
            <el-table-column type="selection" width="48" align="center" />
            <el-table-column label="操作" width="70" align="center">
              <template #default="{ row }">
                <el-button type="primary" link :icon="Document" @click="openEditDialog(row)" />
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
      </section>
    </div>

    <MaintenancePlanDialog
      v-model="dialogVisible"
      :form="form"
      :category-options="CATEGORY_OPTIONS"
      :status-options="STATUS_OPTIONS"
      :saving="saving"
      mode="make"
      @save="handleSave"
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
.page-main {
  display: grid;
  grid-template-columns: 260px 1fr;
  gap: 8px;
}
.content-panel {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.table-card {
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
