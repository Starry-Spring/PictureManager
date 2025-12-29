<template>
  <div class="login-container">
    <div class="login-background">
      <div class="bg-shape shape-1"></div>
      <div class="bg-shape shape-2"></div>
      <div class="bg-shape shape-3"></div>
    </div>
    
    <div class="login-card">
      <div class="login-left">
        <div class="brand-content">
          <div class="brand-icon">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M21 19V5c0-1.1-.9-2-2-2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2zM8.5 13.5l2.5 3.01L14.5 12l4.5 6H5l3.5-4.5z"/>
            </svg>
          </div>
          <h1>亿图了然</h1>
          <p>智能图片管理系统</p>
          <div class="features">
            <div class="feature-item">
              <span class="feature-icon">📷</span>
              <span>智能分类管理</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">🌟</span>
              <span>AI标签识别</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">🔒</span>
              <span>安全云存储</span>
            </div>
          </div>
        </div>
      </div>
      
      <div class="login-right">
        <div class="form-section">
          <h2>欢迎回来</h2>
          <p class="subtitle">登录您的账户继续使用</p>

          <el-form
              ref="loginFormRef"
              :model="loginForm"
              :rules="loginRules"
              label-position="top"
              @submit.prevent="handleLogin"
              class="login-form"
          >
            <el-form-item label="用户名或邮箱" prop="usernameOrEmail">
              <el-input
                  v-model="loginForm.usernameOrEmail"
                  placeholder="请输入用户名或邮箱"
                  size="large"
                  :prefix-icon="User"
              />
            </el-form-item>

            <el-form-item label="密码" prop="password">
              <el-input
                  v-model="loginForm.password"
                  type="password"
                  placeholder="请输入密码"
                  size="large"
                  :prefix-icon="Lock"
                  show-password
              />
            </el-form-item>

            <div class="form-options">
              <el-checkbox v-model="rememberMe">记住我</el-checkbox>
              <el-link type="primary" @click="showForgotPassword = true">
                忘记密码？
              </el-link>
            </div>

            <el-button
                type="primary"
                size="large"
                :loading="loading"
                @click="handleLogin"
                class="login-button"
            >
              登录
            </el-button>

            <div class="register-link">
              还没有账户？
              <el-link type="primary" @click="goToRegister">
                立即注册
              </el-link>
            </div>
          </el-form>
        </div>
      </div>
    </div>

    <!-- 忘记密码对话框 -->
    <el-dialog
        v-model="showForgotPassword"
        title="找回密码"
        width="400px"
    >
      <el-form ref="forgotFormRef" :model="forgotForm" label-position="top">
        <el-form-item label="注册邮箱" required>
          <el-input
              v-model="forgotForm.email"
              placeholder="请输入注册时使用的邮箱"
          />
        </el-form-item>
        <p style="color: #666; font-size: 12px; margin-top: 8px;">
          我们将向该邮箱发送重置密码的链接
        </p>
      </el-form>
      <template #footer>
        <el-button @click="showForgotPassword = false">取消</el-button>
        <el-button type="primary" @click="handleForgotPassword">
          发送重置邮件
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage, type FormInstance } from 'element-plus'
import { useUserStore } from '../stores/userStore'

const router = useRouter()
const userStore = useUserStore()
const loginFormRef = ref<FormInstance>()
const forgotFormRef = ref<FormInstance>()

const loading = ref(false)
const rememberMe = ref(false)
const showForgotPassword = ref(false)

const loginForm = reactive({
  usernameOrEmail: '',
  password: ''
})

const forgotForm = reactive({
  email: ''
})

const loginRules = {
  usernameOrEmail: [
    { required: true, message: '请输入用户名或邮箱', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

onMounted(() => {
  // 检查本地存储的记住我
  const savedUsername = localStorage.getItem('rememberedUsername')
  if (savedUsername) {
    loginForm.usernameOrEmail = savedUsername
    rememberMe.value = true
  }
})

const handleLogin = async () => {
  if (!loginFormRef.value) return

  try {
    await loginFormRef.value.validate()
    loading.value = true

    const result = await userStore.login(
        loginForm.usernameOrEmail,
        loginForm.password
    )

    if (result.success) {
      // 保存记住我
      if (rememberMe.value) {
        localStorage.setItem('rememberedUsername', loginForm.usernameOrEmail)
      } else {
        localStorage.removeItem('rememberedUsername')
      }

      ElMessage.success('登录成功')
      router.push('/gallery')
    } else {
      ElMessage.error(result.message || '登录失败')
    }
  } catch (error) {
    console.error('登录错误:', error)
  } finally {
    loading.value = false
  }
}

const goToRegister = () => {
  router.push('/register')
}

const handleForgotPassword = async () => {
  if (!forgotForm.email) {
    ElMessage.warning('请输入邮箱')
    return
  }

  try {
    // 这里调用后端找回密码接口
    // await axios.post('/api/auth/forgot-password', { email: forgotForm.email })

    ElMessage.success('重置密码邮件已发送，请查收邮箱')
    showForgotPassword.value = false
    forgotForm.email = ''
  } catch (error) {
    ElMessage.error('发送失败，请稍后重试')
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  padding: 20px;
  position: relative;
  overflow: hidden;
}

/* 背景装饰 */
.login-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  overflow: hidden;
  z-index: 0;
}

.bg-shape {
  position: absolute;
  border-radius: 50%;
  opacity: 0.1;
}

.shape-1 {
  width: 400px;
  height: 400px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  top: -100px;
  right: -100px;
  animation: float 6s ease-in-out infinite;
}

.shape-2 {
  width: 300px;
  height: 300px;
  background: linear-gradient(135deg, #f093fb, #f5576c);
  bottom: -50px;
  left: -50px;
  animation: float 8s ease-in-out infinite reverse;
}

.shape-3 {
  width: 200px;
  height: 200px;
  background: linear-gradient(135deg, #4facfe, #00f2fe);
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  animation: pulse 4s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-20px); }
}

@keyframes pulse {
  0%, 100% { transform: translate(-50%, -50%) scale(1); opacity: 0.1; }
  50% { transform: translate(-50%, -50%) scale(1.1); opacity: 0.15; }
}

.login-card {
  width: 100%;
  max-width: 900px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 24px;
  box-shadow: 0 25px 80px rgba(0, 0, 0, 0.3);
  display: grid;
  grid-template-columns: 1fr 1fr;
  overflow: hidden;
  position: relative;
  z-index: 1;
  backdrop-filter: blur(10px);
}

/* 左侧品牌区 */
.login-left {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.login-left::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 60%);
  animation: rotate 20s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.brand-content {
  position: relative;
  z-index: 1;
  text-align: center;
  color: white;
}

.brand-icon {
  width: 80px;
  height: 80px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24px;
  backdrop-filter: blur(10px);
}

.brand-icon svg {
  width: 48px;
  height: 48px;
  color: white;
}

.brand-content h1 {
  margin: 0;
  font-size: 36px;
  font-weight: 700;
  letter-spacing: 2px;
}

.brand-content p {
  margin: 12px 0 0;
  font-size: 16px;
  opacity: 0.9;
}

.features {
  margin-top: 48px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 12px;
  backdrop-filter: blur(10px);
  font-size: 14px;
}

.feature-icon {
  font-size: 20px;
}

/* 右侧表单区 */
.login-right {
  padding: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.form-section {
  width: 100%;
  max-width: 320px;
}

.form-section h2 {
  margin: 0;
  font-size: 28px;
  font-weight: 600;
  color: #1a1a2e;
}

.form-section .subtitle {
  margin: 8px 0 32px;
  color: #666;
  font-size: 14px;
}

.login-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #333;
}

.login-form :deep(.el-input__wrapper) {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.login-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.login-button {
  width: 100%;
  height: 48px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 500;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  transition: all 0.3s;
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
}

.register-link {
  text-align: center;
  margin-top: 24px;
  color: #666;
  font-size: 14px;
}

/* 响应式 */
@media (max-width: 768px) {
  .login-card {
    grid-template-columns: 1fr;
    max-width: 420px;
  }

  .login-left {
    display: none;
  }

  .login-right {
    padding: 40px 32px;
  }

  .form-section h2::before {
    content: '🖼️ ';
  }
}
</style>