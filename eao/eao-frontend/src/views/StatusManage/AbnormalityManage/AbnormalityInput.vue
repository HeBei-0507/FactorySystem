// 异常录入
<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, Plus } from '@element-plus/icons-vue'
import AbnormalitySearchForm from './components/AbnormalitySearchForm.vue'
import {
  addAbnormality,
  deleteAbnormality,
  getAbnormalityPage,
  submitAbnormality,
  updateAbnormality
} from '@/api/abnormality'
import { extractPageData } from '@/util/apiData'

const STATUS_PENDING_SUBMIT = '00'
// 录入页只面向“待提交”数据；提交动作会把它流转到异常处理页的“待处理”。
const loading = ref(false)
const drawerVisible = ref(false)
const drawerSaving = ref(false)
const drawerMode = ref('create')
const tableRows = ref([])
const selectedRows = ref([])
const filters = reactive({
  abnormalCode: '',
  deviceUnitCode: '',
  status: STATUS_PENDING_SUBMIT,
  abnormalType: '',
  reporter: '',
  reportDate: ''
})
const pagination = reactive({ currentPage: 1, pageSize: 10, total: 0 })
const form = reactive({
  id: null,
  abnormalCode: '',
  deviceUnitCode: '',
  source: '手工录入',
  abnormalLocation: '',
  abnormalType: '',
  safetyOutput: '',
  reporter: '',
  abnormalDescription: '',
  reportDate: '',
  createdAt: '',
  status: STATUS_PENDING_SUBMIT,
  handler: '',
  handlerName: '',
  handlerDate: ''
})

const safetyOutputOptions = [
  { value: '', label: '-' },
  { value: '01', label: '01-是' },
  { value: '02', label: '02-否' }
]
const abnormalTypeOptions = [
  { value: '01', label: '01-磨损' },
  { value: '02', label: '02-松动' },
  { value: '03', label: '03-精度超标' },
  { value: '04', label: '04-开裂' },
  { value: '05', label: '05-损坏' },
  { value: '06', label: '06-其他' }
]
const statusOptions = [
  { value: '00', label: '00-待提交' },
  { value: '10', label: '10-待处理' },
  { value: '20', label: '20-已处理' },
  { value: '30', label: '30-已同意' }
]
const inputStatusOptions = [{ value: '00', label: '00-待提交' }]
const drawerTitle = computed(() => (drawerMode.value === 'create' ? '异常录入' : '异常录入维护'))
const trim = (v) => String(v ?? '').trim()
const optionLabel = (options, value) =>
  options.find((item) => item.value === value)?.label || value || ''

function createAbnormalCode() {
  // 先用前端时间戳生成可读单号，避免用户新增时还未落库就没有业务编号可见。
  const d = new Date()
  const p = (v) => String(v).padStart(2, '0')
  return `YC${d.getFullYear()}${p(d.getMonth() + 1)}${p(d.getDate())}${p(d.getHours())}${p(d.getMinutes())}${p(d.getSeconds())}`
}
function resetForm() {
  Object.assign(form, {
    id: null,
    abnormalCode: createAbnormalCode(),
    deviceUnitCode: '',
    source: '手工录入',
    abnormalLocation: '',
    abnormalType: '',
    safetyOutput: '',
    reporter: '',
    abnormalDescription: '',
    reportDate: new Date().toISOString().slice(0, 10),
    createdAt: '',
    status: STATUS_PENDING_SUBMIT,
    handler: '',
    handlerName: '',
    handlerDate: ''
  })
}
function mapRow(i = {}) {
  return {
    id: i.id,
    abnormalCode: i.abnormalCode || '',
    deviceUnitCode: i.deviceUnitCode || '',
    source: i.source || '',
    abnormalLocation: i.abnormalLocation || '',
    abnormalType: i.abnormalType || '',
    safetyOutput: i.safetyOutput || '',
    reporter: i.reporter || '',
    abnormalDescription: i.abnormalDescription || '',
    reportDate: i.reportDate || '',
    status: i.status || '',
    handler: i.handler || '',
    handlerName: i.handlerName || '',
    handlerDate: i.handlerDate || '',
    creatorUsername: i.creatorUsername || '',
    creatorName: i.creatorName || '',
    createdAt: i.createdAt || ''
  }
}
function buildPayload() {
  return {
    ...(form.id ? { id: form.id } : {}),
    abnormalCode: trim(form.abnormalCode),
    deviceUnitCode: trim(form.deviceUnitCode),
    source: trim(form.source),
    abnormalLocation: trim(form.abnormalLocation),
    abnormalType: trim(form.abnormalType),
    safetyOutput: trim(form.safetyOutput),
    reporter: trim(form.reporter),
    abnormalDescription: trim(form.abnormalDescription),
    reportDate: trim(form.reportDate),
    status: STATUS_PENDING_SUBMIT,
    handler: trim(form.handler),
    handlerName: trim(form.handlerName),
    handlerDate: trim(form.handlerDate)
  }
}
function validateForm() {
  if (!trim(form.abnormalCode)) return (ElMessage.warning('异常单编号不能为空'), false)
  if (!trim(form.deviceUnitCode)) return (ElMessage.warning('设备部位编码不能为空'), false)
  if (!trim(form.abnormalType)) return (ElMessage.warning('请选择异常类型'), false)
  if (!trim(form.reportDate)) return (ElMessage.warning('报出日期不能为空'), false)
  return true
}
async function loadPage() {
  // 录入页固定只查 00，避免已提交数据仍停留在当前页面。
  loading.value = true
  try {
    const res = await getAbnormalityPage({
      current: pagination.currentPage,
      size: pagination.pageSize,
      abnormalCode: filters.abnormalCode,
      deviceUnitCode: filters.deviceUnitCode,
      status: STATUS_PENDING_SUBMIT,
      abnormalType: filters.abnormalType,
      reporter: filters.reporter,
      reportDate: filters.reportDate
    })
    const { records, total } = extractPageData(res)
    tableRows.value = records.map(mapRow)
    pagination.total = total
  } catch (e) {
    if (!e?.elMessageNotified) ElMessage.error('获取异常录入列表失败')
  } finally {
    loading.value = false
  }
}
function handleSearch() {
  pagination.currentPage = 1
  loadPage()
}
function handleReset() {
  filters.status = STATUS_PENDING_SUBMIT
  pagination.currentPage = 1
  loadPage()
}
function handleSelectionChange(rows) {
  selectedRows.value = rows
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
function openCreateDrawer() {
  drawerMode.value = 'create'
  resetForm()
  drawerVisible.value = true
}
function openEditDrawer(row) {
  drawerMode.value = 'edit'
  Object.assign(form, {
    id: row.id,
    abnormalCode: row.abnormalCode || '',
    deviceUnitCode: row.deviceUnitCode || '',
    source: row.source || '手工录入',
    abnormalLocation: row.abnormalLocation || '',
    abnormalType: row.abnormalType || '',
    safetyOutput: row.safetyOutput || '',
    reporter: row.reporter || '',
    abnormalDescription: row.abnormalDescription || '',
    reportDate: row.reportDate || '',
    createdAt: row.createdAt || '',
    status: row.status || STATUS_PENDING_SUBMIT,
    handler: row.handler || '',
    handlerName: row.handlerName || '',
    handlerDate: row.handlerDate || ''
  })
  drawerVisible.value = true
}
async function handleSave() {
  if (!validateForm()) return
  drawerSaving.value = true
  try {
    const payload = buildPayload()
    if (drawerMode.value === 'create') {
      await addAbnormality(payload)
      ElMessage.success('新增成功')
    } else {
      await updateAbnormality(payload)
      ElMessage.success('修改成功')
    }
    drawerVisible.value = false
    await loadPage()
  } catch (e) {
    if (!e?.elMessageNotified)
      ElMessage.error(drawerMode.value === 'create' ? '新增异常记录失败' : '修改异常记录失败')
  } finally {
    drawerSaving.value = false
  }
}
async function handleDelete() {
  if (!form.id) return ElMessage.warning('当前记录尚未保存，无法删除')
  try {
    await ElMessageBox.confirm('确认删除当前异常记录吗？', '提示', { type: 'warning' })
    await deleteAbnormality(form.id)
    ElMessage.success('删除成功')
    drawerVisible.value = false
    await loadPage()
  } catch (e) {
    if (e === 'cancel') return
    if (!e?.elMessageNotified) ElMessage.error('删除异常记录失败')
  }
}
async function handleSubmitSelected() {
  if (!selectedRows.value.length) return ElMessage.warning('请至少选择一条待提交异常记录')
  try {
    await ElMessageBox.confirm('确认将选中的异常记录提交为待处理吗？', '提示', { type: 'warning' })
    await submitAbnormality(selectedRows.value.map((item) => item.id))
    ElMessage.success('提交成功')
    selectedRows.value = []
    await loadPage()
  } catch (e) {
    if (e === 'cancel') return
    if (!e?.elMessageNotified) ElMessage.error('提交异常记录失败')
  }
}

onMounted(() => {
  resetForm()
  loadPage()
})
</script>

<template>
  <div class="abnormality-page">
    <AbnormalitySearchForm
      v-model="filters"
      :status-options="inputStatusOptions"
      :abnormal-type-options="abnormalTypeOptions"
      @search="handleSearch"
      @reset="handleReset"
    />
    <div class="table-panel">
      <div class="panel-toolbar">
        <div class="panel-title">异常录入</div>
        <div class="toolbar-actions">
          <el-button type="primary" :icon="Plus" @click="openCreateDrawer">新增</el-button>
          <el-button type="primary" plain @click="handleSubmitSelected">提交</el-button>
        </div>
      </div>
      <el-table
        v-loading="loading"
        :data="tableRows"
        border
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="48" align="center" />
        <!-- 操作列是纯前端交互入口，不对应数据库字段。 -->
        <el-table-column label="操作" width="68" align="center">
          <template #default="{ row }">
            <el-button type="primary" link :icon="Document" @click="openEditDrawer(row)" />
          </template>
        </el-table-column>
        <el-table-column label="异常单编号" prop="abnormalCode" min-width="150" />
        <el-table-column label="设备部位编码" prop="deviceUnitCode" min-width="140" />
        <el-table-column label="来源" prop="source" min-width="120" />
        <el-table-column label="状态" min-width="110">
          <template #default="{ row }">{{ optionLabel(statusOptions, row.status) }}</template>
        </el-table-column>
        <el-table-column label="异常类型" min-width="120">
          <template #default="{ row }">
            {{ optionLabel(abnormalTypeOptions, row.abnormalType) }}
          </template>
        </el-table-column>
        <el-table-column
          label="异常现象"
          prop="abnormalDescription"
          min-width="180"
          show-overflow-tooltip
        />
        <el-table-column label="安全输出" min-width="110">
          <template #default="{ row }">
            {{ optionLabel(safetyOutputOptions, row.safetyOutput) }}
          </template>
        </el-table-column>
        <el-table-column label="报出人" prop="reporter" min-width="120" />
        <el-table-column label="报出日期" prop="reportDate" min-width="120" />
        <el-table-column label="创建人" prop="creatorName" min-width="120" />
        <el-table-column label="创建人工号" prop="creatorUsername" min-width="120" />
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
    <el-drawer v-model="drawerVisible" :title="drawerTitle" size="56%" destroy-on-close>
      <el-form label-width="96px">
        <el-row :gutter="18">
          <el-col :span="8">
            <el-form-item label="异常单编号"><el-input v-model="form.abnormalCode" /></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="设备部位编码">
              <el-input v-model="form.deviceUnitCode" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="来源"><el-input v-model="form.source" /></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="报出日期">
              <el-date-picker
                v-model="form.reportDate"
                type="date"
                value-format="YYYY-MM-DD"
                class="w-full"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="18">
          <el-col :span="8">
            <el-form-item label="安全输出">
              <el-select v-model="form.safetyOutput" class="w-full">
                <el-option
                  v-for="item in safetyOutputOptions"
                  :key="item.value || 'empty'"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="异常类型">
              <el-select v-model="form.abnormalType" class="w-full">
                <el-option
                  v-for="item in abnormalTypeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="状态">
              <el-select v-model="form.status" disabled class="w-full">
                <el-option
                  v-for="item in inputStatusOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="18">
          <el-col :span="8">
            <el-form-item label="创建日期">
              <el-input :model-value="form.createdAt || ''" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="报出人"><el-input v-model="form.reporter" /></el-form-item>
          </el-col>
          <el-col :span="16">
            <el-form-item label="异常位置">
              <el-input v-model="form.abnormalLocation" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="异常现象">
          <el-input v-model="form.abnormalDescription" type="textarea" :rows="5" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="drawer-footer">
          <el-button v-if="drawerMode === 'edit'" type="danger" plain @click="handleDelete">
            删除
          </el-button>
          <div class="drawer-footer-right">
            <el-button @click="drawerVisible = false">取消</el-button>
            <el-button type="primary" :loading="drawerSaving" @click="handleSave">
              {{ drawerMode === 'create' ? '新增' : '修改' }}
            </el-button>
          </div>
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
.panel-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.panel-title {
  font-size: 14px;
  font-weight: 600;
  color: #2b3a4d;
}
.toolbar-actions,
.drawer-footer-right {
  display: flex;
  gap: 8px;
}
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}
.drawer-footer {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.w-full {
  width: 100%;
}
</style>
