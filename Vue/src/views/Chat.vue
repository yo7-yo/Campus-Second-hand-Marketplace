<template>
  <div class="chat-page">
    <div v-if="authStore.isLoggedIn" class="chat-layout">
      <div class="conversation-panel">
        <div class="panel-header">
          <h3>我的消息</h3>
          <el-input v-model="searchText" placeholder="搜索联系人..." clearable prefix-icon="Search" size="small" />
        </div>
        <div class="conversation-list">
          <div
            v-for="conv in filteredConversations"
            :key="conv.id"
            :class="['conv-item', { active: activeConv && activeConv.name === conv.name }]"
            @click="selectConversation(conv)"
          >
            <div class="conv-avatar">{{ conv.name.charAt(0) }}</div>
            <div class="conv-info">
              <div class="conv-top">
                <span class="conv-name">{{ conv.name }}</span>
                <span class="conv-time">{{ conv.time }}</span>
              </div>
              <div class="conv-bottom">
                <span class="conv-msg">{{ conv.lastMsg || '暂无消息' }}</span>
                <span v-if="conv.unread > 0" class="unread-badge">{{ conv.unread }}</span>
              </div>
            </div>
          </div>
          <el-empty v-if="filteredConversations.length === 0" description="暂无会话，去市场咨询卖家吧~" :image-size="80" />
        </div>
      </div>

      <!-- 右侧：聊天窗口 -->
      <div class="chat-panel">
        <template v-if="activeConv">
          <!-- 聊天头部 -->
          <div class="chat-header">
            <div class="chat-header-info">
              <div class="chat-avatar">{{ activeConv.name.charAt(0) }}</div>
              <div>
                <div class="chat-name">{{ activeConv.name }}</div>
                <div class="chat-status">在线</div>
              </div>
            </div>
            <div class="chat-header-actions">
              <el-tooltip content="查看主页" placement="top">
                <el-button :icon="User" circle size="small" @click="viewUserProfile" />
              </el-tooltip>
              <el-tooltip content="投诉该用户" placement="top">
                <el-button type="danger" :icon="Warning" circle size="small" @click="openComplaintDialog" />
              </el-tooltip>
              <el-tooltip content="更多" placement="top">
                <el-button :icon="MoreFilled" circle size="small" />
              </el-tooltip>
            </div>
          </div>

          <!-- 消息区域 -->
          <div class="chat-messages" ref="chatBoxRef">
            <div class="chat-date-divider" v-if="messages.length > 0">
              <span>今天</span>
            </div>
            <div
              v-for="(msg, idx) in messages"
              :key="idx"
              :class="['msg-row', msg.from === 'me' ? 'msg-right' : 'msg-left']"
            >
              <div class="msg-avatar">{{ msg.from === 'me' ? '我' : activeConv.name.charAt(0) }}</div>
              <div class="msg-content">
                <div class="msg-bubble">{{ msg.text }}</div>
                <div class="msg-time">{{ msg.time }}</div>
              </div>
            </div>
            <div v-if="messages.length === 0" class="chat-empty">
              <div class="empty-icon">💬</div>
              <p>暂无消息记录</p>
              <p class="empty-hint">发送一条消息开始聊天吧</p>
            </div>
          </div>

          <!-- 输入区域 -->
          <div class="chat-input-bar">
            <div class="input-toolbar">
              <el-tooltip content="表情" placement="top">
                <el-button :icon="ChatDotRound" circle size="small" text />
              </el-tooltip>
              <el-tooltip content="图片" placement="top">
                <el-button :icon="Picture" circle size="small" text />
              </el-tooltip>
              <el-tooltip content="发送文件" placement="top">
                <el-button :icon="Folder" circle size="small" text />
              </el-tooltip>
            </div>
            <div class="input-row">
              <el-input
                v-model="msgInput"
                type="textarea"
                :rows="2"
                placeholder="输入消息... (Enter发送，Shift+Enter换行)"
                resize="none"
                @keydown="handleKeydown"
              />
              <el-button
                type="primary"
                color="#ff6a00"
                class="send-btn"
                @click="sendMessage"
                :disabled="!msgInput.trim()"
              >
                发送
              </el-button>
            </div>
          </div>
        </template>

        <!-- 未选择会话的空状态 -->
        <template v-else>
          <div class="no-chat-selected">
            <div class="no-chat-icon">💬</div>
            <h3>欢迎使用消息中心</h3>
            <p>在左侧选择一个会话开始聊天</p>
            <p class="no-chat-hint">或前往 <router-link to="/market" class="link-orange">闲置市场</router-link> 咨询卖家</p>
          </div>
        </template>
      </div>
    </div>

    <!-- 未登录：显示提示面板 -->
    <el-card v-else class="unlogged-card" shadow="never">
      <div class="unlogged-content">
        <h2>需要登录</h2>
        <p>您目前处于未登录状态，查看消息需要先登录您的账号。</p>
        <el-button type="primary" color="#ff6a00" size="large" @click="router.push('/login')">立即登录 / 注册</el-button>
      </div>
    </el-card>

    <!-- 投诉弹窗 -->
    <el-dialog v-model="complaintDialogVisible" title="投诉用户" width="450px" destroy-on-close center>
      <el-form label-width="80px">
        <el-form-item label="被投诉人">
          <span>{{ activeConv?.name }}</span>
        </el-form-item>
        <el-form-item label="投诉理由">
          <el-input v-model="complaintReason" type="textarea" :rows="4" placeholder="请详细描述投诉理由" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="complaintDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="submitComplaint" :loading="complaintLoading">提交投诉</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { User, MoreFilled, ChatDotRound, Picture, Folder, Warning } from '@element-plus/icons-vue'
import request from '../utils/request'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const route = useRoute()

const searchText = ref('')
const activeConv = ref(null)
const messages = ref([])
const msgInput = ref('')
const chatBoxRef = ref(null)
let pollTimer = null

// 投诉相关
const complaintDialogVisible = ref(false)
const complaintReason = ref('')
const complaintLoading = ref(false)

const filteredConversations = computed(() => {
  if (!searchText.value.trim()) return authStore.conversations
  const keyword = searchText.value.trim().toLowerCase()
  return authStore.conversations.filter(c => c.name.toLowerCase().includes(keyword))
})

async function loadConversations() {
  if (!authStore.isLoggedIn || !authStore.currentUser) return
  try {
    const res = await request.get('/messages/conversations', { params: { userId: authStore.currentUser.id } })
    const convMap = {}
    for (const msg of res.data) {
      const otherId = msg.senderId === authStore.currentUser.id ? msg.receiverId : msg.senderId
      const otherName = msg.senderId === authStore.currentUser.id ? msg.receiverName : msg.senderName
      if (!convMap[otherId]) {
        convMap[otherId] = {
          id: otherId,
          name: otherName,
          lastMsg: msg.content,
          time: formatTime(msg.createdTime),
          unread: 0,
          messages: []
        }
      }
    }
    authStore.conversations = Object.values(convMap)
  } catch (e) {
    // 保持当前状态
  }
}

async function loadMessages(conv) {
  if (!authStore.currentUser) return
  try {
    const res = await request.get('/messages', {
      params: { user1: authStore.currentUser.id, user2: conv.id }
    })
    messages.value = res.data.map(msg => ({
      from: msg.senderId === authStore.currentUser.id ? 'me' : 'other',
      text: msg.content,
      time: formatTime(msg.createdTime)
    }))
    scrollChat()
  } catch (e) {
    messages.value = []
  }
}

function selectConversation(conv) {
  activeConv.value = conv
  conv.unread = 0
  loadMessages(conv)
}

async function sendMessage() {
  if (!msgInput.value.trim() || !activeConv.value || !authStore.currentUser) return
  const content = msgInput.value.trim()
  msgInput.value = ''

  // 乐观更新：立即在界面上显示消息
  const optimisticMsg = {
    from: 'me',
    text: content,
    time: formatTime(new Date())
  }
  messages.value.push(optimisticMsg)
  scrollChat()

  try {
    await request.post('/messages', {
      senderId: authStore.currentUser.id,
      receiverId: activeConv.value.id,
      content: content
    })
    await loadMessages(activeConv.value)
    await loadConversations()
  } catch (e) {
    // 移除乐观添加的消息，恢复输入
    messages.value.pop()
    msgInput.value = content
    ElMessage.error('消息发送失败，请检查网络或稍后重试')
  }
}

function handleKeydown(e) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    sendMessage()
  }
}

function scrollChat() {
  nextTick(() => {
    if (chatBoxRef.value) chatBoxRef.value.scrollTop = chatBoxRef.value.scrollHeight
  })
}

function formatTime(dt) {
  if (!dt) return ''
  const d = new Date(dt)
  const h = d.getHours().toString().padStart(2, '0')
  const m = d.getMinutes().toString().padStart(2, '0')
  return `${h}:${m}`
}

function viewUserProfile() {
  if (!activeConv.value) return
  router.push({ path: '/market', query: { owner: activeConv.value.id, ownerName: activeConv.value.name } })
}

function openComplaintDialog() {
  complaintReason.value = ''
  complaintDialogVisible.value = true
}

async function submitComplaint() {
  if (!complaintReason.value.trim()) {
    ElMessage.warning('请填写投诉理由')
    return
  }
  complaintLoading.value = true
  try {
    await request.post('/complaints', {
      complainantId: authStore.currentUser.id,
      respondentId: activeConv.value.id,
      reason: complaintReason.value.trim()
    })
    ElMessage.success('投诉已提交，等待管理员审核')
    complaintDialogVisible.value = false
  } catch (e) {
    // interceptor handles error
  } finally {
    complaintLoading.value = false
  }
}

function startPolling() {
  stopPolling()
  pollTimer = setInterval(() => {
    if (authStore.isLoggedIn && activeConv.value) {
      loadMessages(activeConv.value)
    }
  }, 3000)
}

function stopPolling() {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

// 从其他页面跳转过来时自动打开指定会话
watch(() => authStore.openChatTarget, async (target) => {
  if (target) {
    // target 格式: "userId:username"
    const [id, name] = target.split(':')
    let conv = authStore.conversations.find(c => c.id === Number(id))
    if (!conv) {
      conv = { id: Number(id), name: name || `用户${id}`, lastMsg: '', time: '', unread: 0, messages: [] }
      authStore.conversations.unshift(conv)
    }
    selectConversation(conv)
    authStore.openChatTarget = ''
  }
})

// 从路由 query 参数打开会话
watch(() => route.query.target, (target) => {
  if (target) {
    const [id, name] = target.split(':')
    let conv = authStore.conversations.find(c => c.id === Number(id))
    if (!conv) {
      conv = { id: Number(id), name: name || `用户${id}`, lastMsg: '', time: '', unread: 0, messages: [] }
      authStore.conversations.unshift(conv)
    }
    selectConversation(conv)
  }
}, { immediate: true })

onMounted(async () => {
  if (authStore.isLoggedIn) {
    await loadConversations()
    if (authStore.conversations.length > 0 && !activeConv.value && !route.query.target) {
      selectConversation(authStore.conversations[0])
    }
    startPolling()
  }
})

onUnmounted(() => {
  stopPolling()
})

watch(() => authStore.isLoggedIn, (val) => {
  if (val) {
    loadConversations().then(() => {
      if (authStore.conversations.length > 0 && !activeConv.value) {
        selectConversation(authStore.conversations[0])
      }
    })
    startPolling()
  } else {
    stopPolling()
  }
})
</script>

<style scoped>
.chat-page {
  width: 100%;
  padding: 20px;
  box-sizing: border-box;
  background: linear-gradient(180deg, #fff5eb 0%, #f5f7fa 100%);
  min-height: calc(100vh - 64px);
}

.chat-layout {
  display: flex;
  height: calc(100vh - 120px);
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

/* ====== 左侧会话列表 ====== */
.conversation-panel {
  width: 320px;
  min-width: 320px;
  border-right: 1px solid #f0f0f0;
  display: flex;
  flex-direction: column;
  background: #fafafa;
}

.panel-header {
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
  background: #fff;
}

.panel-header h3 {
  margin: 0 0 12px;
  font-size: 17px;
  color: #333;
}

.conversation-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
}

.conv-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  cursor: pointer;
  transition: background 0.2s;
  border-left: 3px solid transparent;
}

.conv-item:hover {
  background: #f0f0f0;
}

.conv-item.active {
  background: #fff;
  border-left-color: #ff6a00;
}

.conv-avatar {
  width: 44px;
  height: 44px;
  min-width: 44px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff9a44, #ff6a00);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: bold;
}

.conv-info {
  flex: 1;
  overflow: hidden;
}

.conv-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.conv-name {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.conv-time {
  font-size: 12px;
  color: #999;
  min-width: 40px;
  text-align: right;
}

.conv-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.conv-msg {
  font-size: 13px;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

.unread-badge {
  background: #ff4d4f;
  color: #fff;
  font-size: 11px;
  min-width: 18px;
  height: 18px;
  line-height: 18px;
  text-align: center;
  border-radius: 9px;
  padding: 0 5px;
  margin-left: 8px;
}

/* ====== 右侧聊天面板 ====== */
.chat-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #fff;
}

/* 聊天头部 */
.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 20px;
  border-bottom: 1px solid #f0f0f0;
  background: #fff;
}

.chat-header-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.chat-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff9a44, #ff6a00);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  font-weight: bold;
}

.chat-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.chat-status {
  font-size: 12px;
  color: #67c23a;
  margin-top: 2px;
}

.chat-header-actions {
  display: flex;
  gap: 8px;
}

/* 消息区域 */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #f5f5f5;
}

.chat-date-divider {
  text-align: center;
  margin: 16px 0;
}

.chat-date-divider span {
  background: #e0e0e0;
  color: #999;
  font-size: 12px;
  padding: 4px 12px;
  border-radius: 10px;
}

.msg-row {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
  align-items: flex-start;
}

.msg-right {
  flex-direction: row-reverse;
}

.msg-avatar {
  width: 36px;
  height: 36px;
  min-width: 36px;
  border-radius: 50%;
  background: #e0e0e0;
  color: #666;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: bold;
}

.msg-right .msg-avatar {
  background: linear-gradient(135deg, #ff9a44, #ff6a00);
  color: #fff;
}

.msg-content {
  max-width: 60%;
}

.msg-bubble {
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-all;
}

.msg-right .msg-bubble {
  background: #ff6a00;
  color: #fff;
  border-bottom-right-radius: 4px;
}

.msg-left .msg-bubble {
  background: #fff;
  color: #333;
  border-bottom-left-radius: 4px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
}

.msg-time {
  font-size: 11px;
  color: #999;
  margin-top: 4px;
}

.msg-right .msg-time {
  text-align: right;
}

.chat-empty {
  text-align: center;
  padding: 80px 0;
  color: #999;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.chat-empty p {
  margin: 4px 0;
  font-size: 14px;
}

.empty-hint {
  color: #bbb;
  font-size: 13px !important;
}

/* 输入区域 */
.chat-input-bar {
  border-top: 1px solid #f0f0f0;
  background: #fff;
  padding: 10px 16px 14px;
}

.input-toolbar {
  display: flex;
  gap: 4px;
  margin-bottom: 8px;
}

.input-row {
  display: flex;
  gap: 10px;
  align-items: flex-end;
}

.input-row .el-textarea {
  flex: 1;
}

.send-btn {
  height: 54px;
  min-width: 80px;
  border-radius: 8px;
  font-weight: bold;
  font-size: 15px;
}

/* 未选择会话的空状态 */
.no-chat-selected {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #999;
}

.no-chat-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.no-chat-selected h3 {
  margin: 0 0 8px;
  font-size: 20px;
  color: #666;
}

.no-chat-selected p {
  margin: 4px 0;
  font-size: 14px;
}

.no-chat-hint {
  margin-top: 8px;
  color: #bbb;
}

.link-orange {
  color: #ff6a00;
  text-decoration: none;
  font-weight: bold;
}

.link-orange:hover {
  text-decoration: underline;
}

/* 滚动条美化 */
.conversation-list::-webkit-scrollbar,
.chat-messages::-webkit-scrollbar {
  width: 6px;
}

.conversation-list::-webkit-scrollbar-thumb,
.chat-messages::-webkit-scrollbar-thumb {
  background: #d0d0d0;
  border-radius: 3px;
}

.conversation-list::-webkit-scrollbar-track,
.chat-messages::-webkit-scrollbar-track {
  background: transparent;
}

/* 未登录状态卡片 */
.unlogged-card {
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

.unlogged-content .el-button {
  border-radius: 20px;
  margin: 0 10px;
  padding: 0 30px;
}
</style>
