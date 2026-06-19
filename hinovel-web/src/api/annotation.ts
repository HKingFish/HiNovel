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
    chapterId: number
    novelId: number
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
export const getChapterAnnotations = (chapterId: number) =>
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
export const updateAnnotation = (annotationId: number, annotationContent: string) =>
    http.put<never, { data: void }>(`/api/novel/annotations/${annotationId}`, {annotationContent})

/**
 * 保存 AI 改写结果
 *
 * @param annotationId 批注 ID
 * @param rewriteResult AI 改写结果
 */
export const saveAiRewriteResult = (annotationId: number, rewriteResult: string) =>
    http.put<never, { data: void }>(`/api/novel/annotations/${annotationId}/ai-rewrite`, {rewriteResult})

/**
 * 采纳批注
 *
 * @param annotationId 批注 ID
 */
export const acceptAnnotation = (annotationId: number) =>
    http.post<never, { data: void }>(`/api/novel/annotations/${annotationId}/accept`)

/**
 * 删除批注
 *
 * @param annotationId 批注 ID
 */
export const deleteAnnotation = (annotationId: number) =>
    http.delete<never, { data: void }>(`/api/novel/annotations/${annotationId}`)

/**
 * 标记批注为已查看
 *
 * @param annotationId 批注 ID
 */
export const markAnnotationViewed = (annotationId: number) =>
    http.post<never, { data: void }>(`/api/novel/annotations/${annotationId}/view`)

/**
 * 标记批注为已处理
 *
 * @param annotationId 批注 ID
 */
export const resolveAnnotation = (annotationId: number) =>
    http.post<never, { data: void }>(`/api/novel/annotations/${annotationId}/resolve`)


/**
 * 批注 AI 改写请求
 */
export interface AnnotationRewriteRequest {
    /** 批注 ID（快捷改写时可为 0 或不传） */
    annotationId?: number
    /** 章节 ID */
    chapterId: number
    /** 小说 ID（快捷改写模式使用） */
    novelId?: number
    /** 整章正文内容（保证改写连贯性） */
    fullChapterContent: string
    /** 被批注的原文片段 */
    originalText: string
    /** 批注意见（作为改写指导） */
    annotationContent: string
}

/**
 * 批注 AI 改写（流式）
 * 将整章内容 + 原文片段 + 批注意见发送给 AI，流式返回改写结果
 *
 * @param data 改写请求
 * @param callbacks 流式回调
 * @returns AbortController 用于取消请求
 */
export const annotationAiRewriteStream = (
    data: AnnotationRewriteRequest,
    callbacks: {
        onToken: (token: string) => void
        onComplete: (fullText: string) => void
        onError: (error: Error) => void
    }
): AbortController => {
    const abortController = new AbortController()

    fetch('/api/novel/annotations/ai-rewrite-stream', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(data),
        signal: abortController.signal
    }).then(async (response) => {
        if (!response.ok) {
            throw new Error(`请求失败：${response.status}`)
        }
        const reader = response.body?.getReader()
        if (!reader) {
            throw new Error('无法获取响应流')
        }

        const decoder = new TextDecoder()
        let fullText = ''

        while (true) {
            const {done, value} = await reader.read()
            if (done) break

            const chunk = decoder.decode(value, {stream: true})
            const lines = chunk.split('\n')

            for (const line of lines) {
                if (line.startsWith('data:')) {
                    const content = line.slice(5).trim()
                    if (content === '[DONE]') {
                        callbacks.onComplete(fullText)
                        return
                    }
                    fullText += content
                    callbacks.onToken(content)
                }
            }
        }

        callbacks.onComplete(fullText)
    }).catch((error) => {
        if (error.name !== 'AbortError') {
            callbacks.onError(error)
        }
    })

    return abortController
}
