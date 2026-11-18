import http from './http'

export interface Novel {
    id: string
    userId: string
    title: string
    description: string
    coverUrl: string
    status: 'ONGOING' | 'COMPLETED' | 'PAUSED'
    wordCount: number
    chapterCount: number
    createTime: string
    updateTime: string
    // Agent配置信息
    authorAgentId?: number | string
    authorAgentName?: string
    editorAgentId?: number | string
    editorAgentName?: string
    qaAgentId?: number | string
    qaAgentName?: string
}

export interface NovelAgentConfig {
    id?: string
    novelId: string
    authorAgentId?: number | string
    authorAgentName?: string
    editorAgentId?: number | string
    editorAgentName?: string
    qaAgentId?: number | string
    qaAgentName?: string
    createTime?: string
    updateTime?: string
}

export interface NovelAgentConfigRequest {
    authorAgentId?: number | string
    editorAgentId?: number | string
    qaAgentId?: number | string
}

export interface NovelChapter {
    id: string
    novelId: string
    title: string
    content: string
    wordCount: number
    sortOrder: number
    status: 'DRAFT' | 'PUBLISHED'
    createTime: string
    updateTime: string
}

export interface ChapterVersion {
    id: string
    chapterId: string
    content: string
    wordCount: number
    changeDiff: string
    createTime: string
}

export interface CreateNovelRequest {
    title: string
    description?: string
    coverUrl?: string
}

export interface UpdateNovelRequest {
    title?: string
    description?: string
    coverUrl?: string
    status?: 'ONGOING' | 'COMPLETED' | 'PAUSED'
}

export interface CreateChapterRequest {
    novelId: string
    title: string
    content?: string
    sortOrder?: number
}

export interface UpdateChapterRequest {
    title?: string
    content?: string
    sortOrder?: number
    status?: 'DRAFT' | 'PUBLISHED'
    createVersion?: boolean
}

// ==================== 小说管理 ====================

export const createNovel = (data: CreateNovelRequest) =>
    http.post<never, { data: Novel }>('/api/novel/platform/novels', data)

export const updateNovel = (novelId: string, data: UpdateNovelRequest) =>
    http.put<never, { data: Novel }>(`/api/novel/platform/novels/${novelId}`, data)

export const deleteNovel = (novelId: string) =>
    http.delete<never, { data: void }>(`/api/novel/platform/novels/${novelId}`)

/**
 * 获取小说详情
 *
 * @param novelId 小说 ID
 */
export const getNovelDetail = (novelId: string) =>
    http.get<never, { data: Novel }>(`/api/novel/platform/novels/${novelId}`)

export const listMyNovels = () =>
    http.get<never, { data: Novel[] }>('/api/novel/platform/novels')

// ==================== 章节管理 ====================

export const createChapter = (data: CreateChapterRequest) =>
    http.post<never, { data: NovelChapter }>('/api/novel/platform/chapters', data)

export const updateChapter = (chapterId: string, data: UpdateChapterRequest) =>
    http.put<never, { data: NovelChapter }>(`/api/novel/platform/chapters/${chapterId}`, data)

export const deleteChapter = (chapterId: string) =>
    http.delete<never, { data: void }>(`/api/novel/platform/chapters/${chapterId}`)

export const getChapter = (chapterId: string) =>
    http.get<never, { data: NovelChapter }>(`/api/novel/platform/chapters/${chapterId}`)

// ==================== 版本管理 ====================

export const listChapterVersions = (chapterId: string, limit?: number) =>
    http.get<never, { data: ChapterVersion[] }>(`/api/novel/platform/chapters/${chapterId}/versions`, {params: {limit}})

export const restoreVersion = (chapterId: string, versionId: string) =>
    http.post<never, { data: NovelChapter }>(`/api/novel/platform/chapters/${chapterId}/versions/${versionId}/restore`)

// ==================== Agent配置管理 ====================

export const getNovelAgentConfig = (novelId: string) =>
    http.get<never, { data: NovelAgentConfig }>(`/api/novel/agent-config/${novelId}`)

export const saveNovelAgentConfig = (novelId: string, data: NovelAgentConfigRequest) =>
    http.put<never, { data: NovelAgentConfig }>(`/api/novel/agent-config/${novelId}`, data)
