<template>
  <div class="author-detail-page">
    <!-- 加载状态 -->
    <div v-if="loading" class="flex justify-center py-20">
      <el-icon class="is-loading" :size="40"><Loading /></el-icon>
    </div>

    <template v-else-if="author">
      <!-- 作者信息卡片 -->
      <div class="bg-white dark:bg-gray-800 rounded-2xl shadow-card p-6 mb-6">
        <div class="flex flex-col md:flex-row items-center md:items-start gap-6">
          <!-- 头像 -->
          <div class="relative">
            <el-avatar 
              :size="100" 
              :src="getImageUrl(author.avatar)"
              class="ring-4 ring-primary/20"
            >
              {{ author.name?.charAt(0) }}
            </el-avatar>
          </div>
          
          <!-- 信息 -->
          <div class="flex-1 text-center md:text-left">
            <h1 class="text-2xl font-bold text-gray-800 dark:text-gray-200">
              {{ author.name }}
            </h1>
            
            <!-- 等级标签 -->
            <div class="flex items-center justify-center md:justify-start gap-2 mt-3">
              <el-tag :type="getRankType(author.rank)" effect="plain" class="rounded-full">
                {{ author.rankName || getRankName(author.rank) }}
              </el-tag>
            </div>
            
            <!-- 简介 -->
            <p v-if="author.introduction" class="text-gray-600 dark:text-gray-300 mt-4 max-w-xl">
              {{ author.introduction }}
            </p>
            
            <!-- 关注按钮 -->
            <div class="mt-4 flex items-center justify-center md:justify-start gap-3">
              <el-button 
                :type="isFollowing ? 'danger' : 'primary'"
                :loading="followLoading"
                @click="toggleFollow"
              >
                <el-icon class="mr-1"><Plus /></el-icon>
                {{ isFollowing ? '已关注' : '关注作者' }}
              </el-button>
            </div>
          </div>
          
          <!-- 统计 -->
          <div class="flex gap-8 text-center">
            <div>
              <p class="text-2xl font-bold text-primary">{{ author.novelCount || 0 }}</p>
              <p class="text-sm text-gray-500">作品</p>
            </div>
            <div>
              <p class="text-2xl font-bold text-primary">{{ totalChapters }}</p>
              <p class="text-sm text-gray-500">章节</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 作品列表 -->
      <div class="bg-white dark:bg-gray-800 rounded-2xl shadow-card p-6">
        <div class="flex items-center gap-2 mb-4">
          <div class="w-1 h-5 bg-gradient-primary rounded-full"></div>
          <h3 class="text-lg font-bold text-gray-800 dark:text-gray-200">TA的作品</h3>
          <span class="text-sm text-gray-400">({{ novels.length }}本)</span>
        </div>
        
        <div v-if="novels.length === 0" class="text-center py-12 text-gray-400">
          <el-icon :size="48" class="mb-3"><Document /></el-icon>
          <p>暂无作品</p>
        </div>
        
        <div v-else class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4">
          <div 
            v-for="novel in novels" 
            :key="novel.id"
            class="group cursor-pointer"
            @click="goToNovel(novel.id)"
          >
            <div class="relative overflow-hidden rounded-xl shadow-md group-hover:shadow-lg transition-shadow">
              <img 
                :src="getImageUrl(novel.url)" 
                :alt="novel.name"
                class="w-full aspect-[3/4] object-cover group-hover:scale-105 transition-transform duration-300"
              />
              <!-- 完结标签 -->
              <span 
                v-if="novel.isFinished"
                class="absolute top-2 right-2 px-2 py-1 bg-green-500 text-white text-xs font-medium rounded-lg"
              >
                完结
              </span>
            </div>
            <div class="mt-2">
              <p class="text-sm font-medium text-gray-800 dark:text-gray-200 truncate">{{ novel.name }}</p>
              <p class="text-xs text-gray-500">{{ novel.chapterCount }}章</p>
            </div>
          </div>
        </div>
      </div>
    </template>

    <!-- 空状态 -->
    <div v-else class="text-center py-20 text-gray-400">
      <el-icon :size="60" class="mb-4"><Warning /></el-icon>
      <p>作者不存在</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Loading, Document, Warning, Plus } from '@element-plus/icons-vue'
import { getAuthorDetail } from '@/api/novel'
import { addFollow, removeFollow, checkFollow } from '@/api/follow'
import { getImageUrl } from '@/utils/file-url'
import { useUserStore } from '@/stores'
import { ElMessage } from 'element-plus'
import type { AuthorDetailVO, NovelListVO } from '@/types'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loading = ref(true)
const author = ref<AuthorDetailVO | null>(null)
const novels = ref<NovelListVO[]>([])
const isFollowing = ref(false)
const followLoading = ref(false)

// 通过小说列表计算章节数
const totalChapters = computed(() => {
  return novels.value.reduce((sum, novel) => sum + (novel.chapterCount || 0), 0)
})

const getRankName = (rank: number) => {
  const ranks: Record<number, string> = {
    1: '执笔者',
    2: '织梦师',
    3: '造界者',
    4: '渡舟人',
    5: '燃灯者'
  }
  return ranks[rank] || '执笔者'
}

const getRankType = (rank: number): 'primary' | 'success' | 'warning' | 'danger' | 'info' => {
  const types: Record<number, 'primary' | 'success' | 'warning' | 'danger' | 'info'> = {
    1: 'info',
    2: 'primary',
    3: 'success',
    4: 'warning',
    5: 'danger'
  }
  return types[rank] || 'info'
}

const goToNovel = (novelId: number) => {
  router.push(`/novel/${novelId}`)
}

const toggleFollow = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  if (!author.value) return

  followLoading.value = true
  try {
    if (isFollowing.value) {
      await removeFollow(author.value.id, userStore.userId!)
      isFollowing.value = false
      ElMessage.success('已取消关注')
    } else {
      await addFollow({
        visitorId: userStore.userId!,
        authorId: author.value.id,
        authorName: author.value.name,
        authorAvatar: author.value.avatar,
        authorRank: author.value.rank
      })
      isFollowing.value = true
      ElMessage.success('关注成功')
    }
  } catch (error) {
    console.error('Failed to toggle follow:', error)
    ElMessage.error('操作失败')
  } finally {
    followLoading.value = false
  }
}

const fetchAuthor = async () => {
  const authorId = Number(route.params.id)
  if (!authorId) return

  loading.value = true
  try {
    const res = await getAuthorDetail(authorId, { pageNum: 1, pageSize: 100 })
    author.value = res.data
    novels.value = res.data?.novels || []
    
    // 检查关注状态
    if (userStore.isLoggedIn) {
      const followRes = await checkFollow(authorId, userStore.userId!)
      isFollowing.value = followRes.data || false
    }
  } catch (error) {
    console.error('Failed to fetch author:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchAuthor()
})
</script>

<style scoped>
/* 样式 */
</style>