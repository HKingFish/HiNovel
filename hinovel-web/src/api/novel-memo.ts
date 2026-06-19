import http from './http'

/**
 * 小说创作手记接口
 */
export interface NovelMemo {
    id: number
    novelId: number
    userId: number
    content: string
    createTime: string
    updateTime: string
}

/**
 * 获取小说的创作手记
 *
 * @param novelId 小说 ID
 */
export const getNovelMemo = (novelId: number) =>
    http.get<never, { data: NovelMemo | null }>(`/api/novel/memo/${novelId}`)

/**
 * 保存创作手记
 *
 * @param novelId 小说 ID
 * @param content 手记内容（Markdown 格式）
 */
export const saveNovelMemo = (novelId: number, content: string) =>
    http.put<never, { data: void }>(`/api/novel/memo/${novelId}`, {content})
