<script setup>
import { computed, reactive, ref } from 'vue'
import * as XLSX from 'xlsx'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getProductionlineList } from '@/api/BaseInfo/prodectionLine'
import { getDeviceUtilList } from '@/api/BaseInfo/deviceUnit'
import {
  addEquipment,
  batchDeleteEquipment,
  getEquipmentPage,
  getEquipmentPageByLine,
  updateEquipment
} from '@/api/BaseInfo/equipment'

const currentMaintainer = '天津职业师范大学'
const productionLines = ref([])
const activeLineId = ref('')
const activeUnitId = ref('')
const expandedUnitId = ref('')
const viewScope = ref('line')
const tableRef = ref()
const tableData = ref([])
const selectedRows = ref([])
const editingRowId = ref('')
const creatingRowId = ref('')
const loading = ref(false)
const importDialogVisible = ref(false)
const importFile = ref(null)
const importRemark = ref('')
const importFileInputRef = ref()
const filters = reactive({ equipmentCode: '', equipmentName: '' })
const pagination = reactive({ currentPage: 1, pageSize: 5, total: 0 })
const equipmentCategoryOptions = [
  '00-动力设备',
  '01-移动设备',
  '02-执行/工作设备',
  '03-控制设备',
  '04-结构设备',
  '05-铺助设备',
  '06-链接设备',
  '07-零部件',
  '08-竞赛设备'
]
const equipmentImportanceOptions = ['01-未辨识', '02-一般设备', '03-重要设备']
const repairStrategyOptions = [
  '01-事后维修',
  '02-预防计划维修',
  '03-日常基础保养',
  '04-预知状态维修'
]
const overhaulTeamOptions = ['01-点检', '02-精密', '03-维修工', '04-外委单位']
const operateTeamOptions = ['01-点检', '02-精密']

const importRequiredHeaders = [
  '设备单元代码',
  '设备单元名称',
  '设备编码',
  '设备名称',
  '检修班组',
  '操作班组',
  '维修策略',
  '设备重要度',
  '设备类别',
  '设备维护人'
]
const importHeaderMap = {
  设备单元代码: 'unitCode',
  设备单元名称: 'unitName',
  设备编码: 'equipmentCode',
  设备名称: 'equipmentName',
  检修班组: 'overhaulTeam',
  操作班组: 'operateTeam',
  维修策略: 'repairStrategy',
  设备重要度: 'equipmentImportance',
  设备类别: 'equipmentCategory',
  设备维护人: 'maintainerName'
}
const activeLine = computed(
  () => productionLines.value.find((v) => v.id === activeLineId.value) || null
)
const activeUnit = computed(
  () => activeLine.value?.units.find((v) => v.id === activeUnitId.value) || null
)
const activeTitle = computed(() => {
  if (viewScope.value === 'unit' && activeUnit.value) return activeUnit.value.unitName
  if (activeLine.value) return activeLine.value.name
  return '未选择生产线'
})
const importFunctionNo = computed(() =>
  activeLine.value?.lineCode ? activeLine.value.lineCode + '.IMP01' : 'IMP01'
)
const now = () => new Date().toISOString().slice(0, 19).replace('T', ' ')
const resetSelection = () => {
  selectedRows.value = []
  tableRef.value?.clearSelection()
}
const mapLine = (i) => ({
  id: i.id,
  lineCode: i.lineCode || i.line_code || i.productionLineCode || i.code || '',
  name: i.lineName,
  units: []
})
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
  viewScope.value = 'line'
  editingRowId.value = ''
  creatingRowId.value = ''
  filters.equipmentCode = ''
  filters.equipmentName = ''
  pagination.currentPage = 1
  resetSelection()
  try {
    const res = await getDeviceUtilList(line.id)
    line.units = (res?.data || []).map(mapUnit)
    await loadEquipment()
  } catch (e) {
    console.error(e)
    ElMessage.error('获取设备单元列表失败')
  }
}
async function loadEquipment() {
  if (viewScope.value === 'unit' && !activeUnit.value) {
    tableData.value = []
    pagination.total = 0
    return
  }
  if (viewScope.value === 'line' && !activeLine.value?.lineCode) {
    tableData.value = []
    pagination.total = 0
    return ElMessage.warning('生产线编码不能为空')
  }
  loading.value = true
  try {
    const params = {
      current: pagination.currentPage,
      size: pagination.pageSize,
      equipmentCode: filters.equipmentCode,
      equipmentName: filters.equipmentName
    }
    const res =
      viewScope.value === 'line'
        ? await getEquipmentPageByLine(activeLine.value.lineCode, params)
        : await getEquipmentPage({ ...params, unitCode: activeUnit.value.unitCode })
    const data = res?.data || {}
    const records = (data.records || []).map(mapEq)
    tableData.value = records
    if (viewScope.value === 'unit') activeUnit.value.devices = records
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
  viewScope.value = 'unit'
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
const shouldDisableUnitFields = () => viewScope.value === 'unit'
function validateUnitMatch(row) {
  if (viewScope.value !== 'line') return true
  const unitCode = String(row.unitCode || '').trim()
  const unitName = String(row.unitName || '').trim()
  const matchedUnit = activeLine.value?.units.find((unit) => unit.unitCode === unitCode)
  if (!matchedUnit) return (ElMessage.warning('当前生产线下不存在该设备单元编码'), false)
  if (matchedUnit.unitName !== unitName)
    return (ElMessage.warning('设备单元编码和设备单元名称不匹配'), false)
  return true
}
function createEmptyRow() {
  if (viewScope.value === 'unit' && !activeUnit.value) return ElMessage.warning('请先选择设备单元')
  if (viewScope.value === 'line' && !activeLine.value) return ElMessage.warning('请先选择生产线')
  if (creatingRowId.value) return ElMessage.warning('请先完成当前新增行')
  tableData.value.unshift({
    id: `draft-${Date.now()}`,
    unitCode: activeUnit.value?.unitCode || '',
    unitName: activeUnit.value?.unitName || '',
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
  if (!validateUnitMatch(r)) return false
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
  const ids = selectedRows.value
    .filter((v) => !v.isNew)
    .map((v) => Number(v.id))
    .filter((id) => Number.isFinite(id) && id > 0)
  if (!ids.length) {
    resetSelection()
    creatingRowId.value = ''
    editingRowId.value = ''
    return ElMessage.success('已删除未保存的新增行')
  }
  try {
    const res = await batchDeleteEquipment(ids)
    if (!res?.success) {
      ElMessage.error(res?.errorMsg || '删除设备档案失败')
      return
    }
    const deletedCount = res?.data?.deletedCount ?? ids.length
    creatingRowId.value = ''
    editingRowId.value = ''
    resetSelection()
    ElMessage.success('删除成功，共删除 ' + deletedCount + ' 条')
    await loadEquipment()
  } catch (e) {
    console.error(e)
    ElMessage.error(e?.response?.data?.errorMsg || e?.message || '删除设备档案失败')
  }
}
function resetImportDialog() {
  importFile.value = null
  importRemark.value = ''
  if (importFileInputRef.value) importFileInputRef.value.value = ''
}
function openImportDialog() {
  resetImportDialog()
  importDialogVisible.value = true
}
function triggerImportFileSelect() {
  importFileInputRef.value?.click()
}
function handleImportFileChange(event) {
  importFile.value = event.target.files?.[0] || null
}
function normalizeImportValue(value) {
  return value == null ? '' : String(value).trim()
}
function validateImportHeaders(headers) {
  const unknownHeaders = headers.filter((header) => header && !importRequiredHeaders.includes(header))
  const missingHeaders = importRequiredHeaders.filter((header) => !headers.includes(header))
  if (unknownHeaders.length) {
    ElMessage.error('导入文件包含当前业务不存在的属性列：' + unknownHeaders.join('、'))
    return false
  }
  if (missingHeaders.length) {
    ElMessage.error('导入文件缺少必要属性列：' + missingHeaders.join('、'))
    return false
  }
  return true
}
function validateImportUnits(rows) {
  const unitMap = new Map((activeLine.value?.units || []).map((unit) => [unit.unitCode, unit.unitName]))
  const missingUnits = new Set()
  const mismatchUnits = []
  rows.forEach((row) => {
    const expectedName = unitMap.get(row.unitCode)
    if (!expectedName) {
      missingUnits.add(`${row.unitCode}${row.unitName ? '（' + row.unitName + '）' : ''}`)
      return
    }
    if (expectedName !== row.unitName) {
      mismatchUnits.push(`${row.unitCode}：文件为“${row.unitName}”，当前生产线下为“${expectedName}”`)
    }
  })
  if (missingUnits.size) {
    ElMessage.error('以下设备单元未创建，无法导入：' + Array.from(missingUnits).join('、'))
    return false
  }
  if (mismatchUnits.length) {
    ElMessage.error('设备单元代码与名称不匹配：' + mismatchUnits.join('；'))
    return false
  }
  return true
}
async function executeImport() {
  if (!importFile.value) return ElMessage.warning('请先选择导入文件')
  if (!/\.xlsx$/i.test(importFile.value.name)) {
    console.error('文件类型错误，请导入Excel表格')
    return ElMessage.error('文件类型错误，请导入Excel表格')
  }
  if (!activeLine.value) return ElMessage.warning('请先选择生产线')
  if (!activeLine.value.units.length) return ElMessage.warning('当前生产线下暂无设备单元，无法导入')
  try {
    const buffer = await importFile.value.arrayBuffer()
    const workbook = XLSX.read(buffer, { type: 'array' })
    const sheet = workbook.Sheets[workbook.SheetNames[0]]
    const rows = XLSX.utils.sheet_to_json(sheet, { header: 1, defval: '' })
    const headers = (rows[0] || []).map(normalizeImportValue)
    if (!validateImportHeaders(headers)) return
    const parsedRows = rows
      .slice(1)
      .filter((row) => row.some((cell) => normalizeImportValue(cell)))
      .map((row) => {
        const data = {}
        headers.forEach((header, index) => {
          data[importHeaderMap[header]] = normalizeImportValue(row[index])
        })
        return data
      })
    if (!parsedRows.length) return ElMessage.warning('导入文件中没有可导入的数据')
    if (!validateImportUnits(parsedRows)) return
    console.log('准备导入的设备档案数据：', parsedRows)
    console.log('导入备注：', importRemark.value)
    ElMessage.success('导入数据校验通过，已输出到控制台')
    importDialogVisible.value = false
    resetImportDialog()
  } catch (error) {
    console.error(error)
    ElMessage.error('解析导入文件失败')
  }
}
loadProductionLines()
</script>

<template>
  <div class="device-file-page">
    <div class="page-breadcrumb">
      设备档案维护 &gt; {{ activeTitle }}
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
              <el-button type="primary" plain @click="openImportDialog">导入</el-button>
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
              <el-input
                v-if="isEditingRow(row)"
                v-model="row.unitCode"
                size="small"
                :disabled="shouldDisableUnitFields()"
              />
              <span v-else>{{ row.unitCode }}</span>
            </template>
          </el-table-column>
          <el-table-column label="设备单元名称" min-width="150">
            <template #default="{ row }">
              <el-input
                v-if="isEditingRow(row)"
                v-model="row.unitName"
                size="small"
                :disabled="shouldDisableUnitFields()"
              />
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
              <el-select
                v-if="isEditingRow(row)"
                v-model="row.equipmentCategory"
                size="small"
                filterable
                allow-create
                default-first-option
                clearable
                class="editable-select"
                placeholder="请选择或输入设备类别"
              >
                <el-option
                  v-for="item in equipmentCategoryOptions"
                  :key="item"
                  :label="item"
                  :value="item"
                />
              </el-select>
              <span v-else>{{ row.equipmentCategory }}</span>
            </template>
          </el-table-column>
          <el-table-column label="设备重要度" min-width="130">
            <template #default="{ row }">
              <el-select
                v-if="isEditingRow(row)"
                v-model="row.equipmentImportance"
                size="small"
                filterable
                allow-create
                default-first-option
                clearable
                class="editable-select"
                placeholder="请选择或输入设备重要度"
              >
                <el-option
                  v-for="item in equipmentImportanceOptions"
                  :key="item"
                  :label="item"
                  :value="item"
                />
              </el-select>
              <span v-else>{{ row.equipmentImportance }}</span>
            </template>
          </el-table-column>
          <el-table-column label="维修策略" min-width="130">
            <template #default="{ row }">
              <el-select
                v-if="isEditingRow(row)"
                v-model="row.repairStrategy"
                size="small"
                filterable
                allow-create
                default-first-option
                clearable
                class="editable-select"
                placeholder="请选择或输入维修策略"
              >
                <el-option
                  v-for="item in repairStrategyOptions"
                  :key="item"
                  :label="item"
                  :value="item"
                />
              </el-select>
              <span v-else>{{ row.repairStrategy }}</span>
            </template>
          </el-table-column>
          <el-table-column label="检修班组" min-width="130">
            <template #default="{ row }">
              <el-select
                v-if="isEditingRow(row)"
                v-model="row.overhaulTeam"
                size="small"
                filterable
                allow-create
                default-first-option
                clearable
                class="editable-select"
                placeholder="请选择或输入检修班组"
              >
                <el-option
                  v-for="item in overhaulTeamOptions"
                  :key="item"
                  :label="item"
                  :value="item"
                />
              </el-select>
              <span v-else>{{ row.overhaulTeam }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作班组" min-width="130">
            <template #default="{ row }">
              <el-select
                v-if="isEditingRow(row)"
                v-model="row.operateTeam"
                size="small"
                filterable
                allow-create
                default-first-option
                clearable
                class="editable-select"
                placeholder="请选择或输入操作班组"
              >
                <el-option
                  v-for="item in operateTeamOptions"
                  :key="item"
                  :label="item"
                  :value="item"
                />
              </el-select>
              <span v-else>{{ row.operateTeam }}</span>
            </template>
          </el-table-column>
        </el-table>
        <el-dialog
          v-model="importDialogVisible"
          title="导入数据"
          width="560px"
          class="import-dialog"
          @closed="resetImportDialog"
        >
          <el-form label-width="82px" class="import-form">
            <el-form-item label="功能号：">
              <el-input :model-value="importFunctionNo" disabled />
            </el-form-item>
            <el-form-item label="页面英文名：">
              <el-input model-value="Equipment" disabled />
            </el-form-item>
            <el-form-item label="导入文件">
              <input
                ref="importFileInputRef"
                class="hidden-file-input"
                type="file"
                accept=".xlsx"
                @change="handleImportFileChange"
              />
              <div class="import-file-box">
                <el-button link type="primary" @click="triggerImportFileSelect">选择导入附件</el-button>
                <div v-if="importFile" class="selected-file">
                  <span>{{ importFile.name }}</span>
                  <span class="file-size">{{ Math.ceil(importFile.size / 1024) }}KB</span>
                  <el-button link type="primary" @click="triggerImportFileSelect">更换</el-button>
                  <el-button link type="danger" @click="resetImportDialog">移除</el-button>
                </div>
                <div v-else class="file-placeholder">仅支持 .xlsx Excel 表格</div>
              </div>
            </el-form-item>
            <el-form-item label="导入备注：">
              <el-input
                v-model="importRemark"
                type="textarea"
                :rows="3"
                placeholder="请输入导入备注"
              />
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="importDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="executeImport">执行导入</el-button>
          </template>
        </el-dialog>
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
.editable-select {
  width: 100%;
}
.hidden-file-input {
  display: none;
}
.import-file-box {
  width: 100%;
  min-height: 72px;
  padding: 8px 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: #fff;
}
.selected-file {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 6px;
  color: #303133;
  font-size: 13px;
}
.file-size,
.file-placeholder {
  color: #909399;
  font-size: 12px;
}
</style>
