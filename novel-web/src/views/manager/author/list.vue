<template>
  <div class="author-manage-page">
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold text-gray-800 dark:text-gray-200">作者管理</h1>
    </div>
    
    <!-- 表格 -->
    <div class="bg-white dark:bg-gray-800 rounded-lg overflow-hidden">
      <el-table :data="authors" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="头像" width="80">
          <template #default="{ row }">
            <el-avatar :src="row.avatar" />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="昵称" width="120" />
        <el-table-column prop="account" label="账号" width="120" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column label="等级" width="100">
          <template #default="{ row }">
            <el-tag type="primary">{{ getRankLabel(row.rank) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="novelCount" label="作品数" width="80" />
        <el-table-column prop="createTime" label="注册时间" width="160">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link type="primary" @click="handleView(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="p-4 flex justify-end">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="fetchAuthors"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getAuthorRankName } from '@/enums'
import { formatDateTime } from '@/utils/format'
import type { AuthorVO } from '@/types'

const loading = ref(true)
const authors = ref<AuthorVO[]>([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const formatTime = formatDateTime

const getRankLabel = (rank: number) => getAuthorRankName(rank)

const fetchAuthors = async () => {
  loading.value = true
  // 模拟数据
  authors.value = [
    { id: 1, name: '作者1', account: '13800138001', email: 'author1@test.com', rank: 3, novelCount: 5, avatar: '', createTime: '2024-01-01' },
    { id: 2, name: '作者2', account: '13800138002', email: 'author2@test.com', rank: 2, novelCount: 3, avatar: '', createTime: '2024-01-02' }
  ]
  total.value = 2
  loading.value = false
}

const handleView = (author: AuthorVO) => {
  console.log('View author:', author)
}

onMounted(() => {
  fetchAuthors()
})
</script>