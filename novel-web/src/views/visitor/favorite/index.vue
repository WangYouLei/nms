<template>
  <div class="favorite-page container mx-auto px-4 py-6">
    <div class="max-w-4xl mx-auto">
      <!-- 页面标题 -->
      <div class="flex items-center justify-between mb-6">
        <h1 class="text-2xl font-bold text-gray-800 dark:text-gray-200">我的收藏</h1>
        <span class="text-gray-500">共 {{ collectList.length }} 本</span>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="flex justify-center py-20">
        <el-icon class="is-loading" :size="40"><Loading /></el-icon>
      </div>

      <!-- 空状态 -->
      <div v-else-if="collectList.length === 0" class="text-center py-20">
        <el-icon :size="60" class="text-gray-300 mb-4"><Star /></el-icon>
        <p class="text-gray-400 mb-4">暂无收藏的小说</p>
        <el-button type="primary" @click="router.push('/home')">
          去发现
        </el-button>
      </div>

      <!-- 收藏列表 -->
      <div v-else class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4">
        <div 
          v-for="item in collectList" 
          :key="item.id"
          class="group cursor-pointer"
        >
          <div class="relative overflow-hidden rounded-xl shadow-md group-hover:shadow-lg transition-shadow">
            <img 
              :src="getImageUrl(item.novelUrl)" 
              :alt="item.novelName"
              class="w-full aspect-[3/4] object-cover group-hover:scale-105 transition-transform duration-300"
            />
            <!-- 删除按钮 -->
            <button 
              class="absolute top-2 right-2 w-8 h-8 bg-black/50 rounded-full flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity"
              @click.stop="handleRemove(item)"
            >
              <el-icon class="text-white"><Close /></el-icon>
            </button>
          </div>
          <div class="mt-2">
            <p class="text-sm font-medium text-gray-800 dark:text-gray-200 truncate">{{ item.novelName }}</p>
            <p class="text-xs text-gray-500">{{ item.authorName }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Loading, Star, Close } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCollectList, removeCollect } from '@/api/collect'
import { getImageUrl } from '@/utils/file-url'
import type { VisitorCollectVO } from '@/types'

const router = useRouter()

const loading = ref(true)
const collectList = ref<VisitorCollectVO[]>([])

const fetchCollectList = async () => {
  loading.value = true
  try {
    const res = await getCollectList()
    collectList.value = res.data || []
  } catch (error) {
    console.error('Failed to fetch collect list:', error)
    ElMessage.error('获取收藏列表失败')
  } finally {
    loading.value = false
  }
}

const handleRemove = async (item: VisitorCollectVO) => {
  try {
    await ElMessageBox.confirm(`确定要取消收藏《${item.novelName}》吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await removeCollect(item.novelId)
    collectList.value = collectList.value.filter(c => c.id !== item.id)
    ElMessage.success('已取消收藏')
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('Failed to remove collect:', error)
      ElMessage.error('操作失败')
    }
  }
}

onMounted(() => {
  fetchCollectList()
})
</script>