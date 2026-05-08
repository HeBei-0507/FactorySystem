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
import { useUserStore } from '@/stores/user'

const props = defineProps({
  device: { type: Object, default: null },
  equipmentCategoryOptions: { type: Array, default: () => [] },
  equipmentImportanceOptions: { type: Array, default: () => [] },
  repairStrategyOptions: { type: Array, default: () => [] },
  actTeamOptions: { type: Array, default: () => [] },
  overhaulTeamOptions: { type: Array, default: () => [] },
  devices: { type: Array, default: () => [] }
})
const emit = defineEmits(['back'])
const userStore = useUserStore()
if (!userStore.profile) userStore.initFromStorage()

const loading = ref(false)
const importLoading = ref(false)
const selectedRows = ref([])
const tableData = ref([])
const editingRowId = ref('')
const creatingRowId = ref('')
const fileInputRef = ref()
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
const code = computed(() => String(props.device?.equipmentCode || '').trim())
const name = computed(() => String(props.device?.equipmentName || '').trim())
const defaultMaintainer = computed(() => String(userStore.profile?.username || '用户').trim())

const norm = (v) => String(v ?? '').trim()
const optionFields = ['partType', 'repairStrategy', 'importanceLevel', 'repairTeam', 'operateTeam']
const optionValueMaps = computed(() => ({
  partType: new Map(props.equipmentCategoryOptions.map((item) => [item.split('-')[0], item])),
  repairStrategy: new Map(props.repairStrategyOptions.map((item) => [item.split('-')[0], item])),
  importanceLevel: new Map(
    props.equipmentImportanceOptions.map((item) => [item.split('-')[0], item])
  ),
  repairTeam: new Map(props.overhaulTeamOptions.map((item) => [item.split('-')[0], item])),
  operateTeam: new Map(props.actTeamOptions.map((item) => [item.split('-')[0], item]))
}))
function normalizeOptionValue(field, value) {
  const normalized = norm(value)
  if (!normalized || !optionFields.includes(field) || normalized.includes('-')) return normalized
  return optionValueMaps.value[field]?.get(normalized) || normalized
}
function displayOptionValue(field, value) {
  return normalizeOptionValue(field, value)
}
const mapRow = (i = {}, options = {}) => ({
  id: i.id,
  equipmentId: i.equipmentId || props.device?.id || null,
  deviceCode: norm(options.keepDeviceFields ? i.deviceCode : i.deviceCode || code.value),
  deviceName: norm(options.keepDeviceFields ? i.deviceName : i.deviceName || name.value),
  partCode: norm(i.partCode),
  partName: norm(i.partName),
  repairTeam: normalizeOptionValue('repairTeam', i.repairTeam),
  operateTeam: normalizeOptionValue('operateTeam', i.operateTeam),
  repairStrategy: normalizeOptionValue('repairStrategy', i.repairStrategy),
  importanceLevel: normalizeOptionValue('importanceLevel', i.importanceLevel),
  partType: normalizeOptionValue('partType', i.partType),
  maintainer: norm(i.maintainer) || defaultMaintainer.value,
  isNew: !!i.isNew
})
function payload(r, withId = false) {
  const d = mapRow(r)
  const x = {
    equipmentId: d.equipmentId || props.device?.id || null,
    deviceCode: d.deviceCode,
    deviceName: d.deviceName,
    partCode: d.partCode,
    partName: d.partName,
    repairTeam: d.repairTeam,
    operateTeam: d.operateTeam,
    repairStrategy: d.repairStrategy,
    importanceLevel: d.importanceLevel,
    partType: d.partType,
    maintainer: d.maintainer
  }
  if (withId) x.id = d.id
  return x
}
function valid(r) {
  if (!r.equipmentId && !norm(r.deviceCode)) return ElMessage.warning('设备不能为空')
  if (!norm(r.deviceName)) return ElMessage.warning('设备名称不能为空')
  if (!norm(r.partCode)) return ElMessage.warning('设备部位编码不能为空')
  if (!norm(r.partName)) return ElMessage.warning('设备部位名称不能为空')
  return true
}
async function loadPage() {
  if (!code.value) return ((tableData.value = []), (pagination.total = 0))
  loading.value = true
  try {
    const res = await getEquipmentPartPage({
      current: pagination.currentPage,
      size: pagination.pageSize,
      equipmentId: props.device?.id,
      partCode: filters.partCode,
      partName: filters.partName,
      maintainer: filters.maintainer,
      partType: normalizeOptionValue('partType', filters.partType),
      repairStrategy: normalizeOptionValue('repairStrategy', filters.repairStrategy),
      importanceLevel: normalizeOptionValue('importanceLevel', filters.importanceLevel),
      repairTeam: normalizeOptionValue('repairTeam', filters.repairTeam),
      operateTeam: normalizeOptionValue('operateTeam', filters.operateTeam)
    })
    const data = res?.data || {}
    const records = Array.isArray(data.records) ? data.records : []
    tableData.value = records.map(mapRow)
    pagination.total = data.total ?? records.length
    selectedRows.value = []
  } catch (e) {
    if (!e?.elMessageNotified) ElMessage.error('获取设备部位列表失败')
  } finally {
    loading.value = false
  }
}
function createEmptyRow() {
  if (!code.value) return ElMessage.warning('请先选择具体设备')
  if (creatingRowId.value) return ElMessage.warning('请先完成当前新增行')
  const row = mapRow({ id: `draft-${Date.now()}`, isNew: true })
  tableData.value.unshift(row)
  creatingRowId.value = row.id
  editingRowId.value = row.id
  selectedRows.value = []
}
const isEditing = (row) => editingRowId.value === row.id
function startEdit() {
  if (selectedRows.value.length !== 1) return ElMessage.warning('请选择一条设备部位进行修改')
  editingRowId.value = selectedRows.value[0].id
}
async function submitCreate() {
  const row = tableData.value.find((v) => v.id === creatingRowId.value)
  if (!row) return ElMessage.warning('当前没有待新增的设备部位')
  if (valid(row) !== true) return
  try {
    await addEquipmentPart(payload(row))
    creatingRowId.value = ''
    editingRowId.value = ''
    ElMessage.success('新增设备部位成功')
    await loadPage()
  } catch (e) {
    if (!e?.elMessageNotified)
      ElMessage.error(e?.response?.data?.message || e?.message || '新增设备部位失败')
  }
}
async function submitEdit() {
  if (selectedRows.value.length !== 1) return ElMessage.warning('请选择一条设备部位进行修改')
  const row = selectedRows.value[0]
  if (valid(row) !== true) return
  try {
    await updateEquipmentPart(payload(row, true))
    editingRowId.value = ''
    ElMessage.success('修改设备部位成功')
    await loadPage()
  } catch (e) {
    if (!e?.elMessageNotified)
      ElMessage.error(e?.response?.data?.message || e?.message || '修改设备部位失败')
  }
}
async function removeRows() {
  if (!selectedRows.value.length) return ElMessage.warning('请至少选择一条设备部位')
  const draftIds = new Set(selectedRows.value.filter((v) => v.isNew).map((v) => v.id))
  tableData.value = tableData.value.filter((v) => !draftIds.has(v.id))
  const ids = selectedRows.value
    .filter((v) => !v.isNew)
    .map((v) => Number(v.id))
    .filter((v) => Number.isFinite(v) && v > 0)
  if (!ids.length) return ElMessage.success('已删除未保存的新增行')
  try {
    await Promise.all(ids.map((id) => deleteEquipmentPart(id)))
    ElMessage.success('删除设备部位成功')
    creatingRowId.value = ''
    editingRowId.value = ''
    await loadPage()
  } catch (e) {
    if (!e?.elMessageNotified)
      ElMessage.error(e?.response?.data?.message || e?.message || '删除设备部位失败')
  }
}
function pick(row, keys) {
  for (const k of keys) {
    const v = norm(row[k])
    if (v) return v
  }
  return ''
}
function buildDeviceLookupMaps() {
  const devices = Array.isArray(props.devices) ? props.devices : []
  const byCode = new Map()
  const byCodeName = new Map()

  devices.forEach((device) => {
    const deviceCode = norm(device.equipmentCode)
    const deviceName = norm(device.equipmentName)
    if (!deviceCode) return
    if (!byCode.has(deviceCode)) byCode.set(deviceCode, device)
    if (deviceName) byCodeName.set(`${deviceCode}__${deviceName}`, device)
  })

  return { byCode, byCodeName }
}
function resolveImportRows(rows) {
  const { byCode, byCodeName } = buildDeviceLookupMaps()
  const validRows = []
  const failedRecords = []

  rows.forEach((row, index) => {
    const rowNo = index + 2
    const deviceCode = norm(row.deviceCode)
    const deviceName = norm(row.deviceName)
    const exactMatch = byCodeName.get(`${deviceCode}__${deviceName}`)
    const codeMatch = byCode.get(deviceCode)
    const matchedDevice = exactMatch || codeMatch

    if (!matchedDevice) {
      failedRecords.push(`第${rowNo}行：设备编码[${deviceCode}]不存在，已跳过`)
      return
    }
    if (deviceName && norm(matchedDevice.equipmentName) !== deviceName) {
      failedRecords.push(
        `第${rowNo}行：设备编码[${deviceCode}]与设备名称[${deviceName}]不匹配，系统中为[${norm(matchedDevice.equipmentName)}]，已跳过`
      )
      return
    }

    validRows.push({
      ...row,
      equipmentId: matchedDevice.id,
      deviceCode: norm(matchedDevice.equipmentCode),
      deviceName: norm(matchedDevice.equipmentName)
    })
  })

  return { validRows, failedRecords }
}
function buildImportPayload(rows) {
  return rows.map((row) => ({
    equipmentId: row.equipmentId,
    deviceCode: norm(row.deviceCode),
    deviceName: norm(row.deviceName),
    partCode: norm(row.partCode),
    partName: norm(row.partName),
    repairTeam: normalizeOptionValue('repairTeam', row.repairTeam),
    operateTeam: normalizeOptionValue('operateTeam', row.operateTeam),
    repairStrategy: normalizeOptionValue('repairStrategy', row.repairStrategy),
    importanceLevel: normalizeOptionValue('importanceLevel', row.importanceLevel),
    partType: normalizeOptionValue('partType', row.partType),
    maintainer: norm(row.maintainer) || defaultMaintainer.value
  }))
}
function triggerFileInput() {
  fileInputRef.value?.click()
}
async function importExcel(e) {
  const input = e.target
  const file = input?.files?.[0]
  if (!file) return
  importLoading.value = true
  try {
    const wb = XLSX.read(await file.arrayBuffer(), { type: 'array' })
    const rows = XLSX.utils
      .sheet_to_json(wb.Sheets[wb.SheetNames[0]], { defval: '', raw: false })
      .map((r) =>
        mapRow(
          {
            deviceCode: pick(r, ['deviceCode', '设备编码']),
            deviceName: pick(r, ['deviceName', '设备名称']),
            partCode: pick(r, ['partCode', '设备部位编码']),
            partName: pick(r, ['partName', '设备部位名称']),
            repairTeam: pick(r, ['repairTeam', '检修班组']),
            operateTeam: pick(r, ['operateTeam', '操作班组']),
            repairStrategy: pick(r, ['repairStrategy', '维修策略']),
            importanceLevel: pick(r, ['importanceLevel', '设备部位重要度']),
            partType: pick(r, ['partType', '设备部位类别']),
            maintainer: pick(r, ['maintainer', '设备部位维护人'])
          },
          { keepDeviceFields: true }
        )
      )
      .filter(
        (r) =>
          r.deviceCode &&
          r.deviceName &&
          r.partCode &&
          r.partName &&
          r.repairTeam &&
          r.operateTeam &&
          r.repairStrategy &&
          r.importanceLevel &&
          r.partType
      )
    if (!rows.length) return ElMessage.warning('Excel 中没有属性列完整的可导入条目')

    const { validRows, failedRecords: precheckFailedRecords } = resolveImportRows(rows)
    if (!validRows.length) {
      ElMessage.warning('Excel 中没有可匹配到设备档案的可导入条目')
      console.warn('设备部位导入前校验失败记录：', precheckFailedRecords)
      return
    }

    const equipmentPartList = buildImportPayload(validRows)
    const res = await batchAddEquipmentPart({ equipmentPartList })
    const data = res?.data || {}
    const successCount = data.successCount ?? equipmentPartList.length
    const backendFailedRecords = Array.isArray(data.failedRecords) ? data.failedRecords : []
    const failedRecords = [...precheckFailedRecords, ...backendFailedRecords]
    const failedCount = failedRecords.length
    if (failedCount > 0) {
      ElMessage.warning(res?.message || `导入完成：成功${successCount}条，失败${failedCount}条`)
      console.warn('设备部位导入失败记录：', failedRecords)
    } else {
      ElMessage.success(res?.message || `导入成功，共${successCount}条`)
    }
    await loadPage()
  } catch (err) {
    if (!err?.elMessageNotified)
      ElMessage.error(err?.response?.data?.message || err?.message || '批量导入设备部位失败')
  } finally {
    importLoading.value = false
    input.value = ''
  }
}
watch(
  () => props.device?.id,
  () => {
    Object.assign(filters, {
      partCode: '',
      partName: '',
      maintainer: '',
      partType: '',
      repairStrategy: '',
      importanceLevel: '',
      repairTeam: '',
      operateTeam: ''
    })
    pagination.currentPage = 1
    creatingRowId.value = ''
    editingRowId.value = ''
    loadPage()
  },
  { immediate: true }
)
</script>

<template>
  <div class="device-part-panel">
    <div class="filter-bar">
      <div class="filter-item">
        <span class="filter-label">设备编码</span>
        <el-input :model-value="code" disabled />
      </div>
      <div class="filter-item">
        <span class="filter-label">设备名称</span>
        <el-input :model-value="name" disabled />
      </div>
      <div class="filter-item">
        <span class="filter-label">部位编码</span>
        <el-input
          v-model="filters.partCode"
          clearable
          placeholder="请输入部位编码"
          @change="loadPage"
        />
      </div>
      <div class="filter-item">
        <span class="filter-label">部位名称</span>
        <el-input
          v-model="filters.partName"
          clearable
          placeholder="请输入部位名称"
          @change="loadPage"
        />
      </div>
      <div class="filter-item">
        <span class="filter-label">维护人</span>
        <el-input
          v-model="filters.maintainer"
          clearable
          placeholder="请输入维护人"
          @change="loadPage"
        />
      </div>
      <div class="filter-item">
        <span class="filter-label">部位类别</span>
        <el-select
          v-model="filters.partType"
          clearable
          filterable
          allow-create
          default-first-option
          placeholder="请选择或输入部位类别"
          @change="loadPage"
        >
          <el-option
            v-for="item in equipmentCategoryOptions"
            :key="item"
            :label="item"
            :value="item"
          />
        </el-select>
      </div>
      <div class="filter-item">
        <span class="filter-label">维修策略</span>
        <el-select
          v-model="filters.repairStrategy"
          clearable
          filterable
          allow-create
          default-first-option
          placeholder="请选择或输入维修策略"
          @change="loadPage"
        >
          <el-option
            v-for="item in repairStrategyOptions"
            :key="item"
            :label="item"
            :value="item"
          />
        </el-select>
      </div>
      <div class="filter-item">
        <span class="filter-label">设备重要度</span>
        <el-select
          v-model="filters.importanceLevel"
          clearable
          filterable
          allow-create
          default-first-option
          placeholder="请选择或输入设备重要度"
          @change="loadPage"
        >
          <el-option
            v-for="item in equipmentImportanceOptions"
            :key="item"
            :label="item"
            :value="item"
          />
        </el-select>
      </div>
      <div class="filter-item">
        <span class="filter-label">检修班组</span>
        <el-select
          v-model="filters.repairTeam"
          clearable
          filterable
          allow-create
          default-first-option
          placeholder="请选择或输入检修班组"
          @change="loadPage"
        >
          <el-option v-for="item in overhaulTeamOptions" :key="item" :label="item" :value="item" />
        </el-select>
      </div>
      <div class="filter-item">
        <span class="filter-label">操作班组</span>
        <el-select
          v-model="filters.operateTeam"
          clearable
          filterable
          allow-create
          default-first-option
          placeholder="请选择或输入操作班组"
          @change="loadPage"
        >
          <el-option v-for="item in actTeamOptions" :key="item" :label="item" :value="item" />
        </el-select>
      </div>
    </div>
    <div class="toolbar-row">
      <div class="toolbar-left">
        <el-button class="plus-btn" type="primary" :icon="Plus" square @click="createEmptyRow" />
        <el-button @click="emit('back')">返回设备档案视图</el-button>
      </div>
      <div class="toolbar-right">
        <el-pagination
          small
          background
          layout="sizes, prev, pager, next, total"
          :current-page="pagination.currentPage"
          :page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          @current-change="
            (p) => {
              pagination.currentPage = p
              loadPage()
            }
          "
          @size-change="
            (s) => {
              pagination.pageSize = s
              pagination.currentPage = 1
              loadPage()
            }
          "
        />
        <div class="toolbar-actions">
          <el-button type="primary" @click="submitCreate">新增</el-button>
          <el-button type="primary" plain @click="editingRowId ? submitEdit() : startEdit()">
            修改
          </el-button>
          <el-button type="danger" plain @click="removeRows">删除</el-button>
          <el-button type="primary" plain :loading="importLoading" @click="triggerFileInput">
            导入
          </el-button>
          <input
            ref="fileInputRef"
            class="hidden-file-input"
            type="file"
            accept=".xlsx,.xls"
            @change="importExcel"
          />
        </div>
      </div>
    </div>
    <el-table
      v-loading="loading"
      :data="tableData"
      border
      class="device-table"
      header-cell-class-name="table-header-cell"
      @selection-change="(rows) => (selectedRows = rows)"
    >
      <el-table-column type="selection" width="48" align="center" />
      <el-table-column label="设备编码" min-width="120">
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
      <el-table-column label="设备部位编码" min-width="140">
        <template #default="{ row }">
          <el-input v-if="isEditing(row)" v-model="row.partCode" size="small" />
          <span v-else>{{ row.partCode }}</span>
        </template>
      </el-table-column>
      <el-table-column label="设备部位名称" min-width="150">
        <template #default="{ row }">
          <el-input v-if="isEditing(row)" v-model="row.partName" size="small" />
          <span v-else>{{ row.partName }}</span>
        </template>
      </el-table-column>
      <el-table-column label="检修班组" min-width="130">
        <template #default="{ row }">
          <el-select
            v-if="isEditing(row)"
            v-model="row.repairTeam"
            size="small"
            class="editable-select"
            filterable
            allow-create
            default-first-option
            clearable
          >
            <el-option
              v-for="item in overhaulTeamOptions"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
          <span v-else>{{ displayOptionValue('repairTeam', row.repairTeam) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作班组" min-width="130">
        <template #default="{ row }">
          <el-select
            v-if="isEditing(row)"
            v-model="row.operateTeam"
            size="small"
            class="editable-select"
            filterable
            allow-create
            default-first-option
            clearable
          >
            <el-option v-for="item in actTeamOptions" :key="item" :label="item" :value="item" />
          </el-select>
          <span v-else>{{ displayOptionValue('operateTeam', row.operateTeam) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="维修策略" min-width="130">
        <template #default="{ row }">
          <el-select
            v-if="isEditing(row)"
            v-model="row.repairStrategy"
            size="small"
            class="editable-select"
            filterable
            allow-create
            default-first-option
            clearable
          >
            <el-option
              v-for="item in repairStrategyOptions"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
          <span v-else>{{ displayOptionValue('repairStrategy', row.repairStrategy) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="设备部位重要度" min-width="150">
        <template #default="{ row }">
          <el-select
            v-if="isEditing(row)"
            v-model="row.importanceLevel"
            size="small"
            class="editable-select"
            filterable
            allow-create
            default-first-option
            clearable
          >
            <el-option
              v-for="item in equipmentImportanceOptions"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
          <span v-else>{{ displayOptionValue('importanceLevel', row.importanceLevel) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="设备部位类别" min-width="150">
        <template #default="{ row }">
          <el-select
            v-if="isEditing(row)"
            v-model="row.partType"
            size="small"
            class="editable-select"
            filterable
            allow-create
            default-first-option
            clearable
          >
            <el-option
              v-for="item in equipmentCategoryOptions"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
          <span v-else>{{ displayOptionValue('partType', row.partType) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="设备部位维护人" min-width="150">
        <template #default="{ row }">
          <el-input v-if="isEditing(row)" v-model="row.maintainer" size="small" />
          <span v-else>{{ row.maintainer }}</span>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<style scoped>
.device-part-panel {
  min-height: 100%;
}
.filter-bar {
  display: grid;
  grid-template-columns: repeat(5, minmax(180px, 1fr));
  gap: 12px 18px;
  margin-bottom: 16px;
}
.filter-item {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}
.filter-item :deep(.el-input),
.filter-item :deep(.el-select) {
  flex: 1;
  min-width: 0;
}
.filter-label {
  min-width: 72px;
  font-size: 13px;
  white-space: nowrap;
}
.toolbar-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 40px;
  margin-bottom: 10px;
}
.toolbar-left,
.toolbar-right,
.toolbar-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}
.plus-btn {
  width: 28px;
  height: 28px;
  padding: 0;
}
.hidden-file-input {
  display: none;
}
.editable-select {
  width: 100%;
}
.device-table :deep(.table-header-cell) {
  background: #e7f0ff;
  color: #365078;
  font-size: 12px;
  font-weight: 700;
}
</style>
