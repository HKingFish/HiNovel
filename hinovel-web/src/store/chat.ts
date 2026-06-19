import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { ChatSessionItem } from '@/api/ai'

/**
 * 前端问答消息（含撤回标记）
 */
export interface ChatMessageLocal {
    id?: number
    role: 'user' | 'assistant'
    content: string
    time: string
    revoked?: number
}

/**
 * AI 问答状态管理。
 *
 * <p>在页面切换时保持问答会话和消息状态，
 * 避免切换到其他工具或页面后问答断开。</p>
 */
export const useChatStore = defineStore('chat', () => {
    /**
     * 按小说 ID 缓存的问答状态
     */
    const chatStateMap = ref<Record<number, {
        sessionId: number
        sessions: ChatSessionItem[]
        messages: ChatMessageLocal[]
    }>>({})

    /**
     * 保存指定小说的问答状态
     *
     * @param novelId 小说 ID
     * @param sessionId 当前会话 ID
     * @param sessions 会话列表
     * @param messages 消息列表
     */
    const saveChatState = (
        novelId: number,
        sessionId: number,
        sessions: ChatSessionItem[],
        messages: ChatMessageLocal[]
    ) => {
        chatStateMap.value[novelId] = {
            sessionId,
            sessions: [...sessions],
            messages: [...messages]
        }
    }

    /**
     * 获取指定小说的问答状态
     *
     * @param novelId 小说 ID
     * @returns 缓存的问答状态，不存在时返回 null
     */
    const getChatState = (novelId: number) => {
        return chatStateMap.value[novelId] || null
    }

    /**
     * 清除指定小说的问答状态
     *
     * @param novelId 小说 ID
     */
    const clearChatState = (novelId: number) => {
        delete chatStateMap.value[novelId]
    }

    return {
        chatStateMap,
        saveChatState,
        getChatState,
        clearChatState
    }
})
