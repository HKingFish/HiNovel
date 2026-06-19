import http from './http'

// AI 改写相关接口
export interface AIAuthorRequest {
  chapterId: number
  content: string
  outline?: string
  plotPoints?: string
  characters?: number[]
  emotionTone?: string
  sceneSetting?: string
    userRequirement?: string
}

export interface AIAuthorResponse {
  id: string
  content: string
  suggestions?: string[]
  wordCount: number
  processingTime: number
}

export interface AIAuditorRequest {
  chapterId: number
  content: string
  novelOutline: string
  previousChapters?: {
    chapterId: number
    chapterNumber: number
    content: string
  }[]
}

export interface AIAuditIssue {
  id: string
  position: {
    start: number
    end: number
  }
  originalText: string
  issueType: 'consistency' | 'logic' | 'character' | 'plot'
  reason: string
  severity: 'low' | 'medium' | 'high'
  suggestion?: string
}

export interface AIAuditorResponse {
  id: string
  overallAssessment: 'pass' | 'warning' | 'fail'
  issues: AIAuditIssue[]
  summary: string
  processingTime: number
  auditorId?: string
  coveredChapters?: number[]
}

export interface AIAuditBatchRequest {
  chapterId: number
  content: string
  novelOutline: string
  chapterRanges: {
    startChapter: number
    endChapter: number
  }[]
}

export interface AIAuditBatchResponse {
  id: string
  audits: AIAuditorResponse[]
  overallAssessment: 'pass' | 'warning' | 'fail'
  summary: string
}

/**
 * AI 流式改写功能（SSE）。
 *
 * 通过 fetch 发起 POST 请求，接收 SSE 流式响应，
 * 逐 Token 回调 onToken，完成时回调 onComplete，异常时回调 onError。
 *
 * @param data 改写请求参数
 * @param callbacks SSE 回调函数集合
 * @returns AbortController 用于外部取消请求
 */
export const aiRewriteStream = (
    data: AIAuthorRequest,
    callbacks: {
        onToken: (token: string) => void
        onComplete: () => void
        onError: (error: Error) => void
    }
): AbortController => {
    const abortController = new AbortController()
    const baseUrl = import.meta.env.VITE_API_BASE_URL || ''
    const token = localStorage.getItem('accessToken')
    let hasErrored = false

    fetch(`${baseUrl}/api/agent/ai/rewrite`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'text/event-stream',
            ...(token ? {'satoken': token} : {})
        },
        body: JSON.stringify(data),
        signal: abortController.signal
    }).then(async (response) => {
        if (!response.ok) {
            callbacks.onError(new Error(`请求失败，状态码：${response.status}`))
            return
        }

        const reader = response.body?.getReader()
        if (!reader) {
            callbacks.onError(new Error('无法获取响应流'))
            return
        }

        const decoder = new TextDecoder()
        let buffer = ''
        const wrappedCallbacks = {
            ...callbacks,
            onError: (error: Error) => {
                hasErrored = true
                callbacks.onError(error)
            }
        }

        while (true) {
            const {done, value} = await reader.read()
            if (done) {
                if (buffer.trim()) {
                    processSSEBuffer(buffer, wrappedCallbacks)
                }
                if (!hasErrored) {
                    callbacks.onComplete()
                }
                break
            }

            buffer += decoder.decode(value, {stream: true})
            const frames = buffer.split('\n\n')
            buffer = frames.pop() || ''

            for (const frame of frames) {
                processSSEBuffer(frame, wrappedCallbacks)
            }
        }
    }).catch((error: Error) => {
        if (error.name !== 'AbortError') {
            callbacks.onError(error)
        }
    })

    return abortController
}

/**
 * 工具执行事件数据接口。
 *
 * <p>当 AI 问答中的 Agent 执行工具后，后端通过 SSE 推送此事件，
 * 前端据此触发 UI 更新（如刷新章节列表、加载新内容等）。</p>
 */
export interface ToolExecutedEvent {
    toolName: string
    novelId: number
}

/**
 * 处理单个 SSE 帧。
 *
 * <p>SSE 协议中，同一帧内多个 data: 行需用 \n 拼接还原原始内容。
 * 支持识别 error 事件和 tool-executed 事件。</p>
 *
 * @param frame SSE 帧文本
 * @param callbacks 回调函数集合
 */
const processSSEBuffer = (
    frame: string,
    callbacks: {
        onToken: (token: string) => void
        onError: (error: Error) => void
        onToolExecuted?: (event: ToolExecutedEvent) => void
    }
) => {
    const lines = frame.split('\n')

    // 检查是否为错误事件
    if (lines.some(line => line.startsWith('event:error') || line.startsWith('event: error'))) {
        const dataLine = lines.find(l => l.startsWith('data:'))
        if (dataLine) {
            callbacks.onError(new Error(extractDataContent(dataLine)))
        }
        return
    }

    // 检查是否为工具执行事件
    if (lines.some(line =>
        line.startsWith('event:tool-executed') || line.startsWith('event: tool-executed')
    )) {
        const dataLine = lines.find(l => l.startsWith('data:'))
        if (dataLine && callbacks.onToolExecuted) {
            try {
                const eventData: ToolExecutedEvent = JSON.parse(extractDataContent(dataLine))
                callbacks.onToolExecuted(eventData)
            } catch (e) {
                console.warn('解析工具执行事件失败：', e)
            }
        }
        return
    }

    // 收集同一帧内所有 data: 行，用 \n 拼接还原原始内容
    const dataLines: string[] = []
    for (const line of lines) {
        if (line.startsWith('data:')) {
            dataLines.push(extractDataContent(line))
        }
    }

    if (dataLines.length > 0) {
        const content = dataLines.join('\n')
        callbacks.onToken(content)
    }
}

/**
 * 从 SSE data: 行中提取内容。
 *
 * <p>SSE 标准格式为 "data: xxx"（冒号后有一个空格），
 * 此方法兼容有空格和无空格两种格式。</p>
 *
 * @param line SSE data: 行
 * @returns 提取的内容
 */
const extractDataContent = (line: string): string => {
    // 去掉 "data:" 前缀，如果紧跟一个空格则一并去掉
    const afterPrefix = line.slice(5)
    if (afterPrefix.startsWith(' ')) {
        return afterPrefix.slice(1)
    }
    return afterPrefix
}

// AI 审核功能
export const aiAudit = async (data: AIAuditorRequest): Promise<AIAuditorResponse> => {
    const res = await http.post('/api/agent/ai/audit', data)
  return res.data
}

// 批量审核功能（处理大纲过长的情况）
export const aiBatchAudit = async (data: AIAuditBatchRequest): Promise<AIAuditBatchResponse> => {
    const res = await http.post('/api/agent/ai/audit/batch', data)
  return res.data
}

// 重新生成内容
export const aiRegenerate = async (data: AIAuthorRequest): Promise<AIAuthorResponse> => {
    const res = await http.post('/api/agent/ai/regenerate', data)
  return res.data
}

// ========== AI 问答接口 ==========

/**
 * 问答会话类型
 */
export interface ChatSessionItem {
    id: number
    novelId: number
    title: string
    favorite: number
    createTime: string
    updateTime: string
}

/**
 * 问答消息类型
 */
export interface ChatMessageItem {
    id: number
    sessionId: number
    role: 'user' | 'assistant'
    content: string
    revoked: number
    createTime: string
}

/**
 * 获取小说的问答会话列表
 */
export const listChatSessions = (novelId: number) =>
    http.get<never, { data: ChatSessionItem[] }>(`/api/agent/ai/chat/sessions/${novelId}`)

/**
 * 创建新的问答会话
 */
export const createChatSession = (novelId: number, title?: string) =>
    http.post<never, { data: ChatSessionItem }>('/api/agent/ai/chat/sessions', {novelId, title})

/**
 * 删除问答会话
 */
export const deleteChatSession = (sessionId: number) =>
    http.delete(`/api/agent/ai/chat/sessions/${sessionId}`)

/**
 * 重命名问答会话
 */
export const renameChatSession = (sessionId: number, title: string) =>
    http.put(`/api/agent/ai/chat/sessions/${sessionId}/rename`, {title})

/**
 * 切换问答会话收藏状态
 */
export const toggleChatSessionFavorite = (sessionId: number) =>
    http.put<never, { data: boolean }>(`/api/agent/ai/chat/sessions/${sessionId}/favorite`)

/**
 * 保存中断的 AI 回复（用于用户主动停止问答场景）
 */
export const savePartialChatResponse = (sessionId: number, novelId: number, content: string) =>
    http.post('/api/agent/ai/chat/save-partial', {sessionId, novelId, content})

/**
 * 获取会话的消息列表（全量，兼容旧接口）
 */
export const listChatMessages = (sessionId: number) =>
    http.get<never, { data: ChatMessageItem[] }>(`/api/agent/ai/chat/messages/${sessionId}`)

/**
 * 消息分页响应
 */
export interface ChatMessagePageResult {
    messages: ChatMessageItem[]
    hasMore: boolean
}

/**
 * 分页获取会话消息（游标分页，用于滚动加载历史消息）
 *
 * @param sessionId 会话 ID
 * @param cursorId 游标消息 ID（null 表示从最新开始）
 * @param pageSize 每页条数
 */
export const listChatMessagesPage = (sessionId: number, cursorId?: number, pageSize = 30) => {
    const params: Record<string, number> = { pageSize }
    if (cursorId) {
        params.cursorId = cursorId
    }
    return http.get<never, { data: ChatMessagePageResult }>(
        `/api/agent/ai/chat/messages/${sessionId}/page`, {params}
    )
}

/**
 * 撤回问答消息（撤回后不再参与 LLM 上下文构建）
 */
export const revokeChatMessage = (messageId: number) =>
    http.put(`/api/agent/ai/chat/messages/${messageId}/revoke`)

/**
 * AI 流式问答（SSE）。
 *
 * 通过 fetch 发起 POST 请求，接收 SSE 流式响应，
 * 逐 Token 回调 onToken，工具执行完成时回调 onToolExecuted，
 * 完成时回调 onComplete，异常时回调 onError。
 *
 * @param data 问答请求参数
 * @param callbacks SSE 回调函数集合
 * @returns AbortController 用于外部取消请求
 */
export const aiChatStream = (
    data: { novelId: number; sessionId: number; question: string },
    callbacks: {
        onToken: (token: string) => void
        onComplete: () => void
        onError: (error: Error) => void
        onToolExecuted?: (event: ToolExecutedEvent) => void
    }
): AbortController => {
    const abortController = new AbortController()
    const baseUrl = import.meta.env.VITE_API_BASE_URL || ''
    const token = localStorage.getItem('accessToken')
    // 标记是否已通过 SSE error 事件触发过 onError
    let hasErrored = false

    fetch(`${baseUrl}/api/agent/ai/chat`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'text/event-stream',
            ...(token ? {'satoken': token} : {})
        },
        body: JSON.stringify(data),
        signal: abortController.signal
    }).then(async (response) => {
        if (!response.ok) {
            callbacks.onError(new Error(`请求失败，状态码：${response.status}`))
            return
        }

        const reader = response.body?.getReader()
        if (!reader) {
            callbacks.onError(new Error('无法获取响应流'))
            return
        }

        const decoder = new TextDecoder()
        let buffer = ''
        // 包装 onError，设置标志避免 onComplete 再次触发
        const wrappedCallbacks = {
            ...callbacks,
            onError: (error: Error) => {
                hasErrored = true
                callbacks.onError(error)
            }
        }

        while (true) {
            let done: boolean
            let value: Uint8Array | undefined
            try {
                const result = await reader.read()
                done = result.done
                value = result.value
            } catch (readError) {
                // 连接中断（后端超时、网络异常等）
                if (!hasErrored) {
                    callbacks.onError(new Error('连接中断，请稍后重试'))
                }
                break
            }
            if (done) {
                if (buffer.trim()) {
                    processSSEBuffer(buffer, wrappedCallbacks)
                }
                if (!hasErrored) {
                    callbacks.onComplete()
                }
                break
            }

            buffer += decoder.decode(value, {stream: true})
            const frames = buffer.split('\n\n')
            buffer = frames.pop() || ''

            for (const frame of frames) {
                processSSEBuffer(frame, wrappedCallbacks)
            }
        }
    }).catch((error: Error) => {
        if (error.name !== 'AbortError') {
            callbacks.onError(error)
        }
    })

    return abortController
}
