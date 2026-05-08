<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Document } from '@element-plus/icons-vue'
import { getProductionlineList } from '@/api/prodectionLine'
import { getDeviceUtilList } from '@/api/deviceUnit'
import { getEquipmentListByDeviceUnitCode } from '@/api/equipment'
import { getEquipmentPartListByDeviceCode } from '@/api/equipmentPart'
import {
  addMaintenanceStandard,
  deleteMaintenanceStandard,
  getMaintenanceMaterialCandidatePage,
  getMaintenanceStandardById,
  getMaintenanceStandardPage,
  updateMaintenanceStandard
} from '@/api/maintenanceStandard'
import { extractPageData, extractRecordsArray } from '@/util/apiData'
import MaintenanceStandardDrawer from './components/MaintenanceStandardDrawer.vue'
import MaintenanceMaterialPicker from './components/MaintenanceMaterialPicker.vue'

const lines = ref([]),
  lineId = ref(''),
  unitId = ref(''),
  devId = ref(''),
  partId = ref(''),
  expU = ref(''),
  expD = ref('')
const loading = ref(false),
  tableLoading = ref(false),
  rows = ref([]),
  drawer = ref(false),
  materialDialog = ref(false),
  tab = ref('spare')
const materialRows = ref([]),
  materialSelected = ref([]),
  materialType = ref('01-备件')
const q = reactive({
  standardCode: '',
  partCode: '',
  partName: '',
  itemName: '',
  profession: '',
  maintenanceCategory: '',
  workCategory: '',
  workContent: '',
  maintenanceStartTime: '',
  maintenanceEndTime: ''
})
const pg = reactive({ current: 1, size: 10, total: 0 })
const workCats = [
  '01-机电仅维修（各单元自立）',
  '02-机电仅维修（设备部自立）',
  '03-机电仅维修（常规工时定额）',
  '04-机电仅维修（单项工时定额）',
  '05-机电仅维修（常规项目定额）'
]
const accepts = ['01-一级验收', '02-二级验收', '03-三级验收']
const maints = ['01-日修', '02-定修', '03-年修', '04-抢修']
const profs = ['01-机械专业', '02-电气专业', '03-仪表专业', '04-过程控制专业']
const cycleUnits = ['01-天', '02-周', '03-月', '04-年']
const tagNatures = ['01-断气、汽挂牌', '02-断介质挂牌', '03-断电挂牌', '04-断油挂牌', '05-断水挂牌']
const tagLocs = ['-', '01-操作室', '02-电气室', '03-现场', '04-近控台']
const form = reactive({
  id: '',
  standardCode: '系统生成',
  partCode: '',
  partName: '',
  itemName: '',
  riskFactor: '',
  safetyMeasure: '',
  workCategory: '',
  workContent: '',
  acceptanceLevel: '',
  maintenanceCategory: '',
  profession: '',
  cycleValue: 1,
  cycleUnit: '01-天',
  lastCompletionDate: '',
  nextScheduleDate: '',
  maintenanceStartTime: '',
  maintenanceEndTime: '',
  spareParts: [],
  tools: [],
  safetyTags: []
})
const mapL = (i = {}) => ({ id: i.id, name: i.lineName || '', units: [] })
const mapU = (i = {}) => ({
  id: i.id,
  unitCode: i.unitCode || '',
  unitName: i.unitName || '',
  devices: [],
  loaded: false
})
const mapD = (i = {}) => ({
  id: i.id,
  equipmentCode: i.equipmentCode || '',
  equipmentName: i.equipmentName || '',
  parts: [],
  loaded: false
})
const mapP = (i = {}) => ({ id: i.id, partCode: i.partCode || '', partName: i.partName || '' })
const line = computed(() => lines.value.find((v) => v.id === lineId.value) || null)
const unit = computed(() => line.value?.units.find((v) => v.id === unitId.value) || null)
const dev = computed(() => unit.value?.devices.find((v) => v.id === devId.value) || null)
const part = computed(() => dev.value?.parts.find((v) => v.id === partId.value) || null)
const title = computed(
  () =>
    part.value?.partName ||
    dev.value?.equipmentName ||
    unit.value?.unitName ||
    line.value?.name ||
    '未选择'
)
function resetForm() {
  Object.assign(form, {
    id: '',
    standardCode: '系统生成',
    partCode: part.value?.partCode || '',
    partName: part.value?.partName || '',
    itemName: '',
    riskFactor: '',
    safetyMeasure: '',
    workCategory: '',
    workContent: '',
    acceptanceLevel: '',
    maintenanceCategory: '',
    profession: '',
    cycleValue: 1,
    cycleUnit: '01-天',
    lastCompletionDate: '',
    nextScheduleDate: '',
    maintenanceStartTime: '',
    maintenanceEndTime: '',
    spareParts: [],
    tools: [],
    safetyTags: []
  })
}
function payload() {
  return {
    ...form,
    spareParts: form.spareParts.map((v) => ({ ...v, quantity: Number(v.quantity || 1) })),
    tools: form.tools.map((v) => ({ ...v, quantity: Number(v.quantity || 1) }))
  }
}
async function loadTree() {
  loading.value = true
  try {
    const r = await getProductionlineList()
    lines.value = (r?.data?.records || []).map(mapL)
    if (lines.value.length) await selectLine(lines.value[0])
  } catch {
    ElMessage.error('获取生产线失败')
  } finally {
    loading.value = false
  }
}
async function selectLine(l) {
  lineId.value = l.id
  unitId.value = ''
  devId.value = ''
  partId.value = ''
  expU.value = ''
  expD.value = ''
  const r = await getDeviceUtilList(l.id)
  l.units = extractRecordsArray(r?.data).map(mapU)
  await loadPage()
}
async function selectUnit(l, u) {
  lineId.value = l.id
  unitId.value = u.id
  devId.value = ''
  partId.value = ''
  if (expU.value === u.id) {
    expU.value = ''
    expD.value = ''
    return loadPage()
  }
  expU.value = u.id
  expD.value = ''
  if (!u.loaded) {
    const r = await getEquipmentListByDeviceUnitCode(u.unitCode)
    u.devices = extractRecordsArray(r?.data).map(mapD)
    u.loaded = true
  }
  await loadPage()
}
async function selectDev(l, u, d) {
  lineId.value = l.id
  unitId.value = u.id
  devId.value = d.id
  partId.value = ''
  if (expD.value === d.id) {
    expD.value = ''
    return loadPage()
  }
  expD.value = d.id
  if (!d.loaded) {
    const r = await getEquipmentPartListByDeviceCode(d.equipmentCode)
    d.parts = extractRecordsArray(r?.data).map(mapP)
    d.loaded = true
  }
  await loadPage()
}
async function selectPart(l, u, d, p) {
  lineId.value = l.id
  unitId.value = u.id
  devId.value = d.id
  partId.value = p.id
  q.partCode = p.partCode
  q.partName = p.partName
  await loadPage()
}
async function loadPage() {
  tableLoading.value = true
  try {
    const r = await getMaintenanceStandardPage({
      current: pg.current,
      size: pg.size,
      ...q,
      partCodes: part.value?.partCode ? [part.value.partCode] : []
    })
    const d = extractPageData(r)
    rows.value = d.records || []
    pg.total = d.total || 0
  } catch (e) {
    if (!e?.elMessageNotified) ElMessage.error('获取检修标准失败')
  } finally {
    tableLoading.value = false
  }
}
async function search() {
  pg.current = 1
  await loadPage()
}
async function pageChange(v) {
  pg.current = v
  await loadPage()
}
async function sizeChange(v) {
  pg.size = v
  pg.current = 1
  await loadPage()
}
function addRow() {
  if (!part.value) return ElMessage.warning('请先选择设备部位')
  resetForm()
  drawer.value = true
}
async function editRow(r) {
  const x = await getMaintenanceStandardById(r.id)
  const d = x?.data || {},
    s = d.standard || {}
  Object.assign(form, {
    id: s.id || '',
    standardCode: s.standardCode || '系统生成',
    partCode: s.partCode || '',
    partName: s.partName || '',
    itemName: s.itemName || '',
    riskFactor: s.riskFactor || '',
    safetyMeasure: s.safetyMeasure || '',
    workCategory: s.workCategory || '',
    workContent: s.workContent || '',
    acceptanceLevel: s.acceptanceLevel || '',
    maintenanceCategory: s.maintenanceCategory || '',
    profession: s.profession || '',
    cycleValue: s.cycleValue || 1,
    cycleUnit: s.cycleUnit || '01-天',
    lastCompletionDate: s.lastCompletionDate || '',
    nextScheduleDate: s.nextScheduleDate || '',
    maintenanceStartTime: s.maintenanceStartTime || '',
    maintenanceEndTime: s.maintenanceEndTime || '',
    spareParts: d.spareParts || [],
    tools: d.tools || [],
    safetyTags: d.safetyTags || []
  })
  drawer.value = true
}
async function saveRow() {
  if (!form.itemName) return ElMessage.warning('请输入标准项目名称')
  if (form.id) await updateMaintenanceStandard(payload())
  else await addMaintenanceStandard(payload())
  ElMessage.success('保存成功')
  drawer.value = false
  await loadPage()
}
async function removeRow(r) {
  await ElMessageBox.confirm('确认删除该记录吗？', '提示')
  await deleteMaintenanceStandard(r.id)
  ElMessage.success('删除成功')
  await loadPage()
}
async function openMaterial(t) {
  materialType.value = t
  materialDialog.value = true
  const r = await getMaintenanceMaterialCandidatePage({
    current: 1,
    size: 10,
    materialSubCategory: t,
    inboundStatus: '10-已确认入库'
  })
  materialRows.value = extractPageData(r).records || []
}
function confirmMaterial() {
  if (materialSelected.value.length !== 1) return ElMessage.warning('请选择一条物料')
  ;(materialType.value === '01-备件' ? form.spareParts : form.tools).push({
    ...materialSelected.value[0],
    quantity: 1
  })
  materialDialog.value = false
}
function addTag() {
  form.safetyTags.push({ tagNature: '', tagLocation: '-', tagDeviceCode: '' })
}
function removeItem(field, index) {
  form[field].splice(index, 1)
}
loadTree()
</script>

<template>
  <div class="p">
    <div class="b">检修标准项目管理 &gt; {{ title }}</div>
    <div class="m">
      <aside class="l" v-loading="loading">
        <div class="h">设备部位</div>
        <div class="c">
          <div v-for="l in lines" :key="l.id">
            <div class="n1" @click="selectLine(l)">{{ l.name }}</div>
            <div v-if="l.id === lineId">
              <div v-for="u in l.units" :key="u.id">
                <div class="n2" @click="selectUnit(l, u)">› {{ u.unitCode }} {{ u.unitName }}</div>
                <div v-if="expU === u.id" class="ml">
                  <div v-for="d in u.devices" :key="d.id">
                    <div class="n3" @click="selectDev(l, u, d)">
                      › {{ d.equipmentCode }} {{ d.equipmentName }}
                    </div>
                    <div v-if="expD === d.id" class="ml">
                      <div
                        v-for="p in d.parts"
                        :key="p.id"
                        :class="['n4', { active: p.id === partId }]"
                        @click="selectPart(l, u, d, p)"
                      >
                        {{ p.partCode }} {{ p.partName }}
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </aside>

      <section class="r">
        <div class="card">
          <div class="g">
            <el-input v-model="q.standardCode" placeholder="标准编码" />
            <el-input v-model="q.partCode" placeholder="设备部位编码" />
            <el-input v-model="q.partName" placeholder="设备部位名称" />
            <el-input v-model="q.itemName" placeholder="项目名称" />
            <el-select v-model="q.profession" clearable placeholder="专业">
              <el-option v-for="i in profs" :key="i" :label="i" :value="i" />
            </el-select>
            <el-select v-model="q.maintenanceCategory" clearable placeholder="检修分类">
              <el-option v-for="i in maints" :key="i" :label="i" :value="i" />
            </el-select>
            <el-select v-model="q.workCategory" clearable placeholder="工事分类">
              <el-option v-for="i in workCats" :key="i" :label="i" :value="i" />
            </el-select>
            <el-input v-model="q.workContent" placeholder="工事内容" />
            <el-date-picker
              v-model="q.maintenanceStartTime"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="维护开始时间"
            />
            <el-date-picker
              v-model="q.maintenanceEndTime"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="维护结束时间"
            />
          </div>
          <div class="bar">
            <el-button type="primary" :icon="Search" @click="search">查询</el-button>
          </div>
        </div>

        <div class="bar">
          <el-button type="primary" :icon="Plus" @click="addRow">新增</el-button>
          <el-pagination
            small
            background
            layout="sizes, prev, pager, next, total"
            :current-page="pg.current"
            :page-size="pg.size"
            :page-sizes="[10, 20, 50, 100]"
            :total="pg.total"
            @current-change="pageChange"
            @size-change="sizeChange"
          />
        </div>

        <el-table v-loading="tableLoading" :data="rows" border class="main-table">
          <el-table-column label="操作" width="68" align="center">
            <template #default="{ row }">
              <el-button type="primary" link :icon="Document" @click="editRow(row)" />
            </template>
          </el-table-column>
          <el-table-column prop="standardCode" label="标准项目编码" min-width="140" />
          <el-table-column prop="partCode" label="设备部位编码" min-width="120" />
          <el-table-column prop="partName" label="设备部位名称" min-width="120" />
          <el-table-column prop="itemName" label="项目名称" min-width="120" />
          <el-table-column prop="workCategory" label="工事分类" min-width="160" />
          <el-table-column prop="workContent" label="工事内容" min-width="120" />
          <el-table-column prop="profession" label="专业" min-width="110" />
          <el-table-column prop="acceptanceLevel" label="验收等级" min-width="110" />
          <el-table-column prop="maintenanceCategory" label="检修分类" min-width="110" />
          <el-table-column label="删除" width="70">
            <template #default="{ row }">
              <el-button link type="danger" @click="removeRow(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </section>
    </div>

    <MaintenanceStandardDrawer
      v-model="drawer"
      :tabs-value="tab"
      :form="form"
      :work-category-options="workCats"
      :acceptance-level-options="accepts"
      :maintenance-category-options="maints"
      :profession-options="profs"
      :cycle-unit-options="cycleUnits"
      :tag-nature-options="tagNatures"
      :tag-location-options="tagLocs"
      @update:tabsValue="tab = $event"
      @save="saveRow"
      @open-material="openMaterial"
      @add-tag="addTag"
      @remove-item="removeItem"
    />

    <MaintenanceMaterialPicker
      v-model="materialDialog"
      :rows="materialRows"
      @selection-change="(v) => (materialSelected = v)"
      @confirm="confirmMaterial"
    />
  </div>
</template>

<style scoped>
.p {
  min-height: calc(100vh - 36px);
  padding: 10px;
  background: #f5f7fa;
}
.b {
  margin-bottom: 8px;
  color: #2f3b52;
  font-size: 14px;
  font-weight: 700;
}
.m {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 8px;
  min-height: calc(100vh - 90px);
}
.l,
.card {
  border: 1px solid #d9e2ef;
  background: #fff;
}
.h {
  padding: 10px 14px;
  border-bottom: 1px solid #e6edf6;
  color: #33507b;
  font-size: 13px;
  font-weight: 700;
}
.c {
  padding: 14px 18px;
  overflow: auto;
}
.n1,
.n2,
.n3,
.n4 {
  cursor: pointer;
}
.n1 {
  margin-bottom: 12px;
  color: #1f2d3d;
  font-size: 16px;
  font-weight: 800;
  line-height: 1.35;
}
.n2,
.n3 {
  display: flex;
  align-items: center;
  gap: 8px;
  border-radius: 5px;
}
.n2 {
  padding: 10px 14px;
  font-size: 16px;
  font-weight: 800;
  color: #1f2d3d;
}
.n3 {
  padding: 5px 0;
  font-size: 14px;
  color: #40577d;
}
.n4 {
  padding: 4px 10px;
  border-radius: 4px;
  color: #40577d;
  font-size: 14px;
}
.n4.active {
  background: #eaf2ff;
  color: #2f76e8;
  font-weight: 700;
}
.n2:hover,
.n3:hover,
.n4:hover {
  background: #f2f7ff;
}
.ml {
  margin-left: 22px;
}
.r {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.card {
  padding: 12px;
}
.g {
  display: grid;
  grid-template-columns: repeat(4, minmax(150px, 1fr));
  gap: 12px;
}
.bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.card .bar {
  margin-top: 10px;
}
.r :deep(.el-pagination.is-background .el-pager li.is-active) {
  background: #2f76e8;
}
.r :deep(.el-input__wrapper),
.r :deep(.el-select__wrapper),
.r :deep(.el-date-editor.el-input) {
  min-height: 32px;
}
.main-table :deep(.el-table__header th) {
  background: #e7f0ff;
  color: #365078;
  font-size: 12px;
  font-weight: 700;
}
.main-table :deep(.el-table__body td) {
  font-size: 12px;
}
</style>
