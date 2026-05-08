<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import {
  addSupplierManage,
  batchDeleteSupplierManage,
  getSupplierManagePage,
  updateSupplierManage
} from '@/api/supplierManage'
import { useUserStore } from '@/stores/user'
import { extractPageData } from '@/util/apiData'

const userStore = useUserStore()
const tableRef = ref()
const tableData = ref([])
const selectedRows = ref([])
const editingRowId = ref('')
const creatingRowId = ref('')
const loading = ref(false)
const filters = reactive({ supplierCode: '', supplierName: '', supplierCategory: '' })
const pagination = reactive({ currentPage: 1, pageSize: 10, total: 0 })

const date = (v) => (v ? String(v).slice(0, 10) : '')
const trim = (v) => String(v || '').trim()
const edit = (r) => editingRowId.value === r.id

function resetSelection() {
  selectedRows.value = []
  tableRef.value && tableRef.value.clearSelection()
}
function userInfo() {
  const p = userStore.profile
  const uid = userStore.userId
  const username = p?.username ? String(p.username) : ''
  const realName = p?.realName && String(p.realName).trim() ? String(p.realName).trim() : username
  return { id: uid != null ? String(uid) : '', username, realName }
}
function mapRow(i = {}) {
  return {
    id: i.id,
    supplierCategory: i.supplierCategory || '',
    supplierCode: i.supplierCode || '',
    supplierName: i.supplierName || '',
    address: i.address || '',
    contactPerson: i.contactPerson || '',
    contactId: i.contactId || '',
    contactEmail: i.contactEmail || '',
    fax: i.fax || '',
    creatorUsername: i.creatorUsername || '',
    creatorName: i.creatorName || '',
    createdAt: date(i.createdAt)
  }
}
async function loadPage() {
  loading.value = true
  try {
    const res = await getSupplierManagePage({
      current: pagination.currentPage,
      size: pagination.pageSize,
      supplierCode: filters.supplierCode,
      supplierName: filters.supplierName,
      supplierCategory: filters.supplierCategory
    })
    const { records, total } = extractPageData(res)
    tableData.value = records.map(mapRow)
    pagination.total = total
  } catch (e) {
    if (!e?.elMessageNotified) ElMessage.error('获取供应商分页数据失败')
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
  Object.assign(filters, { supplierCode: '', supplierName: '', supplierCategory: '' })
  handleSearch()
}
function createEmptyRow() {
  if (creatingRowId.value) return ElMessage.warning('请先完成当前新增行')
  const u = userInfo()
  const d = new Date().toISOString().slice(0, 10)
  tableData.value.unshift({
    id: `draft-${Date.now()}`,
    supplierCategory: '',
    supplierCode: '',
    supplierName: '',
    address: '',
    contactPerson: '',
    contactId: '',
    contactEmail: '',
    fax: '',
    creatorId: u.id,
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
  if (selectedRows.value.length !== 1) return ElMessage.warning('请选择一条需要修改的供应商')
  if (selectedRows.value[0].isNew)
    return ElMessage.warning('未保存的新增行请先点“新增”保存，再编辑')
  editingRowId.value = selectedRows.value[0].id
  creatingRowId.value = ''
}
function valid(row) {
  if (!trim(row.supplierCode)) return (ElMessage.warning('供应商代码不能为空'), false)
  if (!trim(row.supplierName)) return (ElMessage.warning('供应商名称不能为空'), false)
  return true
}
function payload(row, withId = false) {
  const data = {
    supplierCategory: trim(row.supplierCategory),
    supplierCode: trim(row.supplierCode),
    supplierName: trim(row.supplierName),
    address: trim(row.address),
    contactPerson: trim(row.contactPerson),
    contactId: trim(row.contactId),
    contactEmail: trim(row.contactEmail),
    fax: trim(row.fax),
    createdAt: trim(row.createdAt)
  }
  if (withId) data.id = row.id
  return data
}
async function submitCreate() {
  const row = tableData.value.find((item) => item.id === creatingRowId.value)
  if (!row) return ElMessage.warning('当前没有待新增的供应商')
  if (!valid(row)) return
  try {
    await addSupplierManage(payload(row))
    ElMessage.success('新增成功')
    creatingRowId.value = ''
    editingRowId.value = ''
    await loadPage()
  } catch (e) {
    if (!e?.elMessageNotified) ElMessage.error('新增供应商失败')
  }
}
async function submitEdit() {
  if (selectedRows.value.length !== 1) return ElMessage.warning('请选择一条需要修改的供应商')
  const row = selectedRows.value[0]
  if (row.isNew) return ElMessage.warning('未保存的新增行请使用“新增”保存')
  if (!valid(row)) return
  try {
    await updateSupplierManage(payload(row, true))
    ElMessage.success('修改成功')
    editingRowId.value = ''
    await loadPage()
  } catch (e) {
    if (!e?.elMessageNotified) ElMessage.error('修改供应商失败')
  }
}
async function removeRows() {
  if (!selectedRows.value.length) return ElMessage.warning('请至少选择一条供应商')
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
    await batchDeleteSupplierManage(ids)
    ElMessage.success('删除成功')
    creatingRowId.value = ''
    editingRowId.value = ''
    resetSelection()
    await loadPage()
  } catch (e) {
    if (!e?.elMessageNotified) ElMessage.error('删除供应商失败')
  }
}

onMounted(loadPage)
</script>

<template>
  <div class="p">
    <div class="section">
      <div class="section-title">供应商清单</div>
      <div class="s">
        <div class="g supplier-grid">
          <div class="i">
            <span>供应商代码</span>
            <el-input v-model="filters.supplierCode" clearable />
          </div>
          <div class="i">
            <span>供应商名称</span>
            <el-input v-model="filters.supplierName" clearable />
          </div>
          <div class="i">
            <span>供应商分类</span>
            <el-input v-model="filters.supplierCategory" clearable />
          </div>
        </div>
        <div class="a">
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </div>
      </div>
    </div>

    <div class="section">
      <div class="section-title">检索集</div>
      <div class="t">
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
          <el-table-column label="供应商分类" min-width="120">
            <template #default="{ row }">
              <el-input v-if="edit(row)" v-model="row.supplierCategory" size="small" />
              <span v-else>{{ row.supplierCategory }}</span>
            </template>
          </el-table-column>
          <el-table-column label="供应商代码" min-width="120">
            <template #default="{ row }">
              <el-input v-if="edit(row)" v-model="row.supplierCode" size="small" />
              <span v-else>{{ row.supplierCode }}</span>
            </template>
          </el-table-column>
          <el-table-column label="供应商名称" min-width="140">
            <template #default="{ row }">
              <el-input v-if="edit(row)" v-model="row.supplierName" size="small" />
              <span v-else>{{ row.supplierName }}</span>
            </template>
          </el-table-column>
          <el-table-column label="地址" min-width="140">
            <template #default="{ row }">
              <el-input v-if="edit(row)" v-model="row.address" size="small" />
              <span v-else>{{ row.address }}</span>
            </template>
          </el-table-column>
          <el-table-column label="联系人" min-width="110">
            <template #default="{ row }">
              <el-input v-if="edit(row)" v-model="row.contactPerson" size="small" />
              <span v-else>{{ row.contactPerson }}</span>
            </template>
          </el-table-column>
          <el-table-column label="联系人电话" min-width="130">
            <template #default="{ row }">
              <el-input v-if="edit(row)" v-model="row.contactId" size="small" />
              <span v-else>{{ row.contactId }}</span>
            </template>
          </el-table-column>
          <el-table-column label="联系人E-mail" min-width="170">
            <template #default="{ row }">
              <el-input v-if="edit(row)" v-model="row.contactEmail" size="small" />
              <span v-else>{{ row.contactEmail }}</span>
            </template>
          </el-table-column>
          <el-table-column label="传真" min-width="120">
            <template #default="{ row }">
              <el-input v-if="edit(row)" v-model="row.fax" size="small" />
              <span v-else>{{ row.fax }}</span>
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
          <el-table-column label="创建日期" min-width="110">
            <template #default="{ row }">
              <el-input v-if="edit(row)" v-model="row.createdAt" size="small" disabled />
              <span v-else>{{ row.createdAt }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<style scoped>
.p {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.section {
  border: 1px solid #cfe0ef;
  background: #fff;
}
.section-title {
  height: 28px;
  display: flex;
  align-items: center;
  padding: 0 10px;
  font-size: 13px;
  font-weight: 600;
  color: #2f5f98;
  background: #d9ecff;
  border-bottom: 1px solid #cfe0ef;
}
.s {
  padding: 10px 12px 8px;
}
.g {
  display: grid;
  gap: 10px 22px;
}
.supplier-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}
.i {
  display: grid;
  grid-template-columns: 88px 1fr;
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
  margin-top: 10px;
}
.t {
  padding: 8px 10px 0;
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
.mt :deep(.h) {
  background: #d9ecff;
  color: #2f5f98;
  font-weight: 600;
}
.mt :deep(.el-table__cell) {
  padding: 6px 0;
}
.mt :deep(.el-input__wrapper),
.mt :deep(.el-select__wrapper) {
  box-shadow: none;
}
@media (max-width: 1400px) {
  .supplier-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
