<template>
  <router-view />
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useUserStore } from '@/stores/modules/user'
import { useAppStore } from '@/stores/modules/app'

const userStore = useUserStore()
const appStore = useAppStore()

onMounted(() => {
  // 初始化用户状态
  userStore.init()
  // 初始化应用状态
  appStore.init()
})
</script>

<style>
#app {
  width: 100%;
  min-height: 100vh;
}
</style>