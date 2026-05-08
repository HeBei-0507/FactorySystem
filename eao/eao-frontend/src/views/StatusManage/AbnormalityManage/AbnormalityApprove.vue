<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document } from '@element-plus/icons-vue'
import AbnormalitySearchForm from './components/AbnormalitySearchForm.vue'
import {
  approveAbnormalityDeal,
  getAbnormalityApprovePage,
  getAbnormalityDealById,
  rollbackAbnormalityApprove
} from '@/api/abnormalityDeal'
import { extractPageData } from '@/util/apiData'

const STATUS_PROCESSED = '20'
// 审核页只处理“已处理”数据；同意后进入 30，回退则直接打回 00 并清掉沿途处理数据。
const loading = ref(false)
const drawerVisible = ref(false)
const drawerSaving = ref(false)
const tableRows = ref([])
const filters = reactive({
  abnormalCode: '',
  deviceUnitCode: '',
  status: STATUS_PROCESSED,
  abnormalType: '',
  reporter: '',
  reportDate: ''
})
const pagination = reactive({ currentPage: 1, pageSize: 10, total: 0 })
const form = reactive({
  abnormalityId: null,
  abnormalCode: '',
  deviceUnitCode: '',
  partName: '',
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
  status: STATUS_PROCESSED,
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
  { value: '20', label: '20-已处理' },
  { value: '30', label: '30-已同意' }
]
const approveStatusOptions = [
  { value: '20', label: '20-已处理' },
  { value: '30', label: '30-已同意' }
]
const allStatusOptions = [
  { value: '00', label: '00-待提交' },
  { value: '10', label: '10-待处理' },
  { value: '20', label: '20-已处理' },
  { value: '30', label: '30-已同意' }
]
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
function mapRow(i = {}) {
  return {
    id: i.id,
    abnormalCode: i.abnormalCode || '',
    deviceUnitCode: i.deviceUnitCode || '',
    partName: i.partName || '',
    source: i.source || '',
    abnormalType: i.abnormalType || '',
    safetyOutput: i.safetyOutput || '',
    reportDate: i.reportDate || '',
    reporter: i.reporter || '',
    abnormalDescription: i.abnormalDescription || '',
    handler: i.handler || '',
    processMethod: i.processMethod || '',
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
    partName: '',
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
    status: STATUS_PROCESSED,
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
    // 审核页列表遵循界面筛选状态；默认查 20，切到 30 后要真实带上 30，不能写死。
    const res = await getAbnormalityApprovePage({
      current: pagination.currentPage,
      size: pagination.pageSize,
      abnormalCode: filters.abnormalCode,
      deviceUnitCode: filters.deviceUnitCode,
      status: filters.status,
      abnormalType: filters.abnormalType,
      reporter: filters.reporter,
      reportDate: filters.reportDate
    })
    const { records, total } = extractPageData(res)
    tableRows.value = records.map(mapRow)
    pagination.total = total
  } catch (e) {
    if (!e?.elMessageNotified) ElMessage.error('获取异常审核列表失败')
  } finally {
    loading.value = false
  }
}
function handleSearch() {
  pagination.currentPage = 1
  loadPage()
}
function handleReset() {
  filters.status = STATUS_PROCESSED
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
async function openApproveDrawer(row) {
  // 审核页展示的是“主表 + 处理表 + 设备部位名称回填”的组合视图，所以这里取详情接口而不是直接用表格行。
  resetForm()
  try {
    const res = await getAbnormalityDealById(row.id)
    const abnormality = res?.data?.abnormality || {}
    const deal = res?.data?.deal || {}
    Object.assign(form, {
      abnormalityId: abnormality.id || row.id,
      abnormalCode: abnormality.abnormalCode || '',
      deviceUnitCode: abnormality.deviceUnitCode || '',
      partName: abnormality.partName || row.partName || '',
      source: abnormality.source || '',
      abnormalType: abnormality.abnormalType || '',
      safetyOutput: abnormality.safetyOutput || '',
      reportDate: abnormality.reportDate || '',
      reporter: abnormality.reporter || '',
      abnormalDescription: abnormality.abnormalDescription || '',
      status: abnormality.status || STATUS_PROCESSED,
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
    if (!e?.elMessageNotified) ElMessage.error('获取异常审核详情失败')
  }
}
async function handleApprove() {
  // 审核“同意”只推进主记录状态到 30；处理环节数据继续保留，便于后续追溯。
  if (!form.abnormalityId) return ElMessage.warning('异常记录ID不能为空')
  drawerSaving.value = true
  try {
    await approveAbnormalityDeal(form.abnormalityId)
    ElMessage.success('同意成功')
    drawerVisible.value = false
    await loadPage()
  } catch (e) {
    if (!e?.elMessageNotified) ElMessage.error('异常审核同意失败')
  } finally {
    drawerSaving.value = false
  }
}
async function handleRollback() {
  // 审核回退会直接回到异常录入，并删除处理阶段留下的子表记录。
  if (!form.abnormalityId) return ElMessage.warning('异常记录ID不能为空')
  try {
    await ElMessageBox.confirm('确认回退到上一级录入吗？回退后将删除沿途处理记录。', '提示', {
      type: 'warning'
    })
    drawerSaving.value = true
    await rollbackAbnormalityApprove(form.abnormalityId)
    ElMessage.success('回退成功')
    drawerVisible.value = false
    await loadPage()
  } catch (e) {
    if (e === 'cancel') return
    if (!e?.elMessageNotified) ElMessage.error('审核回退失败')
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
      <div class="panel-title">异常审核</div>
      <el-table v-loading="loading" :data="tableRows" border stripe>
        <el-table-column label="操作" width="68" align="center">
          <template #default="{ row }">
            <el-button type="primary" link :icon="Document" @click="openApproveDrawer(row)" />
          </template>
        </el-table-column>
        <el-table-column label="异常单编号" prop="abnormalCode" min-width="150" />
        <el-table-column label="设备部位编码" prop="deviceUnitCode" min-width="140" />
        <el-table-column label="设备部位名称" prop="partName" min-width="140" />
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
        <el-table-column
          label="异常现象"
          prop="abnormalDescription"
          min-width="180"
          show-overflow-tooltip
        />
        <el-table-column label="处理人" prop="handler" min-width="120" />
        <el-table-column label="处理方式" min-width="120">
          <template #default="{ row }">
            {{ optionLabel(processMethodOptions, row.processMethod) }}
          </template>
        </el-table-column>
        <el-table-column
          label="处理/受审意见"
          prop="processOpinion"
          min-width="180"
          show-overflow-tooltip
        />
        <el-table-column label="处理日期" prop="handlerDate" min-width="120" />
        <el-table-column label="复检日期" prop="reviewDate" min-width="120" />
        <el-table-column label="异常发生日期" prop="abnormalOccurredDate" min-width="120" />
        <el-table-column label="状态" min-width="110">
          <template #default="{ row }">{{ optionLabel(allStatusOptions, row.status) }}</template>
        </el-table-column>
        <el-table-column label="创建人" prop="creatorUsername" min-width="120" />
        <el-table-column label="创建人姓名" prop="creatorName" min-width="120" />
        <el-table-column label="创建日期" prop="createdAt" min-width="160" />
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
    <el-drawer v-model="drawerVisible" title="异常审核" size="66%" destroy-on-close>
      <el-form label-width="104px">
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
            <el-form-item label="设备部位名称">
              <el-input :model-value="form.partName" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="18">
          <el-col :span="8">
            <el-form-item label="来源">
              <el-input :model-value="form.source" disabled />
            </el-form-item>
          </el-col>
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
        </el-row>
        <el-row :gutter="18">
          <el-col :span="8">
            <el-form-item label="提出日期">
              <el-input :model-value="form.reportDate" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="提出人">
              <el-input :model-value="form.reporter" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="处理人">
              <el-input :model-value="form.handler" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="18">
          <el-col :span="8">
            <el-form-item label="处理方式">
              <el-input
                :model-value="optionLabel(processMethodOptions, form.processMethod)"
                disabled
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="异常现象">
              <el-input :model-value="form.abnormalDescription" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="处理日期">
              <el-input :model-value="form.handlerDate" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="18">
          <el-col :span="8">
            <el-form-item label="复检日期">
              <el-input :model-value="form.reviewDate" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="状态">
              <el-select v-model="form.status" disabled class="w-full">
                <el-option
                  v-for="item in approveStatusOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="异常发生日期">
              <el-input :model-value="form.abnormalOccurredDate" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="处理/受审意见">
          <el-input :model-value="form.processOpinion" type="textarea" :rows="4" disabled />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="drawer-footer">
          <el-button type="warning" plain :loading="drawerSaving" @click="handleRollback">
            回退
          </el-button>
          <el-button type="primary" :loading="drawerSaving" @click="handleApprove">同意</el-button>
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
