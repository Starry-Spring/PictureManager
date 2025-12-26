<!-- src/components/Layout.vue -->
<template>
  <div class="app-container">
    <!-- 顶部导航栏 -->
    <header class="app-header">
      <div class="header-left">
        <h1 class="logo" @click="goToHome">亿图了然</h1>
        <el-menu
            :default-active="activeMenu"
            mode="horizontal"
            @select="handleMenuSelect"
            class="nav-menu"
        >
          <el-menu-item index="/gallery">
            <el-icon><Picture /></el-icon>
            <span>图片库</span>
          </el-menu-item>
          <el-menu-item index="/ai">
            <el-icon><MagicStick /></el-icon>
            <span>AI 识别</span>
          </el-menu-item>
        </el-menu>
      </div>

      <div class="header-right">
        <el-dropdown @command="handleUserCommand">
          <div class="user-info">
            <el-avatar :size="36" :src="userStore.user?.avatarUrl" />
            <span class="username">{{ userStore.user?.displayName || userStore.user?.username }}</span>
            <el-icon><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">
                <el-icon><User /></el-icon>
                个人资料
              </el-dropdown-item>
              <el-dropdown-item command="settings">
                <el-icon><Setting /></el-icon>
                设置
              </el-dropdown-item>
              <el-dropdown-item divided command="logout">
                <el-icon><SwitchButton /></el-icon>
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <!-- 主要内容区 -->
    <main class="app-main">
      <router-view />
    </main>

    <!-- 上传图片对话框 -->
    <el-dialog
        v-model="uploadDialogVisible"
        title="上传图片"
        width="500px"
        @close="handleUploadClose"
    >
      <upload-image-form
          v-if="uploadDialogVisible"
          @success="handleUploadSuccess"
          @cancel="uploadDialogVisible = false"
      />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, provide } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../stores/userStore'
import {
  Picture,
  MagicStick,
  User as UserIcon,
  Setting,
  SwitchButton,
  ArrowDown
} from '@element-plus/icons-vue'
import UploadImageForm from '../components/UploadImageForm.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)
const uploadDialogVisible = ref(false)

const handleMenuSelect = (index: string) => {
  router.push(index)
}

const handleUserCommand = (command: string) => {
  switch (command) {
    case 'profile':
      router.push('/user')
      break
    case 'settings':
      // 打开设置页面
      break
    case 'logout':
      userStore.logout()
      router.push('/login')
      break
  }
}

const goToHome = () => {
  router.push('/gallery')
}

const openUploadDialog = () => {
  uploadDialogVisible.value = true
}

// 提供给子组件使用
provide('openUploadDialog', openUploadDialog)

const handleUploadSuccess = () => {
  uploadDialogVisible.value = false
  // 刷新图片列表
  window.location.reload()
}

const handleUploadClose = () => {
  uploadDialogVisible.value = false
}

// 暴露方法给子组件调用
defineExpose({
  openUploadDialog
})
</script>

<style scoped>
.app-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.app-header {
  height: 60px;
  background: white;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  position: sticky;
  top: 0;
  z-index: 1000;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 40px;
}

.logo {
  font-size: 24px;
  font-weight: 600;
  color: #409eff;
  margin: 0;
  cursor: pointer;
  user-select: none;
}

.logo:hover {
  color: #79bbff;
}

.nav-menu {
  border-bottom: none;
  height: 60px;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: #f5f7fa;
}

.username {
  font-size: 14px;
  color: #303133;
}

.app-main {
  flex: 1;
  background: #f5f7fa;
  overflow: auto;
}
</style>