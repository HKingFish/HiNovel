import http from './http'

export interface Novel {
    id: number
    userId: number
    title: string
    description: string
    coverUrl: string
    status: 'ONGOING' | 'COMPLETED' | 'PAUSED'
    wordCount: number
    chapterCount: number
    createTime: string
    updateTime: string
    // Agent配置信息
    authorAgentId?: number
    authorAgentName?: string
    editorAgentId?: number
    editorAgentName?: string
    qaAgentId?: number
    qaAgentName?: string
}

export interface NovelAgentConfig {
    id?: number
    novelId: number
    authorAgentId?: number
    authorAgentName?: string
    editorAgentId?: number
    editorAgentName?: string
    qaAgentId?: number
    qaAgentName?: string
    createTime?: string
    updateTime?: string
}

export interface NovelAgentConfigRequest {
    authorAgentId?: number
    editorAgentId?: number
    qaAgentId?: number
}

export interface NovelChapter {
    id: number
    novelId: number
    title: string
    content: string
    wordCount: number
    sortOrder: number
    status: 'DRAFT' | 'PUBLISHED'
    createTime: string
    updateTime: string
}

export interface ChapterVersion {
    id: number
    chapterId: number
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
    novelId: number
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

export const updateNovel = (novelId: number, data: UpdateNovelRequest) =>
    http.put<never, { data: Novel }>(`/api/novel/platform/novels/${novelId}`, data)

export const deleteNovel = (novelId: number) =>
    http.delete<never, { data: void }>(`/api/novel/platform/novels/${novelId}`)

/**
 * 获取小说详情
 *
 * @param novelId 小说 ID
 */
export const getNovelDetail = (novelId: number) =>
    http.get<never, { data: Novel }>(`/api/novel/platform/novels/${novelId}`)

export const listMyNovels = () =>
    http.get<never, { data: Novel[] }>('/api/novel/platform/novels')

// ==================== 章节管理 ====================

export const createChapter = (data: CreateChapterRequest) =>
    http.post<never, { data: NovelChapter }>('/api/novel/platform/chapters', data)

export const updateChapter = (chapterId: number, data: UpdateChapterRequest) =>
    http.put<never, { data: NovelChapter }>(`/api/novel/platform/chapters/${chapterId}`, data)

export const deleteChapter = (chapterId: number) =>
    http.delete<never, { data: void }>(`/api/novel/platform/chapters/${chapterId}`)

export const getChapter = (chapterId: number) =>
    http.get<never, { data: NovelChapter }>(`/api/novel/platform/chapters/${chapterId}`)

// ==================== 版本管理 ====================

export const listChapterVersions = (chapterId: number, limit?: number) =>
    http.get<never, { data: ChapterVersion[] }>(`/api/novel/platform/chapters/${chapterId}/versions`, {params: {limit}})

export const restoreVersion = (chapterId: number, versionId: number) =>
    http.post<never, { data: NovelChapter }>(`/api/novel/platform/chapters/${chapterId}/versions/${versionId}/restore`)

// ==================== Agent配置管理 ====================

export const getNovelAgentConfig = (novelId: number) =>
    http.get<never, { data: NovelAgentConfig }>(`/api/novel/agent-config/${novelId}`)

export const saveNovelAgentConfig = (novelId: number, data: NovelAgentConfigRequest) =>
    http.put<never, { data: NovelAgentConfig }>(`/api/novel/agent-config/${novelId}`, data)
