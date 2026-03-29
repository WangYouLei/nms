import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'
import type { UserInfo } from '@/types'
import { getToken, setToken, removeToken, getUserInfo, setUserInfo, removeUserInfo, getRole, setRole, removeRole } from '@/utils/auth'
import { UserRole } from '@/enums'
import { getPresignedFileUrl } from '@/utils/file-url'
import { visitorLogout, authorLogout } from '@/api'
import { managerLogout } from '@/api/manager'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref<string>(getToken() || '')
  const userInfo = ref<UserInfo | null>(getUserInfo())
  const role = ref<string>(getRole() || '')
  const userAvatarUrl = ref<string>('/default-avatar.png')

  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  const userName = computed(() => userInfo.value?.name || '未登录')
  const userAvatar = computed(() => userAvatarUrl.value)
  const userId = computed(() => userInfo.value?.id || 0)

  // 监听头像变化，异步获取预签名URL
  watch(
    () => userInfo.value?.avatar,
    async (avatar) => {
      if (!avatar) {
        userAvatarUrl.value = '/default-avatar.png'
        return
      }
      try {
        const url = await getPresignedFileUrl(avatar)
        userAvatarUrl.value = url || '/default-avatar.png'
      } catch (error) {
        console.error('Failed to get avatar URL:', error)
        userAvatarUrl.value = '/default-avatar.png'
      }
    },
    { immediate: true }
  )

  // 判断角色
  const isManager = computed(() => role.value === UserRole.MANAGER)
  const isAuthor = computed(() => role.value === UserRole.AUTHOR)
  const isVisitor = computed(() => role.value === UserRole.VISITOR)

  // 方法
  function setTokenAndStore(newToken: string) {
    token.value = newToken
    setToken(newToken)
  }

  function setUserInfoAndStore(info: UserInfo) {
    userInfo.value = info
    setUserInfo(info)
  }

  function setRoleAndStore(newRole: string) {
    role.value = newRole
    setRole(newRole)
  }

  function login(data: { token: string; userInfo: UserInfo; role: string }) {
    setTokenAndStore(data.token)
    setUserInfoAndStore(data.userInfo)
    setRoleAndStore(data.role)
  }

  async function logout() {
    // 先调用后端 logout 接口删除 token
    try {
      const currentUserId = userInfo.value?.id
      if (currentUserId) {
        if (role.value === UserRole.VISITOR) {
          await visitorLogout(currentUserId)
        } else if (role.value === UserRole.AUTHOR) {
          await authorLogout(currentUserId)
        } else if (role.value === UserRole.MANAGER) {
          await managerLogout()
        }
      }
    } catch (error) {
      console.error('Logout API error:', error)
      // 即使后端接口失败，也继续清除本地状态
    }
    
    // 清除本地状态
    token.value = ''
    userInfo.value = null
    role.value = ''
    removeToken()
    removeUserInfo()
    removeRole()
  }

  function init() {
    const savedToken = getToken()
    const savedUserInfo = getUserInfo()
    const savedRole = getRole()
    
    if (savedToken) {
      token.value = savedToken
    }
    if (savedUserInfo) {
      userInfo.value = savedUserInfo
    }
    if (savedRole) {
      role.value = savedRole
    }
  }

  return {
    // 状态
    token,
    userInfo,
    role,
    // 计算属性
    isLoggedIn,
    userName,
    userAvatar,
    userId,
    isManager,
    isAuthor,
    isVisitor,
    // 方法
    setToken: setTokenAndStore,
    setUserInfo: setUserInfoAndStore,
    setRole: setRoleAndStore,
    login,
    logout,
    init
  }
})