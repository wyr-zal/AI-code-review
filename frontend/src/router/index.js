import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'

const routes = [
  {
    path: '/',
    redirect: '/dashboard'
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
      // 验证token是否过期
      if (isTokenExpired(token)) {
        ElMessage.warning('登录已过期，请重新登录')
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        next('/login')
      } else {
        next()
      }
    } else {
      ElMessage.warning('请先登录')
      next('/login')
    }
  } else {
    // 已登录用户访问登录页，重定向到控制台
    if (to.path === '/login' && token && !isTokenExpired(token)) {
      next('/dashboard')
    } else {
      next()
    }
  }
})

/**
 * 检查JWT是否过期
 * @param {string} token - JWT token
 * @returns {boolean} true表示已过期，false表示未过期
 */
function isTokenExpired(token) {
  if (!token) return true

  try {
    // JWT格式：header.payload.signature
    const parts = token.split('.')
    if (parts.length !== 3) {
      return true
    }

    // 解码payload部分
    const payload = JSON.parse(atob(parts[1]))

    // 检查是否有exp字段
    if (!payload.exp) {
      // 如果没有过期时间，认为token有效
      return false
    }

    // exp是秒级时间戳，需要转换为毫秒
    const expTime = payload.exp * 1000
    const currentTime = Date.now()

    // 如果当前时间超过过期时间，返回true
    return currentTime >= expTime
  } catch (error) {
    console.error('Token解析失败:', error)
    // 解析失败认为token无效
    return true
  }
}

export default router
