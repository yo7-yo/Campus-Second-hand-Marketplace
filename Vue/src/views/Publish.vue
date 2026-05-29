<template>
  <div class="publish-view">
    <el-card v-if="authStore.isLoggedIn" class="publish-card" shadow="never">
      <template #header>
        <div class="card-header">
          <h2>{{ editId ? '编辑闲置物品' : '发布闲置物品' }}</h2>
          <p>{{ editId ? '修改商品信息' : '填写详细信息，让买家更快找到你的宝贝' }}</p>
        </div>
      </template>

      <el-form :model="form" :rules="rules" ref="formRef" label-position="top" size="large">
        
        <!-- 图片上传 -->
        <el-form-item label="物品图片 (必填)" prop="image">
          <div class="upload-wrapper" @click="triggerFileInput">
            <img v-if="imageUrl" :src="imageUrl" class="preview-img" />
            <div v-else class="upload-placeholder">
              <span class="plus-text">+</span>
              <span>点击上传图片</span>
            </div>
            <!-- 原生 file input -->
            <input 
              type="file" 
              ref="fileInputRef" 
              class="hidden-input" 
              accept="image/*" 
              @change="handleFileChange" 
            />
          </div>
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="16">
            <el-form-item label="物品标题" prop="title">
              <el-input v-model="form.title" placeholder="例如：9成新 索尼降噪耳机" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="期望价格 (元)" prop="price">
              <el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%" :controls="false" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="物品分类" prop="categoryId">
              <el-select v-model="form.categoryId" placeholder="选择最合适的分类" style="width: 100%">
                <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所在校区" prop="location">
              <el-select v-model="form.location" placeholder="选择校区" style="width: 100%">
                <el-option label="东园" value="东园" />
                <el-option label="西园" value="西园" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="详细描述">
          <el-input 
            v-model="form.description" 
            type="textarea" 
            :rows="5"
            placeholder="描述一下物品的成色、购买时间、转手原因、支持的交易方式等..." 
          />
        </el-form-item>

        <div class="submit-action">
          <el-button type="primary" color="#ff6a00" size="large" class="submit-btn" @click="publish(formRef)">
            {{ editId ? '确认修改' : '确认发布' }}
          </el-button>
          <el-button size="large" class="cancel-btn" @click="$router.push('/market')">取消</el-button>
        </div>
      </el-form>
    </el-card>

    <!-- 未登录：显示提示面板 -->
    <el-card v-else class="unlogged-card" shadow="never">
      <div class="unlogged-content">
        <h2>需要登录</h2>
        <p>您目前处于未登录状态，发布闲置物品需要先登录您的账号。</p>
        <el-button type="primary" color="#ff6a00" size="large" @click="router.push('/login')">立即登录 / 注册</el-button>
        <el-button size="large" @click="$router.push('/market')">返回市场</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import request from '../utils/request'
import { onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const editId = route.params.id ? Number(route.params.id) : null

const categories = ref([])
const formRef = ref(null)

const fileInputRef = ref(null)
const imageUrl = ref('')

const form = reactive({
  title: '',
  description: '',
  price: null,
  location: '东园',
  ownerId: null,
  categoryId: null,
  image: null
})

const rules = {
  image: [{ required: !editId, message: '请上传至少一张物品图片', trigger: 'change' }],
  title: [{ required: true, message: '请输入物品标题', trigger: 'blur' }],
  price: [{ required: true, message: '请输入期望价格', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  location: [{ required: true, message: '请填写交易地点或所在校区', trigger: 'blur' }]
}

function triggerFileInput() {
  if (fileInputRef.value) {
    fileInputRef.value.click()
  }
}

function handleFileChange(event) {
  const file = event.target.files[0]
  if (file) {
    form.image = file 
    imageUrl.value = URL.createObjectURL(file) 
    if (formRef.value) {
      formRef.value.validateField('image')
    }
  }
}

async function loadCategories() {
  try {
    const res = await request.get('/categories')
    categories.value = res.data
  } catch (e) {
    categories.value = [
      { id: 1, name: '数码电子' }, { id: 2, name: '书籍资料' }, { id: 3, name: '生活用品' }, { id: 4, name: '服饰鞋包' }, { id: 5, name: '运动户外' }
    ]
  }
}

async function loadProduct() {
  if (!editId) return
  try {
    const res = await request.get(`/products/${editId}`)
    const p = res.data
    form.title = p.title
    form.description = p.description
    form.price = p.price
    form.location = p.location || '东园'
    form.categoryId = p.categoryId
    if (p.imagePath) {
      imageUrl.value = `/uploads/products/${p.imagePath}`
    }
  } catch (e) {
    ElMessage.error('加载商品信息失败')
  }
}

async function publish(formEl) {
  if (!formEl) return
  await formEl.validate(async (valid) => {
    if (valid) {
      try {
        const formData = new FormData()
        formData.append('title', form.title)
        formData.append('description', form.description || '')
        formData.append('price', form.price)
        formData.append('location', form.location || '')
        formData.append('ownerId', authStore.currentUser?.id || form.ownerId)
        if (form.categoryId != null) {
          formData.append('categoryId', form.categoryId)
        }
        formData.append('status', 'available')
        if (form.image) {
          formData.append('image', form.image)
        }

        if (editId) {
          await request.put(`/products/${editId}`, formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
          })
          ElMessage.success('修改成功！')
          setTimeout(() => router.push('/publish-history'), 1200)
        } else {
          await request.post('/products', formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
          })
          ElMessage.success('发布成功！即将返回市场...')
          setTimeout(() => router.push('/market'), 1200)
        }
      } catch (e) {
        // interceptor already shows error message
      }
    } else {
      ElMessage.warning('请检查是否有漏填的信息（包含图片）')
    }
  })
}

onMounted(async () => {
  await loadCategories()
  if (authStore.currentUser) {
    form.ownerId = authStore.currentUser.id
  }
  if (editId) {
    await loadProduct()
  }
})
</script>

<style scoped>
.publish-view {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  background: linear-gradient(180deg, #fff5eb 0%, #f5f7fa 100%);
  min-height: calc(100vh - 64px);
}

.publish-card {
  border-radius: 12px;
}

.card-header h2 {
  margin: 0 0 5px 0;
  color: #333;
  font-size: 20px;
}

.card-header p {
  margin: 0;
  color: #999;
  font-size: 14px;
}

/* 自定义上传框 */
.upload-wrapper {
  width: 140px;
  height: 140px;
  border: 1px dashed #dcdfe6;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: #fafafa;
  transition: border-color 0.2s;
  overflow: hidden;
}
.upload-wrapper:hover {
  border-color: #ff6a00;
}
.preview-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #8c939d;
}
.plus-text {
  font-size: 32px;
  margin-bottom: 4px;
  font-weight: 300;
  color: #8c939d;
}
.hidden-input {
  display: none;
}

.submit-action {
  margin-top: 40px;
  text-align: center;
}

.submit-btn, .cancel-btn {
  width: 140px;
  border-radius: 20px;
  font-weight: bold;
}

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