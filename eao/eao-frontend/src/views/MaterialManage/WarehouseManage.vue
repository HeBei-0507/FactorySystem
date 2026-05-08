<script setup>
import { onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getProductionlineList } from '@/api/prodectionLine'
import { getDeviceUtilList } from '@/api/deviceUnit'
import {
  addWarehouseLocation,
  batchDeleteWarehouseLocation,
  getWarehouseLocationPage,
  updateWarehouseLocation
} from '@/api/warehouseLocation'
import { useUserStore } from '@/stores/user'
import { extractPageData, extractRecordsArray } from '@/util/apiData'

const userStore = useUserStore()
const lines = ref([])
const lineId = ref('')
const lineName = ref('未选择生产线')
const activeUnitNavId = ref('')
const expandIds = ref([])
const rows = ref([])
const sels = ref([])
const tableRef = ref()
const loading = ref(false)
const editId = ref('')
const newId = ref('')
let timer = null
const f = reactive({ areaCode: '', locationCode: '', keeperUsername: '', keeperRealName: '' })
const pg = reactive({ currentPage: 1, pageSize: 10, total: 0 })

const profileName = () => String(userStore.profile?.realName || '').trim()
const profileUser = () => String(userStore.profile?.username || '').trim()
const clearTimer = () => {
  if (timer) {
    clearTimeout(timer)
    timer = null
  }
}
const resetSelection = () => {
  sels.value = []
  tableRef.value && tableRef.value.clearSelection()
}
const isExpanded = (id) => expandIds.value.includes(id)
const ensureExpanded = (id) => {
  if (id && !isExpanded(id)) expandIds.value.push(id)
}
const queueSearch = () => {
  clearTimer()
  timer = setTimeout(() => handleSearch(), 300)
}
const lineRow = (i) => ({ id: i.id, name: i.lineName, units: [] })
const unitRow = (i) => ({ id: i.id, unitCode: i.unitCode || '', unitName: i.unitName || '' })
const tableRow = (i) => ({
  id: i.id,
  productionLineId: i.productionLineId,
  productionLineName: i.productionLineName || '',
  areaCode: i.areaCode || '',
  locationCode: i.locationCode || '',
  keeperUsername: i.keeperUsername || '',
  keeperRealName: i.keeperRealName || '',
  createdAt: i.createdAt || '',
  isNew: false
})

async function loadLines() {
  try {
    const res = await getProductionlineList()
    lines.value = extractRecordsArray(res?.data)
      .map(lineRow)
      .filter((i) => i.id)
    if (lines.value.length && !lineId.value) {
      lineId.value = lines.value[0].id
      lineName.value = lines.value[0].name
      ensureExpanded(lineId.value)
      await loadUnits(lineId.value)
    }
  } catch (e) {
    if (!e?.elMessageNotified) ElMessage.error('获取生产线列表失败')
  }
}

async function loadUnits(id) {
  const line = lines.value.find((i) => i.id === id)
  if (!line) return
  try {
    const res = await getDeviceUtilList(id)
    line.units = extractRecordsArray(res?.data).map(unitRow)
  } catch (e) {
    line.units = []
    if (!e?.elMessageNotified) ElMessage.error('获取设备单元失败')
  }
}

async function loadPage() {
  if (!lineId.value) {
    rows.value = []
    pg.total = 0
    return
  }
  loading.value = true
  try {
    const res = await getWarehouseLocationPage({
      current: pg.currentPage,
      size: pg.pageSize,
      productionLineId: lineId.value,
      areaCode: f.areaCode,
      locationCode: f.locationCode,
      keeperUsername: f.keeperUsername,
      keeperRealName: f.keeperRealName
    })
    const d = extractPageData(res)
    rows.value = d.records.map(tableRow)
    pg.total = d.total
  } catch (e) {
    if (!e?.elMessageNotified) ElMessage.error('获取库区库位失败')
  } finally {
    loading.value = false
  }
}

function clickLine(line) {
  lineId.value = line.id
  lineName.value = line.name
  activeUnitNavId.value = ''
  pg.currentPage = 1
  editId.value = ''
  newId.value = ''
  resetSelection()
  ensureExpanded(line.id)
  loadUnits(line.id)
  loadPage()
}
function clickUnit(line, unit) {
  lineId.value = line.id
  lineName.value = line.name
  activeUnitNavId.value = unit.id
  ensureExpanded(line.id)
}
function toggle(line) {
  if (isExpanded(line.id)) {
    expandIds.value = expandIds.value.filter((i) => i !== line.id)
    return
  }
  ensureExpanded(line.id)
  if (!line.units.length) loadUnits(line.id)
}
function handleSearch() {
  pg.currentPage = 1
  resetSelection()
  loadPage()
}
function handleReset() {
  clearTimer()
  Object.assign(f, { areaCode: '', locationCode: '', keeperUsername: '', keeperRealName: '' })
  pg.currentPage = 1
  resetSelection()
  loadPage()
}
function handleSelectionChange(v) {
  sels.value = v
}
function handleCurrentChange(v) {
  pg.currentPage = v
  resetSelection()
  loadPage()
}
function handleSizeChange(v) {
  pg.pageSize = v
  pg.currentPage = 1
  resetSelection()
  loadPage()
}
function isEditing(row) {
  return editId.value === row.id
}
function createRow() {
  if (!lineId.value) return ElMessage.warning('请先选择生产线')
  if (newId.value) return ElMessage.warning('请先完成当前新增行')
  rows.value.unshift({
    id: `draft-${Date.now()}`,
    productionLineId: lineId.value,
    productionLineName: lineName.value,
    areaCode: '',
    locationCode: '',
    keeperUsername: profileUser(),
    keeperRealName: profileName() || profileUser(),
    createdAt: new Date().toISOString().slice(0, 19).replace('T', ' '),
    isNew: true
  })
  newId.value = rows.value[0].id
  editId.value = rows.value[0].id
  resetSelection()
}
function startEdit() {
  if (sels.value.length !== 1) return ElMessage.warning('请选择一条数据')
  if (sels.value[0].isNew) return ElMessage.warning('请先保存新增行')
  editId.value = sels.value[0].id
  newId.value = ''
}
function valid(r) {
  if (!String(r.areaCode || '').trim()) return (ElMessage.warning('库区不能为空'), false)
  if (!String(r.locationCode || '').trim()) return (ElMessage.warning('库位不能为空'), false)
  if (!String(r.keeperUsername || '').trim()) return (ElMessage.warning('库管员不能为空'), false)
  if (!String(r.keeperRealName || '').trim())
    return (ElMessage.warning('库管员姓名不能为空'), false)
  return true
}
async function saveAdd() {
  const r = rows.value.find((i) => i.id === newId.value)
  if (!r) return ElMessage.warning('当前没有待新增行')
  if (!valid(r)) return
  try {
    await addWarehouseLocation({
      productionLineId: r.productionLineId,
      areaCode: r.areaCode,
      locationCode: r.locationCode,
      keeperUsername: r.keeperUsername,
      keeperRealName: r.keeperRealName
    })
    ElMessage.success('新增成功')
    newId.value = ''
    editId.value = ''
    loadPage()
  } catch (e) {
    if (!e?.elMessageNotified) ElMessage.error('新增失败')
  }
}
async function saveEdit() {
  if (sels.value.length !== 1) return ElMessage.warning('请选择一条数据')
  const r = sels.value[0]
  if (!valid(r)) return
  try {
    await updateWarehouseLocation({
      id: r.id,
      areaCode: r.areaCode,
      locationCode: r.locationCode,
      keeperUsername: r.keeperUsername,
      keeperRealName: r.keeperRealName
    })
    ElMessage.success('修改成功')
    editId.value = ''
    loadPage()
  } catch (e) {
    if (!e?.elMessageNotified) ElMessage.error('修改失败')
  }
}
async function removeRows() {
  if (!sels.value.length) return ElMessage.warning('请至少选择一条数据')
  const drafts = new Set(sels.value.filter((i) => i.isNew).map((i) => i.id))
  if (drafts.size) rows.value = rows.value.filter((i) => !drafts.has(i.id))
  const ids = sels.value.filter((i) => !i.isNew).map((i) => i.id)
  if (!ids.length) {
    newId.value = ''
    editId.value = ''
    resetSelection()
    return ElMessage.success('已删除未保存行')
  }
  try {
    await ElMessageBox.confirm('确认删除选中数据吗？', '提示', { type: 'warning' })
    await batchDeleteWarehouseLocation(ids)
    ElMessage.success('删除成功')
    newId.value = ''
    editId.value = ''
    resetSelection()
    loadPage()
  } catch (e) {
    if (e === 'cancel') return
    if (!e?.elMessageNotified) ElMessage.error('删除失败')
  }
}

onMounted(async () => {
  await loadLines()
  await loadPage()
})
onBeforeUnmount(clearTimer)
</script>

<template>
  <div class="p">
    <div class="b">库区库位管理 &gt; {{ lineName }}</div>
    <div class="c">
      <aside class="l">
        <div class="lh">组织结构树</div>
        <div class="lt">
          <div v-for="line in lines" :key="line.id">
            <div :class="['li', { a: line.id === lineId }]" @click="clickLine(line)">
              <span>{{ line.name }}</span>
              <span @click.stop="toggle(line)">{{ isExpanded(line.id) ? '▾' : '▸' }}</span>
            </div>
            <div v-if="isExpanded(line.id)" class="ul">
              <div
                v-for="u in line.units"
                :key="u.id"
                :class="['ui', { a: u.id === activeUnitNavId }]"
                @click="clickUnit(line, u)"
              >
                {{ u.unitCode }} {{ u.unitName }}
              </div>
              <div v-if="!line.units.length" class="e">当前生产线下暂无设备单元</div>
            </div>
          </div>
        </div>
      </aside>
      <section class="r">
        <div class="f">
          <div class="i">
            <span>库区</span>
            <el-input v-model="f.areaCode" clearable placeholder="库区" @input="queueSearch" />
          </div>
          <div class="i">
            <span>库位</span>
            <el-input v-model="f.locationCode" clearable placeholder="库位" @input="queueSearch" />
          </div>
          <div class="i">
            <span>库管员</span>
            <el-input
              v-model="f.keeperUsername"
              clearable
              placeholder="username"
              @input="queueSearch"
            />
          </div>
          <div class="i">
            <span>姓名</span>
            <el-input
              v-model="f.keeperRealName"
              clearable
              placeholder="real_name"
              @input="queueSearch"
            />
          </div>
          <div class="fa">
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </div>
        </div>
        <div class="t">
          <el-button class="plus-btn" type="primary" :icon="Plus" @click="createRow" />
          <div class="btns">
            <el-button type="primary" @click="saveAdd">新增</el-button>
            <el-button type="primary" plain @click="editId ? saveEdit() : startEdit()">
              修改
            </el-button>
            <el-button type="danger" plain @click="removeRows">删除</el-button>
          </div>
        </div>
        <el-table
          ref="tableRef"
          v-loading="loading"
          :data="rows"
          border
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="46" />
          <el-table-column label="归属生产线" prop="productionLineName" min-width="140" />
          <el-table-column label="库区" min-width="120">
            <template #default="{ row }">
              <el-input v-if="isEditing(row)" v-model="row.areaCode" size="small" />
              <span v-else>{{ row.areaCode }}</span>
            </template>
          </el-table-column>
          <el-table-column label="库位" min-width="120">
            <template #default="{ row }">
              <el-input v-if="isEditing(row)" v-model="row.locationCode" size="small" />
              <span v-else>{{ row.locationCode }}</span>
            </template>
          </el-table-column>
          <el-table-column label="库管员" min-width="120">
            <template #default="{ row }">
              <el-input v-if="isEditing(row)" v-model="row.keeperUsername" size="small" />
              <span v-else>{{ row.keeperUsername }}</span>
            </template>
          </el-table-column>
          <el-table-column label="库管员姓名" min-width="140">
            <template #default="{ row }">
              <el-input v-if="isEditing(row)" v-model="row.keeperRealName" size="small" />
              <span v-else>{{ row.keeperRealName }}</span>
            </template>
          </el-table-column>
          <el-table-column label="创建日期" prop="createdAt" min-width="160" />
        </el-table>
        <div class="pg">
          <div></div>
          <el-pagination
            background
            layout="total, sizes, prev, pager, next"
            :current-page="pg.currentPage"
            :page-size="pg.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="pg.total"
            @current-change="handleCurrentChange"
            @size-change="handleSizeChange"
          />
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
.p {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 12px;
  background: #f5f8fc;
  min-height: 100%;
}
.b {
  font-size: 14px;
  color: #44566c;
  font-weight: 600;
}
.c {
  display: flex;
  gap: 12px;
  min-height: 680px;
}
.l {
  width: 260px;
  background: #fff;
  border: 1px solid #d9e2ef;
}
.lh {
  padding: 12px;
  font-size: 14px;
  font-weight: 600;
  border-bottom: 1px solid #e5edf6;
}
.lt {
  padding: 10px;
}
.li,
.ui {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 10px;
  border-radius: 4px;
  cursor: pointer;
}
.ui {
  justify-content: flex-start;
}
.a,
.li.a,
.ui.a {
  background: #eaf3ff;
  color: #1d6fd8;
}
.ul {
  margin-left: 18px;
}
.e {
  padding: 8px 10px;
  color: #8a98ab;
  font-size: 12px;
}
.r {
  flex: 1;
  background: #fff;
  border: 1px solid #d9e2ef;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.f {
  display: grid;
  grid-template-columns: repeat(5, minmax(160px, 1fr));
  gap: 12px;
}
.i {
  display: flex;
  align-items: center;
  gap: 10px;
}
.i span {
  min-width: 52px;
  font-size: 13px;
  color: #4b5d73;
}
.t,
.pg {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 30px;
}
.btns,
.fa {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  align-items: center;
}
.plus-btn {
  width: 32px;
  height: 32px;
  padding: 0;
}
</style>
