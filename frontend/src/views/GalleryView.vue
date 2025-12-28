<!-- src/views/GalleryView.vue -->
<template>
  <div class="gallery-container">
    <!-- 搜索和上传区域 -->
    <div class="gallery-header">
      <div class="search-box">
        <el-select v-model="searchType" placeholder="搜索类型" style="width: 120px; margin-right: 10px;">
          <el-option label="全部" value="all"></el-option>
          <el-option label="标题" value="title"></el-option>
          <el-option label="描述" value="description"></el-option>
          <el-option label="标签" value="tag"></el-option>
        </el-select>
        <el-input
            v-model="searchKeyword"
            :placeholder="getSearchPlaceholder"
            clearable
            @clear="handleSearch"
            @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon>
              <Search/>
            </el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="handleSearch">
          搜索
        </el-button>
      </div>

      <div class="actions">
        <el-button type="primary" @click="openUploadDialog">
          <el-icon>
            <Plus/>
          </el-icon>
          上传图片
        </el-button>
      </div>
    </div>

    <!-- 轮播图区域 -->
    <div class="carousel-section" v-if="recentImages.length > 0">
      <h2 class="section-title">最近上传</h2>
      <el-carousel height="400px" :interval="5000" arrow="always">
        <el-carousel-item v-for="image in recentImages" :key="image.id">
          <div class="carousel-item" @click="viewImageDetail(image.id)">
            <img :src="getImageUrl(image)" :alt="image.title" class="carousel-image"/>
            <div class="carousel-overlay">
              <h3>{{ image.title }}</h3>
              <p>{{ formatDate(image.uploadedAt) }}</p>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>

    <!-- 标签过滤 -->
    <div class="tags-section" v-if="tags.length > 0">
      <h2 class="section-title">标签分类</h2>
      <div class="tags-container">
        <el-tag
            v-for="tag in tags"
            :key="tag"
            :type="activeTag === tag ? 'primary' : 'info'"
            class="tag-item"
            @click="toggleTag(tag)"
        >
          {{ tag }}
        </el-tag>
      </div>
    </div>

    <!-- 图片网格 -->
    <div class="images-section">
      <div class="images-header">
        <h2 class="section-title">
          {{ activeTag ? `标签: ${activeTag}` : '所有图片' }}
          <span class="image-count">({{ totalElements }} 张)</span>
        </h2>

        <div class="sort-options">
          <el-select v-model="sortBy" @change="loadImages" placeholder="排序方式">
            <el-option label="最新上传" value="uploadedAt"/>
            <el-option label="文件大小" value="fileSize"/>
            <el-option label="图片名称" value="title"/>
          </el-select>

          <el-select v-model="sortDirection" @change="loadImages" placeholder="排序方向">
            <el-option label="降序" value="desc"/>
            <el-option label="升序" value="asc"/>
          </el-select>
        </div>
      </div>

      <!-- 图片网格 -->
      <div class="images-grid" v-loading="loading">
        <div v-if="images.length === 0 && !loading" class="empty-state">
          <el-empty description="暂无图片">
            <el-button type="primary" @click="openUploadDialog">
              上传第一张图片
            </el-button>
          </el-empty>
        </div>

        <div
            v-for="image in images"
            :key="image.id"
            class="image-card"
            @click="viewImageDetail(image.id)"
        >
          <div class="image-container">
            <img :src="getThumbnailUrl(image)" :alt="image.title"/>
            <div class="image-overlay">
              <div class="image-actions">
                <el-button
                    type="danger"
                    size="small"
                    @click.stop="handleDelete(image.id)"
                    circle
                >
                  <el-icon>
                    <Delete/>
                  </el-icon>
                </el-button>
                <el-button
                    type="primary"
                    size="small"
                    @click.stop="editImage(image)"
                    circle
                >
                  <el-icon>
                    <Edit/>
                  </el-icon>
                </el-button>
              </div>
            </div>
          </div>
          <div class="image-info">
            <h3 class="image-title">{{ image.title }}</h3>
            <div class="image-meta">
              <span>{{ formatDate(image.uploadedAt) }}</span>
              <span>{{ formatFileSize(image.fileSize) }}</span>
            </div>
            <div class="image-tags">
              <el-tag
                  v-for="tag in image.tags"
                  :key="tag"
                  size="small"
                  type="info"
              >
                {{ tag }}
              </el-tag>
            </div>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination" v-if="totalPages > 1">
        <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[12, 24, 48, 96]"
            :total="totalElements"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 编辑图片对话框 -->
    <el-dialog
        v-model="editDialogVisible"
        title="编辑图片信息"
        width="400px"
    >
      <el-form
          ref="editFormRef"
          :model="editingImage"
          label-width="80px"
      >
        <el-form-item label="标题" prop="title">
          <el-input v-model="editingImage.title"/>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
              v-model="editingImage.description"
              type="textarea"
              :rows="3"
          />
        </el-form-item>
        <el-form-item label="标签" prop="tags">
          <el-select
              v-model="editingImage.tags"
              multiple
              filterable
              allow-create
              default-first-option
              placeholder="添加标签"
              style="width: 100%"
          >
            <el-option
                v-for="tag in tags"
                :key="tag"
                :label="tag"
                :value="tag"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveImageEdit" :loading="saving">
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import {ref, onMounted, computed, inject, watch} from 'vue'
import {useRouter, useRoute} from 'vue-router'
import {useUserStore} from '../stores/userStore'
import {ElMessage, ElMessageBox} from 'element-plus'
import {
  Search,
  Plus,
  Delete,
  Edit
} from '@element-plus/icons-vue'
import type {ImageResponseDTO} from "../types/image"
import {formatDate, formatFileSize} from '../utils/formatters'
import axios from "axios";

const router = useRouter()
const route = useRoute()  // 添加 route 引用
const userStore = useUserStore()

// 数据
const images = ref<ImageResponseDTO[]>([])
const recentImages = ref<ImageResponseDTO[]>([])
const tags = ref<string[]>([])
const loading = ref(false)
const saving = ref(false)

// 搜索和过滤
const searchKeyword = ref('')
const searchType = ref('all')  // 添加搜索类型
const activeTag = ref('')

// 分页
const currentPage = ref(1)
const pageSize = ref(24)
const totalElements = ref(0)
const totalPages = ref(0)

// 排序
const sortBy = ref('uploadedAt')
const sortDirection = ref('desc')

// 编辑
const editDialogVisible = ref(false)
const editingImage = ref<{
  id?: number
  title: string
  description: string
  tags: string[]
}>({
  title: '',
  description: '',
  tags: []
})

// 计算属性
const userId = computed(() => userStore.user?.id)

// 图片URL缓存 - 存储imageId到URL的映射
const imageUrlMap = ref<Map<number, string>>(new Map())

// 获取图片URL - 从缓存中获取
const getImageUrl = (image: ImageResponseDTO) => {
  return imageUrlMap.value.get(image.id) || ''
}

// 获取缩略图URL - 从缓存中获取
const getThumbnailUrl = (image: ImageResponseDTO) => {
  const cacheKey = image.id + 1000000
  return imageUrlMap.value.get(cacheKey) || ''
}

// 加载单张图片到缓存
const loadImageToCache = async (imageId: number, isThumbnail: boolean = false) => {
  const cacheKey = isThumbnail ? imageId + 1000000 : imageId
  
  if (imageUrlMap.value.has(cacheKey)) {
    return
  }
  
  try {
    const token = userStore.token
    const endpoint = isThumbnail ? `/api/images/${imageId}/thumbnail` : `/api/images/${imageId}/file`
    const response = await axios.get(endpoint, {
      params: { token },
      responseType: 'blob'
    })
    const blob = new Blob([response.data], { type: response.data.type })
    const url = URL.createObjectURL(blob)
    imageUrlMap.value.set(cacheKey, url)
  } catch (error) {
    console.error(`加载${isThumbnail ? '缩略' : ''}图片失败:`, error)
  }
}

// 获取搜索框提示文本
const getSearchPlaceholder = computed(() => {
  switch (searchType.value) {
    case 'title':
      return '搜索图片标题...'
    case 'description':
      return '搜索图片描述...'
    case 'tag':
      return '搜索图片标签...'
    default:
      return '搜索图片标题、描述或标签...'
  }
})

// 加载数据
const loadRecentImages = async () => {
  try {
    const response = await axios.get(`/api/images/recent`, {
      params: {
        userId: userId.value,
        limit: 10
      }
    })
    recentImages.value = response.data
    
    // 预加载轮播图片
    for (const img of recentImages.value) {
      loadImageToCache(img.id, false)
    }
  } catch (error) {
    console.error('加载最近图片失败:', error)
  }
}

const loadTags = async () => {
  try {
    const response = await axios.get(`/api/images/tags`, {
      params: {
        userId: userId.value
      }
    })
    tags.value = response.data
  } catch (error) {
    console.error('加载标签失败:', error)
  }
}

const loadImages = async () => {
  if (!userId.value) return

  loading.value = true
  try {
    const params: Record<string, any> = {
      userId: userId.value,
      page: currentPage.value - 1,
      size: pageSize.value,
      sortBy: sortBy.value,
      direction: sortDirection.value
    }

    if (searchKeyword.value) {
      params['keyword'] = encodeURIComponent(searchKeyword.value)
      params['searchType'] = searchType.value
    }
    if (activeTag.value) {
      params['tag'] = encodeURIComponent(activeTag.value)
    }

    const response = await axios.get('/api/images', { params })
    const data = response.data

    images.value = data.content
    totalElements.value = data.totalElements
    totalPages.value = data.totalPages
    
    // 预加载缩略图
    for (const img of images.value) {
      loadImageToCache(img.id, true)
    }
  } catch (error) {
    console.error('加载图片失败:', error)
    ElMessage.error('加载图片失败')
  } finally {
    loading.value = false
  }
}

// 事件处理
const handleSearch = () => {
  currentPage.value = 1
  loadImages()
}

const toggleTag = (tag: string) => {
  activeTag.value = activeTag.value === tag ? '' : tag
  currentPage.value = 1
  loadImages()
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  loadImages()
}

const handleCurrentChange = (page: number) => {
  currentPage.value = page
  loadImages()
}

const viewImageDetail = (imageId: number) => {
  router.push(`/image/${imageId}`)
}

// 注入父组件提供的方法
const openUploadDialog = inject('openUploadDialog') as () => void

const editImage = (image: ImageResponseDTO) => {
  editingImage.value = {
    id: image.id,
    title: image.title || '',
    description: image.description || '',
    tags: Array.from(image.tags || [])
  }
  editDialogVisible.value = true
}

const saveImageEdit = async () => {
  if (!editingImage.value.id) return

  saving.value = true
  try {
    const response = await axios.put(`/api/images/${editingImage.value.id}`, {
      title: editingImage.value.title,
      description: editingImage.value.description,
      tags: editingImage.value.tags
    }, {
      params: {
        userId: userId.value
      }
    })

    if (response.status === 200) {
      ElMessage.success('图片信息已更新')
      editDialogVisible.value = false
      loadImages()
      loadRecentImages()
    } else {
      ElMessage.error('更新失败')
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '更新失败')
  } finally {
    saving.value = false
  }
}

const handleDelete = async (imageId: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这张图片吗？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })

    const response = await axios.delete(`/api/images/${imageId}`, {
      params: {
        userId: userId.value
      }
    })

    if (response.status === 200) {
      ElMessage.success('图片已删除')
      loadImages()
      loadRecentImages()
    } else {
      ElMessage.error('删除失败')
    }
  } catch (error: any) {
    // 用户取消删除或发生错误
    if (error !== 'cancel' && error.response) {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
}

// 初始化时从URL参数中获取搜索关键词
onMounted(() => {
  // 从路由参数中获取搜索关键词
  if (route.query.keyword) {
    searchKeyword.value = route.query.keyword as string
  }
  loadRecentImages()
  loadTags()
  loadImages()
})

// 监听路由参数变化
watch(() => route.query.keyword, (newKeyword) => {
  if (newKeyword !== searchKeyword.value) {
    searchKeyword.value = newKeyword as string
    currentPage.value = 1  // 重置到第一页
    loadImages()
  }
})
</script>

<style scoped>
.gallery-container {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
}

.gallery-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
  gap: 16px;
}

.search-box {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  max-width: 400px;
}

.actions {
  display: flex;
  gap: 12px;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 16px 0;
}

.image-count {
  font-size: 14px;
  color: #909399;
  font-weight: normal;
  margin-left: 8px;
}

.carousel-section {
  margin-bottom: 48px;
}

.carousel-item {
  position: relative;
  height: 100%;
  cursor: pointer;
  overflow: hidden;
  border-radius: 8px;
}

.carousel-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.carousel-item:hover .carousel-image {
  transform: scale(1.05);
}

.carousel-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.7));
  color: white;
  padding: 24px;
}

.carousel-overlay h3 {
  margin: 0 0 8px 0;
  font-size: 24px;
}

.carousel-overlay p {
  margin: 0;
  opacity: 0.9;
}

.tags-section {
  margin-bottom: 32px;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-item {
  cursor: pointer;
  transition: all 0.3s;
}

.tag-item:hover {
  transform: translateY(-2px);
}

.images-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.sort-options {
  display: flex;
  gap: 12px;
}

.images-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
  margin-bottom: 32px;
}

.empty-state {
  grid-column: 1 / -1;
  text-align: center;
  padding: 60px 0;
}

.image-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
  cursor: pointer;
}

.image-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.image-container {
  position: relative;
  aspect-ratio: 4/3;
  overflow: hidden;
}

.image-container img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.image-card:hover .image-container img {
  transform: scale(1.05);
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  opacity: 0;
  transition: opacity 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.image-card:hover .image-overlay {
  opacity: 1;
}

.image-actions {
  display: flex;
  gap: 8px;
}

.image-info {
  padding: 16px;
}

.image-title {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.image-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
  margin-bottom: 12px;
}

.image-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.pagination {
  display: flex;
  justify-content: center;
  padding: 24px 0;
}

@media (max-width: 768px) {
  .gallery-header {
    flex-direction: column;
    align-items: stretch;
  }

  .search-box {
    max-width: none;
  }

  .images-grid {
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 16px;
  }
}
</style>