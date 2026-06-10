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
          <div class="w-full">
            <div class="flex items-center gap-2 mb-2">
              <el-button type="success" size="small" @click="handleAiTitleOptimize" :loading="aiLoading" :disabled="!formData.name && !formData.introduction">
                AI优化标题/简介
              </el-button>
            </div>
            <el-input
              v-model="formData.introduction"
              type="textarea"
              :rows="5"
              placeholder="请输入小说简介"
              maxlength="2000"
              show-word-limit
            />
          </div>
        </el-form-item>

        <!-- 写作风格总结 -->
        <el-form-item label="写作风格" v-if="isEdit && novelId">
          <div class="w-full">
            <div class="flex items-center gap-2 mb-2">
              <el-button
                type="info"
                size="small"
                @click="handleRefreshStyle"
                :loading="styleLoading"
              >
                提炼写作风格
              </el-button>
              <el-button
                v-if="!styleEditing && styleExists"
                type="warning"
                size="small"
                @click="startEditStyle"
              >
                编辑风格
              </el-button>
              <el-button
                v-if="styleEditing"
                type="primary"
                size="small"
                @click="saveEditedStyle"
                :loading="styleSaving"
              >
                保存
              </el-button>
              <el-button
                v-if="styleEditing"
                size="small"
                @click="cancelEditStyle"
              >
                取消
              </el-button>
              <span v-if="styleExists && !styleEditing" class="text-xs text-gray-400 ml-2">
                已提炼至第 {{ lastSummarizedChapter }} 章 | v{{ styleVersion }}
              </span>
            </div>
            <div v-if="styleEditing">
              <el-input
                v-model="editableStyleText"
                type="textarea"
                :rows="4"
                placeholder="编辑写作风格描述..."
                maxlength="500"
                show-word-limit
              />
            </div>
            <div v-else-if="styleExists" class="text-sm text-gray-600 dark:text-gray-400 bg-gray-50 dark:bg-gray-700/50 rounded-lg p-3 leading-relaxed">
              {{ styleText || '暂无风格描述' }}
            </div>
            <div v-else class="text-xs text-gray-400">
              点击"提炼写作风格"让AI根据已发布章节分析小说的写作风格特征
            </div>
          </div>
        </el-form-item>

        <el-form-item label="是否完结" prop="isFinished">
          <el-switch v-model="formData.isFinished" :active-value="true" :inactive-value="false" />
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

    <!-- AI写作助手结果弹窗 -->
    <el-dialog
      v-model="aiDialogVisible"
      :title="aiDialogTitle"
      width="600px"
      :close-on-click-modal="false"
    >
      <div class="ai-result-content">
        <div v-if="aiLoading" class="flex items-center justify-center py-8">
          <el-icon class="is-loading mr-2" :size="20"><Loading /></el-icon>
          <span class="text-gray-500">AI正在思考中...</span>
        </div>
        <div v-else>
          <pre class="whitespace-pre-wrap text-sm text-gray-700 dark:text-gray-300 leading-relaxed bg-gray-50 dark:bg-gray-700/50 rounded-lg p-4">{{ aiResult }}</pre>
          <div v-if="aiResultType === 4 && titleOptions.length > 0" class="mt-4 space-y-3">
            <div>
              <p class="text-sm font-medium text-gray-600 mb-1">备选标题：</p>
              <div class="space-y-2">
                <div v-for="(title, index) in titleOptions" :key="index" class="flex items-center gap-2">
                  <el-input :model-value="title" size="small" class="flex-1" readonly />
                  <el-button type="primary" size="small" @click="applyTitle(title)">应用</el-button>
                </div>
              </div>
            </div>
            <div v-if="optimizedIntroduction">
              <p class="text-sm font-medium text-gray-600 mb-1">优化后的简介：</p>
              <div class="flex items-start gap-2">
                <el-input v-model="optimizedIntroduction" type="textarea" :rows="4" size="small" class="flex-1" />
                <el-button type="primary" size="small" @click="applyOptimizedIntro" class="mt-1">应用</el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="aiDialogVisible = false">关闭</el-button>
        <el-button v-if="aiResult && !aiLoading" type="primary" @click="copyAiResult">复制结果</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Loading } from '@element-plus/icons-vue'
import { addNovel, updateNovel, getNovelDetail, uploadFile, getNovelCategory, setNovelCategory, getAllCategories, aiWritingAssist, getStyleSummary, refreshStyleSummary, updateStyleSummary } from '@/api'
import { FileUploadType } from '@/enums'
import { AiWritingType } from '@/types'
import { validateImageFile } from '@/utils/file-validator'
import { getImageUrl } from '@/utils/file-url'
import CategoryDrawer from '@/components/business/CategoryDrawer.vue'
import type { FormInstance, FormRules, UploadRequestOptions } from 'element-plus'
import type { NovelCategoryVO } from '@/types'

const router = useRouter()
const route = useRoute()

const isEdit = computed(() => !!route.params.id)
const novelId = computed(() => route.params.id ? Number(route.params.id) : 0)

const formRef = ref<FormInstance>()
const submitting = ref(false)
const showCategoryDrawer = ref(false)
const selectedCategoryIds = ref<number[]>([])
const selectedCategories = ref<NovelCategoryVO[]>([])
const allCategories = ref<NovelCategoryVO[]>([])

// 写作风格总结
const styleText = ref('')
const styleLoading = ref(false)
const styleSaving = ref(false)
const styleEditing = ref(false)
const styleExists = ref(false)
const lastSummarizedChapter = ref(0)
const styleVersion = ref(0)
const editableStyleText = ref('')

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

// AI写作助手
const aiLoading = ref(false)
const aiDialogVisible = ref(false)
const aiDialogTitle = ref('')
const aiResult = ref('')
const aiResultType = ref(0)
const titleOptions = ref<string[]>([])
const optimizedIntroduction = ref('')

const handleAiTitleOptimize = async () => {
  if (!formData.name && !formData.introduction) return
  aiLoading.value = true
  aiDialogVisible.value = true
  aiDialogTitle.value = 'AI标题/简介优化'
  aiResult.value = ''
  aiResultType.value = AiWritingType.TITLE_OPTIMIZE
  titleOptions.value = []
  optimizedIntroduction.value = ''
  try {
    const res = await aiWritingAssist({
      type: AiWritingType.TITLE_OPTIMIZE,
      title: formData.name,
      introduction: formData.introduction
    })
    aiResult.value = res.data?.result || '暂无优化建议'
    titleOptions.value = res.data?.titleOptions || []
    optimizedIntroduction.value = res.data?.optimizedIntroduction || ''
  } catch (error: any) {
    aiResult.value = '获取优化建议失败，请稍后重试'
  } finally {
    aiLoading.value = false
  }
}

const applyTitle = (title: string) => {
  formData.name = title
  ElMessage.success('已应用标题')
}

const applyOptimizedIntro = () => {
  if (optimizedIntroduction.value) {
    formData.introduction = optimizedIntroduction.value
    ElMessage.success('已应用优化简介')
  }
}

const copyAiResult = () => {
  if (aiResult.value) {
    navigator.clipboard.writeText(aiResult.value).then(() => {
      ElMessage.success('已复制到剪贴板')
    }).catch(() => {
      ElMessage.error('复制失败')
    })
  }
}

// 写作风格总结
const fetchStyleSummary = async () => {
  try {
    const res = await getStyleSummary(novelId.value)
    if (res.data) {
      styleText.value = res.data.style_text || ''
      styleExists.value = res.data.exists
      lastSummarizedChapter.value = res.data.last_summarized_chapter || 0
      styleVersion.value = res.data.version || 0
    }
  } catch {
    // Silently fail on style summary fetch
  }
}

const handleRefreshStyle = async () => {
  styleLoading.value = true
  try {
    await refreshStyleSummary(novelId.value)
    ElMessage.success('写作风格提炼完成')
    await fetchStyleSummary()
  } catch {
    ElMessage.error('风格提炼失败，请稍后重试')
  } finally {
    styleLoading.value = false
  }
}

const startEditStyle = () => {
  editableStyleText.value = styleText.value
  styleEditing.value = true
}

const cancelEditStyle = () => {
  styleEditing.value = false
  editableStyleText.value = ''
}

const saveEditedStyle = async () => {
  styleSaving.value = true
  try {
    await updateStyleSummary(novelId.value, editableStyleText.value)
    styleText.value = editableStyleText.value
    styleEditing.value = false
    ElMessage.success('风格描述已保存')
  } catch {
    ElMessage.error('保存失败')
  } finally {
    styleSaving.value = false
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
    await fetchStyleSummary()
  }
})
</script>
