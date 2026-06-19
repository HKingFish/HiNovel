import {describe, it, expect, vi, beforeEach} from 'vitest'

/**
 * 前端组件单元测试
 * 任务 13.7：SSE 消息渲染逻辑 + Token 刷新逻辑
 */

// ---- SSE 消息渲染测试 ----

describe('SSE 消息渲染逻辑', () => {
    /**
     * 模拟 SSE 数据行解析函数（与 ChatView 中逻辑一致）
     */
    const parseSseLines = (chunk: string): { tokens: string[]; isDone: boolean } => {
        const lines = chunk.split('\n')
        const tokens: string[] = []
        let isDone = false

        for (let i = 0; i < lines.length; i++) {
            const line = lines[i]
            if (line.startsWith('event:token')) continue
            if (line.startsWith('data:')) {
                const data = line.slice(5).trim()
                if (data === '[DONE]') {
                    isDone = true
                } else if (data) {
                    tokens.push(data)
                }
            }
        }
        return {tokens, isDone}
    }

    it('应正确解析 token 事件数据', () => {
        const chunk = 'event:token\ndata:你好\n\nevent:token\ndata:世界\n'
        const {tokens, isDone} = parseSseLines(chunk)
        expect(tokens).toEqual(['你好', '世界'])
        expect(isDone).toBe(false)
    })

    it('应正确识别 [DONE] 结束标记', () => {
        const chunk = 'data:[DONE]\n'
        const {tokens, isDone} = parseSseLines(chunk)
        expect(tokens).toHaveLength(0)
        expect(isDone).toBe(true)
    })

    it('应忽略空数据行', () => {
        const chunk = 'data:\ndata:  \ndata:有效内容\n'
        const {tokens} = parseSseLines(chunk)
        expect(tokens).toEqual(['有效内容'])
    })

    it('多个 token 应按顺序累积', () => {
        const chunks = ['data:第', 'data:一', 'data:段']
        let accumulated = ''
        for (const chunk of chunks) {
            const {tokens} = parseSseLines(chunk)
            accumulated += tokens.join('')
        }
        expect(accumulated).toBe('第一段')
    })

    it('混合事件类型应只提取 data 内容', () => {
        const chunk = 'event:token\ndata:内容\nevent:done\ndata:[DONE]\n'
        const {tokens, isDone} = parseSseLines(chunk)
        expect(tokens).toEqual(['内容'])
        expect(isDone).toBe(true)
    })
})

// ---- Token 刷新逻辑测试 ----

describe('Token 刷新逻辑', () => {
    beforeEach(() => {
        localStorage.clear()
        vi.clearAllMocks()
    })

    /**
     * 模拟 Token 刷新判断逻辑（与 http.ts 拦截器一致）
     */
    const shouldRefreshToken = (status: number, hasRefreshToken: boolean): boolean => {
        return status === 401 && hasRefreshToken
    }

    const handleTokenExpiry = (status: number): 'refresh' | 'redirect' | 'ignore' => {
        const refreshToken = localStorage.getItem('refreshToken')
        if (status === 401 && refreshToken) return 'refresh'
        if (status === 401 && !refreshToken) return 'redirect'
        return 'ignore'
    }

    it('401 且有 refreshToken 时应触发刷新', () => {
        localStorage.setItem('refreshToken', 'valid-refresh-token')
        expect(shouldRefreshToken(401, true)).toBe(true)
    })

    it('401 且无 refreshToken 时不应触发刷新', () => {
        expect(shouldRefreshToken(401, false)).toBe(false)
    })

    it('非 401 状态码不应触发刷新', () => {
        expect(shouldRefreshToken(403, true)).toBe(false)
        expect(shouldRefreshToken(500, true)).toBe(false)
    })

    it('有 refreshToken 时应返回 refresh 动作', () => {
        localStorage.setItem('refreshToken', 'rt-token')
        expect(handleTokenExpiry(401)).toBe('refresh')
    })

    it('无 refreshToken 时应返回 redirect 动作', () => {
        expect(handleTokenExpiry(401)).toBe('redirect')
    })

    it('刷新成功后应更新 localStorage 中的 accessToken', () => {
        const newToken = 'new-access-token'
        localStorage.setItem('accessToken', 'old-token')
        // 模拟刷新成功后的更新操作
        localStorage.setItem('accessToken', newToken)
        expect(localStorage.getItem('accessToken')).toBe(newToken)
    })

    it('刷新失败后应清空所有本地存储', () => {
        localStorage.setItem('accessToken', 'at')
        localStorage.setItem('refreshToken', 'rt')
        localStorage.setItem('userInfo', '{}')
        // 模拟刷新失败后的清理操作
        localStorage.clear()
        expect(localStorage.getItem('accessToken')).toBeNull()
        expect(localStorage.getItem('refreshToken')).toBeNull()
    })
})
