<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown, ArrowUp, MoreFilled, Plus } from '@element-plus/icons-vue'
import {
  addDailyInspectionFrequency,
  batchDeleteDailyInspectionFrequency,
  getDailyInspectionFrequencyPage,
  updateDailyInspectionFrequency
} from '@/api/dailyInspectionFrequency'
import {
  addDailyInspectionTable,
  batchDeleteDailyInspectionTable,
  getDailyInspectionTablePage,
  updateDailyInspectionTable
} from '@/api/dailyInspectionTable'
import { getProductionlineList } from '@/api/prodectionLine'
import { extractPageData, extractRecordsArray } from '@/util/apiData'

const pageTitle = '巡检信息维护'

const tableRef = ref()
const loading = ref(false)
const tableData = ref([])
const selectedRows = ref([])
const activePatrolTableId = ref(null)

const filters = reactive({
  productionLineName: '',
  tableName: '',
  tableCode: '',
  shiftMode: ''
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

const productionLines = ref([])

/** 待提交的草稿行 id */
const creatingRowId = ref('')
/** 当前处于编辑态的行 id（草稿行与 persisted 行共用） */
const editingRowId = ref('')

/* —— 项次表格 —— */
const freqTableRef = ref()
const freqLoading = ref(false)
const freqTableData = ref([])
const freqSelectedRows = ref([])
const freqFilters = reactive({
  frequencyCode: '',
  areaDeviceName: ''
})
const selectedPatrolTable = computed(
  () =>
    tableData.value.find((row) => row.id === activePatrolTableId.value) ||
    selectedRows.value[0] ||
    null
)
const freqPagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})
const freqCreatingRowId = ref('')
const freqEditingRowId = ref('')
const freqPanelExpanded = ref(true)

/** 数据类别（定性 / 定量） */
const dataTypeOptions = [
  { label: '定性', value: '定性' },
  { label: '定量', value: '定量' }
]

/** 巡检周期单位（天、周、月、班） */
const cycleUnitOptions = [
  { label: '天', value: '天' },
  { label: '周', value: '周' },
  { label: '月', value: '月' },
  { label: '班', value: '班' }
]

/** 周日期设定（无、星期一…星期日） */
const weekDateSetOptions = [
  { label: '无', value: '无' },
  { label: '星期一', value: '星期一' },
  { label: '星期二', value: '星期二' },
  { label: '星期三', value: '星期三' },
  { label: '星期四', value: '星期四' },
  { label: '星期五', value: '星期五' },
  { label: '星期六', value: '星期六' },
  { label: '星期日', value: '星期日' }
]

/** 班次设定（无、白班、夜班…） */
const shiftsSetOptions = [
  { label: '无', value: '无' },
  { label: '白班', value: '白班' },
  { label: '夜班', value: '夜班' },
  { label: '早班', value: '早班' },
  { label: '中班', value: '中班' },
  { label: '晚班', value: '晚班' }
]

const shiftModeFilterOptions = [
  { label: '全部', value: '' },
  { label: '甲乙两班制', value: '甲乙两班制' },
  { label: '三班倒', value: '三班倒' },
  { label: '四班三倒', value: '四班三倒' },
  { label: '甲乙丙周转', value: '甲乙丙周转' }
]

const shiftModeFormOptions = [
  { label: '甲乙两班制', value: '甲乙两班制' },
  { label: '三班倒', value: '三班倒' },
  { label: '四班三倒', value: '四班三倒' },
  { label: '甲乙丙周转', value: '甲乙丙周转' }
]

const statusOptions = [
  { label: '启用', value: '启用' },
  { label: '停用', value: '停用' },
  { label: '正常', value: '正常' }
]

function normalizeLine(item) {
  return { id: item.id, name: item.lineName }
}

function normalizeRow(item) {
  return {
    id: item.id,
    tableCode: item.tableCode ?? '',
    tableName: item.tableName ?? '',
    productionLineId: item.productionLineId,
    productionLineName: item.productionLineName ?? '',
    shiftMode: item.shiftMode ?? '',
    implementer: item.implementer ?? '',
    status: item.status ?? '',
    creatorId: item.creatorId,
    creatorUsername: item.creatorUsername != null ? String(item.creatorUsername) : '',
    creatorName: item.creatorName ?? '',
    createdAt: item.createdAt ?? '',
    updatedAt: item.updatedAt ?? '',
    isNew: false
  }
}

function normalizeWeekOrShift(v, fallback = '无') {
  const s = v != null ? String(v).trim() : ''
  return s || fallback
}

function normalizeFreqRow(item) {
  return {
    id: item.id,
    dailyInspectionTableId: item.dailyInspectionTableId,
    frequencyCode: item.frequencyCode ?? '',
    areaDeviceName: item.areaDeviceName ?? '',
    inspectionContent: item.inspectionContent ?? '',
    inspectionStandard: item.inspectionStandard ?? '',
    dataType: item.dataType ?? '',
    cycleUnit: item.cycleUnit ?? '',
    frequencyValue: item.frequencyValue,
    monthDateSet: item.monthDateSet ?? '',
    weekDateSet: normalizeWeekOrShift(item.weekDateSet, '无'),
    shiftsSet: normalizeWeekOrShift(item.shiftsSet, '无'),
    maximums: item.maximums,
    minimums: item.minimums,
    creatorUsername: item.creatorUsername != null ? String(item.creatorUsername) : '',
    creatorName: item.creatorName ?? '',
    createdAt: item.createdAt ?? '',
    updatedAt: item.updatedAt ?? '',
    isNew: false
  }
}

function displayOptText(v) {
  if (v == null || String(v).trim() === '') return '无'
  return String(v)
}

function numPayload(v) {
  if (v === null || v === undefined || v === '') return undefined
  const n = typeof v === 'number' ? v : Number(v)
  return Number.isFinite(n) ? n : undefined
}

function resetSelection() {
  selectedRows.value = []
  activePatrolTableId.value = null
  tableRef.value?.clearSelection()
}

function lineNameById(id) {
  const line = productionLines.value.find((l) => l.id === id)
  return line?.name ?? ''
}

function syncProductionLine(row) {
  row.productionLineName = lineNameById(row.productionLineId)
}

function resetInlineState() {
  creatingRowId.value = ''
  editingRowId.value = ''
}

function hasInlineDraftOrEdit() {
  return !!(creatingRowId.value || editingRowId.value)
}

async function loadProductionLines() {
  try {
    const res = await getProductionlineList()
    const rawList = extractRecordsArray(res?.data)
    productionLines.value = rawList.map(normalizeLine).filter((row) => row.id != null)
  } catch (error) {
    if (error?.elMessageNotified) return
    console.error(error)
    ElMessage.error('获取作业线列表失败')
  }
}

async function loadTablePage() {
  loading.value = true
  try {
    const res = await getDailyInspectionTablePage({
      current: pagination.currentPage,
      size: pagination.pageSize,
      productionLineName: filters.productionLineName || undefined,
      tableName: filters.tableName || undefined,
      tableCode: filters.tableCode || undefined,
      shiftMode: filters.shiftMode || undefined
    })
    const { records, total } = extractPageData(res)
    tableData.value = records.map(normalizeRow)
    pagination.total = total
  } catch (error) {
    if (error?.elMessageNotified) return
    console.error(error)
    ElMessage.error('加载日常巡检表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  if (hasInlineDraftOrEdit()) {
    ElMessage.warning('请先完成当前新增或修改')
    return
  }
  pagination.currentPage = 1
  resetSelection()
  loadTablePage()
}

function handleSelectionChange(rows) {
  selectedRows.value = rows
  if (rows.length === 1) {
    activePatrolTableId.value = rows[0].id
    freqPagination.currentPage = 1
    resetFreqSelection()
    resetFreqInlineState()
    loadFrequencyPage()
    return
  }
  if (!rows.length) {
    activePatrolTableId.value = null
    freqTableData.value = []
    freqPagination.total = 0
    resetFreqSelection()
    resetFreqInlineState()
  }
}

function handlePageChange(page) {
  if (hasInlineDraftOrEdit()) {
    ElMessage.warning('请先完成当前新增或修改')
    return
  }
  pagination.currentPage = page || 1
  resetSelection()
  loadTablePage()
}

function handleSizeChange(size) {
  if (hasInlineDraftOrEdit()) {
    ElMessage.warning('请先完成当前新增或修改')
    return
  }
  pagination.pageSize = size
  pagination.currentPage = 1
  resetSelection()
  loadTablePage()
}

function isEditingRow(row) {
  return editingRowId.value === row.id
}

function tableRowClassName({ row }) {
  return isEditingRow(row) ? 'table-row-editing' : ''
}

function createEmptyRow() {
  if (!productionLines.value.length) {
    ElMessage.warning('暂无作业线，无法新增')
    return
  }
  if (creatingRowId.value) {
    ElMessage.warning('请先完成当前新增行')
    return
  }
  const first = productionLines.value[0]
  const draftId = `draft-${Date.now()}`
  tableData.value.unshift({
    id: draftId,
    tableCode: '',
    tableName: '',
    productionLineId: first.id,
    productionLineName: first.name,
    shiftMode: '',
    implementer: '',
    status: '启用',
    creatorUsername: '',
    creatorName: '',
    isNew: true
  })
  creatingRowId.value = draftId
  editingRowId.value = draftId
  resetSelection()
}

function validateNewRow(row) {
  if (!String(row.tableName || '').trim()) {
    ElMessage.warning('请输入日常巡检表名')
    return false
  }
  if (row.productionLineId == null) {
    ElMessage.warning('请选择作业线')
    return false
  }
  if (!String(row.shiftMode || '').trim()) {
    ElMessage.warning('请选择或输入倒班模式')
    return false
  }
  if (!String(row.status || '').trim()) {
    ElMessage.warning('请选择状态')
    return false
  }
  return true
}

function validateExistingRow(row) {
  return validateNewRow(row)
}

async function submitCreateInline() {
  const row = tableData.value.find((item) => item.id === creatingRowId.value)
  if (!row) return ElMessage.warning('当前没有待新增的记录')
  if (!validateNewRow(row)) return
  try {
    await addDailyInspectionTable({
      tableName: row.tableName.trim(),
      productionLineId: row.productionLineId,
      shiftMode: row.shiftMode.trim(),
      implementer: (row.implementer || '').trim(),
      status: row.status
    })
    ElMessage.success('新增成功')
    resetInlineState()
    await loadTablePage()
    resetSelection()
  } catch (error) {
    if (error?.elMessageNotified) return
    console.error(error)
    ElMessage.error('新增失败')
  }
}

function startInlineEdit() {
  if (selectedRows.value.length !== 1) {
    return ElMessage.warning('请选择一条需要修改的记录')
  }
  const row = selectedRows.value[0]
  if (row.isNew) {
    return ElMessage.warning('未保存的新增行请先点「新增」提交')
  }
  creatingRowId.value = ''
  editingRowId.value = row.id
}

async function submitEditInline() {
  const row =
    editingRowId.value !== ''
      ? tableData.value.find((item) => item.id === editingRowId.value)
      : null
  if (!row) {
    return ElMessage.warning('请先选择一条需要修改的记录')
  }
  if (row.isNew) {
    return ElMessage.warning('未保存的新增行请先点「新增」提交')
  }
  if (!validateExistingRow(row)) return
  try {
    await updateDailyInspectionTable({
      id: row.id,
      tableName: row.tableName.trim(),
      productionLineId: row.productionLineId,
      shiftMode: row.shiftMode.trim(),
      implementer: (row.implementer || '').trim(),
      status: row.status
    })
    ElMessage.success('修改成功')
    resetInlineState()
    await loadTablePage()
    resetSelection()
  } catch (error) {
    if (error?.elMessageNotified) return
    console.error(error)
    ElMessage.error('修改失败')
  }
}

function handleModifyToolbar() {
  if (editingRowId.value) {
    submitEditInline()
  } else {
    startInlineEdit()
  }
}

async function handleDelete() {
  if (!selectedRows.value.length) {
    ElMessage.warning('请选择要删除的记录')
    return
  }

  const selection = [...selectedRows.value]
  const draftIds = new Set(selection.filter((r) => r.isNew).map((r) => r.id))

  if (draftIds.size) {
    tableData.value = tableData.value.filter((row) => !draftIds.has(row.id))
    if ([...draftIds].some((id) => id === creatingRowId.value || id === editingRowId.value)) {
      resetInlineState()
    }
    resetSelection()
    if (!selection.some((r) => !r.isNew)) {
      ElMessage.success('已删除未保存的新增行')
      return
    }
  }

  const persistedRows = selection.filter((r) => !r.isNew)
  if (!persistedRows.length) return

  try {
    await ElMessageBox.confirm(`确定删除选中的 ${persistedRows.length} 条记录吗？`, '提示', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }

  const ids = persistedRows.map((r) => r.id)
  try {
    await batchDeleteDailyInspectionTable(ids)
    ElMessage.success('删除成功')
    resetInlineState()
    resetSelection()
    await loadTablePage()
  } catch (error) {
    if (error?.elMessageNotified) return
    console.error(error)
    ElMessage.error('删除失败')
  }
}

function onGeneratePlan() {
  ElMessage.info('生成计划需对接后端接口后实现')
}

function onMoreCommand(cmd) {
  if (cmd === 'refresh') {
    if (hasInlineDraftOrEdit()) {
      ElMessage.warning('请先完成当前新增或修改')
      return
    }
    loadTablePage()
  }
}

function resetFreqSelection() {
  freqSelectedRows.value = []
  freqTableRef.value?.clearSelection()
}

function resetFreqInlineState() {
  freqCreatingRowId.value = ''
  freqEditingRowId.value = ''
}

function hasFreqInlineDraftOrEdit() {
  return !!(freqCreatingRowId.value || freqEditingRowId.value)
}

function isFreqEditingRow(row) {
  return freqEditingRowId.value === row.id
}

function freqRowClassName({ row }) {
  return isFreqEditingRow(row) ? 'freq-row-editing' : ''
}

async function loadFrequencyPage() {
  if (!activePatrolTableId.value) {
    freqTableData.value = []
    freqPagination.total = 0
    return
  }
  freqLoading.value = true
  try {
    const res = await getDailyInspectionFrequencyPage({
      current: freqPagination.currentPage,
      size: freqPagination.pageSize,
      dailyInspectionTableId: activePatrolTableId.value,
      frequencyCode: freqFilters.frequencyCode || undefined,
      areaDeviceName: freqFilters.areaDeviceName || undefined
    })
    const { records, total } = extractPageData(res)
    freqTableData.value = records.map(normalizeFreqRow)
    freqPagination.total = total
  } catch (error) {
    if (error?.elMessageNotified) return
    console.error(error)
    ElMessage.error('加载日常巡检项次失败')
  } finally {
    freqLoading.value = false
  }
}

function handleFreqSearch() {
  if (hasFreqInlineDraftOrEdit()) {
    ElMessage.warning('请先完成项次表格中当前新增或修改')
    return
  }
  freqPagination.currentPage = 1
  resetFreqSelection()
  loadFrequencyPage()
}

function handleFreqSelectionChange(rows) {
  freqSelectedRows.value = rows
}

function handleFreqPageChange(page) {
  if (hasFreqInlineDraftOrEdit()) {
    ElMessage.warning('请先完成项次表格中当前新增或修改')
    return
  }
  freqPagination.currentPage = page || 1
  resetFreqSelection()
  loadFrequencyPage()
}

function handleFreqSizeChange(size) {
  if (hasFreqInlineDraftOrEdit()) {
    ElMessage.warning('请先完成项次表格中当前新增或修改')
    return
  }
  freqPagination.pageSize = size
  freqPagination.currentPage = 1
  resetFreqSelection()
  loadFrequencyPage()
}

function createFreqEmptyRow() {
  if (!activePatrolTableId.value || !selectedPatrolTable.value) {
    ElMessage.warning('请先勾选一条日常巡检表，再新增对应项次')
    return
  }
  if (freqCreatingRowId.value) {
    ElMessage.warning('请先完成当前新增项次')
    return
  }
  const draftId = `freq-draft-${Date.now()}`
  freqTableData.value.unshift({
    id: draftId,
    dailyInspectionTableId: activePatrolTableId.value,
    frequencyCode: '',
    areaDeviceName: '',
    inspectionContent: '',
    inspectionStandard: '',
    dataType: '定量',
    cycleUnit: '天',
    frequencyValue: 1,
    monthDateSet: '',
    weekDateSet: '无',
    shiftsSet: '无',
    maximums: null,
    minimums: null,
    creatorUsername: '',
    creatorName: '',
    isNew: true
  })
  freqCreatingRowId.value = draftId
  freqEditingRowId.value = draftId
  resetFreqSelection()
}

function validateFreqNew(row) {
  if (!activePatrolTableId.value) {
    ElMessage.warning('请先勾选一条日常巡检表')
    return false
  }
  if (!String(row.frequencyCode || '').trim()) {
    ElMessage.warning('请输入项次号')
    return false
  }
  if (!String(row.areaDeviceName || '').trim()) {
    ElMessage.warning('请输入巡检区域设备/部位名称')
    return false
  }
  if (!String(row.dataType || '').trim()) {
    ElMessage.warning('请选择数据类别')
    return false
  }
  if (!String(row.cycleUnit || '').trim()) {
    ElMessage.warning('请选择周期单位')
    return false
  }
  if (numPayload(row.frequencyValue) === undefined) {
    ElMessage.warning('请输入项次')
    return false
  }
  return true
}

function validateFreqEdit(row) {
  if (!String(row.areaDeviceName || '').trim()) {
    ElMessage.warning('请输入巡检区域设备/部位名称')
    return false
  }
  return true
}

function buildFreqAddPayload(row) {
  const payload = {
    dailyInspectionTableId: activePatrolTableId.value,
    frequencyCode: row.frequencyCode.trim(),
    areaDeviceName: row.areaDeviceName.trim(),
    dataType: row.dataType?.trim() || undefined,
    cycleUnit: row.cycleUnit?.trim() || undefined,
    inspectionContent: row.inspectionContent?.trim() || undefined,
    inspectionStandard: row.inspectionStandard?.trim() || undefined,
    frequencyValue: numPayload(row.frequencyValue),
    monthDateSet: row.monthDateSet?.trim() || undefined,
    weekDateSet: row.weekDateSet?.trim() || undefined,
    shiftsSet: row.shiftsSet?.trim() || undefined,
    maximums: numPayload(row.maximums),
    minimums: numPayload(row.minimums)
  }
  Object.keys(payload).forEach((k) => {
    if (payload[k] === undefined) delete payload[k]
  })
  return payload
}

function buildFreqUpdatePayload(row) {
  const payload = {
    id: row.id,
    areaDeviceName: row.areaDeviceName.trim(),
    dataType: row.dataType?.trim() || undefined,
    cycleUnit: row.cycleUnit?.trim() || undefined,
    inspectionContent: row.inspectionContent?.trim() || undefined,
    inspectionStandard: row.inspectionStandard?.trim() || undefined,
    frequencyValue: numPayload(row.frequencyValue),
    monthDateSet: row.monthDateSet?.trim() || undefined,
    weekDateSet: row.weekDateSet?.trim() || undefined,
    shiftsSet: row.shiftsSet?.trim() || undefined,
    maximums: numPayload(row.maximums),
    minimums: numPayload(row.minimums)
  }
  Object.keys(payload).forEach((k) => {
    if (payload[k] === undefined && k !== 'id') delete payload[k]
  })
  return payload
}

async function submitFreqCreateInline() {
  const row = freqTableData.value.find((item) => item.id === freqCreatingRowId.value)
  if (!row) return ElMessage.warning('当前没有待新增的项次')
  if (!validateFreqNew(row)) return
  try {
    await addDailyInspectionFrequency(buildFreqAddPayload(row))
    ElMessage.success('新增成功')
    resetFreqInlineState()
    await loadFrequencyPage()
    resetFreqSelection()
  } catch (error) {
    if (error?.elMessageNotified) return
    console.error(error)
    ElMessage.error('新增项次失败')
  }
}

function startFreqInlineEdit() {
  if (!activePatrolTableId.value) {
    return ElMessage.warning('请先勾选一条日常巡检表')
  }
  if (freqSelectedRows.value.length !== 1) {
    return ElMessage.warning('请选择一条需要修改的记录')
  }
  const row = freqSelectedRows.value[0]
  if (row.isNew) return ElMessage.warning('未保存的新增行请先点「新增项次」提交')
  freqCreatingRowId.value = ''
  freqEditingRowId.value = row.id
}

async function submitFreqEditInline() {
  const row =
    freqEditingRowId.value !== ''
      ? freqTableData.value.find((item) => item.id === freqEditingRowId.value)
      : null
  if (!row) return ElMessage.warning('请先选择一条需要修改的记录')
  if (row.isNew) return ElMessage.warning('未保存的新增行请先点「新增项次」提交')
  if (!validateFreqEdit(row)) return
  try {
    await updateDailyInspectionFrequency(buildFreqUpdatePayload(row))
    ElMessage.success('修改成功')
    resetFreqInlineState()
    await loadFrequencyPage()
    resetFreqSelection()
  } catch (error) {
    if (error?.elMessageNotified) return
    console.error(error)
    ElMessage.error('修改项次失败')
  }
}

function handleFreqModifyToolbar() {
  if (freqEditingRowId.value) {
    submitFreqEditInline()
  } else {
    startFreqInlineEdit()
  }
}

async function handleFreqDelete() {
  if (!freqSelectedRows.value.length) {
    ElMessage.warning('请选择要删除的记录')
    return
  }

  const selection = [...freqSelectedRows.value]
  const draftIds = new Set(selection.filter((r) => r.isNew).map((r) => r.id))

  if (draftIds.size) {
    freqTableData.value = freqTableData.value.filter((row) => !draftIds.has(row.id))
    if (
      [...draftIds].some((id) => id === freqCreatingRowId.value || id === freqEditingRowId.value)
    ) {
      resetFreqInlineState()
    }
    resetFreqSelection()
    if (!selection.some((r) => !r.isNew)) {
      ElMessage.success('已删除未保存的新增行')
      return
    }
  }

  const persistedRows = selection.filter((r) => !r.isNew)
  if (!persistedRows.length) return

  try {
    await ElMessageBox.confirm(`确定删除选中的 ${persistedRows.length} 条项次记录吗？`, '提示', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }

  const ids = persistedRows.map((r) => r.id)
  try {
    await batchDeleteDailyInspectionFrequency(ids)
    ElMessage.success('删除成功')
    resetFreqInlineState()
    resetFreqSelection()
    await loadFrequencyPage()
  } catch (error) {
    if (error?.elMessageNotified) return
    console.error(error)
    ElMessage.error('删除失败')
  }
}

function onFreqMoreCommand(cmd) {
  if (cmd === 'refresh') {
    if (hasFreqInlineDraftOrEdit()) {
      ElMessage.warning('请先完成项次表格中当前新增或修改')
      return
    }
    loadFrequencyPage()
  }
}

onMounted(async () => {
  await loadProductionLines()
  await loadTablePage()
})
</script>

<template>
  <div class="patrol-page">
    <div class="page-breadcrumb">{{ pageTitle }}</div>

    <div class="search-card">
      <div class="search-card-header">查询区域</div>
      <div class="search-card-body">
        <div class="filter-grid">
          <div class="filter-item">
            <span class="filter-label">作业线</span>
            <el-input
              v-model="filters.productionLineName"
              placeholder="请输入作业线"
              clearable
              @keyup.enter="handleSearch"
            />
          </div>
          <div class="filter-item">
            <span class="filter-label">日常巡检表名</span>
            <el-input
              v-model="filters.tableName"
              placeholder="请输入日常巡检表名"
              clearable
              @keyup.enter="handleSearch"
            />
          </div>
          <div class="filter-item">
            <span class="filter-label">日常巡检表编号</span>
            <el-input
              v-model="filters.tableCode"
              placeholder="请输入日常巡检表编号"
              clearable
              @keyup.enter="handleSearch"
            />
          </div>
          <div class="filter-item">
            <span class="filter-label">倒班模式</span>
            <el-select v-model="filters.shiftMode" placeholder="全部" clearable style="width: 100%">
              <el-option
                v-for="opt in shiftModeFilterOptions"
                :key="opt.label"
                :label="opt.label"
                :value="opt.value"
              />
            </el-select>
          </div>
        </div>
        <div class="search-actions">
          <el-button type="primary" @click="handleSearch">查询</el-button>
        </div>
      </div>
    </div>

    <div class="table-panel">
      <div class="toolbar-row">
        <el-button class="plus-btn" type="primary" :icon="Plus" @click="createEmptyRow" />
        <div class="toolbar-right">
          <el-pagination
            small
            background
            :current-page="pagination.currentPage"
            :page-size="pagination.pageSize"
            :total="pagination.total"
            :page-sizes="[10, 20, 50, 100]"
            layout="sizes, prev, pager, next, jumper"
            @current-change="handlePageChange"
            @size-change="handleSizeChange"
          />
          <div class="toolbar-actions">
            <el-button type="primary" @click="submitCreateInline">新增</el-button>
            <el-button type="primary" plain @click="handleModifyToolbar">修改</el-button>
            <el-button type="danger" plain @click="handleDelete">删除</el-button>
            <el-button type="primary" plain @click="onGeneratePlan">生成计划</el-button>
            <el-dropdown trigger="click" @command="onMoreCommand">
              <el-button type="primary" plain :icon="MoreFilled" class="more-btn" />
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="refresh">刷新列表</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>

      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="tableData"
        border
        class="data-table"
        header-cell-class-name="table-header-cell"
        :row-class-name="tableRowClassName"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="48" align="center" fixed />
        <el-table-column label="日常巡检表编号" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.isNew && !row.tableCode" class="cell-muted">保存后生成</span>
            <span v-else>{{ row.tableCode || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="日常巡检表名" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">
            <el-input
              v-if="isEditingRow(row)"
              v-model="row.tableName"
              size="small"
              placeholder="日常巡检表名"
              maxlength="128"
              class="cell-input"
            />
            <span v-else>{{ row.tableName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="作业线" min-width="170" show-overflow-tooltip>
          <template #default="{ row }">
            <el-select
              v-if="isEditingRow(row)"
              v-model="row.productionLineId"
              filterable
              size="small"
              placeholder="请选择"
              class="cell-select-wide"
              @change="syncProductionLine(row)"
            >
              <el-option
                v-for="line in productionLines"
                :key="line.id"
                :label="line.name"
                :value="line.id"
              />
            </el-select>
            <span v-else>{{ row.productionLineName }}</span>
          </template>
        </el-table-column>
        <el-table-column min-width="148" show-overflow-tooltip>
          <template #header>
            <span class="required-mark">*</span>
            <span>倒班模式</span>
          </template>
          <template #default="{ row }">
            <el-select
              v-if="isEditingRow(row)"
              v-model="row.shiftMode"
              filterable
              allow-create
              default-first-option
              clearable
              size="small"
              placeholder="倒班模式"
              class="cell-select-wide"
            >
              <el-option
                v-for="opt in shiftModeFormOptions"
                :key="opt.value"
                :label="opt.label"
                :value="opt.value"
              />
            </el-select>
            <span v-else>{{ row.shiftMode }}</span>
          </template>
        </el-table-column>
        <el-table-column label="实施人" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">
            <el-input
              v-if="isEditingRow(row)"
              v-model="row.implementer"
              size="small"
              placeholder="选填"
              maxlength="64"
              class="cell-input"
            />
            <span v-else>{{ row.implementer || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-select
              v-if="isEditingRow(row)"
              v-model="row.status"
              size="small"
              class="cell-select-status"
            >
              <el-option
                v-for="opt in statusOptions"
                :key="opt.value"
                :label="opt.label"
                :value="opt.value"
              />
            </el-select>
            <span v-else>{{ row.status }}</span>
          </template>
        </el-table-column>
        <el-table-column label="创建人工号" min-width="110" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.creatorUsername || '—' }}
          </template>
        </el-table-column>
        <el-table-column label="创建人姓名" min-width="120" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.creatorName || '—' }}
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="freq-panel-wrap">
      <div class="freq-panel-head" @click="freqPanelExpanded = !freqPanelExpanded">
        <span class="freq-panel-title">项次表格</span>
        <el-icon class="freq-chevron" :title="freqPanelExpanded ? '收起' : '展开'">
          <ArrowUp v-if="freqPanelExpanded" />
          <ArrowDown v-else />
        </el-icon>
      </div>

      <div v-show="freqPanelExpanded" class="freq-panel-body">
        <div class="freq-filter-bar">
          <div class="freq-filter-summary">
            当前日常巡检表：{{
              selectedPatrolTable
                ? `${selectedPatrolTable.tableName}（${selectedPatrolTable.tableCode || '未生成编号'}）`
                : '请先勾选上方一条日常巡检表'
            }}
          </div>
          <div class="freq-filter-item">
            <span class="freq-filter-label">项次号</span>
            <el-input
              v-model="freqFilters.frequencyCode"
              :disabled="!activePatrolTableId"
              placeholder="支持模糊查询"
              clearable
              size="small"
              @keyup.enter="handleFreqSearch"
            />
          </div>
          <div class="freq-filter-item">
            <span class="freq-filter-label">巡检区域设备/部位</span>
            <el-input
              v-model="freqFilters.areaDeviceName"
              :disabled="!activePatrolTableId"
              placeholder="支持模糊查询"
              clearable
              size="small"
              @keyup.enter="handleFreqSearch"
            />
          </div>
          <el-button
            type="primary"
            size="small"
            :disabled="!activePatrolTableId"
            @click="handleFreqSearch"
          >
            查询
          </el-button>
        </div>

        <div class="toolbar-row">
          <el-button
            class="plus-btn"
            type="primary"
            :icon="Plus"
            :disabled="!activePatrolTableId"
            @click="createFreqEmptyRow"
          />
          <div class="toolbar-right">
            <el-pagination
              small
              background
              :current-page="freqPagination.currentPage"
              :page-size="freqPagination.pageSize"
              :total="freqPagination.total"
              :page-sizes="[10, 20, 50, 100]"
              layout="sizes, prev, pager, next, jumper"
              @current-change="handleFreqPageChange"
              @size-change="handleFreqSizeChange"
            />
            <div class="toolbar-actions">
              <el-button
                type="primary"
                size="small"
                :disabled="!activePatrolTableId"
                @click="submitFreqCreateInline"
              >
                新增项次
              </el-button>
              <el-button type="primary" size="small" plain @click="handleFreqModifyToolbar">
                修改项次
              </el-button>
              <el-button type="danger" size="small" plain @click="handleFreqDelete">
                删除项次
              </el-button>
              <el-dropdown trigger="click" @command="onFreqMoreCommand">
                <el-button type="primary" size="small" plain :icon="MoreFilled" class="more-btn" />
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="refresh">刷新列表</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </div>

        <el-table
          ref="freqTableRef"
          v-loading="freqLoading"
          :data="freqTableData"
          border
          class="data-table freq-table"
          header-cell-class-name="table-header-cell"
          :row-class-name="freqRowClassName"
          @selection-change="handleFreqSelectionChange"
        >
          <el-table-column type="selection" width="48" align="center" fixed />
          <el-table-column min-width="100" fixed>
            <template #header>
              <span class="required-mark">*</span>
              <span>项次号</span>
            </template>
            <template #default="{ row }">
              <el-input
                v-if="isFreqEditingRow(row) && row.isNew"
                v-model="row.frequencyCode"
                size="small"
                placeholder="FQ001"
                class="cell-input"
              />
              <span v-else>{{ row.frequencyCode || '—' }}</span>
            </template>
          </el-table-column>
          <el-table-column min-width="160">
            <template #header>
              <span class="required-mark">*</span>
              <span>巡检区域设备/部位名称</span>
            </template>
            <template #default="{ row }">
              <el-input
                v-if="isFreqEditingRow(row)"
                v-model="row.areaDeviceName"
                size="small"
                placeholder="名称"
                class="cell-input"
              />
              <span v-else>{{ row.areaDeviceName }}</span>
            </template>
          </el-table-column>
          <el-table-column label="巡检内容" min-width="140" show-overflow-tooltip>
            <template #default="{ row }">
              <el-input
                v-if="isFreqEditingRow(row)"
                v-model="row.inspectionContent"
                size="small"
                placeholder="巡检内容"
                type="textarea"
                :autosize="{ minRows: 1, maxRows: 3 }"
                class="cell-input"
              />
              <span v-else>{{ row.inspectionContent ? row.inspectionContent : '—' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="巡检判定标准" min-width="140" show-overflow-tooltip>
            <template #default="{ row }">
              <el-input
                v-if="isFreqEditingRow(row)"
                v-model="row.inspectionStandard"
                size="small"
                placeholder="判定标准"
                type="textarea"
                :autosize="{ minRows: 1, maxRows: 3 }"
                class="cell-input"
              />
              <span v-else>{{ row.inspectionStandard ? row.inspectionStandard : '—' }}</span>
            </template>
          </el-table-column>
          <el-table-column min-width="120" class-name="freq-select-col">
            <template #header>
              <span class="required-mark">*</span>
              <span>数据类别</span>
            </template>
            <template #default="{ row }">
              <el-select
                v-if="isFreqEditingRow(row)"
                v-model="row.dataType"
                teleported
                size="small"
                placeholder="选择"
                class="cell-select-wide"
              >
                <el-option
                  v-for="o in dataTypeOptions"
                  :key="o.value"
                  :label="o.label"
                  :value="o.value"
                />
              </el-select>
              <span v-else>{{ row.dataType ? row.dataType : '—' }}</span>
            </template>
          </el-table-column>
          <el-table-column min-width="110" class-name="freq-select-col">
            <template #header>
              <span class="required-mark">*</span>
              <span>周期单位</span>
            </template>
            <template #default="{ row }">
              <el-select
                v-if="isFreqEditingRow(row)"
                v-model="row.cycleUnit"
                teleported
                size="small"
                placeholder="单位"
                class="cell-select-wide"
              >
                <el-option
                  v-for="o in cycleUnitOptions"
                  :key="o.value"
                  :label="o.label"
                  :value="o.value"
                />
              </el-select>
              <span v-else>{{ row.cycleUnit ? row.cycleUnit : '—' }}</span>
            </template>
          </el-table-column>
          <el-table-column min-width="88" align="right">
            <template #header>
              <span class="required-mark">*</span>
              <span>频次</span>
            </template>
            <template #default="{ row }">
              <el-input-number
                v-if="isFreqEditingRow(row)"
                v-model="row.frequencyValue"
                size="small"
                :min="0"
                :controls="false"
                class="cell-num"
              />
              <span v-else>{{ row.frequencyValue != null ? row.frequencyValue : '—' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="月日期设定" min-width="150" show-overflow-tooltip>
            <template #default="{ row }">
              <el-date-picker
                v-if="isFreqEditingRow(row)"
                v-model="row.monthDateSet"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
                format="YYYY-MM-DD HH:mm"
                placeholder="日期时间"
                size="small"
                class="cell-date"
              />
              <span v-else>{{ displayOptText(row.monthDateSet) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="周日期设定" min-width="140" class-name="freq-select-col">
            <template #default="{ row }">
              <el-select
                v-if="isFreqEditingRow(row)"
                v-model="row.weekDateSet"
                teleported
                size="small"
                placeholder="请选择"
                class="cell-select-wide"
              >
                <el-option
                  v-for="o in weekDateSetOptions"
                  :key="o.value"
                  :label="o.label"
                  :value="o.value"
                />
              </el-select>
              <span v-else>{{ displayOptText(row.weekDateSet) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="班次设定" min-width="130" class-name="freq-select-col">
            <template #default="{ row }">
              <el-select
                v-if="isFreqEditingRow(row)"
                v-model="row.shiftsSet"
                teleported
                size="small"
                placeholder="请选择"
                class="cell-select-wide"
              >
                <el-option
                  v-for="o in shiftsSetOptions"
                  :key="o.value"
                  :label="o.label"
                  :value="o.value"
                />
              </el-select>
              <span v-else>{{ displayOptText(row.shiftsSet) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="上限" min-width="88" align="right">
            <template #default="{ row }">
              <el-input-number
                v-if="isFreqEditingRow(row)"
                v-model="row.maximums"
                size="small"
                :precision="2"
                :controls="false"
                class="cell-num"
              />
              <span v-else>{{ row.maximums != null ? row.maximums : '—' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="下限" min-width="88" align="right">
            <template #default="{ row }">
              <el-input-number
                v-if="isFreqEditingRow(row)"
                v-model="row.minimums"
                size="small"
                :precision="2"
                :controls="false"
                class="cell-num"
              />
              <span v-else>{{ row.minimums != null ? row.minimums : '—' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="创建人工号" min-width="100" show-overflow-tooltip>
            <template #default="{ row }">
              {{ row.creatorUsername || '—' }}
            </template>
          </el-table-column>
          <el-table-column label="创建人姓名" min-width="100" show-overflow-tooltip>
            <template #default="{ row }">
              {{ row.creatorName || '—' }}
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<style scoped>
.patrol-page {
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

.search-card {
  border: 1px solid #d9e2ef;
  background: #fff;
  margin-bottom: 10px;
}

.search-card-header {
  padding: 8px 14px;
  background: #e7f0ff;
  color: #365078;
  font-size: 13px;
  font-weight: 700;
  border-bottom: 1px solid #d9e2ef;
}

.search-card-body {
  padding: 14px 14px 12px;
}

.filter-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px 20px;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.filter-label {
  flex: 0 0 108px;
  color: #47566b;
  font-size: 13px;
  text-align: right;
}

.filter-item :deep(.el-input),
.filter-item :deep(.el-select) {
  flex: 1;
  min-width: 0;
}

.search-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}

.table-panel {
  border: 1px solid #d9e2ef;
  background: #fff;
  padding: 10px 12px 12px;
}

.toolbar-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: flex-end;
  flex: 1;
}

.plus-btn {
  width: 28px;
  height: 28px;
  padding: 0;
}

.toolbar-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.more-btn {
  width: 28px;
  padding: 0;
}

.data-table :deep(.table-header-cell) {
  background: #e7f0ff;
  color: #365078;
  font-size: 12px;
  font-weight: 700;
}

.data-table :deep(.el-table__row) {
  font-size: 12px;
}

.data-table :deep(.el-table__body tr:hover > td) {
  background: #fffaf0;
}

.data-table :deep(.table-row-editing .el-table__cell .cell),
.data-table :deep(.freq-row-editing .el-table__cell .cell) {
  overflow: visible !important;
  white-space: normal;
}

.data-table :deep(.el-select),
.data-table :deep(.el-date-editor),
.data-table :deep(.el-input-number) {
  width: 100%;
}

.data-table :deep(.el-select .el-select__wrapper),
.data-table :deep(.el-input__wrapper),
.data-table :deep(.el-date-editor .el-input__wrapper) {
  min-height: 28px;
}

.required-mark {
  color: #f56c6c;
  margin-right: 2px;
}

.cell-muted {
  color: #909399;
  font-size: 12px;
}

.cell-input {
  width: 100%;
}

.cell-select-wide {
  width: 100%;
  min-width: 120px;
}

.cell-select-status {
  width: 100%;
}

.freq-panel-wrap {
  margin-top: 10px;
  border: 1px solid #d9e2ef;
  background: #fff;
}

.freq-panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  background: #eaf2ff;
  border-bottom: 1px solid #d9e2ef;
  cursor: pointer;
  user-select: none;
}

.freq-panel-title {
  font-size: 13px;
  font-weight: 700;
  color: #365078;
}

.freq-chevron {
  font-size: 16px;
  color: #607697;
}

.freq-panel-body {
  padding: 10px 12px 12px;
}

.freq-filter-bar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.freq-filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.freq-filter-label {
  font-size: 12px;
  color: #47566b;
  white-space: nowrap;
}

.freq-table :deep(.freq-row-editing > td) {
  background: #fffbe6 !important;
}

.cell-num {
  width: 100%;
}

.cell-date {
  width: 100%;
}

.freq-table :deep(.el-table__cell) {
  overflow: hidden;
}

.freq-table :deep(.el-table__cell .cell) {
  overflow: hidden;
}

.freq-table :deep(.freq-row-editing .el-table__cell),
.freq-table :deep(.freq-row-editing .el-table__cell .cell),
.freq-table :deep(.freq-select-col .cell) {
  overflow: visible !important;
}

.freq-table :deep(.freq-select-col .el-select) {
  width: 100%;
}
</style>
