<template>
  <div class="nms-logo flex items-center space-x-2" :class="sizeClass">
    <img :src="logoSrc" :alt="altText" :class="imgClass" />
    <span v-if="showTitle" class="font-bold" :class="titleClass">{{ title }}</span>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  size?: 'sm' | 'md' | 'lg' | 'xl'
  variant?: 'default' | 'light' | 'dark'
  showTitle?: boolean
  title?: string
  alt?: string
}

const props = withDefaults(defineProps<Props>(), {
  size: 'md',
  variant: 'default',
  showTitle: true,
  title: 'NMS',
  alt: 'NMS Logo'
})

const logoSrc = '/logo.png'

const sizeClass = computed(() => {
  const sizeMap = {
    sm: 'text-sm',
    md: 'text-base',
    lg: 'text-lg',
    xl: 'text-xl'
  }
  return sizeMap[props.size]
})

const imgClass = computed(() => {
  const sizeMap = {
    sm: 'w-4 h-4',
    md: 'w-6 h-6',
    lg: 'w-8 h-8',
    xl: 'w-10 h-10'
  }
  return sizeMap[props.size]
})

const titleClass = computed(() => {
  const variantMap = {
    default: 'text-gray-800 dark:text-white',
    light: 'text-white',
    dark: 'text-gray-800 dark:text-white'
  }
  return variantMap[props.variant]
})

const altText = computed(() => props.alt)
</script>