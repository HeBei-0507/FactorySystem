<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import {
  addIDAddress,
  batchDeleteIDAddress,
  getIDAddressPage,
  updateIDAddress
} from '@/api/IDAddress'
import { useUserStore } from '@/stores/user'
import { extractPageData } from '@/util/apiData'

const userStore = useUserStore()
const tableRef = ref()
const tableData = ref([])
const selectedRows = ref([])
const editingRowId = ref('')
const creatingRowId = ref('')
const loading = ref(false)
const filters = reactive({
  idLocationCode: '',
  idLocationName: '',
  idLocationInnerCode: '',
  locationType: '',
  idLocationCategory: ''
})
const pagination = reactive({ currentPage: 1, pageSize: 10, total: 0 })
const locationTypeOptions = [
  { value: '01', label: '01-二维码' },
  { value: '02', label: '02-射频标签' }
]

const date = (v) => (v ? String(v).slice(0, 10) : '')
const trim = (v) => String(v || '').trim()
const edit = (r) => editingRowId.value === r.id
const locationTypeLabel = (value) => {
  const hit = locationTypeOptions.find((item) => item.value === value)
  return hit ? hit.label : value || ''
}

function resetSelection() {
  selectedRows.value = []
  tableRef.value && tableRef.value.clearSelection()
}
function userInfo() {
  const p = userStore.profile
  const username = p?.username ? String(p.username) : ''
  const realName = p?.realName && String(p.realName).trim() ? String(p.realName).trim() : username
  return { username, realName }
}
function mapRow(i = {}) {
  return {
    id: i.id,
    idLocationCode: i.idLocationCode || '',
    idLocationName: i.idLocationName || '',
    idLocationInnerCode: i.idLocationInnerCode || '',
    locationType: i.locationType || '',
    idLocationCategory: i.idLocationCategory || '',
    creatorUsername: i.creatorUsername || '',
    creatorName: i.creatorName || '',
    createdAt: date(i.createdAt)
  }
}
async function loadPage() {
  loading.value = true
  try {
    const res = await getIDAddressPage({
      current: pagination.currentPage,
      size: pagination.pageSize,
      idLocationCode: filters.idLocationCode,
      idLocationName: filters.idLocationName,
      idLocationInnerCode: filters.idLocationInnerCode,
      locationType: filters.locationType,
      idLocationCategory: filters.idLocationCategory
    })
    const { records, total } = extractPageData(res)
    tableData.value = records.map(mapRow)
    pagination.total = total
  } catch (e) {
    if (!e?.elMessageNotified) ElMessage.error('获取ID位置分页数据失败')
  } finally {
    loading.value = false
  }
}
function handleSearch() {
  pagination.currentPage = 1
  resetSelection()
  loadPage()
}
function handlePageChange(page) {
  pagination.currentPage = page
  resetSelection()
  loadPage()
}
function handleSizeChange(size) {
  pagination.pageSize = size
  pagination.currentPage = 1
  resetSelection()
  loadPage()
}
function resetFilters() {
  Object.assign(filters, {
    idLocationCode: '',
    idLocationName: '',
    idLocationInnerCode: '',
    locationType: '',
    idLocationCategory: ''
  })
  handleSearch()
}
function createEmptyRow() {
  if (creatingRowId.value) return ElMessage.warning('请先完成当前新增行')
  const u = userInfo()
  const d = new Date().toISOString().slice(0, 10)
  tableData.value.unshift({
    id: `draft-${Date.now()}`,
    idLocationCode: '',
    idLocationName: '',
    idLocationInnerCode: '',
    locationType: '',
    idLocationCategory: '',
    creatorUsername: u.username,
    creatorName: u.realName,
    createdAt: d,
    isNew: true
  })
  creatingRowId.value = tableData.value[0].id
  editingRowId.value = tableData.value[0].id
  resetSelection()
}
function startEdit() {
  if (selectedRows.value.length !== 1) return ElMessage.warning('请选择一条需要修改的ID位置')
  if (selectedRows.value[0].isNew)
    return ElMessage.warning('未保存的新增行请先点“新增”保存，再编辑')
  editingRowId.value = selectedRows.value[0].id
  creatingRowId.value = ''
}
function valid(row) {
  if (!trim(row.idLocationCode)) return (ElMessage.warning('ID位置编码不能为空'), false)
  if (!trim(row.idLocationName)) return (ElMessage.warning('ID位置不能为空'), false)
  if (!trim(row.creatorUsername)) return (ElMessage.warning('创建人不能为空'), false)
  if (!trim(row.creatorName)) return (ElMessage.warning('创建人姓名不能为空'), false)
  if (!trim(row.createdAt)) return (ElMessage.warning('创建日期不能为空'), false)
  return true
}
function payload(row, withId = false) {
  const data = {
    idLocationCode: trim(row.idLocationCode),
    idLocationName: trim(row.idLocationName),
    idLocationInnerCode: trim(row.idLocationInnerCode),
    locationType: trim(row.locationType),
    idLocationCategory: trim(row.idLocationCategory),
    creatorUsername: trim(row.creatorUsername),
    creatorName: trim(row.creatorName),
    createdAt: trim(row.createdAt)
  }
  if (withId) data.id = row.id
  return data
}
async function submitCreate() {
  const row = tableData.value.find((item) => item.id === creatingRowId.value)
  if (!row) return ElMessage.warning('当前没有待新增的ID位置')
  if (!valid(row)) return
  try {
    await addIDAddress(payload(row))
    ElMessage.success('新增成功')
    creatingRowId.value = ''
    editingRowId.value = ''
    await loadPage()
  } catch (e) {
    if (!e?.elMessageNotified) ElMessage.error('新增ID位置失败')
  }
}
async function submitEdit() {
  if (selectedRows.value.length !== 1) return ElMessage.warning('请选择一条需要修改的ID位置')
  const row = selectedRows.value[0]
  if (row.isNew) return ElMessage.warning('未保存的新增行请使用“新增”保存')
  if (!valid(row)) return
  try {
    await updateIDAddress(payload(row, true))
    ElMessage.success('修改成功')
    editingRowId.value = ''
    await loadPage()
  } catch (e) {
    if (!e?.elMessageNotified) ElMessage.error('修改ID位置失败')
  }
}
async function removeRows() {
  if (!selectedRows.value.length) return ElMessage.warning('请至少选择一条ID位置')
  const ids = selectedRows.value.filter((item) => !item.isNew).map((item) => item.id)
  const drafts = new Set(selectedRows.value.filter((item) => item.isNew).map((item) => item.id))
  tableData.value = tableData.value.filter((item) => !drafts.has(item.id))
  if (!ids.length) {
    resetSelection()
    creatingRowId.value = ''
    editingRowId.value = ''
    return ElMessage.success('已删除未保存的新增行')
  }
  try {
    await batchDeleteIDAddress(ids)
    ElMessage.success('删除成功')
    creatingRowId.value = ''
    editingRowId.value = ''
    resetSelection()
    await loadPage()
  } catch (e) {
    if (!e?.elMessageNotified) ElMessage.error('删除ID位置失败')
  }
}

onMounted(loadPage)
</script>

<template>
  <div class="p">
    <div class="s">
      <div class="g">
        <div class="i">
          <span>ID位置编码</span>
          <el-input v-model="filters.idLocationCode" clearable />
        </div>
        <div class="i">
          <span>ID位置</span>
          <el-input v-model="filters.idLocationName" clearable />
        </div>
        <div class="i">
          <span>ID位置厂内编码</span>
          <el-input v-model="filters.idLocationInnerCode" clearable />
        </div>
        <div class="i">
          <span>位置类型</span>
          <el-select v-model="filters.locationType" clearable placeholder="请选择位置类型">
            <el-option
              v-for="item in locationTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </div>
        <div class="i">
          <span>ID位置分类</span>
          <el-input v-model="filters.idLocationCategory" clearable />
        </div>
      </div>
      <div class="a">
        <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
        <el-button @click="resetFilters">重置</el-button>
      </div>
    </div>
    <div class="t">
      <div class="tit">ID位置</div>
      <div class="r">
        <el-button class="b" type="primary" :icon="Plus" square @click="createEmptyRow" />
        <div class="rr">
          <el-pagination
            small
            background
            layout="sizes, prev, pager, next"
            :current-page="pagination.currentPage"
            :page-size="pagination.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="pagination.total"
            @current-change="handlePageChange"
            @size-change="handleSizeChange"
          />
          <div class="acts">
            <el-button type="primary" @click="submitCreate">新增</el-button>
            <el-button type="primary" plain @click="editingRowId ? submitEdit() : startEdit()">
              修改
            </el-button>
            <el-button type="danger" plain @click="removeRows">删除</el-button>
          </div>
        </div>
      </div>
      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="tableData"
        border
        class="mt"
        header-cell-class-name="h"
        @selection-change="selectedRows = $event"
      >
        <el-table-column type="selection" width="48" align="center" />
        <el-table-column label="ID位置编码" min-width="140">
          <template #default="{ row }">
            <el-input v-if="edit(row)" v-model="row.idLocationCode" size="small" />
            <span v-else>{{ row.idLocationCode }}</span>
          </template>
        </el-table-column>
        <el-table-column label="ID位置" min-width="140">
          <template #default="{ row }">
            <el-input v-if="edit(row)" v-model="row.idLocationName" size="small" />
            <span v-else>{{ row.idLocationName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="ID位置厂内编码" min-width="150">
          <template #default="{ row }">
            <el-input v-if="edit(row)" v-model="row.idLocationInnerCode" size="small" />
            <span v-else>{{ row.idLocationInnerCode }}</span>
          </template>
        </el-table-column>
        <el-table-column label="位置类型" min-width="110">
          <template #default="{ row }">
            <el-select v-if="edit(row)" v-model="row.locationType" size="small" clearable>
              <el-option
                v-for="item in locationTypeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
            <span v-else>{{ locationTypeLabel(row.locationType) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="ID位置分类" min-width="130">
          <template #default="{ row }">
            <el-input v-if="edit(row)" v-model="row.idLocationCategory" size="small" />
            <span v-else>{{ row.idLocationCategory }}</span>
          </template>
        </el-table-column>
        <el-table-column label="创建人" min-width="100">
          <template #default="{ row }">
            <el-input v-if="edit(row)" v-model="row.creatorUsername" size="small" disabled />
            <span v-else>{{ row.creatorUsername }}</span>
          </template>
        </el-table-column>
        <el-table-column label="创建人姓名" min-width="110">
          <template #default="{ row }">
            <el-input v-if="edit(row)" v-model="row.creatorName" size="small" disabled />
            <span v-else>{{ row.creatorName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="创建日期" min-width="120">
          <template #default="{ row }">
            <el-input v-if="edit(row)" v-model="row.createdAt" size="small" disabled />
            <span v-else>{{ row.createdAt }}</span>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<style scoped>
.p {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.s,
.t {
  border: 1px solid #d9e2ef;
  background: #fff;
}
.s {
  padding: 12px 16px 8px;
}
.g {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px 22px;
}
.i {
  display: grid;
  grid-template-columns: 110px 1fr;
  align-items: center;
  column-gap: 10px;
}
.i > span {
  color: #606266;
  font-size: 13px;
  text-align: right;
}
.a {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 12px;
}
.t {
  padding: 10px 10px 0;
}
.tit {
  margin-bottom: 10px;
  color: #2f5f98;
  font-size: 14px;
  font-weight: 600;
}
.r {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}
.rr,
.acts {
  display: flex;
  align-items: center;
  gap: 12px;
}
.acts {
  gap: 8px;
}
.b {
  width: 28px;
  height: 28px;
  padding: 0;
}
.mt:deep(.h) {
  background: #d9ecff;
  color: #2f5f98;
  font-weight: 600;
}
.mt:deep(.el-table__cell) {
  padding: 6px 0;
}
.mt:deep(.el-input__wrapper),
.mt:deep(.el-select__wrapper) {
  box-shadow: none;
}
</style>
