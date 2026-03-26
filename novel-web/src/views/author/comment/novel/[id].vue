<template>
  <div class="novel-comment-page">
    <!-- 返回按钮和标题 -->
    <div class="flex items-center gap-4 mb-6">
      <el-button @click="$router.back()" text>
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <div>
        <h1 class="text-2xl font-bold text-gray-800 dark:text-gray-200">{{ novelName }}</h1>
        <p class="text-gray-500 dark:text-gray-400 mt-1">查看和管理该小说的所有评论</p>
      </div>
    </div>

    <!-- Tab切换 -->
    <el-tabs v-model="activeTab" class="comment-tabs" @tab-change="handleTabChange">
      <el-tab-pane label="小说评论" name="novel">
        <CommentTree
          :novel-id="novelId"
          :target-type="1"
          :author-id="authorId"
        />
      </el-tab-pane>
      <el-tab-pane label="章节评论" name="chapter">
        <CommentTree
          :novel-id="novelId"
          :target-type="2"
          :author-id="authorId"
        />
      </el-tab-pane>
      <el-tab-pane label="我的评论" name="my">
        <MyCommentList
          :novel-id="novelId"
          :user-id="userId"
        />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getNovelDetail } from '@/api/novel'
import { useUserStore } from '@/stores'
import CommentTree from './components/CommentTree.vue'
import MyCommentList from './components/MyCommentList.vue'

const route = useRoute()
const userStore = useUserStore()

const novelId = computed(() => Number(route.params.id))
const userId = computed(() => userStore.userId!)
const authorId = ref<number>()
const novelName = ref('')
const activeTab = ref('novel')

const fetchNovelInfo = async () => {
  try {
    const res = await getNovelDetail(novelId.value)
    novelName.value = res.data?.name || '未知小说'
    authorId.value = res.data?.authorId
  } catch (error) {
    console.error('Failed to fetch novel info:', error)
    novelName.value = '未知小说'
  }
}

const handleTabChange = (name: string) => {
  activeTab.value = name
}

onMounted(() => {
  fetchNovelInfo()
})
</script>

<style scoped>
.comment-tabs :deep(.el-tabs__header) {
  margin-bottom: 20px;
}
</style>