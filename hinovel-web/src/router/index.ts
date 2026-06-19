import {createRouter, createWebHistory} from 'vue-router'
import {useAuthStore} from '@/store/auth'
import {useNovelRouteStore} from '@/store/novel-route'
import MainLayout from '@/components/layout/MainLayout.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/auth/LoginView.vue'),
      meta: { public: true }
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/views/auth/RegisterView.vue'),
      meta: { public: true }
    },
    {
      path: '/',
      component: MainLayout,
      redirect: '/novel',
      children: [
        // 小说创作
        {
          path: 'novel',
          name: 'NovelList',
          component: () => import('@/views/novel/NovelListView.vue'),
          meta: { title: '我的小说', icon: 'Reading' }
        },
          // 模型管理（含 LLM 对话模型、嵌入式模型等，通过 Tab 切换）
          {
              path: 'llm-providers',
              name: 'LlmProviders',
              component: () => import('@/views/llm/LlmProviderView.vue'),
              meta: {title: '模型管理', icon: 'Cpu'}
          },
          // Agent 管理
          {
              path: 'agents',
              name: 'Agents',
              component: () => import('@/views/agent/AgentView.vue'),
              meta: {title: 'Agent 管理', icon: 'User'}
          },
          // LLM 调用记录
          {
              path: 'llm-call-logs',
              name: 'LlmCallLogs',
              component: () => import('@/views/llm/LlmCallLogView.vue'),
              meta: {title: '调用记录', icon: 'Document'}
          },
          // 用户默认设置
          {
              path: 'settings',
              name: 'UserSettings',
              component: () => import('@/views/settings/UserSettingsView.vue'),
              meta: {title: '默认设置', icon: 'Setting', hideInMenu: true}
          },
        {
          path: 'novel/:id',
          name: 'NovelDetail',
          component: () => import('@/views/novel/NovelView.vue'),
          meta: { title: '小说详情', hideInMenu: true }
        },
        {
          path: 'novel/:id/characters',
          name: 'CharacterGraph',
          component: () => import('@/views/novel/CharacterGraphView.vue'),
          meta: { title: '人物图谱', hideInMenu: true }
        },
      ]
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'NotFound',
      component: () => import('@/views/novel/NovelListView.vue')
    }
  ]
})

// 路由守卫
router.beforeEach((to, _from, next) => {
  const authStore = useAuthStore()

  if (!to.meta.public && !authStore.isLoggedIn) {
    next('/login')
  } else if (to.path === '/login' && authStore.isLoggedIn) {
    next('/')
  } else {
      // 记录小说创作页路径（/novel/:id 格式），用于导航栏恢复
      const novelRouteStore = useNovelRouteStore()
      const novelDetailPattern = /^\/novel\/\d+$/
      if (novelDetailPattern.test(to.path)) {
          novelRouteStore.setLastNovelPath(to.path)
      }
    next()
  }
})

export default router
