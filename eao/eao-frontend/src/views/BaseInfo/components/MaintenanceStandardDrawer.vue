<script setup>
const props = defineProps({
  modelValue: Boolean,
  form: { type: Object, required: true },
  tabsValue: { type: String, required: true },
  workCategoryOptions: { type: Array, default: () => [] },
  acceptanceLevelOptions: { type: Array, default: () => [] },
  maintenanceCategoryOptions: { type: Array, default: () => [] },
  professionOptions: { type: Array, default: () => [] },
  cycleUnitOptions: { type: Array, default: () => [] },
  tagNatureOptions: { type: Array, default: () => [] },
  tagLocationOptions: { type: Array, default: () => [] }
})
const emit = defineEmits([
  'update:modelValue',
  'update:tabsValue',
  'save',
  'open-material',
  'add-tag',
  'remove-item'
])
</script>

<template>
  <el-drawer
    :model-value="modelValue"
    title="检修标准明细"
    size="72%"
    @update:model-value="(v) => emit('update:modelValue', v)"
  >
    <div class="grid3">
      <div class="field-item">
        <div class="field-label">标准项目编码</div>
        <el-input v-model="form.standardCode" disabled placeholder="系统生成" />
      </div>
      <div class="field-item">
        <div class="field-label">设备部位编码</div>
        <el-input v-model="form.partCode" disabled placeholder="设备部位编码" />
      </div>
      <div class="field-item">
        <div class="field-label">设备部位名称</div>
        <el-input v-model="form.partName" disabled placeholder="设备部位名称" />
      </div>

      <div class="field-item">
        <div class="field-label">标准项目名称</div>
        <el-input v-model="form.itemName" placeholder="标准项目名称" />
      </div>
      <div class="field-item">
        <div class="field-label">风险因素</div>
        <el-input v-model="form.riskFactor" placeholder="风险因素" />
      </div>
      <div class="field-item">
        <div class="field-label">安全防范措施</div>
        <el-input v-model="form.safetyMeasure" placeholder="安全防范措施" />
      </div>

      <div class="field-item">
        <div class="field-label">工事分类</div>
        <el-select v-model="form.workCategory" placeholder="工事分类">
          <el-option v-for="i in workCategoryOptions" :key="i" :label="i" :value="i" />
        </el-select>
      </div>
      <div class="field-item">
        <div class="field-label">工事内容</div>
        <el-input v-model="form.workContent" placeholder="工事内容" />
      </div>
      <div class="field-item">
        <div class="field-label">验收等级</div>
        <el-select v-model="form.acceptanceLevel" placeholder="验收等级">
          <el-option v-for="i in acceptanceLevelOptions" :key="i" :label="i" :value="i" />
        </el-select>
      </div>

      <div class="field-item">
        <div class="field-label">检修分类</div>
        <el-select v-model="form.maintenanceCategory" placeholder="检修分类">
          <el-option v-for="i in maintenanceCategoryOptions" :key="i" :label="i" :value="i" />
        </el-select>
      </div>
      <div class="field-item">
        <div class="field-label">专业</div>
        <el-select v-model="form.profession" placeholder="专业">
          <el-option v-for="i in professionOptions" :key="i" :label="i" :value="i" />
        </el-select>
      </div>
      <div class="field-item inline-cycle">
        <div class="field-label">周期值</div>
        <el-input-number v-model="form.cycleValue" :min="1" />
      </div>

      <div class="field-item">
        <div class="field-label">周期单位</div>
        <el-select v-model="form.cycleUnit" placeholder="周期单位">
          <el-option v-for="i in cycleUnitOptions" :key="i" :label="i" :value="i" />
        </el-select>
      </div>
      <div class="field-item">
        <div class="field-label">上次完工日期</div>
        <el-date-picker
          v-model="form.lastCompletionDate"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="上次完工日期"
        />
      </div>
      <div class="field-item">
        <div class="field-label">下次排程日期</div>
        <el-date-picker
          v-model="form.nextScheduleDate"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="下次排程日期"
        />
      </div>

      <div class="field-item time-range-cell">
        <div class="field-label">维护时间</div>
        <div class="time-range-row">
          <el-date-picker
            v-model="form.maintenanceStartTime"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="维护开始时间"
          />
          <span class="time-separator">至</span>
          <el-date-picker
            v-model="form.maintenanceEndTime"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="维护结束时间"
          />
        </div>
      </div>
      <div class="field-item empty-cell" />
      <div class="field-item empty-cell" />
    </div>

    <el-tabs :model-value="tabsValue" @update:model-value="(v) => emit('update:tabsValue', v)">
      <el-tab-pane label="备件" name="spare">
        <el-button @click="emit('open-material', '01-备件')">新增备件</el-button>
        <el-table :data="form.spareParts" border>
          <el-table-column prop="materialCode" label="物料代码" />
          <el-table-column prop="materialName" label="物料名称" />
          <el-table-column prop="materialSubCategory" label="物料分类" />
          <el-table-column prop="modelSpecification" label="规格型号" />
          <el-table-column label="数量">
            <template #default="{ row }">
              <el-input-number v-model="row.quantity" :min="1" />
            </template>
          </el-table-column>
          <el-table-column prop="quantityUnit" label="数量单位" />
          <el-table-column width="60">
            <template #default="{ $index }">
              <el-button link type="danger" @click="emit('remove-item', 'spareParts', $index)">
                删
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="工器具" name="tool">
        <el-button @click="emit('open-material', '03-工器具')">新增工器具</el-button>
        <el-table :data="form.tools" border>
          <el-table-column prop="materialCode" label="物料代码" />
          <el-table-column prop="materialName" label="物料名称" />
          <el-table-column prop="materialSubCategory" label="物料分类" />
          <el-table-column prop="modelSpecification" label="规格型号" />
          <el-table-column label="数量">
            <template #default="{ row }">
              <el-input-number v-model="row.quantity" :min="1" />
            </template>
          </el-table-column>
          <el-table-column prop="quantityUnit" label="数量单位" />
          <el-table-column width="60">
            <template #default="{ $index }">
              <el-button link type="danger" @click="emit('remove-item', 'tools', $index)">
                删
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="安全挂牌" name="tag">
        <el-button @click="emit('add-tag')">新增安全挂牌</el-button>
        <el-table :data="form.safetyTags" border>
          <el-table-column label="安全牌性质">
            <template #default="{ row }">
              <el-select v-model="row.tagNature">
                <el-option v-for="i in tagNatureOptions" :key="i" :label="i" :value="i" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="挂牌位置">
            <template #default="{ row }">
              <el-select v-model="row.tagLocation">
                <el-option v-for="i in tagLocationOptions" :key="i" :label="i" :value="i" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="挂牌设备编码">
            <template #default="{ row }"><el-input v-model="row.tagDeviceCode" /></template>
          </el-table-column>
          <el-table-column width="60">
            <template #default="{ $index }">
              <el-button link type="danger" @click="emit('remove-item', 'safetyTags', $index)">
                删
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <div class="bar">
      <span />
      <el-button type="primary" @click="emit('save')">保存</el-button>
    </div>
  </el-drawer>
</template>

<style scoped>
.grid3 {
  display: grid;
  grid-template-columns: repeat(3, minmax(180px, 1fr));
  gap: 12px;
  margin-bottom: 12px;
}
.field-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.field-label {
  color: #606266;
  font-size: 12px;
  line-height: 1;
}
.inline-cycle :deep(.el-input-number) {
  width: 100%;
}
.time-range-cell {
  grid-column: span 3;
}
.time-range-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto minmax(0, 1fr);
  align-items: center;
  gap: 12px;
}
.time-range-row :deep(.el-date-editor.el-input) {
  width: 100%;
}
.time-separator {
  color: #909399;
  font-size: 12px;
}
.empty-cell {
  visibility: hidden;
}
.bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
}
:deep(.el-tabs__item.is-active) {
  color: #2f76e8;
}
:deep(.el-tabs__active-bar) {
  background: #2f76e8;
}
:deep(.el-table__header th) {
  background: #e7f0ff;
  color: #365078;
  font-size: 12px;
  font-weight: 700;
}
:deep(.el-table__body td) {
  font-size: 12px;
}
:deep(.el-input__wrapper),
:deep(.el-select__wrapper),
:deep(.el-date-editor.el-input) {
  min-height: 32px;
}
</style>
