package cn.haowl.hinovel.agent.enums;

import cn.haowl.hinovel.common.exception.ErrorCode;

import static cn.haowl.hinovel.common.exception.ServiceErrorCodeRange.*;

/**
 * Agent 模块错误码常量接口。
 *
 * <p>定义 Agent 模块相关的错误码。
 * 错误码范围：[1-003-000-000 ~ 1-004-000-000)</p>
 *
 * @author haowl
 * @see ErrorCode
 * @since 2024
 */
public interface AgentErrorCodeConstants {

    // ==================== Agent AI 审核模块 [1-003-001-xxx] ====================

    /**
     * 章节不存在（AI 审核时）。
     *
     * @see AgentErrorCodeConstants#AGENT_AUDIT_ERROR_CODE_MIN
     */
    ErrorCode AUDIT_CHAPTER_NOT_EXISTS = new ErrorCode(
        AGENT_AUDIT_ERROR_CODE_MIN + 1,
        "章节不存在，无法进行审核"
    );

    /**
     * AI 审核服务不可用。
     */
    ErrorCode AUDIT_SERVICE_UNAVAILABLE = new ErrorCode(
        AGENT_AUDIT_ERROR_CODE_MIN + 2,
        "AI 审核服务暂不可用，请稍后重试"
    );

    /**
     * AI 审核请求超时。
     */
    ErrorCode AUDIT_REQUEST_TIMEOUT = new ErrorCode(
        AGENT_AUDIT_ERROR_CODE_MIN + 3,
        "AI 审核请求超时，请稍后重试"
    );

    /**
     * 章节内容为空，无法审核。
     */
    ErrorCode AUDIT_CONTENT_EMPTY = new ErrorCode(
        AGENT_AUDIT_ERROR_CODE_MIN + 4,
        "章节内容为空，无法进行审核"
    );

    /**
     * 审核结果解析失败。
     */
    ErrorCode AUDIT_RESULT_PARSE_FAILED = new ErrorCode(
        AGENT_AUDIT_ERROR_CODE_MIN + 5,
        "审核结果解析失败，请稍后重试"
    );

    // ==================== Agent AI 改写模块 [1-003-002-xxx] ====================

    /**
     * 章节不存在（AI 改写时）。
     */
    ErrorCode REWRITE_CHAPTER_NOT_EXISTS = new ErrorCode(
        AGENT_REWRITE_ERROR_CODE_MIN + 1,
        "章节不存在，无法进行改写"
    );

    /**
     * AI 改写服务不可用。
     */
    ErrorCode REWRITE_SERVICE_UNAVAILABLE = new ErrorCode(
        AGENT_REWRITE_ERROR_CODE_MIN + 2,
        "AI 改写服务暂不可用，请稍后重试"
    );

    /**
     * AI 改写请求超时。
     */
    ErrorCode REWRITE_REQUEST_TIMEOUT = new ErrorCode(
        AGENT_REWRITE_ERROR_CODE_MIN + 3,
        "AI 改写请求超时，请稍后重试"
    );

    /**
     * 章节大纲为空，无法改写。
     */
    ErrorCode REWRITE_OUTLINE_EMPTY = new ErrorCode(
        AGENT_REWRITE_ERROR_CODE_MIN + 4,
        "章节大纲为空，无法进行改写"
    );

    /**
     * AI 改写流式响应中断。
     */
    ErrorCode REWRITE_STREAM_INTERRUPTED = new ErrorCode(
        AGENT_REWRITE_ERROR_CODE_MIN + 5,
        "AI 改写过程中断，请重试"
    );

    /**
     * 改写结果为空。
     */
    ErrorCode REWRITE_RESULT_EMPTY = new ErrorCode(
        AGENT_REWRITE_ERROR_CODE_MIN + 6,
        "AI 改写结果为空，请重试"
    );

    // ==================== Agent AI 问答模块 [1-003-003-xxx] ====================

    /**
     * 小说不存在（AI 问答时）。
     */
    ErrorCode CHAT_NOVEL_NOT_EXISTS = new ErrorCode(
        AGENT_CHAT_ERROR_CODE_MIN + 1,
        "小说不存在，无法进行问答"
    );

    /**
     * 问答会话不存在。
     */
    ErrorCode CHAT_SESSION_NOT_EXISTS = new ErrorCode(
        AGENT_CHAT_ERROR_CODE_MIN + 2,
        "问答会话不存在"
    );

    /**
     * 问答会话创建失败。
     */
    ErrorCode CHAT_SESSION_CREATE_FAILED = new ErrorCode(
        AGENT_CHAT_ERROR_CODE_MIN + 3,
        "问答会话创建失败"
    );

    /**
     * 问答消息发送失败。
     */
    ErrorCode CHAT_MESSAGE_SEND_FAILED = new ErrorCode(
        AGENT_CHAT_ERROR_CODE_MIN + 4,
        "问答消息发送失败，请稍后重试"
    );

    /**
     * 向量检索服务不可用。
     */
    ErrorCode CHAT_VECTOR_SERVICE_UNAVAILABLE = new ErrorCode(
        AGENT_CHAT_ERROR_CODE_MIN + 5,
        "向量检索服务不可用，无法进行问答"
    );

    /**
     * 未找到相关内容。
     */
    ErrorCode CHAT_NO_RELEVANT_CONTENT = new ErrorCode(
        AGENT_CHAT_ERROR_CODE_MIN + 6,
        "未找到与问题相关的内容"
    );

    // ==================== Agent Agent 配置模块 [1-003-010-xxx] ====================

    /**
     * Agent 不存在。
     */
    ErrorCode AGENT_NOT_EXISTS = new ErrorCode(
        AGENT_CONFIG_ERROR_CODE_MIN + 1,
        "Agent 不存在，ID：{}"
    );

    /**
     * Agent 名称已存在。
     */
    ErrorCode AGENT_NAME_EXISTS = new ErrorCode(
        AGENT_CONFIG_ERROR_CODE_MIN + 2,
        "Agent 名称已存在"
    );

    /**
     * 无权操作此 Agent。
     */
    ErrorCode AGENT_NOT_OWNED = new ErrorCode(
        AGENT_CONFIG_ERROR_CODE_MIN + 3,
        "无权操作此 Agent"
    );

    /**
     * 内置 Agent 不能删除。
     */
    ErrorCode AGENT_BUILTIN_CANNOT_DELETE = new ErrorCode(
        AGENT_CONFIG_ERROR_CODE_MIN + 4,
        "内置 Agent 不能删除"
    );

    /**
     * 内置 Agent 不能修改。
     */
    ErrorCode AGENT_BUILTIN_CANNOT_MODIFY = new ErrorCode(
        AGENT_CONFIG_ERROR_CODE_MIN + 5,
        "内置 Agent 不能修改"
    );

    /**
     * Agent 系统提示词为空。
     */
    ErrorCode AGENT_SYSTEM_PROMPT_EMPTY = new ErrorCode(
        AGENT_CONFIG_ERROR_CODE_MIN + 6,
        "Agent 系统提示词不能为空"
    );

    /**
     * Agent 模型配置无效。
     */
    ErrorCode AGENT_MODEL_CONFIG_INVALID = new ErrorCode(
        AGENT_CONFIG_ERROR_CODE_MIN + 7,
        "Agent 模型配置无效"
    );

    /**
     * Agent 配置保存失败。
     */
    ErrorCode AGENT_CONFIG_SAVE_FAILED = new ErrorCode(
        AGENT_CONFIG_ERROR_CODE_MIN + 8,
        "Agent 配置保存失败"
    );

    /**
     * 内置 Agent 未初始化。
     */
    ErrorCode AGENT_BUILTIN_NOT_INITIALIZED = new ErrorCode(
        AGENT_CONFIG_ERROR_CODE_MIN + 9,
        "内置 Agent 未初始化，请联系管理员"
    );

    /**
     * Agent 未配置 LLM 提供方。
     */
    ErrorCode AGENT_LLM_PROVIDER_NOT_CONFIGURED = new ErrorCode(
        AGENT_CONFIG_ERROR_CODE_MIN + 10,
        "Agent 未配置 LLM 提供方，Agent ID：{}"
    );

    /**
     * 小说未配置指定角色的 Agent。
     */
    ErrorCode NOVEL_AGENT_NOT_CONFIGURED = new ErrorCode(
        AGENT_CONFIG_ERROR_CODE_MIN + 11,
        "该小说未配置 {}，请先在小说设置中绑定"
    );

    /**
     * Agent 角色未配置 LLM 提供方。
     */
    ErrorCode AGENT_ROLE_PROVIDER_NOT_CONFIGURED = new ErrorCode(
        AGENT_CONFIG_ERROR_CODE_MIN + 12,
        "{} 未配置 LLM 提供方"
    );
}
