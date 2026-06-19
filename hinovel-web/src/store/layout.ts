import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

/**
 * 编辑器布局状态缓存 Key
 */
const LAYOUT_STORAGE_KEY = 'hinovel_editor_layout'

/**
 * 布局状态接口
 */
interface LayoutState {
    outlinePanelWidth: number
    functionPanelWidth: number
    activeFunctionTab: string | null
    isSidebarCollapsed: boolean
    isFocusMode: boolean
}

/**
 * 默认布局状态
 */
const DEFAULT_LAYOUT: LayoutState = {
    outlinePanelWidth: 460,
    functionPanelWidth: 400,
    activeFunctionTab: 'outline',
    isSidebarCollapsed: false,
    isFocusMode: false
}

/**
 * 编辑器布局状态管理。
 *
 * <p>将面板宽度、工具栏选择等布局信息缓存到 localStorage，
 * 下次进入页面时自动恢复，避免重复调整布局。</p>
 */
export const useLayoutStore = defineStore('layout', () => {
    /**
     * 从 localStorage 加载缓存的布局状态
     */
    const loadLayout = (): LayoutState => {
        try {
            const cached = localStorage.getItem(LAYOUT_STORAGE_KEY)
            if (cached) {
                return { ...DEFAULT_LAYOUT, ...JSON.parse(cached) }
            }
        } catch (e) {
            console.warn('加载布局缓存失败：', e)
        }
        return { ...DEFAULT_LAYOUT }
    }

    const cached = loadLayout()

    const outlinePanelWidth = ref(cached.outlinePanelWidth)
    const functionPanelWidth = ref(cached.functionPanelWidth)
    const activeFunctionTab = ref<string | null>(cached.activeFunctionTab)
    const isSidebarCollapsed = ref(cached.isSidebarCollapsed)
    const isFocusMode = ref(cached.isFocusMode)

    /**
     * 持久化当前布局状态到 localStorage
     */
    const persistLayout = () => {
        const state: LayoutState = {
            outlinePanelWidth: outlinePanelWidth.value,
            functionPanelWidth: functionPanelWidth.value,
            activeFunctionTab: activeFunctionTab.value,
            isSidebarCollapsed: isSidebarCollapsed.value,
            isFocusMode: isFocusMode.value
        }
        localStorage.setItem(LAYOUT_STORAGE_KEY, JSON.stringify(state))
    }

    // 监听所有布局状态变化，自动持久化
    watch(
        [outlinePanelWidth, functionPanelWidth, activeFunctionTab, isSidebarCollapsed, isFocusMode],
        persistLayout,
        { deep: true }
    )

    return {
        outlinePanelWidth,
        functionPanelWidth,
        activeFunctionTab,
        isSidebarCollapsed,
        isFocusMode,
        persistLayout
    }
})
