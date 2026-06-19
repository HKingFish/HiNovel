import http from './http'

/**
 * 小说配置接口类型
 */
export interface NovelSettings {
    id?: number
    novelId?: number
    userId?: number
    autoAuditAfterRewrite: number
    autoOutlineAfterPublish: number
    autoVectorAfterPublish: number
    rewriteContextChapters: number
    rewriteIncludeOutline: number
    auditIncludeOutline: number
    qaIncludeOutline: number
    qaContextLength: number
    autoSaveContent: number
    createTime?: string
    updateTime?: string
}

/**
 * 创建默认配置（前端默认值）
 */
export const createDefaultSettings = (): NovelSettings => ({
    autoAuditAfterRewrite: 1,
    autoOutlineAfterPublish: 1,
    autoVectorAfterPublish: 1,
    rewriteContextChapters: 2,
    rewriteIncludeOutline: 1,
    auditIncludeOutline: 1,
    qaIncludeOutline: 1,
    qaContextLength: 10,
    autoSaveContent: 1
})

/**
 * 获取小说有效配置（合并后）
 */
export const getEffectiveSettings = (novelId: number) =>
    http.get<never, { data: NovelSettings }>(`/api/novel/settings/${novelId}`)

/**
 * 获取小说级别配置
 */
export const getNovelLevelSettings = (novelId: number) =>
    http.get<never, { data: NovelSettings | null }>(`/api/novel/settings/${novelId}/novel-level`)

/**
 * 保存小说级别配置
 */
export const saveNovelSettings = (novelId: number, settings: NovelSettings) =>
    http.put<never, { data: NovelSettings }>(`/api/novel/settings/${novelId}`, settings)

/**
 * 获取用户默认配置
 */
export const getUserDefaultSettings = () =>
    http.get<never, { data: NovelSettings | null }>('/api/novel/settings/user-default')

/**
 * 保存用户默认配置
 */
export const saveUserDefaultSettings = (settings: NovelSettings) =>
    http.put<never, { data: NovelSettings }>('/api/novel/settings/user-default', settings)
