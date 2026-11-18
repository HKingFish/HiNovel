import http from './http'

/**
 * 章节批注接口定义
 */
export interface ChapterAnnotation {
    id: string
    chapterId: string
    novelId: string
    userId: string
    startOffset: number
    endOffset: number
    originalText: string
    annotationContent: string
    aiRewriteResult?: string
    annotationType: string
    status: string
    viewed: number
    createTime: string
    updateTime: string
}

/**
 * 创建批注请求
 */
export interface CreateAnnotationRequest {
    chapterId: string
    novelId: string
    startOffset: number
    endOffset: number
    originalText: string
    annotationContent: string
}

/**
 * 获取章节的所有批注
 *
 * @param chapterId 章节 ID
 */
export const getChapterAnnotations = (chapterId: string) =>
    http.get<never, { data: ChapterAnnotation[] }>(`/api/novel/annotations/chapter/${chapterId}`)

/**
 * 创建批注
 *
 * @param data 创建批注请求
 */
export const createAnnotation = (data: CreateAnnotationRequest) =>
    http.post<never, { data: ChapterAnnotation }>('/api/novel/annotations', data)

/**
 * 更新批注内容
 *
 * @param annotationId 批注 ID
 * @param annotationContent 新的批注内容
 */
export const updateAnnotation = (annotationId: string, annotationContent: string) =>
    http.put<never, { data: void }>(`/api/novel/annotations/${annotationId}`, {annotationContent})

/**
 * 保存 AI 改写结果
 *
 * @param annotationId 批注 ID
 * @param rewriteResult AI 改写结果
 */
export const saveAiRewriteResult = (annotationId: string, rewriteResult: string) =>
    http.put<never, { data: void }>(`/api/novel/annotations/${annotationId}/ai-rewrite`, {rewriteResult})

/**
 * 采纳批注
 *
 * @param annotationId 批注 ID
 */
export const acceptAnnotation = (annotationId: string) =>
    http.post<never, { data: void }>(`/api/novel/annotations/${annotationId}/accept`)

/**
 * 删除批注
 *
 * @param annotationId 批注 ID
 */
export const deleteAnnotation = (annotationId: string) =>
    http.delete<never, { data: void }>(`/api/novel/annotations/${annotationId}`)

/**
 * 标记批注为已查看
 *
 * @param annotationId 批注 ID
 */
export const markAnnotationViewed = (annotationId: string) =>
    http.post<never, { data: void }>(`/api/novel/annotations/${annotationId}/view`)

/**
 * 标记批注为已处理
 *
 * @param annotationId 批注 ID
 */
export const resolveAnnotation = (annotationId: string) =>
    http.post<never, { data: void }>(`/api/novel/annotations/${annotationId}/resolve`)


/**
 * 批注 AI 改写请求
 */
export interface AnnotationRewriteRequest {
    /** 批注 ID（快捷改写时可为 0 或不传） */
    annotationId?: string
    /** 章节 ID */
    chapterId: string
    /** 小说 ID（快捷改写模式使用） */
    novelId?: string
    /** 整章正文内容（保证改写连贯性） */
    fullChapterContent: string
    /** 被批注的原文片段 */
    originalText: string
    /** 批注意见（作为改写指导） */
    annotationContent: string
}

export interface AnnotationRewriteCallbacks {
    onToken: (token: string) => void
    onComplete: (fullText: string) => void | Promise<void>
    onError: (error: Error) => void
}

/** 接收完整的批注改写事件，并保留正文中的空白。 */
export const annotationAiRewriteStream = async (
    data: AnnotationRewriteRequest,
    callbacks: AnnotationRewriteCallbacks,
    signal: AbortSignal
): Promise<void> => {
    const baseUrl = import.meta.env.VITE_API_BASE_URL || ''
    const token = localStorage.getItem('accessToken')

    try {
        const response = await fetch(`${baseUrl}/api/novel/annotations/ai-rewrite-stream`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'text/event-stream',
                ...(token ? {'satoken': token} : {})
            },
            body: JSON.stringify(data),
            signal
        })
        if (!response.ok) {
            throw new Error(`请求失败：${response.status}`)
        }
        const reader = response.body?.getReader()
        if (!reader) {
            throw new Error('无法获取响应流')
        }

        const decoder = new TextDecoder()
        let buffer = ''
        let fullText = ''
        try {
            while (!signal.aborted) {
                const {done, value} = await reader.read()
                if (signal.aborted) return
                if (done) break

                buffer += decoder.decode(value, {stream: true})
                const frames = buffer.split(/\r?\n\r?\n/)
                buffer = frames.pop() ?? ''
                for (const frame of frames) {
                    const lines = frame.split(/\r?\n/)
                    const event = lines.find(line => line.startsWith('event:'))?.slice(6).trim()
                    const dataLines = lines.filter(line => line.startsWith('data:'))
                    if (!dataLines.length) continue

                    // Spring 紧接 data: 写入正文，冒号后的空格属于改写内容。
                    const content = dataLines.map(line => line.slice(5)).join('\n')
                    if (event === 'error') {
                        throw new Error(content || 'AI 改写失败')
                    }
                    if (content === '[DONE]') {
                        await callbacks.onComplete(fullText)
                        return
                    }
                    fullText += content
                    callbacks.onToken(content)
                    if (signal.aborted) return
                }
            }
            if (!signal.aborted) {
                throw new Error('改写连接中断，请稍后重试')
            }
        } finally {
            await reader.cancel().catch(() => {})
            reader.releaseLock()
        }
    } catch (error) {
        if (!signal.aborted) {
            callbacks.onError(error instanceof Error ? error : new Error('AI 改写失败'))
        }
    }
}
