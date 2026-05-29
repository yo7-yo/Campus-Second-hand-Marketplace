import { createRouter, createWebHistory } from 'vue-router'
import Market from '../views/Market.vue'

const routes = [
  { path: '/', redirect: '/market' }, // 默认跳转到市场页面
  { path: '/market', name: 'Market', component: Market },
  { path: '/publish', name: 'Publish', component: () => import('../views/Publish.vue') },
  { path: '/publish/:id', name: 'PublishEdit', component: () => import('../views/Publish.vue') },
  { path: '/orders', name: 'Orders', component: () => import('../views/Orders.vue') },
  { path: '/chat', name: 'Chat', component: () => import('../views/Chat.vue') },
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
  { path: '/publish-history', name: 'PublishHistory', component: () => import('../views/PublishHistory.vue') },
  { path: '/personal', name: 'Personal', component: () => import('../views/Personal.vue') },
  { path: '/admin', name: 'Admin', component: () => import('../views/Admin.vue') }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router