<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getProductionlineList } from '@/api/prodectionLine'
import {
  addDeviceUnit,
  batchDeleteDeviceUnit,
  getDeviceUnitPage,
  updateDeviceUnit
} from '@/api/deviceUnit'

const currentUserId = '1'
const currentUserName = 'zhangsan'
const productionLines = ref([])
const activeLineId = ref('')
const activeLineName = ref('未选择生产线')
const tableRef = ref()
const tableData = ref([])
const selectedRows = ref([])
const editingRowId = ref('')
const creatingRowId = ref('')
const loading = ref(false)
const filters = reactive({ unitCode: '', unitName: '' })
const pagination = reactive({ currentPage: 1, pageSize: 5, total: 0 })

function resetSelection() {
  selectedRows.value = []
  if (tableRef.value) tableRef.value.clearSelection()
}
function formatDateTime(value) {
  return value ? String(value).slice(0, 10) : ''
}
function normalizeLine(item) {
  return { id: item.id, name: item.lineName }
}
function normalizeDeviceUnit(item) {
  return {
    id: item.id,
    productionLineId: item.productionLineId,
    productionLineName: item.productionLineName,
    unitCode: item.unitCode,
    unitName: item.unitName,
    creatorId: String(item.creatorId ?? ''),
    creatorName: item.creatorName,
    createDate: formatDateTime(item.createAt)
  }
}

async function loadProductionLines() {
  try {
    const res = await getProductionlineList()
    const rawList = res?.data || []
    productionLines.value = rawList.map(normalizeLine).filter((item) => item.id)
    if (productionLines.value.length && !activeLineId.value) {
      activeLineId.value = productionLines.value[0].id
      activeLineName.value = productionLines.value[0].name
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('获取生产线列表失败')
  }
}

async function loadDeviceUnitPage() {
  if (!activeLineId.value) {
    tableData.value = []
    pagination.total = 0
    return
  }
  loading.value = true
  try {
    const res = await getDeviceUnitPage({
      current: pagination.currentPage,
      size: pagination.pageSize,
      productionLineId: activeLineId.value,
      unitCode: filters.unitCode,
      unitName: filters.unitName
    })
    console.log(res)
    const data = res?.data
    const records = data?.records || []
    tableData.value = records.map(normalizeDeviceUnit)
    pagination.total = data?.total ?? records.length
  } catch (error) {
    console.error(error)
    ElMessage.error('获取设备单元分页数据失败')
  } finally {
    loading.value = false
  }
}

function handleLineClick(line) {
  activeLineId.value = line.id
  activeLineName.value = line.name
  creatingRowId.value = ''
  editingRowId.value = ''
  filters.unitCode = ''
  filters.unitName = ''
  pagination.currentPage = 1
  resetSelection()
  loadDeviceUnitPage()
}
function handleSelectionChange(rows) {
  selectedRows.value = rows
}
function handlePageChange(page) {
  pagination.currentPage = page
  resetSelection()
  loadDeviceUnitPage()
}
function handleSearch() {
  pagination.currentPage = 1
  resetSelection()
  loadDeviceUnitPage()
}
function isEditingRow(row) {
  return editingRowId.value === row.id
}

function createEmptyRow() {
  if (!activeLineId.value) return ElMessage.warning('请先选择生产线')
  if (creatingRowId.value) return ElMessage.warning('请先完成当前新增行')
  tableData.value.unshift({
    id: `draft-${Date.now()}`,
    productionLineId: activeLineId.value,
    productionLineName: activeLineName.value,
    unitCode: '',
    unitName: '',
    creatorId: currentUserId,
    creatorName: currentUserName,
    createDate: new Date().toISOString().slice(0, 10),
    isNew: true
  })
  creatingRowId.value = tableData.value[0].id
  editingRowId.value = tableData.value[0].id
  resetSelection()
}
function startEdit() {
  if (selectedRows.value.length !== 1) return ElMessage.warning('请选择一条需要修改的设备单元')
  const row = selectedRows.value[0]
  editingRowId.value = row.id
  creatingRowId.value = row.isNew ? row.id : ''
}
function validateRow(row) {
  if (!String(row.productionLineName || '').trim())
    return (ElMessage.warning('生产线名称不能为空'), false)
  if (!String(row.unitCode || '').trim()) return (ElMessage.warning('设备单元代码不能为空'), false)
  if (!String(row.unitName || '').trim()) return (ElMessage.warning('设备单元名称不能为空'), false)
  if (!String(row.creatorId || '').trim()) return (ElMessage.warning('创建人不能为空'), false)
  if (!String(row.creatorName || '').trim()) return (ElMessage.warning('创建人姓名不能为空'), false)
  if (!String(row.createDate || '').trim()) return (ElMessage.warning('创建日期不能为空'), false)
  return true
}
async function submitCreate() {
  const row = tableData.value.find((item) => item.id === creatingRowId.value)
  if (!row) return ElMessage.warning('当前没有待新增的设备单元')
  if (!validateRow(row)) return
  const payload = {
    productionLineId: row.productionLineId,
    unitCode: row.unitCode,
    unitName: row.unitName,
    creatorId: row.creatorId,
    creatorName: row.creatorName,
    createAt: row.createDate,
    updateAt: row.createDate
  }
  try {
    await addDeviceUnit(payload)
    console.log('新增设备单元', payload)
    ElMessage.success('新增成功')
    creatingRowId.value = ''
    editingRowId.value = ''
    loadDeviceUnitPage()
  } catch (error) {
    console.error(error)
    ElMessage.error('新增设备单元失败')
  }
}
async function submitEdit() {
  if (selectedRows.value.length !== 1) return ElMessage.warning('请选择一条需要修改的设备单元')
  const row = selectedRows.value[0]
  if (!validateRow(row)) return
  const payload = {
    id: row.id,
    productionLineId: row.productionLineId,
    unitCode: row.unitCode,
    unitName: row.unitName,
    creatorId: row.creatorId,
    creatorName: row.creatorName,
    createAt: row.createDate,
    updateAt: row.createDate
  }
  try {
    await updateDeviceUnit(payload)
    console.log('修改设备单元', payload)
    ElMessage.success('修改成功')
    editingRowId.value = ''
    loadDeviceUnitPage()
  } catch (error) {
    console.error(error)
    ElMessage.error('修改设备单元失败')
  }
}
async function removeRows() {
  if (!selectedRows.value.length) return ElMessage.warning('请至少选择一条设备单元')
  const ids = selectedRows.value.filter((item) => !item.isNew).map((item) => item.id)
  const draftIds = new Set(selectedRows.value.filter((item) => item.isNew).map((item) => item.id))
  tableData.value = tableData.value.filter((item) => !draftIds.has(item.id))
  if (!ids.length) {
    resetSelection()
    creatingRowId.value = ''
    editingRowId.value = ''
    return ElMessage.success('已删除未保存的新增行')
  }
  try {
    await batchDeleteDeviceUnit(ids)
    console.log('删除设备单元', ids)
    ElMessage.success('删除成功')
    creatingRowId.value = ''
    editingRowId.value = ''
    resetSelection()
    loadDeviceUnitPage()
  } catch (error) {
    console.error(error)
    ElMessage.error('删除设备单元失败')
  }
}

onMounted(async () => {
  await loadProductionLines()
  await loadDeviceUnitPage()
})
</script>

<template>
  <div class="device-unit-page">
    <div class="page-breadcrumb">设备单元管理 &gt; {{ activeLineName }}</div>
    <div class="page-content">
      <aside class="line-panel">
        <div class="line-panel-header">设备单元新增导航栏信息设置</div>
        <div class="line-tree">
          <div
            v-for="line in productionLines"
            :key="line.id"
            :class="['line-item', { active: line.id === activeLineId }]"
            @click="handleLineClick(line)"
          >
            <span class="line-dot"></span>
            <span>{{ line.name }}</span>
          </div>
        </div>
      </aside>
      <section class="table-panel">
        <div class="filter-bar">
          <div class="filter-item">
            <span class="filter-label">设备单元代码</span>
            <el-input
              v-model="filters.unitCode"
              placeholder="请输入设备单元代码"
              clearable
              @change="handleSearch"
            />
          </div>
          <div class="filter-item">
            <span class="filter-label">设备单元名称</span>
            <el-input
              v-model="filters.unitName"
              placeholder="请输入设备单元名称"
              clearable
              @change="handleSearch"
            />
          </div>
        </div>
        <div class="toolbar-row">
          <el-button class="plus-btn" type="primary" :icon="Plus" square @click="createEmptyRow" />
          <div class="toolbar-right">
            <el-pagination
              small
              background
              layout="prev, pager, next"
              :current-page="pagination.currentPage"
              :page-size="pagination.pageSize"
              :total="pagination.total"
              @current-change="handlePageChange"
            />
            <div class="toolbar-actions">
              <el-button type="primary" @click="submitCreate">新增</el-button>
              <el-button type="primary" plain @click="startEdit">修改</el-button>
              <el-button type="danger" plain @click="removeRows">删除</el-button>
            </div>
          </div>
        </div>
        <el-table
          ref="tableRef"
          v-loading="loading"
          :data="tableData"
          border
          class="unit-table"
          header-cell-class-name="table-header-cell"
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="48" align="center" />
          <el-table-column label="生产线名称" min-width="180">
            <template #default="{ row }">
              <el-input
                v-if="isEditingRow(row)"
                v-model="row.productionLineName"
                size="small"
                disabled
              />
              <span v-else>{{ row.productionLineName }}</span>
            </template>
          </el-table-column>
          <el-table-column label="设备单元代码" min-width="130">
            <template #default="{ row }">
              <el-input v-if="isEditingRow(row)" v-model="row.unitCode" size="small" />
              <span v-else>{{ row.unitCode }}</span>
            </template>
          </el-table-column>
          <el-table-column label="设备单元名称" min-width="180">
            <template #default="{ row }">
              <el-input v-if="isEditingRow(row)" v-model="row.unitName" size="small" />
              <span v-else>{{ row.unitName }}</span>
            </template>
          </el-table-column>
          <el-table-column label="创建人" min-width="110">
            <template #default="{ row }">
              <el-input v-if="isEditingRow(row)" v-model="row.creatorId" size="small" />
              <span v-else>{{ row.creatorId }}</span>
            </template>
          </el-table-column>
          <el-table-column label="创建人姓名" min-width="140">
            <template #default="{ row }">
              <el-input v-if="isEditingRow(row)" v-model="row.creatorName" size="small" />
              <span v-else>{{ row.creatorName }}</span>
            </template>
          </el-table-column>
          <el-table-column label="创建日期" min-width="160">
            <template #default="{ row }">
              <el-date-picker
                v-if="isEditingRow(row)"
                v-model="row.createDate"
                type="date"
                value-format="YYYY-MM-DD"
                format="YYYY-MM-DD"
                size="small"
                class="date-picker"
              />
              <span v-else>{{ row.createDate }}</span>
            </template>
          </el-table-column>
        </el-table>
      </section>
    </div>
  </div>
</template>

<style scoped>
.device-unit-page {
  min-height: calc(100vh - 36px);
  padding: 10px 10px 14px;
  background: #f5f7fa;
}
.page-breadcrumb {
  margin-bottom: 8px;
  color: #2f3b52;
  font-size: 14px;
  font-weight: 700;
}
.page-content {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 8px;
  min-height: calc(100vh - 90px);
}
.line-panel,
.table-panel {
  border: 1px solid #d9e2ef;
  background: #fff;
}
.line-panel {
  display: flex;
  flex-direction: column;
}
.line-panel-header {
  padding: 10px 14px;
  border-bottom: 1px solid #e6edf6;
  color: #33507b;
  font-size: 13px;
  font-weight: 700;
}
.line-tree {
  flex: 1;
  padding: 10px 12px;
}
.line-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 9px 10px;
  border-radius: 4px;
  color: #4d5c73;
  font-size: 13px;
  cursor: pointer;
}
.line-item:hover {
  background: #f2f7ff;
}
.line-item.active {
  background: #eaf2ff;
  color: #2f76e8;
  font-weight: 700;
}
.line-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #f56c6c;
}
.table-panel {
  padding: 10px 12px 12px;
}
.filter-bar {
  display: grid;
  grid-template-columns: repeat(2, minmax(260px, 320px));
  gap: 22px;
  margin-bottom: 16px;
}
.filter-item {
  display: flex;
  align-items: center;
  gap: 14px;
}
.filter-label {
  min-width: 90px;
  color: #47566b;
  font-size: 13px;
}
.toolbar-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}
.toolbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.plus-btn {
  width: 28px;
  height: 28px;
  padding: 0;
}
.toolbar-actions {
  display: flex;
  gap: 8px;
}
.unit-table :deep(.table-header-cell) {
  background: #e7f0ff;
  color: #365078;
  font-size: 12px;
  font-weight: 700;
}
.unit-table :deep(.el-table__row) {
  font-size: 12px;
}
.unit-table :deep(.el-table__body tr:hover > td) {
  background: #fffaf0;
}
.date-picker {
  width: 100%;
}
</style>
