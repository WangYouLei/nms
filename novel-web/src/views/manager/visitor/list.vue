<template>
  <div class="visitor-manage-page">
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold text-gray-800 dark:text-gray-200">访客管理</h1>
    </div>
    
    <!-- 表格 -->
    <div class="bg-white dark:bg-gray-800 rounded-lg overflow-hidden">
      <el-table :data="visitors" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="头像" width="80">
          <template #default="{ row }">
            <el-avatar :src="getAvatarUrl(row.avatar)">
              {{ row.name?.charAt(0) }}
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="昵称" width="120" />
        <el-table-column prop="account" label="账号" width="120" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column label="VIP等级" width="100">
          <template #default="{ row }">
            <el-tag :type="getVipType(row.vipLevel)">{{ getVipLabel(row.vipLevel) }}</el-tag>
          </template>
        </el-table-column>
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
          @current-change="fetchVisitors"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getVipLevelName, VipLevel } from '@/enums'
import { formatDateTime } from '@/utils/format'
import { getAvatarUrl } from '@/utils/file-url'
import { getVisitorPage, getVisitorDetail } from '@/api/visitor'
import type { VisitorVO } from '@/types'

const loading = ref(true)
const visitors = ref<VisitorVO[]>([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const formatTime = formatDateTime

const getVipLabel = (level: number) => getVipLevelName(level)

const getVipType = (level: number) => {
  if (level === VipLevel.NORMAL) return 'info'
  if (level === VipLevel.GOLD_MASTER) return 'warning'
  return 'success'
}

const fetchVisitors = async () => {
  loading.value = true
  try {
    const res = await getVisitorPage({ pageNum: pageNum.value, pageSize: pageSize.value })
    if (res.data) {
      visitors.value = res.data.list || []
      total.value = res.data.total
    }
  } catch (error) {
    console.error('获取访客列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleView = async (visitor: VisitorVO) => {
  try {
    const res = await getVisitorDetail(visitor.id)
    console.log('访客详情:', res.data)
  } catch (error) {
    console.error('获取访客详情失败:', error)
  }
}

onMounted(() => {
  fetchVisitors()
})
</script>