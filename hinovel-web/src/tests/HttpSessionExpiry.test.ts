import {AxiosError, AxiosHeaders} from 'axios'
import {createPinia, setActivePinia} from 'pinia'
import {afterEach, expect, it, vi} from 'vitest'
import http from '@/api/http'
import router from '@/router'
import {useAuthStore} from '@/store/auth'

vi.mock('@/router', () => ({default: {push: vi.fn()}}))

const originalAdapter = http.defaults.adapter

afterEach(() => {
    http.defaults.adapter = originalAdapter
    localStorage.clear()
    vi.clearAllMocks()
})

it.each([401, 200])('Skill 目录返回未登录（HTTP %s）时，清除登录状态并跳转', async (status) => {
    localStorage.setItem('accessToken', 'test-token')
    localStorage.setItem('userInfo', JSON.stringify({id: '7'}))
    localStorage.setItem('novel_draft_1', '未同步草稿')
    setActivePinia(createPinia())
    const auth = useAuthStore()
    http.defaults.adapter = async config => {
        const response = {
            config, status, statusText: '', headers: new AxiosHeaders(),
            data: {code: 1000010001, message: '未登录'},
        }
        if (status === 401) throw new AxiosError('未登录', 'ERR_BAD_REQUEST', config, undefined, response)
        return response
    }
    vi.mocked(router.push).mockImplementation(async () => {
        expect(auth.isLoggedIn).toBe(false)
        return undefined
    })

    await expect(http.get('/api/agent/skills')).rejects.toThrow('未登录')
    expect(router.push).toHaveBeenCalledWith('/login')
    expect(auth.isLoggedIn).toBe(false)
    expect(auth.user).toBeNull()
    expect(localStorage.getItem('accessToken')).toBeNull()
    expect(localStorage.getItem('novel_draft_1')).toBe('未同步草稿')
})

it('旧请求的延迟 401 保留新登录会话', async () => {
    localStorage.setItem('accessToken', 'expired-token')
    setActivePinia(createPinia())
    const auth = useAuthStore()
    http.defaults.adapter = async config => {
        expect(config.headers.get('satoken')).toBe('expired-token')
        auth.accessToken = 'new-token'
        localStorage.setItem('accessToken', 'new-token')
        throw new AxiosError('未登录', 'ERR_BAD_REQUEST', config, undefined, {
            config, status: 401, statusText: '', headers: new AxiosHeaders(), data: {code: 1000010001},
        })
    }

    await expect(http.get('/api/agent/skills')).rejects.toThrow('未登录')
    expect(router.push).not.toHaveBeenCalled()
    expect(auth.accessToken).toBe('new-token')
    expect(localStorage.getItem('accessToken')).toBe('new-token')
})
