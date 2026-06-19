import http from './http'

/**
 * LLM 提供方接口定义。
 */
export interface LlmProvider {
    id: number
    name: string
    providerType: string
    baseUrl: string
    apiKey: string
    models: string
    isActive: number
    createTime: string
    updateTime: string
}

/**
 * LLM 提供方创建/更新请求 DTO。
 */
export interface LlmProviderRequest {
    name: string
    providerType: string
    baseUrl: string
    apiKey: string
    models: string[]
}

/**
 * 获取所有 LLM 提供方列表。
 */
export const getLlmProviderList = async (): Promise<LlmProvider[]> => {
    const res = await http.get('/api/ai/llm-providers')
    return res.data
}

/**
 * 创建 LLM 提供方。
 */
export const createLlmProvider = async (data: LlmProviderRequest): Promise<LlmProvider> => {
    const res = await http.post('/api/ai/llm-providers', data)
    return res.data
}

/**
 * 更新 LLM 提供方。
 */
export const updateLlmProvider = async (id: number, data: LlmProviderRequest): Promise<LlmProvider> => {
    const res = await http.put(`/api/ai/llm-providers/${id}`, data)
    return res.data
}

/**
 * 删除 LLM 提供方。
 */
export const deleteLlmProvider = async (id: number): Promise<void> => {
    await http.delete(`/api/ai/llm-providers/${id}`)
}

/**
 * 激活 LLM 提供方。
 */
export const activateLlmProvider = async (id: number): Promise<void> => {
    await http.put(`/api/ai/llm-providers/${id}/activate`)
}

/**
 * 停用 LLM 提供方。
 */
export const deactivateLlmProvider = async (id: number): Promise<void> => {
    await http.put(`/api/ai/llm-providers/${id}/deactivate`)
}

/**
 * LLM 提供方类型信息。
 */
export interface ProviderType {
    code: string
    displayName: string
    officialBaseUrl: string
}

/**
 * 获取支持的提供方类型列表。
 */
export const getLlmProviderTypes = async (): Promise<ProviderType[]> => {
    const res = await http.get('/api/ai/llm-providers/types')
    return res.data
}
