<template>
  <div class="register-container">
    <div class="register-background">
      <div class="bg-shape shape-1"></div>
      <div class="bg-shape shape-2"></div>
      <div class="bg-shape shape-3"></div>
    </div>
    
    <div class="register-card">
      <div class="register-left">
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
              <span class="feature-icon">✨</span>
              <span>免费注册使用</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">⚡</span>
              <span>极速上传体验</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">🌐</span>
              <span>多端同步访问</span>
            </div>
          </div>
        </div>
      </div>
      
      <div class="register-right">
        <div class="form-section">
          <h2>创建账户</h2>
          <p class="subtitle">加入我们，开启智能图片管理之旅</p>

          <el-form
              ref="registerFormRef"
              :model="registerForm"
              :rules="registerRules"
              label-position="top"
              @submit.prevent="handleRegister"
              class="register-form"
          >
            <div class="form-row">
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

              <el-form-item label="显示名称" prop="displayName">
                <el-input
                    v-model="registerForm.displayName"
                    placeholder="可选"
                    size="large"
                    :prefix-icon="EditPen"
                />
              </el-form-item>
            </div>

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

            <div class="form-row">
              <el-form-item label="密码" prop="password">
                <el-input
                    v-model="registerForm.password"
                    type="password"
                    placeholder="至少6位"
                    size="large"
                    :prefix-icon="Lock"
                    show-password
                />
              </el-form-item>

              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input
                    v-model="registerForm.confirmPassword"
                    type="password"
                    placeholder="再次输入"
                    size="large"
                    :prefix-icon="Lock"
                    show-password
                />
              </el-form-item>
            </div>

            <el-checkbox v-model="agreeTerms" class="terms-checkbox">
              我已阅读并同意
              <el-link type="primary" @click.stop="showTerms = true">
                《用户协议》
              </el-link>
              和
              <el-link type="primary" @click.stop="showPrivacy = true">
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
              立即注册
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
    </div>

    <!-- 用户协议对话框 -->
    <el-dialog
        v-model="showTerms"
        title="用户协议"
        width="600px"
    >
      <div class="terms-content">
        <h3>1. 服务概述</h3>
        <p>本平台为用户提供图片上传、存储、管理、标签分类、AI分析等服务。用户可通过注册账户使用平台功能。</p>
        
        <h3>2. 注册与账户</h3>
        <ul>
          <li>用户需提供真实、准确的注册信息</li>
          <li>用户有义务保护账户安全，不得将账户转让他人</li>
          <li>用户可随时删除账户，删除后数据将不可恢复</li>
        </ul>
        
        <h3>3. 用户内容</h3>
        <ul>
          <li>用户上传的图片内容应合法、健康</li>
          <li>用户保留对上传内容的所有权</li>
          <li>平台仅提供存储和管理服务，不对内容承担责任</li>
          <li>禁止上传侵犯他人权益的内容</li>
        </ul>
        
        <h3>4. 数据隐私</h3>
        <ul>
          <li>用户数据仅用于提供服务功能</li>
          <li>未经用户同意，不向第三方披露用户信息</li>
          <li>用户可查看、修改、删除自己的数据</li>
        </ul>
        
        <h3>5. 服务限制</h3>
        <ul>
          <li>平台不对服务的连续性作保证</li>
          <li>用户应自行备份重要数据</li>
          <li>保留修改、暂停服务的权利</li>
        </ul>
        
        <h3>6. 免责条款</h3>
        <ul>
          <li>因不可抗力导致的服务中断不承担责任</li>
          <li>用户因使用本服务产生的损失不承担责任</li>
        </ul>
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
        <h3>1. 信息收集</h3>
        <p>我们收集以下信息：</p>
        <ul>
          <li><strong>注册信息</strong>：用户名、邮箱、密码</li>
          <li><strong>上传内容</strong>：用户上传的图片文件</li>
          <li><strong>元数据</strong>：图片EXIF信息、标签、描述</li>
          <li><strong>使用数据</strong>：访问记录、操作日志</li>
        </ul>
        
        <h3>2. 信息使用</h3>
        <p>收集的信息用于：</p>
        <ul>
          <li>提供图片存储和管理功能</li>
          <li>实现标签分类和AI分析</li>
          <li>优化服务性能和用户体验</li>
          <li>保障账户和数据安全</li>
        </ul>
        
        <h3>3. 信息保护</h3>
        <ul>
          <li>采用加密技术保护数据传输和存储</li>
          <li>严格限制员工访问用户数据的权限</li>
          <li>定期进行安全审计和漏洞修复</li>
        </ul>
        
        <h3>4. 信息共享</h3>
        <p>我们不会向第三方出售、交易或转让用户信息，以下情况除外：</p>
        <ul>
          <li>获得用户明确授权</li>
          <li>法律法规要求披露</li>
          <li>保护平台合法权益</li>
        </ul>
        
        <h3>5. 数据存储</h3>
        <ul>
          <li>用户数据存储在中国境内服务器</li>
          <li>用户可随时下载自己的数据</li>
          <li>删除账户时数据将被彻底清除</li>
        </ul>
        
        <h3>6. Cookie使用</h3>
        <p>使用Cookie技术改善用户体验，用户可选择禁用。</p>
        
        <h3>7. 隐私政策更新</h3>
        <p>政策变更将通过平台公告通知用户。</p>
        
        <h3>8. 联系方式</h3>
        <p>如有隐私相关问题，请联系：privacy@picturemanager.com</p>
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
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  padding: 20px;
  position: relative;
  overflow: hidden;
}

/* 背景装饰 */
.register-background {
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
  width: 500px;
  height: 500px;
  background: linear-gradient(135deg, #f093fb, #f5576c);
  top: -150px;
  left: -150px;
  animation: float 8s ease-in-out infinite;
}

.shape-2 {
  width: 350px;
  height: 350px;
  background: linear-gradient(135deg, #4facfe, #00f2fe);
  bottom: -100px;
  right: -100px;
  animation: float 6s ease-in-out infinite reverse;
}

.shape-3 {
  width: 250px;
  height: 250px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  top: 60%;
  left: 60%;
  animation: pulse 5s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0px) rotate(0deg); }
  50% { transform: translateY(-30px) rotate(5deg); }
}

@keyframes pulse {
  0%, 100% { transform: scale(1); opacity: 0.1; }
  50% { transform: scale(1.15); opacity: 0.18; }
}

.register-card {
  width: 100%;
  max-width: 1000px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 24px;
  box-shadow: 0 25px 80px rgba(0, 0, 0, 0.3);
  display: grid;
  grid-template-columns: 400px 1fr;
  overflow: hidden;
  position: relative;
  z-index: 1;
  backdrop-filter: blur(10px);
}

/* 左侧品牌区 */
.register-left {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  padding: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.register-left::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 60%);
  animation: rotate 25s linear infinite;
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
.register-right {
  padding: 40px 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow-y: auto;
  max-height: 100vh;
}

.form-section {
  width: 100%;
}

.form-section h2 {
  margin: 0;
  font-size: 28px;
  font-weight: 600;
  color: #1a1a2e;
}

.form-section .subtitle {
  margin: 8px 0 28px;
  color: #666;
  font-size: 14px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.register-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.register-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #333;
  padding-bottom: 4px;
}

.register-form :deep(.el-input__wrapper) {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.register-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 4px 12px rgba(240, 147, 251, 0.15);
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

.terms-checkbox {
  margin-bottom: 20px;
}

.register-button {
  width: 100%;
  height: 48px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 500;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  border: none;
  transition: all 0.3s;
}

.register-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(240, 147, 251, 0.4);
}

.register-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.login-link {
  text-align: center;
  margin-top: 20px;
  color: #666;
  font-size: 14px;
}

.terms-content {
  max-height: 400px;
  overflow-y: auto;
  line-height: 1.8;
  color: #333;
  padding: 8px 0;
}

/* 响应式 */
@media (max-width: 900px) {
  .register-card {
    grid-template-columns: 1fr;
    max-width: 480px;
  }

  .register-left {
    display: none;
  }

  .register-right {
    padding: 40px 32px;
  }

  .form-row {
    grid-template-columns: 1fr;
    gap: 0;
  }

  .form-section h2::before {
    content: '🖼️ ';
  }
}
</style>