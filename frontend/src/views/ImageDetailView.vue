<template>
  <div class="image-detail-container">
    <!-- 顶部导航 -->
    <div class="image-detail-header">
      <el-button @click="goBack" icon="ArrowLeft" plain>返回</el-button>
      <h1 class="image-title">{{ image?.title || '图片详情' }}</h1>
      <div class="header-actions">
        <el-button @click="toggleEdit" :type="isEditing ? 'warning' : 'primary'">
          {{ isEditing ? '取消编辑' : '编辑' }}
        </el-button>
        <el-button @click="saveChanges" type="success" :loading="saving" :disabled="!isEditing">
          保存
        </el-button>
      </div>
    </div>

    <!-- 主要内容区域：左侧图片 + 右侧编辑工具 -->
    <div class="main-content">
      <!-- 左侧图片预览 -->
      <div class="image-preview-section">
        <div class="crop-preview-container" :style="cropContainerStyle" v-if="imageSrc">
          <img 
            :src="imageSrc" 
            :alt="image?.title" 
            class="main-image" 
            ref="mainImageRef"
            :style="cropImageStyle"
          />
        </div>
        <div class="image-placeholder" v-else>图片加载中...</div>
        <div class="crop-info" v-if="isEditing && hasCropSettings">
          预览尺寸: {{ previewWidth }} × {{ previewHeight }}
        </div>
      </div>

      <!-- 右侧编辑工具 -->
      <div class="editing-panel" v-if="isEditing && image">
        <el-card class="tool-card">
          <template #header>
            <span>色调调整</span>
          </template>
          <div class="tool-item">
            <label>亮度</label>
            <el-slider v-model="editSettings.brightness" :min="-100" :max="100" @change="applyFilters" />
          </div>
          <div class="tool-item">
            <label>对比度</label>
            <el-slider v-model="editSettings.contrast" :min="-100" :max="100" @change="applyFilters" />
          </div>
          <div class="tool-item">
            <label>饱和度</label>
            <el-slider v-model="editSettings.saturation" :min="-100" :max="100" @change="applyFilters" />
          </div>
          <el-button @click="resetFilters" size="small" plain>重置滤镜</el-button>
        </el-card>

        <el-card class="tool-card">
          <template #header>
            <span>裁剪设置</span>
          </template>
          <div class="tool-item">
            <label>宽度裁剪（像素）</label>
            <el-input-number 
              v-model="editSettings.cropLeft" 
              :min="-(image?.width || 0)" 
              :max="image?.width || 0"
              size="small"
              style="width: 100%;"
              @change="applyCropPreview"
            />
            <span class="hint">正数从左保留，负数从右保留</span>
          </div>
          <div class="tool-item">
            <label>高度裁剪（像素）</label>
            <el-input-number 
              v-model="editSettings.cropTop" 
              :min="-(image?.height || 0)" 
              :max="image?.height || 0"
              size="small"
              style="width: 100%;"
              @change="applyCropPreview"
            />
            <span class="hint">正数从上保留，负数从下保留</span>
          </div>
          <div class="current-size">当前: {{ image?.width }} × {{ image?.height }}</div>
          <el-button @click="resetCrop" size="small" plain v-if="hasCropSettings">重置裁剪</el-button>
        </el-card>
      </div>
    </div>

    <!-- 下方信息区域 -->
    <div class="info-section">
      <!-- 图片信息 -->
      <el-card class="info-card">
        <template #header><span>图片信息</span></template>
        <div class="info-item" v-if="image">
          <label>标题:</label>
          <el-input v-model="image.title" @change="markAsEditing" v-if="isEditing" size="small" />
          <span v-else>{{ image.title || '未设置' }}</span>
        </div>
        <div class="info-item" v-if="image">
          <label>描述:</label>
          <el-input v-model="image.description" type="textarea" :rows="2" @change="markAsEditing" v-if="isEditing" />
          <p v-else>{{ image.description || '未设置' }}</p>
        </div>
        <div class="info-item" v-if="image">
          <label>标签:</label>
          <el-select v-model="image.tags" multiple filterable allow-create default-first-option placeholder="添加标签" @change="markAsEditing" v-if="isEditing" style="width: 100%;">
            <el-option v-for="tag in allTags" :key="tag" :label="tag" :value="tag" />
          </el-select>
          <div v-else class="tags-display">
            <el-tag v-for="tag in image.tags" :key="tag" size="small" style="margin-right: 6px;">{{ tag }}</el-tag>
            <span v-if="!image.tags || image.tags.length === 0">无标签</span>
          </div>
        </div>
      </el-card>

      <!-- EXIF信息 -->
      <el-card class="info-card">
        <template #header><span>EXIF信息</span></template>
        <div class="exif-grid">
          <div class="exif-item" v-if="image?.width">
            <label>分辨率</label>
            <span>{{ image.width }} × {{ image.height }}</span>
          </div>
          <div class="exif-item" v-if="exifInfo?.DateTime">
            <label>拍摄时间</label>
            <span>{{ formatDate(exifInfo.DateTime) }}</span>
          </div>
          <div class="exif-item" v-if="exifInfo?.GPSLatitude || exifInfo?.GPSLongitude">
            <label>拍摄地点</label>
            <span>{{ `纬度${exifInfo.GPSLatitude}, 经度${exifInfo.GPSLongitude}` }}</span>
          </div>
          <div class="exif-item" v-if="exifInfo?.Make || exifInfo?.Model">
            <label>拍摄设备</label>
            <span>{{ [exifInfo?.Make, exifInfo?.Model].filter(Boolean).join(' ') }}</span>
          </div>
          <div class="exif-item" v-if="exifInfo?.FNumber">
            <label>光圈</label>
            <span>f/{{ exifInfo.FNumber }}</span>
          </div>
          <div class="exif-item" v-if="exifInfo?.ExposureTime">
            <label>快门</label>
            <span>1/{{ Math.round(1/exifInfo.ExposureTime) }}s</span>
          </div>
          <div class="exif-item" v-if="exifInfo?.ISO">
            <label>ISO</label>
            <span>{{ exifInfo.ISO }}</span>
          </div>
          <div class="exif-item" v-if="exifInfo?.FocalLength">
            <label>焦距</label>
            <span>{{ exifInfo.FocalLength }}mm</span>
          </div>
        </div>
      </el-card>

      <!-- 文件信息 -->
      <el-card class="info-card">
        <template #header><span>文件信息</span></template>
        <div class="file-info-grid">
          <div class="info-item">
            <label>文件名:</label>
            <span>{{ image?.originalFilename }}</span>
          </div>
          <div class="info-item">
            <label>文件大小:</label>
            <span>{{ formatFileSize(image?.fileSize) }}</span>
          </div>
          <div class="info-item">
            <label>上传时间:</label>
            <span>{{ image ? formatDate(image.uploadedAt) : '' }}</span>
          </div>
        </div>
      </el-card>

      <!-- 操作按钮 -->
      <el-card class="info-card">
        <template #header><span>操作</span></template>
        <div class="action-buttons">
          <el-button @click="generateTagsFromExif" icon="MagicStick" plain>从EXIF生成标签</el-button>
          <el-button @click="downloadImage" icon="Download" type="success">下载原图</el-button>
          <el-button @click="deleteImage" icon="Delete" type="danger">删除图片</el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick, watch } from 'vue'
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
const mainImageRef = ref<HTMLImageElement | null>(null)

// 编辑设置
const editSettings = ref({
  brightness: 0,
  contrast: 0,
  saturation: 0,
  cropLeft: null as number | null,
  cropTop: null as number | null
})

// 计算属性
const userId = computed(() => userStore.user?.id)
const imageId = computed(() => Number(route.params.id))

// 是否有裁剪设置
const hasCropSettings = computed(() => {
  return editSettings.value.cropLeft !== null || editSettings.value.cropTop !== null
})

// 预览宽度
const previewWidth = computed(() => {
  if (!image.value) return 0
  const cropLeft = editSettings.value.cropLeft
  if (cropLeft === null) return image.value.width
  if (cropLeft > 0) return Math.min(cropLeft, image.value.width)
  return Math.min(-cropLeft, image.value.width)
})

// 预览高度
const previewHeight = computed(() => {
  if (!image.value) return 0
  const cropTop = editSettings.value.cropTop
  if (cropTop === null) return image.value.height
  if (cropTop > 0) return Math.min(cropTop, image.value.height)
  return Math.min(-cropTop, image.value.height)
})

// 裁剪容器样式
const cropContainerStyle = computed(() => {
  if (!image.value || !hasCropSettings.value) {
    return {}
  }
  
  // 根据屏幕宽度判断是否为移动端
  const isMobile = window.innerWidth < 768
  const maxWidth = isMobile ? 300 : 800
  const maxHeight = isMobile ? 225 : 600
  
  // 计算显示比例（基于原始图片的宽高比）
  const displayRatio = previewWidth.value / previewHeight.value
  
  let width, height
  if (displayRatio > maxWidth / maxHeight) {
    width = Math.min(previewWidth.value, maxWidth)
    height = width / displayRatio
  } else {
    height = Math.min(previewHeight.value, maxHeight)
    width = height * displayRatio
  }
  
  return {
    width: `${width}px`,
    height: `${height}px`,
    overflow: 'hidden' as const,
    position: 'relative' as const,
    border: '2px dashed #409EFF'
  }
})

// 裁剪图片样式
const cropImageStyle = computed(() => {
  if (!image.value || !hasCropSettings.value) {
    return {
      filter: `brightness(${100 + editSettings.value.brightness}%) 
               contrast(${100 + editSettings.value.contrast}%) 
               saturate(${100 + editSettings.value.saturation}%)`
    }
  }
  
  const cropLeft = editSettings.value.cropLeft
  const cropTop = editSettings.value.cropTop
  const originalWidth = image.value.width
  const originalHeight = image.value.height
  
  // 计算图片位置偏移
  let left = 0
  let top = 0
  
  // 计算宽度方向的偏移
  if (cropLeft !== null && cropLeft < 0) {
    // 从右到左保留，需要左移图片
    const keepWidth = Math.min(-cropLeft, originalWidth)
    left = -(originalWidth - keepWidth)
  }
  
  // 计算高度方向的偏移
  if (cropTop !== null && cropTop < 0) {
    // 从下到上保留，需要上移图片
    const keepHeight = Math.min(-cropTop, originalHeight)
    top = -(originalHeight - keepHeight)
  }
  
  // 计算缩放比例以适应容器
  const containerWidth = parseFloat(cropContainerStyle.value.width as string) || previewWidth.value
  const containerHeight = parseFloat(cropContainerStyle.value.height as string) || previewHeight.value
  const scaleX = containerWidth / previewWidth.value
  const scaleY = containerHeight / previewHeight.value
  const scale = Math.min(scaleX, scaleY)
  
  return {
    position: 'absolute' as const,
    left: `${left * scale}px`,
    top: `${top * scale}px`,
    width: `${originalWidth * scale}px`,
    height: `${originalHeight * scale}px`,
    maxWidth: 'none',
    maxHeight: 'none',
    filter: `brightness(${100 + editSettings.value.brightness}%) 
             contrast(${100 + editSettings.value.contrast}%) 
             saturate(${100 + editSettings.value.saturation}%)`
  }
})

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
    const requestData: any = {
      title: image.value.title,
      description: image.value.description,
      tags: image.value.tags
    }
    
    // 添加裁剪参数
    if (editSettings.value.cropLeft !== null) {
      requestData.cropLeft = editSettings.value.cropLeft
    }
    if (editSettings.value.cropTop !== null) {
      requestData.cropTop = editSettings.value.cropTop
    }
    
    // 添加色调参数（只有非0时才发送）
    if (editSettings.value.brightness !== 0) {
      requestData.brightness = editSettings.value.brightness
    }
    if (editSettings.value.contrast !== 0) {
      requestData.contrast = editSettings.value.contrast
    }
    if (editSettings.value.saturation !== 0) {
      requestData.saturation = editSettings.value.saturation
    }

    const response = await axios.put(`/api/images/${image.value.id}`, requestData, {
      params: {
        userId: userId.value
      }
    })

    if (response.status === 200) {
      ElMessage.success('更改已保存')
      isEditing.value = false
      
      // 重新加载图片数据
      await loadImage()
      
      // 重置编辑设置
      editSettings.value = {
        brightness: 0,
        contrast: 0,
        saturation: 0,
        cropLeft: null,
        cropTop: null
      }
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

// 应用裁剪预览
const applyCropPreview = () => {
  markAsEditing()
  // 裁剪预览通过计算属性自动更新
}

// 重置裁剪
const resetCrop = () => {
  editSettings.value.cropLeft = null
  editSettings.value.cropTop = null
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
    // 重置编辑设置
    editSettings.value = {
      brightness: 0,
      contrast: 0,
      saturation: 0,
      cropLeft: null,
      cropTop: null
    }
    applyFilters()
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
  editSettings.value.brightness = 0
  editSettings.value.contrast = 0
  editSettings.value.saturation = 0
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
  // 如果用户信息已存在，直接加载
  if (userStore.user) {
    loadImage()
  }
})

// 监听用户信息变化，确保用户信息已准备好
watch(() => userStore.user, (newUser) => {
  if (newUser) {
    loadImage()
  }
}, { immediate: true })
</script>

<style scoped>
.image-detail-container {
  padding: 24px;
  max-width: 1600px;
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

.header-actions {
  display: flex;
  gap: 12px;
}

.image-title {
  margin: 0;
  font-size: 24px;
  color: #303133;
  flex: 1;
  text-align: center;
}

/* 主要内容区域：左侧图片 + 右侧编辑工具 */
.main-content {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 24px;
  margin-bottom: 24px;
}

.image-preview-section {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 500px;
}

.crop-preview-container {
  display: inline-block;
  background: #f5f5f5;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.crop-info {
  margin-top: 12px;
  padding: 6px 16px;
  background: #409EFF;
  color: white;
  border-radius: 4px;
  font-size: 13px;
}

.main-image {
  max-width: 100%;
  max-height: 60vh;
  object-fit: contain;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.image-placeholder {
  height: 400px;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  border-radius: 8px;
  color: #999;
}

/* 右侧编辑面板 */
.editing-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.tool-card {
  background: white;
}

.tool-item {
  margin-bottom: 16px;
}

.tool-item label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  font-size: 13px;
  color: #606266;
}

.tool-item .hint {
  display: block;
  font-size: 11px;
  color: #999;
  margin-top: 4px;
}

.current-size {
  font-size: 12px;
  color: #666;
  margin-top: 8px;
}

/* 下方信息区域 */
.info-section {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.info-card {
  background: white;
}

.info-item {
  margin-bottom: 12px;
}

.info-item:last-child {
  margin-bottom: 0;
}

.info-item label {
  display: block;
  font-weight: 500;
  color: #909399;
  margin-bottom: 4px;
  font-size: 12px;
}

.info-item span,
.info-item p {
  color: #303133;
  font-size: 14px;
  margin: 0;
}

.tags-display {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

/* EXIF网格 */
.exif-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.exif-item {
  padding: 8px;
  background: #f8f9fa;
  border-radius: 6px;
}

.exif-item label {
  display: block;
  font-size: 11px;
  color: #909399;
  margin-bottom: 4px;
}

.exif-item span {
  font-size: 13px;
  color: #303133;
  font-weight: 500;
}

/* 文件信息网格 */
.file-info-grid {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

@media (max-width: 1200px) {
  .main-content {
    grid-template-columns: 1fr;
  }
  
  .info-section {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .info-section {
    grid-template-columns: 1fr;
  }
  
  .image-detail-header {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }
  
  .exif-grid {
    grid-template-columns: 1fr;
  }
}
</style>