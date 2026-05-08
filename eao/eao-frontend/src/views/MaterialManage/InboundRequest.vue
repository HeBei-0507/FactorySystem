<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { getProductionlineList } from '@/api/prodectionLine'
import { getDeviceUtilList } from '@/api/deviceUnit'
import { getMaterialCodePage } from '@/api/materialCode'
import { getWarehouseLocationPage } from '@/api/warehouseLocation'
import {
  addInboundRequest,
  batchDeleteInboundRequest,
  confirmInboundRequest,
  getInboundRequestPage,
  updateInboundRequest
} from '@/api/inboundRequest'
import { useUserStore } from '@/stores/user'
import { extractPageData, extractRecordsArray } from '@/util/apiData'
const us = useUserStore(),
  lines = ref([]),
  lineId = ref(''),
  lineName = ref('未选择生产线'),
  unitNavId = ref(''),
  exp = ref([]),
  rows = ref([]),
  sels = ref([]),
  loading = ref(false),
  editId = ref(''),
  newId = ref(''),
  tableRef = ref(),
  mDlg = ref(false),
  wDlg = ref(false),
  pickRow = ref(null),
  mRows = ref([]),
  wRows = ref([]),
  mSel = ref([]),
  wSel = ref([])
const q = reactive({ inboundNo: '', materialCode: '', inboundStatus: '' }),
  pg = reactive({ currentPage: 1, pageSize: 10, total: 0 })
const ip = ['00-新品', '10-待修待报废', '20-已修复', '30-已报废'],
  it = ['01-备件下机', '02-新件', '03-修复件', '04-机加工'],
  st = ['00-待确认入库', '10-已确认入库']
const er = (r) => editId.value === r.id,
  ux = () => String(us.profile?.username || ''),
  rn = () => String(us.profile?.realName || '') || ux(),
  exok = (id) => {
    if (id && !exp.value.includes(id)) exp.value.push(id)
  },
  exrm = (id) => {
    exp.value = exp.value.filter((x) => x !== id)
  },
  exed = (id) => exp.value.includes(id),
  amt = (r) =>
    (r.inboundAmount = Math.round(Number(r.inboundQty || 0) * Number(r.unitPrice || 0) * 100) / 100)
const mapL = (i) => ({ id: i.id, name: i.lineName, units: [] }),
  mapU = (i) => ({ id: i.id, unitCode: i.unitCode || '', unitName: i.unitName || '' }),
  mapR = (i) => ({
    id: i.id,
    productionLineId: i.productionLineId,
    productionLineName: i.productionLineName || '',
    inboundNo: i.inboundNo || '',
    materialCodeId: i.materialCodeId,
    materialCode: i.materialCode || '',
    materialName: i.materialName || '',
    modelSpecification: i.modelSpecification || '',
    warehouseLocationId: i.warehouseLocationId,
    areaCode: i.areaCode || '',
    locationCode: i.locationCode || '',
    inboundQty: i.inboundQty ?? 1,
    unitPrice: i.unitPrice ?? 0,
    inboundAmount: i.inboundAmount ?? 0,
    inboundStatus: i.inboundStatus || '00-待确认入库',
    inventoryProperty: i.inventoryProperty || '',
    inboundType: i.inboundType || '',
    inboundDate: i.inboundDate || '',
    creatorUsername: i.creatorUsername || '',
    creatorName: i.creatorName || '',
    createdAt: i.createdAt || '',
    isNew: false
  })
async function loadLines() {
  const r = await getProductionlineList()
  lines.value = extractRecordsArray(r?.data)
    .map(mapL)
    .filter((i) => i.id)
  if (lines.value.length && !lineId.value) {
    lineId.value = lines.value[0].id
    lineName.value = lines.value[0].name
    exok(lineId.value)
    await loadUnits(lineId.value)
  }
}
async function loadUnits(id) {
  const line = lines.value.find((i) => i.id === id)
  if (!line) return
  const r = await getDeviceUtilList(id)
  line.units = extractRecordsArray(r?.data).map(mapU)
}
async function loadPage() {
  if (!lineId.value) return
  loading.value = true
  try {
    const r = await getInboundRequestPage({
      current: pg.currentPage,
      size: pg.pageSize,
      productionLineId: lineId.value,
      inboundNo: q.inboundNo,
      materialCode: q.materialCode,
      inboundStatus: q.inboundStatus
    })
    const d = extractPageData(r)
    rows.value = d.records.map(mapR)
    pg.total = d.total
  } catch (e) {
    if (!e?.elMessageNotified) ElMessage.error('获取入库申请失败')
  } finally {
    loading.value = false
  }
}
function selectLine(l) {
  lineId.value = l.id
  lineName.value = l.name
  unitNavId.value = ''
  pg.currentPage = 1
  editId.value = ''
  newId.value = ''
  if (!exed(l.id)) {
    exok(l.id)
    loadUnits(l.id)
  }
  loadPage()
}
async function clickLine(l) {
  const opened = exed(l.id)
  lineId.value = l.id
  lineName.value = l.name
  unitNavId.value = ''
  pg.currentPage = 1
  editId.value = ''
  newId.value = ''
  if (opened) {
    exrm(l.id)
    return
  }
  exok(l.id)
  await loadUnits(l.id)
  loadPage()
}
function clickUnit(l, u) {
  lineId.value = l.id
  lineName.value = l.name
  unitNavId.value = u.id
}
function toggleLine(l) {
  if (exed(l.id)) {
    exrm(l.id)
    return
  }
  exok(l.id)
  loadUnits(l.id)
}
function addRow() {
  rows.value.unshift({
    id: `d-${Date.now()}`,
    productionLineId: lineId.value,
    productionLineName: lineName.value,
    inboundNo: '系统生成',
    materialCodeId: null,
    materialCode: '',
    materialName: '',
    modelSpecification: '',
    warehouseLocationId: null,
    areaCode: '',
    locationCode: '',
    inboundQty: 1,
    unitPrice: 0,
    inboundAmount: 0,
    inboundStatus: '00-待确认入库',
    inventoryProperty: '00-新品',
    inboundType: '02-新件',
    inboundDate: new Date().toISOString().slice(0, 10),
    creatorUsername: ux(),
    creatorName: rn(),
    createdAt: new Date().toISOString().slice(0, 19).replace('T', ' '),
    isNew: true
  })
  newId.value = rows.value[0].id
  editId.value = rows.value[0].id
}
function valid(r) {
  if (!r.materialCodeId) return (ElMessage.warning('请选择物料'), false)
  if (!r.warehouseLocationId) return (ElMessage.warning('请选择库区库位'), false)
  if (Number(r.inboundQty) <= 0) return (ElMessage.warning('入库数量必须大于0'), false)
  return true
}
function payload(r, id) {
  amt(r)
  const d = {
    productionLineId: r.productionLineId,
    materialCodeId: r.materialCodeId,
    materialCode: r.materialCode,
    materialName: r.materialName,
    modelSpecification: r.modelSpecification,
    warehouseLocationId: r.warehouseLocationId,
    areaCode: r.areaCode,
    locationCode: r.locationCode,
    inboundQty: Number(r.inboundQty),
    unitPrice: Number(r.unitPrice),
    inventoryProperty: r.inventoryProperty,
    inboundType: r.inboundType,
    inboundDate: r.inboundDate
  }
  if (id) d.id = r.id
  return d
}
async function saveAdd() {
  const r = rows.value.find((i) => i.id === newId.value)
  if (!r || !valid(r)) return
  await addInboundRequest(payload(r))
  ElMessage.success('新增成功')
  newId.value = ''
  editId.value = ''
  loadPage()
}
async function saveEdit() {
  if (sels.value.length !== 1) return ElMessage.warning('请选择一条数据')
  const r = sels.value[0]
  if (!valid(r)) return
  await updateInboundRequest(payload(r, true))
  ElMessage.success('修改成功')
  editId.value = ''
  loadPage()
}
async function removeRows() {
  const ids = sels.value.filter((i) => !i.isNew).map((i) => i.id)
  if (!ids.length) return ElMessage.warning('请至少选择一条已保存数据')
  await batchDeleteInboundRequest(ids)
  ElMessage.success('删除成功')
  loadPage()
}
async function confirmRows() {
  const ids = sels.value.filter((i) => !i.isNew).map((i) => i.id)
  if (!ids.length) return ElMessage.warning('请至少选择一条已保存数据')
  await confirmInboundRequest(ids)
  ElMessage.success('确认入库成功')
  loadPage()
}
async function openMaterial(r) {
  pickRow.value = r
  mDlg.value = true
  const x = await getMaterialCodePage({ current: 1, size: 10, status: '02-激活' })
  mRows.value = extractPageData(x).records
}
async function openWarehouse(r) {
  pickRow.value = r
  wDlg.value = true
  const x = await getWarehouseLocationPage({ current: 1, size: 10, productionLineId: lineId.value })
  wRows.value = extractPageData(x).records
}
function pickMaterial() {
  if (mSel.value.length !== 1) return ElMessage.warning('请选择一条物料')
  const m = mSel.value[0]
  if (m.status !== '02-激活') return ElMessage.warning('仅状态为02-激活的物料允许选择')
  pickRow.value.materialCodeId = m.id
  pickRow.value.materialCode = m.materialCode
  pickRow.value.materialName = m.materialName
  pickRow.value.modelSpecification = m.modelSpecification || ''
  mDlg.value = false
}
function pickWarehouse() {
  if (wSel.value.length !== 1) return ElMessage.warning('请选择一条库区库位')
  const w = wSel.value[0]
  if (Number(w.productionLineId) !== Number(pickRow.value.productionLineId))
    return ElMessage.warning('所选库区库位所属生产线与当前入库申请所属生产线不一致')
  pickRow.value.warehouseLocationId = w.id
  pickRow.value.areaCode = w.areaCode
  pickRow.value.locationCode = w.locationCode
  wDlg.value = false
}
onMounted(async () => {
  await loadLines()
  await loadPage()
})
</script>
<template>
  <div class="p">
    <div class="c">
      <aside class="l">
        <div class="h">入库申请新增</div>
        <div class="lt">
          <div v-for="l in lines" :key="l.id">
            <div :class="['li', { a: l.id === lineId }]">
              <span class="line-label" @click="selectLine(l)">{{ l.name }}</span>
              <span class="line-arrow" @click.stop="toggleLine(l)">
                {{ exed(l.id) ? '▾' : '▸' }}
              </span>
            </div>
            <div v-if="exed(l.id)" class="ul">
              <div
                v-for="u in l.units"
                :key="u.id"
                :class="['ui', { a: u.id === unitNavId }]"
                @click="clickUnit(l, u)"
              >
                {{ u.unitCode }} {{ u.unitName }}
              </div>
            </div>
          </div>
        </div>
      </aside>
      <section class="r">
        <div class="bar">
          <el-input v-model="q.inboundNo" size="default" placeholder="入库单号" />
          <el-input v-model="q.materialCode" size="default" placeholder="物料代码" />
          <el-select v-model="q.inboundStatus" size="default" clearable placeholder="入库状态">
            <el-option v-for="x in st" :key="x" :label="x" :value="x" />
          </el-select>
          <el-button type="primary" :icon="Search" @click="loadPage">查询</el-button>
        </div>
        <div class="tools">
          <el-button class="plus" type="primary" :icon="Plus" @click="addRow" />
          <div class="tool-actions">
            <el-button class="main-btn" type="primary" @click="saveAdd">新增</el-button>
            <el-button class="main-btn" type="primary" plain @click="saveEdit">修改</el-button>
            <el-button class="main-btn" type="danger" plain @click="removeRows">删除</el-button>
            <el-button class="main-btn" type="warning" plain @click="confirmRows">
              确认入库
            </el-button>
          </div>
        </div>
        <div class="table-wrap">
          <el-table
            ref="tableRef"
            v-loading="loading"
            :data="rows"
            size="small"
            border
            @selection-change="(v) => (sels = v)"
          >
            <el-table-column type="selection" width="46" />
            <el-table-column label="生产线" prop="productionLineName" min-width="110" />
            <el-table-column label="入库单号" prop="inboundNo" min-width="120" />
            <el-table-column label="物料代码" min-width="120">
              <template #default="{ row }">
                <el-input v-if="er(row)" v-model="row.materialCode" readonly>
                  <template #append>
                    <el-button :icon="Search" @click="openMaterial(row)" />
                  </template>
                </el-input>
                <span v-else>{{ row.materialCode }}</span>
              </template>
            </el-table-column>
            <el-table-column label="物料名称" prop="materialName" min-width="110" />
            <el-table-column label="型号规格" prop="modelSpecification" min-width="110" />
            <el-table-column label="库区" min-width="90">
              <template #default="{ row }">
                <el-input v-if="er(row)" v-model="row.areaCode" readonly>
                  <template #append>
                    <el-button :icon="Search" @click="openWarehouse(row)" />
                  </template>
                </el-input>
                <span v-else>{{ row.areaCode }}</span>
              </template>
            </el-table-column>
            <el-table-column label="库位" prop="locationCode" min-width="80" />
            <el-table-column label="入库数量" min-width="90">
              <template #default="{ row }">
                <el-input-number
                  v-if="er(row)"
                  v-model="row.inboundQty"
                  :min="1"
                  size="small"
                  @change="amt(row)"
                />
                <span v-else>{{ row.inboundQty }}</span>
              </template>
            </el-table-column>
            <el-table-column label="单价" min-width="90">
              <template #default="{ row }">
                <el-input-number
                  v-if="er(row)"
                  v-model="row.unitPrice"
                  :min="0"
                  :precision="2"
                  size="small"
                  @change="amt(row)"
                />
                <span v-else>{{ row.unitPrice }}</span>
              </template>
            </el-table-column>
            <el-table-column label="入库金额" prop="inboundAmount" min-width="90" />
            <el-table-column label="入库状态" prop="inboundStatus" min-width="110" />
            <el-table-column label="库存属性" min-width="110">
              <template #default="{ row }">
                <el-select v-if="er(row)" v-model="row.inventoryProperty" size="small">
                  <el-option v-for="x in ip" :key="x" :label="x" :value="x" />
                </el-select>
                <span v-else>{{ row.inventoryProperty }}</span>
              </template>
            </el-table-column>
            <el-table-column label="入库类型" min-width="110">
              <template #default="{ row }">
                <el-select v-if="er(row)" v-model="row.inboundType" size="small">
                  <el-option v-for="x in it" :key="x" :label="x" :value="x" />
                </el-select>
                <span v-else>{{ row.inboundType }}</span>
              </template>
            </el-table-column>
            <el-table-column label="入库日期" min-width="110">
              <template #default="{ row }">
                <el-date-picker
                  v-if="er(row)"
                  v-model="row.inboundDate"
                  type="date"
                  value-format="YYYY-MM-DD"
                  size="small"
                />
                <span v-else>{{ row.inboundDate }}</span>
              </template>
            </el-table-column>
            <el-table-column label="创建人" prop="creatorUsername" min-width="90" />
            <el-table-column label="创建人姓名" prop="creatorName" min-width="100" />
            <el-table-column label="创建日期" prop="createdAt" min-width="150" />
          </el-table>
        </div>
      </section>
    </div>
    <el-dialog v-model="mDlg" title="选择物料" width="800px">
      <el-table :data="mRows" border @selection-change="(v) => (mSel = v)">
        <el-table-column type="selection" width="46" />
        <el-table-column label="物料代码" prop="materialCode" />
        <el-table-column label="物料名称" prop="materialName" />
        <el-table-column label="型号规格" prop="modelSpecification" />
        <el-table-column label="状态" prop="status" />
      </el-table>
      <div class="df">
        <el-button @click="mDlg = false">取消</el-button>
        <el-button type="primary" @click="pickMaterial">确认</el-button>
      </div>
    </el-dialog>
    <el-dialog v-model="wDlg" title="选择库区库位" width="800px">
      <el-table :data="wRows" border @selection-change="(v) => (wSel = v)">
        <el-table-column type="selection" width="46" />
        <el-table-column label="生产线" prop="productionLineName" />
        <el-table-column label="库区" prop="areaCode" />
        <el-table-column label="库位" prop="locationCode" />
        <el-table-column label="库管员" prop="keeperUsername" />
        <el-table-column label="库管员姓名" prop="keeperRealName" />
      </el-table>
      <div class="df">
        <el-button @click="wDlg = false">取消</el-button>
        <el-button type="primary" @click="pickWarehouse">确认</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<style scoped>
.p {
  padding: 12px;
  background: #f5f8fc;
  height: calc(100vh - 120px);
  overflow: hidden;
}
.c {
  display: flex;
  gap: 12px;
  height: 100%;
  min-height: 0;
}
.l {
  width: 250px;
  flex: 0 0 250px;
  background: #fff;
  border: 1px solid #d9e2ef;
  overflow: auto;
}
.h {
  padding: 12px;
  font-size: 14px;
  font-weight: 600;
  background: #eef6ff;
  border-bottom: 1px solid #d9e2ef;
}
.lt {
  padding: 10px;
}
.li,
.ui {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 10px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
  line-height: 1.6;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.line-label {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
}
.line-arrow {
  flex: 0 0 auto;
  width: 18px;
  text-align: center;
}
.ui {
  justify-content: flex-start;
}
.a,
.li.a,
.ui.a {
  background: #eaf3ff;
  color: #1d6fd8;
}
.ul {
  margin-left: 18px;
}
.r {
  min-width: 0;
  flex: 1;
  background: #fff;
  border: 1px solid #d9e2ef;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  overflow: hidden;
}
.bar,
.tools,
.df {
  display: flex;
  gap: 12px;
  align-items: center;
}
.bar {
  flex-wrap: wrap;
}
.bar .el-input,
.bar .el-select {
  width: 280px;
}
.tools {
  justify-content: space-between;
}
.tool-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: flex-end;
}
.main-btn {
  min-width: 76px;
  height: 34px;
}
.table-wrap {
  flex: 1;
  min-height: 0;
  overflow: auto;
}
.plus {
  width: 32px;
  height: 32px;
  padding: 0;
}
.df {
  justify-content: flex-end;
  padding-top: 12px;
}
</style>
