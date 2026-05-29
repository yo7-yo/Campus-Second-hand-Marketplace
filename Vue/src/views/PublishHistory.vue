<template>
  <div class="publish-history-page">
    <div v-if="authStore.isLoggedIn" class="history-container">
      <div class="page-header">
        <h2>我的发布</h2>
        <el-button type="primary" color="#ff6a00" @click="$router.push('/publish')">发布新闲置</el-button>
      </div>

      <div class="history-list" v-if="publishList.length > 0">
        <el-card v-for="item in publishList" :key="item.id" class="history-card" shadow="hover">
          <div class="history-item">
            <div class="item-image">
              <div class="img-placeholder">{{ item.title.charAt(0) }}</div>
            </div>
            <div class="item-info">
              <h3 class="item-title">{{ item.title }}</h3>
              <p class="item-desc">{{ item.description }}</p>
              <div class="item-meta">
                <span class="item-price">￥{{ item.price }}</span>
                <span class="item-time">{{ item.time }}</span>
                <el-tag :type="item.status === 'available' ? 'success' : 'info'" size="small">{{ item.statusLabel }}</el-tag>
              </div>
            </div>
            <div class="item-actions">
              <el-button text type="primary" @click="editItem(item)">编辑</el-button>
              <el-button text type="danger" @click="deleteItem(item)">删除</el-button>
            </div>
          </div>
        </el-card>
      </div>

      <el-empty v-else description="您还没有发布过闲置物品" />
    </div>

    <!-- 未登录：显示提示面板 -->
    <el-card v-else class="unlogged-card" shadow="never">
      <div class="unlogged-content">
        <h2>需要登录</h2>
        <p>您目前处于未登录状态，查看发布历史需要先登录您的账号。</p>
        <el-button type="primary" color="#ff6a00" size="large" @click="router.push('/login')">立即登录 / 注册</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const publishList = ref([])

async function loadPublishList() {
  if (!authStore.isLoggedIn || !authStore.currentUser) return
  try {
    const res = await request.get(`/products/owner/${authStore.currentUser.id}`)
    publishList.value = res.data.map(p => ({
      ...p,
      time: p.createdTime ? p.createdTime.substring(0, 10) : '',
      statusLabel: p.status === 'available' ? '在售' : '已售出'
    }))
  } catch (e) {
    publishList.value = []
  }
}

function editItem(item) {
  router.push(`/publish/${item.id}`)
}

async function deleteItem(item) {
  try {
    await ElMessageBox.confirm('确定要删除这件商品吗？', '确认删除', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await request.delete(`/products/${item.id}`)
    ElMessage.success('删除成功')
    loadPublishList()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

watch(() => authStore.isLoggedIn, (val) => {
  if (val) loadPublishList()
})

onMounted(() => {
  if (authStore.isLoggedIn) loadPublishList()
})
</script>

<style scoped>
.publish-history-page {
  width: 100%;
  padding: 20px;
  box-sizing: border-box;
  background: linear-gradient(180deg, #fff5eb 0%, #f5f7fa 100%);
  min-height: calc(100vh - 64px);
}

.history-container {
  max-width: 900px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
  color: #333;
}

.history-card {
  margin-bottom: 16px;
  border-radius: 12px;
}

.history-item {
  display: flex;
  align-items: center;
  gap: 20px;
}

.item-image {
  flex-shrink: 0;
}

.img-placeholder {
  width: 80px;
  height: 80px;
  background: #f0f2f5;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  color: #ccc;
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-title {
  margin: 0 0 8px;
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.item-desc {
  margin: 0 0 8px;
  font-size: 14px;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-meta {
  display: flex;
  align-items: center;
  gap: 16px;
}

.item-price {
  font-size: 18px;
  font-weight: bold;
  color: #ff4d4f;
}

.item-time {
  font-size: 13px;
  color: #999;
}

.item-actions {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
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
