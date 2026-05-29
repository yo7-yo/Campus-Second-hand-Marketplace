<template>
  <div class="personal-page">
    <button class="back-btn" @click="$router.push('/market')">返回市场</button>
    <div v-if="authStore.isLoggedIn" class="personal-container">
      <div class="user-header">
        <div class="user-info">
          <div class="avatar-large">{{ authStore.currentUser?.username?.charAt(0) || '用' }}</div>
          <div class="user-details">
            <h2 class="username">{{ authStore.currentUser?.username || '用户' }}</h2>
            <div class="info-row">信用分：<span class="credit-tag">{{ authStore.currentUser?.creditScore ?? 100 }}</span></div>
          </div>
        </div>
        <el-button type="danger" plain @click="logout">退出登录</el-button>
      </div>

      <!-- 统计数据 -->
      <div class="stats-section">
        <div class="stat-card" @click="$router.push('/publish-history')">
          <div class="stat-number">发布</div>
          <div class="stat-label">我的发布</div>
        </div>
        <div class="stat-card" @click="$router.push('/orders')">
          <div class="stat-number">订单</div>
          <div class="stat-label">我的订单</div>
        </div>
        <div class="stat-card" @click="$router.push('/chat')">
          <div class="stat-number">消息</div>
          <div class="stat-label">我的消息</div>
        </div>
      </div>

      <!-- 功能菜单 -->
      <div class="menu-section">
        <div class="menu-card">
          <div class="menu-item" @click="$router.push('/publish-history')">
            <div class="menu-icon">📝</div>
            <div class="menu-content">
              <div class="menu-title">我的发布</div>
              <div class="menu-desc">查看已发布的闲置物品</div>
            </div>
            <div class="menu-arrow">></div>
          </div>
          <div class="menu-item" @click="$router.push('/orders')">
            <div class="menu-icon">📦</div>
            <div class="menu-content">
              <div class="menu-title">我的订单</div>
              <div class="menu-desc">查看订单状态和物流</div>
            </div>
            <div class="menu-arrow">></div>
          </div>
          <div class="menu-item" @click="$router.push('/chat')">
            <div class="menu-icon">💬</div>
            <div class="menu-content">
              <div class="menu-title">我的消息</div>
              <div class="menu-desc">查看聊天记录</div>
            </div>
            <div class="menu-arrow">></div>
          </div>
        </div>
      </div>

      <!-- 其他功能 -->
      <div class="menu-section">
        <div class="menu-card">
          <div class="menu-item" @click="$router.push('/publish')">
            <div class="menu-icon">📝</div>
            <div class="menu-content">
              <div class="menu-title">发布闲置</div>
              <div class="menu-desc">发布新的闲置物品</div>
            </div>
            <div class="menu-arrow">></div>
          </div>
          <div class="menu-item" @click="handleSettings">
            <div class="menu-icon">⚙️</div>
            <div class="menu-content">
              <div class="menu-title">设置</div>
              <div class="menu-desc">账号设置和隐私</div>
            </div>
            <div class="menu-arrow">></div>
          </div>
        </div>
      </div>
    </div>

    <!-- 未登录：显示提示面板 -->
    <el-card v-else class="unlogged-card" shadow="never">
      <div class="unlogged-content">
        <h2>需要登录</h2>
        <p>您目前处于未登录状态，查看个人主页需要先登录您的账号。</p>
        <el-button type="primary" color="#ff6a00" size="large" @click="router.push('/login')">立即登录 / 注册</el-button>
        <el-button size="large" @click="$router.push('/market')">返回市场</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()

function logout() {
  authStore.logout()
  ElMessage.info('已退出登录')
  router.push('/market')
}

function handleSettings() {
  ElMessage.info('设置功能开发中')
}
</script>

<style scoped>
.personal-page {
  width: 100%;
  min-height: calc(100vh - 64px);
  background: linear-gradient(180deg, #fff5eb 0%, #f5f7fa 100%);
  padding: 20px;
  box-sizing: border-box;
}

.back-btn {
  position: fixed;
  top: 70px;
  left: 20px;
  z-index: 10000;
  background: #ff6a00;
  color: #fff;
  border: none;
  padding: 12px 24px;
  border-radius: 6px;
  font-size: 18px;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.back-btn:hover {
  background: #ff5500;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
  transform: translateX(-2px);
}

.personal-container {
  max-width: 800px;
  margin: 0 auto;
}

/* 用户信息头部 */
.user-header {
  background: #fff;
  border-radius: 16px;
  padding: 30px;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.avatar-large {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #ff9a44, #ff6a00);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #fff;
  font-weight: bold;
}

.user-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.username {
  margin: 0;
  font-size: 24px;
  color: #333;
}

.info-row {
  font-size: 14px;
  color: #666;
}

.credit-tag {
  color: #67c23a;
  font-weight: bold;
}

/* 统计数据 */
.stats-section {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px 16px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
}

.stat-number {
  font-size: 32px;
  font-weight: bold;
  color: #ff6a00;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

/* 功能菜单 */
.menu-section {
  margin-bottom: 20px;
}

.menu-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 18px 24px;
  cursor: pointer;
  transition: background 0.2s;
  border-bottom: 1px solid #f5f5f5;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-item:hover {
  background: #fff5eb;
}

.menu-icon {
  font-size: 24px;
  margin-right: 16px;
  width: 40px;
  text-align: center;
}

.menu-content {
  flex: 1;
}

.menu-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-bottom: 4px;
}

.menu-desc {
  font-size: 13px;
  color: #999;
}

.menu-arrow {
  font-size: 16px;
  color: #ccc;
}

/* 未登录状态卡片 */
.unlogged-card {
  max-width: 500px;
  margin: 100px auto;
  border-radius: 12px;
  text-align: center;
  padding: 60px 20px;
}

.unlogged-content h2 {
  color: #333;
  margin-bottom: 15px;
}

.unlogged-content p {
  color: #666;
  margin-bottom: 30px;
}
</style>
