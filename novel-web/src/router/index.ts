import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  // ==================== 公共页面 ====================
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: () => import('@/views/forgot-password/index.vue'),
    meta: { title: '找回密码' }
  },
  {
    path: '/404',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '页面不存在' }
  },
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('@/views/error/403.vue'),
    meta: { title: '无权限' }
  },

  // ==================== 访客端 ====================
  {
    path: '/',
    component: () => import('@/layouts/visitor/index.vue'),
    children: [
      {
        path: '',
        redirect: '/home'
      },
      {
        path: 'home',
        name: 'VisitorHome',
        component: () => import('@/views/visitor/home/index.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'category/:id?',
        name: 'VisitorCategory',
        component: () => import('@/views/visitor/category/index.vue'),
        meta: { title: '分类' }
      },
      {
        path: 'novel/:id',
        name: 'VisitorNovelDetail',
        component: () => import('@/views/visitor/novel/detail.vue'),
        meta: { title: '小说详情' }
      },
      {
        path: 'read/:novelId/:chapterId',
        name: 'VisitorRead',
        component: () => import('@/views/visitor/novel/read.vue'),
        meta: { title: '阅读' }
      },
      {
        path: 'search',
        name: 'VisitorSearch',
        component: () => import('@/views/visitor/search/index.vue'),
        meta: { title: '搜索' }
      },
      {
        path: 'user',
        name: 'VisitorUser',
        component: () => import('@/views/visitor/user/index.vue'),
        meta: { title: '个人中心', requiresAuth: true, role: 'VISITOR' }
      },
      {
        path: 'settings',
        name: 'VisitorSettings',
        component: () => import('@/views/visitor/settings/index.vue'),
        meta: { title: '账号设置', requiresAuth: true, role: 'VISITOR' }
      },
      {
        path: 'comments',
        name: 'VisitorComments',
        component: () => import('@/views/visitor/comment/index.vue'),
        meta: { title: '我的评论', requiresAuth: true, role: 'VISITOR' }
      },
      {
        path: 'favorite',
        name: 'VisitorFavorite',
        component: () => import('@/views/visitor/favorite/index.vue'),
        meta: { title: '我的收藏', requiresAuth: true, role: 'VISITOR' }
      },
      {
        path: 'history',
        name: 'VisitorHistory',
        component: () => import('@/views/visitor/history/index.vue'),
        meta: { title: '阅读历史', requiresAuth: true, role: 'VISITOR' }
      },
      {
        path: 'author/:id',
        name: 'VisitorAuthor',
        component: () => import('@/views/visitor/author/index.vue'),
        meta: { title: '作者主页' }
      }
    ]
  },
  {
    path: '/visitor/register',
    name: 'VisitorRegister',
    component: () => import('@/views/visitor/auth/register.vue'),
    meta: { title: '访客注册' }
  },

  // ==================== 作者端 ====================
  {
    path: '/author/login',
    name: 'AuthorLogin',
    component: () => import('@/views/author/auth/login.vue'),
    meta: { title: '作者登录' }
  },
  {
    path: '/author/register',
    name: 'AuthorRegister',
    component: () => import('@/views/author/auth/register.vue'),
    meta: { title: '作者注册' }
  },
  {
    path: '/author',
    component: () => import('@/layouts/author/index.vue'),
    meta: { requiresAuth: true, role: 'AUTHOR' },
    children: [
      {
        path: '',
        redirect: 'dashboard'
      },
      {
        path: 'dashboard',
        name: 'AuthorDashboard',
        component: () => import('@/views/author/dashboard/index.vue'),
        meta: { title: '工作台' }
      },
      {
        path: 'novels',
        name: 'AuthorNovels',
        component: () => import('@/views/author/novel/list.vue'),
        meta: { title: '我的小说' }
      },
      {
        path: 'novel/create',
        name: 'AuthorNovelCreate',
        component: () => import('@/views/author/novel/edit.vue'),
        meta: { title: '新建小说' }
      },
      {
        path: 'novel/edit/:id',
        name: 'AuthorNovelEdit',
        component: () => import('@/views/author/novel/edit.vue'),
        meta: { title: '编辑小说' }
      },
      {
        path: 'novel/:novelId/chapters',
        name: 'AuthorChapters',
        component: () => import('@/views/author/chapter/list.vue'),
        meta: { title: '章节管理' }
      },
      {
        path: 'novel/:novelId/chapter/create',
        name: 'AuthorChapterCreate',
        component: () => import('@/views/author/chapter/create.vue'),
        meta: { title: '新建章节' }
      },
      {
        path: 'novel/:novelId/chapter/:chapterId/edit',
        name: 'AuthorChapterEdit',
        component: () => import('@/views/author/chapter/edit.vue'),
        meta: { title: '编辑章节' }
      },
      {
        path: 'comments',
        name: 'AuthorComments',
        component: () => import('@/views/author/comment/index.vue'),
        meta: { title: '评论管理' }
      },
      {
        path: 'comments/novel/:id',
        name: 'AuthorNovelComments',
        component: () => import('@/views/author/comment/novel/[id].vue'),
        meta: { title: '小说评论' }
      },
      {
        path: 'settings',
        name: 'AuthorSettings',
        component: () => import('@/views/author/settings/index.vue'),
        meta: { title: '账号设置' }
      }
    ]
  },

  // ==================== 管理端 ====================
  {
    path: '/manager/login',
    name: 'ManagerLogin',
    component: () => import('@/views/manager/auth/login.vue'),
    meta: { title: '管理员登录' }
  },
  {
    path: '/manager',
    component: () => import('@/layouts/manager/index.vue'),
    meta: { requiresAuth: true, role: 'MANAGER' },
    children: [
      {
        path: '',
        redirect: 'dashboard'
      },
      {
        path: 'dashboard',
        name: 'ManagerDashboard',
        component: () => import('@/views/manager/dashboard/index.vue'),
        meta: { title: '数据概览' }
      },
      {
        path: 'novels',
        name: 'ManagerNovels',
        component: () => import('@/views/manager/novel/list.vue'),
        meta: { title: '小说管理' }
      },
      {
        path: 'visitors',
        name: 'ManagerVisitors',
        component: () => import('@/views/manager/visitor/list.vue'),
        meta: { title: '访客管理' }
      },
      {
        path: 'authors',
        name: 'ManagerAuthors',
        component: () => import('@/views/manager/author/list.vue'),
        meta: { title: '作者管理' }
      },
      {
        path: 'categories',
        name: 'ManagerCategories',
        component: () => import('@/views/manager/category/list.vue'),
        meta: { title: '分类管理' }
      },
      {
        path: 'comments',
        name: 'ManagerComments',
        component: () => import('@/views/manager/comment-manage/index.vue'),
        meta: { title: '评论管理' }
      },
      {
        path: 'audit',
        name: 'ManagerAudit',
        component: () => import('@/views/manager/comment/index.vue'),
        meta: { title: '人工审核' }
      },
      {
        path: 'sensitive-words',
        name: 'ManagerSensitiveWords',
        component: () => import('@/views/manager/sensitive-word/index.vue'),
        meta: { title: '敏感词管理' }
      },
      {
        path: 'administrators',
        name: 'ManagerAdministrators',
        component: () => import('@/views/manager/administrator/list.vue'),
        meta: { title: '管理员管理' }
      }
    ]
  },

  // 未知路由重定向
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

export default router