<template>
  <div class="search-page">
    <!-- 搜索头部 -->
    <section class="bg-white dark:bg-gray-800 border-b border-gray-100 dark:border-gray-700">
      <div class="container mx-auto px-4 py-6">
        <div class="max-w-2xl mx-auto">
          <!-- 搜索框 -->
          <div class="relative">
            <input 
              v-model="keyword"
              type="text"
              placeholder="搜索小说、作者..."
              class="w-full px-6 py-4 pr-28 rounded-2xl bg-gray-100 dark:bg-gray-700 text-gray-800 dark:text-gray-200 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-primary/50 transition-all"
              @keyup.enter="handleSearch"
              @input="fetchSuggestions(keyword)"
              @blur="hideSuggestions"
            />
            <button 
              class="absolute right-2 top-1/2 -translate-y-1/2 px-6 py-2 rounded-xl bg-gradient-primary text-white font-medium hover:shadow-lg transition-shadow"
              @click="handleSearch"
            >
              搜索
            </button>
            <!-- 搜索建议下拉 -->
            <div 
              v-if="showSuggestions"
              class="absolute left-0 right-0 top-full mt-1 bg-white dark:bg-gray-800 rounded-xl shadow-lg border border-gray-100 dark:border-gray-700 z-50 overflow-hidden"
            >
              <button 
                v-for="(item, index) in suggestions" 
                :key="index"
                class="w-full px-6 py-3 text-left text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors text-sm"
                @mousedown.prevent="selectSuggestion(item)"
              >
                {{ item }}
              </button>
            </div>
          </div>
          
          <!-- 搜索历史 -->
          <div v-if="searchHistory.length > 0 && !searchResult.length && !authorResult.length" class="mt-4">
            <div class="flex items-center justify-between mb-2">
              <span class="text-sm text-gray-500">搜索历史</span>
              <button class="text-xs text-gray-400 hover:text-red-500 transition-colors" @click="clearHistory">
                清除
              </button>
            </div>
            <div class="flex flex-wrap gap-2">
              <button 
                v-for="(item, index) in searchHistory.slice(0, 6)" 
                :key="index"
                class="px-3 py-1 rounded-full bg-gray-100 dark:bg-gray-700 text-sm text-gray-600 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors"
                @click="keyword = item; handleSearch()"
              >
                {{ item }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </section>

    <div class="container mx-auto px-4 py-6">
      <!-- 热门搜索 -->
      <section v-if="!searchResult.length && !authorResult.length && !loading" class="max-w-2xl mx-auto">
        <div class="bg-white dark:bg-gray-800 rounded-2xl shadow-card p-6">
          <h3 class="font-bold text-gray-800 dark:text-gray-200 mb-4 flex items-center gap-2">
            <el-icon class="text-orange-500" :size="18"><TrendCharts /></el-icon>
            热门搜索
          </h3>
          <div class="flex flex-wrap gap-3">
            <button 
              v-for="(tag, index) in hotKeywords" 
              :key="tag"
              class="px-4 py-2 rounded-xl text-sm transition-all duration-200"
              :class="index < 3 
                ? 'bg-gradient-warm text-white shadow-md' 
                : 'bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-600'"
              @click="keyword = tag; handleSearch()"
            >
              {{ tag }}
            </button>
          </div>
        </div>
        
        <!-- 推荐小说 -->
        <div class="mt-6 bg-white dark:bg-gray-800 rounded-2xl shadow-card p-6">
          <h3 class="font-bold text-gray-800 dark:text-gray-200 mb-4 flex items-center gap-2">
            <el-icon class="text-primary" :size="18"><Star /></el-icon>
            精品推荐
          </h3>
          <div class="grid grid-cols-2 sm:grid-cols-4 gap-4">
            <NovelCard 
              v-for="novel in recommendNovels" 
              :key="novel.id"
              :novel="novel"
              @click="handleNovelClick"
            />
          </div>
        </div>
      </section>

      <!-- 搜索结果 -->
      <section v-else class="max-w-4xl mx-auto">
        <!-- 加载状态 -->
        <div v-if="loading" class="flex justify-center py-16">
          <el-icon class="is-loading" :size="40"><Loading /></el-icon>
        </div>
        
        <!-- 结果列表 -->
        <template v-else-if="searchResult.length || authorResult.length">
          <!-- 搜索类型切换 + 结果统计 -->
          <div class="flex items-center justify-between mb-5">
            <div class="flex items-center gap-1 bg-gray-100 dark:bg-gray-700 rounded-xl p-1">
              <button 
                class="px-5 py-2 rounded-lg text-sm font-medium transition-all"
                :class="searchType === 'novel' ? 'bg-white dark:bg-gray-600 text-primary shadow-sm' : 'text-gray-500 dark:text-gray-400 hover:text-gray-700'"
                @click="searchType = 'novel'; handleSearchTypeChange()"
              >
                小说
              </button>
              <button 
                class="px-5 py-2 rounded-lg text-sm font-medium transition-all"
                :class="searchType === 'author' ? 'bg-white dark:bg-gray-600 text-primary shadow-sm' : 'text-gray-500 dark:text-gray-400 hover:text-gray-700'"
                @click="searchType = 'author'; handleSearchTypeChange()"
              >
                作者
              </button>
            </div>
            <p class="text-gray-600 dark:text-gray-300">
              搜索 "<span class="text-primary font-medium">{{ searchedKeyword }}</span>" 
              找到 <span class="font-bold">{{ total }}</span> 个结果
            </p>
          </div>
          
          <!-- 小说结果 -->
          <div v-if="searchType === 'novel'" class="space-y-4">
            <div 
              v-for="novel in searchResult" 
              :key="novel.id"
              class="bg-white dark:bg-gray-800 rounded-2xl shadow-card overflow-hidden hover:shadow-card-hover transition-all duration-300 cursor-pointer"
              @click="handleNovelClick(novel)"
            >
              <div class="flex gap-4 p-4">
                <!-- 封面 -->
                <img 
                  :src="getImageUrl(novel.url)" 
                  class="w-20 h-28 object-cover rounded-xl flex-shrink-0"
                />
                
                <!-- 信息 -->
                <div class="flex-1 min-w-0 py-1">
                  <h3 
                    class="font-bold text-gray-800 dark:text-gray-200 text-lg"
                    v-html="highlightKeyword(novel.name)"
                  ></h3>
                  <p class="text-gray-500 dark:text-gray-400 mt-1">
                    {{ novel.authorName }}
                  </p>
                  <div class="flex items-center gap-3 mt-2 text-sm text-gray-400">
                    <span>{{ novel.chapterCount }}章</span>
                    <span v-if="novel.categoryName" class="px-2 py-0.5 bg-gray-100 dark:bg-gray-700 rounded">
                      {{ novel.categoryName }}
                    </span>
                    <span v-if="novel.isFinished" class="text-green-500">完结</span>
                    <span v-if="novel.isHot" class="text-red-500">热门</span>
                  </div>
                </div>
                
                <!-- 箭头 -->
                <div class="flex items-center text-gray-300">
                  <el-icon :size="24"><ArrowRight /></el-icon>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 作者结果 -->
          <div v-else class="space-y-4">
            <div 
              v-for="author in authorResult" 
              :key="author.id"
              class="bg-white dark:bg-gray-800 rounded-2xl shadow-card overflow-hidden hover:shadow-card-hover transition-all duration-300 cursor-pointer"
              @click="handleAuthorClick(author)"
            >
              <div class="flex gap-4 p-4 items-center">
                <!-- 头像 -->
                <img 
                  :src="author.avatar ? getImageUrl(author.avatar) : '/default-avatar.png'" 
                  class="w-16 h-16 object-cover rounded-full flex-shrink-0"
                />
                
                <!-- 信息 -->
                <div class="flex-1 min-w-0">
                  <h3 
                    class="font-bold text-gray-800 dark:text-gray-200 text-lg"
                    v-html="highlightKeyword(author.name)"
                  ></h3>
                  <div class="flex items-center gap-3 mt-1 text-sm text-gray-400">
                    <span>Lv.{{ author.rank }}</span>
                    <span>{{ author.novelCount }}部作品</span>
                  </div>
                  <p v-if="author.introduction" class="text-gray-500 dark:text-gray-400 mt-1 text-sm line-clamp-1">
                    {{ author.introduction }}
                  </p>
                </div>
                
                <!-- 箭头 -->
                <div class="flex items-center text-gray-300">
                  <el-icon :size="24"><ArrowRight /></el-icon>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 分页 -->
          <div class="mt-8 flex justify-center">
            <el-pagination
              v-model:current-page="pageNum"
              v-model:page-size="pageSize"
              :total="total"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next"
              background
              @size-change="handleSearch"
              @current-change="handleSearch"
            />
          </div>
        </template>
        
        <!-- 无结果 -->
        <div v-else-if="keyword && !searchResult.length && !authorResult.length" class="text-center py-16">
          <el-icon :size="60" class="text-gray-300 dark:text-gray-600 mb-4"><Search /></el-icon>
          <p class="text-gray-500 dark:text-gray-400 text-lg">未找到相关结果</p>
          <p class="text-gray-400 text-sm mt-2">换个关键词试试</p>
          <div class="mt-6">
            <button 
              class="px-6 py-2 bg-gray-100 dark:bg-gray-700 rounded-full text-gray-600 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors"
              @click="keyword = ''"
            >
              清空搜索
            </button>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Loading, TrendCharts, Star, Search, ArrowRight } from '@element-plus/icons-vue'
import { getHotNovels } from '@/api'
import { searchNovelsByES, searchAuthorsByES, getSearchSuggest } from '@/api/search'
import NovelCard from '@/components/business/novel-card.vue'
import { getImageUrl } from '@/utils/file-url'
import type { NovelListVO, VisitorAuthorVO, SearchDTO } from '@/types'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const keyword = ref('')
const searchedKeyword = ref('')
const searchResult = ref<NovelListVO[]>([])
const authorResult = ref<VisitorAuthorVO[]>([])
const recommendNovels = ref<NovelListVO[]>([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchType = ref<'novel' | 'author'>('novel')
const suggestions = ref<string[]>([])
const showSuggestions = ref(false)
let suggestTimer: ReturnType<typeof setTimeout> | null = null

const hotKeywords = ['玄幻', '都市', '修仙', '系统', '重生', '穿越', '言情', '甜宠']

// 搜索历史
const searchHistory = ref<string[]>([])
const HISTORY_KEY = 'novel_search_history'

const loadHistory = () => {
  try {
    const history = localStorage.getItem(HISTORY_KEY)
    if (history) {
      searchHistory.value = JSON.parse(history)
    }
  } catch (e) {
    console.error('Failed to load search history:', e)
  }
}

const saveHistory = (kw: string) => {
  const newHistory = [kw, ...searchHistory.value.filter(h => h !== kw)].slice(0, 10)
  searchHistory.value = newHistory
  localStorage.setItem(HISTORY_KEY, JSON.stringify(newHistory))
}

const clearHistory = () => {
  searchHistory.value = []
  localStorage.removeItem(HISTORY_KEY)
}

// 搜索建议
const fetchSuggestions = (prefix: string) => {
  if (suggestTimer) clearTimeout(suggestTimer)
  if (!prefix.trim()) {
    suggestions.value = []
    showSuggestions.value = false
    return
  }
  suggestTimer = setTimeout(async () => {
    try {
      const res = await getSearchSuggest(prefix.trim())
      suggestions.value = res.data || []
      showSuggestions.value = suggestions.value.length > 0
    } catch {
      suggestions.value = []
      showSuggestions.value = false
    }
  }, 300)
}

const selectSuggestion = (suggestion: string) => {
  keyword.value = suggestion
  showSuggestions.value = false
  suggestions.value = []
  handleSearch()
}

const hideSuggestions = () => {
  setTimeout(() => {
    showSuggestions.value = false
  }, 200)
}

// 高亮关键词（XSS安全：先转义HTML，再替换关键词）
const highlightKeyword = (text: string) => {
  if (!searchedKeyword.value) return text
  const escaped = text.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
  const escapedKeyword = searchedKeyword.value.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
  const regex = new RegExp(`(${escapedKeyword.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')})`, 'gi')
  return escaped.replace(regex, '<span class="text-primary font-bold">$1</span>')
}

const fetchRecommend = async () => {
  try {
    const res = await getHotNovels({ pageNum: 1, pageSize: 4 })
    recommendNovels.value = res.data?.list || []
  } catch (error) {
    console.error('Failed to fetch recommend:', error)
  }
}

// 防止 handleSearch 更新 URL 后触发 watch 重复搜索
let skipWatch = false

const handleSearch = async () => {
  if (!keyword.value.trim()) return
  
  loading.value = true
  searchedKeyword.value = keyword.value.trim()
  saveHistory(searchedKeyword.value)
  showSuggestions.value = false
  
  // 更新URL（跳过 watch 触发）
  skipWatch = true
  router.replace({ query: { keyword: searchedKeyword.value, type: searchType.value } })
  nextTick(() => { skipWatch = false })
  
  try {
    const params: SearchDTO = {
      keyword: searchedKeyword.value,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }
    if (searchType.value === 'novel') {
      const res = await searchNovelsByES(params)
      searchResult.value = res.data?.list || []
      total.value = res.data?.total || 0
      authorResult.value = []
    } else {
      const res = await searchAuthorsByES(params)
      authorResult.value = res.data?.list || []
      total.value = res.data?.total || 0
      searchResult.value = []
    }
  } catch (error) {
    console.error('Search failed:', error)
  } finally {
    loading.value = false
  }
}

const handleNovelClick = (novel: NovelListVO) => {
  router.push(`/novel/${novel.id}`)
}

const handleAuthorClick = (author: VisitorAuthorVO) => {
  router.push(`/author/${author.id}`)
}

const handleSearchTypeChange = () => {
  pageNum.value = 1
  if (searchedKeyword.value) {
    handleSearch()
  }
}

// 监听路由参数
watch(() => route.query.keyword, (newKeyword) => {
  if (skipWatch) return
  if (newKeyword && typeof newKeyword === 'string') {
    keyword.value = newKeyword
    const type = route.query.type as string
    if (type === 'author') searchType.value = 'author'
    else searchType.value = 'novel'
    handleSearch()
  }
}, { immediate: true })

onMounted(() => {
  loadHistory()
  fetchRecommend()
})

onUnmounted(() => {
  if (suggestTimer) clearTimeout(suggestTimer)
})
</script>