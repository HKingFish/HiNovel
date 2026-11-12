import {afterEach, describe, expect, it, vi} from 'vitest'
import http from '@/api/http'
import {aiChatStream, listChatMessages, listChatMessagesPage} from '@/api/ai'

vi.mock('@/api/http', () => ({default: {get: vi.fn()}}))

afterEach(() => {
    vi.unstubAllGlobals()
    vi.resetAllMocks()
})

function runChat(frames: string[]) {
    const stream = new ReadableStream<Uint8Array>({
        start(controller) {
            for (const frame of frames) controller.enqueue(new TextEncoder().encode(frame))
            controller.close()
        },
    })
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response(stream)))
    const onToken = vi.fn()
    const onSkillLoaded = vi.fn()
    const onToolExecuted = vi.fn()
    const onComplete = vi.fn()
    const onError = vi.fn()
    aiChatStream({novelId: '9007199254740993', sessionId: '9007199254740995', question: '用冷峻悬疑风格写作'}, {
        onToken, onSkillLoaded, onToolExecuted, onComplete, onError,
    })
    return {onToken, onSkillLoaded, onToolExecuted, onComplete, onError}
}

describe('聊天 Skill 加载记录', () => {
    it('从分片流读取成功名称，保留正文及工具事件并忽略未知事件', async () => {
        const callbacks = runChat([
            'event: skill-loa',
            'ded\r\ndata: {"skillName":"去 AI 味"}\r\n\r\nevent: future\r\ndata: ignored\r\n\r\n',
            'event: skill-loaded\ndata: {"skillName":"冷峻悬疑"}\n\n',
            'event: tool-executed\ndata: {"toolName":"saveNovelOutline","novelId":1}\n\n',
            'event: token\ndata: 正文\n\n',
        ])

        await vi.waitFor(() => expect(callbacks.onComplete).toHaveBeenCalledOnce())
        expect(fetch).toHaveBeenCalledWith('/api/agent/ai/chat', expect.objectContaining({
            body: '{"novelId":9007199254740993,"sessionId":9007199254740995,"question":"用冷峻悬疑风格写作"}',
        }))
        expect(callbacks.onSkillLoaded.mock.calls).toEqual([['去 AI 味'], ['冷峻悬疑']])
        expect(callbacks.onToolExecuted).toHaveBeenCalledWith({toolName: 'saveNovelOutline', novelId: 1})
        expect(callbacks.onToken.mock.calls).toEqual([['正文']])
    })

    it('风格读取失败时保留正文中的提示，且会话错误仍走原有错误通道', async () => {
        const success = runChat([
            'event: skill-loaded\ndata: {"skillName":"去 AI 味"}\n\n',
            'event: token\ndata: 正文。本次未加载冷峻悬疑 Skill。\n\n',
        ])
        await vi.waitFor(() => expect(success.onComplete).toHaveBeenCalledOnce())
        expect(success.onSkillLoaded.mock.calls).toEqual([['去 AI 味']])
        expect(success.onToken).toHaveBeenCalledWith('正文。本次未加载冷峻悬疑 Skill。')

        const failure = runChat(['event: error\ndata: [1003003002] 会话不存在\n\n'])
        await vi.waitFor(() => expect(failure.onError).toHaveBeenCalledOnce())
        expect(failure.onError.mock.calls[0][0].message).toContain('1003003002')
        expect(failure.onComplete).not.toHaveBeenCalled()
        expect(failure.onSkillLoaded).not.toHaveBeenCalled()
    })

    it('在全量和分页历史中保留成功名称并兼容旧消息', async () => {
        const messages = [
            {id: '40', role: 'assistant', loadedSkillNames: null},
            {id: '41', role: 'user'},
            {id: '42', role: 'assistant', loadedSkillNames: ['去 AI 味', '冷峻悬疑']},
        ]
        vi.mocked(http.get)
            .mockResolvedValueOnce({data: messages})
            .mockResolvedValueOnce({data: {messages, hasMore: true}})
        const expected = [[], [], ['去 AI 味', '冷峻悬疑']]

        const full = await listChatMessages(2)
        const page = await listChatMessagesPage(2, 44)

        expect(http.get).toHaveBeenNthCalledWith(1, '/api/agent/ai/chat/messages/2')
        expect(http.get).toHaveBeenNthCalledWith(2, '/api/agent/ai/chat/messages/2/page', {params: {pageSize: 30, cursorId: 44}})
        expect(full.data.map(message => message.loadedSkillNames)).toEqual(expected)
        expect(page.data.messages.map(message => message.loadedSkillNames)).toEqual(expected)
        expect(page.data.hasMore).toBe(true)
    })
})
