<template>
  <div class="novel-edit-page">
    <h1 class="text-2xl font-bold text-gray-800 dark:text-gray-200 mb-6">{{ isEdit ? '编辑小说' : '新建小说' }}</h1>
    
    <div class="bg-white dark:bg-gray-800 rounded-lg p-6">
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
        <el-form-item label="小说名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入小说名称" maxlength="100" />
        </el-form-item>
        
        <el-form-item label="副标题" prop="subName">
          <el-input v-model="formData.subName" placeholder="请输入副标题（可选）" maxlength="100" />
        </el-form-item>
        
        <el-form-item label="封面图片" prop="url">
          <div class="flex items-start gap-4">
            <img 
              :src="getImageUrl(formData.url)" 
              class="w-32 h-44 object-cover rounded border"
            />
            <div>
              <el-upload
                :show-file-list="false"
                :before-upload="beforeUpload"
                :http-request="handleUpload"
                accept=".jpg,.jpeg,.png,.gif"
              >
                <el-button>上传封面</el-button>
              </el-upload>
              <p class="text-xs text-gray-400 mt-2">支持 JPG、PNG、GIF 格式，建议尺寸 300x400</p>
            </div>
          </div>
        </el-form-item>
        
        <el-form-item label="小说标签" prop="tags">
          <el-input v-model="formData.tags" placeholder="多个标签用英文逗号分隔，如：玄幻,热血,穿越" />
        </el-form-item>
        
        <el-form-item label="小说分类">
          <div class="flex items-center gap-2">
            <el-tag 
              v-for="cat in selectedCategories" 
              :key="cat.id"
              closable
              @close="removeCategory(cat.id)"
            >
              {{ cat.type }}
            </el-tag>
            <el-button type="primary" link @click="showCategoryDrawer = true">
              <el-icon class="mr-1"><Plus /></el-icon>
              选择分类
            </el-button>
          </div>
        </el-form-item>
        
        <el-form-item label="小说简介" prop="introduction">
          <el-input 
            v-model="formData.introduction" 
            type="textarea" 
            :rows="5"
            placeholder="请输入小说简介"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="是否完结" prop="isFinished">
          <el-switch v-model="formData.isFinished" />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">
            {{ isEdit ? '保存' : '创建' }}
          </el-button>
          <el-button @click="router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 分类选择抽屉 -->
    <CategoryDrawer 
      v-model="showCategoryDrawer"
      :selected-category-ids="selectedCategoryIds"
      @confirm="handleCategoryConfirm"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { addNovel, updateNovel, getNovelDetail, uploadFile, getNovelCategory, setNovelCategory, getAllCategories } from '@/api'
import { FileUploadType } from '@/enums'
import { validateImageFile } from '@/utils/file-validator'
import { getImageUrl } from '@/utils/file-url'
import CategoryDrawer from '@/components/business/CategoryDrawer.vue'
import type { FormInstance, FormRules, UploadRequestOptions } from 'element-plus'
import type { NovelCategoryVO } from '@/types'

const router = useRouter()
const route = useRoute()

const isEdit = computed(() => !!route.params.id)

const formRef = ref<FormInstance>()
const submitting = ref(false)
const showCategoryDrawer = ref(false)
const selectedCategoryIds = ref<number[]>([])
const selectedCategories = ref<NovelCategoryVO[]>([])
const allCategories = ref<NovelCategoryVO[]>([])

const formData = reactive({
  id: undefined as number | undefined,
  name: '',
  subName: '',
  url: '',
  tags: '',
  introduction: '',
  isFinished: false
})

const rules: FormRules = {
  name: [
    { required: true, message: '请输入小说名称', trigger: 'blur' },
    { max: 100, message: '名称不能超过100个字符', trigger: 'blur' }
  ]
}

const beforeUpload = (file: File) => {
  // 小说封面使用10MB限制
  const error = validateImageFile(file)
  if (error) {
    ElMessage.error(error)
    return false
  }
  return true
}

const handleUpload = async (options: UploadRequestOptions) => {
  try {
    const res = await uploadFile(options.file as File, FileUploadType.NOVEL_COVER)
    formData.url = res.data
    ElMessage.success('上传成功')
  } catch (error) {
    ElMessage.error('上传失败')
  }
}

const fetchAllCategories = async () => {
  try {
    const res = await getAllCategories()
    allCategories.value = res.data || []
  } catch (error) {
    console.error('Failed to fetch categories:', error)
  }
}

const fetchNovelCategory = async (novelId: number) => {
  try {
    const res = await getNovelCategory(novelId)
    const categories = res.data || []
    selectedCategoryIds.value = categories.map((c: NovelCategoryVO) => c.id)
    selectedCategories.value = categories
  } catch (error) {
    console.error('Failed to fetch novel category:', error)
  }
}

const fetchNovel = async () => {
  if (!route.params.id) return
  try {
    const res = await getNovelDetail(Number(route.params.id))
    const novel = res.data
    formData.id = novel.id
    formData.name = novel.name
    formData.subName = novel.subName || ''
    formData.url = novel.url || ''
    formData.tags = novel.tags || ''
    formData.introduction = novel.introduction || ''
    formData.isFinished = novel.isFinished
    
    // 获取小说分类
    await fetchNovelCategory(novel.id)
  } catch (error) {
    console.error('Failed to fetch novel:', error)
  }
}

const handleCategoryConfirm = (categoryIds: number[]) => {
  selectedCategoryIds.value = categoryIds
  // 更新显示的分类列表
  selectedCategories.value = allCategories.value.filter(c => categoryIds.includes(c.id))
}

const removeCategory = (categoryId: number) => {
  const index = selectedCategoryIds.value.indexOf(categoryId)
  if (index > -1) {
    selectedCategoryIds.value.splice(index, 1)
    selectedCategories.value = selectedCategories.value.filter(c => c.id !== categoryId)
  }
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  
  submitting.value = true
  try {
    let novelId: number
    
    if (isEdit.value) {
      await updateNovel(formData as any)
      novelId = formData.id!
      ElMessage.success('保存成功')
    } else {
      const res = await addNovel(formData as any)
      novelId = res.data
      ElMessage.success('创建成功')
    }
    
    // 设置小说分类
    if (selectedCategoryIds.value.length > 0) {
      await setNovelCategory({
        novelId,
        categoryIds: selectedCategoryIds.value
      })
    }
    
    router.push('/author/novels')
  } catch (error) {
    console.error('Failed to save novel:', error)
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  await fetchAllCategories()
  if (isEdit.value) {
    await fetchNovel()
  }
})
</script>
