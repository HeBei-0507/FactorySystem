<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Document, Search } from '@element-plus/icons-vue'
import {
  approveMaintenanceTicket,
  getMaintenanceTicketById,
  getMaintenanceTicketPage,
  rollbackMaintenanceTicket
} from '@/api/maintenanceTicket'
import { extractPageData } from '@/util/apiData'
import MaintenanceTicketDrawer from './components/MaintenanceTicketDrawer.vue'

const loading = ref(false),
  saving = ref(false),
  drawer = ref(false),
  tab = ref('spare')
const rows = ref([]),
  selectedIds = ref([])
const q = reactive({
    ticketCode: '',
    ticketName: '',
    status: '01-待审核',
    partCode: '',
    partName: '',
    standardCode: '',
    planCode: ''
  }),
  pg = reactive({ current: 1, size: 10, total: 0 })
const form = reactive({
  id: '',
  ticketCode: '',
  ticketName: '',
  status: '',
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
  statusOptions = ['01-待审核', '02-已通过']
async function loadPage() {
  loading.value = true
  try {
    const r = await getMaintenanceTicketPage({ current: pg.current, size: pg.size, ...q })
    const d = extractPageData(r)
    rows.value = d.records || []
    pg.total = d.total || 0
  } catch (e) {
    if (!e?.elMessageNotified) ElMessage.error('获取工单审核列表失败')
  } finally {
    loading.value = false
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
async function openDrawer(row) {
  const x = await getMaintenanceTicketById(row.id),
    d = x?.data || {},
    t = d.ticket || {}
  Object.assign(form, {
    ...form,
    id: t.id || '',
    ticketCode: t.ticketCode || '',
    ticketName: t.ticketName || '',
    status: t.status || '',
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
async function doApprove(target) {
  const ids = target?.id ? [target.id] : selectedIds.value
  if (!ids.length) return ElMessage.warning('请选择要同意的工单')
  saving.value = true
  try {
    await approveMaintenanceTicket(ids)
    ElMessage.success('同意成功')
    drawer.value = false
    selectedIds.value = []
    loadPage()
  } catch (e) {
    if (!e?.elMessageNotified) ElMessage.error('同意失败')
  } finally {
    saving.value = false
  }
}
async function doRollback(target) {
  const ids = target?.id ? [target.id] : selectedIds.value
  if (!ids.length) return ElMessage.warning('请选择要回退的工单')
  saving.value = true
  try {
    await rollbackMaintenanceTicket(ids)
    ElMessage.success('回退成功')
    drawer.value = false
    selectedIds.value = []
    loadPage()
  } catch (e) {
    if (!e?.elMessageNotified) ElMessage.error('回退失败')
  } finally {
    saving.value = false
  }
}
loadPage()
</script>
<template>
  <div class="p">
    <div class="b">工单管理 &gt; 工单审核</div>
    <div class="card">
      <div class="g">
        <el-input v-model="q.ticketCode" placeholder="工单编码" />
        <el-input v-model="q.ticketName" placeholder="工单名称" />
        <el-select v-model="q.status" clearable placeholder="审核状态">
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
        <el-button type="primary" @click="doApprove()">同意</el-button>
        <el-button type="warning" @click="doRollback()">回退</el-button>
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
      v-loading="loading"
      :data="rows"
      border
      class="main-table"
      @selection-change="(v) => (selectedIds = v.map((i) => i.id))"
    >
      <el-table-column type="selection" width="46" />
      <el-table-column label="操作" width="68" align="center">
        <template #default="{ row }">
          <el-button type="primary" link :icon="Document" @click="openDrawer(row)" />
        </template>
      </el-table-column>
      <el-table-column prop="ticketCode" label="工单编号" min-width="140" />
      <el-table-column prop="ticketName" label="工单名称" min-width="120" />
      <el-table-column prop="partName" label="设备部位名称" min-width="120" />
      <el-table-column prop="partCode" label="设备部位编码" min-width="120" />
      <el-table-column prop="status" label="审核状态" min-width="110" />
      <el-table-column prop="maintenanceCategory" label="检修分类" min-width="110" />
      <el-table-column prop="taskType" label="任务类型" min-width="130" />
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
      @save="() => {}"
      @open-plan="() => {}"
      @open-standard="() => {}"
      @open-material="() => {}"
      @add-tag="() => {}"
      @remove-item="() => {}"
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
