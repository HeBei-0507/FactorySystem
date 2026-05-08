<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowDown,
  Bell,
  CollectionTag,
  Connection,
  Guide,
  Finished,
  Management,
  Memo,
  OfficeBuilding,
  Promotion,
  Tickets,
  User,
  Warning,
  WarningFilled,
  FirstAidKit,
  House
} from '@element-plus/icons-vue'
import { getUserInfo, logout as logoutRequest } from '@/api/user'
import { useUserStore } from '@/stores/user'
import { storeToRefs } from 'pinia'
import { unwrapData } from '@/util/result'

const router = useRouter()
const userStore = useUserStore()
const { displayName, profile } = storeToRefs(userStore)

const activeMenuGroups = ref(['基础信息管理'])

const subLine = computed(() => {
  const p = profile.value
  if (!p) return '天津职业师范大学-TSZsh01'
  const ph = p.phone
  if (typeof ph === 'string' && ph) return ph
  return '天津职业师范大学-TSZsh01'
})

async function refreshUserFromServer() {
  const id = userStore.userId
  if (id == null) return
  try {
    const raw = await getUserInfo(id)
    const u = unwrapData(raw)
    if (u == null || typeof u !== 'object') return
    userStore.mergeFromServerUser(u)
  } catch {
    // 使用 store / 本地缓存即可
  }
}

onMounted(() => {
  refreshUserFromServer()
})

async function handleUserCommand(cmd) {
  if (cmd !== 'logout') return
  try {
    await logoutRequest()
    ElMessage.success('已退出登录')
  } catch {
    ElMessage.info('已清除本地登录状态')
  } finally {
    userStore.clearUser()
    router.push('/login')
  }
}

const reminderList = [
  {
    title: '设备巡检提醒',
    content: '今日共有 12 台设备需要完成例行巡检，请及时处理。',
    level: '高优先级',
    tagType: 'danger'
  },
  {
    title: '合同即将到期',
    content: '供应商“华北智造”合作合同将在 7 天后到期。',
    level: '待跟进',
    tagType: 'warning'
  },
  {
    title: '仓库库存预警',
    content: '标签耗材库存低于安全阈值，建议尽快补货。',
    level: '提醒',
    tagType: 'info'
  }
]

const todoList = [
  { title: '处理设备迁移申请', time: '09:30', status: '待审批', type: 'warning' },
  { title: '核对供应商报价单', time: '11:00', status: '处理中', type: 'primary' },
  { title: '更新本周盘点结果', time: '16:30', status: '待提交', type: 'success' }
]

const noticeList = [
  {
    title: '五一节后安全检查通知',
    summary: '请各部门于本周五前完成所属设备与仓储区域安全排查。',
    time: '今天 08:30'
  },
  {
    title: '系统维护公告',
    summary: '平台将于本周六 22:00 - 23:00 进行升级维护。',
    time: '昨天 17:20'
  },
  {
    title: '编码规则更新提醒',
    summary: '物料编码新增二级分类字段，请按新模板录入。',
    time: '04-20 14:15'
  }
]

const menuGroups = [
  {
    title: '基础管理',
    items: [
      { label: '设备单元管理', icon: OfficeBuilding, path: '/base-info/device-unit' },
      { label: '设备档案维护', icon: Memo, path: '/base-info/device-file' },
      { label: '物料代码管理', icon: Tickets, path: '/base-info/material-code' },
      { label: 'ID位置管理', icon: Connection, path: '/base-info/id-address' },
      { label: '供应商管理', icon: Management, path: '/base-info/supplier-manage' }
    ]
  },
  {
    title: '标准管理',
    items: [
      { label: '点检标准管理', icon: Memo, path: '/base-info/inspection-standard' },
      { label: '润滑标准管理', icon: CollectionTag, path: '/base-info/lubrication-standard' },
      { label: '检修标准项目管理', icon: FirstAidKit, path: '/base-info/maintenance-standard' }
    ]
  },
  {
    title: '点检管理',
    items: [{ label: '点检路线管理', icon: Guide, path: '/inspection/inspection-route' }]
  },
  {
    title: '巡检管理',
    items: [{ label: '巡检信息维护', icon: Promotion, path: '/patrol/patrol-info' }]
  },
  {
    title: '检修管理',
    items: [
      {
        label: '检修计划管理',
        icon: FirstAidKit,
        children: [
          { label: '检修计划编制', path: '/maintenance-manage/make-plan' },
          { label: '检修计划审核', path: '/maintenance-manage/plan-approve' }
        ]
      },
      {
        label: '工单管理',
        icon: Memo,
        children: [
          { label: '工单维护', path: '/maintenance-manage/ticket-maintain' },
          { label: '工单审核', path: '/maintenance-manage/ticket-approve' },
          { label: '工单查询', path: '/maintenance-manage/ticket-lookup' }
        ]
      }
    ]
  },
  {
    title: '物料管理',
    items: [
      { label: '库区库位管理', icon: House, path: '/material-manage/warehouse-manage' },
      { label: '入库申请', icon: House, path: '/material-manage/inbound-request' }
    ]
  },
  {
    title: '状态管理',
    items: [
      {
        label: '异常管理',
        icon: Warning,
        children: [
          { label: '异常录入', path: '/status-manage/abnormality-manage/abnormality-input' },
          { label: '异常处理', path: '/status-manage/abnormality-manage/abnormality-deal' },
          { label: '异常审核', path: '/status-manage/abnormality-manage/abnormality-approve' }
        ]
      },
      {
        label: '故障管理',
        icon: WarningFilled,
        children: [
          { label: '故障录入', path: '/status-manage/failure-manage/failure-input' },
          { label: '故障处理', path: '/status-manage/failure-manage/failure-deal' },
          { label: '故障审核', path: '/status-manage/failure-manage/failure-approve' }
        ]
      }
    ]
  },
  { title: '状态检测项', items: [] }
]

function handleMenuClick(item) {
  if (!item.path) return
  const { href } = router.resolve(item.path)
  window.open(href, '_blank')
}

function hasMenuChildren(item) {
  return !!(item && Array.isArray(item.children) && item.children.length)
}
</script>

<template>
  <el-container class="page">
    <el-header class="topbar">
      <div class="brand">
        <div class="brand-badge">高仁</div>
        <div class="brand-text">
          <div class="brand-line1">天津高仁科技有限公司</div>
          <div class="brand-line2">智能点检系统</div>
        </div>
      </div>
      <div class="topbar-right">
        <span class="link-like">操作手册</span>
        <el-dropdown trigger="click" @command="handleUserCommand">
          <span class="user-block">
            <el-icon class="user-icon"><User /></el-icon>
            <span class="user-name">{{ displayName }}</span>
            <el-icon class="user-caret"><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <span class="user-sub">{{ subLine }}</span>
      </div>
    </el-header>

    <el-container class="main-layout">
      <el-aside width="210px" class="sidebar">
        <el-scrollbar>
          <el-collapse v-model="activeMenuGroups" class="menu-collapse">
            <el-collapse-item
              v-for="group in menuGroups"
              :key="group.title"
              :name="group.title"
              class="menu-group"
            >
              <template #title>
                <span class="group-title">{{ group.title }}</span>
              </template>
              <div v-if="group.items.length" class="menu-list">
                <div v-for="item in group.items" :key="item.label" class="menu-node">
                  <div
                    class="menu-item"
                    :class="{ 'menu-item-parent': hasMenuChildren(item) }"
                    @click="hasMenuChildren(item) ? null : handleMenuClick(item)"
                  >
                    <div class="menu-item-main">
                      <el-icon><component :is="item.icon" /></el-icon>
                      <span>{{ item.label }}</span>
                    </div>
                  </div>

                  <div v-if="hasMenuChildren(item)" class="sub-menu-list">
                    <div
                      v-for="subItem in item.children"
                      :key="subItem.label"
                      class="sub-menu-item"
                      @click="handleMenuClick(subItem)"
                    >
                      <span>{{ subItem.label }}</span>
                    </div>
                  </div>
                </div>
              </div>
              <div v-else class="menu-empty">暂无子项</div>
            </el-collapse-item>
          </el-collapse>
        </el-scrollbar>
      </el-aside>

      <el-main class="main">
        <div class="grid">
          <el-card shadow="never" class="panel reminder-panel">
            <template #header>
              <div class="panel-head"><span class="panel-name">提醒</span></div>
            </template>
            <div class="reminder-list">
              <div class="reminder-item" v-for="item in reminderList" :key="item.title">
                <div class="reminder-icon">
                  <el-icon><Bell /></el-icon>
                </div>
                <div class="reminder-body">
                  <div class="reminder-top">
                    <span class="item-title">{{ item.title }}</span>
                    <el-tag size="small" :type="item.tagType" effect="light">
                      {{ item.level }}
                    </el-tag>
                  </div>
                  <div class="item-text">{{ item.content }}</div>
                </div>
              </div>
            </div>
          </el-card>

          <el-card shadow="never" class="panel calendar-panel">
            <div class="calendar-wrap">
              <div class="calendar-head">
                <el-button text circle><span>«</span></el-button>
                <el-button text circle><span>‹</span></el-button>
                <span class="calendar-title">2026年5月</span>
                <div class="calendar-actions">
                  <el-button text circle><span>›</span></el-button>
                  <el-button text circle><span>»</span></el-button>
                </div>
              </div>
              <el-calendar />
              <div class="calendar-empty">
                <el-icon><Finished /></el-icon>
                <span>暂无数据</span>
              </div>
            </div>
          </el-card>

          <div class="bottom-row">
            <el-card shadow="never" class="panel half-panel">
              <template #header>
                <div class="panel-head">
                  <span class="panel-name">待办</span>
                  <el-button text type="primary">查看更多</el-button>
                </div>
              </template>
              <div class="list-wrap">
                <div class="list-item" v-for="item in todoList" :key="item.title">
                  <div>
                    <div class="item-title">{{ item.title }}</div>
                    <div class="item-time">{{ item.time }}</div>
                  </div>
                  <el-tag :type="item.type" effect="light">{{ item.status }}</el-tag>
                </div>
              </div>
            </el-card>

            <el-card shadow="never" class="panel half-panel">
              <template #header>
                <div class="panel-head">
                  <span class="panel-name">通知</span>
                  <el-button text type="primary">查看更多</el-button>
                </div>
              </template>
              <div class="list-wrap">
                <div class="list-item" v-for="item in noticeList" :key="item.title">
                  <div class="notice-main">
                    <div class="notice-title">
                      <el-icon class="notice-icon"><Opportunity /></el-icon>
                      <span class="item-title">{{ item.title }}</span>
                    </div>
                    <div class="item-text">{{ item.summary }}</div>
                  </div>
                  <div class="item-time">{{ item.time }}</div>
                </div>
              </div>
            </el-card>
          </div>
        </div>
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
:global(body) {
  margin: 0;
  min-width: 1280px;
  background: #f5f7fa;
  color: #303133;
  font-family: 'PingFang SC', 'Microsoft YaHei', 'Helvetica Neue', Arial, sans-serif;
}
:global(#app) {
  min-height: 100vh;
}
:global(*) {
  box-sizing: border-box;
}
.page {
  min-height: 100vh;
  background: #f5f7fa;
}
.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 56px;
  padding: 0 14px;
  background: linear-gradient(90deg, #1c55c8, #2b61ca);
  color: #fff;
}
.brand {
  display: flex;
  align-items: center;
  gap: 10px;
}
.brand-badge {
  display: grid;
  place-items: center;
  width: 38px;
  height: 28px;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.18);
  font-size: 14px;
  font-weight: 700;
}
.brand-line1 {
  font-size: 12px;
}
.brand-line2 {
  font-size: 18px;
  font-weight: 700;
}
.topbar-right {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 12px;
}
.link-like {
  cursor: pointer;
  opacity: 0.95;
}
.user-block {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  padding: 4px 2px;
  outline: none;
  max-width: 200px;
}
.user-block:hover {
  opacity: 0.95;
}
.user-icon {
  font-size: 16px;
  flex-shrink: 0;
}
.user-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.user-caret {
  font-size: 12px;
  opacity: 0.85;
}
.user-sub {
  opacity: 0.9;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.main-layout {
  height: calc(100vh - 56px);
}
.sidebar {
  background: #fff;
  border-right: 1px solid #e5eaf3;
}
.menu-collapse {
  padding: 12px 10px 24px;
  border: 0;
  background: transparent;
}
.menu-collapse :deep(.el-collapse-item__header) {
  height: 46px;
  padding: 0 14px;
  border: 0;
  background: #fff;
  font-size: 14px;
  font-weight: 700;
}
.menu-collapse :deep(.el-collapse-item__wrap) {
  border: 0;
  background: transparent;
}
.menu-collapse :deep(.el-collapse-item__content) {
  padding: 2px 0 8px;
}
.menu-group {
  margin-bottom: 6px;
}
.group-title {
  font-size: 14px;
}
.menu-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.menu-node {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.menu-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  min-height: 40px;
  padding: 0 14px 0 22px;
  border-radius: 6px;
  color: #606266;
  font-size: 14px;
  cursor: pointer;
}
.menu-item-main {
  display: flex;
  align-items: center;
  gap: 10px;
}
.menu-item-parent {
  cursor: default;
}
.menu-item:hover {
  background: #f5f9ff;
  color: #409eff;
}
.sub-menu-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 0 0 4px 46px;
}
.sub-menu-item {
  display: flex;
  align-items: center;
  min-height: 34px;
  padding: 0 12px;
  border-radius: 6px;
  color: #7a869a;
  font-size: 13px;
  cursor: pointer;
}
.sub-menu-item:hover {
  background: #f5f9ff;
  color: #409eff;
}
.menu-empty {
  padding: 0 14px 10px 22px;
  color: #909399;
  font-size: 13px;
}
.main {
  padding: 6px;
}
.grid {
  display: grid;
  grid-template-columns: minmax(560px, 1.7fr) minmax(260px, 1fr);
  grid-template-rows: minmax(330px, 1fr) minmax(260px, 0.9fr);
  gap: 6px;
  min-height: calc(100vh - 68px);
}
.panel {
  border: 1px solid #e9eef5;
  border-radius: 0;
  background: #fff;
  box-shadow: none;
}
.panel :deep(.el-card__header) {
  padding: 10px 14px;
  border-bottom: 1px solid #edf1f7;
}
.panel :deep(.el-card__body) {
  height: calc(100% - 45px);
  padding: 12px 14px;
}
.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.panel-name {
  font-size: 16px;
  font-weight: 700;
}
.reminder-panel {
  grid-column: 1 / 2;
  grid-row: 1 / 2;
}
.calendar-panel {
  grid-column: 2 / 3;
  grid-row: 1 / 3;
}
.bottom-row {
  grid-column: 1 / 2;
  grid-row: 2 / 3;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 6px;
}
.calendar-panel :deep(.el-card__body) {
  display: flex;
  height: 100%;
  flex-direction: column;
  padding: 10px;
}
.calendar-wrap {
  display: flex;
  min-height: 100%;
  flex-direction: column;
}
.calendar-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 0 4px 8px;
}
.calendar-title {
  flex: 1;
  text-align: center;
  font-size: 18px;
  font-weight: 700;
}
.calendar-actions {
  display: flex;
  gap: 4px;
}
.calendar-panel :deep(.el-calendar) {
  flex: 1;
}
.calendar-panel :deep(.el-calendar__header) {
  display: none;
}
.calendar-panel :deep(.el-calendar-table td) {
  border-color: #eef2f8;
}
.calendar-panel :deep(.el-calendar-table .el-calendar-day) {
  height: 54px;
  padding: 0;
}
.calendar-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 18px 0 6px;
  color: #909399;
}
.reminder-list,
.list-wrap {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.reminder-item {
  display: flex;
  gap: 14px;
  padding: 14px;
  border: 1px solid #edf2f9;
  background: #fff;
}
.reminder-icon {
  display: grid;
  flex: 0 0 40px;
  width: 40px;
  height: 40px;
  place-items: center;
  background: rgba(64, 158, 255, 0.1);
  color: #409eff;
  font-size: 18px;
}
.reminder-body,
.notice-main {
  flex: 1;
}
.reminder-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
}
.item-title {
  font-size: 14px;
  font-weight: 700;
}
.item-text {
  color: #606266;
  line-height: 1.7;
  font-size: 12px;
}
.list-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px dashed #e8edf5;
}
.list-item:last-child {
  border-bottom: 0;
}
.item-time {
  margin-top: 6px;
  color: #909399;
  font-size: 12px;
}
.notice-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}
.notice-icon {
  color: #409eff;
}
</style>
