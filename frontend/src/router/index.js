import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/Dashboard.vue'),
    meta: { title: '控制台', requiresAuth: true },
    redirect: '/dashboard/review',
    children: [
      {
        path: 'review',
        name: 'Review',
        component: () => import('@/views/Review.vue'),
        meta: { title: '代码审查', requiresAuth: true }
      },
      {
        path: 'history',
        name: 'History',
        component: () => import('@/views/History.vue'),
        meta: { title: '审查历史', requiresAuth: true }
      },
      {
        path: 'detail/:id',
        name: 'Detail',
        component: () => import('@/views/Detail.vue'),
        meta: { title: '审查详情', requiresAuth: true }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: '个人中心', requiresAuth: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')

  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - AI代码审查平台` : 'AI代码审查平台'

  // 需要登录的页面
  if (to.meta.requiresAuth) {
    if (token) {
      next()
    } else {
      ElMessage.warning('请先登录')
      next('/login')
    }
  } else {
    // 已登录用户访问登录页，重定向到控制台
    if (to.path === '/login' && token) {
      next('/dashboard')
    } else {
      next()
    }
  }
})

export default router
