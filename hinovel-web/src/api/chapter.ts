import http from './http'

export interface Chapter {
    id: number
    novelId: number
    chapterNumber: number
    title: string
    content: string
    wordCount: number
    status: string
    needRepublish: number
    vectorStored: number
    published: boolean
    createTime: string
    updateTime: string
}

export interface ChapterCreateRequest {
    novelId: number
    title?: string
}

export interface ChapterUpdateRequest {
    title?: string
    content?: string
}

export interface ChapterVersion {
    id: number
    chapterId: number
    content: string
    wordCount: number
    changeDiff?: string
    remark?: string
    published: number
    createTime: string
}

export interface NovelOutline {
    id: number
    novelId: number
    outlineContent: string
    createTime: string
    updateTime: string
}

/**
 * 获取小说的所有章节
 */
export function getNovelChapters(novelId: number) {
    return http.get<never, { data: Chapter[] }>(`/api/novel/chapters/novel/${novelId}`)
}

/**
 * 分页获取小说的章节
 * @param novelId 小说ID
 * @param pageNum 页码（从1开始）
 * @param pageSize 每页大小
 * @param status 发布状态筛选（可选，0=草稿，1=已发布）
 * @param vectorStored 入库状态筛选（可选，0=未入库，1=已入库）
 * @param title 标题关键词筛选（可选）
 */
export function getNovelChaptersPage(
    novelId: number,
    pageNum: number,
    pageSize: number,
    status?: number,
    vectorStored?: number,
    title?: string
) {
    let url = `/api/novel/chapters/novel/${novelId}/page?pageNum=${pageNum}&pageSize=${pageSize}`
    if (status !== undefined && status !== null) {
        url += `&status=${status}`
    }
    if (vectorStored !== undefined && vectorStored !== null) {
        url += `&vectorStored=${vectorStored}`
    }
    if (title && title.trim()) {
        url += `&title=${encodeURIComponent(title.trim())}`
    }
    return http.get<never, { data: ChapterPageResponse }>(url)
}

/**
 * 分页响应
 */
export interface ChapterPageResponse {
    list: Chapter[]
    total: number
    pageNum: number
    pageSize: number
    hasMore: boolean
}

/**
 * 获取章节详情
 */
export function getChapter(chapterId: number) {
    return http.get<never, { data: Chapter }>(`/api/novel/chapters/${chapterId}`)
}

/**
 * 创建章节
 */
export function createChapter(data: ChapterCreateRequest) {
    return http.post<never, { data: Chapter }>('/api/novel/chapters', data)
}

/**
 * 更新章节
 */
export function updateChapter(chapterId: number, data: ChapterUpdateRequest) {
    return http.put<never, { data: Chapter }>(`/api/novel/chapters/${chapterId}`, data)
}

/**
 * 删除章节
 */
export function deleteChapter(chapterId: number) {
    return http.delete<never, void>(`/api/novel/chapters/${chapterId}`)
}

/**
 * 发布章节响应
 */
export interface PublishChapterResponse {
    published: boolean
    message: string
}

/**
 * AI 大纲响应
 */
export interface AiOutlineResponse {
    aiOutlineContent: string
    hasAiOutline: boolean
}

/**
 * 发布章节
 * @param chapterId 章节ID
 */
export function publishChapterApi(chapterId: number) {
    return http.post<never, { data: PublishChapterResponse }>(`/api/novel/chapters/${chapterId}/publish`)
}

/**
 * 手动同步章节内容到向量数据库
 * @param chapterId 章节ID
 */
export function syncChapterVectorApi(chapterId: number) {
    return http.post<never, { data: void }>(`/api/novel/chapters/${chapterId}/sync-vector`)
}

/**
 * 获取 AI 生成的章节大纲
 */
export function getAiOutline(chapterId: number) {
    return http.get<never, { data: AiOutlineResponse }>(`/api/novel/chapters/${chapterId}/ai-outline`)
}

/**
 * 用 AI 大纲替换用户大纲
 */
export function replaceOutlineWithAi(chapterId: number) {
    return http.post<never, { data: void }>(`/api/novel/chapters/${chapterId}/replace-outline`)
}

/**
 * 获取章节历史版本
 */
export function getChapterVersions(chapterId: number) {
    return http.get<never, { data: ChapterVersion[] }>(`/api/novel/chapters/${chapterId}/versions`)
}

/**
 * 恢复章节到指定版本
 */
export function restoreChapterVersion(chapterId: number, versionId: number) {
    return http.post<never, { data: Chapter }>(`/api/novel/chapters/${chapterId}/restore/${versionId}`)
}

/**
 * 更新版本备注
 * @param chapterId 章节ID
 * @param versionId 版本ID
 * @param remark 备注内容
 */
export function updateVersionRemark(chapterId: number, versionId: number, remark: string) {
    return http.put<never, { data: void }>(`/api/novel/chapters/${chapterId}/versions/${versionId}/remark`, {remark})
}

/**
 * 获取小说大纲
 */
export function getNovelOutline(novelId: number) {
    return http.get<never, { data: NovelOutline }>(`/api/novel/chapters/outline/novel/${novelId}`)
}

/**
 * 保存小说大纲
 */
export function saveNovelOutline(novelId: number, outlineContent: string) {
    return http.post<never, { data: NovelOutline }>('/api/novel/chapters/outline', { novelId, outlineContent })
}

/**
 * 修改章节号
 */
export function updateChapterNumber(chapterId: number, chapterNumber: number) {
    return http.put<never, { data: Chapter }>(`/api/novel/chapters/${chapterId}/chapter-number`, { chapterNumber })
}

/**
 * 一键重排章节号，使章节号连续
 */
export function reorderChapters(novelId: number) {
    return http.post<never, { data: void }>(`/api/novel/chapters/novel/${novelId}/reorder`)
}

/**
 * 章节大纲
 */
export interface ChapterOutline {
    id: number
    novelId: number
    chapterId: number
    outlineContent: string
    aiOutlineContent: string
    plotPoints: string
    involvedCharacters: string
    emotionTone: string
    sceneSetting: string
    createTime: string
    updateTime: string
}

/**
 * 获取章节大纲
 */
export function getChapterOutline(chapterId: number) {
    return http.get<never, { data: ChapterOutline }>(`/api/novel/chapter-outline/chapter/${chapterId}`)
}

/**
 * 保存或更新章节大纲
 */
export function saveChapterOutline(outline: Partial<ChapterOutline>) {
    return http.post<never, { data: ChapterOutline }>('/api/novel/chapter-outline', outline)
}

/**
 * 批量更新章节排序（拖拽排序）
 *
 * @param novelId 小说 ID
 * @param chapterIds 按期望顺序排列的章节 ID 列表
 */
export function batchUpdateChapterSort(novelId: number, chapterIds: number[]) {
    return http.put<never, { data: void }>(`/api/novel/chapters/novel/${novelId}/sort`, { chapterIds })
}
