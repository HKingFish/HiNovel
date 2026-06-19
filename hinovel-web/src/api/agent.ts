import http from './http'

/**
 * Agent 配置接口（匹配后端 Agent 实体）
 */
export interface AgentConfig {
    id: number
    userId: number
    roleId?: number
    name: string
    description: string
    systemPrompt: string
    llmProviderId?: number
    modelName: string
    customBaseUrl?: string
    temperature: number
    maxTokens: number
    topP: number
    maxIterations: number
    isBuiltin: number
    sortOrder?: number
    createTime: string
    updateTime: string
}

/**
 * 创建/更新 Agent 请求（匹配后端 AgentRequest DTO）
 */
export interface AgentRequestDto {
    name: string
    description?: string
    systemPrompt: string
    llmProviderId: number | undefined
    modelName: string
    customBaseUrl?: string
    temperature?: number
    maxTokens?: number
    topP?: number
    maxIterations?: number
    roleId?: number
    mcpServerIds?: number[]
}

/**
 * LLM 提供方选项（匹配后端 LlmProvider 实体）
 */
export interface LlmProviderOption {
    id: number
    name: string
    providerType: string
    baseUrl: string
    models: string
    modelList: string[]
    isActive: number
}

/**
 * 获取当前用户可用的所有 Agent（自建 + 内置）
 */
export const getAvailableAgentList = async (): Promise<AgentConfig[]> => {
    const res = await http.get('/api/agent/agents/available')
    return res.data || []
}

/**
 * 创建 Agent
 *
 * @param data 创建请求参数
 */
export const createAgent = async (data: AgentRequestDto): Promise<AgentConfig> => {
    const res = await http.post('/api/agent/agents', data)
    return res.data
}

/**
 * 更新 Agent
 *
 * @param id Agent ID
 * @param data 更新请求参数
 */
export const updateAgent = async (id: number, data: AgentRequestDto): Promise<AgentConfig> => {
    const res = await http.put(`/api/agent/agents/${id}`, data)
    return res.data
}

/**
 * 删除 Agent
 *
 * @param id Agent ID
 */
export const deleteAgentById = async (id: number): Promise<void> => {
    await http.delete(`/api/agent/agents/${id}`)
}

/**
 * 获取激活的 LLM 提供方列表（供 Agent 创建时选择）
 */
export const getLlmProviderList = async (): Promise<LlmProviderOption[]> => {
    const res = await http.get('/api/agent/agents/llm-providers')
    return res.data || []
}

/**
 * 获取 Agent 默认配置模板列表（内置 Agent 作为模板，供新建 Agent 时选择）
 */
export const getAgentDefaultConfigs = async (): Promise<AgentConfig[]> => {
    const res = await http.get('/api/agent/agents/default-configs')
    return res.data || []
}
