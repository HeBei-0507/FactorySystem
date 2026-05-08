<script setup>
import { computed, nextTick, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as XLSX from 'xlsx'
import { getProductionlineList } from '@/api/prodectionLine'
import { getDeviceUtilList } from '@/api/deviceUnit'
import { getEquipmentById, getEquipmentListByDeviceUnitId } from '@/api/equipment'
import { getEquipmentPartListByEquipmentId } from '@/api/equipmentPart'
import {
  addInspectionStandard,
  batchAddInspectionStandard,
  deleteInspectionStandard,
  getInspectionStandardPage,
  updateInspectionStandard
} from '@/api/inspectionStandard'
import { getIDAddressPage } from '@/api/IDAddress'
import { extractPageData, extractRecordsArray } from '@/util/apiData'

const pageTitle = '点检标准管理'
const productionLines = ref([])
const activeLineId = ref('')
const activeUnitId = ref('')
const activeDeviceId = ref('')
const expandedUnitIds = ref([])

const inspectionFilters = reactive({
  inspectionName: '',
  partCode: '',
  partName: '',
  cycleUnit: '',
  dataType: '',
  signalType: '',
  implementationMethod: '',
  standardCategory: '',
  unitOfMeasurement: '',
  profession: ''
})
const inspectionRows = ref([])
const inspectionLoading = ref(false)
const inspectionSelectedRows = ref([])
const currentDeviceParts = ref([])
const currentScopePartCodes = ref([])
const inspectionSelectionSyncLocked = ref(false)
const currentScopeRequestSeq = ref(0)
const selectedPartCode = ref('')
const inspectionPagination = reactive({ currentPage: 1, pageSize: 10, total: 0 })
const inspectionDrawerVisible = ref(false)
const inspectionDrawerMode = ref('create')
const inspectionDrawerSaving = ref(false)
const idAddressOptions = ref([])

const inspectionImportLoading = ref(false)
const inspectionFileInputRef = ref(null)

/** Excel 列名：英文字段优先，兼容中文列头（模板见 public） */
const inspectionImportColumnMap = {
  inspectionName: ['inspectionName', '点检标准名称', '标准名称', '名称'],
  partCode: ['partCode', '部位编码', '设备部位编码'],
  partName: ['partName', '部位名称', '设备部位名称'],
  implementationCycle: ['implementationCycle', '执行周期', '实施周期'],
  cycleUnit: ['cycleUnit', '周期单位', '巡检周期单位'],
  dataType: ['dataType', '数据类型'],
  signalType: ['signalType', '信号类型'],
  implementationMethod: ['implementationMethod', '实施方法', '实施'],
  inspectionContent: ['inspectionContent', '点检内容', '巡检内容'],
  inspectionPart: ['inspectionPart', '点检部位', '巡检部位'],
  qualitativeStandard: ['qualitativeStandard', '定性标准'],
  standardCategory: ['standardCategory', '标准类别'],
  quantitativeStandard: ['quantitativeStandard', '定量标准'],
  unitOfMeasurement: ['unitOfMeasurement', '计量单位'],
  profession: ['profession', '专业'],
  upperLimit: ['upperLimit', '上限', '上限值'],
  lowerLimit: ['lowerLimit', '下限', '下限值'],
  idLocationCode: ['idLocationCode', 'ID位置编码'],
  idLocationName: ['idLocationName', 'ID位置']
}
const inspectionForm = reactive({
  id: '',
  inspectionName: '',
  partCode: '',
  partName: '',
  implementationCycle: 1,
  cycleUnit: '01',
  dataType: '',
  signalType: '',
  implementationMethod: '',
  inspectionContent: '',
  inspectionPart: '',
  qualitativeStandard: '',
  standardCategory: '',
  quantitativeStandard: '',
  unitOfMeasurement: '',
  profession: '',
  upperLimit: '',
  lowerLimit: '',
  idLocationCode: '',
  idLocationName: ''
})

const optionCycleUnit = [
  { value: '01', label: '天' },
  { value: '02', label: '周' },
  { value: '03', label: '月' },
  { value: '04', label: '年' }
]
const optionDataType = [
  { value: '01', label: '数值' },
  { value: '02', label: '时间波形' },
  { value: '03', label: '长数据(频道波形)' },
  { value: '04', label: '高频段(频谱波形)' },
  { value: '05', label: '频带指标' },
  { value: '06', label: '观察量' },
  { value: '07', label: '仪操量' },
  { value: '08', label: '手操量' }
]
const optionSignalType = [
  { value: '00', label: '速度' },
  { value: '01', label: '加速度' },
  { value: '02', label: '位移' },
  { value: '10', label: '压力' },
  { value: '20', label: '温度' },
  { value: '21', label: '温差' },
  { value: '30', label: '电流' },
  { value: '31', label: '电压' },
  { value: '40', label: '频率' },
  { value: '50', label: '亮度' },
  { value: '51', label: '功率' },
  { value: '52', label: '光强' },
  { value: '60', label: '流量' },
  { value: '61', label: '扭矩' },
  { value: '62', label: '转速' },
  { value: '70', label: '观察量' },
  { value: '80', label: '长度' }
]
const optionImplementationMethod = [
  { value: '01', label: '五感' },
  { value: '02', label: '仪器' },
  { value: '03', label: '量具' }
]
const optionStandardCategory = [
  { value: '01', label: '定量' },
  { value: '02', label: '定性' }
]
const optionProfession = [
  { value: '01', label: '01-机械专业' },
  { value: '02', label: '02-电气专业' },
  { value: '03', label: '03-仪表专业' },
  { value: '04', label: '04-过程控制专业' }
]
const optionUnitOfMeasurement = [
  { value: '01', label: '毫米(mm)' },
  { value: '02', label: '摄氏度(°C)' },
  { value: '03', label: '兆帕(MPa)' }
]

function optionLabel(options, value) {
  if (value == null || value === '') return ''
  const normalizedValue = String(value).trim()
  const hit = options.find((x) => x.value === normalizedValue)
  return hit ? hit.label : normalizedValue
}

function optionValueByLabel(options, value) {
  if (value == null || value === '') return ''
  const normalizedValue = String(value).trim()
  const compactValue = normalizedValue.replace(/\s+/g, '')
  const hit = options.find(
    (x) =>
      x.value === normalizedValue ||
      x.label === normalizedValue ||
      x.label.replace(/\s+/g, '') === compactValue
  )
  return hit ? hit.value : normalizedValue
}

function normalizeInspectionEnumFields(target = {}) {
  target.cycleUnit = optionValueByLabel(optionCycleUnit, target.cycleUnit)
  target.dataType = optionValueByLabel(optionDataType, target.dataType)
  target.signalType = optionValueByLabel(optionSignalType, target.signalType)
  target.implementationMethod = optionValueByLabel(
    optionImplementationMethod,
    target.implementationMethod
  )
  target.standardCategory = optionValueByLabel(optionStandardCategory, target.standardCategory)
  target.unitOfMeasurement = optionValueByLabel(optionUnitOfMeasurement, target.unitOfMeasurement)
  target.profession = optionValueByLabel(optionProfession, target.profession)
  return target
}

function decimalOrNull(value) {
  if (value == null || value === '') return null
  const n = Number(value)
  return Number.isNaN(n) ? null : n
}

function trimVal(v) {
  return String(v ?? '').trim()
}

async function loadIDAddressOptions() {
  try {
    const res = await getIDAddressPage({ current: 1, size: 1000 })
    idAddressOptions.value = extractRecordsArray(res?.data)
      .map((item) => ({
        id: item.id,
        idLocationCode: trimVal(item.idLocationCode),
        idLocationName: trimVal(item.idLocationName)
      }))
      .filter((item) => item.idLocationCode || item.idLocationName)
  } catch (e) {
    if (!e?.elMessageNotified) ElMessage.error('获取ID位置列表失败')
  }
}

function handleIDAddressChange(code) {
  const current = idAddressOptions.value.find((item) => item.idLocationCode === code)
  inspectionForm.idLocationCode = current?.idLocationCode || ''
  inspectionForm.idLocationName = current?.idLocationName || ''
}

function pickInspectionCell(row, aliases = []) {
  for (const key of aliases) {
    if (row[key] !== undefined && row[key] !== null && trimVal(row[key]) !== '') {
      return trimVal(row[key])
    }
  }
  return ''
}
function mapInspectionImportRow(row) {
  const out = {
    inspectionName: pickInspectionCell(row, inspectionImportColumnMap.inspectionName),
    partCode: pickInspectionCell(row, inspectionImportColumnMap.partCode)
  }
  const optionalStr = (field) => {
    const v = pickInspectionCell(row, inspectionImportColumnMap[field])
    if (v !== '') out[field] = v
  }
  optionalStr('partName')
  optionalStr('cycleUnit')
  optionalStr('dataType')
  optionalStr('signalType')
  optionalStr('implementationMethod')
  optionalStr('inspectionContent')
  optionalStr('inspectionPart')
  optionalStr('qualitativeStandard')
  optionalStr('standardCategory')
  optionalStr('quantitativeStandard')
  optionalStr('unitOfMeasurement')
  optionalStr('profession')
  optionalStr('upperLimit')
  optionalStr('lowerLimit')
  optionalStr('idLocationCode')
  optionalStr('idLocationName')
  const cycleRaw = pickInspectionCell(row, inspectionImportColumnMap.implementationCycle)
  if (cycleRaw !== '') {
    const n = Number(cycleRaw)
    if (!Number.isNaN(n) && Number.isFinite(n)) {
      out.implementationCycle = Math.trunc(n)
    }
  }
  return normalizeInspectionEnumFields(out)
}

/** 与后端 @NotBlank / @Length 一致，仅合格数据参与提交 */
const INSPECTION_IMPORT_NAME_MAX = 100
const INSPECTION_IMPORT_PART_CODE_MAX = 100

function normalizeInspectionImportRecord(m) {
  return {
    ...m,
    inspectionName: trimVal(m.inspectionName),
    partCode: trimVal(m.partCode)
  }
}

function partitionInspectionImportRows(mapped) {
  const valid = []
  const skipped = []
  mapped.forEach((row, idx) => {
    const m = normalizeInspectionImportRecord(row)
    const { inspectionName: name, partCode: code } = m
    const reasons = []
    if (!name) reasons.push('点检标准名称不能为空')
    else if (name.length > INSPECTION_IMPORT_NAME_MAX) {
      reasons.push(`点检标准名称长度不能超过${INSPECTION_IMPORT_NAME_MAX}`)
    }
    if (!code) reasons.push('设备部位编码不能为空')
    else if (code.length > INSPECTION_IMPORT_PART_CODE_MAX) {
      reasons.push(`设备部位编码长度不能超过${INSPECTION_IMPORT_PART_CODE_MAX}`)
    }
    if (reasons.length) {
      skipped.push({ excelRow: idx + 2, reasons })
    } else {
      valid.push(m)
    }
  })
  return { valid, skipped }
}

function openInspectionImportDialog() {
  if (inspectionImportLoading.value) return
  inspectionFileInputRef.value?.click()
}
async function handleInspectionImportFileChange(event) {
  const input = event.target
  const file = input?.files?.[0]
  if (!file) return
  if (!/\.(xlsx|xls)$/i.test(file.name)) {
    ElMessage.warning('请上传 .xlsx 或 .xls 格式文件')
    input.value = ''
    return
  }
  inspectionImportLoading.value = true
  try {
    const buffer = await file.arrayBuffer()
    const workbook = XLSX.read(buffer, { type: 'array' })
    const sheetName = workbook.SheetNames[0]
    if (!sheetName) {
      ElMessage.warning('Excel 中未找到工作表')
      return
    }
    const jsonRows = XLSX.utils.sheet_to_json(workbook.Sheets[sheetName], {
      defval: '',
      raw: false
    })
    const mapped = jsonRows.map(mapInspectionImportRow)
    const { valid: toSubmit, skipped } = partitionInspectionImportRows(mapped)
    if (!toSubmit.length) {
      const detail =
        skipped.length > 0
          ? `共 ${skipped.length} 行不符合规范（点检标准名称、设备部位编码须非空且长度分别不超过 ${INSPECTION_IMPORT_NAME_MAX}、${INSPECTION_IMPORT_PART_CODE_MAX}）`
          : 'Excel 中没有可解析的数据行'
      ElMessage.warning(detail)
      if (skipped.length) {
        const lines = skipped
          .slice(0, 30)
          .map((s) => `第 ${s.excelRow} 行：${s.reasons.join('；')}`)
        if (skipped.length > 30) lines.push(`… 其余 ${skipped.length - 30} 行略`)
        await ElMessageBox.alert(lines.join('\n'), '不规范数据明细', {
          type: 'warning',
          confirmButtonText: '知道了'
        })
      }
      return
    }
    if (skipped.length) {
      ElMessage.info(
        `已过滤 ${skipped.length} 条不规范数据（名称、部位编码须非空且长度分别不超过 ${INSPECTION_IMPORT_NAME_MAX}、${INSPECTION_IMPORT_PART_CODE_MAX}），将提交 ${toSubmit.length} 条`
      )
    }
    const res = await batchAddInspectionStandard({ inspectionStandardList: toSubmit })
    const stat = res?.data ?? {}
    const ok = typeof stat.successCount === 'number' ? stat.successCount : 0
    const bad = typeof stat.failedCount === 'number' ? stat.failedCount : 0
    const failedRecords = Array.isArray(stat.failedRecords) ? stat.failedRecords : []

    if (bad > 0) {
      ElMessage.warning(
        res?.message ||
          (ok > 0 ? `部分导入成功：成功 ${ok} 条，失败 ${bad} 条` : `全部失败：失败 ${bad} 条`)
      )
      if (failedRecords.length) {
        await ElMessageBox.alert(failedRecords.join('\n'), '失败明细', {
          type: 'warning',
          confirmButtonText: '知道了'
        })
      }
    } else {
      ElMessage.success(res?.message || `批量导入成功，共 ${ok} 条`)
    }
    await loadInspectionPage()
  } catch (e) {
    if (e?.elMessageNotified) return
    console.error(e)
    ElMessage.error('批量导入失败，请检查数据与模板格式后重试')
  } finally {
    inspectionImportLoading.value = false
    input.value = ''
  }
}

const activeLine = computed(
  () => productionLines.value.find((v) => v.id === activeLineId.value) || null
)
const activeUnit = computed(
  () => activeLine.value?.units.find((v) => v.id === activeUnitId.value) || null
)
const activeDevice = computed(
  () =>
    activeUnit.value?.devices?.find((v) => String(v.id) === String(activeDeviceId.value)) || null
)
const mapLine = (i) => ({ id: i.id, name: i.lineName, units: [] })
const mapUnit = (i) => ({ id: i.id, unitCode: i.unitCode, unitName: i.unitName, devices: [] })
const mapDeviceBrief = (i) => ({
  id: i.id,
  equipmentName: i.equipmentName || '',
  equipmentCode: i.equipmentCode || ''
})
function uniquePartList(items = []) {
  const seen = new Set()
  return items.filter((item) => {
    const key = String(item.partCode || '').trim()
    if (!key || seen.has(key)) return false
    seen.add(key)
    return true
  })
}
function resetInspectionScope() {
  selectedPartCode.value = ''
  inspectionFilters.partCode = ''
  inspectionFilters.partName = ''
  currentDeviceParts.value = []
  currentScopePartCodes.value = []
  inspectionRows.value = []
  inspectionPagination.total = 0
  inspectionSelectedRows.value = []
}
const mapInspectionRow = (i = {}) => ({
  id: i.id,
  inspectionName: i.inspectionName || '',
  partCode: i.partCode || '',
  partName: i.partName || '',
  implementationCycle: i.implementationCycle,
  cycleUnit: i.cycleUnit || '',
  dataType: i.dataType || '',
  signalType: i.signalType || '',
  implementationMethod: i.implementationMethod || '',
  inspectionContent: i.inspectionContent || '',
  inspectionPart: i.inspectionPart || '',
  qualitativeStandard: i.qualitativeStandard || '',
  standardCategory: i.standardCategory || '',
  quantitativeStandard:
    i.quantitativeStandard != null && String(i.quantitativeStandard).trim() !== ''
      ? String(i.quantitativeStandard).trim()
      : '',
  unitOfMeasurement: i.unitOfMeasurement || '',
  profession: i.profession || '',
  upperLimit: i.upperLimit,
  lowerLimit: i.lowerLimit,
  idLocationCode: i.idLocationCode || '',
  idLocationName: i.idLocationName || '',
  creatorUsername: i.creatorUsername || '',
  creatorName: i.creatorName || '',
  createdAt: i.createdAt || '',
  updatedAt: i.updatedAt || ''
})

async function loadProductionLines() {
  try {
    const res = await getProductionlineList()
    productionLines.value = extractRecordsArray(res?.data).map(mapLine)
    if (productionLines.value.length) await clickLine(productionLines.value[0])
  } catch (e) {
    if (!e?.elMessageNotified) ElMessage.error('获取生产线列表失败')
  }
}
async function clickLine(line) {
  const reqSeq = ++currentScopeRequestSeq.value
  activeLineId.value = line.id
  activeUnitId.value = ''
  activeDeviceId.value = ''
  resetInspectionScope()
  try {
    const res = await getDeviceUtilList(line.id)
    if (reqSeq !== currentScopeRequestSeq.value) return
    line.units = extractRecordsArray(res?.data).map(mapUnit)
    expandedUnitIds.value = []
    if (line.units.length) await clickUnit(line, line.units[0])
  } catch (e) {
    if (reqSeq !== currentScopeRequestSeq.value) return
    if (!e?.elMessageNotified) ElMessage.error('获取设备单元列表失败')
  }
}
async function loadDeviceBriefs(line, unit) {
  if (!line || !unit?.unitCode) return
  try {
    const res = await getEquipmentListByDeviceUnitId(unit.id)
    const list = extractRecordsArray(res?.data).map(mapDeviceBrief)
    const targetLine = productionLines.value.find((x) => x.id === line.id)
    const targetUnit = targetLine?.units.find((x) => x.id === unit.id)
    if (targetUnit) targetUnit.devices = list
  } catch {}
}
async function clickUnit(line, unit) {
  const reqSeq = ++currentScopeRequestSeq.value
  activeLineId.value = line.id
  activeUnitId.value = unit.id
  activeDeviceId.value = ''
  resetInspectionScope()
  if (!expandedUnitIds.value.includes(unit.id)) expandedUnitIds.value.push(unit.id)
  if (!unit.devices?.length) await loadDeviceBriefs(line, unit)
  if (reqSeq !== currentScopeRequestSeq.value) return

  const devices = Array.isArray(unit.devices) ? unit.devices : []
  const partResults = await Promise.all(
    devices.map(async (device) => {
      try {
        const res = await getEquipmentPartListByEquipmentId(device.id)
        const rawList = Array.isArray(res?.data) ? res.data : extractRecordsArray(res?.data)
        return (rawList || [])
          .filter((item) => item && typeof item === 'object')
          .map((item) => ({
            id: item.id,
            partCode: String(item.partCode || '').trim(),
            partName: String(item.partName || '').trim()
          }))
          .filter((item) => item.partCode || item.partName)
      } catch {
        return []
      }
    })
  )
  if (reqSeq !== currentScopeRequestSeq.value) return

  const mergedParts = uniquePartList(partResults.flat())
  currentDeviceParts.value = mergedParts
  currentScopePartCodes.value = mergedParts.map((item) => item.partCode).filter(Boolean)
  inspectionPagination.currentPage = 1
  if (!currentScopePartCodes.value.length) return
  await loadInspectionPage()
}
function isUnitExpanded(unit) {
  return expandedUnitIds.value.includes(unit.id)
}
async function toggleUnitExpand(line, unit) {
  if (isUnitExpanded(unit)) {
    expandedUnitIds.value = expandedUnitIds.value.filter((id) => id !== unit.id)
    return
  }
  expandedUnitIds.value.push(unit.id)
  if (!unit.devices?.length) await loadDeviceBriefs(line, unit)
}
async function clickDevice(line, unit, device) {
  const reqSeq = ++currentScopeRequestSeq.value
  activeLineId.value = line.id
  activeUnitId.value = unit.id
  activeDeviceId.value = device.id
  resetInspectionScope()

  let deviceCode = String(device?.equipmentCode || '').trim()
  if (!deviceCode && device?.id) {
    try {
      const detailRes = await getEquipmentById(device.id)
      if (reqSeq !== currentScopeRequestSeq.value) return
      deviceCode = String(detailRes?.data?.equipmentCode || '').trim()
    } catch {}
  }
  if (device?.id) {
    try {
      const res = await getEquipmentPartListByEquipmentId(device.id)
      if (reqSeq !== currentScopeRequestSeq.value) return
      const rawList = Array.isArray(res?.data) ? res.data : extractRecordsArray(res?.data)
      const list = (rawList || [])
        .filter((item) => item && typeof item === 'object')
        .map((item) => ({
          id: item.id,
          partCode: String(item.partCode || '').trim(),
          partName: String(item.partName || '').trim()
        }))
        .filter((item) => item.partCode || item.partName)
      currentDeviceParts.value = list
      currentScopePartCodes.value = list.map((item) => item.partCode).filter(Boolean)
    } catch (e) {
      if (reqSeq !== currentScopeRequestSeq.value) return
      resetInspectionScope()
      if (!e?.elMessageNotified) ElMessage.error('获取设备部位列表失败')
    }
  }
  if (reqSeq !== currentScopeRequestSeq.value) return
  inspectionPagination.currentPage = 1
  if (!currentScopePartCodes.value.length) return
  await loadInspectionPage()
}

async function loadInspectionPage() {
  inspectionLoading.value = true
  inspectionSelectionSyncLocked.value = true
  try {
    const effectivePartCodes = currentScopePartCodes.value.filter(Boolean)
    const effectivePartCode = String(inspectionFilters.partCode || '').trim()
    const effectivePartName = String(inspectionFilters.partName || '').trim()
    const normalizedFilters = normalizeInspectionEnumFields({
      cycleUnit: inspectionFilters.cycleUnit,
      dataType: inspectionFilters.dataType,
      signalType: inspectionFilters.signalType,
      implementationMethod: inspectionFilters.implementationMethod,
      standardCategory: inspectionFilters.standardCategory,
      unitOfMeasurement: inspectionFilters.unitOfMeasurement,
      profession: inspectionFilters.profession
    })
    const res = await getInspectionStandardPage({
      current: inspectionPagination.currentPage,
      size: inspectionPagination.pageSize,
      inspectionName: inspectionFilters.inspectionName,
      partCode: effectivePartCode || undefined,
      partCodes: !effectivePartCode && effectivePartCodes.length ? effectivePartCodes : undefined,
      partName: effectivePartName || undefined,
      cycleUnit: normalizedFilters.cycleUnit || undefined,
      dataType: normalizedFilters.dataType || undefined,
      signalType: normalizedFilters.signalType || undefined,
      implementationMethod: normalizedFilters.implementationMethod || undefined,
      standardCategory: normalizedFilters.standardCategory || undefined,
      unitOfMeasurement: normalizedFilters.unitOfMeasurement || undefined,
      profession: normalizedFilters.profession || undefined
    })
    const { records, total } = extractPageData(res)
    inspectionRows.value = (records || []).map((record) =>
      mapInspectionRow(normalizeInspectionEnumFields({ ...record }))
    )
    inspectionPagination.total = total
    inspectionSelectedRows.value = []
  } catch (e) {
    if (!e?.elMessageNotified) ElMessage.error('获取点检标准列表失败')
  } finally {
    await nextTick()
    inspectionSelectionSyncLocked.value = false
    inspectionLoading.value = false
  }
}
function handleInspectionSearch() {
  inspectionPagination.currentPage = 1
  loadInspectionPage()
}
function handlePartChangeByCode(code) {
  selectedPartCode.value = code || ''
  if (!selectedPartCode.value) {
    inspectionFilters.partCode = ''
    inspectionFilters.partName = ''
    inspectionPagination.currentPage = 1
    loadInspectionPage()
    return
  }
  const current = currentDeviceParts.value.find((x) => x.partCode === selectedPartCode.value)
  inspectionFilters.partCode = current?.partCode || ''
  inspectionFilters.partName = current?.partName || ''
  inspectionPagination.currentPage = 1
  loadInspectionPage()
}
function handleInspectionSelectionChange(rows) {
  if (inspectionSelectionSyncLocked.value) return
  inspectionSelectedRows.value = rows
}
function resetInspectionForm() {
  Object.assign(
    inspectionForm,
    normalizeInspectionEnumFields({
      id: '',
      inspectionName: '',
      partCode: '',
      partName: '',
      implementationCycle: 1,
      cycleUnit: '01',
      dataType: '',
      signalType: '',
      implementationMethod: '',
      inspectionContent: '',
      inspectionPart: '',
      qualitativeStandard: '',
      standardCategory: '',
      quantitativeStandard: '',
      unitOfMeasurement: '',
      profession: '',
      upperLimit: '',
      lowerLimit: ''
    })
  )
}
function openCreateInspectionDrawer() {
  if (!currentDeviceParts.value.length)
    return ElMessage.warning('请先在左侧设备树中选择包含设备部位的设备或设备单元')
  resetInspectionForm()
  inspectionDrawerMode.value = 'create'
  inspectionDrawerVisible.value = true
}
function handleDrawerPartChangeByCode(code) {
  const current = currentDeviceParts.value.find((x) => x.partCode === code)
  inspectionForm.partCode = current?.partCode || ''
  inspectionForm.partName = current?.partName || ''
}
function openEditInspectionDrawer() {
  if (inspectionSelectedRows.value.length !== 1)
    return ElMessage.warning('请选择一条点检标准进行修改')
  Object.assign(
    inspectionForm,
    normalizeInspectionEnumFields({ ...inspectionSelectedRows.value[0] })
  )
  inspectionDrawerMode.value = 'edit'
  inspectionDrawerVisible.value = true
}
function validInspectionRow(row) {
  if (!String(row.inspectionName || '').trim())
    return (ElMessage.warning('点检标准名称不能为空'), false)
  if (!String(row.partCode || '').trim())
    return (ElMessage.warning('请在新增界面选择设备部位'), false)
  return true
}
function buildInspectionPayload(row, includeId = false) {
  const normalizedEnums = normalizeInspectionEnumFields({
    cycleUnit: row.cycleUnit,
    dataType: row.dataType,
    signalType: row.signalType,
    implementationMethod: row.implementationMethod,
    standardCategory: row.standardCategory,
    unitOfMeasurement: row.unitOfMeasurement,
    profession: row.profession
  })
  const data = {
    inspectionName: String(row.inspectionName || '').trim(),
    partCode: String(row.partCode || '').trim(),
    partName: String(row.partName || '').trim(),
    implementationCycle:
      row.implementationCycle == null || row.implementationCycle === ''
        ? undefined
        : Number(row.implementationCycle),
    cycleUnit: String(normalizedEnums.cycleUnit || '').trim() || undefined,
    dataType: String(normalizedEnums.dataType || '').trim() || undefined,
    signalType: String(normalizedEnums.signalType || '').trim() || undefined,
    implementationMethod: String(normalizedEnums.implementationMethod || '').trim() || undefined,
    inspectionContent: String(row.inspectionContent || '').trim() || undefined,
    inspectionPart: String(row.inspectionPart || '').trim() || undefined,
    qualitativeStandard: String(row.qualitativeStandard || '').trim() || undefined,
    standardCategory: String(normalizedEnums.standardCategory || '').trim() || undefined,
    quantitativeStandard: String(row.quantitativeStandard || '').trim() || undefined,
    unitOfMeasurement: String(normalizedEnums.unitOfMeasurement || '').trim() || undefined,
    profession: String(normalizedEnums.profession || '').trim() || undefined,
    upperLimit: decimalOrNull(row.upperLimit),
    lowerLimit: decimalOrNull(row.lowerLimit),
    idLocationCode: String(row.idLocationCode || '').trim() || undefined,
    idLocationName: String(row.idLocationName || '').trim() || undefined
  }
  Object.keys(data).forEach((k) => {
    if (data[k] === undefined) delete data[k]
  })
  if (includeId) data.id = row.id
  return data
}
async function submitCreateInspection() {
  if (!validInspectionRow(inspectionForm)) return
  try {
    inspectionDrawerSaving.value = true
    await addInspectionStandard(buildInspectionPayload(inspectionForm, false))
    ElMessage.success('新增点检标准成功')
    inspectionDrawerVisible.value = false
    await loadInspectionPage()
  } catch (e) {
    if (!e?.elMessageNotified) ElMessage.error('新增点检标准失败')
  } finally {
    inspectionDrawerSaving.value = false
  }
}
async function submitEditInspection() {
  if (!inspectionForm.id) return ElMessage.warning('点检标准ID无效')
  if (!validInspectionRow(inspectionForm)) return
  try {
    inspectionDrawerSaving.value = true
    await updateInspectionStandard(buildInspectionPayload(inspectionForm, true))
    ElMessage.success('修改点检标准成功')
    inspectionDrawerVisible.value = false
    await loadInspectionPage()
  } catch (e) {
    if (!e?.elMessageNotified) ElMessage.error('修改点检标准失败')
  } finally {
    inspectionDrawerSaving.value = false
  }
}
async function removeInspectionRows() {
  if (!inspectionSelectedRows.value.length) return ElMessage.warning('请至少选择一条点检标准')
  try {
    await Promise.all(inspectionSelectedRows.value.map((x) => deleteInspectionStandard(x.id)))
    ElMessage.success('删除点检标准成功')
    await loadInspectionPage()
  } catch (e) {
    if (!e?.elMessageNotified) ElMessage.error('删除点检标准失败')
  }
}

loadProductionLines()
loadIDAddressOptions()
</script>

<template>
  <div class="standard-page">
    <div class="page-breadcrumb">
      {{ pageTitle }} &gt; {{ activeDevice?.equipmentName || activeUnit?.unitName || '未选择设备' }}
    </div>
    <div class="page-content">
      <aside class="tree-panel">
        <div class="tree-panel-header">设备树导航</div>
        <div class="tree-content">
          <div v-for="line in productionLines" :key="line.id" class="line-block">
            <div
              :class="['line-title', { active: line.id === activeLineId }]"
              @click="clickLine(line)"
            >
              {{ line.name }}
            </div>
            <div class="unit-list">
              <div
                v-for="unit in line.units"
                :key="unit.id"
                :class="['unit-item', { active: unit.id === activeUnitId }]"
              >
                <div class="unit-row" @click="clickUnit(line, unit)">
                  <span class="arrow" @click.stop="toggleUnitExpand(line, unit)">
                    {{ isUnitExpanded(unit) ? '⌄' : '›' }}
                  </span>
                  <span>{{ unit.unitName }}</span>
                </div>
                <div v-show="isUnitExpanded(unit)" class="device-list">
                  <div
                    v-for="d in unit.devices"
                    :key="d.id"
                    :class="['device-item', { active: String(d.id) === String(activeDeviceId) }]"
                    @click.stop="clickDevice(line, unit, d)"
                  >
                    {{ d.equipmentName }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </aside>

      <section class="table-panel">
        <div class="toolbar-row">
          <div class="selected-device">
            当前范围：{{ activeDevice?.equipmentCode || activeUnit?.unitCode || '--' }}
            {{ activeDevice?.equipmentName || activeUnit?.unitName || '--' }}
          </div>
        </div>

        <div class="filter-bar">
          <div class="filter-item">
            <span class="filter-label">标准名称</span>
            <el-input
              v-model="inspectionFilters.inspectionName"
              clearable
              placeholder="请输入标准名称"
            />
          </div>
          <div class="filter-item">
            <span class="filter-label">部位编码</span>
            <el-input
              v-model="inspectionFilters.partCode"
              :disabled="!activeDeviceId"
              :placeholder="
                activeDeviceId ? '随部位名称自动带出' : '设备单元范围下可选全部设备部位'
              "
            />
          </div>
          <div class="filter-item">
            <span class="filter-label">部位名称</span>
            <el-select
              v-model="selectedPartCode"
              class="sel-full"
              clearable
              placeholder="请选择设备部位"
              @change="handlePartChangeByCode"
            >
              <el-option
                v-for="p in currentDeviceParts"
                :key="p.partCode"
                :label="`${p.partName} (${p.partCode})`"
                :value="p.partCode"
              />
            </el-select>
          </div>
          <div class="filter-item">
            <span class="filter-label">周期单位</span>
            <el-select v-model="inspectionFilters.cycleUnit" clearable placeholder="请选择周期单位">
              <el-option
                v-for="o in optionCycleUnit"
                :key="o.value"
                :label="o.label"
                :value="o.value"
              />
            </el-select>
          </div>
          <div class="filter-item">
            <span class="filter-label">数据类型</span>
            <el-select
              v-model="inspectionFilters.dataType"
              clearable
              filterable
              placeholder="请选择数据类型"
            >
              <el-option
                v-for="o in optionDataType"
                :key="o.value"
                :label="o.label"
                :value="o.value"
              />
            </el-select>
          </div>
          <div class="filter-item">
            <span class="filter-label">信号类型</span>
            <el-select
              v-model="inspectionFilters.signalType"
              clearable
              filterable
              placeholder="请选择信号类型"
            >
              <el-option
                v-for="o in optionSignalType"
                :key="o.value"
                :label="o.label"
                :value="o.value"
              />
            </el-select>
          </div>
          <div class="filter-item">
            <span class="filter-label">实施方法</span>
            <el-select
              v-model="inspectionFilters.implementationMethod"
              clearable
              placeholder="请选择实施方法"
            >
              <el-option
                v-for="o in optionImplementationMethod"
                :key="o.value"
                :label="o.label"
                :value="o.value"
              />
            </el-select>
          </div>
          <div class="filter-item">
            <span class="filter-label">标准类别</span>
            <el-select
              v-model="inspectionFilters.standardCategory"
              clearable
              placeholder="请选择标准类别"
            >
              <el-option
                v-for="o in optionStandardCategory"
                :key="o.value"
                :label="o.label"
                :value="o.value"
              />
            </el-select>
          </div>
          <div class="filter-item">
            <span class="filter-label">计量单位</span>
            <el-select
              v-model="inspectionFilters.unitOfMeasurement"
              clearable
              filterable
              placeholder="请选择计量单位"
            >
              <el-option
                v-for="o in optionUnitOfMeasurement"
                :key="o.value"
                :label="o.label"
                :value="o.value"
              />
            </el-select>
          </div>
          <div class="filter-item">
            <span class="filter-label">专业</span>
            <el-select v-model="inspectionFilters.profession" clearable placeholder="请选择专业">
              <el-option
                v-for="o in optionProfession"
                :key="o.value"
                :label="o.label"
                :value="o.value"
              />
            </el-select>
          </div>
          <div class="filter-item">
            <el-button type="primary" @click="handleInspectionSearch">查询</el-button>
          </div>
        </div>

        <div class="toolbar-row">
          <el-pagination
            small
            background
            layout="prev, pager, next"
            :current-page="inspectionPagination.currentPage"
            :page-size="inspectionPagination.pageSize"
            :total="inspectionPagination.total"
            @current-change="
              (p) => {
                inspectionPagination.currentPage = p
                loadInspectionPage()
              }
            "
          />
          <div class="toolbar-right">
            <div class="import-actions">
              <a class="template-link" href="/1.四轴机器人单元点检标准.xlsx" download>下载模板</a>
              <el-button
                type="primary"
                plain
                :loading="inspectionImportLoading"
                @click="openInspectionImportDialog"
              >
                批量导入
              </el-button>
              <input
                ref="inspectionFileInputRef"
                class="hidden-file-input"
                type="file"
                accept=".xlsx,.xls"
                @change="handleInspectionImportFileChange"
              />
            </div>
            <div class="toolbar-actions">
              <el-button type="primary" @click="openCreateInspectionDrawer">新增</el-button>
              <el-button type="primary" plain @click="openEditInspectionDrawer">修改</el-button>
              <el-button type="danger" plain @click="removeInspectionRows">删除</el-button>
            </div>
          </div>
        </div>

        <el-table
          v-loading="inspectionLoading"
          :data="inspectionRows"
          border
          class="standard-table"
          header-cell-class-name="table-header-cell"
          @selection-change="handleInspectionSelectionChange"
        >
          <el-table-column type="selection" width="48" align="center" />
          <el-table-column label="点检标准名称" prop="inspectionName" min-width="170" />
          <el-table-column label="部位编码" prop="partCode" min-width="120" />
          <el-table-column label="部位名称" prop="partName" min-width="140" />
          <el-table-column label="执行周期" prop="implementationCycle" min-width="90" />
          <el-table-column label="周期单位" min-width="90">
            <template #default="{ row }">
              {{ optionLabel(optionCycleUnit, row.cycleUnit) }}
            </template>
          </el-table-column>
          <el-table-column label="数据类型" min-width="120">
            <template #default="{ row }">{{ optionLabel(optionDataType, row.dataType) }}</template>
          </el-table-column>
          <el-table-column label="信号类型" min-width="110">
            <template #default="{ row }">
              {{ optionLabel(optionSignalType, row.signalType) }}
            </template>
          </el-table-column>
          <el-table-column label="实施方法" min-width="90">
            <template #default="{ row }">
              {{ optionLabel(optionImplementationMethod, row.implementationMethod) }}
            </template>
          </el-table-column>
          <el-table-column
            label="点检内容"
            prop="inspectionContent"
            min-width="180"
            show-overflow-tooltip
          />
          <el-table-column label="点检部位" prop="inspectionPart" min-width="140" />
          <el-table-column
            label="定性标准"
            prop="qualitativeStandard"
            min-width="140"
            show-overflow-tooltip
          />
          <el-table-column label="标准类别" min-width="100">
            <template #default="{ row }">
              {{ optionLabel(optionStandardCategory, row.standardCategory) }}
            </template>
          </el-table-column>
          <el-table-column
            label="定量标准"
            prop="quantitativeStandard"
            min-width="140"
            show-overflow-tooltip
          />
          <el-table-column label="计量单位" min-width="120">
            <template #default="{ row }">
              {{ optionLabel(optionUnitOfMeasurement, row.unitOfMeasurement) }}
            </template>
          </el-table-column>
          <el-table-column label="专业" min-width="150">
            <template #default="{ row }">
              {{ optionLabel(optionProfession, row.profession) }}
            </template>
          </el-table-column>
          <el-table-column label="上限值" prop="upperLimit" min-width="100" />
          <el-table-column label="下限值" prop="lowerLimit" min-width="100" />
          <el-table-column label="ID位置编码" prop="idLocationCode" min-width="120" />
          <el-table-column label="ID位置" prop="idLocationName" min-width="120" />
          <el-table-column label="创建人工号" prop="creatorUsername" min-width="110" />
          <el-table-column label="创建人姓名" prop="creatorName" min-width="110" />
        </el-table>
      </section>
    </div>

    <el-drawer
      v-model="inspectionDrawerVisible"
      :title="inspectionDrawerMode === 'create' ? '新增点检标准' : '修改点检标准'"
      direction="rtl"
      size="980px"
    >
      <el-form label-position="top" class="drawer-form drawer-form-3col">
        <el-row :gutter="16">
          <el-col :xs="24" :sm="24" :md="8">
            <el-form-item label="部位名称">
              <el-select
                v-if="inspectionDrawerMode === 'create'"
                :model-value="inspectionForm.partCode"
                class="sel-full"
                clearable
                filterable
                placeholder="请选择设备部位"
                @change="handleDrawerPartChangeByCode"
              >
                <el-option
                  v-for="p in currentDeviceParts"
                  :key="p.partCode"
                  :label="`${p.partName} (${p.partCode})`"
                  :value="p.partCode"
                />
              </el-select>
              <el-input v-else v-model="inspectionForm.partName" disabled />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="24" :md="8">
            <el-form-item label="部位编码">
              <el-input v-model="inspectionForm.partCode" disabled />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="24" :md="8">
            <el-form-item label="标准名称">
              <el-input v-model="inspectionForm.inspectionName" placeholder="点检标准名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :xs="24" :sm="24" :md="8">
            <el-form-item label="执行周期">
              <el-input-number
                v-model="inspectionForm.implementationCycle"
                :min="0"
                class="sel-full"
                :controls="false"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="24" :md="8">
            <el-form-item label="周期单位">
              <el-select v-model="inspectionForm.cycleUnit" class="sel-full" clearable>
                <el-option
                  v-for="o in optionCycleUnit"
                  :key="o.value"
                  :label="o.label"
                  :value="o.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="24" :md="8">
            <el-form-item label="数据类型">
              <el-select v-model="inspectionForm.dataType" class="sel-full" clearable filterable>
                <el-option
                  v-for="o in optionDataType"
                  :key="o.value"
                  :label="o.label"
                  :value="o.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :xs="24" :sm="24" :md="8">
            <el-form-item label="信号类型">
              <el-select v-model="inspectionForm.signalType" class="sel-full" clearable filterable>
                <el-option
                  v-for="o in optionSignalType"
                  :key="o.value"
                  :label="o.label"
                  :value="o.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="24" :md="8">
            <el-form-item label="实施方法">
              <el-select v-model="inspectionForm.implementationMethod" class="sel-full" clearable>
                <el-option
                  v-for="o in optionImplementationMethod"
                  :key="o.value"
                  :label="o.label"
                  :value="o.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="24" :md="8">
            <el-form-item label="标准类别">
              <el-select v-model="inspectionForm.standardCategory" class="sel-full" clearable>
                <el-option
                  v-for="o in optionStandardCategory"
                  :key="o.value"
                  :label="o.label"
                  :value="o.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :xs="24" :sm="24" :md="8">
            <el-form-item label="点检部位">
              <el-input v-model="inspectionForm.inspectionPart" placeholder="点检部位" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="24" :md="8">
            <el-form-item label="计量单位">
              <el-select
                v-model="inspectionForm.unitOfMeasurement"
                class="sel-full"
                clearable
                filterable
                allow-create
                default-first-option
              >
                <el-option
                  v-for="o in optionUnitOfMeasurement"
                  :key="o.value"
                  :label="o.label"
                  :value="o.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="24" :md="8">
            <el-form-item label="专业">
              <el-select v-model="inspectionForm.profession" class="sel-full" clearable>
                <el-option
                  v-for="o in optionProfession"
                  :key="o.value"
                  :label="o.label"
                  :value="o.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :xs="24" :sm="24" :md="8">
            <el-form-item label="上限值">
              <el-input-number
                v-model="inspectionForm.upperLimit"
                :precision="2"
                :controls="false"
                class="sel-full"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="24" :md="8">
            <el-form-item label="下限值">
              <el-input-number
                v-model="inspectionForm.lowerLimit"
                :precision="2"
                :controls="false"
                class="sel-full"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="24" :md="8">
            <el-form-item label="ID位置编码">
              <el-select
                v-model="inspectionForm.idLocationCode"
                class="sel-full"
                clearable
                filterable
                placeholder="请选择ID位置编码"
                @change="handleIDAddressChange"
              >
                <el-option
                  v-for="item in idAddressOptions"
                  :key="item.id || item.idLocationCode"
                  :label="`${item.idLocationCode} ${item.idLocationName ? `(${item.idLocationName})` : ''}`"
                  :value="item.idLocationCode"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :xs="24" :sm="24" :md="8">
            <el-form-item label="ID位置">
              <el-input v-model="inspectionForm.idLocationName" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="点检内容">
              <el-input
                v-model="inspectionForm.inspectionContent"
                type="textarea"
                :rows="2"
                placeholder="点检内容"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="定性标准">
              <el-input
                v-model="inspectionForm.qualitativeStandard"
                type="textarea"
                :rows="2"
                placeholder="定性标准"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="定量标准">
              <el-input
                v-model="inspectionForm.quantitativeStandard"
                type="textarea"
                :rows="2"
                maxlength="255"
                show-word-limit
                placeholder="如：80.00 或文字说明"
                class="sel-full"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="drawer-footer">
          <el-button @click="inspectionDrawerVisible = false">取消</el-button>
          <el-button
            type="primary"
            :loading="inspectionDrawerSaving"
            @click="
              inspectionDrawerMode === 'create' ? submitCreateInspection() : submitEditInspection()
            "
          >
            保存
          </el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<style scoped>
.standard-page {
  min-height: calc(100vh - 36px);
  padding: 10px;
  background: #f5f7fa;
}
.page-breadcrumb {
  margin-bottom: 8px;
  color: #2f3b52;
  font-size: 14px;
  font-weight: 700;
}
.page-content {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 8px;
  min-height: calc(100vh - 90px);
}
.tree-panel,
.table-panel {
  border: 1px solid #d9e2ef;
  background: #fff;
}
.tree-panel-header {
  padding: 10px 14px;
  border-bottom: 1px solid #e6edf6;
  color: #33507b;
  font-size: 13px;
  font-weight: 700;
}
.tree-content {
  padding: 10px 12px;
}
.line-block + .line-block {
  margin-top: 12px;
}
.line-title {
  margin-bottom: 6px;
  font-weight: 700;
  cursor: pointer;
}
.line-title.active {
  color: #2f76e8;
}
.unit-item {
  padding: 8px 10px;
  border-radius: 4px;
  cursor: pointer;
}
.unit-item:hover {
  background: #f2f7ff;
}
.unit-item.active {
  background: #eaf2ff;
  color: #2f76e8;
}
.unit-row {
  display: flex;
  gap: 6px;
  font-weight: 700;
}
.device-list {
  margin-top: 4px;
  margin-left: 18px;
}
.device-item {
  padding: 3px 0;
  color: #5a6a85;
  font-size: 12px;
  cursor: pointer;
}
.device-item.active {
  color: #2f76e8;
  font-weight: 700;
}
.table-panel {
  padding: 10px 12px;
  overflow: auto;
}
.toolbar-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.toolbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: flex-end;
}
.selected-device {
  color: #47566b;
  font-size: 13px;
}
.toolbar-actions {
  display: flex;
  gap: 8px;
}
.import-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}
.template-link {
  color: #2f76e8;
  font-size: 13px;
  text-decoration: none;
}
.template-link:hover {
  text-decoration: underline;
}
.hidden-file-input {
  display: none;
}
.filter-bar {
  display: grid;
  grid-template-columns: repeat(4, minmax(220px, 1fr));
  gap: 18px;
  margin-bottom: 16px;
}
.filter-item {
  display: flex;
  align-items: center;
  gap: 12px;
}
.filter-label {
  min-width: 78px;
  font-size: 13px;
}
.standard-table :deep(.table-header-cell) {
  background: #e7f0ff;
  color: #365078;
  font-size: 12px;
  font-weight: 700;
}
.sel-full {
  width: 100%;
  min-width: 0;
}
.drawer-form {
  padding-right: 8px;
}
.drawer-form-3col :deep(.el-form-item) {
  margin-bottom: 14px;
}
.drawer-form-3col :deep(.el-form-item:last-child) {
  margin-bottom: 0;
}
.drawer-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
