<template>
  <div class="login-container">
    <div class="login-card">
      <div class="logo-section">
        <h1>亿图了然</h1>
        <p>图片管理系统</p>
      </div>

      <div class="form-section">
        <h2>用户登录</h2>

        <el-form
            ref="loginFormRef"
            :model="loginForm"
            :rules="loginRules"
            label-position="top"
            @submit.prevent="handleLogin"
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-card {
  width: 100%;
  max-width: 420px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.logo-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 40px 20px;
  text-align: center;
}

.logo-section h1 {
  margin: 0;
  font-size: 32px;
  font-weight: 600;
}

.logo-section p {
  margin: 8px 0 0;
  font-size: 16px;
  opacity: 0.9;
}

.form-section {
  padding: 40px 32px;
}

.form-section h2 {
  margin: 0 0 24px;
  font-size: 24px;
  font-weight: 500;
  color: #333;
  text-align: center;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.login-button {
  width: 100%;
  height: 44px;
  margin-top: 8px;
}

.register-link {
  text-align: center;
  margin-top: 24px;
  color: #666;
  font-size: 14px;
}

.register-link a {
  margin-left: 8px;
}

@media (max-width: 768px) {
  .login-card {
    max-width: 100%;
  }

  .form-section {
    padding: 32px 24px;
  }
}
</style>