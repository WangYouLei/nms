<template>
  <div 
    class="reader-container h-full flex flex-col transition-colors duration-300"
    :class="themeClass"
    :style="contentStyle"
  >
    <!-- 顶部标题栏 -->
    <Transition name="slide-down">
      <div
        v-show="!isImmersive"
        class="reader-header flex items-center justify-between px-4 py-3 border-b"
        :class="headerClass"
      >
        <div class="flex items-center gap-1">
          <button class="p-2 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors" @click="emit('back')">
            <el-icon :size="20"><ArrowLeft /></el-icon>
          </button>
          <button
            class="flex items-center gap-1 px-3 py-2 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 text-gray-900 dark:text-gray-300 transition-colors text-sm"
            :disabled="!hasPrev"
            @click.stop="emit('prev')"
          >
            <el-icon :size="16"><ArrowLeft /></el-icon>
            上一章
          </button>
        </div>
        <button
          class="flex items-center gap-1 px-3 py-2 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 text-gray-900 dark:text-gray-300 transition-colors text-sm"
          @click.stop="emit('catalog')"
        >
          <el-icon :size="16"><List /></el-icon>
          目录
        </button>
        <div class="flex items-center gap-1">
          <button
            class="flex items-center gap-1 px-3 py-2 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 text-gray-900 dark:text-gray-300 transition-colors text-sm"
            :disabled="!hasNext"
            @click.stop="emit('next')"
          >
            下一章
            <el-icon :size="16"><ArrowRight /></el-icon>
          </button>
          <button class="p-2 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors" @click="toggleImmersive">
            <el-icon :size="20"><FullScreen /></el-icon>
          </button>
        </div>
      </div>
    </Transition>

    <!-- 阅读进度条 -->
    <div 
      class="h-1 bg-gray-200 dark:bg-gray-700 transition-opacity duration-300"
      :class="{ 'opacity-0': isImmersive }"
    >
      <div 
        class="h-full bg-gradient-primary transition-all duration-300"
        :style="{ width: `${readProgress}%` }"
      ></div>
    </div>

    <!-- 章节内容 -->
    <div 
      ref="contentRef"
      class="flex-1 overflow-y-auto px-4 py-6 md:px-8 lg:px-16"
      @scroll="handleScroll"
      @click="toggleControls"
    >
      <div
        class="mx-auto reader-content whitespace-pre-wrap transition-all duration-200"
        :class="[contentWidth === 'full' ? 'max-w-full' : 'max-w-3xl']"
        :style="{ lineHeight: lineHeightValue }"
      >
        <p class="text-2xl font-bold mb-8 text-center">{{ title }}</p>
        {{ content }}
      </div>
      
      <!-- 章节导航 -->
      <div class="mx-auto mt-12 pt-8 border-t border-current/10" :class="[contentWidth === 'full' ? 'max-w-full' : 'max-w-3xl']">
        <div class="flex justify-between">
          <button
            class="flex items-center gap-2 px-4 py-2 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 text-gray-900 dark:text-gray-300 transition-colors"
            :disabled="!hasPrev"
            @click.stop="emit('prev')"
          >
            <el-icon><ArrowLeft /></el-icon>
            上一章
          </button>
          <button
            class="flex items-center gap-2 px-4 py-2 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 text-gray-900 dark:text-gray-300 transition-colors"
            :disabled="!hasNext"
            @click.stop="emit('next')"
          >
            下一章
            <el-icon><ArrowRight /></el-icon>
          </button>
        </div>
      </div>
    </div>

    <!-- 底部工具栏 -->
    <Transition name="slide-up">
      <div
        v-show="!isImmersive"
        class="reader-footer flex items-center justify-center gap-2 px-4 py-3 border-t"
        :class="footerClass"
      >
        <button
          class="flex items-center gap-1 px-4 py-2 rounded-xl bg-gray-100 dark:bg-gray-700 text-sm transition-colors hover:bg-gray-200 dark:hover:bg-gray-600"
          :disabled="!hasPrev"
          @click="emit('prev')"
        >
          <el-icon><ArrowLeft /></el-icon>
          上一章
        </button>
        <button
          class="flex items-center gap-1 px-4 py-2 rounded-xl bg-gray-100 dark:bg-gray-700 text-sm transition-colors hover:bg-gray-200 dark:hover:bg-gray-600"
          @click="emit('catalog')"
        >
          <el-icon><List /></el-icon>
          目录
        </button>
        <button
          class="flex items-center gap-1 px-4 py-2 rounded-xl bg-gray-100 dark:bg-gray-700 text-sm transition-colors hover:bg-gray-200 dark:hover:bg-gray-600"
          @click="showSettings = true"
        >
          <el-icon><Setting /></el-icon>
          设置
        </button>
        <button
          class="flex items-center gap-1 px-4 py-2 rounded-xl bg-gray-100 dark:bg-gray-700 text-sm transition-colors hover:bg-gray-200 dark:hover:bg-gray-600"
          :disabled="!hasNext"
          @click="emit('next')"
        >
          下一章
          <el-icon><ArrowRight /></el-icon>
        </button>
      </div>
    </Transition>

    <!-- 设置面板 -->
    <Transition name="fade">
      <div 
        v-if="showSettings"
        class="fixed inset-0 bg-black/50 z-50 flex items-end justify-center"
        @click="showSettings = false"
      >
        <div 
          class="w-full max-w-lg rounded-t-3xl p-6 space-y-6 transition-colors duration-300"
          :class="panelClass"
          @click.stop
        >
          <!-- 字体大小 -->
          <div>
            <div class="flex items-center justify-between mb-3">
              <span class="text-sm font-medium">字体大小</span>
              <span class="text-sm text-gray-400">{{ fontSize }}px</span>
            </div>
            <div class="flex items-center gap-4">
              <button 
                class="w-10 h-10 rounded-full bg-gray-100 dark:bg-gray-700 flex items-center justify-center text-lg"
                @click="fontSize = Math.max(14, fontSize - 2)"
              >
                A-
              </button>
              <input 
                type="range" 
                v-model="fontSize" 
                min="14" 
                max="32" 
                step="2"
                class="flex-1 h-2 bg-gray-200 dark:bg-gray-600 rounded-full appearance-none cursor-pointer"
              />
              <button 
                class="w-10 h-10 rounded-full bg-gray-100 dark:bg-gray-700 flex items-center justify-center text-lg"
                @click="fontSize = Math.min(32, fontSize + 2)"
              >
                A+
              </button>
            </div>
          </div>

          <!-- 行高 -->
          <div>
            <div class="flex items-center justify-between mb-3">
              <span class="text-sm font-medium">行间距</span>
              <span class="text-sm text-gray-400">{{ lineHeight }}</span>
            </div>
            <div class="flex gap-2">
              <button 
                v-for="lh in lineHeightOptions" 
                :key="lh.value"
                class="flex-1 py-2 rounded-xl text-sm transition-colors"
                :class="lineHeight === lh.value 
                  ? 'bg-primary text-white' 
                  : 'bg-gray-100 dark:bg-gray-700 text-gray-900 dark:text-gray-300'"
                @click="lineHeight = lh.value"
              >
                {{ lh.label }}
              </button>
            </div>
          </div>

          <!-- 页面宽度 -->
          <div>
            <div class="flex items-center justify-between mb-3">
              <span class="text-sm font-medium">页面宽度</span>
            </div>
            <div class="flex gap-2">
              <button 
                v-for="w in widthOptions" 
                :key="w.value"
                class="flex-1 py-2 rounded-xl text-sm transition-colors"
                :class="contentWidth === w.value 
                  ? 'bg-primary text-white' 
                  : 'bg-gray-100 dark:bg-gray-700 text-gray-900 dark:text-gray-300'"
                @click="contentWidth = w.value"
              >
                {{ w.label }}
              </button>
            </div>
          </div>

          <!-- 主题选择 -->
          <div>
            <div class="flex items-center justify-between mb-3">
              <span class="text-sm font-medium">阅读主题</span>
            </div>
            <div class="grid grid-cols-4 gap-3">
              <button 
                v-for="theme in themes" 
                :key="theme.value"
                class="relative p-3 rounded-xl border-2 transition-all"
                :class="currentTheme === theme.value 
                  ? 'border-primary shadow-lg' 
                  : 'border-transparent'"
                :style="{ backgroundColor: theme.bgColor }"
                @click="currentTheme = theme.value"
              >
                <span 
                  class="text-xs"
                  :style="{ color: theme.textColor }"
                >
                  {{ theme.label }}
                </span>
                <span 
                  v-if="currentTheme === theme.value"
                  class="absolute -top-1 -right-1 w-4 h-4 rounded-full bg-primary text-white flex items-center justify-center"
                >
                  <el-icon :size="10"><Check /></el-icon>
                </span>
              </button>
            </div>
          </div>

          <!-- 关闭按钮 -->
          <button 
            class="w-full py-3 rounded-xl bg-gray-100 dark:bg-gray-700 text-gray-900 dark:text-gray-300 font-medium transition-colors hover:bg-gray-200 dark:hover:bg-gray-600"
            @click="showSettings = false"
          >
            完成
          </button>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { ArrowLeft, ArrowRight, List, Setting, FullScreen, Check } from '@element-plus/icons-vue'

interface Props {
  content: string
  title: string
  hasPrev?: boolean
  hasNext?: boolean
  progress?: number
}

const props = withDefaults(defineProps<Props>(), {
  hasPrev: false,
  hasNext: false,
  progress: 0
})

const emit = defineEmits<{
  (e: 'prev'): void
  (e: 'next'): void
  (e: 'catalog'): void
  (e: 'back'): void
}>()

const contentRef = ref<HTMLElement | null>(null)
const showSettings = ref(false)
const isImmersive = ref(false)
const readProgress = ref(props.progress)

// 阅读设置
const fontSize = ref(18)
const lineHeight = ref(1.8)
const contentWidth = ref('full')
const currentTheme = ref('light')

// 配置选项
const lineHeightOptions = [
  { label: '紧凑', value: 1.5 },
  { label: '适中', value: 1.8 },
  { label: '宽松', value: 2.0 },
  { label: '超宽', value: 2.4 }
]

const widthOptions = [
  { label: '全宽', value: 'full' },
  { label: '窄', value: 'narrow' },
  { label: '适中', value: 'normal' },
  { label: '宽', value: 'wide' }
]

const themes = [
  { value: 'light', label: '日间', bgColor: '#ffffff', textColor: '#333333' },
  { value: 'dark', label: '夜间', bgColor: '#1a1a1a', textColor: '#e0e0e0' },
  { value: 'sepia', label: '羊皮纸', bgColor: '#f5f0e6', textColor: '#5c4b37' },
  { value: 'green', label: '护眼', bgColor: '#cce8cf', textColor: '#2d5a3d' }
]

// 计算样式
const themeClass = computed(() => {
  const classes: Record<string, string> = {
    light: 'bg-white text-gray-800',
    dark: 'bg-gray-900 text-gray-100',
    sepia: 'bg-amber-50 text-amber-900',
    green: 'bg-green-100 text-green-900'
  }
  return classes[currentTheme.value]
})

const headerClass = computed(() => {
  const classes: Record<string, string> = {
    light: 'border-gray-100 bg-white text-gray-800',
    dark: 'border-gray-800 bg-gray-900 text-gray-100',
    sepia: 'border-amber-200 bg-amber-50 text-amber-900',
    green: 'border-green-200 bg-green-100 text-green-900'
  }
  return classes[currentTheme.value]
})

const footerClass = computed(() => {
  const classes: Record<string, string> = {
    light: 'border-gray-100 bg-white text-gray-800',
    dark: 'border-gray-800 bg-gray-900 text-gray-100',
    sepia: 'border-amber-200 bg-amber-50 text-amber-900',
    green: 'border-green-200 bg-green-100 text-green-900'
  }
  return classes[currentTheme.value]
})

const panelClass = computed(() => {
  const classes: Record<string, string> = {
    light: 'bg-white text-gray-800',
    dark: 'bg-gray-800 text-gray-100',
    sepia: 'bg-amber-50 text-amber-900',
    green: 'bg-green-100 text-green-900'
  }
  return classes[currentTheme.value]
})

const contentStyle = computed(() => {
  const widthMap: Record<string, string> = {
    full: '100%',
    narrow: '42rem',
    normal: '48rem',
    wide: '56rem'
  }

  return {
    fontSize: `${fontSize.value}px`,
    maxWidth: widthMap[contentWidth.value]
  }
})

const lineHeightValue = computed(() => lineHeight.value)

// 方法
const toggleImmersive = () => {
  isImmersive.value = !isImmersive.value
}

const toggleControls = () => {
  if (isImmersive.value) {
    isImmersive.value = false
  }
}

const handleScroll = () => {
  if (!contentRef.value) return
  const { scrollTop, scrollHeight, clientHeight } = contentRef.value
  readProgress.value = Math.round((scrollTop / (scrollHeight - clientHeight)) * 100) || 0
}

// 加载设置
const loadSettings = () => {
  try {
    const saved = localStorage.getItem('reader_settings')
    if (saved) {
      const settings = JSON.parse(saved)
      fontSize.value = settings.fontSize || 18
      lineHeight.value = settings.lineHeight || 1.8
      contentWidth.value = settings.contentWidth || 'full'
      currentTheme.value = settings.theme || 'light'
    }
  } catch (e) {
    console.error('Failed to load reader settings:', e)
  }
}

// 保存设置
watch([fontSize, lineHeight, contentWidth, currentTheme], () => {
  localStorage.setItem('reader_settings', JSON.stringify({
    fontSize: fontSize.value,
    lineHeight: lineHeight.value,
    contentWidth: contentWidth.value,
    theme: currentTheme.value
  }))
}, { deep: true })

onMounted(() => {
  loadSettings()
})
</script>

<style scoped>
.reader-content {
  text-align: justify;
  word-break: break-word;
}

/* 过渡动画 */
.slide-down-enter-active,
.slide-down-leave-active {
  transition: all 0.3s ease;
}

.slide-down-enter-from,
.slide-down-leave-to {
  transform: translateY(-100%);
  opacity: 0;
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.3s ease;
}

.slide-up-enter-from,
.slide-up-leave-to {
  transform: translateY(100%);
  opacity: 0;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 自定义滑块样式 */
input[type="range"]::-webkit-slider-thumb {
  appearance: none;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  cursor: pointer;
}

input[type="range"]::-moz-range-thumb {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  cursor: pointer;
  border: none;
}
</style>