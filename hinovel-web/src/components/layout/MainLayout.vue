<template>
    <div class="main-layout">
        <!-- 顶部导航栏 -->
        <header class="top-header">
            <div class="header-left">
                <router-link class="logo" to="/novel">
                    <el-icon :size="21" class="brand-mark">
                        <Reading />
                    </el-icon>
                    <span class="logo-text">HiNovel</span>
                </router-link>
                <nav aria-label="主导航" class="header-nav">
                    <router-link
                        :class="{ 'nav-link-active': isNovelRouteActive }"
                        :to="novelMenuTarget"
                        class="nav-link"
                    >
                        <el-icon>
                            <Reading />
                        </el-icon>
                        <span>{{ novelMenuTarget === '/novel' ? '我的书架' : '继续写作' }}</span>
                    </router-link>
                    <router-link active-class="nav-link-active" class="nav-link" to="/agents">
                        <el-icon>
                            <User />
                        </el-icon>
                        <span>Agent 管理</span>
                    </router-link>
                    <router-link active-class="nav-link-active" class="nav-link" to="/llm-providers">
                        <el-icon>
                            <Cpu />
                        </el-icon>
                        <span>模型管理</span>
                    </router-link>
                    <router-link active-class="nav-link-active" class="nav-link" to="/llm-call-logs">
                        <el-icon>
                            <Document />
                        </el-icon>
                        <span>调用记录</span>
                    </router-link>
                </nav>
            </div>
            <div class="header-right">
                <el-dropdown trigger="click" @command="handleCommand">
                    <button aria-label="账户菜单" class="user-info" type="button">
                        <el-avatar :size="32" :src="authStore.user?.avatarUrl">
                            <el-icon><UserFilled /></el-icon>
                        </el-avatar>
                        <span class="username">{{ authStore.user?.username || '用户' }}</span>
                        <el-icon class="arrow-icon"><ArrowDown /></el-icon>
                    </button>
                    <template #dropdown>
                        <el-dropdown-menu>
                            <el-dropdown-item command="settings">
                                <el-icon>
                                    <Setting />
                                </el-icon>
                                <span>默认设置</span>
                            </el-dropdown-item>
                            <el-dropdown-item command="logout" divided>
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
                    <component :is="Component" :key="route.fullPath" />
                </keep-alive>
            </router-view>
        </main>
    </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown, Cpu, Document, Reading, Setting, SwitchButton, User, UserFilled } from '@element-plus/icons-vue'
import { useAuthStore } from '@/store/auth'
import { useNovelRouteStore } from '@/store/novel-route'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const novelRouteStore = useNovelRouteStore()

// "我的小说"菜单跳转目标：优先恢复到上次的创作页
const novelMenuTarget = computed(() => (route.path.startsWith('/novel') ? '/novel' : novelRouteStore.novelMenuPath()))

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
                type: 'warning',
            })
        } catch {
            return
        }
        try {
            await authStore.logout()
            ElMessage.success('已退出登录')
        } catch {
            // 登出请求的失败提示由 HTTP 层处理，本地会话已清除。
        } finally {
            await router.replace('/login')
        }
    }
}
</script>

<style scoped>
.main-layout {
    min-height: 100vh;
}
.top-header {
    height: var(--shell-height);
    position: sticky;
    top: 0;
    z-index: 100;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 20px;
    padding: 0 clamp(18px, 4vw, 72px);
    background: var(--surface);
    border-bottom: 1px solid var(--rule);
}

.header-left,
.header-nav,
.logo,
.nav-link,
.user-info {
    display: flex;
    align-items: center;
}

.header-left {
    gap: clamp(26px, 5vw, 80px);
    height: 100%;
    min-width: 0;
}

.logo {
    gap: 9px;
    text-decoration: none;
    color: var(--ink);
}

.brand-mark {
    box-sizing: content-box;
    padding: 6px;
    background: var(--pine);
    color: white;
    border-radius: 3px 8px 3px 3px;
}

.logo-text {
    font:
        bold 27px/1 Georgia,
        serif;
    letter-spacing: -1.5px;
}

.header-nav {
    align-self: stretch;
    gap: clamp(12px, 2.4vw, 38px);
}

.nav-link {
    position: relative;
    height: 100%;
    gap: 6px;
    color: var(--muted);
    text-decoration: none;
    white-space: nowrap;
    font-size: 14px;
}

.nav-link:hover,
.nav-link-active {
    color: var(--pine);
}

.nav-link-active {
    font-weight: 650;
}

.nav-link-active::after {
    content: '';
    position: absolute;
    bottom: 0;
    inset-inline: 0;
    height: 3px;
    background: var(--pine);
}

.user-info {
    background: none;
    border: 0;
    color: var(--ink);
    gap: 8px;
    cursor: pointer;
}

.user-info :deep(.el-avatar) {
    background: var(--sage);
    color: var(--pine);
}

.username {
    max-width: 12ch;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.main-content {
    width: min(100%, 1416px);
    padding: clamp(20px, 3.4vw, 52px) clamp(16px, 4vw, 70px) 48px;
    margin-inline: auto;
}

@media (max-width: 700px) {
    .top-header {
        display: grid;
        grid-template: 50px 46px / 1fr auto;
        gap: 0;
    }

    .header-left {
        display: contents;
    }

    .logo {
        grid-area: 1 / 1;
        justify-self: start;
    }

    .header-right {
        grid-area: 1 / 2;
    }

    .header-nav {
        grid-area: 2 / 1 / 3 / 3;
        justify-content: space-between;
        gap: 6px;
    }

    .nav-link {
        font-size: 13px;
        gap: 4px;
    }
}
</style>
