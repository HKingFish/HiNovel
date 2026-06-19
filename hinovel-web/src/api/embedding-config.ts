import http from './http'

/**
 * 嵌入式模型配置接口定义。
 */
export interface EmbeddingConfig {
    id: number
    userId: number
    name: string
    modelType: string
    baseUrl: string
    apiKey: string
    modelName: string
    dimensions: number
    isActive: number
    createTime: string
    updateTime: string
}

/**
 * 嵌入式模型配置创建/更新请求 DTO。
 */
export interface EmbeddingConfigRequest {
    name: string
    modelType: string
    baseUrl: string
    apiKey: string
    modelName: string
    dimensions: number
}

/** 获取当前用户的嵌入式模型配置列表 */
export const getEmbeddingConfigList = async (): Promise<EmbeddingConfig[]> => {
    const res = await http.get('/api/ai/embedding-configs')
    return res.data
}

/** 创建嵌入式模型配置 */
export const createEmbeddingConfig = async (data: EmbeddingConfigRequest): Promise<EmbeddingConfig> => {
    const res = await http.post('/api/ai/embedding-configs', data)
    return res.data
}

/** 更新嵌入式模型配置 */
export const updateEmbeddingConfig = async (id: number, data: EmbeddingConfigRequest): Promise<EmbeddingConfig> => {
    const res = await http.put(`/api/ai/embedding-configs/${id}`, data)
    return res.data
}

/** 删除嵌入式模型配置 */
export const deleteEmbeddingConfig = async (id: number): Promise<void> => {
    await http.delete(`/api/ai/embedding-configs/${id}`)
}

/** 激活嵌入式模型配置 */
export const activateEmbeddingConfig = async (id: number): Promise<void> => {
    await http.put(`/api/ai/embedding-configs/${id}/activate`)
}

/** 停用嵌入式模型配置 */
export const deactivateEmbeddingConfig = async (id: number): Promise<void> => {
    await http.put(`/api/ai/embedding-configs/${id}/deactivate`)
}
