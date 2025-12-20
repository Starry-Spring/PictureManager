<template>
  <div class="register-container">
    <div class="register-card">
      <div class="logo-section">
        <h1>亿图了然</h1>
        <p>图片管理系统</p>
      </div>

      <div class="form-section">
        <h2>用户注册</h2>

        <el-form
            ref="registerFormRef"
            :model="registerForm"
            :rules="registerRules"
            label-position="top"
            @submit.prevent="handleRegister"
        >
          <el-form-item label="用户名" prop="username">
            <el-input
                v-model="registerForm.username"
                placeholder="请输入用户名"
                size="large"
                :prefix-icon="User"
                @blur="checkUsername"
            />
            <div v-if="usernameAvailable !== null" class="availability-hint">
              <span :class="usernameAvailable ? 'available' : 'unavailable'">
                {{ usernameAvailable ? '✓ 用户名可用' : '✗ 用户名已存在' }}
              </span>
            </div>
          </el-form-item>

          <el-form-item label="邮箱" prop="email">
            <el-input
                v-model="registerForm.email"
                placeholder="请输入邮箱"
                size="large"
                :prefix-icon="Message"
                @blur="checkEmail"
                type="email"
            />
            <div v-if="emailAvailable !== null" class="availability-hint">
              <span :class="emailAvailable ? 'available' : 'unavailable'">
                {{ emailAvailable ? '✓ 邮箱可用' : '✗ 邮箱已被注册' }}
              </span>
            </div>
          </el-form-item>

          <el-form-item label="显示名称" prop="displayName">
            <el-input
                v-model="registerForm.displayName"
                placeholder="请输入显示名称（可选）"
                size="large"
                :prefix-icon="EditPen"
            />
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="请输入密码"
                size="large"
                :prefix-icon="Lock"
                show-password
            />
            <div class="password-hint">
              密码长度至少6位，建议包含字母和数字
            </div>
          </el-form-item>

          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
                v-model="registerForm.confirmPassword"
                type="password"
                placeholder="请再次输入密码"
                size="large"
                :prefix-icon="Lock"
                show-password
            />
          </el-form-item>

          <el-checkbox v-model="agreeTerms" required>
            我已阅读并同意
            <el-link type="primary" @click="showTerms = true">
              《用户协议》
            </el-link>
            和
            <el-link type="primary" @click="showPrivacy = true">
              《隐私政策》
            </el-link>
          </el-checkbox>

          <el-button
              type="primary"
              size="large"
              :loading="loading"
              @click="handleRegister"
              class="register-button"
              :disabled="!agreeTerms"
          >
            注册
          </el-button>

          <div class="login-link">
            已有账户？
            <el-link type="primary" @click="goToLogin">
              立即登录
            </el-link>
          </div>
        </el-form>
      </div>
    </div>

    <!-- 用户协议对话框 -->
    <el-dialog
        v-model="showTerms"
        title="用户协议"
        width="600px"
    >
      <div class="terms-content">
        <!-- 用户协议内容 -->
        <p>欢迎使用亿图了然图片管理系统...</p>
      </div>
      <template #footer>
        <el-button @click="showTerms = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 隐私政策对话框 -->
    <el-dialog
        v-model="showPrivacy"
        title="隐私政策"
        width="600px"
    >
      <div class="terms-content">
        <!-- 隐私政策内容 -->
        <p>我们非常重视您的隐私...</p>
      </div>
      <template #footer>
        <el-button @click="showPrivacy = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, Message, EditPen } from '@element-plus/icons-vue'
import { ElMessage, type FormInstance } from 'element-plus'
import { useUserStore } from '../stores/userStore'
import axios from 'axios'

const router = useRouter()
const userStore = useUserStore()
const registerFormRef = ref<FormInstance>()

const loading = ref(false)
const agreeTerms = ref(false)
const showTerms = ref(false)
const showPrivacy = ref(false)
const usernameAvailable = ref<boolean | null>(null)
const emailAvailable = ref<boolean | null>(null)

const registerForm = reactive({
  username: '',
  email: '',
  displayName: '',
  password: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule: any, value: string, callback: Function) => {
  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 1, max: 50, message: '用户名长度在1-50个字符之间', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const checkUsername = async () => {
  if (!registerForm.username) return

  try {
    // 这里可以调用后端接口检查用户名是否可用
    // const response = await axios.get(`/api/auth/check-username?username=${registerForm.username}`)
    // usernameAvailable.value = response.data.available

    // 暂时使用模拟检查
    usernameAvailable.value = Math.random() > 0.5
  } catch (error) {
    console.error('检查用户名失败:', error)
  }
}

const checkEmail = async () => {
  if (!registerForm.email) return

  try {
    // 这里可以调用后端接口检查邮箱是否可用
    // const response = await axios.get(`/api/auth/check-email?email=${registerForm.email}`)
    // emailAvailable.value = response.data.available

    // 暂时使用模拟检查
    emailAvailable.value = true
  } catch (error) {
    console.error('检查邮箱失败:', error)
  }
}

const handleRegister = async () => {
  if (!registerFormRef.value || !agreeTerms.value) return

  try {
    await registerFormRef.value.validate()
    loading.value = true

    const result = await userStore.register({
      username: registerForm.username,
      email: registerForm.email,
      password: registerForm.password,
      displayName: registerForm.displayName || registerForm.username
    })

    if (result.success) {
      ElMessage.success('注册成功')
      router.push('/gallery')
    } else {
      ElMessage.error(result.message || '注册失败')
    }
  } catch (error) {
    console.error('注册错误:', error)
  } finally {
    loading.value = false
  }
}

const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  padding: 20px;
}

.register-card {
  width: 100%;
  max-width: 480px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.logo-section {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
  padding: 32px 20px;
  text-align: center;
}

.logo-section h1 {
  margin: 0;
  font-size: 28px;
  font-weight: 600;
}

.logo-section p {
  margin: 8px 0 0;
  font-size: 16px;
  opacity: 0.9;
}

.form-section {
  padding: 32px 32px 40px;
}

.form-section h2 {
  margin: 0 0 24px;
  font-size: 22px;
  font-weight: 500;
  color: #333;
  text-align: center;
}

.availability-hint {
  font-size: 12px;
  margin-top: 4px;
}

.availability-hint .available {
  color: #67c23a;
}

.availability-hint .unavailable {
  color: #f56c6c;
}

.password-hint {
  font-size: 12px;
  color: #666;
  margin-top: 4px;
}

.register-button {
  width: 100%;
  height: 44px;
  margin-top: 20px;
}

.login-link {
  text-align: center;
  margin-top: 24px;
  color: #666;
  font-size: 14px;
}

.login-link a {
  margin-left: 8px;
}

.terms-content {
  max-height: 400px;
  overflow-y: auto;
  line-height: 1.6;
  color: #333;
}

@media (max-width: 768px) {
  .register-card {
    max-width: 100%;
  }

  .form-section {
    padding: 24px 20px 32px;
  }
}
</style>