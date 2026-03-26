<template>
  <div class="category-page">
    <!-- 顶部导航区 -->
    <section class="bg-white dark:bg-gray-800 border-b border-gray-100 dark:border-gray-700 sticky top-0 z-10">
      <div class="container mx-auto px-4 py-4">
        <CategoryNav 
          :categories="categories"
          :active-category="activeCategory"
          :show-channel="true"
          @change="handleCategoryChange"
        />
      </div>
    </section>

    <div class="container mx-auto px-4 py-6">
      <div class="flex gap-6">
        <!-- 左侧筛选栏 -->
        <aside class="hidden lg:block w-56 flex-shrink-0">
          <div class="sticky top-24 space-y-6">
            <!-- 状态筛选 -->
            <div class="bg-white dark:bg-gray-800 rounded-2xl shadow-card p-4">
              <h3 class="font-bold text-gray-800 dark:text-gray-200 mb-3 flex items-center gap-2">
                <el-icon class="text-primary" :size="16"><Filter /></el-icon>
                状态筛选
              </h3>
              <div class="space-y-2">
                <button 
                  v-for="option in statusOptions" 
                  :key="option.value"
                  class="w-full px-3 py-2 rounded-xl text-left text-sm transition-colors"
                  :class="filterStatus === option.value 
                    ? 'bg-primary/10 text-primary font-medium' 
                    : 'text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700'"
                  @click="handleStatusChange(option.value)"
                >
                  {{ option.label }}
                </button>
              </div>
            </div>

            <!-- 排序方式 -->
            <div class="bg-white dark:bg-gray-800 rounded-2xl shadow-card p-4">
              <h3 class="font-bold text-gray-800 dark:text-gray-200 mb-3 flex items-center gap-2">
                <el-icon class="text-primary" :size="16"><Sort /></el-icon>
                排序方式
              </h3>
              <div class="space-y-2">
                <button 
                  v-for="option in sortOptions" 
                  :key="option.value"
                  class="w-full px-3 py-2 rounded-xl text-left text-sm transition-colors"
                  :class="sortBy === option.value 
                    ? 'bg-primary/10 text-primary font-medium' 
                    : 'text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700'"
                  @click="handleSortChange(option.value)"
                >
                  {{ option.label }}
                </button>
              </div>
            </div>

            <!-- 统计信息 -->
            <div class="bg-gradient-primary rounded-2xl p-4 text-white">
              <p class="text-sm opacity-80">当前分类</p>
              <p class="text-2xl font-bold mt-1">{{ total }}</p>
              <p class="text-sm opacity-80">部作品</p>
            </div>
          </div>
        </aside>

        <!-- 主内容区 -->
        <main class="flex-1 min-w-0">
          <!-- 移动端筛选 -->
          <div class="lg:hidden mb-4 flex flex-wrap gap-2">
            <el-select v-model="filterStatus" placeholder="状态" size="small" class="w-28" @change="handleFilterChange">
              <el-option v-for="opt in statusOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
            </el-select>
            <el-select v-model="sortBy" placeholder="排序" size="small" class="w-28" @change="handleFilterChange">
              <el-option v-for="opt in sortOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
            </el-select>
          </div>

          <!-- 加载状态 -->
          <div v-if="loading" class="flex justify-center py-16">
            <el-icon class="is-loading" :size="40"><Loading /></el-icon>
          </div>

          <!-- 空状态 -->
          <div v-else-if="novels.length === 0" class="text-center py-16">
            <el-icon :size="60" class="text-gray-300 dark:text-gray-600 mb-4"><DocumentRemove /></el-icon>
            <p class="text-gray-400">暂无小说</p>
            <p class="text-sm text-gray-300 mt-2">换个分类或筛选条件试试</p>
          </div>

          <!-- 小说网格 -->
          <template v-else>
            <div class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-4 xl:grid-cols-5 gap-4">
              <NovelCard 
                v-for="novel in novels" 
                :key="novel.id"
                :novel="novel"
                :show-new-tag="sortBy === 'update'"
                @click="handleNovelClick"
              />
            </div>
            
            <!-- 分页 -->
            <div class="mt-8 flex justify-center">
              <el-pagination
                v-model:current-page="pageNum"
                v-model:page-size="pageSize"
                :total="total"
                :page-sizes="[12, 24, 48]"
                layout="total, sizes, prev, pager, next"
                background
                @size-change="fetchNovels"
                @current-change="fetchNovels"
              />
            </div>
          </template>
        </main>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Loading, Filter, Sort, DocumentRemove } from '@element-plus/icons-vue'
import { getNovelsByCategory, searchNovels, getAllCategories } from '@/api'
import NovelCard from '@/components/business/novel-card.vue'
import CategoryNav from '@/components/business/category-nav.vue'
import type { NovelListVO, NovelCategoryVO } from '@/types'

const router = useRouter()
const route = useRoute()

const loading = ref(true)
const novels = ref<NovelListVO[]>([])
const categories = ref<NovelCategoryVO[]>([])
const activeCategory = ref(0)
const filterStatus = ref('')
const sortBy = ref('update')
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)

const statusOptions = [
  { label: '全部', value: '' },
  { label: '连载中', value: 'false' },
  { label: '已完结', value: 'true' }
]

const sortOptions = [
  { label: '最新更新', value: 'update' },
  { label: '收藏最多', value: 'collect' },
  { label: '字数最多', value: 'word' }
]

const fetchCategories = async () => {
  try {
    const res = await getAllCategories()
    categories.value = res.data || []
  } catch (error) {
    console.error('Failed to fetch categories:', error)
  }
}

const fetchNovels = async () => {
  loading.value = true
  try {
    const params: any = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      sortBy: sortBy.value,
      isFinished: filterStatus.value === 'true' ? true : filterStatus.value === 'false' ? false : undefined
    }
    
    if (activeCategory.value) {
      params.categoryId = activeCategory.value
      const res = await getNovelsByCategory(activeCategory.value, params)
      novels.value = res.data?.list || []
      total.value = res.data?.total || 0
    } else {
      const res = await searchNovels(params)
      novels.value = res.data?.list || []
      total.value = res.data?.total || 0
    }
  } catch (error) {
    console.error('Failed to fetch novels:', error)
  } finally {
    loading.value = false
  }
}

const handleCategoryChange = (categoryId: number) => {
  activeCategory.value = categoryId
  pageNum.value = 1
  fetchNovels()
}

const handleStatusChange = (value: string) => {
  filterStatus.value = value
  pageNum.value = 1
  fetchNovels()
}

const handleSortChange = (value: string) => {
  sortBy.value = value
  pageNum.value = 1
  fetchNovels()
}

const handleFilterChange = () => {
  pageNum.value = 1
  fetchNovels()
}

const handleNovelClick = (novel: NovelListVO) => {
  router.push(`/novel/${novel.id}`)
}

// 监听路由参数变化
watch(() => route.params.id, (newId) => {
  if (newId) {
    activeCategory.value = Number(newId)
    fetchNovels()
  }
})

onMounted(() => {
  if (route.params.id) {
    activeCategory.value = Number(route.params.id)
  }
  fetchCategories()
  fetchNovels()
})
</script>