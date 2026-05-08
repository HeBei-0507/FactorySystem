<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import * as XLSX from 'xlsx'
import {
  addEquipmentPart,
  batchAddEquipmentPart,
  deleteEquipmentPart,
  getEquipmentPartPage,
  updateEquipmentPart
} from '@/api/equipmentPart'
import { extractPageData } from '@/util/apiData'
import { useUserStore } from '@/stores/user'

const props = defineProps({
  device: { type: Object, default: null },
  optionEquipmentCategory: { type: Array, default: () => [] },
  optionEquipmentImportance: { type: Array, default: () => [] },
  optionRepairStrategy: { type: Array, default: () => [] },
  optionActTeam: { type: Array, default: () => [] },
  optionOverhaulTeam: { type: Array, default: () => [] }
})

const emit = defineEmits(['back'])
const userStore = useUserStore()
const loading = ref(false)
const importLoading = ref(false)
const selectedRows = ref([])
const tableData = ref([])
const editingRowId = ref('')
const creatingRowId = ref('')
const fileInputRef = ref(null)
const pagination = reactive({ currentPage: 1, pageSize: 10, total: 0 })
const filters = reactive({
  partCode: '',
  partName: '',
  maintainer: '',
  partType: '',
  repairStrategy: '',
  importanceLevel: '',
  repairTeam: '',
  operateTeam: ''
})
const currentDeviceCode = computed(() => String(props.device?.equipmentCode || '').trim())
const currentDeviceName = computed(() => String(props.device?.equipmentName || '').trim())
const maintainerName = computed(() => {
  const p = userStore.profile
  return (p?.realName || p?.username || userStore.displayName || '').trim() || ''
})
function optionLabel(list, v) {
  if (v == null || v === '') return ''
  const hit = list.find((o) => o.value === v)
  return hit ? hit.label : String(v)
}

function mapPartRow(i = {}) {
  return {
    id: i.id,
    deviceCode: i.deviceCode || currentDeviceCode.value,
    deviceName: i.deviceName || currentDeviceName.value,
    partCode: i.partCode || '',
    partName: i.partName || '',
    repairTeam: i.repairTeam || '',
    operateTeam: i.operateTeam || '',
    repairStrategy: i.repairStrategy || '',
    importanceLevel: i.importanceLevel || '',
    partType: i.partType || '',
    maintainer: i.maintainer || maintainerName.value
  }
}
function createEmptyRow() {
  if (!currentDeviceCode.value) return ElMessage.warning('当前设备编码为空，无法新增部位')
  if (creatingRowId.value) return ElMessage.warning('请先保存当前新增行')
  const row = mapPartRow({ id: `draft-${Date.now()}`, isNew: true })
  tableData.value.unshift({ ...row, isNew: true })
  creatingRowId.value = row.id
  editingRowId.value = row.id
}
function isEditing(row) {
  return editingRowId.value === row.id
}
function startEdit() {
  if (selectedRows.value.length !== 1) return ElMessage.warning('请选择一条设备部位进行修改')
  editingRowId.value = selectedRows.value[0].id
}
function validRow(row) {
  if (!String(row.deviceCode || '').trim()) return (ElMessage.warning('设备编码不能为空'), false)
  if (!String(row.partCode || '').trim()) return (ElMessage.warning('设备部位编码不能为空'), false)
  if (!String(row.partName || '').trim()) return (ElMessage.warning('设备部位名称不能为空'), false)
  return true
}
function buildPayload(row, includeId = false) {
  const data = {
    deviceCode: String(row.deviceCode || '').trim(),
    deviceName: String(row.deviceName || '').trim(),
    partCode: String(row.partCode || '').trim(),
    partName: String(row.partName || '').trim(),
    repairTeam: String(row.repairTeam || '').trim(),
    operateTeam: String(row.operateTeam || '').trim(),
    repairStrategy: String(row.repairStrategy || '').trim(),
    importanceLevel: String(row.importanceLevel || '').trim(),
    partType: String(row.partType || '').trim(),
    maintainer: String(row.maintainer || '').trim()
  }
  if (includeId) data.id = row.id
  return data
}
async function loadPartPage() {
  if (!currentDeviceCode.value) {
    tableData.value = []
    pagination.total = 0
    return
  }
  loading.value = true
  try {
    const res = await getEquipmentPartPage({
      current: pagination.currentPage,
      size: pagination.pageSize,
      deviceCode: currentDeviceCode.value,
      partCode: filters.partCode,
      partName: filters.partName,
      maintainer: filters.maintainer,
      partType: filters.partType,
      repairStrategy: filters.repairStrategy,
      importanceLevel: filters.importanceLevel,
      repairTeam: filters.repairTeam,
      operateTeam: filters.operateTeam
    })
    const { records, total } = extractPageData(res)
    tableData.value = (records || []).map(mapPartRow)
    pagination.total = total
    selectedRows.value = []
  } catch (e) {
    if (e?.elMessageNotified) return
    ElMessage.error('获取设备部位列表失败')
  } finally {
    loading.value = false
  }
}
async function submitCreate() {
  const row = tableData.value.find((v) => v.id === creatingRowId.value)
  if (!row) return ElMessage.warning('当前没有待新增的部位')
  if (!validRow(row)) return
  try {
    await addEquipmentPart(buildPayload(row, false))
    ElMessage.success('新增设备部位成功')
    creatingRowId.value = ''
    editingRowId.value = ''
    await loadPartPage()
  } catch (e) {
    if (e?.elMessageNotified) return
    ElMessage.error(e?.message || '新增设备部位失败')
  }
}
async function submitEdit() {
  if (selectedRows.value.length !== 1) return ElMessage.warning('请选择一条设备部位进行修改')
  const row = selectedRows.value[0]
  if (!validRow(row)) return
  try {
    await updateEquipmentPart(buildPayload(row, true))
    ElMessage.success('修改设备部位成功')
    editingRowId.value = ''
    await loadPartPage()
  } catch (e) {
    if (e?.elMessageNotified) return
    ElMessage.error(e?.message || '修改设备部位失败')
  }
}
async function removeRows() {
  if (!selectedRows.value.length) return ElMessage.warning('请至少选择一条设备部位')
  const draftIds = new Set(selectedRows.value.filter((v) => v.isNew).map((v) => v.id))
  tableData.value = tableData.value.filter((v) => !draftIds.has(v.id))
  const ids = selectedRows.value.filter((v) => !v.isNew).map((v) => v.id)
  if (!ids.length) {
    creatingRowId.value = ''
    editingRowId.value = ''
    selectedRows.value = []
    return ElMessage.success('已删除未保存行')
  }
  try {
    await Promise.all(ids.map((id) => deleteEquipmentPart(id)))
    ElMessage.success('删除设备部位成功')
    creatingRowId.value = ''
    editingRowId.value = ''
    await loadPartPage()
  } catch (e) {
    if (e?.elMessageNotified) return
    ElMessage.error('删除设备部位失败')
  }
}
function handleSearch() {
  pagination.currentPage = 1
  loadPartPage()
}
function handleSelectionChange(rows) {
  selectedRows.value = rows
}
function openImportDialog() {
  if (importLoading.value) return
  fileInputRef.value?.click()
}
function trimVal(v) {
  return String(v ?? '').trim()
}
function pickCellValue(row, keys = []) {
  for (const key of keys) {
    if (row[key] !== undefined && row[key] !== null && trimVal(row[key]) !== '') {
      return trimVal(row[key])
    }
  }
  return ''
}
function mapImportRow(row) {
  return {
    deviceCode: pickCellValue(row, ['deviceCode', '设备编码']),
    deviceName: pickCellValue(row, ['deviceName', '设备名称']),
    partCode: pickCellValue(row, ['partCode', '设备部位编码', '部位编码']),
    partName: pickCellValue(row, ['partName', '设备部位名称', '部位名称']),
    repairTeam: pickCellValue(row, ['repairTeam', '检修班组', '维修班组']),
    operateTeam: pickCellValue(row, ['operateTeam', '操作班组']),
    repairStrategy: pickCellValue(row, ['repairStrategy', '维修策略']),
    importanceLevel: pickCellValue(row, ['importanceLevel', '设备部位重要度', '设备重要度']),
    partType: pickCellValue(row, ['partType', '设备部位类别', '部位类型', '设备类别']),
    maintainer: pickCellValue(row, ['maintainer', '设备部位维护人', '维护人', '设备维护人'])
  }
}
function hasRequiredImportFields(row) {
  return [
    row.deviceCode,
    row.deviceName,
    row.partCode,
    row.partName,
    row.repairTeam,
    row.operateTeam,
    row.repairStrategy,
    row.importanceLevel,
    row.partType
  ].every((v) => trimVal(v) !== '')
}
async function handleImportFileChange(event) {
  const input = event.target
  const file = input?.files?.[0]
  if (!file) return
  if (!/\.(xlsx|xls)$/i.test(file.name)) {
    ElMessage.warning('请上传 .xlsx 或 .xls 文件')
    input.value = ''
    return
  }
  importLoading.value = true
  try {
    const buffer = await file.arrayBuffer()
    const wb = XLSX.read(buffer, { type: 'array' })
    const name = wb.SheetNames[0]
    if (!name) return ElMessage.warning('Excel 中未找到工作表')
    const jsonRows = XLSX.utils.sheet_to_json(wb.Sheets[name], { defval: '', raw: false })
    const rows = jsonRows.map(mapImportRow).filter(hasRequiredImportFields)
    if (!rows.length) return
    const res = await batchAddEquipmentPart({ equipmentPartList: rows })
    const data = res?.data?.data || res?.data || {}
    if (typeof data.successCount === 'number' && typeof data.failedCount === 'number') {
      ElMessage.success(`导入完成：成功${data.successCount}条，失败${data.failedCount}条`)
      if (Array.isArray(data.failedRecords) && data.failedRecords.length) {
        ElMessage.warning(`失败明细：${data.failedRecords[0]}`)
      }
    } else {
      ElMessage.success('批量导入成功')
    }
    await loadPartPage()
  } catch (e) {
    if (e?.elMessageNotified) return
    ElMessage.error('批量导入设备部位失败')
  } finally {
    importLoading.value = false
    input.value = ''
  }
}

watch(
  () => props.device?.id,
  () => {
    pagination.currentPage = 1
    filters.partCode = ''
    filters.partName = ''
    filters.maintainer = ''
    filters.partType = ''
    filters.repairStrategy = ''
    filters.importanceLevel = ''
    filters.repairTeam = ''
    filters.operateTeam = ''
    creatingRowId.value = ''
    editingRowId.value = ''
    loadPartPage()
  },
  { immediate: true }
)
</script>

<template>
  <div class="device-detail">
    <div class="filter-bar">
      <div class="filter-item">
        <span class="filter-label">设备编码</span>
        <el-input :model-value="currentDeviceCode" disabled />
      </div>
      <div class="filter-item">
        <span class="filter-label">设备名称</span>
        <el-input :model-value="currentDeviceName" disabled />
      </div>
      <div class="filter-item">
        <span class="filter-label">部位编码</span>
        <el-input
          v-model="filters.partCode"
          clearable
          placeholder="请输入部位编码"
          @change="handleSearch"
        />
      </div>
      <div class="filter-item">
        <span class="filter-label">部位名称</span>
        <el-input
          v-model="filters.partName"
          clearable
          placeholder="请输入部位名称"
          @change="handleSearch"
        />
      </div>
      <div class="filter-item">
        <span class="filter-label">维护人</span>
        <el-input
          v-model="filters.maintainer"
          clearable
          placeholder="请输入维护人"
          @change="handleSearch"
        />
      </div>
      <div class="filter-item">
        <span class="filter-label">部位类型</span>
        <el-select
          v-model="filters.partType"
          clearable
          filterable
          placeholder="请选择部位类型"
          @change="handleSearch"
        >
          <el-option
            v-for="o in optionEquipmentCategory"
            :key="o.value"
            :label="o.label"
            :value="o.value"
          />
        </el-select>
      </div>
      <div class="filter-item">
        <span class="filter-label">维修策略</span>
        <el-select
          v-model="filters.repairStrategy"
          clearable
          filterable
          placeholder="请选择维修策略"
          @change="handleSearch"
        >
          <el-option
            v-for="o in optionRepairStrategy"
            :key="o.value"
            :label="o.label"
            :value="o.value"
          />
        </el-select>
      </div>
      <div class="filter-item">
        <span class="filter-label">设备重要度</span>
        <el-select
          v-model="filters.importanceLevel"
          clearable
          filterable
          placeholder="请选择设备重要度"
          @change="handleSearch"
        >
          <el-option
            v-for="o in optionEquipmentImportance"
            :key="o.value"
            :label="o.label"
            :value="o.value"
          />
        </el-select>
      </div>
      <div class="filter-item">
        <span class="filter-label">检修班组</span>
        <el-select
          v-model="filters.repairTeam"
          clearable
          filterable
          placeholder="请选择检修班组"
          @change="handleSearch"
        >
          <el-option
            v-for="o in optionOverhaulTeam"
            :key="o.value"
            :label="o.label"
            :value="o.value"
          />
        </el-select>
      </div>
      <div class="filter-item">
        <span class="filter-label">操作班组</span>
        <el-select
          v-model="filters.operateTeam"
          clearable
          filterable
          placeholder="请选择操作班组"
          @change="handleSearch"
        >
          <el-option v-for="o in optionActTeam" :key="o.value" :label="o.label" :value="o.value" />
        </el-select>
      </div>
    </div>

    <div class="toolbar-row">
      <div class="toolbar-left">
        <el-button class="plus-btn" type="primary" :icon="Plus" square @click="createEmptyRow" />
        <el-button @click="$emit('back')">返回设备单元视图</el-button>
      </div>
      <div class="toolbar-right">
        <div class="import-actions">
          <el-button type="primary" plain :loading="importLoading" @click="openImportDialog">
            批量导入
          </el-button>
          <input
            ref="fileInputRef"
            class="hidden-file-input"
            type="file"
            accept=".xlsx,.xls"
            @change="handleImportFileChange"
          />
        </div>
        <el-pagination
          small
          background
          layout="prev, pager, next"
          :current-page="pagination.currentPage"
          :page-size="pagination.pageSize"
          :total="pagination.total"
          @current-change="
            (p) => {
              pagination.currentPage = p
              loadPartPage()
            }
          "
        />
        <div class="toolbar-actions">
          <el-button type="primary" @click="submitCreate">新增</el-button>
          <el-button type="primary" plain @click="editingRowId ? submitEdit() : startEdit()">
            修改
          </el-button>
          <el-button type="danger" plain @click="removeRows">删除</el-button>
        </div>
      </div>
    </div>

    <el-table
      v-loading="loading"
      :data="tableData"
      border
      class="device-table"
      header-cell-class-name="table-header-cell"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="48" align="center" fixed />
      <el-table-column label="设备编码" min-width="140">
        <template #default="{ row }">
          <el-input v-if="isEditing(row)" v-model="row.deviceCode" size="small" disabled />
          <span v-else>{{ row.deviceCode }}</span>
        </template>
      </el-table-column>
      <el-table-column label="设备名称" min-width="140">
        <template #default="{ row }">
          <el-input v-if="isEditing(row)" v-model="row.deviceName" size="small" disabled />
          <span v-else>{{ row.deviceName }}</span>
        </template>
      </el-table-column>
      <el-table-column label="部位编码" min-width="140">
        <template #default="{ row }">
          <el-input v-if="isEditing(row)" v-model="row.partCode" size="small" />
          <span v-else>{{ row.partCode }}</span>
        </template>
      </el-table-column>
      <el-table-column label="部位名称" min-width="150">
        <template #default="{ row }">
          <el-input v-if="isEditing(row)" v-model="row.partName" size="small" />
          <span v-else>{{ row.partName }}</span>
        </template>
      </el-table-column>
      <el-table-column label="检修班组" min-width="150">
        <template #default="{ row }">
          <el-select
            v-if="isEditing(row)"
            v-model="row.repairTeam"
            size="small"
            class="sel-full"
            clearable
          >
            <el-option
              v-for="o in optionOverhaulTeam"
              :key="o.value"
              :label="o.label"
              :value="o.value"
            />
          </el-select>
          <span v-else>{{ optionLabel(optionOverhaulTeam, row.repairTeam) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作班组" min-width="150">
        <template #default="{ row }">
          <el-select
            v-if="isEditing(row)"
            v-model="row.operateTeam"
            size="small"
            class="sel-full"
            clearable
          >
            <el-option
              v-for="o in optionActTeam"
              :key="o.value"
              :label="o.label"
              :value="o.value"
            />
          </el-select>
          <span v-else>{{ optionLabel(optionActTeam, row.operateTeam) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="维修策略" min-width="150">
        <template #default="{ row }">
          <el-select
            v-if="isEditing(row)"
            v-model="row.repairStrategy"
            size="small"
            class="sel-full"
            clearable
          >
            <el-option
              v-for="o in optionRepairStrategy"
              :key="o.value"
              :label="o.label"
              :value="o.value"
            />
          </el-select>
          <span v-else>{{ optionLabel(optionRepairStrategy, row.repairStrategy) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="设备重要度" min-width="150">
        <template #default="{ row }">
          <el-select
            v-if="isEditing(row)"
            v-model="row.importanceLevel"
            size="small"
            class="sel-full"
            clearable
          >
            <el-option
              v-for="o in optionEquipmentImportance"
              :key="o.value"
              :label="o.label"
              :value="o.value"
            />
          </el-select>
          <span v-else>{{ optionLabel(optionEquipmentImportance, row.importanceLevel) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="部位类型" min-width="150">
        <template #default="{ row }">
          <el-select
            v-if="isEditing(row)"
            v-model="row.partType"
            size="small"
            class="sel-full"
            clearable
          >
            <el-option
              v-for="o in optionEquipmentCategory"
              :key="o.value"
              :label="o.label"
              :value="o.value"
            />
          </el-select>
          <span v-else>{{ optionLabel(optionEquipmentCategory, row.partType) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="维护人" min-width="150">
        <template #default="{ row }">
          <el-input v-if="isEditing(row)" v-model="row.maintainer" size="small" />
          <span v-else>{{ row.maintainer }}</span>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<style scoped>
.device-detail {
  min-height: 100%;
}
.filter-bar {
  display: grid;
  grid-template-columns: repeat(5, minmax(180px, 1fr));
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
.toolbar-left {
  display: flex;
  align-items: center;
  gap: 8px;
}
.toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}
.toolbar-actions {
  display: flex;
  gap: 8px;
}
.import-actions {
  display: flex;
  align-items: center;
}
.hidden-file-input {
  display: none;
}
.plus-btn {
  width: 28px;
  height: 28px;
  padding: 0;
}
.sel-full {
  width: 100%;
  min-width: 0;
}
.device-table :deep(.table-header-cell) {
  background: #e7f0ff;
  color: #365078;
  font-size: 12px;
  font-weight: 700;
}
</style>
