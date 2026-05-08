<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document } from '@element-plus/icons-vue'
import FailureSearchForm from './components/FailureSearchForm.vue'
import {
  getFailureDealById,
  getFailureDealPage,
  processFailureDeal,
  rollbackFailureDeal
} from '@/api/failureDeal'
import { extractPageData } from '@/util/apiData'
const S = '01',
  loading = ref(false),
  show = ref(false),
  saving = ref(false),
  rows = ref([])
const fs = reactive({
    failureCode: '',
    devicePartCode: '',
    status: S,
    failureType: '',
    failureName: '',
    failureStartTime: ''
  }),
  pager = reactive({ currentPage: 1, pageSize: 10, total: 0 })
const f = reactive({
  failureId: null,
  failureCode: '',
  devicePartCode: '',
  devicePartName: '',
  failureName: '',
  failurePhenomenon: '',
  failureDescription: '',
  failureNoticeType: '',
  failureType: '',
  failureStartTime: '',
  severityLevel: '',
  processTime: '',
  processDescription: '',
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
  stOps = [{ value: '01', label: '01-待处理' }]
const t = (v) => String(v ?? '').trim(),
  lab = (a, v) => a.find((i) => i.value === v)?.label || v || ''
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
  severityLevel: i.severityLevel || '',
  processTime: i.processTime || '',
  processDescription: i.processDescription || '',
  status: i.status || '',
  creatorUsername: i.creatorUsername || '',
  creatorName: i.creatorName || '',
  createdAt: i.createdAt || ''
})
function resetForm() {
  Object.assign(f, {
    failureId: null,
    failureCode: '',
    devicePartCode: '',
    devicePartName: '',
    failureName: '',
    failurePhenomenon: '',
    failureDescription: '',
    failureNoticeType: '',
    failureType: '',
    failureStartTime: '',
    severityLevel: '',
    processTime: '',
    processDescription: '',
    status: S,
    creatorUsername: '',
    creatorName: '',
    createdAt: ''
  })
}
async function load() {
  loading.value = true
  try {
    const r = await getFailureDealPage({
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
    if (!e?.elMessageNotified) ElMessage.error('获取故障处理列表失败')
  } finally {
    loading.value = false
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
  size = (v) => {
    pager.pageSize = v
    pager.currentPage = 1
    load()
  },
  page = (v) => {
    pager.currentPage = v
    load()
  }
async function openRow(r) {
  resetForm()
  try {
    const res = await getFailureDealById(r.id)
    const a = res?.data?.failure || {},
      d = res?.data?.deal || {}
    Object.assign(f, {
      failureId: a.id || r.id,
      failureCode: a.failureCode || '',
      devicePartCode: a.devicePartCode || '',
      devicePartName: a.devicePartName || '',
      failureName: a.failureName || '',
      failurePhenomenon: a.failurePhenomenon || '',
      failureDescription: a.failureDescription || '',
      failureNoticeType: a.failureNoticeType || '',
      failureType: a.failureType || '',
      failureStartTime: a.failureStartTime || '',
      severityLevel: d.severityLevel || r.severityLevel || '',
      processTime: d.processTime || r.processTime || '',
      processDescription: d.processDescription || r.processDescription || '',
      status: a.status || S,
      creatorUsername: a.creatorUsername || '',
      creatorName: a.creatorName || '',
      createdAt: a.createdAt || ''
    })
    show.value = true
  } catch (e) {
    if (!e?.elMessageNotified) ElMessage.error('获取故障处理详情失败')
  }
}
function body() {
  return {
    failureId: f.failureId,
    severityLevel: t(f.severityLevel),
    processTime: t(f.processTime),
    processDescription: t(f.processDescription)
  }
}
function check() {
  if (!f.failureId) return (ElMessage.warning('故障记录ID不能为空'), false)
  if (!t(f.severityLevel)) return (ElMessage.warning('事件等级不能为空'), false)
  if (!t(f.processTime)) return (ElMessage.warning('处理时间不能为空'), false)
  return true
}
async function processRow() {
  if (!check()) return
  saving.value = true
  try {
    await processFailureDeal(body())
    ElMessage.success('处理成功')
    show.value = false
    await load()
  } catch (e) {
    if (!e?.elMessageNotified) ElMessage.error('处理故障记录失败')
  } finally {
    saving.value = false
  }
}
async function rollbackRow() {
  if (!f.failureId) return ElMessage.warning('故障记录ID不能为空')
  try {
    await ElMessageBox.confirm('确认将当前故障记录回退到录入吗？', '提示', { type: 'warning' })
    saving.value = true
    await rollbackFailureDeal(body())
    ElMessage.success('回退成功')
    show.value = false
    await load()
  } catch (e) {
    if (e === 'cancel') return
    if (!e?.elMessageNotified) ElMessage.error('回退故障记录失败')
  } finally {
    saving.value = false
  }
}
onMounted(load)
</script>
<template>
  <div class="failure-page">
    <FailureSearchForm
      v-model="fs"
      :status-options="stOps"
      :failure-type-options="typeOps"
      @search="search"
      @reset="reset"
    />
    <div class="table-panel">
      <div class="panel-title">故障处理</div>
      <el-table v-loading="loading" :data="rows" border stripe>
        <el-table-column label="操作" width="68" align="center">
          <template #default="{ row }">
            <el-button type="primary" link :icon="Document" @click="openRow(row)" />
          </template>
        </el-table-column>
        <el-table-column label="设备部位编码" prop="devicePartCode" min-width="140" />
        <el-table-column label="设备部位名称" prop="devicePartName" min-width="140" />
        <el-table-column label="故障编号" prop="failureCode" min-width="150" />
        <el-table-column label="故障名称" prop="failureName" min-width="140" />
        <el-table-column
          label="故障现象"
          prop="failurePhenomenon"
          min-width="160"
          show-overflow-tooltip
        />
        <el-table-column label="事件等级" prop="severityLevel" min-width="120" />
        <el-table-column label="处理时间" prop="processTime" min-width="160" />
        <el-table-column
          label="处理说明"
          prop="processDescription"
          min-width="180"
          show-overflow-tooltip
        />
        <el-table-column label="状态" min-width="110">
          <template #default="{ row }">{{ lab(stOps, row.status) }}</template>
        </el-table-column>
        <el-table-column label="创建人" prop="creatorUsername" min-width="120" />
        <el-table-column label="创建人姓名" prop="creatorName" min-width="120" />
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
    <el-drawer v-model="show" title="故障处理工作场" size="66%">
      <el-form label-width="118px">
        <el-row :gutter="18">
          <el-col :span="8">
            <el-form-item label="设备部位编码">
              <el-input :model-value="f.devicePartCode" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="设备部位名称">
              <el-input :model-value="f.devicePartName" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="故障编号">
              <el-input :model-value="f.failureCode" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="18">
          <el-col :span="8">
            <el-form-item label="故障名称">
              <el-input :model-value="f.failureName" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="故障现象">
              <el-input :model-value="f.failurePhenomenon" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="故障分类">
              <el-input :model-value="lab(typeOps, f.failureType)" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="18">
          <el-col :span="8">
            <el-form-item label="故障开始时间">
              <el-input :model-value="f.failureStartTime" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="事件等级"><el-input v-model="f.severityLevel" /></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="处理时间">
              <el-date-picker
                v-model="f.processTime"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
                class="w-full"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="18">
          <el-col :span="12">
            <el-form-item label="处理说明">
              <el-input v-model="f.processDescription" type="textarea" :rows="3" />
            </el-form-item>
          </el-col>
        </el-row>
        <div class="drawer-actions">
          <el-button type="primary" :loading="saving" @click="processRow">处理</el-button>
          <el-button type="warning" plain :loading="saving" @click="rollbackRow">回退</el-button>
          <el-button @click="show = false">返回</el-button>
        </div>
      </el-form>
    </el-drawer>
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
.panel-title {
  font-size: 16px;
  font-weight: 600;
  color: #2b3a4d;
  margin-bottom: 12px;
}
.pagination-wrap {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}
.drawer-actions {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
.w-full {
  width: 100%;
}
</style>
