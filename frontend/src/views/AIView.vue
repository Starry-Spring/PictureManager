<!-- src/views/AIView.vue -->
<template>
  <div class="ai-container">
    <div class="ai-header">
      <h1>AI 图片识别与分析</h1>
      <p class="subtitle">智能识别图片内容、生成描述、自动分类等</p>
    </div>

    <div class="ai-content">
      <!-- Tab切换 -->
      <el-tabs v-model="activeTab" class="ai-tabs">
        <!-- 图片分析 -->
        <el-tab-pane label="图片分析" name="analyze">
          <div class="analyze-section">
            <el-card class="image-select-card">
              <template #header>
                <div class="card-header">
                  <span>选择要分析的图片</span>
                  <el-button type="primary" size="small" @click="loadUserImages" :loading="loadingImages">
                    刷新图片列表
                  </el-button>
                </div>
              </template>
              
              <div class="image-grid" v-if="userImages.length > 0">
                <div 
                  v-for="img in userImages" 
                  :key="img.id" 
                  class="image-item"
                  :class="{ selected: selectedImage?.id === img.id }"
                  @click="selectImage(img)">
                  <img :src="getThumbnailUrl(img.id)" :alt="img.title" />
                  <div class="image-title">{{ img.title || '未命名' }}</div>
                </div>
              </div>
              <el-empty v-else description="暂无图片，请先上传图片" />
            </el-card>

            <el-card v-if="selectedImage" class="analyze-result-card">
              <template #header>
                <div class="card-header">
                  <span>分析结果</span>
                  <el-button 
                    type="primary" 
                    @click="analyzeImage" 
                    :loading="analyzing"
                    :disabled="!selectedImage">
                    <el-icon><MagicStick /></el-icon>
                    开始分析
                  </el-button>
                </div>
              </template>

              <div class="selected-image-preview">
                <img :src="getImageUrl(selectedImage.id)" :alt="selectedImage.title" />
                <div class="image-info">
                  <p><strong>标题:</strong> {{ selectedImage.title || '未命名' }}</p>
                  <p><strong>尺寸:</strong> {{ selectedImage.width }} x {{ selectedImage.height }}</p>
                </div>
              </div>

              <div v-if="analyzeResult" class="result-content">
                <el-divider>AI 分析结果</el-divider>
                
                <div v-if="analyzeResult.success" class="result-sections">
                  <div class="result-section" v-if="analyzeResult.description">
                    <h4>描述</h4>
                    <p>{{ analyzeResult.description }}</p>
                  </div>

                  <div class="result-section" v-if="analyzeResult.category">
                    <h4>分类</h4>
                    <el-tag type="primary" size="large">{{ analyzeResult.category }}</el-tag>
                  </div>

                  <div class="result-section" v-if="analyzeResult.tags?.length">
                    <h4>标签</h4>
                    <div class="tags">
                      <el-tag v-for="tag in analyzeResult.tags" :key="tag" type="info">{{ tag }}</el-tag>
                    </div>
                  </div>

                  <div class="result-section" v-if="analyzeResult.suggestedTags?.length">
                    <h4>建议标签</h4>
                    <div class="tags">
                      <el-tag 
                        v-for="tag in analyzeResult.suggestedTags" 
                        :key="tag" 
                        type="success"
                        class="suggested-tag"
                        @click="addSuggestedTag(tag)">
                        <el-icon><Plus /></el-icon>
                        {{ tag }}
                      </el-tag>
                    </div>
                  </div>
                </div>

                <el-alert v-else type="error" :title="analyzeResult.error || '分析失败'" :closable="false" />
              </div>
            </el-card>
          </div>
        </el-tab-pane>

        <!-- AI对话 -->
        <el-tab-pane label="AI 对话检索" name="chat">
          <div class="chat-section">
            <el-card class="chat-card">
              <div class="chat-messages" ref="chatContainer">
                <div v-for="(msg, index) in chatMessages" :key="index" 
                     :class="['chat-message', msg.role]">
                  <div class="message-content">
                    <div class="message-text" v-html="formatMessage(msg.content)"></div>
                    
                    <!-- 显示搜索结果 -->
                    <div v-if="msg.searchResult?.images?.length" class="search-results">
                      <p class="search-reason" v-if="msg.searchResult.reason">
                        <el-icon><InfoFilled /></el-icon>
                        {{ msg.searchResult.reason }}
                      </p>
                      <div class="result-images">
                        <div 
                          v-for="img in msg.searchResult.images" 
                          :key="img.id" 
                          class="result-image"
                          @click="viewImage(img.id)">
                          <img :src="getThumbnailUrl(img.id)" :alt="img.title" />
                          <span>{{ img.title || '未命名' }}</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
                
                <div v-if="chatLoading" class="chat-message assistant">
                  <div class="message-content">
                    <el-icon class="is-loading"><Loading /></el-icon>
                    正在思考...
                  </div>
                </div>
              </div>

              <div class="chat-input">
                <el-input 
                  v-model="chatInput" 
                  placeholder="输入消息，例如：帮我找一张风景照片" 
                  @keyup.enter="sendMessage"
                  :disabled="chatLoading">
                  <template #append>
                    <el-button type="primary" @click="sendMessage" :loading="chatLoading">
                      <el-icon><Promotion /></el-icon>
                    </el-button>
                  </template>
                </el-input>
              </div>

              <div class="chat-hints">
                <span class="hint-label">试试问:</span>
                <el-tag 
                  v-for="hint in chatHints" 
                  :key="hint" 
                  class="hint-tag"
                  @click="chatInput = hint; sendMessage()">
                  {{ hint }}
                </el-tag>
              </div>
            </el-card>
          </div>
        </el-tab-pane>

        <!-- 功能概览 -->
        <el-tab-pane label="功能说明" name="features">
          <div class="features-grid">
            <el-card class="feature-card">
              <template #header>
                <div class="feature-header">
                  <el-icon color="#409EFF" :size="32"><Camera /></el-icon>
                  <h3>物体识别</h3>
                </div>
              </template>
              <p>自动识别图片中的物体、人物、场景等</p>
              <div class="feature-status">
                <el-tag type="success">已就绪</el-tag>
              </div>
            </el-card>

            <el-card class="feature-card">
              <template #header>
                <div class="feature-header">
                  <el-icon color="#67C23A" :size="32"><Edit /></el-icon>
                  <h3>自动标签</h3>
                </div>
              </template>
              <p>根据图片内容自动生成智能标签</p>
              <div class="feature-status">
                <el-tag type="success">已就绪</el-tag>
              </div>
            </el-card>

            <el-card class="feature-card">
              <template #header>
                <div class="feature-header">
                  <el-icon color="#E6A23C" :size="32"><Document /></el-icon>
                  <h3>AI 对话检索</h3>
                </div>
              </template>
              <p>通过自然语言对话方式检索图片</p>
              <div class="feature-status">
                <el-tag type="success">已就绪</el-tag>
              </div>
            </el-card>

            <el-card class="feature-card">
              <template #header>
                <div class="feature-header">
                  <el-icon color="#F56C6C" :size="32"><Collection /></el-icon>
                  <h3>智能分类</h3>
                </div>
              </template>
              <p>自动将图片分类到不同相册</p>
              <div class="feature-status">
                <el-tag type="success">已就绪</el-tag>
              </div>
            </el-card>
          </div>

          <el-alert
            title="功能说明"
            type="info"
            :closable="false"
            class="info-alert">
            <p>AI 功能基于 DeepSeek 大模型实现。</p>
            <p>MCP 接口可供其他大模型调用，实现跨平台图片检索。</p>
          </el-alert>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/userStore'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import {
  Camera,
  Edit,
  Document,
  Collection,
  MagicStick,
  Plus,
  Promotion,
  Loading,
  InfoFilled
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('analyze')
const loadingImages = ref(false)
const analyzing = ref(false)
const userImages = ref<any[]>([])
const selectedImage = ref<any>(null)
const analyzeResult = ref<any>(null)

// 对话相关
const chatMessages = ref<any[]>([
  { role: 'assistant', content: '您好！我是您的图片管理助手。您可以问我关于图片的问题，例如"帮我找一张风景照片"或"我有多少张照片"。' }
])
const chatInput = ref('')
const chatLoading = ref(false)
const chatContainer = ref<HTMLElement | null>(null)

const chatHints = [
  '帮我找一张风景照片',
  '列出我的所有标签',
  '我有多少张照片'
]

// 图片URL缓存
const imageUrlMap = ref<Map<number, string>>(new Map())

// 获取图片URL
const getImageUrl = (imageId: number) => {
  return imageUrlMap.value.get(imageId) || ''
}

const getThumbnailUrl = (imageId: number) => {
  const cacheKey = imageId + 1000000
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

// 加载用户图片
const loadUserImages = async () => {
  loadingImages.value = true
  try {
    const userId = userStore.user?.id
    const response = await axios.get(`/api/images?userId=${userId}&size=50`)
    userImages.value = response.data.content || []
    
    // 预加载缩略图
    for (const img of userImages.value) {
      loadImageToCache(img.id, true)
    }
  } catch (error) {
    console.error('加载图片失败:', error)
    ElMessage.error('加载图片列表失败')
  } finally {
    loadingImages.value = false
  }
}

// 选择图片
const selectImage = async (img: any) => {
  selectedImage.value = img
  analyzeResult.value = null
  
  // 加载完整图片用于分析预览
  await loadImageToCache(img.id, false)
}

// 分析图片
const analyzeImage = async () => {
  if (!selectedImage.value) return
  
  analyzing.value = true
  analyzeResult.value = null
  
  try {
    const userId = userStore.user?.id
    const response = await axios.post(
      `/api/ai/analyze/${selectedImage.value.id}?userId=${userId}`
    )
    analyzeResult.value = response.data
  } catch (error: any) {
    console.error('分析失败:', error)
    analyzeResult.value = {
      success: false,
      error: error.response?.data?.message || '分析失败，请稍后重试'
    }
  } finally {
    analyzing.value = false
  }
}

// 添加建议的标签
const addSuggestedTag = async (tag: string) => {
  if (!selectedImage.value) return
  
  try {
    const userId = userStore.user?.id
    const currentTags = selectedImage.value.tags || []
    
    await axios.put(`/api/images/${selectedImage.value.id}?userId=${userId}`, {
      tags: [...currentTags, tag]
    })
    
    selectedImage.value.tags = [...currentTags, tag]
    ElMessage.success(`已添加标签: ${tag}`)
  } catch (error) {
    ElMessage.error('添加标签失败')
  }
}

// 发送消息
const sendMessage = async () => {
  if (!chatInput.value.trim() || chatLoading.value) return
  
  const message = chatInput.value.trim()
  chatInput.value = ''
  
  chatMessages.value.push({
    role: 'user',
    content: message
  })
  
  await nextTick()
  scrollToBottom()
  
  chatLoading.value = true
  
  try {
    const userId = userStore.user?.id
    const history = chatMessages.value.slice(-10).map(m => ({
      role: m.role,
      content: m.content
    }))
    
    const response = await axios.post(`/api/ai/chat?userId=${userId}`, {
      message,
      history
    })
    
    const result = response.data
    
    chatMessages.value.push({
      role: 'assistant',
      content: result.message,
      searchResult: result.hasSearch ? result.searchResult : null
    })
    
  } catch (error: any) {
    chatMessages.value.push({
      role: 'assistant',
      content: '抱歉，我遇到了一些问题。请稍后重试。'
    })
  } finally {
    chatLoading.value = false
    await nextTick()
    scrollToBottom()
  }
}

// 格式化消息
const formatMessage = (content: string) => {
  // 移除搜索标记
  content = content.replace(/\[SEARCH:[^\]]+\]/g, '')
  // 转换换行
  return content.replace(/\n/g, '<br>')
}

// 滚动到底部
const scrollToBottom = () => {
  if (chatContainer.value) {
    chatContainer.value.scrollTop = chatContainer.value.scrollHeight
  }
}

// 查看图片
const viewImage = (imageId: number) => {
  router.push(`/image/${imageId}`)
}

onMounted(() => {
  loadUserImages()
})
</script>

<style scoped>
.ai-container {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
}

.ai-header {
  text-align: center;
  margin-bottom: 32px;
}

.ai-header h1 {
  font-size: 32px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
}

.subtitle {
  font-size: 16px;
  color: #606266;
  margin: 0;
}

.ai-tabs {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

/* 图片分析部分 */
.analyze-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.image-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 12px;
  max-height: 400px;
  overflow-y: auto;
}

.image-item {
  cursor: pointer;
  border-radius: 8px;
  overflow: hidden;
  border: 2px solid transparent;
  transition: all 0.3s;
}

.image-item:hover {
  border-color: #409eff;
}

.image-item.selected {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.3);
}

.image-item img {
  width: 100%;
  height: 80px;
  object-fit: cover;
}

.image-title {
  padding: 4px 8px;
  font-size: 12px;
  color: #606266;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  background: #f5f7fa;
}

.selected-image-preview {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
}

.selected-image-preview img {
  width: 200px;
  height: 150px;
  object-fit: cover;
  border-radius: 8px;
}

.image-info {
  flex: 1;
}

.image-info p {
  margin: 8px 0;
  color: #606266;
}

.result-content {
  margin-top: 16px;
}

.result-sections {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.result-section h4 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 14px;
}

.result-section p {
  margin: 0;
  color: #606266;
  line-height: 1.6;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.suggested-tag {
  cursor: pointer;
}

.suggested-tag:hover {
  transform: scale(1.05);
}

/* 对话部分 */
.chat-section {
  max-width: 800px;
  margin: 0 auto;
}

.chat-card {
  height: 600px;
  display: flex;
  flex-direction: column;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.chat-message {
  display: flex;
}

.chat-message.user {
  justify-content: flex-end;
}

.chat-message.assistant {
  justify-content: flex-start;
}

.message-content {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 12px;
}

.chat-message.user .message-content {
  background: #409eff;
  color: white;
  border-bottom-right-radius: 4px;
}

.chat-message.assistant .message-content {
  background: #f5f7fa;
  color: #303133;
  border-bottom-left-radius: 4px;
}

.message-text {
  line-height: 1.6;
}

.search-results {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #e4e7ed;
}

.search-reason {
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.result-images {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.result-image {
  cursor: pointer;
  text-align: center;
}

.result-image img {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 6px;
  border: 2px solid transparent;
  transition: all 0.3s;
}

.result-image:hover img {
  border-color: #409eff;
}

.result-image span {
  display: block;
  font-size: 10px;
  color: #606266;
  margin-top: 4px;
  max-width: 60px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chat-input {
  padding: 16px;
  border-top: 1px solid #ebeef5;
}

.chat-hints {
  padding: 8px 16px 16px;
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}

.hint-label {
  font-size: 12px;
  color: #909399;
}

.hint-tag {
  cursor: pointer;
  font-size: 12px;
}

.hint-tag:hover {
  background: #409eff;
  color: white;
}

/* 功能概览 */
.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 24px;
  margin-bottom: 24px;
}

.feature-card {
  transition: all 0.3s;
}

.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.feature-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.feature-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.feature-card p {
  color: #606266;
  line-height: 1.6;
  margin: 16px 0;
}

.feature-status {
  margin-top: 16px;
}

.info-alert {
  margin-top: 24px;
}

.info-alert p {
  margin: 4px 0;
}

@media (max-width: 768px) {
  .analyze-section {
    grid-template-columns: 1fr;
  }
  
  .features-grid {
    grid-template-columns: 1fr;
  }
  
  .selected-image-preview {
    flex-direction: column;
  }
  
  .selected-image-preview img {
    width: 100%;
    height: auto;
  }
}
</style>
