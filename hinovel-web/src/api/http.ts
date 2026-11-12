import axios, {type AxiosInstance, type AxiosResponse} from 'axios'
import {ElMessage} from 'element-plus'
import router from '@/router'
import {useAuthStore} from '@/store/auth'

export class HttpBusinessError extends Error {
    constructor(message: string, readonly code: number) {
        super(message)
    }
}

export const getBusinessErrorCode = (error: unknown): number | undefined => {
    if (error instanceof HttpBusinessError) return error.code
    if (axios.isAxiosError(error)) return error.response?.data?.code
}

const redirectToLogin = (message: string, request: AxiosResponse['config'] | undefined) => {
    const requestToken = request?.headers.get('satoken') ?? null
    if (requestToken !== localStorage.getItem('accessToken')) return
    ElMessage.warning(message)
    useAuthStore().clearSession()
    void router.push('/login')
}

// 创建 axios 实例
// baseURL 默认为空，走相对路径由 Vite 代理转发到后端，避免开发环境跨域问题
// 生产环境可通过 VITE_API_BASE_URL 环境变量指定完整后端地址
const http: AxiosInstance = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || '',
    timeout: 30000,
    headers: {'Content-Type': 'application/json'},
})

// 请求拦截器：自动附加 Sa-Token
http.interceptors.request.use((config) => {
    const token = localStorage.getItem('accessToken')
    if (token) {
        // Sa-Token 默认从请求头 satoken 读取令牌
        config.headers['satoken'] = token
    }
    return config
})

// 响应拦截器：统一处理错误和 Token 刷新
http.interceptors.response.use(
    (response: AxiosResponse) => {
        const data = response.data
        // 业务错误码（HTTP 200 但 code 非 200）统一处理
        if (data && data.code !== undefined) {
            if (data.code === 401 || data.code === 1000010001) {
                // 未登录或登录过期，提示用户并跳转登录页
                redirectToLogin(data.message || '登录已过期，请重新登录', response.config)
                return Promise.reject(new HttpBusinessError(data.message || '未登录', data.code))
            } else if (data.code !== 200) {
                // 其他业务错误，弹出错误提示
                ElMessage.error(data.message || '操作失败')
                return Promise.reject(new HttpBusinessError(data.message || '操作失败', data.code))
            }
        }
        return data
    },
    async (error) => {
        const status = error.response?.status
        if (status === 401) {
            // Token 无效或过期，提示用户并跳转登录页
            redirectToLogin('登录已过期，请重新登录', error.config)
        } else if (status === 403) {
            ElMessage.error('权限不足')
        } else {
            const message = error.response?.data?.message || '请求失败，请稍后重试'
            ElMessage.error(message)
        }
        return Promise.reject(error)
    }
)

export default http
