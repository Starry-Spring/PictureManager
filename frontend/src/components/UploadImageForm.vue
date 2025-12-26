<template>
  <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
    <el-form-item label="图片文件" prop="file">
      <el-upload
        class="upload-demo"
        drag
        action="#"
        :auto-upload="false"
        :limit="1"
        :on-change="handleFileChange"
        :on-remove="handleFileRemove"
        list-type="picture"
        :file-list="fileList"
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          拖拽文件到此处或 <em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            只能上传 jpg/png/gif 文件，且不超过 100MB
          </div>
        </template>
      </el-upload>
    </el-form-item>

    <el-form-item label="标题" prop="title">
      <el-input v-model="form.title" placeholder="请输入图片标题" />
    </el-form-item>

    <el-form-item label="描述" prop="description">
      <el-input
        v-model="form.description"
        type="textarea"
        placeholder="请输入图片描述"
        :rows="3"
      />
    </el-form-item>

    <el-form-item label="标签" prop="tags">
      <el-select
        v-model="form.tags"
        multiple
        filterable
        allow-create
        default-first-option
        placeholder="请选择或输入标签"
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

    <div class="form-actions">
      <el-button @click="$emit('cancel')">取消</el-button>
      <el-button type="primary" :loading="uploading" @click="submitUpload">
        开始上传
      </el-button>
    </div>
  </el-form>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { UploadFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules, UploadFile, UploadUserFile } from 'element-plus'
import { useUserStore } from '@/stores/userStore'
import axios from 'axios'

const emit = defineEmits(['success', 'cancel'])
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const uploading = ref(false)
const fileList = ref<UploadUserFile[]>([])
const availableTags = ref<string[]>([])

const form = reactive({
  file: null as File | null,
  title: '',
  description: '',
  tags: [] as string[]
})

// 自定义验证规则：检查文件是否存在
const validateFile = (rule: any, value: any, callback: any) => {
  if (!form.file) {
    callback(new Error('请选择图片文件'))
  } else {
    callback()
  }
}

const rules = reactive<FormRules>({
  title: [
    { required: true, message: '请输入图片标题', trigger: 'blur' },
    { min: 1, max: 100, message: '长度在 1 到 100 个字符', trigger: 'blur' }
  ],
  file: [
    { validator: validateFile, trigger: 'change' }
  ]
})

const userId = computed(() => userStore.user?.id)

const handleFileChange = (uploadFile: UploadFile) => {
  form.file = uploadFile.raw || null

  // 自动填充标题（如果标题为空）
  if (form.title === '' && uploadFile.name) {
    const name = uploadFile.name.substring(0, uploadFile.name.lastIndexOf('.'))
    form.title = name
  }

  // 触发表单验证
  if (formRef.value) {
    formRef.value.validateField('file')
  }
}

const handleFileRemove = () => {
  form.file = null
  // 触发表单验证
  if (formRef.value) {
    formRef.value.validateField('file')
  }
}

const loadTags = async () => {
  if (!userId.value) return
  try {
    const response = await axios.get(`/api/images/tags`, {
      params: { userId: userId.value }
    })
    availableTags.value = response.data
  } catch (error) {
    console.error('加载标签失败:', error)
  }
}

const submitUpload = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      if (!form.file) {
        ElMessage.warning('请选择文件')
        return
      }

      uploading.value = true
      try {
        const formData = new FormData()
        formData.append('file', form.file)
        formData.append('title', form.title)
        formData.append('description', form.description)

        // 处理标签
        form.tags.forEach(tag => {
          formData.append('tags', tag)
        })

        if (userId.value) {
          formData.append('userId', userId.value.toString())
        }

        await axios.post('/api/images/upload', formData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        })

        ElMessage.success('上传成功')
        emit('success')
      } catch (error: any) {
        console.error('上传失败:', error)
        ElMessage.error(error.response?.data?.message || '上传失败')
      } finally {
        uploading.value = false
      }
    }
  })
}

onMounted(() => {
  loadTags()
})
</script>

<style scoped>
.upload-demo {
  width: 100%;
}
.form-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  gap: 12px;
}
</style>