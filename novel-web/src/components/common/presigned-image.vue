<template>
  <img 
    :src="displayUrl" 
    :alt="alt"
    :class="imgClass"
    @error="handleError"
  />
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { getPresignedFileUrl } from '@/utils/file-url'

interface Props {
  src?: string | null
  alt?: string
  defaultUrl?: string
  imgClass?: string
}

const props = withDefaults(defineProps<Props>(), {
  alt: '',
  defaultUrl: '/default-cover.jpg',
  imgClass: ''
})

const displayUrl = ref(props.defaultUrl)
const hasError = ref(false)

const updateUrl = async () => {
  if (!props.src) {
    displayUrl.value = props.defaultUrl
    return
  }
  
  hasError.value = false
  
  try {
    // 异步获取预签名URL
    const url = await getPresignedFileUrl(props.src)
    displayUrl.value = url || props.defaultUrl
  } catch (error) {
    console.error('Failed to get image URL:', error)
    displayUrl.value = props.defaultUrl
  }
}

const handleError = () => {
  if (!hasError.value) {
    hasError.value = true
    displayUrl.value = props.defaultUrl
  }
}

watch(() => props.src, updateUrl, { immediate: true })
</script>