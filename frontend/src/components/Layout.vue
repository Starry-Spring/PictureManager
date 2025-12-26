<template>
  <div class="main-layout">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="header-left">
        <router-link to="/gallery" class="logo">
          <span class="logo-text">亿图了然</span>
        </router-link>

        <nav class="nav-menu">
          <router-link to="/gallery" class="nav-item" active-class="active">
            <el-icon><Picture /></el-icon>
            <span>图片库</span>
          </router-link>
          <router-link to="/ai" class="nav-item" active-class="active">
            <el-icon><MagicStick /></el-icon>
            <span>AI分析</span>
          </router-link>
          <router-link to="/user" class="nav-item" active-class="active">
            <el-icon><User /></el-icon>
            <span>用户设置</span>
          </router-link>
        </nav>
      </div>

      <div class="header-right">
        <el-input
            v-model="searchKeyword"
            placeholder="搜索图片..."
            class="search-input"
            size="small"
            :prefix-icon="Search"
            @keyup.enter="handleSearch"
        />

        <el-dropdown @command="handleUserCommand" trigger="click">
          <div class="user-info">
            <el-avatar :size="32" :src="userStore.user?.avatarUrl" class="user-avatar">
              {{ userStore.user?.username?.charAt(0)?.toUpperCase() }}
            </el-avatar>
            <span class="username">{{ userStore.user?.displayName || userStore.user?.username }}</span>
            <el-icon><ArrowDown /></el-icon>
          </div>

          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="user">
                <el-icon><User /></el-icon>
                个人中心
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

    <!-- 主内容区 -->
    <main class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <!-- 上传按钮（固定在右下角） -->
    <el-tooltip content="上传图片" placement="left">
      <el-button
          type="primary"
          circle
          class="upload-fab"
          @click="showUploadDialog = true"
      >
        <el-icon><Plus /></el-icon>
      </el-button>
    </el-tooltip>

    <!-- 上传对话框 -->
    <el-dialog
        v-model="showUploadDialog"
        title="上传图片"
        width="500px"
        @close="handleUploadClose"
    >
      <UploadImageForm @success="handleUploadSuccess" />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/userStore'
import {
  Picture,
  MagicStick,
  Search,
  User,
  Setting,
  SwitchButton,
  ArrowDown,
  Plus
} from '@element-plus/icons-vue'
import UploadImageForm from '../components/UploadImageForm.vue'

const router = useRouter()
const userStore = useUserStore()

const searchKeyword = ref('')
const showUploadDialog = ref(false)

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    // 跳转到搜索页面或执行搜索
    console.log('搜索:', searchKeyword.value)
  }
}

const handleUserCommand = (command: string) => {
  switch (command) {
    case 'user':
      router.push('/user')
      break
    case 'settings':
      // 跳转到设置页面
      break
    case 'logout':
      userStore.logout()
      break
  }
}

const handleUploadSuccess = () => {
  showUploadDialog.value = false
  // 刷新图片列表
}

const handleUploadClose = () => {
  // 清理上传状态
}
</script>

<style scoped>
.main-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  height: 60px;
  background: white;
  border-bottom: 1px solid #f0f0f0;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: sticky;
  top: 0;
  z-index: 1000;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 32px;
}

.logo {
  text-decoration: none;
  color: #409eff;
  font-weight: 600;
  font-size: 18px;
}

.logo-text {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.nav-menu {
  display: flex;
  gap: 4px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  text-decoration: none;
  color: #666;
  border-radius: 6px;
  transition: all 0.3s;
  font-size: 14px;
}

.nav-item:hover {
  background-color: #f5f7fa;
  color: #409eff;
}

.nav-item.active {
  background-color: #ecf5ff;
  color: #409eff;
  font-weight: 500;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.search-input {
  width: 200px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 8px;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: #f5f7fa;
}

.user-avatar {
  background-color: #409eff;
  color: white;
}

.username {
  font-size: 14px;
  color: #333;
}

.main-content {
  flex: 1;
  background-color: #f8f9fa;
  padding: 24px;
}

.upload-fab {
  position: fixed;
  right: 40px;
  bottom: 40px;
  width: 56px;
  height: 56px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@media (max-width: 768px) {
  .header {
    padding: 0 16px;
  }

  .nav-item span {
    display: none;
  }

  .search-input {
    width: 150px;
  }

  .username {
    display: none;
  }

  .main-content {
    padding: 16px;
  }

  .upload-fab {
    right: 20px;
    bottom: 20px;
  }
}
</style>