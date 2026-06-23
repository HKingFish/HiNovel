package cn.haowl.hinovel.ai.enums;

import cn.haowl.hinovel.common.exception.ErrorCode;

import static cn.haowl.hinovel.common.exception.ServiceErrorCodeRange.AI_EMBEDDING_ERROR_CODE_MIN;
import static cn.haowl.hinovel.common.exception.ServiceErrorCodeRange.AI_LLM_PROVIDER_ERROR_CODE_MIN;

/**
 * AI 模块错误码常量接口。
 *
 * <p>定义 AI 配置模块相关的错误码。
 * 错误码范围：[1-004-000-000 ~ 1-005-000-000)</p>
 *
 * @author haowl
 * @see ErrorCode
 * @since 2024
 */
public interface AiErrorCodeConstants {

    // ==================== AI LLM Provider 模块 [1-004-001-xxx] ====================

    /**
     * LLM Provider 不存在。
     */
    ErrorCode LLM_PROVIDER_NOT_EXISTS = new ErrorCode(
        AI_LLM_PROVIDER_ERROR_CODE_MIN + 1,
        "LLM Provider 不存在"
    );

    /**
     * LLM Provider 名称已存在。
     */
    ErrorCode LLM_PROVIDER_NAME_EXISTS = new ErrorCode(
        AI_LLM_PROVIDER_ERROR_CODE_MIN + 2,
        "LLM Provider 名称已存在"
    );

    /**
     * LLM Provider 已禁用。
     */
    ErrorCode LLM_PROVIDER_DISABLED = new ErrorCode(
        AI_LLM_PROVIDER_ERROR_CODE_MIN + 3,
        "LLM Provider 已禁用"
    );

    /**
     * LLM Provider 配置无效。
     */
    ErrorCode LLM_PROVIDER_CONFIG_INVALID = new ErrorCode(
        AI_LLM_PROVIDER_ERROR_CODE_MIN + 4,
        "LLM Provider 配置无效"
    );

    /**
     * LLM Provider 连接测试失败。
     */
    ErrorCode LLM_PROVIDER_CONNECTION_FAILED = new ErrorCode(
        AI_LLM_PROVIDER_ERROR_CODE_MIN + 5,
        "LLM Provider 连接测试失败"
    );

    /**
     * LLM Provider 不可用。
     */
    ErrorCode LLM_PROVIDER_UNAVAILABLE = new ErrorCode(
        AI_LLM_PROVIDER_ERROR_CODE_MIN + 6,
        "LLM Provider 不可用"
    );

    /**
     * LLM Provider API Key 无效。
     */
    ErrorCode LLM_PROVIDER_API_KEY_INVALID = new ErrorCode(
        AI_LLM_PROVIDER_ERROR_CODE_MIN + 6,
        "LLM Provider API Key 无效"
    );

    /**
     * LLM 调用失败。
     */
    ErrorCode LLM_CALL_FAILED = new ErrorCode(
        AI_LLM_PROVIDER_ERROR_CODE_MIN + 10,
        "LLM 调用失败，请稍后重试"
    );

    /**
     * LLM 响应解析失败。
     */
    ErrorCode LLM_RESPONSE_PARSE_FAILED = new ErrorCode(
        AI_LLM_PROVIDER_ERROR_CODE_MIN + 11,
        "LLM 响应解析失败"
    );

    /**
     * LLM 请求超时。
     */
    ErrorCode LLM_REQUEST_TIMEOUT = new ErrorCode(
        AI_LLM_PROVIDER_ERROR_CODE_MIN + 12,
        "LLM 请求超时，请稍后重试"
    );

    /**
     * LLM 请求频率超限。
     */
    ErrorCode LLM_RATE_LIMIT_EXCEEDED = new ErrorCode(
        AI_LLM_PROVIDER_ERROR_CODE_MIN + 13,
        "LLM 请求频率超限，请稍后重试"
    );

    /**
     * LLM 余额不足。
     */
    ErrorCode LLM_INSUFFICIENT_QUOTA = new ErrorCode(
        AI_LLM_PROVIDER_ERROR_CODE_MIN + 14,
        "LLM 余额不足，请联系管理员"
    );

    // ==================== AI Embedding 配置模块 [1-004-002-xxx] ====================

    /**
     * Embedding 配置不存在。
     */
    ErrorCode EMBEDDING_CONFIG_NOT_EXISTS = new ErrorCode(
        AI_EMBEDDING_ERROR_CODE_MIN + 1,
        "Embedding 配置不存在"
    );

    /**
     * Embedding 配置已存在。
     */
    ErrorCode EMBEDDING_CONFIG_EXISTS = new ErrorCode(
        AI_EMBEDDING_ERROR_CODE_MIN + 2,
        "Embedding 配置已存在"
    );

    /**
     * Embedding 服务不可用。
     */
    ErrorCode EMBEDDING_SERVICE_UNAVAILABLE = new ErrorCode(
        AI_EMBEDDING_ERROR_CODE_MIN + 3,
        "Embedding 服务不可用"
    );

    /**
     * Embedding 连接测试失败。
     */
    ErrorCode EMBEDDING_CONNECTION_FAILED = new ErrorCode(
        AI_EMBEDDING_ERROR_CODE_MIN + 4,
        "Embedding 服务连接测试失败"
    );

    /**
     * 向量维度不匹配。
     */
    ErrorCode EMBEDDING_DIMENSION_MISMATCH = new ErrorCode(
        AI_EMBEDDING_ERROR_CODE_MIN + 5,
        "向量维度不匹配"
    );

    /**
     * 向量存储服务不可用。
     */
    ErrorCode VECTOR_STORE_UNAVAILABLE = new ErrorCode(
        AI_EMBEDDING_ERROR_CODE_MIN + 10,
        "向量存储服务不可用"
    );

    /**
     * 向量检索失败。
     */
    ErrorCode VECTOR_SEARCH_FAILED = new ErrorCode(
        AI_EMBEDDING_ERROR_CODE_MIN + 11,
        "向量检索失败"
    );

    /**
     * 向量插入失败。
     */
    ErrorCode VECTOR_INSERT_FAILED = new ErrorCode(
        AI_EMBEDDING_ERROR_CODE_MIN + 12,
        "向量插入失败"
    );
}
