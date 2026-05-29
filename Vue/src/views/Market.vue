<template>
  <div class="recommend-container">
    <el-row :gutter="20">
      <el-col :span="24">

        <div class="search-banner">
          <h2 class="search-title">校园闲置交易平台</h2>
          <div class="search-bar">
            <input
              :value="searchKeyword"
              @input="onSearchInput"
              placeholder="搜索你想要的宝贝..."
              class="search-native-input"
              @keyup.enter="handleSearch"
            />
            <el-button
              type="primary"
              color="#ff6a00"
              size="large"
              class="search-main-btn"
              @click="handleSearch"
            >
              搜索
            </el-button>
          </div>
        </div>

        <!-- 筛选部分 -->
        <div class="filter-panel">
          <div class="filter-row-single">
            <div class="filter-item campus-filter">
              <span class="filter-label">校区</span>
              <el-select
                v-model="region"
                placeholder="全部校区"
                size="default"
                class="campus-select"
              >
                <el-option label="全部" value="" />
                <el-option label="东园" value="东园" />
                <el-option label="西园" value="西园" />
              </el-select>
            </div>

            <!-- 分类下拉框 -->
            <div class="filter-item category-filter">
              <span class="filter-label">分类</span>
              <el-select
                v-model="selectedCategory"
                placeholder="全部分类"
                size="default"
                class="category-select"
              >
                <el-option label="全部分类" value="" />
                <el-option
                  v-for="cat in categories"
                  :key="cat.id"
                  :label="cat.name"
                  :value="cat.name"
                />
              </el-select>
            </div>

            <!-- 价格范围 -->
            <div class="filter-item price-filter">
              <span class="filter-label">价格范围</span>
              <div class="price-range">
                <el-input
                  v-model="priceMin"
                  placeholder="最低价"
                  size="default"
                  type="number"
                  class="price-input"
                  clearable
                />
                <span class="price-separator">—</span>
                <el-input
                  v-model="priceMax"
                  placeholder="最高价"
                  size="default"
                  type="number"
                  class="price-input"
                  clearable
                />
              </div>
            </div>

            <!-- 重置筛选按钮 -->
            <el-button class="reset-btn" @click="resetFilters" size="default">
              重置筛选
            </el-button>
          </div>
        </div>

        <!-- 卖家主页提示 -->
        <div v-if="isOwnerView" class="owner-banner">
          <span>正在查看 <strong>{{ route.query.ownerName || '该用户' }}</strong> 发布的商品</span>
          <el-button type="primary" text color="#ff6a00" @click="clearOwnerFilter">返回全部商品</el-button>
        </div>

        <!-- 推荐好物列表 -->
        <div class="section-title">
          <h2>
            {{ sectionTitleText }}
            <span class="badge">精选优质闲置</span>
          </h2>
        </div>

        <el-row :gutter="16" v-if="filteredProducts.length > 0">
          <el-col
            :span="6"
            v-for="item in filteredProducts"
            :key="item.id"
            style="margin-bottom: 16px;"
          >
            <el-card
              shadow="hover"
              class="product-card"
              @click="handleProductClick(item)"
              :body-style="{ padding: '0px' }"
            >
              <img
                :src="getImageUrl(item.imagePath)"
                class="product-img"
                :alt="item.title"
              />
              <div class="product-info">
                <h3 class="title">{{ item.title }}</h3>
                <div class="category-row">
                  <span class="meta-category">{{ item.categoryName || '平台严选' }}</span>
                  <span class="meta-location">{{ item.location || '东园' }}</span>
                </div>
                <p class="desc">{{ item.description || '暂无详细描述' }}</p>
                <div class="bottom-row">
                  <span class="price">￥{{ Number(item.price).toFixed(2) }}</span>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <el-empty v-else description="未找到符合条件的闲置物品~" />

      </el-col>
    </el-row>

    <!-- 物品详情弹窗 -->
    <el-dialog
      v-model="detailVisible"
      title="物品详情"
      width="650px"
      destroy-on-close
      center
      class="custom-dialog"
    >
      <div v-if="currentProduct" class="detail-container">
        <img
          :src="getImageUrl(currentProduct.imagePath)"
          class="detail-img-large"
          :alt="currentProduct.title"
        />

        <div class="detail-content">
          <div class="detail-header">
            <h2 class="detail-title">{{ currentProduct.title }}</h2>
            <div class="detail-price-box">
              <div class="detail-price">
                ￥{{ Number(currentProduct.price).toFixed(2) }}
              </div>
            </div>
          </div>

          <div class="detail-tags">
            <span class="detail-tag">分类: {{ currentProduct.categoryName || '平台严选' }}</span>
            <span class="detail-tag">发布地点: {{ currentProduct.location || '东园' }}</span>
          </div>

          <el-divider border-style="dashed" />

          <div class="detail-desc-block">
            <h3>宝贝描述</h3>
            <p>{{ currentProduct.description || '无~' }}</p>
          </div>

          <div class="detail-seller">
            <div class="avatar">{{ currentProduct.ownerName ? currentProduct.ownerName.charAt(0) : '卖' }}</div>
            <div class="seller-info">
              <div class="seller-name">{{ currentProduct.ownerName || '神秘卖家' }}</div>
              <div class="seller-status">已实名认证 · 信用极好</div>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button
            size="large"
            @click="detailVisible = false"
            style="border-radius: 20px;"
          >
            再看看
          </el-button>

          <template v-if="!isOwner">
            <el-button
              size="large"
              @click="openChatWithSeller"
              style="border-radius: 20px; color: #ff6a00; border-color: #ff6a00;"
            >
              咨询卖家
            </el-button>

            <el-button
              type="primary"
              color="#ff6a00"
              size="large"
              @click="buyProduct"
              style="border-radius: 20px; font-weight: bold;"
            >
              立即购买
            </el-button>
          </template>
        </span>
      </template>
    </el-dialog>

    <!-- 聊天弹窗 -->
    <el-dialog
      v-model="chatVisible"
      :title="'与 ' + chatSellerName + ' 的对话'"
      width="480px"
      destroy-on-close
      class="chat-dialog"
    >
      <div class="mini-chat-messages" ref="miniChatRef">
        <div v-if="chatMessages.length === 0" class="mini-chat-empty">
          <p>暂无消息，发送一条消息开始咨询吧</p>
        </div>
        <div
          v-for="(msg, idx) in chatMessages"
          :key="idx"
          :class="['msg-row', msg.from === 'me' ? 'msg-right' : 'msg-left']"
        >
          <div class="msg-avatar">{{ msg.from === 'me' ? '我' : chatSellerName.charAt(0) }}</div>
          <div class="msg-content">
            <div class="msg-bubble">{{ msg.text }}</div>
            <div class="msg-time">{{ msg.time }}</div>
          </div>
        </div>
      </div>
      <div class="mini-chat-input">
        <el-input
          v-model="chatInput"
          type="textarea"
          :rows="2"
          placeholder="输入消息... (Enter发送)"
          resize="none"
          @keydown="handleChatKeydown"
        />
        <el-button
          type="primary"
          color="#ff6a00"
          @click="sendChatMessage"
          :disabled="!chatInput.trim()"
          style="margin-top: 8px; width: 100%;"
        >
          发送
        </el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script setup>
import request from '../utils/request'
import { ref, onMounted, watch, computed, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const products = ref([])
const searchKeyword = ref('')
const searchActiveKeyword = ref('')

const defaultProductImage = 'https://images.unsplash.com/photo-1607082348824-0a96f2a4b9da?auto=format&fit=crop&w=800&q=80'

function getImageUrl(imagePath) {
  if (imagePath && String(imagePath).trim()) {
    return '/uploads/products/' + imagePath
  }
  return defaultProductImage
}

// 监听全局搜索关键词，如果有则赋值到本地搜索框
watch(() => authStore.searchKeyword, (newVal) => {
  if (newVal && newVal.trim()) {
    searchKeyword.value = newVal
    searchActiveKeyword.value = newVal.trim()
    authStore.searchKeyword = ''
  }
})

// 监听路由query变化（从聊天页跳转查看卖家主页）
watch(() => route.query.owner, () => {
  loadProducts()
})

// 详情弹窗状态
const detailVisible = ref(false)
const currentProduct = ref(null)

const isOwner = computed(() => {
  return currentProduct.value && authStore.currentUser && Number(currentProduct.value.ownerId) === Number(authStore.currentUser.id)
})

// 聊天弹窗状态
const chatVisible = ref(false)
const chatSellerId = ref(null)
const chatSellerName = ref('')
const chatMessages = ref([])
const chatInput = ref('')
const miniChatRef = ref(null)

// 筛选状态
const priceMin = ref('')
const priceMax = ref('')
const region = ref('')
const selectedCategory = ref('')
const categories = ref([])

function openChatWithSeller() {
  if (!authStore.isLoggedIn) {
    ElMessage.warning('咨询需要先登录')
    router.push('/login')
    return
  }

  const ownerId = currentProduct.value?.ownerId

  if (!ownerId) {
    ElMessage.warning('无法获取卖家信息')
    return
  }

  detailVisible.value = false
  chatSellerId.value = ownerId
  chatSellerName.value = currentProduct.value?.ownerName || `卖家${ownerId}`
  chatMessages.value = []
  chatInput.value = ''
  chatVisible.value = true
  loadChatMessages()
}

async function loadProducts() {
  try {
    const ownerId = route.query.owner
    if (ownerId) {
      const res = await request.get(`/products/owner/${ownerId}`)
      products.value = res.data
      searchKeyword.value = route.query.ownerName ? `${route.query.ownerName} 的商品` : ''
    } else {
      const res = await request.get('/products')
      products.value = res.data
    }
  } catch (e) {
    const allTags = [
      '个人闲置',
      '验货宝',
      '验号担保',
      '包邮',
      '超赞鱼小铺',
      '全新',
      '严选',
      '转卖'
    ]

    const mockProducts = [
      {
        id: 1,
        title: '九成新 iPhone 手机',
        description: '电池健康良好，外观干净，适合日常使用。',
        price: 1599,
        categoryName: '数码电子',
        location: '东园',
        ownerId: 1001
      },
      {
        id: 2,
        title: '联想笔记本电脑',
        description: '办公学习都可以用，运行流畅，轻微使用痕迹。',
        price: 2399,
        categoryName: '数码电子',
        location: '西园',
        ownerId: 1002
      },
      {
        id: 3,
        title: '蓝牙耳机',
        description: '音质不错，续航正常，适合通勤和学习。',
        price: 99,
        categoryName: '数码电子',
        location: '东园',
        ownerId: 1003
      },
      {
        id: 4,
        title: '机械键盘',
        description: '按键手感好，适合写代码和打字。',
        price: 168,
        categoryName: '数码电子',
        location: '西园',
        ownerId: 1004
      },
      {
        id: 5,
        title: '考研数学教材',
        description: '书本保存完整，有少量笔记，适合备考。',
        price: 35,
        categoryName: '书籍资料',
        location: '东园',
        ownerId: 1005
      },
      {
        id: 6,
        title: '四六级英语资料',
        description: '包含真题和词汇资料，适合考试复习。',
        price: 25,
        categoryName: '书籍资料',
        location: '西园',
        ownerId: 1006
      },
      {
        id: 7,
        title: '计算机专业课本',
        description: '数据结构、操作系统相关书籍，适合专业学习。',
        price: 45,
        categoryName: '书籍资料',
        location: '东园',
        ownerId: 1007
      },
      {
        id: 8,
        title: '宿舍台灯',
        description: '亮度可调，适合宿舍学习使用。',
        price: 39,
        categoryName: '生活用品',
        location: '西园',
        ownerId: 1008
      },
      {
        id: 9,
        title: '保温杯',
        description: '容量适中，保温效果不错，外观较新。',
        price: 28,
        categoryName: '生活用品',
        location: '东园',
        ownerId: 1009
      },
      {
        id: 10,
        title: '宿舍收纳箱',
        description: '容量大，适合收纳衣物和杂物。',
        price: 30,
        categoryName: '生活用品',
        location: '西园',
        ownerId: 1010
      },
      {
        id: 11,
        title: '简约书桌',
        description: '桌面宽敞，适合学习和放电脑。',
        price: 120,
        categoryName: '生活用品',
        location: '东园',
        ownerId: 1011
      },
      {
        id: 12,
        title: '宽松卫衣',
        description: '版型好看，适合秋冬穿，成色较新。',
        price: 59,
        categoryName: '服饰鞋包',
        location: '西园',
        ownerId: 1012
      },
      {
        id: 13,
        title: '运动鞋',
        description: '鞋底磨损较少，适合日常运动。',
        price: 128,
        categoryName: '服饰鞋包',
        location: '东园',
        ownerId: 1013
      },
      {
        id: 14,
        title: '双肩书包',
        description: '容量大，可以放电脑和课本。',
        price: 68,
        categoryName: '服饰鞋包',
        location: '西园',
        ownerId: 1014
      },
      {
        id: 15,
        title: '篮球',
        description: '弹性好，适合室外球场使用。',
        price: 45,
        categoryName: '运动户外',
        location: '东园',
        ownerId: 1015
      },
      {
        id: 16,
        title: '羽毛球拍',
        description: '轻便好用，适合日常锻炼。',
        price: 75,
        categoryName: '运动户外',
        location: '西园',
        ownerId: 1016
      },
      {
        id: 17,
        title: '山地自行车',
        description: '校园代步方便，刹车正常。',
        price: 399,
        categoryName: '运动户外',
        location: '东园',
        ownerId: 1017
      },
      {
        id: 18,
        title: '瑜伽垫',
        description: '厚度适中，适合健身和拉伸。',
        price: 35,
        categoryName: '运动户外',
        location: '西园',
        ownerId: 1018
      },
      {
        id: 19,
        title: 'iPad 平板',
        description: '适合看网课和记笔记，屏幕显示正常。',
        price: 1299,
        categoryName: '数码电子',
        location: '东园',
        ownerId: 1019
      },
      {
        id: 20,
        title: '单反相机',
        description: '适合摄影入门，镜头成像清晰。',
        price: 1888,
        categoryName: '数码电子',
        location: '西园',
        ownerId: 1020
      }
    ]

    products.value = mockProducts.map(item => {
      const tagCount = Math.floor(Math.random() * 3) + 1
      const shuffled = [...allTags].sort(() => Math.random() - 0.5)
      const daysAgo = Math.floor(Math.random() * 30)

      return {
        ...item,
        tags: shuffled.slice(0, tagCount),
        createdAt: Date.now() - daysAgo * 24 * 60 * 60 * 1000
      }
    })
  }
}

function handleSearch() {
  searchActiveKeyword.value = searchKeyword.value.trim()
}

function onSearchInput(e) {
  searchKeyword.value = e.target.value
}

function resetFilters() {
  priceMin.value = ''
  priceMax.value = ''
  region.value = ''
  selectedCategory.value = ''
}

const isOwnerView = computed(() => !!route.query.owner)

const sectionTitleText = computed(() => {
  if (route.query.ownerName) return `${route.query.ownerName} 的闲置好物`
  return searchActiveKeyword.value ? '搜索好物结果' : '全部好物'
})

function clearOwnerFilter() {
  router.push('/market')
}

// 实时筛选
const filteredProducts = computed(() => {
  let list = [...products.value]

  // 关键词搜索
  if (searchActiveKeyword.value) {
    const kw = searchActiveKeyword.value.trim().toLowerCase()

    list = list.filter(p => {
      const title = String(p.title || '').toLowerCase()
      const desc = String(p.description || '').toLowerCase()
      const category = String(p.categoryName || '').toLowerCase()

      return title.includes(kw) || desc.includes(kw) || category.includes(kw)
    })
  }

  // 分类筛选
  if (selectedCategory.value) {
    list = list.filter(p => p.categoryName === selectedCategory.value)
  }

  // 价格区间
  if (priceMin.value !== '') {
    list = list.filter(p => Number(p.price) >= Number(priceMin.value))
  }

  if (priceMax.value !== '') {
    list = list.filter(p => Number(p.price) <= Number(priceMax.value))
  }

  // 校区筛选
  if (region.value) {
    list = list.filter(p => p.location === region.value)
  }

  return list
})

async function loadCategories() {
  try {
    const res = await request.get('/categories')
    categories.value = res.data
  } catch (e) {
    categories.value = [
      { id: 1, name: '数码电子' },
      { id: 2, name: '书籍资料' },
      { id: 3, name: '生活用品' },
      { id: 4, name: '服饰鞋包' },
      { id: 5, name: '运动户外' }
    ]
  }
}

function handleProductClick(product) {
  if (!authStore.isLoggedIn) {
    ElMessage.warning('您还未登录，请先登录后再查看物品详情')
    router.push('/login')
    return
  }

  currentProduct.value = product
  detailVisible.value = true
}

async function loadChatMessages() {
  if (!authStore.currentUser || !chatSellerId.value) return
  try {
    const res = await request.get('/messages', {
      params: { user1: authStore.currentUser.id, user2: chatSellerId.value }
    })
    chatMessages.value = res.data.map(msg => ({
      from: msg.senderId === authStore.currentUser.id ? 'me' : 'other',
      text: msg.content,
      time: formatTime(msg.createdTime)
    }))
    scrollMiniChat()
  } catch (e) {
    chatMessages.value = []
  }
}

async function sendChatMessage() {
  if (!chatInput.value.trim() || !authStore.currentUser || !chatSellerId.value) return
  const content = chatInput.value.trim()
  chatInput.value = ''

  // 乐观更新
  chatMessages.value.push({
    from: 'me',
    text: content,
    time: formatTime(new Date())
  })
  scrollMiniChat()

  try {
    await request.post('/messages', {
      senderId: authStore.currentUser.id,
      receiverId: chatSellerId.value,
      content: content
    })
    await loadChatMessages()
  } catch (e) {
    chatMessages.value.pop()
    chatInput.value = content
    ElMessage.error('消息发送失败')
  }
}

function handleChatKeydown(e) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    sendChatMessage()
  }
}

function scrollMiniChat() {
  nextTick(() => {
    if (miniChatRef.value) miniChatRef.value.scrollTop = miniChatRef.value.scrollHeight
  })
}

function formatTime(dt) {
  if (!dt) return ''
  const d = new Date(dt)
  const h = d.getHours().toString().padStart(2, '0')
  const m = d.getMinutes().toString().padStart(2, '0')
  return `${h}:${m}`
}

async function buyProduct() {
  try {
    await ElMessageBox.confirm(
      `确定要购买【${currentProduct.value.title}】吗？需支付 ￥${Number(currentProduct.value.price).toFixed(2)}`,
      '确认订单',
      {
        confirmButtonText: '确认付款',
        cancelButtonText: '取消',
        type: 'warning',
        center: true
      }
    )

    if (!authStore.isLoggedIn || !authStore.currentUser) {
      ElMessage.warning('请先登录再购买')
      router.push('/login')
      return
    }

    await request.post('/orders', {
      productId: currentProduct.value.id,
      buyerId: authStore.currentUser.id
    })
    ElMessage.success('下单成功！可在”我的订单”中查看。')
    detailVisible.value = false
    loadProducts()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.response?.data?.error || '下单失败')
    }
  }
}

onMounted(() => {
  loadProducts()
  loadCategories()
})
</script>

<style scoped>
.recommend-container {
  width: 100%;
  padding: 20px;
  box-sizing: border-box;
  background: linear-gradient(180deg, #fff5eb 0%, #f5f7fa 100%);
  min-height: calc(100vh - 64px);
}

/* ====== 筛选面板 ====== */
.filter-panel {
  background: #fff;
  border-radius: 14px;
  padding: 18px 22px;
  margin-bottom: 24px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
  border: 1px solid #f3f3f3;
}

.filter-row-single {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
  justify-content: space-between;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
  min-width: 200px;
}

.filter-label {
  font-size: 14px;
  font-weight: 700;
  color: #333;
  white-space: nowrap;
  min-width: 60px;
}

.category-select {
  flex: 1;
  min-width: 140px;
}

.campus-filter {
  flex: 1;
}

.campus-select {
  flex: 1;
  min-width: 140px;
}

.price-filter {
  flex: 1.5;
}

.price-range {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.price-input {
  flex: 1;
  min-width: 100px;
  max-width: 120px;
}

.price-separator {
  color: #bbb;
  font-size: 14px;
  flex-shrink: 0;
}

.category-select :deep(.el-input__wrapper),
.price-input :deep(.el-input__wrapper),
.campus-select :deep(.el-input__wrapper) {
  border-radius: 18px;
  background: #fafafa;
  box-shadow: 0 0 0 1px #eee inset;
}

.category-select :deep(.el-input__wrapper:hover),
.price-input :deep(.el-input__wrapper:hover),
.campus-select :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #ffb26b inset;
}

.reset-btn {
  height: 32px;
  padding: 0 16px;
  background: #fff3e0;
  color: #ff6a00;
  border: 1px solid #ffb26b;
  font-size: 14px;
  flex-shrink: 0;
}

.reset-btn:hover {
  background: #ff6a00;
  color: #fff;
  border-color: #ff6a00;
}

/* 搜索栏 */
.search-banner {
  background: linear-gradient(135deg, #ff9a44 0%, #ff6a00 100%);
  border-radius: 12px;
  padding: 40px 30px;
  margin-bottom: 24px;
  text-align: center;
}

.search-title {
  color: #fff;
  font-size: 28px;
  margin: 0 0 20px;
  font-weight: bold;
}

.search-bar {
  display: flex;
  max-width: 600px;
  margin: 0 auto;
  gap: 10px;
}

.search-main-btn {
  border-radius: 20px;
  padding: 0 30px;
  height: 44px;
  background: #fff;
  color: #ff6a00;
  border: none;
  font-weight: bold;
}

.search-main-btn:hover {
  background: #fff;
  color: #ff6a00;
}

.search-native-input {
  flex: 1;
  height: 44px;
  border: none;
  border-radius: 20px;
  padding: 0 20px;
  font-size: 15px;
  outline: none;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.search-native-input:focus {
  box-shadow: 0 2px 8px rgba(255, 106, 0, 0.3);
}

/* 卖家主页提示条 */
.owner-banner {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff5eb;
  border: 1px solid #ffe0c2;
  border-radius: 10px;
  padding: 12px 20px;
  margin-bottom: 16px;
  font-size: 14px;
  color: #666;
}

.owner-banner strong {
  color: #ff6a00;
}

/* 商品推荐区标题 */
.section-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  margin-top: 10px;
}

.section-title h2 {
  margin: 0;
  font-size: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
  color: #333;
}

.badge {
  background: #fff3e0;
  color: #ff6a00;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 4px;
  font-weight: normal;
}

/* 商品卡片 */
.product-card {
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.2s;
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04) !important;
  overflow: hidden;
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.08) !important;
}

.product-img {
  width: 100%;
  height: 160px;
  object-fit: cover;
  display: block;
  background: #f6f6f6;
}

.product-info {
  padding: 12px 16px;
}

.title {
  margin: 0 0 8px;
  font-size: 15px;
  font-weight: bold;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.category-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.meta-category {
  font-size: 12px;
  color: #ff6a00;
  background: #fff3e0;
  padding: 2px 6px;
  border-radius: 4px;
}

.meta-location {
  font-size: 12px;
  color: #999;
}

.desc {
  margin: 0 0 12px;
  font-size: 13px;
  color: #666;
  height: 36px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.4;
}

.bottom-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}

.price {
  color: #ff4d4f;
  font-size: 18px;
  font-weight: bold;
}

/* 详情弹窗 */
.detail-container {
  padding: 0 10px;
}

.detail-img-large {
  width: 100%;
  height: 280px;
  object-fit: cover;
  border-radius: 8px;
  margin-bottom: 24px;
  background: #f6f6f6;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.detail-title {
  margin: 0;
  font-size: 22px;
  color: #333;
  line-height: 1.4;
  flex: 1;
  padding-right: 20px;
}

.detail-price-box {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}

.detail-price {
  color: #ff4d4f;
  font-size: 28px;
  font-weight: bold;
}

.detail-tags {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
}

.detail-tag {
  background: #f4f4f5;
  color: #909399;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 13px;
}

.detail-desc-block h3 {
  margin: 0 0 12px;
  font-size: 16px;
  color: #333;
  border-left: 4px solid #ff6a00;
  padding-left: 8px;
}

.detail-desc-block p {
  color: #666;
  line-height: 1.8;
  font-size: 15px;
  margin-bottom: 30px;
  white-space: pre-wrap;
}

.detail-seller {
  display: flex;
  align-items: center;
  gap: 12px;
  background: #fafafa;
  padding: 16px;
  border-radius: 8px;
}

.detail-seller .avatar {
  width: 44px;
  height: 44px;
  font-size: 16px;
  background: #e0e0e0;
  border-radius: 50%;
  color: #666;
  display: flex;
  align-items: center;
  justify-content: center;
}

.seller-name {
  font-size: 15px;
  font-weight: bold;
  color: #333;
  margin-bottom: 4px;
}

.seller-status {
  font-size: 12px;
  color: #67c23a;
}

.dialog-footer {
  display: flex;
  justify-content: center;
  gap: 15px;
}

/* 聊天弹窗 */
.chat-dialog :deep(.el-dialog__body) {
  padding: 0 20px 20px;
}

/* 迷你聊天弹窗 */
.mini-chat-messages {
  height: 320px;
  overflow-y: auto;
  padding: 16px;
  background: #f5f5f5;
  border-radius: 8px;
  margin-bottom: 12px;
}

.mini-chat-empty {
  text-align: center;
  padding: 60px 0;
  color: #999;
}

.mini-chat-empty p {
  font-size: 14px;
}

.mini-chat-input .el-textarea {
  width: 100%;
}

.msg-row {
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
  align-items: flex-start;
}

.msg-right {
  flex-direction: row-reverse;
}

.msg-avatar {
  width: 32px;
  height: 32px;
  min-width: 32px;
  border-radius: 50%;
  background: #e0e0e0;
  color: #666;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: bold;
}

.msg-right .msg-avatar {
  background: linear-gradient(135deg, #ff9a44, #ff6a00);
  color: #fff;
}

.msg-content {
  max-width: 70%;
}

.msg-bubble {
  padding: 8px 12px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.5;
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
  margin-top: 2px;
}

.msg-right .msg-time {
  text-align: right;
}

.mini-chat-messages::-webkit-scrollbar {
  width: 6px;
}

.mini-chat-messages::-webkit-scrollbar-thumb {
  background: #d0d0d0;
  border-radius: 3px;
}
</style>