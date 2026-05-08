<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document } from '@element-plus/icons-vue'
import AbnormalitySearchForm from './components/AbnormalitySearchForm.vue'
import {
  getAbnormalityDealById,
  getAbnormalityDealPage,
  processAbnormalityDeal,
  rollbackAbnormalityDeal
} from '@/api/abnormalityDeal'
import { extractPageData } from '@/util/apiData'

const STATUS_PENDING_DEAL = '10'
const loading = ref(false)
const drawerVisible = ref(false)
const drawerSaving = ref(false)
const tableRows = ref([])
const filters = reactive({
  abnormalCode: '',
  deviceUnitCode: '',
  status: STATUS_PENDING_DEAL,
  abnormalType: '',
  reporter: '',
  reportDate: ''
})
const pagination = reactive({ currentPage: 1, pageSize: 10, total: 0 })
const form = reactive({
  abnormalityId: null,
  abnormalCode: '',
  deviceUnitCode: '',
  source: '',
  abnormalType: '',
  safetyOutput: '',
  reportDate: '',
  reporter: '',
  handler: '',
  processMethod: '',
  abnormalDescription: '',
  handlerDate: '',
  reviewDate: '',
  status: STATUS_PENDING_DEAL,
  creatorUsername: '',
  creatorName: '',
  createdAt: '',
  abnormalOccurredDate: '',
  processOpinion: ''
})
const abnormalTypeOptions = [
  { value: '01', label: '01-磨损' },
  { value: '02', label: '02-松动' },
  { value: '03', label: '03-精度超标' },
  { value: '04', label: '04-开裂' },
  { value: '05', label: '05-损坏' },
  { value: '06', label: '06-其他' }
]
const statusOptions = [
  { value: '10', label: '10-待处理' },
  { value: '20', label: '20-已处理' },
  { value: '30', label: '30-已同意' }
]
const dealStatusOptions = [{ value: '10', label: '10-待处理' }]
const safetyOutputOptions = [
  { value: '', label: '-' },
  { value: '01', label: '01-是' },
  { value: '02', label: '02-否' }
]
const processMethodOptions = [
  { value: '', label: '-' },
  { value: '01', label: '01-正常' },
  { value: '02', label: '02-复检' },
  { value: '03', label: '03-转检修' }
]
const optionLabel = (options, value) =>
  options.find((item) => item.value === value)?.label || value || ''
const trim = (v) => String(v ?? '').trim()
function mapRow(i = {}) {
  return {
    id: i.id,
    abnormalCode: i.abnormalCode || '',
    deviceUnitCode: i.deviceUnitCode || '',
    source: i.source || '',
    abnormalType: i.abnormalType || '',
    safetyOutput: i.safetyOutput || '',
    reportDate: i.reportDate || '',
    reporter: i.reporter || '',
    handler: i.handler || '',
    processMethod: i.processMethod || '',
    abnormalDescription: i.abnormalDescription || '',
    handlerDate: i.handlerDate || '',
    reviewDate: i.reviewDate || '',
    status: i.status || '',
    creatorUsername: i.creatorUsername || '',
    creatorName: i.creatorName || '',
    createdAt: i.createdAt || '',
    abnormalOccurredDate: i.abnormalOccurredDate || '',
    processOpinion: i.processOpinion || ''
  }
}
function resetForm() {
  Object.assign(form, {
    abnormalityId: null,
    abnormalCode: '',
    deviceUnitCode: '',
    source: '',
    abnormalType: '',
    safetyOutput: '',
    reportDate: '',
    reporter: '',
    handler: '',
    processMethod: '',
    abnormalDescription: '',
    handlerDate: '',
    reviewDate: '',
    status: STATUS_PENDING_DEAL,
    creatorUsername: '',
    creatorName: '',
    createdAt: '',
    abnormalOccurredDate: '',
    processOpinion: ''
  })
}
async function loadPage() {
  loading.value = true
  try {
    const res = await getAbnormalityDealPage({
      current: pagination.currentPage,
      size: pagination.pageSize,
      abnormalCode: filters.abnormalCode,
      deviceUnitCode: filters.deviceUnitCode,
      status: STATUS_PENDING_DEAL,
      abnormalType: filters.abnormalType,
      reporter: filters.reporter,
      reportDate: filters.reportDate
    })
    const { records, total } = extractPageData(res)
    tableRows.value = records.map(mapRow)
    pagination.total = total
  } catch (e) {
    if (!e?.elMessageNotified) ElMessage.error('获取异常处理列表失败')
  } finally {
    loading.value = false
  }
}
function handleSearch() {
  pagination.currentPage = 1
  loadPage()
}
function handleReset() {
  filters.status = STATUS_PENDING_DEAL
  pagination.currentPage = 1
  loadPage()
}
function handleSizeChange(size) {
  pagination.pageSize = size
  pagination.currentPage = 1
  loadPage()
}
function handleCurrentChange(page) {
  pagination.currentPage = page
  loadPage()
}
async function openDealDrawer(row) {
  // 处理页抽屉需要把“录入主表 + 处理子表”拼成一个完整视图，因此这里单独查详情接口。
  resetForm()
  try {
    const res = await getAbnormalityDealById(row.id)
    const abnormality = res?.data?.abnormality || {}
    const deal = res?.data?.deal || {}
    Object.assign(form, {
      abnormalityId: abnormality.id || row.id,
      abnormalCode: abnormality.abnormalCode || '',
      deviceUnitCode: abnormality.deviceUnitCode || '',
      source: abnormality.source || '',
      abnormalType: abnormality.abnormalType || '',
      safetyOutput: abnormality.safetyOutput || '',
      reportDate: abnormality.reportDate || '',
      reporter: abnormality.reporter || '',
      abnormalDescription: abnormality.abnormalDescription || '',
      status: abnormality.status || STATUS_PENDING_DEAL,
      creatorUsername: abnormality.creatorUsername || '',
      creatorName: abnormality.creatorName || '',
      createdAt: abnormality.createdAt || '',
      handler: deal.processor || row.handler || '',
      processMethod: deal.processMethod || row.processMethod || '',
      handlerDate: deal.processDate || row.handlerDate || '',
      reviewDate: deal.reviewDate || row.reviewDate || '',
      abnormalOccurredDate: deal.abnormalOccurredDate || row.abnormalOccurredDate || '',
      processOpinion: deal.processOpinion || row.processOpinion || ''
    })
    drawerVisible.value = true
  } catch (e) {
    if (!e?.elMessageNotified) ElMessage.error('获取异常处理详情失败')
  }
}
function buildPayload() {
  return {
    abnormalityId: form.abnormalityId,
    processor: trim(form.handler),
    processMethod: trim(form.processMethod),
    processDate: trim(form.handlerDate),
    reviewDate: trim(form.reviewDate),
    abnormalOccurredDate: trim(form.abnormalOccurredDate),
    processOpinion: trim(form.processOpinion)
  }
}
function validateDeal() {
  if (!form.abnormalityId) return (ElMessage.warning('异常记录ID不能为空'), false)
  if (!trim(form.handler)) return (ElMessage.warning('处理人不能为空'), false)
  return true
}
async function handleProcess() {
  // “处理”会保留处理表数据，并把主记录状态流转到已处理，供下一级审核继续使用。
  if (!validateDeal()) return
  drawerSaving.value = true
  try {
    await processAbnormalityDeal(buildPayload())
    ElMessage.success('处理成功')
    drawerVisible.value = false
    await loadPage()
  } catch (e) {
    if (!e?.elMessageNotified) ElMessage.error('处理异常记录失败')
  } finally {
    drawerSaving.value = false
  }
}
async function handleRollback() {
  // 回退不仅把主记录状态打回待提交，还会清空处理表中的本环节数据。
  if (!form.abnormalityId) return ElMessage.warning('异常记录ID不能为空')
  try {
    await ElMessageBox.confirm('确认将当前异常记录回退到上一级吗？', '提示', { type: 'warning' })
    drawerSaving.value = true
    await rollbackAbnormalityDeal(buildPayload())
    ElMessage.success('回退成功')
    drawerVisible.value = false
    await loadPage()
  } catch (e) {
    if (e === 'cancel') return
    if (!e?.elMessageNotified) ElMessage.error('回退异常记录失败')
  } finally {
    drawerSaving.value = false
  }
}
onMounted(loadPage)
</script>

<template>
  <div class="abnormality-page">
    <AbnormalitySearchForm
      v-model="filters"
      :status-options="statusOptions"
      :abnormal-type-options="abnormalTypeOptions"
      @search="handleSearch"
      @reset="handleReset"
    />
    <div class="table-panel">
      <div class="panel-title">异常处理</div>
      <el-table v-loading="loading" :data="tableRows" border stripe>
        <el-table-column label="操作" width="68" align="center">
          <template #default="{ row }">
            <el-button type="primary" link :icon="Document" @click="openDealDrawer(row)" />
          </template>
        </el-table-column>
        <el-table-column label="异常单编号" prop="abnormalCode" min-width="150" />
        <el-table-column label="设备部位编码" prop="deviceUnitCode" min-width="140" />
        <el-table-column label="来源" prop="source" min-width="100" />
        <el-table-column label="异常类型" min-width="120">
          <template #default="{ row }">
            {{ optionLabel(abnormalTypeOptions, row.abnormalType) }}
          </template>
        </el-table-column>
        <el-table-column label="安全输出" min-width="110">
          <template #default="{ row }">
            {{ optionLabel(safetyOutputOptions, row.safetyOutput) }}
          </template>
        </el-table-column>
        <el-table-column label="提出日期" prop="reportDate" min-width="120" />
        <el-table-column label="提出人" prop="reporter" min-width="120" />
        <el-table-column label="处理人" prop="handler" min-width="120" />
        <el-table-column label="处理方式" min-width="120">
          <template #default="{ row }">
            {{ optionLabel(processMethodOptions, row.processMethod) }}
          </template>
        </el-table-column>
        <el-table-column
          label="异常现象"
          prop="abnormalDescription"
          min-width="180"
          show-overflow-tooltip
        />
        <el-table-column label="处理日期" prop="handlerDate" min-width="120" />
        <el-table-column label="复检日期" prop="reviewDate" min-width="120" />
        <el-table-column label="状态" min-width="110">
          <template #default="{ row }">{{ optionLabel(statusOptions, row.status) }}</template>
        </el-table-column>
        <el-table-column label="创建人" prop="creatorUsername" min-width="120" />
        <el-table-column label="创建人姓名" prop="creatorName" min-width="120" />
        <el-table-column label="创建日期" prop="createdAt" min-width="160" />
        <el-table-column
          label="处理/受审意见"
          prop="processOpinion"
          min-width="180"
          show-overflow-tooltip
        />
        <el-table-column label="异常发生日期" prop="abnormalOccurredDate" min-width="120" />
      </el-table>
      <div class="pagination-wrap">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next"
          :current-page="pagination.currentPage"
          :page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
    <el-drawer v-model="drawerVisible" title="异常处理工作场" size="66%" destroy-on-close>
      <el-form label-width="96px">
        <el-row :gutter="18">
          <el-col :span="8">
            <el-form-item label="异常单编号">
              <el-input :model-value="form.abnormalCode" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="设备部位编码">
              <el-input :model-value="form.deviceUnitCode" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="来源">
              <el-input :model-value="form.source" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="18">
          <el-col :span="8">
            <el-form-item label="异常类型">
              <el-input
                :model-value="optionLabel(abnormalTypeOptions, form.abnormalType)"
                disabled
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="安全输出">
              <el-input
                :model-value="optionLabel(safetyOutputOptions, form.safetyOutput)"
                disabled
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="提出日期">
              <el-input :model-value="form.reportDate" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="18">
          <el-col :span="8">
            <el-form-item label="提出人">
              <el-input :model-value="form.reporter" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="处理人"><el-input v-model="form.handler" /></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="处理方式">
              <el-select v-model="form.processMethod" class="w-full">
                <el-option
                  v-for="item in processMethodOptions"
                  :key="item.value || 'empty'"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="18">
          <el-col :span="8">
            <el-form-item label="异常现象">
              <el-input :model-value="form.abnormalDescription" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="处理日期">
              <el-date-picker
                v-model="form.handlerDate"
                type="date"
                value-format="YYYY-MM-DD"
                class="w-full"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="复检日期">
              <el-date-picker
                v-model="form.reviewDate"
                type="date"
                value-format="YYYY-MM-DD"
                class="w-full"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="18">
          <el-col :span="8">
            <el-form-item label="状态">
              <el-select v-model="form.status" disabled class="w-full">
                <el-option
                  v-for="item in dealStatusOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="异常发生日期">
              <el-date-picker
                v-model="form.abnormalOccurredDate"
                type="date"
                value-format="YYYY-MM-DD"
                class="w-full"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="创建日期">
              <el-input :model-value="form.createdAt" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="处理/受审意见">
          <el-input v-model="form.processOpinion" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="drawer-footer">
          <el-button type="warning" plain :loading="drawerSaving" @click="handleRollback">
            回退
          </el-button>
          <el-button type="primary" :loading="drawerSaving" @click="handleProcess">处理</el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<style scoped>
.abnormality-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 12px;
  background: #f5f8fc;
  min-height: 100%;
}
.table-panel {
  background: #fff;
  border: 1px solid #d9e2ef;
  padding: 12px;
}
.panel-title {
  font-size: 14px;
  font-weight: 600;
  color: #2b3a4d;
  margin-bottom: 12px;
}
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}
.drawer-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
.w-full {
  width: 100%;
}
</style>
