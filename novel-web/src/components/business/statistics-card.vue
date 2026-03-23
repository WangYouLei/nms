<template>
  <div 
    class="statistics-card relative overflow-hidden rounded-2xl p-5 transition-all duration-300"
    :class="[
      glassEffect 
        ? 'bg-white/70 dark:bg-gray-800/70 backdrop-blur-md border border-white/30 dark:border-gray-700/50 shadow-glass' 
        : 'bg-white dark:bg-gray-800 shadow-card hover:shadow-card-hover',
      { 'hover:-translate-y-1': hoverable }
    ]"
  >
    <!-- 背景装饰 -->
    <div 
      v-if="showDecor" 
      class="absolute -right-4 -top-4 w-24 h-24 rounded-full opacity-10"
      :class="decorClass"
    ></div>

    <div class="relative flex items-start justify-between">
      <!-- 左侧内容 -->
      <div class="flex-1">
        <!-- 标题 -->
        <p class="text-sm text-gray-500 dark:text-gray-400 flex items-center gap-1">
          <el-icon v-if="showTitleIcon && icon" :size="16" class="text-gray-400">
            <component :is="icon" />
          </el-icon>
          {{ title }}
        </p>
        
        <!-- 数值 -->
        <div class="mt-2 flex items-baseline gap-2">
          <span class="text-3xl font-bold text-gray-800 dark:text-gray-200">
            {{ formatNumber(value) }}
          </span>
          <span v-if="unit" class="text-sm text-gray-400">{{ unit }}</span>
        </div>
        
        <!-- 趋势指示器 -->
        <div v-if="trendValue !== undefined" class="mt-2 flex items-center gap-1">
          <span 
            class="flex items-center text-xs font-medium"
            :class="trendClass"
          >
            <el-icon :size="14">
              <CaretTop v-if="trend === 'up'" />
              <CaretBottom v-else-if="trend === 'down'" />
              <Minus v-else />
            </el-icon>
            {{ trendValue }}
          </span>
          <span v-if="trendLabel" class="text-xs text-gray-400">{{ trendLabel }}</span>
        </div>

        <!-- 进度条 -->
        <div v-if="progress !== undefined" class="mt-3">
          <div class="h-1.5 bg-gray-100 dark:bg-gray-700 rounded-full overflow-hidden">
            <div 
              class="h-full rounded-full transition-all duration-500"
              :class="progressColorClass"
              :style="{ width: `${Math.min(progress, 100)}%` }"
            ></div>
          </div>
          <p v-if="progressLabel" class="text-xs text-gray-400 mt-1">{{ progressLabel }}</p>
        </div>
      </div>

      <!-- 右侧图标 -->
      <div 
        v-if="icon && !showTitleIcon"
        class="w-12 h-12 rounded-xl flex items-center justify-center transition-transform duration-300"
        :class="[iconBgClass, { 'group-hover:scale-110': hoverable }]"
      >
        <el-icon :size="24" :color="iconColor">
          <component :is="icon" />
        </el-icon>
      </div>
    </div>

    <!-- 底部操作区 -->
    <div v-if="$slots.footer" class="mt-4 pt-3 border-t border-gray-100 dark:border-gray-700">
      <slot name="footer"></slot>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { CaretTop, CaretBottom, Minus } from '@element-plus/icons-vue'
import type { Component } from 'vue'

interface Props {
  title: string
  value: number
  unit?: string
  icon?: Component
  iconColor?: string
  iconBgClass?: string
  trend?: 'up' | 'down' | 'flat'
  trendValue?: string
  trendLabel?: string
  progress?: number
  progressLabel?: string
  progressColor?: 'primary' | 'success' | 'warning' | 'danger'
  glassEffect?: boolean
  hoverable?: boolean
  showDecor?: boolean
  showTitleIcon?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  iconColor: '#409eff',
  iconBgClass: 'bg-primary/10 dark:bg-primary/20',
  trend: 'flat',
  progressColor: 'primary',
  glassEffect: false,
  hoverable: true,
  showDecor: false,
  showTitleIcon: false
})

const trendClass = computed(() => {
  if (props.trend === 'up') return 'text-green-500'
  if (props.trend === 'down') return 'text-red-500'
  return 'text-gray-400'
})

const progressColorClass = computed(() => {
  const colors = {
    primary: 'bg-primary',
    success: 'bg-green-500',
    warning: 'bg-yellow-500',
    danger: 'bg-red-500'
  }
  return colors[props.progressColor]
})

const decorClass = computed(() => {
  if (props.iconBgClass.includes('primary')) return 'bg-primary'
  if (props.iconBgClass.includes('green') || props.iconBgClass.includes('success')) return 'bg-green-500'
  if (props.iconBgClass.includes('yellow') || props.iconBgClass.includes('warning')) return 'bg-yellow-500'
  if (props.iconBgClass.includes('red') || props.iconBgClass.includes('danger')) return 'bg-red-500'
  return 'bg-primary'
})

const formatNumber = (num: number): string => {
  if (num >= 100000000) {
    return (num / 100000000).toFixed(1) + '亿'
  }
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + '万'
  }
  return num.toLocaleString()
}
</script>

<style scoped>
.statistics-card {
  will-change: transform, box-shadow;
}
</style>