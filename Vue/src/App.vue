<template>
  <div class="app-wrapper">
    <!-- 全局顶部导航栏 (登录页面不显示) -->
    <header class="global-header" v-if="route.name !== 'Login'">
      <div class="header-content">
        <div class="logo">
          <span class="logo-text">校园闲置交易平台</span>
          <span class="logo-sub">让闲置焕发新生</span>
        </div>

        <div class="user-actions">
          <!-- 点击头像进入登录界面 -->
          <div class="avatar-wrap" @click="goToLogin">
            <div class="avatar">头像</div>
            <span class="username">
              {{ authStore.isLoggedIn ? (authStore.currentUser?.username || '用户') : '未登录' }}
            </span>
          </div>

          <el-button
            v-if="authStore.currentUser?.isAdmin"
            class="publish-btn"
            color="#fff"
            @click="router.push('/admin')"
          >
            管理后台
          </el-button>

          <el-button
            type="primary"
            class="publish-btn"
            color="#ff6a00"
            @click="handlePublishClick"
          >
            发布闲置
          </el-button>
        </div>
      </div>
    </header>

    <!-- 路由视图 -->
    <main class="main-content" :class="{ 'no-padding': route.name === 'Login' }">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from './stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

onMounted(() => {
  authStore.restoreLogin()
})

function goToLogin() {
  if (authStore.isLoggedIn) {
    router.push('/personal')
  } else {
    router.push('/login')
  }
}

function handlePublishClick() {
  if (!authStore.isLoggedIn) {
    ElMessage.warning('发布闲置需要先登录')
    router.push('/login')
    return
  }

  router.push('/publish')
}
</script>

<style scoped>
.app-wrapper {
  min-height: 100vh;
  background: linear-gradient(180deg, #fff5eb 0%, #f5f7fa 100%);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Arial, sans-serif;
}

/* 导航栏样式：保持原有结构 */
.global-header {
  position: fixed;
  top: 0;
  left: 0;
  z-index: 999;
  box-sizing: border-box;
  width: 100%;
  padding: 0 20px;
  background: #ff9a44;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  height: 64px;
}

.main-content {
  box-sizing: border-box;
  width: 100%;
  padding-top: 64px;
}

.main-content.no-padding {
  padding-top: 0;
}

.logo {
  display: flex;
  flex-direction: column;
}

.logo-text {
  font-size: 20px;
  font-weight: bold;
  color: #fff;
}

.logo-sub {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.9);
}

.user-actions {
  display: flex;
  align-items: center;
  gap: 20px;
}

.avatar-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 5px 10px;
  cursor: pointer;
  border-radius: 20px;
  transition: background 0.2s;
}

.avatar-wrap:hover {
  background: rgba(255, 255, 255, 0.2);
}

.avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  font-size: 12px;
  color: #ff6a00;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 50%;
}

.username {
  font-size: 14px;
  color: #fff;
}

.publish-btn {
  font-weight: bold;
  border-radius: 20px;
  background: #fff;
  color: #ff6a00;
  border: none;
}

.publish-btn:hover {
  background: rgba(255, 255, 255, 0.9);
  color: #ff6a00;
}

/* 页面切换动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(8px);
}

</style>