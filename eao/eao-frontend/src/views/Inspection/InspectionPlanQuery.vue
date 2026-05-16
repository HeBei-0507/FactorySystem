<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getInspectionPlanSummaryPage } from '@/api/inspectionRecord'
import { getProductionlineList } from '@/api/prodectionLine'
import { extractPageData, extractRecordsArray } from '@/util/apiData'

const loading = ref(false)
const lineOptions = ref([])
const rows = ref([])
const pageSizes = [10, 20, 50, 100]
const filters = reactive({ productionLineId: '', planSource: '', routeName: '' })
const pagination = reactive({ current: 1, size: 10, total: 0 })
const planSourceOptions = [{ value: 'DJ', label: 'DJ-点检' }]

const currentPageTotal = computed(() => rows.value.reduce((sum, item) => sum + Number(item.totalCount || 0), 0))

function mapLine(item) {
  return { id: item.id, name: item.lineName || item.name || '' }
}

async function loadLines() {
  try {
    const res = await getProductionlineList()
    lineOptions.value = extractRecordsArray(res?.data).map(mapLine)
  } catch (error) {
    if (!error?.elMessageNotified) ElMessage.error('获取点检路线名称失败')
  }
}

async function loadTable() {
  loading.value = true
  try {
    const res = await getInspectionPlanSummaryPage({
      current: pagination.current,
      size: pagination.size,
      productionLineId: filters.productionLineId || undefined,
      planSource: filters.planSource || undefined,
      routeName: filters.routeName || undefined,
    })
    const data = extractPageData(res)
    rows.value = data.records || []
    pagination.total = data.total || 0
  } catch (error) {
    if (!error?.elMessageNotified) ElMessage.error('获取点检计划查询结果失败')
  } finally {
    loading.value = false
  }
}

function search() {
  pagination.current = 1
  loadTable()
}

function resetFilters() {
  Object.assign(filters, { productionLineId: '', planSource: '', routeName: '' })
  search()
}

function handlePageChange(current) {
  pagination.current = current
  loadTable()
}

function handleSizeChange(size) {
  pagination.size = size
  pagination.current = 1
  loadTable()
}

onMounted(async () => {
  await loadLines()
  await loadTable()
})
</script>

<template>
  <div class="inspection-plan-page">
    <el-card shadow="never" class="search-card">
      <template #header>
        <span>查询条件</span>
      </template>
      <el-form inline>
        <el-form-item label="点检路线名称">
          <el-input v-model="filters.routeName" clearable placeholder="请输入点检路线名称" />
        </el-form-item>
        <el-form-item label="计划来源">
          <el-select v-model="filters.planSource" clearable placeholder="请选择计划来源" style="width: 180px">
            <el-option v-for="item in planSourceOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="产线">
          <el-select v-model="filters.productionLineId" clearable placeholder="请选择产线" style="width: 220px">
            <el-option v-for="item in lineOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <template #header>
        <span>今日任务</span>
      </template>
      <el-table v-loading="loading" :data="rows" border>
        <el-table-column type="selection" width="48" />
        <el-table-column prop="planSource" label="计划来源" min-width="120" />
        <el-table-column prop="routeName" label="点检路线名称" min-width="220" />
        <el-table-column prop="routeCode" label="路线编号" min-width="140" />
        <el-table-column prop="totalCount" label="总数" min-width="100" />
        <el-table-column prop="inspectedCount" label="已检数量" min-width="120" />
        <el-table-column prop="remainingCount" label="剩余数量" min-width="120" />
      </el-table>
      <div class="footer-row">
        <span class="current-total">当前页总数：{{ currentPageTotal }}</span>
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="pageSizes"
          background
          layout="total, sizes, prev, pager, next"
          :total="pagination.total"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.inspection-plan-page {
  padding: 16px;
}

.table-card {
  margin-top: 12px;
}

.footer-row {
  margin-top: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.current-total {
  color: #606266;
  font-size: 14px;
}
</style>
