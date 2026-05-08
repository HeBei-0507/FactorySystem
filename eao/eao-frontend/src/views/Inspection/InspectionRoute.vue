<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { getEquipmentPage } from '@/api/equipment'
import { getProductionlineList } from '@/api/prodectionLine'
import {
  addInspectionRoute,
  batchDeleteInspectionRoute,
  getInspectionRouteList,
  getInspectionRoutePage,
  updateInspectionRoute
} from '@/api/inspectionRoute'
import {
  addInspectionRouteDevice,
  batchDeleteInspectionRouteDevice,
  getInspectionRouteDevicePage,
  updateInspectionRouteDevice
} from '@/api/inspectionRouteDevice'
import { extractPageData, extractRecordsArray } from '@/util/apiData'

const mapLine = (i) => ({ id: i.id, name: i.lineName || i.name || '', routes: [] })
const now = () => new Date().toISOString().slice(0, 19).replace('T', ' ')

const productionLines = ref([])
const activeLineId = ref('')
const activeRouteId = ref('')
/** 产线节点展开，显示其下点检路线列表 */
const expandedLineIds = ref([])

/** 'route' = 点检路线(InspectionRoute) | 'device' = 路线点检设备(InspectionRouteDevice) */
const viewMode = ref('route')

const routeTableRef = ref()
const deviceTableRef = ref()
const routeTableData = ref([])
const deviceTableData = ref([])
const routeSelectedRows = ref([])
const deviceSelectedRows = ref([])

const routeEditingId = ref('')
const routeCreatingId = ref('')
const deviceEditingId = ref('')
const deviceCreatingId = ref('')

const routeLoading = ref(false)
const deviceLoading = ref(false)
const treeLoading = ref(false)

const routeFilters = reactive({ routeCode: '', routeName: '' })
const routePagination = reactive({ currentPage: 1, pageSize: 10, total: 0 })
const deviceFilters = reactive({ equipmentCode: '', equipmentName: '' })
const devicePagination = reactive({ currentPage: 1, pageSize: 10, total: 0 })

const activeLine = computed(
  () => productionLines.value.find((v) => v.id === activeLineId.value) || null
)
const activeRoute = computed(() => {
  const line = activeLine.value
  if (!line?.routes?.length) return null
  return line.routes.find((r) => String(r.id) === String(activeRouteId.value)) || null
})

const breadcrumbTail = computed(() => {
  if (!activeLine.value) {
    return '未选择生产线'
  }
  if (viewMode.value === 'route') {
    return `${activeLine.value.name} · 点检路线`
  }
  if (activeRoute.value) {
    return `${activeLine.value.name} · ${activeRoute.value.routeName || activeRoute.value.routeCode} · 路线点检设备`
  }
  return activeLine.value.name
})

const mapRouteRow = (i) => ({
  id: i.id,
  productionLineId: i.productionLineId,
  productionLineName: i.productionLineName || '',
  routeCode: i.routeCode || '',
  routeName: i.routeName || '',
  creatorName: i.creatorName || '',
  creatorUsername: i.creatorUsername || '',
  createdAt: i.createdAt || '',
  updatedAt: i.updatedAt || ''
})
const mapDeviceRow = (i) => ({
  id: i.id,
  routeCode: i.routeCode || '',
  routeName: i.routeName || '',
  equipmentCode: i.equipmentCode || '',
  equipmentName: i.equipmentName || '',
  sort: i.sort == null || i.sort === '' ? 0 : Number(i.sort),
  creatorName: i.creatorName || '',
  createdAt: i.createdAt || '',
  updatedAt: i.updatedAt || ''
})
function mapTreeRoute(i) {
  return {
    id: i.id,
    productionLineId: i.productionLineId,
    routeCode: i.routeCode || '',
    routeName: i.routeName || ''
  }
}

function isLineExpanded(line) {
  return expandedLineIds.value.includes(line.id)
}
async function toggleLineExpand(line) {
  if (isLineExpanded(line)) {
    expandedLineIds.value = expandedLineIds.value.filter((id) => id !== line.id)
    return
  }
  if (!line.routes?.length) {
    try {
      const res = await getInspectionRouteList(line.id)
      line.routes = extractRecordsArray(res?.data).map(mapTreeRoute)
    } catch (e) {
      if (e?.elMessageNotified) {
        return
      }
      ElMessage.error('获取点检路线列表失败')
      return
    }
  }
  expandedLineIds.value = [...expandedLineIds.value, line.id]
}
function ensureLineExpanded(line) {
  if (!isLineExpanded(line)) {
    expandedLineIds.value = [...expandedLineIds.value, line.id]
  }
}

function resetTableSelection() {
  routeSelectedRows.value = []
  deviceSelectedRows.value = []
  routeTableRef.value?.clearSelection()
  deviceTableRef.value?.clearSelection()
}

async function refreshRoutesForCurrentLine() {
  const line = activeLine.value
  if (!line) {
    return
  }
  const res = await getInspectionRouteList(line.id)
  line.routes = extractRecordsArray(res?.data).map(mapTreeRoute)
}

async function loadProductionLines() {
  try {
    const res = await getProductionlineList()
    productionLines.value = extractRecordsArray(res?.data).map(mapLine)
    if (productionLines.value.length) {
      await clickLine(productionLines.value[0])
    }
  } catch (e) {
    if (e?.elMessageNotified) return
    ElMessage.error('获取生产线列表失败')
  }
}

function resetEdits() {
  routeEditingId.value = ''
  routeCreatingId.value = ''
  deviceEditingId.value = ''
  deviceCreatingId.value = ''
}

/** 点击生产线：右侧为点检路线(InspectionRoute) 分页 */
async function clickLine(line) {
  activeLineId.value = line.id
  activeRouteId.value = ''
  viewMode.value = 'route'
  resetEdits()
  resetTableSelection()
  routeFilters.routeCode = ''
  routeFilters.routeName = ''
  routePagination.currentPage = 1
  deviceFilters.equipmentCode = ''
  deviceFilters.equipmentName = ''
  devicePagination.currentPage = 1
  treeLoading.value = true
  try {
    const res = await getInspectionRouteList(line.id)
    line.routes = extractRecordsArray(res?.data).map(mapTreeRoute)
    ensureLineExpanded(line)
    await loadRouteTable()
  } catch (e) {
    if (e?.elMessageNotified) {
      return
    }
    ElMessage.error('获取点检路线列表失败')
  } finally {
    treeLoading.value = false
  }
}

/** 点击产线下的路线：右侧为路线点检设备(InspectionRouteDevice) 分页（树不展示设备子节点） */
async function clickRoute(line, route) {
  if (!route?.routeCode) {
    return ElMessage.warning('路线数据无效')
  }
  activeLineId.value = line.id
  activeRouteId.value = route.id
  viewMode.value = 'device'
  resetEdits()
  resetTableSelection()
  deviceFilters.equipmentCode = ''
  deviceFilters.equipmentName = ''
  devicePagination.currentPage = 1
  await loadDeviceTable()
}

async function loadRouteTable() {
  if (!activeLine.value) {
    routeTableData.value = []
    routePagination.total = 0
    return
  }
  routeLoading.value = true
  try {
    const res = await getInspectionRoutePage({
      current: routePagination.currentPage,
      size: routePagination.pageSize,
      productionLineId: activeLineId.value,
      routeCode: routeFilters.routeCode,
      routeName: routeFilters.routeName
    })
    const { records, total } = extractPageData(res)
    routeTableData.value = records.map(mapRouteRow)
    routePagination.total = total
  } catch (e) {
    if (e?.elMessageNotified) return
    ElMessage.error('获取点检路线数据失败')
  } finally {
    routeLoading.value = false
  }
}

async function loadDeviceTable() {
  const rc = activeRoute.value?.routeCode
  if (!rc) {
    deviceTableData.value = []
    devicePagination.total = 0
    return
  }
  deviceLoading.value = true
  try {
    const res = await getInspectionRouteDevicePage({
      current: devicePagination.currentPage,
      size: devicePagination.pageSize,
      routeCode: rc,
      equipmentCode: deviceFilters.equipmentCode,
      equipmentName: deviceFilters.equipmentName
    })
    const { records, total } = extractPageData(res)
    deviceTableData.value = records.map(mapDeviceRow)
    devicePagination.total = total
  } catch (e) {
    if (e?.elMessageNotified) return
    ElMessage.error('获取路线点检设备数据失败')
  } finally {
    deviceLoading.value = false
  }
}

const handleRouteSelectionChange = (rows) => (routeSelectedRows.value = rows)
const handleDeviceSelectionChange = (rows) => (deviceSelectedRows.value = rows)

const handleRoutePageChange = async (p) => {
  routePagination.currentPage = p
  resetTableSelection()
  await loadRouteTable()
}
const handleDevicePageChange = async (p) => {
  devicePagination.currentPage = p
  resetTableSelection()
  await loadDeviceTable()
}
const handleRouteSearch = async () => {
  routePagination.currentPage = 1
  resetTableSelection()
  await loadRouteTable()
}
const handleDeviceSearch = async () => {
  devicePagination.currentPage = 1
  resetTableSelection()
  await loadDeviceTable()
}

const isEditingRoute = (row) => routeEditingId.value === row.id
const isEditingDevice = (row) => deviceEditingId.value === row.id

const deviceRowForPicker = ref(null)
const equipmentPickerVisible = ref(false)
const equipmentPickerLoading = ref(false)
const equipmentPickerCode = ref('')
const equipmentPickerName = ref('')
const equipmentPickerRows = ref([])
const equipmentPickerPagination = reactive({ currentPage: 1, pageSize: 10, total: 0 })

function onEquipmentPickerClosed() {
  deviceRowForPicker.value = null
  equipmentPickerRows.value = []
  equipmentPickerCode.value = ''
  equipmentPickerName.value = ''
}
function openEquipmentPicker(row) {
  if (!isEditingDevice(row)) {
    return
  }
  deviceRowForPicker.value = row
  equipmentPickerCode.value = String(row.equipmentCode || '').trim()
  equipmentPickerName.value = String(row.equipmentName || '').trim()
  equipmentPickerPagination.currentPage = 1
  equipmentPickerVisible.value = true
  loadEquipmentPickerPage()
}
function buildEquipmentPageParams() {
  const params = {
    current: equipmentPickerPagination.currentPage,
    size: equipmentPickerPagination.pageSize
  }
  const code = String(equipmentPickerCode.value || '').trim()
  const name = String(equipmentPickerName.value || '').trim()
  if (code) {
    params.equipmentCode = code
  }
  if (name) {
    params.equipmentName = name
  }
  return params
}
async function loadEquipmentPickerPage() {
  equipmentPickerLoading.value = true
  try {
    const res = await getEquipmentPage(buildEquipmentPageParams())
    const { records, total } = extractPageData(res)
    equipmentPickerRows.value = Array.isArray(records) ? records : []
    equipmentPickerPagination.total = total
  } catch (e) {
    if (e?.elMessageNotified) {
      return
    }
    ElMessage.error('设备查询失败')
  } finally {
    equipmentPickerLoading.value = false
  }
}
function handleEquipmentPickerQuery() {
  equipmentPickerPagination.currentPage = 1
  loadEquipmentPickerPage()
}
function handleEquipmentPickerPageChange(p) {
  equipmentPickerPagination.currentPage = p
  loadEquipmentPickerPage()
}
function applyPickedEquipment(eq) {
  if (!eq || !deviceRowForPicker.value) {
    return
  }
  const r = deviceRowForPicker.value
  r.equipmentCode = String(eq.equipmentCode ?? '').trim()
  r.equipmentName = String(eq.equipmentName ?? '').trim()
  equipmentPickerVisible.value = false
  onEquipmentPickerClosed()
}

function createEmptyRoute() {
  if (!activeLine.value) {
    return ElMessage.warning('请先在左侧选择生产线')
  }
  if (routeCreatingId.value) {
    return ElMessage.warning('请先完成当前新增行')
  }
  routeTableData.value.unshift({
    id: `draft-${Date.now()}`,
    productionLineId: activeLineId.value,
    productionLineName: activeLine.value?.name || '',
    routeCode: '',
    routeName: '',
    creatorName: '',
    createdAt: now(),
    updatedAt: now(),
    isNew: true
  })
  routeCreatingId.value = routeTableData.value[0].id
  routeEditingId.value = routeTableData.value[0].id
  resetTableSelection()
}
function startEditRoute() {
  if (routeSelectedRows.value.length !== 1) {
    return ElMessage.warning('请选择一条需要修改的点检路线')
  }
  const row = routeSelectedRows.value[0]
  routeEditingId.value = row.id
  routeCreatingId.value = row.isNew ? row.id : ''
}
function validRoute(r) {
  if (!String(r.routeCode || '').trim()) {
    return (ElMessage.warning('路线编号不能为空'), false)
  }
  if (!String(r.routeName || '').trim()) {
    return (ElMessage.warning('路线名称不能为空'), false)
  }
  return true
}
function routePayload(r, withId) {
  const o = {
    productionLineId: r.productionLineId ?? activeLineId.value,
    routeCode: r.routeCode,
    routeName: r.routeName
  }
  if (withId) o.id = r.id
  return o
}
async function submitCreateRoute() {
  const row = routeTableData.value.find((v) => v.id === routeCreatingId.value)
  if (!row) {
    return ElMessage.warning('当前没有待新增的点检路线')
  }
  if (!validRoute(row)) {
    return
  }
  try {
    await addInspectionRoute(routePayload(row, false))
    routeCreatingId.value = ''
    routeEditingId.value = ''
    ElMessage.success('新增成功')
    await refreshRoutesForCurrentLine()
    const line = activeLine.value
    await loadRouteTable()
  } catch (e) {
    if (e?.elMessageNotified) {
      return
    }
  }
}
async function submitEditRoute() {
  if (routeSelectedRows.value.length !== 1) {
    return ElMessage.warning('请选择一条需要修改的点检路线')
  }
  const row = routeSelectedRows.value[0]
  if (row.isNew) {
    return ElMessage.warning('请使用「新增」保存新路线')
  }
  if (!validRoute(row)) {
    return
  }
  try {
    await updateInspectionRoute({ id: row.id, routeCode: row.routeCode, routeName: row.routeName })
    routeEditingId.value = ''
    ElMessage.success('修改成功')
    await refreshRoutesForCurrentLine()
    await loadRouteTable()
  } catch (e) {
    if (e?.elMessageNotified) {
      return
    }
  }
}
async function removeRoutes() {
  if (!routeSelectedRows.value.length) {
    return ElMessage.warning('请至少选择一条点检路线')
  }
  const draftIds = new Set(routeSelectedRows.value.filter((v) => v.isNew).map((v) => v.id))
  routeTableData.value = routeTableData.value.filter((v) => !draftIds.has(v.id))
  const ids = routeSelectedRows.value.filter((v) => !v.isNew).map((v) => v.id)
  if (!ids.length) {
    resetTableSelection()
    routeCreatingId.value = ''
    routeEditingId.value = ''
    return ElMessage.success('已删除未保存的新增行')
  }
  const prevRid = activeRouteId.value
  const wasDevice = viewMode.value === 'device'
  try {
    await batchDeleteInspectionRoute(ids)
    routeCreatingId.value = ''
    routeEditingId.value = ''
    resetTableSelection()
    ElMessage.success('删除成功')
    await refreshRoutesForCurrentLine()
    const line = activeLine.value
    if (!line?.routes?.length) {
      activeRouteId.value = ''
      viewMode.value = 'route'
      routeTableData.value = []
      routePagination.total = 0
      deviceTableData.value = []
      devicePagination.total = 0
      await loadRouteTable()
      return
    }
    const still = line.routes.find((r) => String(r.id) === String(prevRid))
    if (!still) {
      if (wasDevice) {
        const r0 = line.routes[0]
        await clickRoute(line, r0)
      } else {
        await loadRouteTable()
      }
    } else {
      if (wasDevice) {
        await loadDeviceTable()
      } else {
        await loadRouteTable()
      }
    }
  } catch (e) {
    if (e?.elMessageNotified) {
      return
    }
  }
}

function createEmptyDevice() {
  if (!activeRoute.value?.routeCode) {
    return ElMessage.warning('请先在左侧选择点检路线并进入设备层')
  }
  if (viewMode.value !== 'device') {
    return ElMessage.warning('请在左侧点击某条点检路线以管理路线点检设备')
  }
  if (deviceCreatingId.value) {
    return ElMessage.warning('请先完成当前新增行')
  }
  deviceTableData.value.unshift({
    id: `draft-${Date.now()}`,
    routeCode: activeRoute.value.routeCode,
    routeName: activeRoute.value.routeName,
    equipmentCode: '',
    equipmentName: '',
    sort: 0,
    createdAt: now(),
    updatedAt: now(),
    isNew: true
  })
  deviceCreatingId.value = deviceTableData.value[0].id
  deviceEditingId.value = deviceTableData.value[0].id
  resetTableSelection()
}
function startEditDevice() {
  if (deviceSelectedRows.value.length !== 1) {
    return ElMessage.warning('请选择一条需要修改的路线点检设备')
  }
  const row = deviceSelectedRows.value[0]
  deviceEditingId.value = row.id
  deviceCreatingId.value = row.isNew ? row.id : ''
}
function validDevice(r) {
  if (!String(r.routeCode || '').trim()) {
    return (ElMessage.warning('路线编号不能为空'), false)
  }
  if (!String(r.routeName || '').trim()) {
    return (ElMessage.warning('路线名称不能为空'), false)
  }
  if (!String(r.equipmentCode || '').trim()) {
    return (ElMessage.warning('设备编码不能为空'), false)
  }
  if (!String(r.equipmentName || '').trim()) {
    return (ElMessage.warning('设备名称不能为空'), false)
  }
  if (Number.isNaN(Number(r.sort))) {
    return (ElMessage.warning('排序必须为数字'), false)
  }
  return true
}
function devicePayload(r, withId) {
  const o = {
    routeCode: r.routeCode,
    routeName: r.routeName,
    equipmentCode: r.equipmentCode,
    equipmentName: r.equipmentName,
    sort: Number(r.sort) || 0
  }
  if (withId) o.id = r.id
  return o
}
async function submitCreateDevice() {
  const row = deviceTableData.value.find((v) => v.id === deviceCreatingId.value)
  if (!row) {
    return ElMessage.warning('当前没有待新增的路线点检设备')
  }
  if (!validDevice(row)) {
    return
  }
  try {
    await addInspectionRouteDevice(devicePayload(row, false))
    deviceCreatingId.value = ''
    deviceEditingId.value = ''
    ElMessage.success('新增成功')
    await loadDeviceTable()
  } catch (e) {
    if (e?.elMessageNotified) {
      return
    }
  }
}
async function submitEditDevice() {
  if (deviceSelectedRows.value.length !== 1) {
    return ElMessage.warning('请选择一条需要修改的路线点检设备')
  }
  const row = deviceSelectedRows.value[0]
  if (row.isNew) {
    return ElMessage.warning('请使用「新增」保存新设备')
  }
  if (!validDevice(row)) {
    return
  }
  try {
    await updateInspectionRouteDevice(devicePayload(row, true))
    deviceEditingId.value = ''
    ElMessage.success('修改成功')
    await loadDeviceTable()
  } catch (e) {
    if (e?.elMessageNotified) {
      return
    }
  }
}
async function removeDevices() {
  if (!deviceSelectedRows.value.length) {
    return ElMessage.warning('请至少选择一条路线点检设备')
  }
  const draftIds = new Set(deviceSelectedRows.value.filter((v) => v.isNew).map((v) => v.id))
  deviceTableData.value = deviceTableData.value.filter((v) => !draftIds.has(v.id))
  const ids = deviceSelectedRows.value.filter((v) => !v.isNew).map((v) => v.id)
  if (!ids.length) {
    resetTableSelection()
    deviceCreatingId.value = ''
    deviceEditingId.value = ''
    return ElMessage.success('已删除未保存的新增行')
  }
  try {
    await batchDeleteInspectionRouteDevice(ids)
    deviceCreatingId.value = ''
    deviceEditingId.value = ''
    resetTableSelection()
    ElMessage.success('删除成功')
    await loadDeviceTable()
  } catch (e) {
    if (e?.elMessageNotified) {
      return
    }
  }
}

loadProductionLines()
</script>

<template>
  <div class="inspection-route-page">
    <div class="page-breadcrumb">点检路线管理 &gt; {{ breadcrumbTail }}</div>
    <div class="page-content">
      <aside class="tree-panel" v-loading="treeLoading">
        <div class="tree-panel-header">点检路线导航</div>
        <div class="tree-content">
          <div v-if="!productionLines.length" class="menu-empty">暂无生产线数据</div>
          <template v-else>
            <div v-for="line in productionLines" :key="line.id" class="line-block">
              <div class="line-head">
                <span class="line-arrow" @click.stop="toggleLineExpand(line)">
                  {{ isLineExpanded(line) ? '⌄' : '›' }}
                </span>
                <div
                  :class="['line-title', { active: String(line.id) === String(activeLineId) }]"
                  @click="clickLine(line)"
                >
                  {{ line.name }}
                </div>
              </div>
              <div v-show="isLineExpanded(line)" class="unit-list">
                <div
                  v-for="route in line.routes"
                  :key="route.id"
                  :class="[
                    'unit-item',
                    {
                      active: viewMode === 'device' && String(activeRouteId) === String(route.id)
                    }
                  ]"
                >
                  <div class="unit-row" @click="clickRoute(line, route)">
                    <span>{{ route.routeName || route.routeCode }}</span>
                  </div>
                </div>
              </div>
            </div>
          </template>
        </div>
      </aside>

      <section v-show="viewMode === 'route'" class="table-panel">
        <div class="filter-bar">
          <div class="filter-item">
            <span class="filter-label">路线编号</span>
            <el-input
              v-model="routeFilters.routeCode"
              clearable
              placeholder="支持模糊查询"
              @change="handleRouteSearch"
            />
          </div>
          <div class="filter-item">
            <span class="filter-label">路线名称</span>
            <el-input
              v-model="routeFilters.routeName"
              clearable
              placeholder="支持模糊查询"
              @change="handleRouteSearch"
            />
          </div>
        </div>
        <div class="toolbar-row">
          <el-button
            class="plus-btn"
            type="primary"
            :icon="Plus"
            square
            :disabled="!activeLine"
            @click="createEmptyRoute"
          />
          <div class="toolbar-right">
            <el-pagination
              small
              background
              layout="prev, pager, next"
              :current-page="routePagination.currentPage"
              :page-size="routePagination.pageSize"
              :total="routePagination.total"
              @current-change="handleRoutePageChange"
            />
            <div class="toolbar-actions">
              <el-button type="primary" :disabled="!activeLine" @click="submitCreateRoute">
                新增
              </el-button>
              <el-button
                type="primary"
                plain
                :disabled="!activeLine"
                @click="routeEditingId ? submitEditRoute() : startEditRoute()"
              >
                修改
              </el-button>
              <el-button type="danger" plain :disabled="!activeLine" @click="removeRoutes">
                删除
              </el-button>
            </div>
          </div>
        </div>
        <el-table
          ref="routeTableRef"
          v-loading="routeLoading"
          :data="routeTableData"
          border
          class="device-table"
          header-cell-class-name="table-header-cell"
          @selection-change="handleRouteSelectionChange"
        >
          <el-table-column type="selection" width="48" align="center" fixed />
          <el-table-column label="路线编号" min-width="130">
            <template #default="{ row }">
              <el-input v-if="isEditingRoute(row)" v-model="row.routeCode" size="small" />
              <span v-else>{{ row.routeCode }}</span>
            </template>
          </el-table-column>
          <el-table-column label="路线名称" min-width="160">
            <template #default="{ row }">
              <el-input v-if="isEditingRoute(row)" v-model="row.routeName" size="small" />
              <span v-else>{{ row.routeName }}</span>
            </template>
          </el-table-column>
          <el-table-column label="生产线" min-width="140" show-overflow-tooltip>
            <template #default="{ row }">
              <span>{{ row.productionLineName || activeLine?.name }}</span>
            </template>
          </el-table-column>
          <el-table-column label="创建人" min-width="100" prop="creatorName" />
          <el-table-column label="创建时间" min-width="140" prop="createdAt" />
          <el-table-column label="更新时间" min-width="140" prop="updatedAt" />
        </el-table>
      </section>

      <section v-show="viewMode === 'device'" class="table-panel">
        <div class="filter-bar">
          <div class="filter-item">
            <span class="filter-label">设备编码</span>
            <el-input
              v-model="deviceFilters.equipmentCode"
              clearable
              placeholder="支持模糊查询"
              @change="handleDeviceSearch"
            />
          </div>
          <div class="filter-item">
            <span class="filter-label">设备名称</span>
            <el-input
              v-model="deviceFilters.equipmentName"
              clearable
              placeholder="支持模糊查询"
              @change="handleDeviceSearch"
            />
          </div>
        </div>
        <div class="toolbar-row">
          <el-button
            class="plus-btn"
            type="primary"
            :icon="Plus"
            square
            :disabled="!activeRoute"
            @click="createEmptyDevice"
          />
          <div class="toolbar-right">
            <el-pagination
              small
              background
              layout="prev, pager, next"
              :current-page="devicePagination.currentPage"
              :page-size="devicePagination.pageSize"
              :total="devicePagination.total"
              @current-change="handleDevicePageChange"
            />
            <div class="toolbar-actions">
              <el-button type="primary" :disabled="!activeRoute" @click="submitCreateDevice">
                新增
              </el-button>
              <el-button
                type="primary"
                plain
                :disabled="!activeRoute"
                @click="deviceEditingId ? submitEditDevice() : startEditDevice()"
              >
                修改
              </el-button>
              <el-button type="danger" plain :disabled="!activeRoute" @click="removeDevices">
                删除
              </el-button>
            </div>
          </div>
        </div>
        <el-table
          ref="deviceTableRef"
          v-loading="deviceLoading"
          :data="deviceTableData"
          border
          class="device-table"
          header-cell-class-name="table-header-cell"
          @selection-change="handleDeviceSelectionChange"
        >
          <el-table-column type="selection" width="48" align="center" fixed />
          <el-table-column label="路线编号" min-width="120">
            <template #default="{ row }">
              <el-input v-if="isEditingDevice(row)" v-model="row.routeCode" size="small" disabled />
              <span v-else>{{ row.routeCode }}</span>
            </template>
          </el-table-column>
          <el-table-column label="路线名称" min-width="140" show-overflow-tooltip>
            <template #default="{ row }">
              <el-input v-if="isEditingDevice(row)" v-model="row.routeName" size="small" disabled />
              <span v-else>{{ row.routeName }}</span>
            </template>
          </el-table-column>
          <el-table-column label="设备编码" min-width="148">
            <template #default="{ row }">
              <el-input
                v-if="isEditingDevice(row)"
                v-model="row.equipmentCode"
                size="small"
                class="eq-code-input"
                clearable
              >
                <template #suffix>
                  <el-icon class="eq-search-ico" @click.stop="openEquipmentPicker(row)">
                    <Search />
                  </el-icon>
                </template>
              </el-input>
              <span v-else>{{ row.equipmentCode }}</span>
            </template>
          </el-table-column>
          <el-table-column label="设备名称" min-width="140">
            <template #default="{ row }">
              <el-input v-if="isEditingDevice(row)" v-model="row.equipmentName" size="small" />
              <span v-else>{{ row.equipmentName }}</span>
            </template>
          </el-table-column>
          <el-table-column label="排序" min-width="90" align="right">
            <template #default="{ row }">
              <el-input-number
                v-if="isEditingDevice(row)"
                v-model="row.sort"
                :min="0"
                :controls="false"
                class="sort-input"
                size="small"
              />
              <span v-else>{{ row.sort }}</span>
            </template>
          </el-table-column>
          <el-table-column label="创建人" min-width="100" prop="creatorName" />
          <el-table-column label="创建时间" min-width="140" prop="createdAt" />
          <el-table-column label="更新时间" min-width="140" prop="updatedAt" />
        </el-table>
      </section>
    </div>

    <el-dialog
      v-model="equipmentPickerVisible"
      title="选择设备"
      width="860px"
      destroy-on-close
      @closed="onEquipmentPickerClosed"
    >
      <div class="eq-picker-toolbar">
        <div class="eq-picker-filters">
          <div class="eq-picker-item">
            <span class="filter-label">设备编码</span>
            <el-input
              v-model="equipmentPickerCode"
              clearable
              placeholder="单独按设备编码搜索"
              @keyup.enter="handleEquipmentPickerQuery"
            />
          </div>
          <div class="eq-picker-item">
            <span class="filter-label">设备名称</span>
            <el-input
              v-model="equipmentPickerName"
              clearable
              placeholder="单独按设备名称搜索"
              @keyup.enter="handleEquipmentPickerQuery"
            />
          </div>
        </div>
        <el-button type="primary" @click="handleEquipmentPickerQuery">查询</el-button>
      </div>
      <el-table
        v-loading="equipmentPickerLoading"
        :data="equipmentPickerRows"
        border
        height="320"
        class="eq-picker-table"
      >
        <el-table-column prop="id" label="ID" width="72" align="right" />
        <el-table-column
          prop="equipmentCode"
          label="设备编码"
          min-width="120"
          show-overflow-tooltip
        />
        <el-table-column
          prop="equipmentName"
          label="设备名称"
          min-width="140"
          show-overflow-tooltip
        />
        <el-table-column prop="unitName" label="设备单元" min-width="120" show-overflow-tooltip />
        <el-table-column label="确认" width="88" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click.stop="applyPickedEquipment(row)">确认</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="eq-picker-page">
        <el-pagination
          small
          background
          layout="total, prev, pager, next"
          :current-page="equipmentPickerPagination.currentPage"
          :page-size="equipmentPickerPagination.pageSize"
          :total="equipmentPickerPagination.total"
          @current-change="handleEquipmentPickerPageChange"
        />
      </div>
      <template #footer>
        <el-button @click="equipmentPickerVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.inspection-route-page {
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
  padding: 10px 12px;
}
.line-block + .line-block {
  margin-top: 12px;
}
.line-head {
  display: flex;
  align-items: center;
  gap: 2px;
  margin-bottom: 2px;
}
.line-arrow {
  display: inline-grid;
  flex: 0 0 16px;
  place-items: center;
  user-select: none;
  color: #909399;
  font-size: 12px;
  cursor: pointer;
}
.line-arrow:hover {
  color: #409eff;
}
.line-title {
  flex: 1;
  min-width: 0;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
}
.line-title.active {
  color: #2f76e8;
}
.unit-list {
  margin-top: 2px;
  margin-left: 4px;
  padding-left: 12px;
  border-left: 2px solid #e3ebf5;
}
.unit-item {
  padding: 5px 8px 5px 6px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  line-height: 1.4;
  color: #5a6a85;
  font-weight: 600;
}
.unit-item:hover {
  background: #f2f7ff;
}
.unit-item.active {
  background: #eaf2ff;
  color: #2f76e8;
}
.unit-row {
  display: flex;
  gap: 6px;
}
.menu-empty {
  padding: 0 0 4px 2px;
  color: #909399;
  font-size: 13px;
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
  min-width: 66px;
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
.toolbar-actions {
  display: flex;
  gap: 8px;
}
.plus-btn {
  width: 28px;
  height: 28px;
  padding: 0;
}
.device-table :deep(.table-header-cell) {
  background: #e7f0ff;
  color: #365078;
  font-size: 12px;
  font-weight: 700;
}
.sort-input {
  width: 100%;
}
.eq-code-input {
  width: 100%;
}
.eq-search-ico {
  cursor: pointer;
  color: var(--el-color-primary);
  font-size: 16px;
}
.eq-search-ico:hover {
  color: var(--el-color-primary-light-3);
}
.eq-picker-toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-end;
  gap: 12px;
  margin-bottom: 12px;
}
.eq-picker-filters {
  display: flex;
  flex: 1;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px 16px;
  min-width: 0;
}
.eq-picker-item {
  display: flex;
  flex: 1;
  min-width: 200px;
  max-width: 100%;
  align-items: center;
  gap: 8px;
}
.eq-picker-item .filter-label {
  flex-shrink: 0;
  min-width: 64px;
  font-size: 13px;
  color: #606266;
}
.eq-picker-item :deep(.el-input) {
  flex: 1;
  min-width: 0;
}
.eq-picker-page {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}
.eq-picker-table :deep(.table-header-cell) {
  background: #e7f0ff;
  color: #365078;
  font-size: 12px;
  font-weight: 700;
}
</style>
