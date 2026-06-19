<template>
  <div class="main-layout">
    <!-- 顶部导航栏 -->
    <header class="top-header">
      <div class="header-left">
        <router-link to="/novel" class="logo">
          <el-icon :size="28" color="#667eea"><Reading /></el-icon>
          <span class="logo-text">HiNovel</span>
        </router-link>
        <nav class="header-nav">
          <router-link
              :to="novelMenuTarget"
              class="nav-link"
              :class="{ 'nav-link-active': isNovelRouteActive }"
          >
            <el-icon>
              <Reading/>
            </el-icon>
            <span>我的小说</span>
          </router-link>
          <router-link to="/agents" class="nav-link" active-class="nav-link-active">
            <el-icon>
              <User/>
            </el-icon>
            <span>Agent 管理</span>
          </router-link>
          <router-link to="/llm-providers" class="nav-link" active-class="nav-link-active">
            <el-icon>
              <Cpu/>
            </el-icon>
            <span>模型管理</span>
          </router-link>
          <router-link to="/llm-call-logs" class="nav-link" active-class="nav-link-active">
            <el-icon>
              <Document/>
            </el-icon>
            <span>调用记录</span>
          </router-link>
        </nav>
      </div>
      <div class="header-right">
        <el-dropdown @command="handleCommand" trigger="click">
          <span class="user-info">
            <el-avatar :size="32" :src="authStore.user?.avatarUrl">
              <el-icon><UserFilled /></el-icon>
            </el-avatar>
            <span class="username">{{ authStore.user?.username || '用户' }}</span>
            <el-icon class="arrow-icon"><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="settings">
                <el-icon>
                  <Setting/>
                </el-icon>
                <span>默认设置</span>
              </el-dropdown-item>
              <el-dropdown-item divided command="logout">
                <el-icon><SwitchButton /></el-icon>
                <span>退出登录</span>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <!-- 主内容区 -->
    <main class="main-content">
      <router-view v-slot="{ Component, route }">
        <keep-alive :include="['NovelView']">
          <component :is="Component" :key="route.fullPath"/>
        </keep-alive>
      </router-view>
    </main>
  </div>
</template>

<script setup lang="ts">
import {computed} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {ElMessage, ElMessageBox} from 'element-plus'
import {ArrowDown, Cpu, Document, Reading, Setting, SwitchButton, User, UserFilled} from '@element-plus/icons-vue'
import {useAuthStore} from '@/store/auth'
import {useNovelRouteStore} from '@/store/novel-route'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const novelRouteStore = useNovelRouteStore()

// "我的小说"菜单跳转目标：优先恢复到上次的创作页
const novelMenuTarget = computed(() => novelRouteStore.novelMenuPath())

// 判断当前路由是否属于小说相关页面（列表页、创作页、人物图谱页）
const isNovelRouteActive = computed(() => {
  const path = route.path
  return path === '/novel' || /^\/novel\/\d+/.test(path)
})

const handleCommand = async (command: string) => {
  if (command === 'settings') {
    router.push('/settings')
  } else if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await authStore.logout()
      router.push('/login')
      ElMessage.success('已退出登录')
    } catch {
      // 用户取消
    }
  }
}
</script>

<style scoped lang="scss">
.main-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f8fafc;
  min-width: 1200px; /* 设置最小宽度，防止内容被压缩 */
}

.top-header {
  height: 64px;
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32px;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 24px;
}

.header-nav {
  display: flex;
  align-items: center;
  gap: 4px;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  border-radius: 8px;
  font-size: 15px;
  color: #6b7280;
  text-decoration: none;
  transition: background-color 0.2s, color 0.2s;

  &:hover {
    background-color: #f3f4f6;
    color: #374151;
  }
}

.nav-link-active {
  background-color: #eef2ff;
  color: #6366f1;
  font-weight: 500;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;

  .logo-text {
    font-size: 22px;
    font-weight: 700;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
  }
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 8px;
  transition: background-color 0.2s;

  &:hover {
    background-color: #f3f4f6;
  }

  .username {
    font-size: 14px;
    color: #374151;
    font-weight: 500;
  }

  .arrow-icon {
    font-size: 12px;
    color: #9ca3af;
  }
}

.main-content {
  flex: 1;
  padding: 24px 32px;
  max-width: 1400px;
  width: 100%;
  margin: 0 auto;
  overflow-x: auto; /* 允许内容区域横向滚动 */
}

// 过渡动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
