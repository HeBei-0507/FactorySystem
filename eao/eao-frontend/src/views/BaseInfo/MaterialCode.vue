<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import {
  addMaterialCode,
  batchDeleteMaterialCode,
  getMaterialCodePage,
  updateMaterialCode
} from '@/api/materialCode'
import { useUserStore } from '@/stores/user'
import { extractPageData } from '@/util/apiData'
const userStore = useUserStore()
const tableRef = ref(),
  tableData = ref([]),
  selectedRows = ref([]),
  editingRowId = ref(''),
  creatingRowId = ref(''),
  loading = ref(false)
const filters = reactive({
  materialCode: '',
  materialName: '',
  materialMajorCategory: '',
  materialSubCategory: '',
  materialProperty: '',
  modelSpecification: '',
  status: ''
})
const pagination = reactive({ currentPage: 1, pageSize: 10, total: 0 })
const materialMajorCategoryOptions = [
  { value: '自制', label: '自制' },
  { value: '外购', label: '外购' }
]
const materialSubCategoryOptions = [
  { value: '01-备件', label: '01-备件' },
  { value: '02-材料', label: '02-材料' },
  { value: '03-工器具', label: '03-工器具' }
]
const materialPropertyOptions = [
  { value: '01-易损件', label: '01-易损件' },
  { value: '02-周转件', label: '02-周转件' },
  { value: '03-事故件', label: '03-事故件' },
  { value: '04-材料', label: '04-材料' },
  { value: '05-工器具', label: '05-工器具' }
]
const statusOptions = [
  { value: '01-冻结', label: '01-冻结' },
  { value: '02-激活', label: '02-激活' }
]
const date = (v) => (v ? String(v).slice(0, 10) : '')
const edit = (r) => editingRowId.value === r.id
const editOld = (r) => edit(r) && !r.isNew
const trim = (v) => String(v || '').trim()
function resetSelection() {
  selectedRows.value = []
  tableRef.value && tableRef.value.clearSelection()
}
function userInfo() {
  const p = userStore.profile,
    u = userStore.userId,
    n = p?.username ? String(p.username) : '',
    rn = p?.realName && String(p.realName).trim() ? String(p.realName).trim() : n
  return { id: u != null ? String(u) : '', u: n, n: rn }
}
function mapRow(i) {
  return {
    id: i.id,
    materialCode: i.materialCode || '',
    materialName: i.materialName || '',
    materialDescription: i.materialDescription || '',
    modelSpecification: i.modelSpecification || '',
    drawingNo: i.drawingNo || '',
    materialMajorCategory: i.materialMajorCategory || '',
    materialSubCategory: i.materialSubCategory || '',
    materialProperty: i.materialProperty || '',
    status: i.status || '',
    creatorId: String(i.creatorId ?? ''),
    creatorName: i.creatorName || '',
    creatorUsername: i.creatorUsername || '',
    createdAt: date(i.createdAt),
    modifierName: i.modifierName || '',
    modifierUsername: i.modifierUsername || '',
    modifiedAt: date(i.modifiedAt)
  }
}
async function loadPage() {
  loading.value = true
  try {
    const res = await getMaterialCodePage({
      current: pagination.currentPage,
      size: pagination.pageSize,
      materialCode: filters.materialCode,
      materialName: filters.materialName,
      materialMajorCategory: filters.materialMajorCategory,
      materialSubCategory: filters.materialSubCategory,
      materialProperty: filters.materialProperty,
      modelSpecification: filters.modelSpecification,
      status: filters.status
    })
    const { records, total } = extractPageData(res)
    tableData.value = records.map(mapRow)
    pagination.total = total
  } catch (e) {
    if (!e?.elMessageNotified) ElMessage.error('获取物料代码分页数据失败')
  } finally {
    loading.value = false
  }
}
function handleSearch() {
  pagination.currentPage = 1
  resetSelection()
  loadPage()
}
function handlePageChange(p) {
  pagination.currentPage = p
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
    materialCode: '',
    materialName: '',
    materialMajorCategory: '',
    materialSubCategory: '',
    materialProperty: '',
    modelSpecification: '',
    status: ''
  })
  handleSearch()
}
function createEmptyRow() {
  if (creatingRowId.value) return ElMessage.warning('请先完成当前新增行')
  const u = userInfo(),
    d = new Date().toISOString().slice(0, 10)
  tableData.value.unshift({
    id: `draft-${Date.now()}`,
    materialCode: '',
    materialName: '',
    materialDescription: '',
    modelSpecification: '',
    drawingNo: '',
    materialMajorCategory: '',
    materialSubCategory: '',
    materialProperty: '',
    status: '02-激活',
    creatorId: u.id,
    creatorUsername: u.u,
    creatorName: u.n,
    createdAt: d,
    modifierUsername: u.u,
    modifierName: u.n,
    modifiedAt: d,
    isNew: true
  })
  creatingRowId.value = tableData.value[0].id
  editingRowId.value = tableData.value[0].id
  resetSelection()
}
function startEdit() {
  if (selectedRows.value.length !== 1) return ElMessage.warning('请选择一条需要修改的物料代码')
  if (selectedRows.value[0].isNew)
    return ElMessage.warning('未保存的新增行请先点“新增”保存，再编辑')
  editingRowId.value = selectedRows.value[0].id
  creatingRowId.value = ''
}
function valid(r) {
  if (!trim(r.materialCode)) return (ElMessage.warning('物料代码不能为空'), false)
  if (!trim(r.materialName)) return (ElMessage.warning('物料名称不能为空'), false)
  return true
}
function payload(r, id = false) {
  const d = {
    materialCode: trim(r.materialCode),
    materialName: trim(r.materialName),
    materialDescription: trim(r.materialDescription),
    modelSpecification: trim(r.modelSpecification),
    drawingNo: trim(r.drawingNo),
    materialMajorCategory: trim(r.materialMajorCategory),
    materialSubCategory: trim(r.materialSubCategory),
    materialProperty: trim(r.materialProperty),
    status: trim(r.status)
  }
  if (id) d.id = r.id
  return d
}
async function submitCreate() {
  const row = tableData.value.find((i) => i.id === creatingRowId.value)
  if (!row) return ElMessage.warning('当前没有待新增的物料代码')
  if (!valid(row)) return
  try {
    await addMaterialCode(payload(row))
    ElMessage.success('新增成功')
    creatingRowId.value = ''
    editingRowId.value = ''
    await loadPage()
  } catch (e) {
    if (!e?.elMessageNotified) ElMessage.error('新增物料代码失败')
  }
}
async function submitEdit() {
  if (selectedRows.value.length !== 1) return ElMessage.warning('请选择一条需要修改的物料代码')
  const row = selectedRows.value[0]
  if (row.isNew) return ElMessage.warning('未保存的新增行请使用“新增”保存')
  if (!valid(row)) return
  try {
    await updateMaterialCode(payload(row, true))
    ElMessage.success('修改成功')
    editingRowId.value = ''
    await loadPage()
  } catch (e) {
    if (!e?.elMessageNotified) ElMessage.error('修改物料代码失败')
  }
}
async function removeRows() {
  if (!selectedRows.value.length) return ElMessage.warning('请至少选择一条物料代码')
  const ids = selectedRows.value.filter((i) => !i.isNew).map((i) => i.id),
    drafts = new Set(selectedRows.value.filter((i) => i.isNew).map((i) => i.id))
  tableData.value = tableData.value.filter((i) => !drafts.has(i.id))
  if (!ids.length) {
    resetSelection()
    creatingRowId.value = ''
    editingRowId.value = ''
    return ElMessage.success('已删除未保存的新增行')
  }
  try {
    await batchDeleteMaterialCode(ids)
    ElMessage.success('删除成功')
    creatingRowId.value = ''
    editingRowId.value = ''
    resetSelection()
    await loadPage()
  } catch (e) {
    if (!e?.elMessageNotified) ElMessage.error('删除物料代码失败')
  }
}
onMounted(loadPage)
</script>
<template>
  <div class="p">
    <div class="s">
      <div class="g">
        <div class="i">
          <span>物料代码</span>
          <el-input v-model="filters.materialCode" clearable />
        </div>
        <div class="i">
          <span>物料名称</span>
          <el-input v-model="filters.materialName" clearable />
        </div>
        <div class="i">
          <span>物料属性</span>
          <el-select v-model="filters.materialProperty" clearable placeholder="请选择物料属性">
            <el-option
              v-for="x in materialPropertyOptions"
              :key="x.value"
              :label="x.label"
              :value="x.value"
            />
          </el-select>
        </div>
        <div class="i">
          <span>型号规格</span>
          <el-input v-model="filters.modelSpecification" clearable />
        </div>
        <div class="i">
          <span>物料类别</span>
          <el-select v-model="filters.materialMajorCategory" clearable placeholder="请选择物料类别">
            <el-option
              v-for="x in materialMajorCategoryOptions"
              :key="x.value"
              :label="x.label"
              :value="x.value"
            />
          </el-select>
        </div>
        <div class="i">
          <span>物料分类</span>
          <el-select v-model="filters.materialSubCategory" clearable placeholder="请选择物料分类">
            <el-option
              v-for="x in materialSubCategoryOptions"
              :key="x.value"
              :label="x.label"
              :value="x.value"
            />
          </el-select>
        </div>
        <div class="i">
          <span>状态</span>
          <el-select v-model="filters.status" clearable placeholder="请选择状态">
            <el-option
              v-for="x in statusOptions"
              :key="x.value"
              :label="x.label"
              :value="x.value"
            />
          </el-select>
        </div>
      </div>
      <div class="a">
        <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
        <el-button @click="resetFilters">重置</el-button>
      </div>
    </div>
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
        <el-table-column label="物料代码" min-width="120">
          <template #default="{ row }">
            <el-input v-if="edit(row)" v-model="row.materialCode" size="small" />
            <span v-else>{{ row.materialCode }}</span>
          </template>
        </el-table-column>
        <el-table-column label="物料名称" min-width="150">
          <template #default="{ row }">
            <el-input v-if="edit(row)" v-model="row.materialName" size="small" />
            <span v-else>{{ row.materialName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="物料描述" min-width="120">
          <template #default="{ row }">
            <el-input v-if="edit(row)" v-model="row.materialDescription" size="small" />
            <span v-else>{{ row.materialDescription }}</span>
          </template>
        </el-table-column>
        <el-table-column label="型号规格" min-width="120">
          <template #default="{ row }">
            <el-input v-if="edit(row)" v-model="row.modelSpecification" size="small" />
            <span v-else>{{ row.modelSpecification }}</span>
          </template>
        </el-table-column>
        <el-table-column label="图号" min-width="120">
          <template #default="{ row }">
            <el-input v-if="edit(row)" v-model="row.drawingNo" size="small" />
            <span v-else>{{ row.drawingNo }}</span>
          </template>
        </el-table-column>
        <el-table-column label="物料类别" min-width="120">
          <template #default="{ row }">
            <el-select v-if="edit(row)" v-model="row.materialMajorCategory" size="small">
              <el-option
                v-for="x in materialMajorCategoryOptions"
                :key="x.value"
                :label="x.label"
                :value="x.value"
              />
            </el-select>
            <span v-else>{{ row.materialMajorCategory }}</span>
          </template>
        </el-table-column>
        <el-table-column label="物料分类" min-width="120">
          <template #default="{ row }">
            <el-select v-if="edit(row)" v-model="row.materialSubCategory" size="small">
              <el-option
                v-for="x in materialSubCategoryOptions"
                :key="x.value"
                :label="x.label"
                :value="x.value"
              />
            </el-select>
            <span v-else>{{ row.materialSubCategory }}</span>
          </template>
        </el-table-column>
        <el-table-column label="物料属性" min-width="120">
          <template #default="{ row }">
            <el-select v-if="edit(row)" v-model="row.materialProperty" size="small">
              <el-option
                v-for="x in materialPropertyOptions"
                :key="x.value"
                :label="x.label"
                :value="x.value"
              />
            </el-select>
            <span v-else>{{ row.materialProperty }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="90">
          <template #default="{ row }">
            <el-select v-if="edit(row)" v-model="row.status" size="small">
              <el-option
                v-for="x in statusOptions"
                :key="x.value"
                :label="x.label"
                :value="x.value"
              />
            </el-select>
            <span v-else>{{ row.status }}</span>
          </template>
        </el-table-column>
        <el-table-column label="创建人" min-width="100">
          <template #default="{ row }">
            <el-input
              v-if="edit(row) && row.isNew"
              v-model="row.creatorUsername"
              size="small"
              disabled
            />
            <el-input
              v-else-if="editOld(row)"
              :model-value="row.creatorUsername || row.creatorName"
              size="small"
              disabled
            />
            <span v-else>{{ row.creatorUsername || row.creatorName }}</span>
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
        <el-table-column label="修改人" min-width="100">
          <template #default="{ row }">
            <span>{{ row.modifierUsername || row.modifierName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="修改人姓名" min-width="110">
          <template #default="{ row }">
            <span>{{ row.modifierName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="修改日期" min-width="110">
          <template #default="{ row }">
            <span>{{ row.modifiedAt }}</span>
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
  margin-top: 12px;
}
.t {
  padding: 10px 10px 0;
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
@media (max-width: 1400px) {
  .g {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
