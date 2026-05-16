<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, Plus, Search } from '@element-plus/icons-vue'
import FailureSearchForm from './components/FailureSearchForm.vue'
import { useUserStore } from '@/stores/user'
import { getUserProfile } from '@/util/authStorage'
import {
  addFailure,
  deleteFailure,
  getFailurePage,
  submitFailure,
  updateFailure
} from '@/api/failure'
import { getEquipmentPartPage } from '@/api/equipmentPart'
import { extractPageData } from '@/util/apiData'
const S = '00',
  loading = ref(false),
  show = ref(false),
  saving = ref(false),
  mode = ref('create'),
  rows = ref([]),
  sels = ref([]),
  partShow = ref(false),
  partLoading = ref(false),
  parts = ref([])
const pager = reactive({ currentPage: 1, pageSize: 10, total: 0 }),
  pp = reactive({ current: 1, size: 10, total: 0 }),
  fs = reactive({
    failureCode: '',
    devicePartCode: '',
    status: S,
    failureType: '',
    failureName: '',
    failureStartTime: ''
  }),
  pf = reactive({ partCode: '', partName: '' }),
  userStore = useUserStore()
const f = reactive({
  id: null,
  failureCode: '',
  devicePartCode: '',
  devicePartName: '',
  failureName: '',
  failurePhenomenon: '',
  failureDescription: '',
  failureNoticeType: '',
  failureType: '',
  failureStartTime: '',
  status: S,
  creatorUsername: '',
  creatorName: '',
  createdAt: ''
})
const typeOps = [
    { value: '', label: '-' },
    { value: '01', label: '01-机械故障' },
    { value: '02', label: '02-电气故障' },
    { value: '03', label: '03-仪表故障' },
    { value: '04', label: '04-过程控制故障' },
    { value: '05', label: '05-其他故障' }
  ],
  stOps = [
    { value: '00', label: '00-待提交' },
    { value: '01', label: '01-待处理' },
    { value: '02', label: '02-待审核' },
    { value: '03', label: '03-已通过' }
  ],
  inputSt = [{ value: '00', label: '00-待提交' }]
const title = computed(() => (mode.value === 'create' ? '故障录入' : '故障录入维护')),
  t = (v) => String(v ?? '').trim(),
  lab = (a, v) => a.find((i) => i.value === v)?.label || v || ''
const p = (v) => String(v).padStart(2, '0')
const ms = (v) => String(v).padStart(3, '0')
const now = () => {
  const d = new Date()
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`
}
const code = () => {
  const d = new Date()
  const random = String(Math.floor(Math.random() * 1000)).padStart(3, '0')
  return `GZ${d.getFullYear()}${p(d.getMonth() + 1)}${p(d.getDate())}${p(d.getHours())}${p(d.getMinutes())}${p(d.getSeconds())}${ms(d.getMilliseconds())}${random}`
}
function init() {
  const u = userStore.profile || getUserProfile() || {}
  Object.assign(f, {
    id: null,
    failureCode: code(),
    devicePartCode: '',
    devicePartName: '',
    failureName: '',
    failurePhenomenon: '',
    failureDescription: '',
    failureNoticeType: '',
    failureType: '',
    failureStartTime: now(),
    status: S,
    creatorUsername: u.username || '',
    creatorName: u.realName || u.username || '',
    createdAt: now()
  })
}
const map = (i) => ({
  id: i.id,
  failureCode: i.failureCode || '',
  devicePartCode: i.devicePartCode || '',
  devicePartName: i.devicePartName || '',
  failureName: i.failureName || '',
  failurePhenomenon: i.failurePhenomenon || '',
  failureDescription: i.failureDescription || '',
  failureNoticeType: i.failureNoticeType || '',
  failureType: i.failureType || '',
  failureStartTime: i.failureStartTime || '',
  status: i.status || '',
  creatorUsername: i.creatorUsername || '',
  creatorName: i.creatorName || '',
  createdAt: i.createdAt || ''
})
const body = () => ({
  ...(f.id ? { id: f.id } : {}),
  failureCode: t(f.failureCode),
  devicePartCode: t(f.devicePartCode),
  devicePartName: t(f.devicePartName),
  failureName: t(f.failureName),
  failurePhenomenon: t(f.failurePhenomenon),
  failureDescription: t(f.failureDescription),
  failureNoticeType: t(f.failureNoticeType),
  failureType: t(f.failureType),
  failureStartTime: t(f.failureStartTime),
  status: S
})
function check() {
  if (!t(f.failureCode)) return (ElMessage.warning('故障编号不能为空'), false)
  if (!t(f.devicePartCode)) return (ElMessage.warning('设备部位编码不能为空'), false)
  if (!t(f.failureName)) return (ElMessage.warning('故障名称不能为空'), false)
  if (!t(f.failureStartTime)) return (ElMessage.warning('故障开始时间不能为空'), false)
  return true
}
async function load() {
  loading.value = true
  try {
    const r = await getFailurePage({
      current: pager.currentPage,
      size: pager.pageSize,
      failureCode: fs.failureCode,
      devicePartCode: fs.devicePartCode,
      status: S,
      failureType: fs.failureType,
      failureName: fs.failureName,
      failureStartTime: fs.failureStartTime
    })
    const { records, total } = extractPageData(r)
    rows.value = records.map(map)
    pager.total = total
  } catch (e) {
    if (!e?.elMessageNotified) ElMessage.error('获取故障录入列表失败')
  } finally {
    loading.value = false
  }
}
async function loadParts() {
  partLoading.value = true
  try {
    const r = await getEquipmentPartPage({
      current: pp.current,
      size: pp.size,
      partCode: t(pf.partCode),
      partName: t(pf.partName)
    })
    const { records, total } = extractPageData(r)
    parts.value = records
    pp.total = total
  } catch (e) {
    if (!e?.elMessageNotified) ElMessage.error('获取设备部位列表失败')
  } finally {
    partLoading.value = false
  }
}
const search = () => {
    pager.currentPage = 1
    load()
  },
  reset = () => {
    fs.status = S
    pager.currentPage = 1
    load()
  },
  sel = (v) => (sels.value = v),
  size = (v) => {
    pager.pageSize = v
    pager.currentPage = 1
    load()
  },
  page = (v) => {
    pager.currentPage = v
    load()
  }
function addRow() {
  mode.value = 'create'
  init()
  show.value = true
}
function editRow(r) {
  mode.value = 'edit'
  Object.assign(f, r)
  show.value = true
}
async function save() {
  if (!check()) return
  saving.value = true
  try {
    mode.value === 'create' ? await addFailure(body()) : await updateFailure(body())
    ElMessage.success(mode.value === 'create' ? '新增成功' : '修改成功')
    show.value = false
    await load()
  } catch (e) {
    if (!e?.elMessageNotified)
      ElMessage.error(mode.value === 'create' ? '新增故障记录失败' : '修改故障记录失败')
  } finally {
    saving.value = false
  }
}
async function del() {
  if (!f.id) return ElMessage.warning('当前记录尚未保存，无法删除')
  try {
    await ElMessageBox.confirm('确认删除当前故障记录吗？', '提示', { type: 'warning' })
    await deleteFailure(f.id)
    ElMessage.success('删除成功')
    show.value = false
    await load()
  } catch (e) {
    if (e === 'cancel') return
    if (!e?.elMessageNotified) ElMessage.error('删除故障记录失败')
  }
}
async function submitRows() {
  if (!sels.value.length) return ElMessage.warning('请至少选择一条待提交故障记录')
  try {
    await ElMessageBox.confirm('确认将选中的故障记录提交送审吗？', '提示', { type: 'warning' })
    await submitFailure(sels.value.map((i) => i.id))
    ElMessage.success('提交成功')
    sels.value = []
    await load()
  } catch (e) {
    if (e === 'cancel') return
    if (!e?.elMessageNotified) ElMessage.error('提交故障记录失败')
  }
}
const openParts = () => {
    pp.current = 1
    partShow.value = true
    loadParts()
  },
  partSearch = () => {
    pp.current = 1
    loadParts()
  },
  partReset = () => {
    pf.partCode = ''
    pf.partName = ''
    pp.current = 1
    loadParts()
  },
  partSize = (v) => {
    pp.size = v
    pp.current = 1
    loadParts()
  },
  partPage = (v) => {
    pp.current = v
    loadParts()
  },
  pick = (r) => {
    f.devicePartCode = r.partCode || ''
    f.devicePartName = r.partName || ''
    partShow.value = false
  }
onMounted(() => {
  userStore.initFromStorage()
  init()
  load()
})
</script>
<template>
  <div class="failure-page">
    <FailureSearchForm
      v-model="fs"
      :status-options="inputSt"
      :failure-type-options="typeOps"
      @search="search"
      @reset="reset"
    />
    <div class="table-panel">
      <div class="panel-toolbar">
        <div class="panel-title">故障录入</div>
        <div class="toolbar-actions">
          <el-button type="primary" :icon="Plus" @click="addRow">新增</el-button>
          <el-button type="primary" plain @click="submitRows">送审</el-button>
        </div>
      </div>
      <el-table v-loading="loading" :data="rows" border stripe @selection-change="sel">
        <el-table-column type="selection" width="48" align="center" />
        <el-table-column label="操作" width="68" align="center">
          <template #default="{ row }">
            <el-button type="primary" link :icon="Document" @click="editRow(row)" />
          </template>
        </el-table-column>
        <el-table-column label="设备部位编码" prop="devicePartCode" min-width="140" />
        <el-table-column label="设备部位名称" prop="devicePartName" min-width="140" />
        <el-table-column label="故障编号" prop="failureCode" min-width="150" />
        <el-table-column label="故障名称" prop="failureName" min-width="140" />
        <el-table-column
          label="故障现象"
          prop="failurePhenomenon"
          min-width="150"
          show-overflow-tooltip
        />
        <el-table-column label="故障分类" min-width="140">
          <template #default="{ row }">{{ lab(typeOps, row.failureType) }}</template>
        </el-table-column>
        <el-table-column label="故障开始时间" prop="failureStartTime" min-width="160" />
        <el-table-column label="状态" min-width="110">
          <template #default="{ row }">{{ lab(stOps, row.status) }}</template>
        </el-table-column>
        <el-table-column label="创建人" prop="creatorUsername" min-width="120" />
        <el-table-column label="创建人姓名" prop="creatorName" min-width="120" />
        <el-table-column label="创建日期" prop="createdAt" min-width="160" />
      </el-table>
      <div class="pagination-wrap">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next"
          :current-page="pager.currentPage"
          :page-size="pager.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pager.total"
          @size-change="size"
          @current-change="page"
        />
      </div>
    </div>
    <el-drawer v-model="show" :title="title" size="62%">
      <el-form label-width="128px">
        <el-row :gutter="18">
          <el-col :span="8">
            <el-form-item label="设备部位编码">
              <el-input v-model="f.devicePartCode" readonly>
                <template #append><el-button :icon="Search" @click="openParts" /></template>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="设备部位名称">
              <el-input v-model="f.devicePartName" readonly />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="故障编号"><el-input v-model="f.failureCode" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="18">
          <el-col :span="8">
            <el-form-item label="故障名称"><el-input v-model="f.failureName" /></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="故障现象"><el-input v-model="f.failurePhenomenon" /></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="故障描述">
              <el-input v-model="f.failureDescription" type="textarea" :rows="2" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="18">
          <el-col :span="8">
            <el-form-item label="故障事故通知类型">
              <el-input v-model="f.failureNoticeType" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="故障分类">
              <el-select v-model="f.failureType" class="w-full">
                <el-option
                  v-for="i in typeOps"
                  :key="i.value || 'e'"
                  :label="i.label"
                  :value="i.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="故障开始时间">
              <el-date-picker
                v-model="f.failureStartTime"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
                class="w-full"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="18">
          <el-col :span="8">
            <el-form-item label="状态">
              <el-select v-model="f.status" disabled class="w-full">
                <el-option v-for="i in inputSt" :key="i.value" :label="i.label" :value="i.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="创建人">
              <el-input :model-value="f.creatorUsername" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="创建人姓名">
              <el-input :model-value="f.creatorName" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="18">
          <el-col :span="8">
            <el-form-item label="创建日期">
              <el-input :model-value="f.createdAt" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <div class="drawer-actions">
          <el-button type="primary" :loading="saving" @click="save">保存</el-button>
          <el-button v-if="mode === 'edit'" type="danger" plain @click="del">删除</el-button>
          <el-button @click="show = false">返回</el-button>
        </div>
      </el-form>
    </el-drawer>
    <el-dialog v-model="partShow" title="设备部位选择" width="72%">
      <div class="part-search-bar">
        <el-input v-model="pf.partCode" placeholder="设备部位编码" clearable />
        <el-input v-model="pf.partName" placeholder="设备部位名称" clearable />
        <el-button type="primary" @click="partSearch">查询</el-button>
        <el-button @click="partReset">重置</el-button>
      </div>
      <el-table v-loading="partLoading" :data="parts" border stripe>
        <el-table-column label="选择" width="80" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="pick(row)">选择</el-button>
          </template>
        </el-table-column>
        <el-table-column label="设备部位编码" prop="partCode" min-width="150" />
        <el-table-column label="设备部位名称" prop="partName" min-width="150" />
      </el-table>
      <div class="pagination-wrap">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next"
          :current-page="pp.current"
          :page-size="pp.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pp.total"
          @size-change="partSize"
          @current-change="partPage"
        />
      </div>
    </el-dialog>
  </div>
</template>
<style scoped>
.failure-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.table-panel {
  padding: 12px;
  border: 1px solid #d9e2ef;
  background: #fff;
}
.panel-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.panel-title {
  font-size: 16px;
  font-weight: 600;
  color: #2b3a4d;
}
.toolbar-actions,
.drawer-actions,
.part-search-bar {
  display: flex;
  gap: 8px;
  align-items: center;
}
.pagination-wrap {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}
.w-full {
  width: 100%;
}
.drawer-actions {
  margin-top: 16px;
  justify-content: flex-end;
}
.part-search-bar {
  margin-bottom: 12px;
}
</style>
