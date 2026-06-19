import http from './http'

export interface LoginRequest {
    account: string
    password: string
}

export interface RegisterRequest {
    username: string
    email: string
    password: string
}

export interface TokenResponse {
    accessToken: string
    expiresIn: number
    userInfo: {
        id: number
        username: string
        email: string
        role: string
        avatarUrl?: string
    }
}

/** 用户登录 */
export const login = (data: LoginRequest) =>
    http.post<never, { data: TokenResponse }>('/api/user/auth/login', data)

/** 用户注册 */
export const register = (data: RegisterRequest) =>
    http.post<never, { data: TokenResponse }>('/api/user/auth/register', data)

/** 登出 */
export const logout = () => http.post('/api/user/auth/logout')

/** 刷新 Token */
export const refreshToken = (token: string) =>
    http.post<never, { data: { accessToken: string } }>('/api/user/auth/refresh', {refreshToken: token})
