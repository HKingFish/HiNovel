import {afterEach, expect, it, vi} from 'vitest'
import {annotationAiRewriteStream} from '@/api/annotation'

vi.mock('@/api/http', () => ({default: {}}))

const request = {
    annotationId: '9007199254740995',
    chapterId: '9007199254740993',
    fullChapterContent: '整章内容',
    originalText: '原文',
    annotationContent: '改写要求',
}

const mockStream = (chunks: string[]) => {
    const stream = new ReadableStream<Uint8Array>({
        start(controller) {
            const encoder = new TextEncoder()
            for (const chunk of chunks) controller.enqueue(encoder.encode(chunk))
            controller.close()
        },
    })
    const fetchMock = vi.fn().mockResolvedValue(new Response(stream))
    vi.stubGlobal('fetch', fetchMock)
    return fetchMock
}

afterEach(() => {
    localStorage.removeItem('accessToken')
    vi.unstubAllGlobals()
})

it('带上登录令牌和完整 ID，并保留跨分片改写帧里的空白', async () => {
    localStorage.setItem('accessToken', 'test-token')
    const fetchMock = mockStream(['da', 'ta: 前半\r\ndata:后半  \r\n\r', '\ndata:[DONE]\n\n'])
    const callbacks = {onToken: vi.fn(), onComplete: vi.fn(), onError: vi.fn()}

    await annotationAiRewriteStream(request, callbacks, new AbortController().signal)

    expect(fetchMock).toHaveBeenCalledWith('/api/novel/annotations/ai-rewrite-stream', expect.objectContaining({
        body: JSON.stringify(request),
        headers: expect.objectContaining({satoken: 'test-token'}),
    }))
    expect(callbacks.onToken.mock.calls).toEqual([[' 前半\n后半  ']])
    expect(callbacks.onComplete.mock.calls).toEqual([[' 前半\n后半  ']])
    expect(callbacks.onError).not.toHaveBeenCalled()
})

it.each([
    {frames: ['data:已生成\n\nevent:error\ndata:模型不可用\n\n'], message: '模型不可用'},
    {frames: ['data:已生成\n\n'], message: '改写连接中断，请稍后重试'},
])('收到 $message 时结束流并报告错误', async ({frames, message}) => {
    mockStream(frames)
    const callbacks = {onToken: vi.fn(), onComplete: vi.fn(), onError: vi.fn()}

    await annotationAiRewriteStream(request, callbacks, new AbortController().signal)

    expect(callbacks.onToken.mock.calls).toEqual([['已生成']])
    expect(callbacks.onComplete).not.toHaveBeenCalled()
    expect(callbacks.onError.mock.calls).toEqual([[new Error(message)]])
})

it('取消后忽略同一网络分片里其余事件', async () => {
    mockStream(['data:第一段\n\ndata:第二段\n\ndata:[DONE]\n\n'])
    const controller = new AbortController()
    const callbacks = {
        onToken: vi.fn(() => controller.abort()),
        onComplete: vi.fn(),
        onError: vi.fn(),
    }

    await annotationAiRewriteStream(request, callbacks, controller.signal)

    expect(callbacks.onToken.mock.calls).toEqual([['第一段']])
    expect(callbacks.onComplete).not.toHaveBeenCalled()
    expect(callbacks.onError).not.toHaveBeenCalled()
})
