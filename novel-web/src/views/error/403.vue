<template>
  <div class="error-page min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900">
    <div class="text-center">
      <h1 class="text-9xl font-bold text-gray-200 dark:text-gray-700">403</h1>
      <p class="text-2xl font-medium text-gray-600 dark:text-gray-300 mt-4">无权限访问</p>
      <p class="text-gray-400 mt-2">抱歉，您没有权限访问此页面</p>
      <el-button type="primary" class="mt-6" @click="router.push('/')">
        返回首页
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
</script>