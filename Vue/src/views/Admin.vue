<template>
  <div class="admin-page">
    <el-card v-if="!authStore.isLoggedIn" class="unlogged-card" shadow="never">
      <div class="unlogged-content">
        <h2>需要登录</h2>
        <p>请先登录后再访问管理后台。</p>
        <el-button type="primary" color="#ff6a00" size="large" @click="router.push('/login')" class="login-btn">立即登录</el-button>
      </div>
    </el-card>

    <el-card v-else-if="!isAdmin" class="unlogged-card" shadow="never">
      <div class="unlogged-content">
        <h2>无权限</h2>
        <p>您不是管理员，无法访问此页面。</p>
        <el-button type="primary" color="#ff6a00" size="large" @click="router.push('/market')" class="login-btn">返回市场</el-button>
      </div>
    </el-card>

    <template v-else>
      <el-card shadow="never" class="admin-card">
        <template #header>
          <div class="card-header">
            <h3>管理后台</h3>
            <el-radio-group v-model="activeTab" size="small">
              <el-radio-button value="refunds">退款审核</el-radio-button>
              <el-radio-button value="complaints">投诉审核</el-radio-button>
            </el-radio-group>
          </div>
        </template>

        <!-- 退款审核 -->
        <div v-if="activeTab === 'refunds'">
          <el-table :data="pendingRefunds" style="width: 100%" v-loading="loading">
            <el-table-column prop="id" label="订单ID" width="80" />
            <el-table-column prop="productTitle" label="商品" min-width="180" show-overflow-tooltip />
            <el-table-column prop="buyerName" label="买家" width="100" />
            <el-table-column prop="sellerName" label="卖家" width="100" />
            <el-table-column prop="price" label="金额" width="100">
              <template #default="{ row }">
                <span class="price-text">￥{{ Number(row.price || 0).toFixed(2) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="refundReason" label="退款理由" min-width="200" show-overflow-tooltip />
            <el-table-column prop="refundResponse" label="卖家回应" min-width="200" show-overflow-tooltip>
              <template #default="{ row }">
                <span>{{ row.refundResponse || '未回应' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="refundStatus" label="状态" width="120" align="center">
              <template #default="{ row }">
                <el-tag :type="row.refundStatus === 'seller_responded' ? 'warning' : 'info'" size="small">
                  {{ row.refundStatus === 'pending' ? '待卖家回应' : '已回应' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180" align="center">
              <template #default="{ row }">
                <el-button type="success" size="small" @click="approveRefund(row)">批准退款</el-button>
                <el-button type="danger" size="small" @click="rejectRefund(row)">驳回</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="pendingRefunds.length === 0 && !loading" description="暂无待审核退款" />
        </div>

        <!-- 投诉审核 -->
        <div v-if="activeTab === 'complaints'">
          <el-table :data="pendingComplaints" style="width: 100%" v-loading="loading">
            <el-table-column prop="id" label="投诉ID" width="80" />
            <el-table-column prop="complainantName" label="投诉人" width="100" />
            <el-table-column prop="respondentName" label="被投诉人" width="100" />
            <el-table-column prop="productTitle" label="相关商品" min-width="150" show-overflow-tooltip />
            <el-table-column prop="reason" label="投诉理由" min-width="200" show-overflow-tooltip />
            <el-table-column prop="respondentReply" label="被投诉人回复" min-width="200" show-overflow-tooltip>
              <template #default="{ row }">
                <span :class="{ 'text-muted': !row.respondentReply }">{{ row.respondentReply || '未回复' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="createdTime" label="投诉时间" width="160">
              <template #default="{ row }">
                <span>{{ row.createdTime?.substring(0, 16)?.replace('T', ' ') || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="280" align="center">
              <template #default="{ row }">
                <div style="display: flex; gap: 8px; align-items: center;">
                  <el-input v-model="row._adminReply" placeholder="审核备注" size="small" style="width: 140px;" />
                  <el-button type="success" size="small" @click="resolveComplaint(row, true)">通过(扣20分)</el-button>
                  <el-button type="danger" size="small" @click="resolveComplaint(row, false)">驳回</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="pendingComplaints.length === 0 && !loading" description="暂无待审核投诉" />
        </div>
      </el-card>
    </template>
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

const isAdmin = ref(false)
const activeTab = ref('refunds')
const loading = ref(false)
const pendingRefunds = ref([])
const pendingComplaints = ref([])

async function checkAdmin() {
  if (!authStore.isLoggedIn || !authStore.currentUser) return
  try {
    const res = await request.get(`/users/check-admin/${authStore.currentUser.id}`)
    isAdmin.value = res.data.isAdmin
    if (isAdmin.value) {
      loadData()
    }
  } catch (e) {
    isAdmin.value = false
  }
}

async function loadData() {
  loading.value = true
  try {
    if (activeTab.value === 'refunds') {
      const res = await request.get('/orders/refunds/pending')
      pendingRefunds.value = res.data
    } else {
      const res = await request.get('/complaints/pending')
      pendingComplaints.value = res.data.map(c => ({ ...c, _adminReply: '' }))
    }
  } catch (e) {
    // interceptor handles
  } finally {
    loading.value = false
  }
}

async function approveRefund(row) {
  try {
    await ElMessageBox.confirm(`确认批准订单 #${row.id} 的退款？`, '确认', { type: 'warning' })
    await request.put(`/orders/${row.id}/refund/approve`)
    ElMessage.success('退款已批准')
    loadData()
  } catch (e) {
    if (e !== 'cancel') { /* handled */ }
  }
}

async function rejectRefund(row) {
  try {
    await ElMessageBox.confirm(`确认驳回订单 #${row.id} 的退款申请？`, '确认', { type: 'warning' })
    await request.put(`/orders/${row.id}/refund/reject`)
    ElMessage.success('退款已驳回')
    loadData()
  } catch (e) {
    if (e !== 'cancel') { /* handled */ }
  }
}

async function resolveComplaint(row, approved) {
  const action = approved ? '通过(扣除被投诉人20信用分)' : '驳回'
  try {
    await ElMessageBox.confirm(`确认${action}投诉 #${row.id}？`, '确认', { type: 'warning' })
    await request.put(`/complaints/${row.id}/resolve`, {
      approved,
      adminReply: row._adminReply || null
    })
    ElMessage.success(approved ? '投诉已通过，已扣信用分' : '投诉已驳回')
    loadData()
  } catch (e) {
    if (e !== 'cancel') { /* handled */ }
  }
}

watch(activeTab, () => loadData())

onMounted(() => {
  checkAdmin()
})
</script>

<style scoped>
.admin-page {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
  background: linear-gradient(180deg, #fff5eb 0%, #f5f7fa 100%);
  min-height: calc(100vh - 64px);
}

.admin-card {
  border-radius: 12px;
  border: none;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  color: #333;
  font-size: 18px;
}

.price-text {
  font-weight: bold;
  color: #ff4d4f;
}

.unlogged-card {
  border-radius: 12px;
  text-align: center;
  padding: 80px 20px;
  border: none;
}

.unlogged-content h2 {
  color: #333;
  margin-bottom: 15px;
  font-size: 22px;
}

.unlogged-content p {
  color: #666;
  margin-bottom: 30px;
  font-size: 16px;
}

.login-btn {
  border-radius: 20px;
  padding: 0 40px;
  font-weight: bold;
}

.text-muted {
  color: #999;
}
</style>
