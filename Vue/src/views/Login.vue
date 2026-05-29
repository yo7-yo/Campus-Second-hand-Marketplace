<template>
  <div class="login-page">
    <button class="back-btn" @click="handleBack">返回市场</button>
    <div class="login-container">
      <div class="login-left">
        <div class="left-content">
          <h1>校园闲置交易平台</h1>
          <p>让闲置焕发新生</p>
          <div class="features">
            <div class="feature-item">
              <span class="feature-icon">🛍️</span>
              <span>海量闲置好物</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">🔒</span>
              <span>校内交易安全</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧登录/注册表单 -->
      <div class="login-right">
        <div class="form-container">
          <!-- 标签切换 -->
          <div class="tab-header">
            <div
              :class="['tab-item', { active: currentTab === 'login' }]"
              @click="currentTab = 'login'"
            >
              登录
            </div>
            <div
              :class="['tab-item', { active: currentTab === 'register' }]"
              @click="currentTab = 'register'"
            >
              注册
            </div>
          </div>

          <!-- 登录表单 -->
          <el-form
            v-if="currentTab === 'login'"
            ref="loginFormRef"
            :model="loginForm"
            :rules="loginRules"
            label-position="left"
            label-width="70px"
            size="large"
            class="login-form-inline"
          >
            <el-form-item label="用户名" prop="username">
              <el-input
                v-model="loginForm.username"
                placeholder="请输入用户名"
                prefix-icon="User"
              />
            </el-form-item>

            <el-form-item label="密码" prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                prefix-icon="Lock"
                show-password
              />
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                color="#ff8237"
                class="submit-btn"
                @click="handleLogin(loginFormRef)"
                :loading="loginLoading"
              >
                登录
              </el-button>
            </el-form-item>

            <div class="form-footer">
              <span>还没有账号？</span>
              <span class="link-orange" @click="currentTab = 'register'">立即注册</span>
            </div>
          </el-form>

          <!-- 注册表单 -->
          <el-form
            v-else
            ref="registerFormRef"
            :model="registerForm"
            :rules="registerRules"
            label-position="left"
            label-width="70px"
            size="large"
            class="register-form-scroll"
          >
            <el-form-item label="用户名" prop="username">
              <el-input
                v-model="registerForm.username"
                placeholder="请输入用户名（2-12位字母数字/下划线）"
                prefix-icon="User"
              />
            </el-form-item>

            <el-form-item label="密码" prop="password">
              <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="请输入密码（6-16位）"
                prefix-icon="Lock"
                show-password
              />
            </el-form-item>

            <el-form-item label="验证码" prop="captcha" class="captcha-item">
              <div class="captcha-wrap">
                <el-input
                  v-model="registerForm.captcha"
                  maxlength="4"
                  placeholder="请输入4位验证码"
                  prefix-icon="Message"
                  class="captcha-input"
                />
                <span class="captcha-error">{{ captchaError }}</span>
              </div>
              <div class="captcha-row2">
                <div class="captcha-box">{{ captchaCode }}</div>
                <el-button
                  color="#ff8237"
                  text
                  @click="generateCaptcha"
                >
                  刷新
                </el-button>
              </div>
            </el-form-item>

            <el-form-item label="学号" prop="studentId">
              <el-input
                v-model="registerForm.studentId"
                placeholder="请输入学号"
                prefix-icon="Document"
              />
            </el-form-item>

            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="registerForm.gender">
                <el-radio value="女">女</el-radio>
                <el-radio value="男">男</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="学生证">
              <el-upload
                v-model:file-list="studentCardFile"
                action="#"
                :auto-upload="false"
                :limit="1"
                accept="image/*"
              >
                <template #trigger>
                  <el-button color="#ff8237">选择学生证</el-button>
                </template>
                <template #tip>
                  <div class="el-upload__tip" style="color: #666; font-size: 12px;">
                    请上传学生证照片（用于实名认证）
                  </div>
                </template>
              </el-upload>
            </el-form-item>

            <el-form-item label="个人介绍" prop="intro">
              <el-input
                v-model="registerForm.intro"
                type="textarea"
                placeholder="请输入个人介绍（选填）"
                maxlength="200"
                show-word-limit
                :rows="3"
              />
            </el-form-item>

            <el-form-item prop="agree">
              <el-checkbox v-model="registerForm.agree">
                我已同意《隐私相关协议》
              </el-checkbox>
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                color="#ff8237"
                class="submit-btn"
                @click="handleRegister(registerFormRef)"
                :loading="registerLoading"
              >
                注册
              </el-button>
            </el-form-item>

            <div class="form-footer">
              <span>已有账号？</span>
              <span class="link-orange" @click="currentTab = 'login'">立即登录</span>
            </div>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../utils/request'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const currentTab = ref('login')
const loginLoading = ref(false)
const registerLoading = ref(false)

const loginFormRef = ref(null)
const registerFormRef = ref(null)

// 登录表单
const loginForm = reactive({
  username: '',
  password: ''
})

// 注册表单
const registerForm = reactive({
  username: '',
  password: '',
  captcha: '',
  studentId: '',
  gender: '女',
  intro: '',
  agree: false
})

// 学生证文件
const studentCardFile = ref([])

// 验证码
const captchaCode = ref('')
const captchaError = ref('')

watch(() => registerForm.captcha, () => { captchaError.value = '' })

// 生成验证码
function generateCaptcha() {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789'
  let code = ''
  for (let i = 0; i < 4; i++) {
    code += chars.charAt(Math.floor(Math.random() * chars.length))
  }
  captchaCode.value = code
  registerForm.captcha = ''
  captchaError.value = ''
}

// 登录验证规则
const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

// 注册验证规则
const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ],
  captcha: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ],
  studentId: [
    { required: true, message: '请输入学号', trigger: 'blur' }
  ],
  gender: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  agree: [
    { required: true, message: '请同意隐私协议', trigger: 'change' }
  ]
}

// 登录
async function handleLogin(formEl) {
  if (!formEl) return
  await formEl.validate(async (valid) => {
    if (valid) {
      loginLoading.value = true
      try {
        const res = await request.post('/users/signin', {
          username: loginForm.username,
          password: loginForm.password
        })
        const user = res.data
        authStore.login(user)
        ElMessage.success('登录成功！')
        router.push('/market')
      } catch (e) {
        ElMessage.error(e.response?.data?.error || '登录失败，请检查用户名和密码')
      } finally {
        loginLoading.value = false
      }
    }
  })
}

// 注册
async function handleRegister(formEl) {
  if (!formEl) return
  captchaError.value = ''
  await formEl.validate(async (valid) => {
    if (valid) {
      if (studentCardFile.value.length === 0) {
        ElMessage.error('请上传学生证')
        return
      }
      if (!registerForm.agree) {
        ElMessage.error('请同意隐私协议')
        return
      }

      if (registerForm.captcha !== captchaCode.value) {
        captchaError.value = '验证码错误'
        return
      }

      registerLoading.value = true
      const registeredUsername = registerForm.username
      try {
        await request.post('/users', {
          username: registerForm.username,
          password: registerForm.password,
          studentId: registerForm.studentId,
          gender: registerForm.gender
        })
        ElMessage.success('注册成功！请登录')
        // 重置表单
        registerForm.username = ''
        registerForm.password = ''
        registerForm.captcha = ''
        registerForm.studentId = ''
        registerForm.gender = '女'
        registerForm.intro = ''
        registerForm.agree = false
        studentCardFile.value = []
        generateCaptcha()

        currentTab.value = 'login'
        loginForm.username = registeredUsername
      } catch (e) {
        ElMessage.error(e.response?.data?.error || '注册失败，请稍后重试')
      } finally {
        registerLoading.value = false
      }
    }
  })
}

// 初始化
onMounted(() => {
  generateCaptcha()
  document.body.style.overflow = 'hidden'
  document.body.style.margin = '0'
  document.documentElement.style.overflow = 'hidden'
})

onUnmounted(() => {
  document.body.style.overflow = ''
  document.body.style.margin = ''
  document.documentElement.style.overflow = ''
})

// 返回
function handleBack() {
  router.push('/market')
}

</script>

<style scoped>
.back-btn {
  position: fixed;
  top: 20px;
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

.login-page {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #fff3e0 0%, #ffe0b2 100%);
  overflow: hidden;
  z-index: 9999;
}


.login-container {
  display: flex;
  width: 1000px;
  height: auto;
  min-height: auto;
  border-radius: 16px;
  overflow: visible;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.1);
}

.login-left {
  display: none;
}

.login-left {
  width: 270px;
  flex-shrink: 0;
  background: linear-gradient(135deg, #ff9a44 0%, #ff6a00 100%);
  padding: 50px 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.left-content h1 {
  font-size: 32px;
  margin: 0 0 10px;
  font-weight: bold;
}

.left-content p {
  font-size: 18px;
  margin: 0 0 40px;
  opacity: 0.9;
}

.features {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 16px;
}

.feature-icon {
  font-size: 24px;
}

/* 右侧表单区 */
.login-right {
  flex: 1;
  background: #fff;
  padding: 10px 5px;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  overflow: hidden;
  border-radius: 0 16px 16px 0;
}

.form-container {
  width: 100%;
  max-width: 480px;
  max-height: calc(100vh - 40px);
  overflow: hidden;
  padding: 10px 5px;
}

/* 标签切换 */
.tab-header {
  display: flex;
  gap: 20px;
  margin-bottom: 8px;
  border-bottom: 2px solid #f0f0f0;
  padding-bottom: 8px;
}

.tab-item {
  font-size: 18px;
  font-weight: bold;
  color: #999;
  cursor: pointer;
  padding-bottom: 10px;
  margin-bottom: -12px;
  border-bottom: 2px solid transparent;
  transition: all 0.3s;
}

.tab-item.active {
  color: #ff8237;
  border-bottom-color: #ff8237;
}

/* 提交按钮 */
.submit-btn {
  width: 75%;
  height: 44px;
  font-size: 16px;
  font-weight: bold;
  border-radius: 22px;
}

/* 表单底部 */
.form-footer {
  text-align: center;
  margin-top: 5px;
  font-size: 14px;
  color: #666;
}

.link-orange {
  color: #ff8237;
  cursor: pointer;
  font-weight: bold;
}

.link-orange:hover {
  text-decoration: underline;
}

/* 验证码行 - 见下方 captcha-wrap */

/* 登录表单内联错误 */
.login-form-inline :deep(.el-form-item) {
  margin-bottom: 12px;
  display: flex;
  align-items: flex-start;
}

.login-form-inline :deep(.el-form-item__label) {
  padding-bottom: 0;
  line-height: 32px;
  min-width: 70px;
  flex-shrink: 0;
  text-align: right;
  padding-right: 5px;
  font-size: 15px;
}

.login-form-inline :deep(.el-input__wrapper) {
  font-size: 13px;
}

.login-form-inline :deep(.el-form-item__content) {
  line-height: 1.2;
  flex: 1;
  display: flex;
  flex-wrap: nowrap;
  align-items: center;
}

.login-form-inline :deep(.el-form-item__error) {
  position: static;
  margin-left: 10px;
  white-space: nowrap;
  font-size: 12px;
  flex-shrink: 0;
}

.login-form-inline :deep(.el-form-item__content .el-input) {
  flex: none;
  width: 260px;
}

/* 注册表单 */
.register-form-scroll {
  padding-right: 0;
  overflow: hidden;
}

/* 注册表单项间距缩小和布局 */
.register-form-scroll :deep(.el-form-item) {
  margin-bottom: 8px;
  display: flex;
  align-items: flex-start;
}

.register-form-scroll :deep(.el-form-item__label) {
  padding-bottom: 0;
  line-height: 28px;
  min-width: 70px;
  flex-shrink: 0;
  text-align: right;
  padding-right: 5px;
  font-size: 15px;
}

.register-form-scroll :deep(.el-input__wrapper),
.register-form-scroll :deep(.el-textarea__inner) {
  font-size: 13px;
}

.register-form-scroll :deep(.el-form-item__content) {
  line-height: 1.2;
  flex: 1;
  display: flex;
  flex-wrap: nowrap;
  align-items: center;
}

/* 验证错误信息显示在输入框后面 */
.register-form-scroll :deep(.el-form-item__error) {
  position: static;
  margin-left: 10px;
  white-space: nowrap;
  font-size: 12px;
  flex-shrink: 0;
}

.register-form-scroll :deep(.el-form-item__content .el-input),
.register-form-scroll :deep(.el-form-item__content .el-upload) {
  flex: none;
  width: 260px;
}

.register-form-scroll :deep(.el-form-item__content .el-textarea) {
  flex: none;
  width: 100%;
}

/* 验证码行特殊处理 */
.register-form-scroll :deep(.captcha-item .el-form-item__content) {
  display: block !important;
  width: auto !important;
}

/* 隐藏 el-form-item 自带的错误渲染 */
.register-form-scroll :deep(.captcha-item .el-form-item__error) {
  display: none !important;
}

.captcha-wrap {
  display: flex !important;
  flex-wrap: wrap !important;
  gap: 6px;
  align-items: center;
}

.register-form-scroll :deep(.captcha-item .captcha-input) {
  flex: none !important;
  width: 260px !important;
}

.captcha-row2 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
  width: 260px;
}

.captcha-error {
  color: #f56c6c;
  font-size: 12px;
  flex-shrink: 0;
}

.captcha-box {
  flex-shrink: 0;
  width: 70px;
  height: 32px;
  background: #f5f5f5;
  border: 1px solid #ddd;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: bold;
  letter-spacing: 2px;
  color: #ff8237;
  user-select: none;
}

/* 单选按钮和复选框对齐 */
.register-form-scroll :deep(.el-radio-group),
.register-form-scroll :deep(.el-checkbox) {
  flex-shrink: 0;
}
</style>
