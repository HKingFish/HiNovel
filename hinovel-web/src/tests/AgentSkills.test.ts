import {flushPromises, mount, type VueWrapper} from '@vue/test-utils'
import {createPinia, setActivePinia} from 'pinia'
import {beforeEach, describe, expect, it, vi} from 'vitest'
import http from '@/api/http'
import AgentView from '@/views/agent/AgentView.vue'

vi.mock('@/api/http', () => ({
    default: {get: vi.fn(), post: vi.fn(), put: vi.fn()},
    getBusinessErrorCode: (error: {code?: number}) => error.code,
}))

const skills = [
    {id: 1, name: '冷峻悬疑', description: '短句、动作与留白'},
    {id: 2, name: '温暖细腻', description: '感官细节与人物关系'},
    {id: 3, name: '去 AI 味', description: '写作与润色时减少套路表达'},
]
const agent = {
    id: '21', userId: '7', isBuiltin: 0, roleId: '101',
    name: '作者 Agent', systemPrompt: '协助写作',
    llmProviderId: '9007199254740993', modelName: 'test-model', skillIds: [3],
}
const template = {...agent, id: '101', isBuiltin: 1, name: '创作模板'}

beforeEach(() => {
    localStorage.setItem('userInfo', JSON.stringify({id: '7'}))
    setActivePinia(createPinia())
    vi.resetAllMocks()
    vi.mocked(http.get).mockImplementation(async (url) => {
        switch (url) {
            case '/api/agent/skills': return {data: skills}
            case '/api/agent/agents/available': return {data: [agent, template, {...agent, id: '22', userId: '8', name: '其他 Agent'}]}
            case '/api/agent/agents/default-configs': return {data: [template]}
            case '/api/agent/agents/llm-providers': return {data: [{id: '9007199254740993', name: '测试提供方', modelList: ['test-model']}]}
            default: throw new Error(url)
        }
    })
    vi.mocked(http.put).mockResolvedValue({data: agent})
    vi.mocked(http.post).mockResolvedValue({data: agent})
})

describe('Agent Skill 配置', () => {
    it('加载内置目录并展示可用 Agent 的实际挂载和只读权限', async () => {
        const view = mount(AgentView)
        await flushPromises()

        expect(http.get).toHaveBeenCalledWith('/api/agent/skills')
        const cards = view.findAll('.agent-card')
        expect(cards).toHaveLength(3)
        expect(cards.map(card => card.text())).toEqual(expect.arrayContaining([
            expect.stringContaining('作者 Agent'),
            expect.stringContaining('创作模板'),
            expect.stringContaining('其他 Agent'),
        ]))
        const readOnlyCard = cards.find(card => card.text().includes('其他 Agent'))!
        expect(readOnlyCard.text()).toContain('去 AI 味')
        expect(readOnlyCard.text()).toContain('只读')
        expect(readOnlyCard.find('button').exists()).toBe(false)
        view.unmount()
    })

    it('更新时省略未改动的挂载，并在用户移除所有项后提交空数组', async () => {
        const view = mount(AgentView)
        await flushPromises()
        const openEdit = async () => {
            view.get('.agent-card').getComponent({name: 'ElDropdown'}).vm.$emit('command', 'edit')
            await flushPromises()
        }
        await openEdit()
        await clickButton(view, '保存')
        expect(http.put).toHaveBeenCalledWith('/api/agent/agents/21', expect.stringContaining('"llmProviderId":9007199254740993'))
        const firstRequest = JSON.parse(String(vi.mocked(http.put).mock.calls[0][1]))
        expect(firstRequest.roleId).toBe(101)
        expect(firstRequest).not.toHaveProperty('skillIds')

        await openEdit()
        view.findAllComponents({name: 'ElSelect'}).find(select => select.props('multiple'))!
            .vm.$emit('update:modelValue', [])
        await clickButton(view, '保存')
        expect(http.put).toHaveBeenLastCalledWith('/api/agent/agents/21', expect.stringContaining('"skillIds":[]'))
        view.unmount()
    })

    it.each([false, true])('套用写作模板时提交模板 ID，并保留显式空挂载选择（%s）', async (clearSkills) => {
        const view = mount(AgentView)
        await flushPromises()
        await clickButton(view, '新建 Agent')
        const selects = view.findAllComponents({name: 'ElSelect'})
        const templateSelect = selects.find(select => select.props('placeholder') === '选择模板快速填充配置')!
        templateSelect.vm.$emit('update:modelValue', '101')
        templateSelect.vm.$emit('change', '101')
        await flushPromises()
        expect(view.text()).toContain('模板当前挂载 去 AI 味')
        selects.find(select => select.props('placeholder') === '请选择')!.vm.$emit('update:modelValue', '9007199254740993')
        selects.find(select => select.props('placeholder') === '请先选择提供方')!.vm.$emit('update:modelValue', 'test-model')
        if (clearSkills) await clickButton(view, '不挂载 Skill')
        await clickButton(view, '创建')
        expect(http.post).toHaveBeenCalledWith('/api/agent/agents', expect.stringContaining('"llmProviderId":9007199254740993'))
        const request = JSON.parse(String(vi.mocked(http.post).mock.calls[0][1]))
        expect(request.roleId).toBe(101)
        if (clearSkills) {
            expect(request).toHaveProperty('skillIds', [])
        } else {
            expect(request).not.toHaveProperty('skillIds')
        }
        view.unmount()
    })

    it('Skill 失效时刷新目录，更新被拒绝后保留只读挂载', async () => {
        const view = mount(AgentView)
        await flushPromises()
        view.get('.agent-card').getComponent({name: 'ElDropdown'}).vm.$emit('command', 'edit')
        await flushPromises()

        vi.mocked(http.put).mockRejectedValueOnce({code: 1003011001})
        await clickButton(view, '保存')
        expect(vi.mocked(http.get).mock.calls.filter(([url]) => url === '/api/agent/skills')).toHaveLength(2)

        vi.mocked(http.put).mockRejectedValueOnce({code: 1003010003})
        await clickButton(view, '保存')
        expect(view.get('.agent-card').text()).toContain('只读')
        expect(view.get('.agent-card').text()).toContain('去 AI 味')
        view.unmount()
    })
})

async function clickButton(view: VueWrapper, text: string) {
    const button = view.findAll('button').find(button => button.text() === text)
    expect(button).toBeDefined()
    await button!.trigger('click')
    await flushPromises()
}
