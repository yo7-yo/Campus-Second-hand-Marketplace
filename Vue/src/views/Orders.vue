<template>
  <div class="orders-view">
    <el-card v-if="authStore.isLoggedIn" shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <div class="card-header-left">
            <h3>我的订单记录</h3>
            <el-radio-group v-model="orderTab" size="small" @change="handleTabChange" class="tab-group">
              <el-radio-button value="buyer">我买到的</el-radio-button>
              <el-radio-button value="seller">我卖出的</el-radio-button>
              <el-radio-button value="complaints">被投诉</el-radio-button>
            </el-radio-group>
          </div>
          <span class="refresh-text" @click="loadOrders" :class="{ 'is-loading': loading }">
            刷新记录
          </span>
        </div>
      </template>

      <!-- 订单表格 -->
      <el-table v-if="orderTab !== 'complaints'" :data="orders" style="width: 100%" v-loading="loading" :row-style="{ height: '60px' }">
        <el-table-column prop="id" label="订单编号" width="120" />
        <el-table-column prop="productTitle" label="商品名称" min-width="250" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="product-title-cell">{{ row.productTitle }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="成交价格" width="120">
          <template #default="{ row }">
            <span class="price-text">￥{{ Number(row.price || 0).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="buyerName" label="买家" width="120" />
        <el-table-column prop="sellerName" label="卖家" width="120" />
        <el-table-column prop="status" label="交易状态" width="140" align="center">
          <template #default="{ row }">
            <span :class="['status-text', row.status === 'completed' ? 'text-success' : row.status === 'refunded' ? 'text-danger' : row.status === 'refund_pending' ? 'text-orange' : 'text-warning']">
              {{ statusLabel(row) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" align="right">
          <template #default="{ row }">
            <span class="action-text" @click="viewDetail(row)">详情</span>
            <span v-if="orderTab === 'buyer' && row.status === 'pending'" class="action-text refund-text" @click="openRefundDialog(row)">退款</span>
            <span v-if="orderTab === 'seller' && row.status === 'refund_pending'" class="action-text refund-text" @click="openRespondDialog(row)">回应退款</span>
          </template>
        </el-table-column>
      </el-table>

      <!-- 被投诉表格 -->
      <el-table v-else :data="complaints" style="width: 100%" v-loading="loading" :row-style="{ height: '60px' }">
        <el-table-column prop="id" label="投诉编号" width="100" />
        <el-table-column prop="productTitle" label="相关商品" min-width="200" show-overflow-tooltip />
        <el-table-column prop="complainantName" label="投诉人" width="120" />
        <el-table-column prop="reason" label="投诉理由" min-width="250" show-overflow-tooltip />
        <el-table-column prop="respondentReply" label="我的回复" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <span :class="{ 'text-muted': !row.respondentReply }">{{ row.respondentReply || '未回复' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120" align="center">
          <template #default="{ row }">
            <span :class="['status-text', row.status === 'resolved' ? 'text-danger' : row.status === 'rejected' ? 'text-success' : 'text-warning']">
              {{ complaintStatusLabel(row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="right">
          <template #default="{ row }">
            <span v-if="row.status === 'pending' && !row.respondentReply" class="action-text" @click="openComplaintReplyDialog(row)">回复</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 未登录：显示提示面板 -->
    <el-card v-else class="unlogged-card" shadow="never">
      <div class="unlogged-content">
        <h2>需要登录</h2>
        <p>您目前处于未登录状态，无法查看历史订单和交易记录。</p>
        <el-button type="primary" color="#ff6a00" size="large" @click="router.push('/login')" class="login-btn">立即登录 / 注册</el-button>
      </div>
    </el-card>

    <!-- 订单详情弹窗 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="550px" destroy-on-close center>
      <div v-if="currentOrder" class="order-detail-container">
        <div class="order-status-header">
          <h2 :class="statusClass(currentOrder)">
            {{ statusLabel(currentOrder) }}
          </h2>
          <p class="order-id-text">订单号：{{ currentOrder.id }}</p>
        </div>
        <el-divider border-style="dashed" />
        <div class="info-list">
          <div class="info-item">
            <span class="info-label">商品名称：</span>
            <span class="info-value title-value">{{ currentOrder.productTitle }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">实付款：</span>
            <span class="info-value price-value">￥{{ Number(currentOrder.price || 0).toFixed(2) }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">卖家：</span>
            <span class="info-value">{{ currentOrder.sellerName }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">买家：</span>
            <span class="info-value">{{ currentOrder.buyerName }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">创建时间：</span>
            <span class="info-value">{{ currentOrder.createdTime || '-' }}</span>
          </div>
          <template v-if="currentOrder.refundReason">
            <el-divider border-style="dashed" />
            <div class="info-item">
              <span class="info-label">退款理由：</span>
              <span class="info-value text-danger">{{ currentOrder.refundReason }}</span>
            </div>
          </template>
          <template v-if="currentOrder.refundResponse">
            <div class="info-item">
              <span class="info-label">卖家回应：</span>
              <span class="info-value">{{ currentOrder.refundResponse }}</span>
            </div>
          </template>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" color="#ff6a00" size="large" @click="detailVisible = false" style="border-radius: 20px; padding: 0 40px;">
          我知道了
        </el-button>
      </template>
    </el-dialog>

    <!-- 退款申请弹窗 -->
    <el-dialog v-model="refundDialogVisible" title="申请退款" width="450px" destroy-on-close center>
      <el-form label-width="80px">
        <el-form-item label="商品">
          <span>{{ refundTarget?.productTitle }}</span>
        </el-form-item>
        <el-form-item label="退款理由">
          <el-input v-model="refundReason" type="textarea" :rows="4" placeholder="请填写退款理由，卖家和管理员将会看到" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="refundDialogVisible = false">取消</el-button>
        <el-button type="primary" color="#ff6a00" @click="submitRefund" :loading="refundLoading">提交退款申请</el-button>
      </template>
    </el-dialog>

    <!-- 卖家回应退款弹窗 -->
    <el-dialog v-model="respondDialogVisible" title="回应退款申请" width="450px" destroy-on-close center>
      <div v-if="respondTarget" class="respond-info">
        <p><b>买家退款理由：</b>{{ respondTarget.refundReason }}</p>
      </div>
      <el-form label-width="80px">
        <el-form-item label="我的回应">
          <el-input v-model="refundResponse" type="textarea" :rows="4" placeholder="请填写您的回应，管理员将会看到" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="respondDialogVisible = false">取消</el-button>
        <el-button type="primary" color="#ff6a00" @click="submitResponse" :loading="respondLoading">提交回应</el-button>
      </template>
    </el-dialog>

    <!-- 被投诉回复弹窗 -->
    <el-dialog v-model="complaintReplyVisible" title="回复投诉" width="450px" destroy-on-close center>
      <div v-if="complaintReplyTarget" class="respond-info">
        <p><b>投诉人：</b>{{ complaintReplyTarget.complainantName }}</p>
        <p><b>投诉理由：</b>{{ complaintReplyTarget.reason }}</p>
      </div>
      <el-form label-width="80px">
        <el-form-item label="我的回复">
          <el-input v-model="complaintReplyContent" type="textarea" :rows="4" placeholder="请填写您的回复，管理员将会看到" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="complaintReplyVisible = false">取消</el-button>
        <el-button type="primary" color="#ff6a00" @click="submitComplaintReply" :loading="complaintReplyLoading">提交回复</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import request from '../utils/request'
import { onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const orders = ref([])
const complaints = ref([])
const loading = ref(false)
const orderTab = ref('buyer')

const detailVisible = ref(false)
const currentOrder = ref(null)

// 退款相关
const refundDialogVisible = ref(false)
const refundTarget = ref(null)
const refundReason = ref('')
const refundLoading = ref(false)

// 卖家回应相关
const respondDialogVisible = ref(false)
const respondTarget = ref(null)
const refundResponse = ref('')
const respondLoading = ref(false)

// 被投诉回复相关
const complaintReplyVisible = ref(false)
const complaintReplyTarget = ref(null)
const complaintReplyContent = ref('')
const complaintReplyLoading = ref(false)

function statusLabel(row) {
  const map = {
    completed: '已完成',
    refunded: '已退款',
    refund_pending: '退款审核中',
    pending: '交易中'
  }
  return map[row.status] || '交易中'
}

function complaintStatusLabel(status) {
  const map = {
    pending: '待审核',
    resolved: '已通过',
    rejected: '已驳回'
  }
  return map[status] || '待审核'
}

function statusClass(row) {
  if (row.status === 'completed') return 'text-success'
  if (row.status === 'refunded') return 'text-danger'
  if (row.status === 'refund_pending') return 'text-orange'
  return 'text-warning'
}

async function loadOrders() {
  if (!authStore.isLoggedIn || !authStore.currentUser) return
  loading.value = true
  try {
    const url = orderTab.value === 'buyer'
      ? `/orders/buyer/${authStore.currentUser.id}`
      : `/orders/seller/${authStore.currentUser.id}`
    const res = await request.get(url)
    orders.value = res.data
  } catch (e) {
    orders.value = []
    ElMessage.error('加载订单失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

async function loadComplaints() {
  if (!authStore.isLoggedIn || !authStore.currentUser) return
  loading.value = true
  try {
    const res = await request.get('/complaints')
    complaints.value = res.data.filter(c => c.respondentId === authStore.currentUser.id)
  } catch (e) {
    complaints.value = []
    ElMessage.error('加载投诉失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

function handleTabChange() {
  if (orderTab.value === 'complaints') {
    loadComplaints()
  } else {
    loadOrders()
  }
}

function viewDetail(row) {
  currentOrder.value = row
  detailVisible.value = true
}

function openRefundDialog(row) {
  refundTarget.value = row
  refundReason.value = ''
  refundDialogVisible.value = true
}

async function submitRefund() {
  if (!refundReason.value.trim()) {
    ElMessage.warning('请填写退款理由')
    return
  }
  refundLoading.value = true
  try {
    await request.put(`/orders/${refundTarget.value.id}/refund`, { reason: refundReason.value.trim() })
    ElMessage.success('退款申请已提交，等待管理员审核')
    refundDialogVisible.value = false
    loadOrders()
  } catch (e) {
    // interceptor handles error
  } finally {
    refundLoading.value = false
  }
}

function openRespondDialog(row) {
  respondTarget.value = row
  refundResponse.value = ''
  respondDialogVisible.value = true
}

async function submitResponse() {
  if (!refundResponse.value.trim()) {
    ElMessage.warning('请填写回应内容')
    return
  }
  respondLoading.value = true
  try {
    await request.put(`/orders/${respondTarget.value.id}/refund/respond`, { response: refundResponse.value.trim() })
    ElMessage.success('回应已提交')
    respondDialogVisible.value = false
    loadOrders()
  } catch (e) {
    // interceptor handles error
  } finally {
    respondLoading.value = false
  }
}

function openComplaintReplyDialog(row) {
  complaintReplyTarget.value = row
  complaintReplyContent.value = ''
  complaintReplyVisible.value = true
}

async function submitComplaintReply() {
  if (!complaintReplyContent.value.trim()) {
    ElMessage.warning('请填写回复内容')
    return
  }
  complaintReplyLoading.value = true
  try {
    await request.put(`/complaints/${complaintReplyTarget.value.id}/reply`, { respondentReply: complaintReplyContent.value.trim() })
    ElMessage.success('回复已提交')
    complaintReplyVisible.value = false
    loadComplaints()
  } catch (e) {
    // interceptor handles error
  } finally {
    complaintReplyLoading.value = false
  }
}

// 监听登录状态变化，如果从外部登录了，立刻加载数据
watch(() => authStore.isLoggedIn, (newVal) => {
  if (newVal) {
    loadOrders()
  }
})

onMounted(() => {
  if (authStore.isLoggedIn) {
    loadOrders()
  }
})
</script>

<style scoped>
.orders-view {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  background: linear-gradient(180deg, #fff5eb 0%, #f5f7fa 100%);
  min-height: calc(100vh - 64px);
}

.table-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 8px rgba(0,0,0,0.02) !important;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.card-header h3 {
  margin: 0;
  color: #333;
  font-size: 18px;
}

.tab-group {
  margin-left: 8px;
}

.refresh-text {
  font-size: 14px;
  color: #ff6a00;
  cursor: pointer;
  transition: opacity 0.2s;
}
.refresh-text:hover { opacity: 0.8; text-decoration: underline; }
.refresh-text.is-loading { color: #999; cursor: not-allowed; text-decoration: none; }

.product-title-cell { font-weight: bold; color: #333; }
.price-text { font-weight: bold; color: #ff4d4f; font-size: 15px; }

.status-text { font-weight: bold; font-size: 14px; }
.text-success { color: #67c23a; }
.text-warning { color: #ff6a00; }
.text-danger { color: #f56c6c; }
.text-orange { color: #e6a23c; }

.respond-info {
  background: #fef0f0;
  border-radius: 8px;
  padding: 12px 16px;
  margin-bottom: 16px;
}
.respond-info p { margin: 0; color: #666; font-size: 14px; }

.action-text { color: #409eff; cursor: pointer; font-size: 14px; transition: color 0.2s; margin-right: 10px; }
.action-text:hover { color: #ff6a00; text-decoration: underline; }
.refund-text { color: #f56c6c; }
.refund-text:hover { color: #f78989; }
.text-muted { color: #999; }

.unlogged-card { border-radius: 12px; text-align: center; padding: 80px 20px; border: none; }
.unlogged-content h2 { color: #333; margin-bottom: 15px; font-size: 22px; }
.unlogged-content p { color: #666; margin-bottom: 30px; font-size: 16px; }
.login-btn { border-radius: 20px; padding: 0 40px; font-weight: bold; }

.order-detail-container {
  padding: 10px 20px;
}
.order-status-header {
  text-align: center;
  margin-bottom: 20px;
}
.order-status-header h2 {
  margin: 0 0 10px 0;
  font-size: 24px;
}
.order-id-text {
  margin: 0;
  color: #999;
  font-size: 14px;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 20px;
}
.info-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  line-height: 1.5;
}
.info-label {
  color: #909399;
  font-size: 15px;
  width: 90px;
  flex-shrink: 0;
}
.info-value {
  color: #333;
  font-size: 15px;
  text-align: right;
  flex: 1;
}
.title-value {
  font-weight: bold;
}
.price-value {
  color: #ff4d4f;
  font-size: 20px;
  font-weight: bold;
}
</style>