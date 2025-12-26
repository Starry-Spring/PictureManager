<!-- src/views/ImageDetailView.vue -->
<template>
  <div class="image-detail-container" v-loading="loading">
    <!-- 返回按钮 -->
    <div class="back-button">
      <el-button @click="goBack" type="text">
        <el-icon><ArrowLeft /></el-icon>
        返回图片库
      </el-button>
    </div>

    <div class="image-content" v-if="image">
      <!-- 图片展示区域 -->
      <div class="image-display">
        <div class="image-wrapper">
          <img :src="getImageUrl()" :alt="image.title" class="main-image" />
          <div class="image-overlay">
            <div class="image-toolbar">
              <el-button-group>
                <el-button @click="downloadImage" type="primary">
                  <el-icon><Download /></el-icon>
                  下载
                </el-button>
                <el-button @click="editImage" type="warning">
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>
                <el-button @click="deleteImage" type="danger">
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </el-button-group>
            </div>
          </div>
        </div>
      </div>

      <!-- 图片信息区域 -->
      <div class="image-info">
        <!-- 标题和操作 -->
        <div class="info-header">
          <h1 class="image-title">{{ image.title }}</h1>
          <div class="header-actions">
            <el-button @click="toggleInfoPanel" type="text">
              <el-icon>
                <component :is="showInfoPanel ? 'ArrowUp' : 'ArrowDown'" />
              </el-icon>
              {{ showInfoPanel ? '隐藏详细信息' : '显示详细信息' }}
            </el-button>
          </div>
        </div>

        <!-- 基本信息 -->
        <div class="basic-info">
          <div class="info-row">
            <span class="info-label">上传时间:</span>
            <span class="info-value">{{ formatDate(image.uploadedAt) }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">文件大小:</span>
            <span class="info-value">{{ formatFileSize(image.fileSize) }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">分辨率:</span>
            <span class="info-value">{{ image.width }} × {{ image.height }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">格式:</span>
            <span class="info-value">{{ getFileExtension() }}</span>
          </div>
        </div>

        <!-- 标签 -->
        <div class="tags-section" v-if="image.tags && image.tags.size > 0">
          <h3>标签</h3>
          <div class="tags-container">
            <el-tag
                v-for="tag in image.tags"
                :key="tag"
                type="info"
                size="large"
                class="tag-item"
            >
              {{ tag }}
            </el-tag>
          </div>
        </div>

        <!-- 描述 -->
        <div class="description-section" v-if="image.description">
          <h3>描述</h3>
          <p class="description">{{ image.description }}</p>
        </div>

        <!-- 详细信息面板 -->
        <el-collapse-transition>
          <div class="detail-panel" v-show="showInfoPanel">
            <!-- EXIF信息 -->
            <div class="exif-section" v-if="hasExifInfo">
              <h3>EXIF 信息</h3>
              <div class="info-grid">
                <div class="info-item" v-if="image.cameraMake || image.cameraModel">
                  <span class="item-label">相机:</span>
                  <span class="item-value">{{ image.cameraMake }} {{ image.cameraModel }}</span>
                </div>
                <div class="info-item" v-if="image.takenAt">
                  <span class="item-label">拍摄时间:</span>
                  <span class="item-value">{{ formatDate(image.takenAt) }}</span>
                </div>
                <div class="info-item" v-if="image.exposureTime">
                  <span class="item-label">曝光时间:</span>
                  <span class="item-value">{{ image.exposureTime }}</span>
                </div>
                <div class="info-item" v-if="image.fNumber">
                  <span class="item-label">光圈:</span>
                  <span class="item-value">{{ image.fNumber }}</span>
                </div>
                <div class="info-item" v-if="image.isoSpeed">
                  <span class="item-label">ISO:</span>
                  <span class="item-value">{{ image.isoSpeed }}</span>
                </div>
                <div class="info-item" v-if="image.focalLength">
                  <span class="item-label">焦距:</span>
                  <span class="item-value">{{ image.focalLength }}</span>
                </div>
              </div>
            </div>

            <!-- 位置信息 -->
            <div class="location-section" v-if="image.latitude && image.longitude">
              <h3>拍摄位置</h3>
              <div class="location-info">
                <el-icon><Location /></el-icon>
                <span>纬度: {{ image.latitude.toFixed(6) }}, 经度: {{ image.longitude.toFixed(6) }}</span>
                <el-button type="text" @click="openInMaps" class="map-link">
                  在地图中查看
                </el-button>
              </div>
            </div>

            <!-- 文件信息 -->
            <div class="file-section">
              <h3>文件信息</h3>
              <div class="info-grid">
                <div class="info-item">
                  <span class="item-label">原始文件名:</span>
                  <span class="item-value">{{ image.originalFilename }}</span>
                </div>
                <div class="info-item">
                  <span class="item-label">MIME 类型:</span>
                  <span class="item-value">{{ image.mimeType }}</span>
                </div>
                <div class="info-item">
                  <span class="item-label">存储路径:</span>
                  <span class="item-value file-path">{{ image.filePath }}</span>
                </div>
              </div>
            </div>
          </div>
        </el-collapse-transition>
      </div>
    </div>

    <!-- 编辑对话框 -->
    <el-dialog
        v-model="editDialogVisible"
        title="编辑图片信息"
        width="500px"
        @closed="resetEditForm"
    >
      <el-form
          ref="editFormRef"
          :model="editForm"
          :rules="editRules"
          label-width="80px"
      >
        <el-form-item label="标题" prop="title">
          <el-input
              v-model="editForm.title"
              placeholder="请输入图片标题"
          />
        </el-form-item>

        <el-form-item label="描述" prop="description">
          <el-input
              v-model="editForm.description"
              type="textarea"
              :rows="4"
              placeholder="请输入图片描述"
          />
        </el-form-item>

        <el-form-item label="标签" prop="tags">
          <el-select
              v-model="editForm.tags"
              multiple
              filterable
              allow-create
              default-first-option
              placeholder="请输入标签"
              style="width: 100%"
          >
            <el-option
                v-for="tag in availableTags"
                :key="tag"
                :label="tag"
                :value="tag"
            />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button
            type="primary"
            @click="saveEdit"
            :loading="saving"
        >
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft,
  Download,
  Edit,
  Delete,
  ArrowUp,
  ArrowDown,
  Location
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/userStore'
import type { ImageResponseDTO } from '@/types/image'
import { formatDate, formatFileSize } from '@/utils/formatters'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 数据
const image = ref<ImageResponseDTO | null>(null)
const loading = ref(true)
const saving = ref(false)
const showInfoPanel = ref(false)
const availableTags = ref<string[]>([])

// 编辑相关
const editDialogVisible = ref(false)
const editForm = ref({
  title: '',
  description: '',
  tags: [] as string[]
})

const editRules = {
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { max: 100, message: '标题不能超过100个字符', trigger: 'blur' }
  ]
}

// 计算属性
const userId = computed(() => userStore.user?.id)
const imageId = computed(() => parseInt(route.params.id as string))

const hasExifInfo = computed(() => {
  return image.value?.cameraMake ||
      image.value?.cameraModel ||
      image.value?.takenAt ||
      image.value?.exposureTime ||
      image.value?.fNumber ||
      image.value?.isoSpeed ||
      image.value?.focalLength
})

// 方法
const getImageUrl = () => {
  if (!image.value) return ''
  return `/api/images/${image.value.id}/file?userId=${userId.value}`
}

const getFileExtension = () => {
  if (!image.value) return ''
  const filename = image.value.originalFilename || ''
  return filename.substring(filename.lastIndexOf('.') + 1).toUpperCase()
}

const loadImage = async () => {
  if (!userId.value) return

  loading.value = true
  try {
    const response = await fetch(`/api/images/${imageId.value}?userId=${userId.value}`)
    if (response.ok) {
      image.value = await response.json()
    } else {
      const error = await response.json()
      ElMessage.error(error.message || '加载图片失败')
      router.push('/gallery')
    }
  } catch (error) {
    ElMessage.error('加载图片失败')
    router.push('/gallery')
  } finally {
    loading.value = false
  }
}

const loadTags = async () => {
  try {
    const response = await fetch(`/api/images/tags?userId=${userId.value}`)
    availableTags.value = await response.json()
  } catch (error) {
    console.error('加载标签失败:', error)
  }
}

const goBack = () => {
  router.push('/gallery')
}

const downloadImage = async () => {
  if (!image.value) return

  try {
    const response = await fetch(getImageUrl())
    const blob = await response.blob()
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = image.value.title || image.value.originalFilename
    document.body.appendChild(a)
    a.click()
    window.URL.revokeObjectURL(url)
    document.body.removeChild(a)

    ElMessage.success('开始下载')
  } catch (error) {
    ElMessage.error('下载失败')
  }
}

const editImage = () => {
  if (!image.value) return

  editForm.value = {
    title: image.value.title || '',
    description: image.value.description || '',
    tags: Array.from(image.value.tags || [])
  }
  editDialogVisible.value = true
}

const deleteImage = async () => {
  try {
    await ElMessageBox.confirm('确定要删除这张图片吗？此操作不可撤销。', '删除确认', {
      type: 'warning',
      confirmButtonText: '确定删除',
      cancelButtonText: '取消'
    })

    const response = await fetch(`/api/images/${imageId.value}?userId=${userId.value}`, {
      method: 'DELETE'
    })

    if (response.ok) {
      ElMessage.success('图片已删除')
      router.push('/gallery')
    } else {
      const error = await response.json()
      ElMessage.error(error.message || '删除失败')
    }
  } catch (error) {
    // 用户取消删除
  }
}

const saveEdit = async () => {
  if (!image.value) return

  saving.value = true
  try {
    const response = await fetch(`/api/images/${image.value.id}?userId=${userId.value}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(editForm.value)
    })

    if (response.ok) {
      const updatedImage = await response.json()
      image.value = updatedImage
      editDialogVisible.value = false
      ElMessage.success('图片信息已更新')
    } else {
      const error = await response.json()
      ElMessage.error(error.message || '更新失败')
    }
  } catch (error) {
    ElMessage.error('更新失败')
  } finally {
    saving.value = false
  }
}

const resetEditForm = () => {
  editForm.value = {
    title: '',
    description: '',
    tags: []
  }
}

const toggleInfoPanel = () => {
  showInfoPanel.value = !showInfoPanel.value
}

const openInMaps = () => {
  if (image.value?.latitude && image.value?.longitude) {
    const url = `https://maps.google.com/?q=${image.value.latitude},${image.value.longitude}`
    window.open(url, '_blank')
  }
}

// 生命周期
onMounted(() => {
  loadImage()
  loadTags()
})
</script>

<style scoped>
.image-detail-container {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
}

.back-button {
  margin-bottom: 24px;
}

.image-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 48px;
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.image-display {
  position: relative;
  background: #f5f7fa;
  min-height: 600px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.image-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
  max-height: 700px;
  overflow: hidden;
  border-radius: 8px;
}

.main-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  transition: transform 0.3s;
}

.image-wrapper:hover .main-image {
  transform: scale(1.02);
}

.image-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 24px;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.7));
  opacity: 0;
  transition: opacity 0.3s;
}

.image-wrapper:hover .image-overlay {
  opacity: 1;
}

.image-toolbar {
  display: flex;
  justify-content: center;
}

.image-info {
  padding: 48px 32px;
  overflow-y: auto;
  max-height: 800px;
}

.info-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e4e7ed;
}

.image-title {
  margin: 0;
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  line-height: 1.4;
}

.basic-info {
  margin-bottom: 32px;
}

.info-row {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.info-label {
  width: 100px;
  color: #909399;
  font-size: 14px;
}

.info-value {
  flex: 1;
  color: #303133;
  font-weight: 500;
}

.tags-section,
.description-section {
  margin-bottom: 32px;
}

.tags-section h3,
.description-section h3 {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 12px 0;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-item {
  font-size: 14px;
  padding: 6px 12px;
}

.description {
  margin: 0;
  line-height: 1.6;
  color: #606266;
  white-space: pre-wrap;
}

.detail-panel {
  margin-top: 32px;
  padding-top: 32px;
  border-top: 1px solid #e4e7ed;
}

.exif-section,
.location-section,
.file-section {
  margin-bottom: 24px;
}

.exif-section h3,
.location-section h3,
.file-section h3 {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 16px 0;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
}

.item-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.item-value {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
  word-break: break-all;
}

.file-path {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
  color: #67c23a;
  background: #f0f9eb;
  padding: 4px 8px;
  border-radius: 4px;
}

.location-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
}

.map-link {
  margin-left: auto;
}

@media (max-width: 1024px) {
  .image-content {
    grid-template-columns: 1fr;
  }

  .image-display {
    min-height: 400px;
  }

  .image-info {
    max-height: none;
  }
}

@media (max-width: 768px) {
  .image-title {
    font-size: 22px;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>