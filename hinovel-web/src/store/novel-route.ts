import {defineStore} from 'pinia'
import {ref} from 'vue'

/**
 * 小说路由状态管理
 * 记录用户最后访问的小说创作页路由，用于导航栏"我的小说"菜单恢复创作页状态
 */
export const useNovelRouteStore = defineStore('novelRoute', () => {
    // 最后访问的小说创作页完整路径（如 /novel/123）
    const lastNovelPath = ref<string | null>(null)

    /**
     * 记录当前访问的小说创作页路径
     *
     * @param path 创作页完整路径
     */
    const setLastNovelPath = (path: string) => {
        lastNovelPath.value = path
    }

    /**
     * 清除记录的创作页路径
     */
    const clearLastNovelPath = () => {
        lastNovelPath.value = null
    }

    /**
     * 获取"我的小说"菜单应跳转的目标路径
     * 如果有记录的创作页路径则返回该路径，否则返回小说列表页
     */
    const novelMenuPath = () => {
        return lastNovelPath.value || '/novel'
    }

    return {
        lastNovelPath,
        setLastNovelPath,
        clearLastNovelPath,
        novelMenuPath
    }
})
