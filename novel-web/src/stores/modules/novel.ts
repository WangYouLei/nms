import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { NovelDetailVO, NovelChapterVO } from '@/types'

interface ReadHistory {
  novelId: number
  novelName: string
  chapterId: number
  chapterTitle: string
  cover: string
  time: string
}

const READ_HISTORY_KEY = 'readHistory'
const MAX_HISTORY = 50

export const useNovelStore = defineStore('novel', () => {
  // 当前阅读的小说
  const currentNovel = ref<NovelDetailVO | null>(null)
  
  // 当前章节
  const currentChapter = ref<NovelChapterVO | null>(null)
  
  // 阅读历史
  const readHistory = ref<ReadHistory[]>([])

  // 设置当前小说
  function setCurrentNovel(novel: NovelDetailVO) {
    currentNovel.value = novel
  }

  // 设置当前章节
  function setCurrentChapter(chapter: NovelChapterVO) {
    currentChapter.value = chapter
    // 添加到阅读历史
    if (currentNovel.value) {
      addToHistory({
        novelId: currentNovel.value.id,
        novelName: currentNovel.value.name,
        chapterId: chapter.id,
        chapterTitle: chapter.title,
        cover: currentNovel.value.url || '',
        time: new Date().toISOString()
      })
    }
  }

  // 添加到阅读历史
  function addToHistory(history: ReadHistory) {
    // 移除重复
    const index = readHistory.value.findIndex(h => h.novelId === history.novelId)
    if (index > -1) {
      readHistory.value.splice(index, 1)
    }
    // 添加到开头
    readHistory.value.unshift(history)
    // 最多保存
    if (readHistory.value.length > MAX_HISTORY) {
      readHistory.value.pop()
    }
    // 持久化
    localStorage.setItem(READ_HISTORY_KEY, JSON.stringify(readHistory.value))
  }

  // 清空阅读历史
  function clearHistory() {
    readHistory.value = []
    localStorage.removeItem(READ_HISTORY_KEY)
  }

  // 初始化
  function init() {
    const savedHistory = localStorage.getItem(READ_HISTORY_KEY)
    if (savedHistory) {
      try {
        readHistory.value = JSON.parse(savedHistory)
      } catch {
        readHistory.value = []
      }
    }
  }

  return {
    currentNovel,
    currentChapter,
    readHistory,
    setCurrentNovel,
    setCurrentChapter,
    addToHistory,
    clearHistory,
    init
  }
})