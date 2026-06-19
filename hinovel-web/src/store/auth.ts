import {defineStore} from 'pinia'
import {computed, ref} from 'vue'
import type {LoginRequest, RegisterRequest} from '@/api/auth'
import {login as apiLogin, logout as apiLogout, register as apiRegister} from '@/api/auth'

interface UserInfo {
    id: number
    username: string
    email: string
    role: string
    avatarUrl?: string
    createdAt?: string
}

export const useAuthStore = defineStore('auth', () => {
    const accessToken = ref<string | null>(localStorage.getItem('accessToken'))
    const user = ref<UserInfo | null>(
        JSON.parse(localStorage.getItem('userInfo') || 'null')
    )

    const isLoggedIn = computed(() => !!accessToken.value)
    const isAdmin = computed(() => user.value?.role === 'ADMIN')

    /** 登录并持久化 Token */
    async function login(data: LoginRequest) {
        const res = await apiLogin(data)
        const {accessToken: at, userInfo} = res.data
        accessToken.value = at
        user.value = userInfo
        localStorage.setItem('accessToken', at)
        localStorage.setItem('userInfo', JSON.stringify(userInfo))
    }

    /** 注册（注册成功后需手动登录） */
    async function register(data: RegisterRequest) {
        await apiRegister(data)
    }

    /** 登出并清除本地状态 */
    async function logout() {
        try {
            await apiLogout()
        } finally {
            accessToken.value = null
            user.value = null
            localStorage.clear()
        }
    }

    return {accessToken, user, isLoggedIn, isAdmin, login, register, logout}
})
