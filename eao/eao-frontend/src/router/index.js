import { createRouter, createWebHistory } from 'vue-router'

const APP_NAME = '设备管理与智能运维系统'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/Login.vue'),
      meta: { public: true, title: '登录' }
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/views/Register.vue'),
      meta: { public: true, title: '注册' }
    },
    {
      path: '/',
      name: 'Home',
      component: () => import('@/views/layout.vue'),
      meta: { title: '首页' }
    },
    {
      path: '/base-info/device-unit',
      name: 'DeviceUnit',
      component: () => import('@/views/BaseInfo/DeviceUnit.vue'),
      meta: { title: '设备单元管理' }
    },
    {
      path: '/base-info/device-file',
      name: 'DeviceFile',
      component: () => import('@/views/BaseInfo/DeviceFile.vue'),
      meta: { title: '设备档案维护' }
    },
    {
      path: '/base-info/material-code',
      name: 'MaterialCode',
      component: () => import('@/views/BaseInfo/MaterialCode.vue'),
      meta: { title: '物料代码管理' }
    },
    {
      path: '/base-info/id-address',
      name: 'IdAddress',
      component: () => import('@/views/BaseInfo/IDAddress.vue'),
      meta: { title: 'ID位置管理' }
    },
    {
      path: '/base-info/supplier-manage',
      name: 'SupplierManage',
      component: () => import('@/views/BaseInfo/SupplierManege.vue'),
      meta: { title: '供应商管理' }
    },
    {
      path: '/base-info/inspection-standard',
      name: 'InspectionStandard',
      component: () => import('@/views/BaseInfo/InspectionStandard.vue'),
      meta: { title: '点检标准管理' }
    },
    {
      path: '/base-info/lubrication-standard',
      name: 'LubricationStandard',
      component: () => import('@/views/BaseInfo/LubricationStandard.vue'),
      meta: { title: '润滑标准管理' }
    },
    {
      path: '/base-info/maintenance-standard',
      name: 'MaintenanceStandard',
      component: () => import('@/views/BaseInfo/MaintenanceStandard.vue'),
      meta: { title: '检修标准项目管理' }
    },
    {
      path: '/inspection/inspection-route',
      name: 'InspectionRoute',
      component: () => import('@/views/Inspection/InspectionRoute.vue'),
      meta: { title: '点检路线管理' }
    },
    {
      path: '/inspection/inspection-record-query',
      name: 'InspectionRecordQuery',
      component: () => import('@/views/Inspection/InspectionRecordQuery.vue'),
      meta: { title: '点检实绩查询' }
    },
    {
      path: '/inspection/inspection-plan-query',
      name: 'InspectionPlanQuery',
      component: () => import('@/views/Inspection/InspectionPlanQuery.vue'),
      meta: { title: '点检计划查询' }
    },
    {
      path: '/patrol/patrol-info',
      name: 'PatrolInfo',
      component: () => import('@/views/Patrol/PatrolInfo.vue'),
      meta: { title: '巡检信息维护' }
    },
    {
      path: '/status-manage/abnormality-manage/abnormality-input',
      name: 'AbnormalityInput',
      component: () => import('@/views/StatusManage/AbnormalityManage/AbnormalityInput.vue'),
      meta: { title: '异常录入' }
    },
    {
      path: '/status-manage/abnormality-manage/abnormality-deal',
      name: 'AbnormalityDeal',
      component: () => import('@/views/StatusManage/AbnormalityManage/AbnormalityDeal.vue'),
      meta: { title: '异常处理' }
    },
    {
      path: '/status-manage/abnormality-manage/abnormality-approve',
      name: 'AbnormalityApprove',
      component: () => import('@/views/StatusManage/AbnormalityManage/AbnormalityApprove.vue'),
      meta: { title: '异常审核' }
    },
    {
      path: '/status-manage/failure-manage/failure-input',
      name: 'FailureInput',
      component: () => import('@/views/StatusManage/FailureManage/FailureInput.vue'),
      meta: { title: '故障录入' }
    },
    {
      path: '/status-manage/failure-manage/failure-deal',
      name: 'FailureDeal',
      component: () => import('@/views/StatusManage/FailureManage/FailureDeal.vue'),
      meta: { title: '故障处理' }
    },
    {
      path: '/status-manage/failure-manage/failure-approve',
      name: 'FailureApprove',
      component: () => import('@/views/StatusManage/FailureManage/FailureApprove.vue'),
      meta: { title: '故障审核' }
    },
    {
      path: '/maintenance-manage/make-plan',
      name: 'MakePlan',
      component: () => import('@/views/MaintenanceManage/MaintainPlanManage/MakePlan.vue'),
      meta: { title: '检修计划编制' }
    },
    {
      path: '/maintenance-manage/plan-approve',
      name: 'PlanApprove',
      component: () => import('@/views/MaintenanceManage/MaintainPlanManage/PlanApprove.vue'),
      meta: { title: '检修计划审核' }
    },
    {
      path: '/maintenance-manage/ticket-maintain',
      name: 'TicketMaintain',
      component: () => import('@/views/MaintenanceManage/TicketManage/TicketMaintain.vue'),
      meta: { title: '工单维护' }
    },
    {
      path: '/maintenance-manage/ticket-approve',
      name: 'TicketApprove',
      component: () => import('@/views/MaintenanceManage/TicketManage/TicketApprove.vue'),
      meta: { title: '工单审核' }
    },
    {
      path: '/maintenance-manage/ticket-lookup',
      name: 'TicketLookup',
      component: () => import('@/views/MaintenanceManage/TicketManage/TicketLookup.vue'),
      meta: { title: '工单查询' }
    },
    {
      path: '/material-manage/warehouse-manage',
      name: 'WarehouseManage',
      component: () => import('@/views/MaterialManage/WarehouseManage.vue'),
      meta: { title: '库区库位管理' }
    },
    {
      path: '/material-manage/inbound-request',
      name: 'InboundRequest',
      component: () => import('@/views/MaterialManage/InboundRequest.vue'),
      meta: { title: '入库申请' }
    },
    {
      path: '/material-manage/inbound-approve',
      name: 'InboundApprove',
      component: () => import('@/views/MaterialManage/InboundApprove.vue'),
      meta: { title: '入库审核' }
    }
  ]
})

router.beforeEach((to) => {
  const pageTitle = to.meta?.title || to.name || APP_NAME
  document.title = `${pageTitle} - ${APP_NAME}`

  if (to.meta.public) {
    return true
  }
  const tokenKey = import.meta.env.VITE_USER_TOKEN_KEY
  if (tokenKey && localStorage.getItem(tokenKey)) {
    return true
  }
  return { path: '/login', query: { redirect: to.fullPath } }
})

export default router
