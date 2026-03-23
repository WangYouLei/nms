import type { Router } from 'vue-router'
import { useUserStore } from '@/stores/modules/user'
import { getToken } from '@/utils/auth'

export function setupRouterGuards(router: Router) {
  router.beforeEach((to, _from, next) => {
    // 设置页面标题
    const title = to.meta.title as string
    document.title = title ? `${title} - NMS` : 'NMS'

    const userStore = useUserStore()
    const token = getToken()

    // 需要登录的页面
    if (to.meta.requiresAuth) {
      if (!token) {
        // 未登录，跳转登录页
        next({
          path: '/login',
          query: { redirect: to.fullPath }
        })
        return
      }

      // 检查角色权限
      const requiredRole = to.meta.role as string
      if (requiredRole && userStore.role !== requiredRole) {
        next('/403')
        return
      }
    }

    next()
  })

  router.afterEach(() => {
    // 滚动到顶部
    window.scrollTo(0, 0)
  })
}