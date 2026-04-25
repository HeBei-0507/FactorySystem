<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getProductionlineList } from '@/api/prodectionLine'
import { getDeviceUtilList } from '@/api/deviceUnit'
import { addEquipment, deleteEquipment, getEquipmentPage, updateEquipment } from '@/api/equipment'

const currentMaintainer = '天津职业师范大学'
const productionLines = ref([])
const activeLineId = ref('')
const activeUnitId = ref('')
const expandedUnitId = ref('')
const tableRef = ref()
const tableData = ref([])
const selectedRows = ref([])
const editingRowId = ref('')
const creatingRowId = ref('')
const loading = ref(false)
const filters = reactive({ equipmentCode: '', equipmentName: '' })
const pagination = reactive({ currentPage: 1, pageSize: 5, total: 0 })
const activeLine = computed(
  () => productionLines.value.find((v) => v.id === activeLineId.value) || null
)
const activeUnit = computed(
  () => activeLine.value?.units.find((v) => v.id === activeUnitId.value) || null
)
const now = () => new Date().toISOString().slice(0, 19).replace('T', ' ')
const resetSelection = () => {
  selectedRows.value = []
  tableRef.value?.clearSelection()
}
const mapLine = (i) => ({ id: i.id, name: i.lineName, units: [] })
const mapUnit = (i) => ({ id: i.id, unitCode: i.unitCode, unitName: i.unitName, devices: [] })
const mapEq = (i) => ({
  id: i.id,
  unitCode: i.unitCode,
  unitName: i.unitName,
  equipmentCode: i.equipmentCode,
  equipmentName: i.equipmentName,
  maintainerName: i.maintainerName || '',
  equipmentCategory: i.equipmentCategory || '',
  equipmentImportance: i.equipmentImportance || '',
  repairStrategy: i.repairStrategy || '',
  overhaulTeam: i.overhaulTeam || '',
  operateTeam: i.operateTeam || '',
  createdAt: i.createdAt || '',
  updatedAt: i.updatedAt || ''
})

async function loadProductionLines() {
  try {
    const res = await getProductionlineList()
    productionLines.value = (res?.data || []).map(mapLine)
    if (productionLines.value.length) await clickLine(productionLines.value[0])
  } catch (e) {
    console.error(e)
    ElMessage.error('获取生产线列表失败')
  }
}
async function clickLine(line) {
  activeLineId.value = line.id
  activeUnitId.value = ''
  expandedUnitId.value = ''
  editingRowId.value = ''
  creatingRowId.value = ''
  resetSelection()
  try {
    const res = await getDeviceUtilList(line.id)
    line.units = (res?.data || []).map(mapUnit)
    tableData.value = []
    pagination.currentPage = 1
    pagination.total = 0
  } catch (e) {
    console.error(e)
    ElMessage.error('获取设备单元列表失败')
  }
}
async function loadEquipment() {
  if (!activeUnit.value) {
    tableData.value = []
    pagination.total = 0
    return
  }
  loading.value = true
  try {
    const res = await getEquipmentPage({
      current: pagination.currentPage,
      size: pagination.pageSize,
      unitCode: activeUnit.value.unitCode,
      equipmentCode: filters.equipmentCode,
      equipmentName: filters.equipmentName
    })
    const data = res?.data || {}
    const records = (data.records || []).map(mapEq)
    tableData.value = records
    activeUnit.value.devices = records
    pagination.total = data.total ?? records.length
  } catch (e) {
    console.error(e)
    ElMessage.error('获取设备档案失败')
  } finally {
    loading.value = false
  }
}
async function clickUnit(line, unit) {
  activeLineId.value = line.id
  activeUnitId.value = unit.id
  expandedUnitId.value = unit.id
  editingRowId.value = ''
  creatingRowId.value = ''
  filters.equipmentCode = ''
  filters.equipmentName = ''
  pagination.currentPage = 1
  resetSelection()
  await loadEquipment()
}
function toggleUnit(line, unit) {
  if (activeLineId.value !== line.id || activeUnitId.value !== unit.id) {
    clickUnit(line, unit)
    return
  }
  expandedUnitId.value = expandedUnitId.value === unit.id ? '' : unit.id
}
const isUnitExpanded = (unit) => expandedUnitId.value === unit.id
const handleSelectionChange = (rows) => (selectedRows.value = rows)
const handlePageChange = async (p) => {
  pagination.currentPage = p
  resetSelection()
  await loadEquipment()
}
const handleSearch = async () => {
  pagination.currentPage = 1
  resetSelection()
  await loadEquipment()
}
const isEditingRow = (row) => editingRowId.value === row.id
function createEmptyRow() {
  if (!activeUnit.value) return ElMessage.warning('请先选择设备单元')
  if (creatingRowId.value) return ElMessage.warning('请先完成当前新增行')
  tableData.value.unshift({
    id: `draft-${Date.now()}`,
    unitCode: activeUnit.value.unitCode,
    unitName: activeUnit.value.unitName,
    equipmentCode: '',
    equipmentName: '',
    maintainerName: currentMaintainer,
    equipmentCategory: '',
    equipmentImportance: '',
    repairStrategy: '',
    overhaulTeam: '',
    operateTeam: '',
    createdAt: now(),
    updatedAt: now(),
    isNew: true
  })
  creatingRowId.value = tableData.value[0].id
  editingRowId.value = tableData.value[0].id
  resetSelection()
}
function startEdit() {
  if (selectedRows.value.length !== 1) return ElMessage.warning('请选择一条需要修改的设备档案')
  const row = selectedRows.value[0]
  editingRowId.value = row.id
  creatingRowId.value = row.isNew ? row.id : ''
}
function valid(r) {
  if (!String(r.unitCode || '').trim()) return (ElMessage.warning('设备单元编码不能为空'), false)
  if (!String(r.unitName || '').trim()) return (ElMessage.warning('设备单元名称不能为空'), false)
  if (!String(r.equipmentCode || '').trim()) return (ElMessage.warning('设备编码不能为空'), false)
  if (!String(r.equipmentName || '').trim()) return (ElMessage.warning('设备名称不能为空'), false)
  return true
}
function payload(r, includeId = false) {
  const data = {
    unitCode: r.unitCode,
    unitName: r.unitName,
    equipmentCode: r.equipmentCode,
    equipmentName: r.equipmentName,
    maintainerName: r.maintainerName,
    equipmentCategory: r.equipmentCategory,
    equipmentImportance: r.equipmentImportance,
    repairStrategy: r.repairStrategy,
    overhaulTeam: r.overhaulTeam,
    operateTeam: r.operateTeam,
    createdAt: r.createdAt || now(),
    updatedAt: now()
  }
  if (includeId) data.id = r.id
  return data
}
async function submitCreate() {
  const row = tableData.value.find((v) => v.id === creatingRowId.value)
  if (!row) return ElMessage.warning('当前没有待新增的设备档案')
  if (!valid(row)) return
  try {
    await addEquipment(payload(row, false))
    creatingRowId.value = ''
    editingRowId.value = ''
    ElMessage.success('新增成功')
    await loadEquipment()
  } catch (e) {
    console.error(e)
    ElMessage.error('新增设备档案失败')
  }
}
async function submitEdit() {
  if (selectedRows.value.length !== 1) return ElMessage.warning('请选择一条需要修改的设备档案')
  const row = selectedRows.value[0]
  if (!valid(row)) return
  try {
    await updateEquipment(payload(row, true))
    editingRowId.value = ''
    ElMessage.success('修改成功')
    await loadEquipment()
  } catch (e) {
    console.error(e)
    ElMessage.error('修改设备档案失败')
  }
}
async function removeRows() {
  if (!selectedRows.value.length) return ElMessage.warning('请至少选择一条设备档案')
  const draftIds = new Set(selectedRows.value.filter((v) => v.isNew).map((v) => v.id))
  tableData.value = tableData.value.filter((v) => !draftIds.has(v.id))
  const ids = selectedRows.value.filter((v) => !v.isNew).map((v) => v.id)
  if (!ids.length) {
    resetSelection()
    creatingRowId.value = ''
    editingRowId.value = ''
    return ElMessage.success('已删除未保存的新增行')
  }
  try {
    await Promise.all(ids.map((id) => deleteEquipment(id)))
    creatingRowId.value = ''
    editingRowId.value = ''
    resetSelection()
    ElMessage.success('删除成功')
    await loadEquipment()
  } catch (e) {
    console.error(e)
    ElMessage.error('删除设备档案失败')
  }
}
loadProductionLines()
</script>

<template>
  <div class="device-file-page">
    <div class="page-breadcrumb">
      设备档案维护 &gt; {{ activeUnit ? activeUnit.unitName : '未选择设备单元' }}
    </div>
    <div class="page-content">
      <aside class="tree-panel">
        <div class="tree-panel-header">设备档案</div>
        <div class="tree-content">
          <div v-for="line in productionLines" :key="line.id" class="line-block">
            <div
              :class="['line-title', { active: line.id === activeLineId }]"
              @click="clickLine(line)"
            >
              {{ line.name }}
            </div>
            <div v-if="line.id === activeLineId" class="unit-list">
              <div
                v-for="unit in line.units"
                :key="unit.id"
                :class="['unit-item', { active: unit.id === activeUnitId }]"
              >
                <div class="unit-row" @click="toggleUnit(line, unit)">
                  <span :class="['arrow', { expanded: isUnitExpanded(unit) }]">›</span>
                  <span>{{ unit.unitCode }} {{ unit.unitName }}</span>
                </div>
                <div v-if="isUnitExpanded(unit)" class="device-list">
                  <div v-for="d in unit.devices" :key="d.id" class="device-item">
                    {{ d.equipmentCode }} {{ d.equipmentName }}
                  </div>
                  <div v-if="!unit.devices.length" class="device-empty">暂无设备</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </aside>
      <section class="table-panel">
        <div class="filter-bar">
          <div class="filter-item">
            <span class="filter-label">设备编码</span>
            <el-input
              v-model="filters.equipmentCode"
              clearable
              placeholder="请输入设备编码"
              @change="handleSearch"
            />
          </div>
          <div class="filter-item">
            <span class="filter-label">设备名称</span>
            <el-input
              v-model="filters.equipmentName"
              clearable
              placeholder="请输入设备名称"
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
              <el-button type="primary" plain @click="editingRowId ? submitEdit() : startEdit()">修改</el-button>
              <el-button type="danger" plain @click="removeRows">删除</el-button>
            </div>
          </div>
        </div>
        <el-table
          ref="tableRef"
          v-loading="loading"
          :data="tableData"
          border
          class="device-table"
          header-cell-class-name="table-header-cell"
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="48" align="center" />
          <el-table-column label="设备单元编码" min-width="110">
            <template #default="{ row }">
              <el-input v-if="isEditingRow(row)" v-model="row.unitCode" size="small" disabled />
              <span v-else>{{ row.unitCode }}</span>
            </template>
          </el-table-column>
          <el-table-column label="设备单元名称" min-width="150">
            <template #default="{ row }">
              <el-input v-if="isEditingRow(row)" v-model="row.unitName" size="small" disabled />
              <span v-else>{{ row.unitName }}</span>
            </template>
          </el-table-column>
          <el-table-column label="设备编码" min-width="110">
            <template #default="{ row }">
              <el-input v-if="isEditingRow(row)" v-model="row.equipmentCode" size="small" />
              <span v-else>{{ row.equipmentCode }}</span>
            </template>
          </el-table-column>
          <el-table-column label="设备名称" min-width="130">
            <template #default="{ row }">
              <el-input v-if="isEditingRow(row)" v-model="row.equipmentName" size="small" />
              <span v-else>{{ row.equipmentName }}</span>
            </template>
          </el-table-column>
          <el-table-column label="设备维护人" min-width="130">
            <template #default="{ row }">
              <el-input v-if="isEditingRow(row)" v-model="row.maintainerName" size="small" />
              <span v-else>{{ row.maintainerName }}</span>
            </template>
          </el-table-column>
          <el-table-column label="设备类别" min-width="130">
            <template #default="{ row }">
              <el-input v-if="isEditingRow(row)" v-model="row.equipmentCategory" size="small" />
              <span v-else>{{ row.equipmentCategory }}</span>
            </template>
          </el-table-column>
          <el-table-column label="设备重要度" min-width="130">
            <template #default="{ row }">
              <el-input v-if="isEditingRow(row)" v-model="row.equipmentImportance" size="small" />
              <span v-else>{{ row.equipmentImportance }}</span>
            </template>
          </el-table-column>
          <el-table-column label="维修策略" min-width="130">
            <template #default="{ row }">
              <el-input v-if="isEditingRow(row)" v-model="row.repairStrategy" size="small" />
              <span v-else>{{ row.repairStrategy }}</span>
            </template>
          </el-table-column>
          <el-table-column label="检修班组" min-width="130">
            <template #default="{ row }">
              <el-input v-if="isEditingRow(row)" v-model="row.overhaulTeam" size="small" />
              <span v-else>{{ row.overhaulTeam }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作班组" min-width="130">
            <template #default="{ row }">
              <el-input v-if="isEditingRow(row)" v-model="row.operateTeam" size="small" />
              <span v-else>{{ row.operateTeam }}</span>
            </template>
          </el-table-column>
        </el-table>
      </section>
    </div>
  </div>
</template>

<style scoped>
.device-file-page {
  min-height: calc(100vh - 36px);
  padding: 10px;
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
.tree-panel,
.table-panel {
  border: 1px solid #d9e2ef;
  background: #fff;
}
.tree-panel-header {
  padding: 10px 14px;
  border-bottom: 1px solid #e6edf6;
  color: #33507b;
  font-size: 13px;
  font-weight: 700;
}
.tree-content {
  padding: 14px 18px;
}
.line-block + .line-block {
  margin-top: 18px;
}
.line-title {
  margin-bottom: 12px;
  color: #1f2d3d;
  font-size: 16px;
  font-weight: 800;
  line-height: 1.35;
  cursor: pointer;
}
.line-title.active {
  color: #2f76e8;
}
.unit-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 12px;
}
.unit-item {
  border-radius: 5px;
  color: #1f2d3d;
  cursor: pointer;
}
.unit-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  border-radius: 5px;
  font-size: 16px;
  font-weight: 800;
}
.unit-row:hover {
  background: #f2f7ff;
}
.unit-item.active .unit-row {
  background: #eaf2ff;
  color: #2f76e8;
}
.arrow {
  display: inline-flex;
  width: 12px;
  transform: rotate(0deg);
  transition: transform 0.18s ease;
}
.arrow.expanded {
  transform: rotate(90deg);
}
.device-list {
  margin: 2px 0 8px 42px;
}
.device-item,
.device-empty {
  padding: 3px 0;
  color: #40577d;
  font-size: 14px;
  line-height: 1.4;
}
.device-empty {
  color: #9aa7ba;
  font-size: 14px;
}
.table-panel {
  padding: 10px 12px;
  overflow: auto;
}
.filter-bar {
  display: grid;
  grid-template-columns: repeat(2, minmax(220px, 320px));
  gap: 18px;
  margin-bottom: 16px;
}
.filter-item {
  display: flex;
  align-items: center;
  gap: 12px;
}
.filter-label {
  min-width: 78px;
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
.device-table :deep(.table-header-cell) {
  background: #e7f0ff;
  color: #365078;
  font-size: 12px;
  font-weight: 700;
}
</style>
