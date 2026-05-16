<script setup>
import { computed, nextTick, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getProductionlineList } from '@/api/prodectionLine'
import { getDeviceUtilList } from '@/api/deviceUnit'
import { getEquipmentListByDeviceUnitId } from '@/api/equipment'
import { getEquipmentPartListByEquipmentId } from '@/api/equipmentPart'
import {
  addLubricationStandard,
  deleteLubricationStandard,
  getLubricationStandardPage,
  updateLubricationStandard
} from '@/api/lubricationStandard'
import { extractPageData, extractRecordsArray } from '@/util/apiData'

const pageTitle = '润滑标准管理'
const productionLines = ref([])
const expandedUnitIds = ref([])
const expandedDeviceIds = ref([])
const lubricationRows = ref([])
const lubricationSelectedRows = ref([])
const lubricationLoading = ref(false)
const lubricationSelectionSyncLocked = ref(false)
const lubricationPagination = reactive({ currentPage: 1, pageSize: 10, total: 0 })
const lubricationDrawerVisible = ref(false)
const lubricationDrawerMode = ref('create')
const lubricationDrawerSaving = ref(false)
const activeNode = reactive({ type: 'line', lineId: '', unitId: '', deviceId: '', partCode: '', partName: '' })
const lubricationFilters = reactive({
  standardCode: '',
  partCode: '',
  partName: '',
  feedPoint: '',
  oilModels: '',
  profession: '',
  oilFeedType: ''
})
const lubricationForm = reactive({
  id: '',
  partCode: '',
  partName: '',
  standardCode: '',
  standardName: '',
  method: '',
  profession: '',
  oilFeedType: '',
  feedPoint: '',
  oilModels: '',
  oilVolume: '',
  nextCheckTime: ''
})

const optionMethod = [
  { value: '01', label: '01-手工润滑' },
  { value: '02', label: '02-滴注润滑' },
  { value: '03', label: '03-飞溅润滑' },
  { value: '04', label: '04-油环与油链润滑' },
  { value: '05', label: '05-油绳与油垫润滑' },
  { value: '06', label: '06-自润滑' }
]
const optionProfession = [
  { value: '01', label: '01-机械专业' },
  { value: '02', label: '02-电气专业' },
  { value: '03', label: '03-仪表专业' },
  { value: '04', label: '04-过程控制专业' }
]
const optionOilFeedType = [
  { value: '01', label: '01-补油' },
  { value: '02', label: '02-换油' },
  { value: '03', label: '03-油质化验' }
]

function optionLabel(options, value) {
  if (value == null || value === '') return ''
  return options.find((x) => x.value === value)?.label || String(value)
}

const activeLine = computed(() => productionLines.value.find((v) => String(v.id) === String(activeNode.lineId)) || null)
const activeUnit = computed(() => activeLine.value?.units.find((v) => String(v.id) === String(activeNode.unitId)) || null)
const activeDevice = computed(() => activeUnit.value?.devices.find((v) => String(v.id) === String(activeNode.deviceId)) || null)
const activePart = computed(() => activeDevice.value?.parts.find((v) => v.partCode === activeNode.partCode) || null)
const activeScopeLabel = computed(() => {
  if (activeNode.type === 'part') return activePart.value?.partName || activeNode.partName || '未选择设备部位'
  if (activeNode.type === 'device') return activeDevice.value?.equipmentName || '未选择设备'
  if (activeNode.type === 'unit') return activeUnit.value?.unitName || '未选择设备单元'
  return activeLine.value?.name || '未选择生产线'
})

const mapLine = (i) => ({ id: i.id, name: i.lineName, units: [] })
const mapUnit = (i) => ({ id: i.id, unitName: i.unitName, devices: [] })
const mapDevice = (i) => ({ id: i.id, equipmentName: i.equipmentName || '', parts: [] })
const mapPart = (i) => ({ id: i.id, partCode: String(i.partCode || '').trim(), partName: String(i.partName || '').trim() })
const mapLubricationRow = (i = {}) => ({
  id: i.id,
  partCode: i.partCode || '',
  partName: i.partName || '',
  standardCode: i.standardCode || '',
  standardName: i.standardName || '',
  method: i.method || '',
  profession: i.profession || '',
  oilFeedType: i.oilFeedType || '',
  feedPoint: i.feedPoint || '',
  oilModels: i.oilModels || '',
  oilVolume: i.oilVolume || '',
  nextCheckTime: i.nextCheckTime || ''
})

function resetActiveNode(payload = {}) {
  Object.assign(activeNode, { type: 'line', lineId: '', unitId: '', deviceId: '', partCode: '', partName: '' }, payload)
}
function isUnitExpanded(unit) {
  return expandedUnitIds.value.includes(unit.id)
}
function isDeviceExpanded(device) {
  return expandedDeviceIds.value.includes(device.id)
}

async function loadProductionLines() {
  try {
    const res = await getProductionlineList()
    productionLines.value = extractRecordsArray(res?.data).map(mapLine)
    if (productionLines.value.length) await clickLine(productionLines.value[0])
  } catch (e) {
    if (!e?.elMessageNotified) ElMessage.error('获取生产线列表失败')
  }
}
async function loadUnits(line) {
  const res = await getDeviceUtilList(line.id)
  line.units = extractRecordsArray(res?.data).map(mapUnit)
}
async function loadDevices(unit) {
  const res = await getEquipmentListByDeviceUnitId(unit.id)
  unit.devices = extractRecordsArray(res?.data).map(mapDevice)
}
async function loadParts(device) {
  const res = await getEquipmentPartListByEquipmentId(device.id)
  const rawList = Array.isArray(res?.data) ? res.data : extractRecordsArray(res?.data)
  device.parts = (rawList || []).map(mapPart).filter((x) => x.partCode || x.partName)
}

async function clickLine(line) {
  resetActiveNode({ type: 'line', lineId: line.id })
  lubricationFilters.partCode = ''
  lubricationFilters.partName = ''
  if (!line.units.length) await loadUnits(line)
  lubricationPagination.currentPage = 1
  await loadLubricationPage()
}
async function clickUnit(line, unit) {
  resetActiveNode({ type: 'unit', lineId: line.id, unitId: unit.id })
  lubricationFilters.partCode = ''
  lubricationFilters.partName = ''
  if (!expandedUnitIds.value.includes(unit.id)) expandedUnitIds.value.push(unit.id)
  if (!unit.devices.length) await loadDevices(unit)
  lubricationPagination.currentPage = 1
  await loadLubricationPage()
}
async function clickDevice(line, unit, device) {
  resetActiveNode({ type: 'device', lineId: line.id, unitId: unit.id, deviceId: device.id })
  lubricationFilters.partCode = ''
  lubricationFilters.partName = ''
  if (!expandedUnitIds.value.includes(unit.id)) expandedUnitIds.value.push(unit.id)
  if (!device.parts.length) await loadParts(device)
  lubricationPagination.currentPage = 1
  await loadLubricationPage()
}
async function clickPart(line, unit, device, part) {
  resetActiveNode({ type: 'part', lineId: line.id, unitId: unit.id, deviceId: device.id, partCode: part.partCode, partName: part.partName })
  lubricationFilters.partCode = part.partCode
  lubricationFilters.partName = part.partName
  if (!expandedUnitIds.value.includes(unit.id)) expandedUnitIds.value.push(unit.id)
  if (!expandedDeviceIds.value.includes(device.id)) expandedDeviceIds.value.push(device.id)
  lubricationPagination.currentPage = 1
  await loadLubricationPage()
}
async function toggleUnitExpand(unit) {
  if (isUnitExpanded(unit)) {
    expandedUnitIds.value = expandedUnitIds.value.filter((id) => id !== unit.id)
    return
  }
  expandedUnitIds.value.push(unit.id)
  if (!unit.devices.length) await loadDevices(unit)
}
async function toggleDeviceExpand(device) {
  if (isDeviceExpanded(device)) {
    expandedDeviceIds.value = expandedDeviceIds.value.filter((id) => id !== device.id)
    return
  }
  expandedDeviceIds.value.push(device.id)
  if (!device.parts.length) await loadParts(device)
}

async function loadLubricationPage() {
  lubricationLoading.value = true
  lubricationSelectionSyncLocked.value = true
  try {
    const res = await getLubricationStandardPage({
      current: lubricationPagination.currentPage,
      size: lubricationPagination.pageSize,
      productionLineId: activeNode.lineId || undefined,
      deviceUnitId: ['unit', 'device', 'part'].includes(activeNode.type) ? activeNode.unitId || undefined : undefined,
      equipmentId: ['device', 'part'].includes(activeNode.type) ? activeNode.deviceId || undefined : undefined,
      standardCode: lubricationFilters.standardCode,
      partCode: lubricationFilters.partCode,
      partName: lubricationFilters.partName,
      feedPoint: lubricationFilters.feedPoint,
      oilModels: lubricationFilters.oilModels,
      profession: lubricationFilters.profession,
      oilFeedType: lubricationFilters.oilFeedType
    })
    const { records, total } = extractPageData(res)
    lubricationRows.value = (records || []).map(mapLubricationRow)
    lubricationPagination.total = total
    lubricationSelectedRows.value = []
  } catch (e) {
    if (!e?.elMessageNotified) ElMessage.error('获取润滑标准列表失败')
  } finally {
    await nextTick()
    lubricationSelectionSyncLocked.value = false
    lubricationLoading.value = false
  }
}
function handleLubricationSearch() {
  lubricationPagination.currentPage = 1
  loadLubricationPage()
}
function handleLubricationSelectionChange(rows) {
  if (lubricationSelectionSyncLocked.value) return
  lubricationSelectedRows.value = rows
}
function resetLubricationForm() {
  Object.assign(lubricationForm, {
    id: '',
    partCode: '',
    partName: '',
    standardCode: '',
    standardName: '',
    method: '',
    profession: '',
    oilFeedType: '',
    feedPoint: '',
    oilModels: '',
    oilVolume: '',
    nextCheckTime: ''
  })
}
function openCreateLubricationDrawer() {
  if (activeNode.type !== 'part') return ElMessage.warning('请先在左侧导航树中定位到具体设备部位后再新增')
  resetLubricationForm()
  lubricationForm.partCode = activeNode.partCode
  lubricationForm.partName = activeNode.partName
  lubricationDrawerMode.value = 'create'
  lubricationDrawerVisible.value = true
}
function openEditLubricationDrawer() {
  if (lubricationSelectedRows.value.length !== 1) return ElMessage.warning('请选择一条润滑标准进行修改')
  Object.assign(lubricationForm, lubricationSelectedRows.value[0])
  lubricationDrawerMode.value = 'edit'
  lubricationDrawerVisible.value = true
}
function validLubricationRow(row) {
  if (!String(row.partCode || '').trim()) return (ElMessage.warning('部位编码不能为空'), false)
  if (!String(row.partName || '').trim()) return (ElMessage.warning('部位名称不能为空'), false)
  if (!String(row.standardName || '').trim()) return (ElMessage.warning('标准名称不能为空'), false)
  if (!String(row.method || '').trim()) return (ElMessage.warning('润滑方式不能为空'), false)
  if (!String(row.profession || '').trim()) return (ElMessage.warning('专业不能为空'), false)
  if (!String(row.oilFeedType || '').trim()) return (ElMessage.warning('给油类型不能为空'), false)
  return true
}
function buildLubricationPayload(row, includeId = false) {
  const data = {
    partCode: String(row.partCode || '').trim(),
    partName: String(row.partName || '').trim(),
    standardName: String(row.standardName || '').trim(),
    method: String(row.method || '').trim(),
    profession: String(row.profession || '').trim(),
    oilFeedType: String(row.oilFeedType || '').trim(),
    feedPoint: String(row.feedPoint || '').trim(),
    oilModels: String(row.oilModels || '').trim(),
    oilVolume: String(row.oilVolume || '').trim(),
    nextCheckTime: String(row.nextCheckTime || '').trim()
  }
  if (includeId) {
    data.id = row.id
    if (String(row.standardCode || '').trim()) data.standardCode = String(row.standardCode).trim()
  }
  return data
}
async function submitCreateLubrication() {
  if (!validLubricationRow(lubricationForm)) return
  try {
    lubricationDrawerSaving.value = true
    await addLubricationStandard(buildLubricationPayload(lubricationForm, false))
    ElMessage.success('新增润滑标准成功')
    lubricationDrawerVisible.value = false
    await loadLubricationPage()
  } catch (e) {
    if (!e?.elMessageNotified) ElMessage.error('新增润滑标准失败')
  } finally {
    lubricationDrawerSaving.value = false
  }
}
async function submitEditLubrication() {
  if (!lubricationForm.id) return ElMessage.warning('润滑标准ID无效')
  if (!validLubricationRow(lubricationForm)) return
  try {
    lubricationDrawerSaving.value = true
    await updateLubricationStandard(buildLubricationPayload(lubricationForm, true))
    ElMessage.success('修改润滑标准成功')
    lubricationDrawerVisible.value = false
    await loadLubricationPage()
  } catch (e) {
    if (!e?.elMessageNotified) ElMessage.error('修改润滑标准失败')
  } finally {
    lubricationDrawerSaving.value = false
  }
}
async function removeLubricationRows() {
  if (!lubricationSelectedRows.value.length) return ElMessage.warning('请至少选择一条润滑标准')
  try {
    await Promise.all(lubricationSelectedRows.value.map((x) => deleteLubricationStandard(x.id)))
    ElMessage.success('删除润滑标准成功')
    await loadLubricationPage()
  } catch (e) {
    if (!e?.elMessageNotified) ElMessage.error('删除润滑标准失败')
  }
}
loadProductionLines()
</script>

<template>
  <div class="standard-page">
    <div class="page-breadcrumb">
      {{ pageTitle }} &gt; {{ activeScopeLabel }}
    </div>
    <div class="page-content">
      <aside class="tree-panel">
        <div class="tree-panel-header">设备树导航</div>
        <div class="tree-content">
          <div v-for="line in productionLines" :key="line.id" class="line-block">
            <div :class="['line-title', { active: String(line.id) === String(activeNode.lineId) && activeNode.type === 'line' }]" @click="clickLine(line)">
              {{ line.name }}
            </div>
            <div class="unit-list">
              <div v-for="unit in line.units" :key="unit.id" :class="['unit-item', { active: String(unit.id) === String(activeNode.unitId) && activeNode.type === 'unit' }]">
                <div class="unit-row" @click="clickUnit(line, unit)">
                  <span class="arrow" @click.stop="toggleUnitExpand(unit)">{{ isUnitExpanded(unit) ? '⌄' : '›' }}</span>
                  <span>{{ unit.unitName }}</span>
                </div>
                <div v-show="isUnitExpanded(unit)" class="device-list">
                  <div v-for="device in unit.devices" :key="device.id" class="device-block">
                    <div :class="['device-item', { active: String(device.id) === String(activeNode.deviceId) && activeNode.type === 'device' }]" @click.stop="clickDevice(line, unit, device)">
                      <span class="arrow" @click.stop="toggleDeviceExpand(device)">{{ isDeviceExpanded(device) ? '⌄' : '›' }}</span>
                      <span>{{ device.equipmentName }}</span>
                    </div>
                    <div v-show="isDeviceExpanded(device)" class="part-list">
                      <div v-for="part in device.parts" :key="part.partCode" :class="['part-item', { active: part.partCode === activeNode.partCode && activeNode.type === 'part' }]" @click.stop="clickPart(line, unit, device, part)">
                        {{ part.partName }}
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </aside>

      <section class="table-panel">
        <div class="toolbar-row">
          <div class="selected-device">当前定位：{{ activeScopeLabel }}</div>
        </div>

        <div class="filter-bar">
          <div class="filter-item">
            <span class="filter-label">标准编码</span>
            <el-input v-model="lubricationFilters.standardCode" clearable placeholder="请输入标准编码" />
          </div>
          <div class="filter-item">
            <span class="filter-label">部位编码</span>
            <el-input v-model="lubricationFilters.partCode" :disabled="activeNode.type === 'part'" placeholder="定位到设备部位时自动带出" />
          </div>
          <div class="filter-item">
            <span class="filter-label">部位名称</span>
            <el-input v-model="lubricationFilters.partName" :disabled="activeNode.type === 'part'" placeholder="定位到设备部位时自动带出" />
          </div>
          <div class="filter-item">
            <span class="filter-label">给油点</span>
            <el-input v-model="lubricationFilters.feedPoint" clearable placeholder="请输入给油点" />
          </div>
          <div class="filter-item">
            <span class="filter-label">油品型号</span>
            <el-input v-model="lubricationFilters.oilModels" clearable placeholder="请输入油品型号" />
          </div>
          <div class="filter-item">
            <span class="filter-label">专业</span>
            <el-select v-model="lubricationFilters.profession" clearable placeholder="请选择专业">
              <el-option v-for="o in optionProfession" :key="o.value" :label="o.label" :value="o.value" />
            </el-select>
          </div>
          <div class="filter-item">
            <span class="filter-label">给油类型</span>
            <el-select v-model="lubricationFilters.oilFeedType" clearable placeholder="请选择给油类型">
              <el-option v-for="o in optionOilFeedType" :key="o.value" :label="o.label" :value="o.value" />
            </el-select>
          </div>
          <div class="filter-item">
            <el-button type="primary" @click="handleLubricationSearch">查询</el-button>
          </div>
        </div>

        <div class="toolbar-row">
          <el-pagination
            small
            background
            layout="prev, pager, next"
            :current-page="lubricationPagination.currentPage"
            :page-size="lubricationPagination.pageSize"
            :total="lubricationPagination.total"
            @current-change="(p) => { lubricationPagination.currentPage = p; loadLubricationPage() }"
          />
          <div class="toolbar-actions">
            <el-button type="primary" @click="openCreateLubricationDrawer">新增</el-button>
            <el-button type="primary" plain @click="openEditLubricationDrawer">修改</el-button>
            <el-button type="danger" plain @click="removeLubricationRows">删除</el-button>
          </div>
        </div>

        <el-table v-loading="lubricationLoading" :data="lubricationRows" border class="standard-table" header-cell-class-name="table-header-cell" @selection-change="handleLubricationSelectionChange">
          <el-table-column type="selection" width="48" align="center" />
          <el-table-column label="部位编码" prop="partCode" min-width="120" />
          <el-table-column label="部位名称" prop="partName" min-width="140" />
          <el-table-column label="标准编码" prop="standardCode" min-width="140" />
          <el-table-column label="标准名称" prop="standardName" min-width="170" />
          <el-table-column label="润滑方式" min-width="180"><template #default="{ row }">{{ optionLabel(optionMethod, row.method) }}</template></el-table-column>
          <el-table-column label="专业" min-width="150"><template #default="{ row }">{{ optionLabel(optionProfession, row.profession) }}</template></el-table-column>
          <el-table-column label="给油类型" min-width="150"><template #default="{ row }">{{ optionLabel(optionOilFeedType, row.oilFeedType) }}</template></el-table-column>
          <el-table-column label="给油点" prop="feedPoint" min-width="180" />
          <el-table-column label="油品型号" prop="oilModels" min-width="150" />
          <el-table-column label="油量" prop="oilVolume" min-width="120" />
          <el-table-column label="下次点检时间" prop="nextCheckTime" min-width="180" />
        </el-table>
      </section>
    </div>

    <el-drawer v-model="lubricationDrawerVisible" :title="lubricationDrawerMode === 'create' ? '新增润滑标准' : '修改润滑标准'" direction="rtl" size="520px">
      <el-form label-width="100px" class="drawer-form">
        <el-form-item label="部位名称"><el-input v-model="lubricationForm.partName" disabled /></el-form-item>
        <el-form-item label="部位编码"><el-input v-model="lubricationForm.partCode" disabled /></el-form-item>
        <el-form-item v-if="lubricationDrawerMode === 'edit'" label="标准编码"><el-input v-model="lubricationForm.standardCode" disabled /></el-form-item>
        <el-form-item label="标准名称"><el-input v-model="lubricationForm.standardName" /></el-form-item>
        <el-form-item label="润滑方式"><el-select v-model="lubricationForm.method" class="sel-full" clearable><el-option v-for="o in optionMethod" :key="o.value" :label="o.label" :value="o.value" /></el-select></el-form-item>
        <el-form-item label="专业"><el-select v-model="lubricationForm.profession" class="sel-full" clearable><el-option v-for="o in optionProfession" :key="o.value" :label="o.label" :value="o.value" /></el-select></el-form-item>
        <el-form-item label="给油类型"><el-select v-model="lubricationForm.oilFeedType" class="sel-full" clearable><el-option v-for="o in optionOilFeedType" :key="o.value" :label="o.label" :value="o.value" /></el-select></el-form-item>
        <el-form-item label="给油点"><el-input v-model="lubricationForm.feedPoint" /></el-form-item>
        <el-form-item label="油品型号"><el-input v-model="lubricationForm.oilModels" /></el-form-item>
        <el-form-item label="油量"><el-input v-model="lubricationForm.oilVolume" /></el-form-item>
        <el-form-item label="下次点检"><el-date-picker v-model="lubricationForm.nextCheckTime" type="datetime" class="sel-full" placeholder="请选择下次点检时间" format="YYYY-MM-DD HH:mm:ss" value-format="YYYY-MM-DD HH:mm:ss" /></el-form-item>
      </el-form>
      <template #footer>
        <div class="drawer-footer">
          <el-button @click="lubricationDrawerVisible = false">取消</el-button>
          <el-button type="primary" :loading="lubricationDrawerSaving" @click="lubricationDrawerMode === 'create' ? submitCreateLubrication() : submitEditLubrication()">保存</el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<style scoped>
.standard-page { min-height: calc(100vh - 36px); padding: 10px; background: #f5f7fa; }
.page-breadcrumb { margin-bottom: 8px; color: #2f3b52; font-size: 14px; font-weight: 700; }
.page-content { display: grid; grid-template-columns: 320px 1fr; gap: 8px; min-height: calc(100vh - 90px); }
.tree-panel, .table-panel { border: 1px solid #d9e2ef; background: #fff; }
.tree-panel-header { padding: 10px 14px; border-bottom: 1px solid #e6edf6; color: #33507b; font-size: 13px; font-weight: 700; }
.tree-content { padding: 10px 12px; }
.line-block + .line-block { margin-top: 12px; }
.line-title { margin-bottom: 6px; font-weight: 700; cursor: pointer; }
.line-title.active { color: #2f76e8; }
.unit-item { padding: 8px 10px; border-radius: 4px; cursor: pointer; }
.unit-item:hover { background: #f2f7ff; }
.unit-item.active { background: #eaf2ff; color: #2f76e8; }
.unit-row { display: flex; gap: 6px; font-weight: 700; }
.device-list { margin-top: 4px; margin-left: 18px; }
.device-block { margin-top: 4px; }
.device-item { padding: 3px 0; color: #5a6a85; font-size: 12px; cursor: pointer; display: flex; gap: 6px; }
.device-item.active, .part-item.active { color: #2f76e8; font-weight: 700; }
.part-list { margin-top: 4px; margin-left: 18px; }
.part-item { padding: 3px 0; color: #5a6a85; font-size: 12px; cursor: pointer; }
.arrow { width: 12px; display: inline-flex; justify-content: center; }
.table-panel { padding: 10px 12px; overflow: auto; }
.toolbar-row { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.selected-device { color: #47566b; font-size: 13px; }
.toolbar-actions { display: flex; gap: 8px; }
.filter-bar { display: grid; grid-template-columns: repeat(4, minmax(220px, 1fr)); gap: 18px; margin-bottom: 16px; }
.filter-item { display: flex; align-items: center; gap: 12px; }
.filter-label { min-width: 78px; font-size: 13px; }
.standard-table :deep(.table-header-cell) { background: #e7f0ff; color: #365078; font-size: 12px; font-weight: 700; }
.sel-full { width: 100%; min-width: 0; }
.drawer-form { padding-right: 8px; }
.drawer-footer { display: flex; justify-content: flex-end; gap: 8px; }
</style>
