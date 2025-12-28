<!-- src/views/UserView.vue -->
<template>
  <div class="user-container">
    <div class="user-profile">
      <!-- 用户头像和信息 -->
      <div class="profile-header">
        <div class="avatar-section">
          <el-avatar :size="120" :src="userInfo.avatarUrl">
            {{ userInfo.displayName?.charAt(0) }}
          </el-avatar>
          <el-upload
              class="avatar-upload"
              action="/api/user/avatar"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
              :before-upload="beforeAvatarUpload"
          >
            <el-button type="text">更换头像</el-button>
          </el-upload>
        </div>

        <div class="profile-info">
          <h1 class="display-name">{{ userInfo.displayName }}</h1>
          <p class="username">@{{ userInfo.username }}</p>
          <p class="email">
            <el-icon><Message /></el-icon>
            {{ userInfo.email }}
          </p>
          <div class="stats">
            <div class="stat-item">
              <span class="stat-number">{{ imageCount }}</span>
              <span class="stat-label">张图片</span>
            </div>
            <div class="stat-item">
              <span class="stat-number">{{ tagCount }}</span>
              <span class="stat-label">个标签</span>
            </div>
            <div class="stat-item">
              <span class="stat-number">{{ formatDate(userInfo.lastLoginAt) }}</span>
              <span class="stat-label">最后登录</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 标签页 -->
      <el-tabs v-model="activeTab" class="profile-tabs">
        <el-tab-pane label="个人信息" name="info">
          <el-form
              ref="infoFormRef"
              :model="infoForm"
              :rules="infoRules"
              label-width="100px"
              class="info-form"
          >
            <el-form-item label="显示名称" prop="displayName">
              <el-input
                  v-model="infoForm.displayName"
                  placeholder="请输入显示名称"
                  maxlength="50"
                  show-word-limit
              />
            </el-form-item>

            <el-form-item label="邮箱" prop="email">
              <el-input
                  v-model="infoForm.email"
                  placeholder="请输入邮箱"
                  type="email"
                  disabled
              />
              <div class="form-hint">
                邮箱不可修改，如需修改请联系管理员
              </div>
            </el-form-item>

            <el-form-item label="个人简介">
              <el-input
                  v-model="infoForm.bio"
                  type="textarea"
                  :rows="4"
                  placeholder="介绍一下自己吧"
                  maxlength="200"
                  show-word-limit
              />
            </el-form-item>

            <el-form-item>
              <el-button
                  type="primary"
                  @click="saveInfo"
                  :loading="savingInfo"
              >
                保存修改
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="安全设置" name="security">
          <div class="security-content">
            <h3>修改密码</h3>
            <el-form
                ref="passwordFormRef"
                :model="passwordForm"
                :rules="passwordRules"
                label-width="120px"
                class="password-form"
            >
              <el-form-item label="当前密码" prop="currentPassword">
                <el-input
                    v-model="passwordForm.currentPassword"
                    type="password"
                    placeholder="请输入当前密码"
                    show-password
                />
              </el-form-item>

              <el-form-item label="新密码" prop="newPassword">
                <el-input
                    v-model="passwordForm.newPassword"
                    type="password"
                    placeholder="请输入新密码"
                    show-password
                />
              </el-form-item>

              <el-form-item label="确认新密码" prop="confirmPassword">
                <el-input
                    v-model="passwordForm.confirmPassword"
                    type="password"
                    placeholder="请再次输入新密码"
                    show-password
                />
              </el-form-item>

              <el-form-item>
                <el-button
                    type="primary"
                    @click="changePassword"
                    :loading="changingPassword"
                >
                  修改密码
                </el-button>
              </el-form-item>
            </el-form>

            <el-divider />

            <h3>登录记录</h3>
            <div class="login-history">
              <div class="login-item" v-for="(login, index) in loginHistory" :key="index">
                <div class="login-info">
                  <el-icon><Monitor /></el-icon>
                  <span class="login-device">未知设备</span>
                  <span class="login-time">{{ formatDate(login.time) }}</span>
                </div>
                <el-tag v-if="index === 0" type="success" size="small">
                  本次登录
                </el-tag>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="账户设置" name="account">
          <div class="account-content">
            <h3>数据统计</h3>
            <div class="account-stats">
              <el-card shadow="never">
                <template #header>
                  <span>存储空间</span>
                </template>
                <div class="storage-info">
                  <el-progress
                      :percentage="storagePercentage"
                      :color="storageColor"
                      :stroke-width="8"
                      :show-text="false"
                  />
                  <div class="storage-details">
                    <span>已使用 {{ formatFileSize(usedStorage) }} / {{ formatFileSize(totalStorage) }}</span>
                    <span class="storage-percentage">{{ storagePercentage }}%</span>
                  </div>
                </div>
              </el-card>

              <el-card shadow="never">
                <template #header>
                  <span>活跃度</span>
                </template>
                <div class="activity-info">
                  <div class="activity-item">
                    <span class="activity-label">今日上传</span>
                    <span class="activity-value">{{ todayUploads }} 张</span>
                  </div>
                  <div class="activity-item">
                    <span class="activity-label">本周上传</span>
                    <span class="activity-value">{{ weekUploads }} 张</span>
                  </div>
                  <div class="activity-item">
                    <span class="activity-label">本月上传</span>
                    <span class="activity-value">{{ monthUploads }} 张</span>
                  </div>
                </div>
              </el-card>
            </div>

            <el-divider />

            <h3>账户操作</h3>
            <div class="account-actions">
              <el-button type="primary" plain @click="exportData">
                <el-icon><Download /></el-icon>
                导出数据
              </el-button>

              <el-button type="warning" plain @click="clearCache">
                <el-icon><Delete /></el-icon>
                清理缓存
              </el-button>

              <el-button type="danger" plain @click="showDeleteDialog">
                <el-icon><Warning /></el-icon>
                删除账户
              </el-button>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useUserStore } from '../stores/userStore'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Message,
  Monitor,
  Download,
  Delete,
  Warning
} from '@element-plus/icons-vue'
import { formatDate, formatFileSize } from '../utils/formatters'

const userStore = useUserStore()

// 数据
const activeTab = ref('info')
const userInfo = ref({
  username: '',
  email: '',
  displayName: '',
  avatarUrl: '',
  lastLoginAt: '',
  bio: ''
})

const infoForm = ref({
  displayName: '',
  email: '',
  bio: ''
})

const passwordForm = ref({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const savingInfo = ref(false)
const changingPassword = ref(false)

// 统计数据
const imageCount = ref(0)
const tagCount = ref(0)
const usedStorage = ref(0)
const totalStorage = ref(1024 * 1024 * 1024) // 1GB
const todayUploads = ref(0)
const weekUploads = ref(0)
const monthUploads = ref(0)

const loginHistory = ref([
  { time: new Date().toISOString(), device: 'Chrome on Windows' },
  { time: new Date(Date.now() - 86400000).toISOString(), device: 'Safari on iPhone' },
  { time: new Date(Date.now() - 172800000).toISOString(), device: 'Chrome on Windows' }
])

// 计算属性
const storagePercentage = computed(() => {
  return Math.round((usedStorage.value / totalStorage.value) * 100)
})

const storageColor = computed(() => {
  if (storagePercentage.value < 70) return '#67c23a'
  if (storagePercentage.value < 90) return '#e6a23c'
  return '#f56c6c'
})

// 验证规则
const infoRules = {
  displayName: [
    { required: true, message: '请输入显示名称', trigger: 'blur' },
    { max: 50, message: '显示名称不能超过50个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

// 密码验证函数（必须在passwordRules之前定义）
const validateNewPassword = (rule: any, value: string, callback: Function) => {
  if (value === passwordForm.value.currentPassword) {
    callback(new Error('新密码不能与当前密码相同'))
  } else {
    callback()
  }
}

const validateConfirmPassword = (rule: any, value: string, callback: Function) => {
  if (value !== passwordForm.value.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  currentPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' },
    { validator: validateNewPassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 计算属性
const userId = computed(() => userStore.user?.id)

// 方法
const loadUserInfo = async () => {
  // 从 userStore 获取用户信息
  if (userStore.user) {
    userInfo.value = {
      username: userStore.user.username || '',
      email: userStore.user.email || '',
      displayName: userStore.user.displayName || userStore.user.username || '',
      avatarUrl: userStore.user.avatarUrl || '',
      lastLoginAt: '',
      bio: ''
    }

    infoForm.value = {
      displayName: userInfo.value.displayName,
      email: userInfo.value.email,
      bio: userInfo.value.bio
    }
  }

  // 加载统计数据
  loadStatistics()
}

const loadStatistics = async () => {
  try {
    // 这里应该调用后端API获取统计数据
    // 暂时使用模拟数据
    imageCount.value = 42
    tagCount.value = 8
    usedStorage.value = 256 * 1024 * 1024 // 256MB
    todayUploads.value = 3
    weekUploads.value = 15
    monthUploads.value = 42
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const handleAvatarSuccess = (response: any) => {
  if (response.success) {
    userInfo.value.avatarUrl = response.data.avatarUrl
    ElMessage.success('头像更新成功')
  } else {
    ElMessage.error(response.message || '头像更新失败')
  }
}

const beforeAvatarUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB')
    return false
  }
  return true
}

const saveInfo = async () => {
  try {
    savingInfo.value = true
    // 调用后端API更新用户信息
    // 暂时模拟成功
    await new Promise(resolve => setTimeout(resolve, 1000))

    userInfo.value.displayName = infoForm.value.displayName
    userInfo.value.bio = infoForm.value.bio

    // 更新 userStore
    if (userStore.user) {
      userStore.user.displayName = infoForm.value.displayName
    }

    ElMessage.success('个人信息已更新')
  } catch (error) {
    ElMessage.error('更新失败')
  } finally {
    savingInfo.value = false
  }
}

const changePassword = async () => {
  try {
    changingPassword.value = true
    // 调用后端API修改密码
    // 暂时模拟成功
    await new Promise(resolve => setTimeout(resolve, 1000))

    passwordForm.value = {
      currentPassword: '',
      newPassword: '',
      confirmPassword: ''
    }

    ElMessage.success('密码修改成功')
  } catch (error) {
    ElMessage.error('密码修改失败')
  } finally {
    changingPassword.value = false
  }
}

const exportData = () => {
  ElMessage.info('导出功能开发中...')
}

const clearCache = () => {
  ElMessageBox.confirm('确定要清理本地缓存吗？这不会删除您的任何图片数据，但会需要重新登录。', '清理缓存', {
    type: 'warning'
  }).then(() => {
    // 保存token
    const token = localStorage.getItem('token')
    // 清理其他缓存
    localStorage.clear()
    // 恢复token
    if (token) {
      localStorage.setItem('token', token)
    }
    ElMessage.success('缓存已清理')
  })
}

const showDeleteDialog = () => {
  ElMessageBox.confirm(
      '此操作将永久删除您的账户和所有数据，且无法恢复。确定要继续吗？',
      '删除账户',
      {
        type: 'error',
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        confirmButtonClass: 'el-button--danger'
      }
  ).then(() => {
    ElMessage.info('账户删除功能开发中...')
  })
}

// 生命周期
onMounted(() => {
  // 如果用户信息已存在，直接加载
  if (userStore.user) {
    loadUserInfo()
  }
})

// 监听用户信息变化，确保用户信息已准备好
watch(() => userStore.user, (newUser) => {
  if (newUser) {
    loadUserInfo()
  }
}, { immediate: true })
</script>

<style scoped>
.user-container {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.user-profile {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.profile-header {
  display: flex;
  padding: 48px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  margin-right: 48px;
}

.avatar-upload :deep(.el-upload) {
  color: rgba(255, 255, 255, 0.9);
}

.avatar-upload :deep(.el-upload:hover) {
  color: white;
}

.profile-info {
  flex: 1;
}

.display-name {
  font-size: 36px;
  font-weight: 600;
  margin: 0 0 8px 0;
}

.username {
  font-size: 18px;
  opacity: 0.9;
  margin: 0 0 16px 0;
}

.email {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  margin: 0 0 32px 0;
}

.stats {
  display: flex;
  gap: 48px;
}

.stat-item {
  display: flex;
  flex-direction: column;
}

.stat-number {
  font-size: 28px;
  font-weight: 600;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
}

.profile-tabs {
  padding: 0 48px;
}

.profile-tabs :deep(.el-tabs__header) {
  margin: 0;
}

.profile-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
}

.info-form {
  padding: 32px 0;
  max-width: 600px;
}

.form-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.security-content {
  padding: 32px 0;
}

.security-content h3 {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 24px 0;
}

.password-form {
  max-width: 500px;
  margin-bottom: 48px;
}

.login-history {
  max-width: 500px;
}

.login-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #e4e7ed;
}

.login-item:last-child {
  border-bottom: none;
}

.login-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.login-device {
  font-weight: 500;
}

.login-time {
  font-size: 12px;
  color: #909399;
}

.account-content {
  padding: 32px 0;
}

.account-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 24px;
  margin-bottom: 48px;
}

.storage-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.storage-details {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  color: #606266;
}

.storage-percentage {
  font-weight: 600;
  color: #303133;
}

.activity-info {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.activity-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.activity-label {
  color: #606266;
}

.activity-value {
  font-weight: 600;
  color: #303133;
}

.account-actions {
  display: flex;
  gap: 16px;
  margin-top: 24px;
}

@media (max-width: 768px) {
  .profile-header {
    flex-direction: column;
    align-items: center;
    text-align: center;
    padding: 32px 24px;
  }

  .avatar-section {
    margin-right: 0;
    margin-bottom: 32px;
  }

  .stats {
    justify-content: center;
    gap: 32px;
  }

  .profile-tabs {
    padding: 0 24px;
  }

  .account-stats {
    grid-template-columns: 1fr;
  }

  .account-actions {
    flex-direction: column;
  }
}
</style>