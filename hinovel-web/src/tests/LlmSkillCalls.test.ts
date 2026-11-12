import {flushPromises, mount} from '@vue/test-utils'
import {expect, it, vi} from 'vitest'
import http from '@/api/http'
import LlmCallLogView from '@/views/llm/LlmCallLogView.vue'

vi.mock('@/api/http', () => ({default: {get: vi.fn()}}))

it('在调用详情中按发起顺序展示 Skill 读取状态和可用错误码', async () => {
    const record = {
        id: '51',
        status: 'SUCCESS',
        skillReadCalls: [
            {sequence: 2, skillName: '冷峻悬疑', status: 'FAILED', errorCode: 'SKILL_READ_FAILED'},
            {sequence: 1, skillName: '去 AI 味', status: 'SUCCESS'},
        ],
    }
    vi.mocked(http.get)
        .mockResolvedValueOnce({data: {records: [record], total: 1}})
        .mockResolvedValueOnce({data: {successCount: 1, failCount: 0, totalTokens: 0, totalDuration: 0}})

    const view = mount(LlmCallLogView, {
        global: {
            stubs: {
                ElTable: {
                    props: ['data'],
                    emits: ['row-click'],
                    template: '<button @click="$emit(\'row-click\', data[0])">详情</button>',
                },
            },
        },
    })
    await flushPromises()
    expect(http.get).toHaveBeenCalledWith('/api/ai/llm-call-logs', expect.anything())
    await view.findAll('button').find(button => button.text() === '详情')!.trigger('click')
    const rows = view.get('ol').findAll('li')
    expect(rows[0].text()).toContain('去 AI 味')
    expect(rows[0].text()).toContain('成功')
    expect(rows[1].text()).toContain('冷峻悬疑')
    expect(rows[1].text()).toContain('失败')
    expect(rows[1].text()).toContain('SKILL_READ_FAILED')
    view.unmount()
})
