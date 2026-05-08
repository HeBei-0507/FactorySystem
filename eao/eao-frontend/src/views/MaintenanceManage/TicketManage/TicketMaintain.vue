<script setup>
import { reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, Plus, Search } from '@element-plus/icons-vue'
import {
  addMaintenanceTicket,
  batchDeleteMaintenanceTicket,
  getMaintenanceTicketById,
  getMaintenanceTicketPage,
  submitMaintenanceTicket,
  updateMaintenanceTicket
} from '@/api/maintenanceTicket'
import { getMaintenancePlanPage } from '@/api/maintenancePlan'
import {
  getMaintenanceMaterialCandidatePage,
  getMaintenanceStandardById,
  getMaintenanceStandardPage
} from '@/api/maintenanceStandard'
import { extractPageData } from '@/util/apiData'
import MaintenanceMaterialPicker from '@/views/BaseInfo/components/MaintenanceMaterialPicker.vue'
import MaintenanceTicketDrawer from './components/MaintenanceTicketDrawer.vue'
import MaintenancePlanPicker from './components/MaintenancePlanPicker.vue'
import MaintenanceStandardPicker from './components/MaintenanceStandardPicker.vue'
const tableLoading = ref(false),
  drawer = ref(false),
  materialDialog = ref(false),
  planDialog = ref(false),
  standardDialog = ref(false),
  tab = ref('spare')
const rows = ref([]),
  selectedIds = ref([]),
  materialRows = ref([]),
  materialSelected = ref([]),
  materialType = ref('01-备件'),
  planRows = ref([]),
  planSelected = ref([]),
  standardRows = ref([]),
  standardSelected = ref([])
const q = reactive({
    ticketCode: '',
    ticketName: '',
    status: '',
    partCode: '',
    partName: '',
    standardCode: '',
    planCode: ''
  }),
  pg = reactive({ current: 1, size: 10, total: 0 })
const operationTeams = ['01-点检', '02-精密'],
  taskTypes = [
    '01-定期修理',
    '02-设备维护',
    '03-定期校验',
    '04-设备润滑',
    '05-设备隐患',
    '06-设备故障',
    '07-设备异常',
    '08-设备整改',
    '09-临时下达',
    '10-非周期换油',
    '11-备件修复',
    '12-精密点检',
    '13-特种设备',
    '14-设备缺陷',
    '15-生产工艺',
    '16-设备技改',
    '17-返工',
    '18-配合协助'
  ],
  productionImpacts = ['-', '01-全线停产', '02-局部停产', '03-不影响生产'],
  entrustTypes = ['-', '01-自营', '02-委托'],
  workCats = [
    '01-机电仅维修（各单元自立）',
    '02-机电仅维修（设备部自立）',
    '03-机电仅维修（常规工时定额）',
    '04-机电仅维修（单项工时定额）',
    '05-机电仅维修（常规项目定额）'
  ],
  accepts = ['01-一级验收', '02-二级验收', '03-三级验收'],
  maints = ['01-日修', '02-定修', '03-年修', '04-抢修'],
  profs = ['01-机械专业', '02-电气专业', '03-仪表专业', '04-过程控制专业'],
  tagNatures = ['01-断气、汽挂牌', '02-断介质挂牌', '03-断电挂牌', '04-断油挂牌', '05-断水挂牌'],
  tagLocs = ['-', '01-操作室', '02-电气室', '03-现场', '04-近控台'],
  statusOptions = ['00-待提交', '01-待审核', '02-已通过']
const form = reactive({
  id: '',
  ticketCode: '系统生成',
  ticketName: '',
  status: '00-待提交',
  maintenancePlanName: '',
  operationTeam: '',
  taskType: '',
  productionImpact: '-',
  entrustType: '-',
  maintenanceResource: '',
  maintenanceContent: '',
  abnormalPhenomenon: '',
  handlingOpinion: '',
  planId: null,
  planCode: '',
  maintenanceCategory: '',
  maintenanceStartTime: '',
  maintenanceEndTime: '',
  standardId: null,
  standardCode: '',
  partCode: '',
  partName: '',
  itemName: '',
  profession: '',
  riskFactor: '',
  safetyMeasure: '',
  workCategory: '',
  acceptanceLevel: '',
  spareParts: [],
  tools: [],
  safetyTags: []
})
function resetForm() {
  Object.assign(form, {
    id: '',
    ticketCode: '系统生成',
    ticketName: '',
    status: '00-待提交',
    maintenancePlanName: '',
    operationTeam: '',
    taskType: '',
    productionImpact: '-',
    entrustType: '-',
    maintenanceResource: '',
    maintenanceContent: '',
    abnormalPhenomenon: '',
    handlingOpinion: '',
    planId: null,
    planCode: '',
    maintenanceCategory: '',
    maintenanceStartTime: '',
    maintenanceEndTime: '',
    standardId: null,
    standardCode: '',
    partCode: '',
    partName: '',
    itemName: '',
    profession: '',
    riskFactor: '',
    safetyMeasure: '',
    workCategory: '',
    acceptanceLevel: '',
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
async function loadPage() {
  tableLoading.value = true
  try {
    const r = await getMaintenanceTicketPage({ current: pg.current, size: pg.size, ...q })
    const d = extractPageData(r)
    rows.value = d.records || []
    pg.total = d.total || 0
  } catch (e) {
    if (!e?.elMessageNotified) ElMessage.error('获取工单列表失败')
  } finally {
    tableLoading.value = false
  }
}
function search() {
  pg.current = 1
  loadPage()
}
function sizeChange(v) {
  pg.size = v
  pg.current = 1
  loadPage()
}
function pageChange(v) {
  pg.current = v
  loadPage()
}
function addRow() {
  resetForm()
  drawer.value = true
}
async function editRow(row) {
  const x = await getMaintenanceTicketById(row.id),
    d = x?.data || {},
    t = d.ticket || {}
  Object.assign(form, {
    ...form,
    id: t.id || '',
    ticketCode: t.ticketCode || '系统生成',
    ticketName: t.ticketName || '',
    status: t.status || '00-待提交',
    maintenancePlanName: t.maintenancePlanName || '',
    operationTeam: t.operationTeam || '',
    taskType: t.taskType || '',
    productionImpact: t.productionImpact || '-',
    entrustType: t.entrustType || '-',
    maintenanceResource: t.maintenanceResource || '',
    maintenanceContent: t.maintenanceContent || '',
    abnormalPhenomenon: t.abnormalPhenomenon || '',
    handlingOpinion: t.handlingOpinion || '',
    planId: t.planId || null,
    planCode: t.planCode || '',
    maintenanceCategory: t.maintenanceCategory || '',
    maintenanceStartTime: t.maintenanceStartTime || '',
    maintenanceEndTime: t.maintenanceEndTime || '',
    standardId: t.standardId || null,
    standardCode: t.standardCode || '',
    partCode: t.partCode || '',
    partName: t.partName || '',
    itemName: t.itemName || '',
    profession: t.profession || '',
    riskFactor: t.riskFactor || '',
    safetyMeasure: t.safetyMeasure || '',
    workCategory: t.workCategory || '',
    acceptanceLevel: t.acceptanceLevel || '',
    spareParts: d.spareParts || [],
    tools: d.tools || [],
    safetyTags: d.safetyTags || []
  })
  drawer.value = true
}
async function saveRow() {
  if (!form.ticketName) return ElMessage.warning('请输入工单名称')
  if (!form.partCode) return ElMessage.warning('请选择检修标准')
  if (form.id) await updateMaintenanceTicket(payload())
  else await addMaintenanceTicket(payload())
  ElMessage.success('保存成功')
  drawer.value = false
  loadPage()
}
async function removeRows() {
  if (!selectedIds.value.length) return ElMessage.warning('请选择要删除的工单')
  await ElMessageBox.confirm('确认删除所选工单吗？仅待提交状态允许删除。', '提示', {
    type: 'warning'
  })
  await batchDeleteMaintenanceTicket(selectedIds.value)
  ElMessage.success('删除成功')
  selectedIds.value = []
  loadPage()
}
async function submitRows() {
  if (!selectedIds.value.length) return ElMessage.warning('请选择要送审的工单')
  await submitMaintenanceTicket(selectedIds.value)
  ElMessage.success('送审成功')
  selectedIds.value = []
  loadPage()
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
function removeItem(f, i) {
  form[f].splice(i, 1)
}
async function openPlan() {
  planDialog.value = true
  const r = await getMaintenancePlanPage({ current: 1, size: 50, status: '20-已通过' })
  planRows.value = extractPageData(r).records || []
}
function confirmPlan() {
  if (planSelected.value.length !== 1) return ElMessage.warning('请选择一条检修计划')
  const x = planSelected.value[0]
  form.planId = x.id
  form.planCode = x.planCode
  form.maintenanceCategory = x.maintenanceCategory || form.maintenanceCategory
  form.maintenanceStartTime = x.maintenanceStartTime || form.maintenanceStartTime
  form.maintenanceEndTime = x.maintenanceEndTime || form.maintenanceEndTime
  planDialog.value = false
}
async function openStandard() {
  standardDialog.value = true
  const r = await getMaintenanceStandardPage({ current: 1, size: 50 })
  standardRows.value = extractPageData(r).records || []
}
async function confirmStandard() {
  if (standardSelected.value.length !== 1) return ElMessage.warning('请选择一条检修标准')
  if (form.standardId && (form.spareParts.length || form.tools.length || form.safetyTags.length)) {
    try {
      await ElMessageBox.confirm(
        '重新选择检修标准将覆盖当前备件、工器具、安全挂牌明细，是否继续？',
        '提示',
        { type: 'warning' }
      )
    } catch {
      return
    }
  }
  const x = standardSelected.value[0]
  const r = await getMaintenanceStandardById(x.id)
  const d = r?.data || {}
  form.standardId = x.id
  form.standardCode = x.standardCode
  form.partCode = x.partCode
  form.partName = x.partName || form.partName
  form.itemName = x.itemName
  form.profession = x.profession || form.profession
  form.riskFactor = x.riskFactor || form.riskFactor
  form.safetyMeasure = x.safetyMeasure || form.safetyMeasure
  form.workCategory = x.workCategory || form.workCategory
  form.acceptanceLevel = x.acceptanceLevel || form.acceptanceLevel
  form.maintenanceCategory = x.maintenanceCategory || form.maintenanceCategory
  form.spareParts = (d.spareParts || []).map((i) => ({ ...i, quantity: Number(i.quantity || 1) }))
  form.tools = (d.tools || []).map((i) => ({ ...i, quantity: Number(i.quantity || 1) }))
  form.safetyTags = (d.safetyTags || []).map((i) => ({ ...i }))
  standardDialog.value = false
}
loadPage()
</script>
<template>
  <div class="p">
    <div class="b">工单管理 &gt; 工单维护</div>
    <div class="card">
      <div class="g">
        <el-input v-model="q.ticketCode" placeholder="工单编码" />
        <el-input v-model="q.ticketName" placeholder="工单名称" />
        <el-select v-model="q.status" clearable placeholder="状态">
          <el-option v-for="i in statusOptions" :key="i" :label="i" :value="i" />
        </el-select>
        <el-input v-model="q.planCode" placeholder="计划编号" />
        <el-input v-model="q.standardCode" placeholder="标准项目编码" />
        <el-input v-model="q.partCode" placeholder="设备部位编码" />
        <el-input v-model="q.partName" placeholder="设备部位名称" />
      </div>
      <div class="bar">
        <el-button type="primary" :icon="Search" @click="search">查询</el-button>
      </div>
    </div>
    <div class="bar">
      <div>
        <el-button type="primary" :icon="Plus" @click="addRow">新增</el-button>
        <el-button @click="submitRows">送审</el-button>
        <el-button type="danger" @click="removeRows">删除</el-button>
      </div>
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
    <el-table
      v-loading="tableLoading"
      :data="rows"
      border
      class="main-table"
      @selection-change="(v) => (selectedIds = v.map((i) => i.id))"
    >
      <el-table-column type="selection" width="46" />
      <el-table-column label="操作" width="68" align="center">
        <template #default="{ row }">
          <el-button type="primary" link :icon="Document" @click="editRow(row)" />
        </template>
      </el-table-column>
      <el-table-column prop="ticketCode" label="工单编码" min-width="140" />
      <el-table-column prop="ticketName" label="工单名称" min-width="120" />
      <el-table-column prop="partName" label="设备部位名称" min-width="120" />
      <el-table-column prop="planCode" label="检修计划编号" min-width="140" />
      <el-table-column prop="standardCode" label="标准项目编码" min-width="140" />
      <el-table-column prop="status" label="状态" min-width="110" />
      <el-table-column prop="maintenanceCategory" label="检修分类" min-width="110" />
      <el-table-column prop="taskType" label="任务类型" min-width="140" />
      <el-table-column prop="creatorName" label="创建人" min-width="100" />
      <el-table-column prop="creatorUsername" label="创建人账号" min-width="110" />
      <el-table-column prop="createdAt" label="创建日期" min-width="160" />
    </el-table>
    <MaintenanceTicketDrawer
      v-model="drawer"
      :tabs-value="tab"
      :form="form"
      :operation-team-options="operationTeams"
      :task-type-options="taskTypes"
      :production-impact-options="productionImpacts"
      :entrust-type-options="entrustTypes"
      :work-category-options="workCats"
      :acceptance-level-options="accepts"
      :maintenance-category-options="maints"
      :profession-options="profs"
      :tag-nature-options="tagNatures"
      :tag-location-options="tagLocs"
      @update:tabsValue="tab = $event"
      @save="saveRow"
      @open-plan="openPlan"
      @open-standard="openStandard"
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
    <MaintenancePlanPicker
      v-model="planDialog"
      :rows="planRows"
      @selection-change="(v) => (planSelected = v)"
      @confirm="confirmPlan"
    />
    <MaintenanceStandardPicker
      v-model="standardDialog"
      :rows="standardRows"
      @selection-change="(v) => (standardSelected = v)"
      @confirm="confirmStandard"
    />
  </div>
</template>
<style scoped>
.p {
  min-height: calc(100vh - 36px);
  background: #f5f7fb;
  padding: 8px;
}
.b {
  background: linear-gradient(90deg, #2f76e8, #3d8cff);
  color: #fff;
  font-size: 12px;
  padding: 6px 10px;
  border-radius: 4px;
  margin-bottom: 8px;
}
.card {
  background: #fff;
  border: 1px solid #dbe7f7;
  border-radius: 4px;
  padding: 12px;
  margin-bottom: 8px;
}
.g {
  display: grid;
  grid-template-columns: repeat(4, minmax(160px, 1fr));
  gap: 12px;
}
.bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 8px 0;
}
.main-table :deep(.el-table__header th) {
  background: #e7f0ff;
  color: #365078;
  font-size: 12px;
  font-weight: 700;
}
</style>
