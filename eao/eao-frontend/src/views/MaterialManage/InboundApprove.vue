<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getProductionlineList } from '@/api/prodectionLine'
import { confirmInboundRequest, getInboundApprovePage } from '@/api/inboundRequest'
import { extractPageData, extractRecordsArray } from '@/util/apiData'

const lines = ref([])
const loading = ref(false)
const rows = ref([])
const sels = ref([])
const q = reactive({ productionLineId: '', inboundNo: '', materialCode: '', inboundStatus: '00-待确认入库' })
const pg = reactive({ currentPage: 1, pageSize: 10, total: 0 })
const pageSizes = [10, 20, 50, 100]
const st = ['00-待确认入库', '10-已确认入库']

const mapLine = (i) => ({ id: i.id, name: i.lineName || i.name || '' })
const mapRow = (i) => ({
  id: i.id,
  productionLineName: i.productionLineName || '',
  inboundNo: i.inboundNo || '',
  materialCode: i.materialCode || '',
  materialName: i.materialName || '',
  modelSpecification: i.modelSpecification || '',
  areaCode: i.areaCode || '',
  locationCode: i.locationCode || '',
  inboundQty: i.inboundQty ?? 0,
  unitPrice: i.unitPrice ?? 0,
  inboundAmount: i.inboundAmount ?? 0,
  inboundStatus: i.inboundStatus || '',
  inventoryProperty: i.inventoryProperty || '',
  inboundType: i.inboundType || '',
  inboundDate: i.inboundDate || '',
  creatorUsername: i.creatorUsername || '',
  creatorName: i.creatorName || '',
  createdAt: i.createdAt || ''
})

async function loadLines() {
  try {
    const r = await getProductionlineList()
    lines.value = extractRecordsArray(r?.data).map(mapLine)
    if (lines.value.length && !q.productionLineId) q.productionLineId = lines.value[0].id
  } catch (e) {
    if (!e?.elMessageNotified) ElMessage.error('获取生产线失败')
  }
}

async function loadPage() {
  loading.value = true
  try {
    const r = await getInboundApprovePage({
      current: pg.currentPage,
      size: pg.pageSize,
      productionLineId: q.productionLineId || undefined,
      inboundNo: q.inboundNo || undefined,
      materialCode: q.materialCode || undefined,
      inboundStatus: q.inboundStatus || undefined
    })
    const d = extractPageData(r)
    rows.value = (d.records || []).map(mapRow)
    pg.total = d.total || 0
  } catch (e) {
    if (!e?.elMessageNotified) ElMessage.error('获取入库审核列表失败')
  } finally {
    loading.value = false
  }
}

function search() {
  pg.currentPage = 1
  loadPage()
}

async function confirmRows() {
  const ids = sels.value.filter((i) => i.inboundStatus === '00-待确认入库').map((i) => i.id)
  if (!ids.length) return ElMessage.warning('请至少选择一条待确认入库数据')
  await confirmInboundRequest(ids)
  ElMessage.success('确认入库成功')
  await loadPage()
}

function handlePageChange(p) {
  pg.currentPage = p
  loadPage()
}

function handleSizeChange(s) {
  pg.pageSize = s
  pg.currentPage = 1
  loadPage()
}

onMounted(async () => {
  await loadLines()
  await loadPage()
})
</script>

<template>
  <div class="p">
    <div class="bar">
      <el-select v-model="q.productionLineId" clearable placeholder="生产线" style="width: 220px">
        <el-option v-for="x in lines" :key="x.id" :label="x.name" :value="x.id" />
      </el-select>
      <el-input v-model="q.inboundNo" placeholder="入库单号" />
      <el-input v-model="q.materialCode" placeholder="物料代码" />
      <el-select v-model="q.inboundStatus" clearable placeholder="入库状态" style="width: 180px">
        <el-option v-for="x in st" :key="x" :label="x" :value="x" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="search">查询</el-button>
      <el-button type="success" @click="confirmRows">确认入库</el-button>
    </div>

    <div class="table-wrap">
      <el-table v-loading="loading" :data="rows" size="small" border @selection-change="(v) => (sels = v)">
        <el-table-column type="selection" width="46" />
        <el-table-column label="生产线" prop="productionLineName" min-width="110" />
        <el-table-column label="入库单号" prop="inboundNo" min-width="120" />
        <el-table-column label="物料代码" prop="materialCode" min-width="120" />
        <el-table-column label="物料名称" prop="materialName" min-width="110" />
        <el-table-column label="型号规格" prop="modelSpecification" min-width="110" />
        <el-table-column label="库区" prop="areaCode" min-width="90" />
        <el-table-column label="库位" prop="locationCode" min-width="80" />
        <el-table-column label="入库数量" prop="inboundQty" min-width="90" />
        <el-table-column label="单价" prop="unitPrice" min-width="90" />
        <el-table-column label="入库金额" prop="inboundAmount" min-width="100" />
        <el-table-column label="入库状态" prop="inboundStatus" min-width="110" />
        <el-table-column label="库存属性" prop="inventoryProperty" min-width="110" />
        <el-table-column label="入库类型" prop="inboundType" min-width="110" />
        <el-table-column label="入库日期" prop="inboundDate" min-width="110" />
        <el-table-column label="创建人" prop="creatorUsername" min-width="90" />
        <el-table-column label="创建人姓名" prop="creatorName" min-width="100" />
        <el-table-column label="创建日期" prop="createdAt" min-width="150" />
      </el-table>
    </div>

    <div class="pg">
      <el-pagination
        v-model:current-page="pg.currentPage"
        v-model:page-size="pg.pageSize"
        :page-sizes="pageSizes"
        background
        layout="total, sizes, prev, pager, next"
        :total="pg.total"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </div>
  </div>
</template>

<style scoped>
.p { padding: 12px; background: #f5f8fc; min-height: calc(100vh - 120px); }
.bar { display: flex; gap: 10px; align-items: center; margin-bottom: 12px; flex-wrap: wrap; }
.table-wrap { background: #fff; border: 1px solid #d9e2ef; }
.pg { display: flex; justify-content: flex-end; margin-top: 12px; }
</style>
