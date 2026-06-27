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
 * 创建默认配置（仅用于 ref 初始化占位，避免加载时界面闪烁）。
 *
 * 注意：此值仅为乐观占位，权威默认值由后端 `NovelSettings.createDefault()` 返回。
 * 如需修改默认值，请修改后端 `cn.haowl.hinovel.novel.domain.entity.NovelSettings#createDefault()`。
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
 * 获取用户默认配置（后端保证非 null：未保存过时返回系统默认配置）
 */
export const getUserDefaultSettings = () =>
  http.get<never, { data: NovelSettings }>('/api/novel/settings/user-default')

/**
 * 获取系统默认配置（不查数据库，供「恢复默认」按钮使用）
 */
export const getSystemDefaultSettings = () =>
  http.get<never, { data: NovelSettings }>('/api/novel/settings/system-default')

/**
 * 保存用户默认配置
 */
export const saveUserDefaultSettings = (settings: NovelSettings) =>
    http.put<never, { data: NovelSettings }>('/api/novel/settings/user-default', settings)
