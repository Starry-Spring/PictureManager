<template>
  <div class="image-detail-container">
    <div class="image-detail-header">
      <el-button @click="goBack" icon="ArrowLeft" plain>返回</el-button>
      <h1 class="image-title">{{ image?.title || '图片详情' }}</h1>
      <el-button @click="saveChanges" type="primary" :loading="saving" :disabled="!isEditing">
        保存更改
      </el-button>
    </div>

    <div class="image-detail-content">
      <div class="image-preview-section">
        <div class="image-display">
          <img :src="imageSrc" :alt="image?.title" class="main-image" v-if="imageSrc" />
          <div class="image-placeholder" v-else>图片加载中...</div>
        </div>
        
        <!-- 图片编辑工具栏 -->
        <div class="editing-tools" v-if="isEditing">
          <h3>编辑工具</h3>
          <div class="tool-group">
            <div class="tool-item">
              <label>亮度:</label>
              <el-slider v-model="editSettings.brightness" :min="-100" :max="100" @change="applyFilters" />
            </div>
            <div class="tool-item">
              <label>对比度:</label>
              <el-slider v-model="editSettings.contrast" :min="-100" :max="100" @change="applyFilters" />
            </div>
            <div class="tool-item">
              <label>饱和度:</label>
              <el-slider v-model="editSettings.saturation" :min="-100" :max="100" @change="applyFilters" />
            </div>
          </div>
          
          <el-button @click="resetFilters" plain>重置</el-button>
        </div>
      </div>

      <div class="image-info-section">
        <el-card class="info-card">
          <template #header>
            <div class="card-header">
              <span>图片信息</span>
            </div>
          </template>
          
          <div class="info-item" v-if="image">
            <label>标题:</label>
            <el-input v-model="image.title" @change="markAsEditing" v-if="isEditing" />
            <span v-else>{{ image.title || '未设置' }}</span>
          </div>
          
          <div class="info-item">
            <label>描述:</label>
            <el-input 
              v-model="image.description" 
              type="textarea" 
              :rows="3" 
              @change="markAsEditing"
              v-if="isEditing" 
            />
            <p v-else>{{ image.description || '未设置' }}</p>
          </div>
          
          <div class="info-item">
            <label>标签:</label>
            <el-select
              v-model="image.tags"
              multiple
              filterable
              allow-create
              default-first-option
              placeholder="添加或选择标签"
              @change="markAsEditing"
              v-if="isEditing"
            >
              <el-option
                v-for="tag in allTags"
                :key="tag"
                :label="tag"
                :value="tag"
              />
            </el-select>
            <div v-else class="tags-display">
              <el-tag 
                v-for="tag in image.tags" 
                :key="tag" 
                size="small" 
                style="margin-right: 8px; margin-bottom: 4px;"
              >
                {{ tag }}
              </el-tag>
              <span v-if="!image.tags || image.tags.length === 0">无标签</span>
            </div>
          </div>
        </el-card>

        <!-- EXIF信息卡片 -->
        <el-card class="info-card" v-if="exifInfo">
          <template #header>
            <div class="card-header">
              <span>EXIF信息</span>
            </div>
          </template>
          
          <div class="info-item" v-if="exifInfo.DateTime">
            <label>拍摄时间:</label>
            <span>{{ formatDate(exifInfo.DateTime) }}</span>
          </div>
          
          <div class="info-item" v-if="exifInfo.Make || exifInfo.Model">
            <label>设备:</label>
            <span>{{ exifInfo.Make }} {{ exifInfo.Model }}</span>
          </div>
          
          <div class="info-item" v-if="exifInfo.FNumber">
            <label>光圈:</label>
            <span>f/{{ exifInfo.FNumber }}</span>
          </div>
          
          <div class="info-item" v-if="exifInfo.ExposureTime">
            <label>快门:</label>
            <span>1/{{ Math.round(1/exifInfo.ExposureTime) }}s</span>
          </div>
          
          <div class="info-item" v-if="exifInfo.ISO">
            <label>ISO:</label>
            <span>{{ exifInfo.ISO }}</span>
          </div>
          
          <div class="info-item" v-if="exifInfo.FocalLength">
            <label>焦距:</label>
            <span>{{ exifInfo.FocalLength }}mm</span>
          </div>
          
          <div class="info-item" v-if="exifInfo.GPSLatitude && exifInfo.GPSLongitude">
            <label>位置:</label>
            <span>纬度: {{ exifInfo.GPSLatitude }}, 经度: {{ exifInfo.GPSLongitude }}</span>
          </div>
        </el-card>

        <!-- 文件信息卡片 -->
        <el-card class="info-card">
          <template #header>
            <div class="card-header">
              <span>文件信息</span>
            </div>
          </template>
          
          <div class="info-item" v-if="image">
            <label>文件名:</label>
            <span>{{ image.originalFilename }}</span>
          </div>
          
          <div class="info-item">
            <label>文件大小:</label>
            <span>{{ formatFileSize(image?.fileSize) }}</span>
          </div>
          
          <div class="info-item">
            <label>上传时间:</label>
            <span>{{ image ? formatDate(image.uploadedAt) : '' }}</span>
          </div>
          
          <div class="info-item">
            <label>分辨率:</label>
            <span v-if="image">{{ image.width }} × {{ image.height }}</span>
          </div>
        </el-card>

        <!-- 操作按钮 -->
        <div class="action-buttons">
          <el-button @click="toggleEdit" :type="isEditing ? 'warning' : 'primary'" :icon="isEditing ? 'Close' : 'Edit'">
            {{ isEditing ? '取消编辑' : '编辑信息' }}
          </el-button>
          <el-button @click="generateTagsFromExif" icon="MagicStick" plain>
            从EXIF生成标签
          </el-button>
          <el-button @click="downloadImage" icon="Download" type="success">
            下载原图
          </el-button>
          <el-button @click="deleteImage" icon="Delete" type="danger">
            删除图片
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../stores/userStore'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatDate, formatFileSize } from '../utils/formatters'
import type { ImageResponseDTO } from '../types/image'
import axios from 'axios'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 数据
const image = ref<ImageResponseDTO | null>(null)
const imageSrc = ref('')
const exifInfo = ref<any>(null)
const allTags = ref<string[]>([])
const saving = ref(false)
const isEditing = ref(false)
const originalData = ref<ImageResponseDTO | null>(null)

// 编辑设置
const editSettings = ref({
  brightness: 0,
  contrast: 0,
  saturation: 0
})

// 计算属性
const userId = computed(() => userStore.user?.id)
const imageId = computed(() => Number(route.params.id))

// 获取图片详细信息
const loadImage = async () => {
  if (!userId.value || !imageId.value) return

  try {
    const response = await axios.get(`/api/images/${imageId.value}`, {
      params: {
        userId: userId.value
      }
    })
    image.value = response.data
    originalData.value = JSON.parse(JSON.stringify(response.data)) // 保存原始数据副本
    
    // 加载图片源
    await loadOriginalImage()
    
    // 加载EXIF信息
    await loadExifInfo()
    
    // 加载所有标签
    await loadAllTags()
  } catch (error) {
    console.error('加载图片失败:', error)
    ElMessage.error('加载图片失败')
    router.push('/gallery')
  }
}

// 加载原始图片
const loadOriginalImage = async () => {
  try {
    const token = userStore.token
    const response = await axios.get(`/api/images/${imageId.value}/file`, {
      params: {
        token: token
      },
      responseType: 'blob'
    })
    
    const blob = new Blob([response.data], { type: response.data.type })
    imageSrc.value = URL.createObjectURL(blob)
  } catch (error) {
    console.error('加载图片失败:', error)
  }
}

// 加载EXIF信息
const loadExifInfo = async () => {
  try {
    const response = await axios.get(`/api/images/${imageId.value}/exif`, {
      params: {
        userId: userId.value
      }
    })
    exifInfo.value = response.data
  } catch (error) {
    console.error('加载EXIF信息失败:', error)
    // EXIF信息是可选的，即使加载失败也不影响主要功能
  }
}

// 加载所有标签
const loadAllTags = async () => {
  try {
    const response = await axios.get(`/api/images/tags`, {
      params: {
        userId: userId.value
      }
    })
    allTags.value = response.data
  } catch (error) {
    console.error('加载标签失败:', error)
  }
}

// 保存更改
const saveChanges = async () => {
  if (!image.value || !userId.value) return

  saving.value = true
  try {
    const response = await axios.put(`/api/images/${image.value.id}`, {
      title: image.value.title,
      description: image.value.description,
      tags: image.value.tags
    }, {
      params: {
        userId: userId.value
      }
    })

    if (response.status === 200) {
      ElMessage.success('更改已保存')
      isEditing.value = false
      originalData.value = JSON.parse(JSON.stringify(image.value)) // 更新原始数据副本
    } else {
      ElMessage.error('保存失败')
    }
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 标记为正在编辑
const markAsEditing = () => {
  isEditing.value = true
}

// 切换编辑模式
const toggleEdit = () => {
  if (isEditing.value) {
    // 取消编辑，恢复原始数据
    if (image.value && originalData.value) {
      image.value.title = originalData.value.title
      image.value.description = originalData.value.description
      image.value.tags = [...originalData.value.tags || []]
    }
  }
  isEditing.value = !isEditing.value
}

// 从EXIF信息生成标签
const generateTagsFromExif = () => {
  if (!exifInfo.value || !image.value) return

  const newTags: string[] = []

  // 根据设备信息生成标签
  if (exifInfo.value.Make) {
    newTags.push(exifInfo.value.Make)
  }
  if (exifInfo.value.Model) {
    newTags.push(exifInfo.value.Model)
  }

  // 根据拍摄时间生成标签
  if (exifInfo.value.DateTime) {
    const date = new Date(exifInfo.value.DateTime)
    newTags.push(`${date.getFullYear()}年`)
    newTags.push(`第${Math.ceil(date.getMonth()/3)+1}季度`)
  }

  // 根据光圈值生成标签
  if (exifInfo.value.FNumber) {
    if (exifInfo.value.FNumber < 2.8) {
      newTags.push('大光圈')
    } else if (exifInfo.value.FNumber > 8) {
      newTags.push('小光圈')
    }
  }

  // 根据ISO值生成标签
  if (exifInfo.value.ISO) {
    if (exifInfo.value.ISO > 1600) {
      newTags.push('高ISO')
    } else if (exifInfo.value.ISO < 400) {
      newTags.push('低ISO')
    }
  }

  // 根据焦距生成标签
  if (exifInfo.value.FocalLength) {
    if (exifInfo.value.FocalLength < 35) {
      newTags.push('广角')
    } else if (exifInfo.value.FocalLength > 100) {
      newTags.push('长焦')
    }
  }

  // 添加到现有标签中，避免重复
  if (image.value.tags) {
    newTags.forEach(tag => {
      if (!image.value?.tags.includes(tag)) {
        image.value?.tags.push(tag)
      }
    })
  } else {
    image.value.tags = newTags
  }

  isEditing.value = true
  ElMessage.success('已根据EXIF信息生成标签')
}

// 应用滤镜效果
const applyFilters = () => {
  const img = document.querySelector('.main-image') as HTMLImageElement
  if (img) {
    img.style.filter = `brightness(${100 + editSettings.value.brightness}%) 
                       contrast(${100 + editSettings.value.contrast}%) 
                       saturate(${100 + editSettings.value.saturation}%)`
  }
}

// 重置滤镜
const resetFilters = () => {
  editSettings.value = {
    brightness: 0,
    contrast: 0,
    saturation: 0
  }
  applyFilters()
}

// 下载图片
const downloadImage = async () => {
  try {
    const token = userStore.token
    const response = await axios.get(`/api/images/${imageId.value}/file`, {
      params: {
        token: token
      },
      responseType: 'blob'
    })

    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', image.value?.originalFilename || `image-${imageId.value}`)
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('开始下载')
  } catch (error) {
    console.error('下载失败:', error)
    ElMessage.error('下载失败')
  }
}

// 删除图片
const deleteImage = async () => {
  try {
    await ElMessageBox.confirm('确定要删除这张图片吗？此操作不可撤销。', '删除确认', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const response = await axios.delete(`/api/images/${imageId.value}`, {
      params: {
        userId: userId.value
      }
    })

    if (response.status === 200) {
      ElMessage.success('图片已删除')
      router.push('/gallery')
    } else {
      ElMessage.error('删除失败')
    }
  } catch (error) {
    console.error('删除失败:', error)
  }
}

// 返回上一页
const goBack = () => {
  router.go(-1)
}

onMounted(() => {
  loadImage()
})
</script>

<style scoped>
.image-detail-container {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
}

.image-detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #eee;
}

.image-title {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.image-detail-content {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 24px;
}

.image-preview-section {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.image-display {
  width: 100%;
  text-align: center;
  margin-bottom: 24px;
}

.main-image {
  max-width: 100%;
  max-height: 70vh;
  object-fit: contain;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.image-placeholder {
  height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  border-radius: 8px;
  color: #999;
}

.editing-tools {
  width: 100%;
  padding: 16px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.tool-group {
  margin: 16px 0;
}

.tool-item {
  margin-bottom: 16px;
}

.tool-item label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
}

.info-card {
  margin-bottom: 24px;
}

.card-header {
  font-weight: 600;
  font-size: 16px;
}

.info-item {
  margin-bottom: 16px;
}

.info-item label {
  display: block;
  font-weight: 500;
  color: #666;
  margin-bottom: 4px;
}

.info-item span,
.info-item p {
  color: #333;
}

.tags-display {
  display: flex;
  flex-wrap: wrap;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 24px;
}

@media (max-width: 768px) {
  .image-detail-content {
    grid-template-columns: 1fr;
  }
  
  .image-detail-header {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }
}
</style>