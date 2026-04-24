import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: () => import('@/views/layout.vue'),
    },
    {
      path: '/base-info/device-unit',
      component: () => import('@/views/BaseInfo/DeviceUnit.vue'),
    },
    {
      path: '/base-info/device-file',
      component: () => import('@/views/BaseInfo/DeviceFile.vue'),
    },
    {
      path: '/base-info/material-code',
      component: () => import('@/views/BaseInfo/MaterialCode.vue'),
    },
    {
      path: '/base-info/id-address',
      component: () => import('@/views/BaseInfo/IDAddress.vue'),
    },
    {
      path: '/base-info/supplier-manage',
      component: () => import('@/views/BaseInfo/SupplierManege.vue'),
    },
  ],
})

export default router
