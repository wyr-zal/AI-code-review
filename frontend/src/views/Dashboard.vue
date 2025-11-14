<template>
  <el-container class="dashboard-container">
    <!-- 侧边栏 -->
    <el-aside width="200px">
      <div class="logo">
        <h2>AI代码审查</h2>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#545c64"
        text-color="#fff"
        active-text-color="#ffd04b"
      >
        <el-menu-item index="/dashboard/review">
          <el-icon><DocumentAdd /></el-icon>
          <span>代码审查</span>
        </el-menu-item>
        <el-menu-item index="/dashboard/history">
          <el-icon><Document /></el-icon>
          <span>审查历史</span>
        </el-menu-item>
        <el-menu-item index="/dashboard/profile">
          <el-icon><User /></el-icon>
          <span>个人中心</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 主内容区 -->
    <el-container>
      <!-- 顶部导航栏 -->
      <el-header>
        <div class="header-content">
          <div class="breadcrumb">
            <el-icon><Location /></el-icon>
            <span>{{ pageTitle }}</span>
          </div>
          <div class="user-info">
            <el-dropdown @command="handleCommand">
              <span class="user-name">
                <el-avatar :size="32" :src="userStore.userInfo.avatar || undefined">
                  {{ userStore.userInfo.nickname?.charAt(0) || 'U' }}
                </el-avatar>
                <span>{{ userStore.userInfo.nickname || '用户' }}</span>
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-header>

      <!-- 内容区域 -->
      <el-main>
        <router-view v-slot="{ Component }">
          <keep-alive :include="['Review', 'History']">
            <component :is="Component" />
          </keep-alive>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessageBox } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)
const pageTitle = computed(() => route.meta.title || '控制台')

const handleCommand = (command) => {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      userStore.logout()
      router.push('/login')
    }).catch(() => {})
  } else if (command === 'profile') {
    router.push('/dashboard/profile')
  }
}
</script>

<style scoped>
.dashboard-container {
  height: 100vh;
  background: #f7fafc;
}

.el-aside {
  background: linear-gradient(180deg, #1e293b 0%, #0f172a 100%);
  overflow-x: hidden;
  transition: width 0.3s ease;
  box-shadow: 4px 0 24px rgba(0, 0, 0, 0.12);
}

.logo {
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
}

.logo h2 {
  font-size: 18px;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 0.5px;
}

.el-menu {
  border-right: none;
  background: transparent;
  padding: 12px;
}

.el-menu :deep(.el-menu-item) {
  border-radius: 12px;
  margin-bottom: 8px;
  transition: all 0.3s ease;
  font-weight: 500;
}

.el-menu :deep(.el-menu-item:hover) {
  background: rgba(102, 126, 234, 0.15);
  transform: translateX(4px);
}

.el-menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.el-menu :deep(.el-menu-item.is-active):hover {
  transform: translateX(0);
}

.el-header {
  background: #ffffff;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  padding: 0 32px;
  height: 70px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  position: relative;
  z-index: 10;
}

.header-content {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 18px;
  font-weight: 600;
  color: #1a1a2e;
}

.breadcrumb .el-icon {
  color: #667eea;
}

.user-info {
  display: flex;
  align-items: center;
}

.user-name {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 8px 16px;
  border-radius: 12px;
  transition: all 0.3s ease;
  font-weight: 500;
  color: #334155;
}

.user-name:hover {
  background: #f1f5f9;
  transform: translateY(-2px);
}

.user-name :deep(.el-avatar) {
  border: 2px solid #e2e8f0;
  transition: all 0.3s ease;
}

.user-name:hover :deep(.el-avatar) {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.el-main {
  background: #f7fafc;
  padding: 24px;
  overflow-y: auto;
  min-height: calc(100vh - 70px);
}

/* 内容区域淡入动画 */
.el-main :deep(> *) {
  animation: fadeIn 0.4s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 下拉菜单美化 */
:deep(.el-dropdown-menu) {
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  padding: 8px;
}

:deep(.el-dropdown-menu__item) {
  border-radius: 8px;
  margin: 2px 0;
  transition: all 0.2s ease;
}

:deep(.el-dropdown-menu__item:hover) {
  background: #f1f5f9;
  color: #667eea;
}
</style>
