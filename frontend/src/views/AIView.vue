<!-- src/views/AIView.vue -->
<template>
  <div class="ai-container">
    <div class="ai-header">
      <h1>AI 图片识别与分析</h1>
      <p class="subtitle">智能识别图片内容、生成描述、自动分类等</p>
    </div>

    <div class="ai-content">
      <!-- 功能概览 -->
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
              <h3>描述生成</h3>
            </div>
          </template>
          <p>自动生成图片的详细描述文字</p>
          <div class="feature-status">
            <el-tag type="warning">开发中</el-tag>
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
            <el-tag type="info">计划中</el-tag>
          </div>
        </el-card>
      </div>

      <!-- 使用说明 -->
      <el-card class="instruction-card">
        <template #header>
          <h3>如何使用 AI 功能</h3>
        </template>
        <div class="instructions">
          <el-steps direction="vertical" :active="4">
            <el-step title="选择图片" description="在图片库中选择要分析的图片" />
            <el-step title="点击分析" description="在图片详情页点击 AI 分析按钮" />
            <el-step title="等待结果" description="AI 模型会分析图片内容" />
            <el-step title="查看结果" description="查看生成的标签、描述和分类" />
          </el-steps>
        </div>
      </el-card>

      <!-- 示例展示 -->
      <el-card class="demo-card">
        <template #header>
          <h3>示例展示</h3>
        </template>
        <div class="demo-content">
          <div class="demo-image">
            <img src="https://via.placeholder.com/400x300" alt="示例图片" />
          </div>
          <div class="demo-results">
            <h4>AI 分析结果</h4>
            <div class="result-section">
              <h5>识别到的物体:</h5>
              <div class="tags">
                <el-tag v-for="tag in demoTags" :key="tag" type="info">{{ tag }}</el-tag>
              </div>
            </div>
            <div class="result-section">
              <h5>生成的描述:</h5>
              <p class="description">这是一张美丽的风景照片，画面中有蓝天白云、绿色的山脉和清澈的湖水...</p>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 功能说明 -->
      <el-alert
          title="功能说明"
          type="info"
          :closable="false"
          class="info-alert"
      >
        <p>AI 功能基于深度学习模型实现，可能需要一些时间处理图片。</p>
        <p>部分功能需要联网使用，请确保网络连接正常。</p>
      </el-alert>
    </div>

    <!-- 底部提示 -->
    <div class="ai-footer">
      <el-button type="primary" size="large" @click="goToGallery">
        <el-icon><Picture /></el-icon>
        开始使用 AI 分析
      </el-button>
      <p class="footer-note">前往图片库选择要分析的图片</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import {
  Camera,
  Edit,
  Document,
  Collection,
  Picture
} from '@element-plus/icons-vue'

const router = useRouter()

const demoTags = ['风景', '山脉', '湖泊', '蓝天', '白云', '自然']

const goToGallery = () => {
  router.push('/gallery')
}
</script>

<style scoped>
.ai-container {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.ai-header {
  text-align: center;
  margin-bottom: 48px;
}

.ai-header h1 {
  font-size: 36px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 12px 0;
}

.subtitle {
  font-size: 18px;
  color: #606266;
  margin: 0;
}

.ai-content {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 24px;
}

.feature-card {
  transition: all 0.3s;
  height: 100%;
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

.instruction-card,
.demo-card {
  margin-top: 24px;
}

.instruction-card h3,
.demo-card h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.instructions {
  padding: 20px;
}

.demo-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 32px;
  padding: 20px;
}

.demo-image img {
  width: 100%;
  height: auto;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.demo-results h4 {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 20px 0;
}

.result-section {
  margin-bottom: 24px;
}

.result-section h5 {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 12px 0;
  color: #303133;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.description {
  color: #606266;
  line-height: 1.6;
  margin: 0;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 6px;
}

.info-alert {
  margin-top: 32px;
}

.info-alert p {
  margin: 4px 0;
}

.ai-footer {
  text-align: center;
  margin-top: 48px;
  padding: 32px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.footer-note {
  color: #909399;
  margin: 16px 0 0 0;
  font-size: 14px;
}

@media (max-width: 768px) {
  .ai-header h1 {
    font-size: 28px;
  }

  .subtitle {
    font-size: 16px;
  }

  .features-grid {
    grid-template-columns: 1fr;
  }

  .demo-content {
    grid-template-columns: 1fr;
    gap: 24px;
  }
}
</style>