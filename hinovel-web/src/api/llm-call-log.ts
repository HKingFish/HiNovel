import http from './http'

/**
 * LLM 调用记录接口类型定义
 */
export interface LlmCallLog {
    id: number
    callScene: string
    agentId: number | null
    agentName: string | null
    llmProviderId: number | null
    llmProviderName: string | null
    modelName: string | null
    requestContent: string | null
    responseContent: string | null
    promptTokens: number
    completionTokens: number
    totalTokens: number
    processingTimeMs: number
    status: string
    errorMessage: string | null
    isStreaming: number
    createTime: string
}

/**
 * 调用消耗统计结果
 */
export interface CallStatistics {
    successCount: number
    failCount: number
    totalTokens: number
    totalDuration: number
}

/**
 * 分页结果接口
 */
export interface PageResult<T> {
    records: T[]
    total: number
    size: number
    current: number
    pages: number
}

/**
 * 分页查询 LLM 调用记录
 *
 * @param pageNum   页码
 * @param pageSize  每页条数
 * @param callScene 调用场景（可选）
 * @param status    调用状态（可选）
 * @param startTime 开始时间（可选，格式 yyyy-MM-dd HH:mm:ss）
 * @param endTime   结束时间（可选，格式 yyyy-MM-dd HH:mm:ss）
 */
export const getLlmCallLogList = (
    pageNum: number = 1,
    pageSize: number = 20,
    callScene?: string,
    status?: string,
    startTime?: string,
    endTime?: string
) => {
    const params: Record<string, string | number> = {pageNum, pageSize}
    if (callScene) {
        params.callScene = callScene
    }
    if (status) {
        params.status = status
    }
    if (startTime) {
        params.startTime = startTime
    }
    if (endTime) {
        params.endTime = endTime
    }
    return http.get<PageResult<LlmCallLog>>('/api/ai/llm-call-logs', {params})
}

/**
 * 查询调用消耗统计
 *
 * @param callScene 调用场景（可选）
 * @param status    调用状态（可选）
 * @param startTime 开始时间（可选，格式 yyyy-MM-dd HH:mm:ss）
 * @param endTime   结束时间（可选，格式 yyyy-MM-dd HH:mm:ss）
 */
export const getCallStatistics = (
    callScene?: string,
    status?: string,
    startTime?: string,
    endTime?: string
) => {
    const params: Record<string, string> = {}
    if (callScene) {
        params.callScene = callScene
    }
    if (status) {
        params.status = status
    }
    if (startTime) {
        params.startTime = startTime
    }
    if (endTime) {
        params.endTime = endTime
    }
    return http.get<CallStatistics>('/api/ai/llm-call-logs/statistics', {params})
}


