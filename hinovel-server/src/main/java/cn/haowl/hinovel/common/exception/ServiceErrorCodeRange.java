package cn.haowl.hinovel.common.exception;

/**
 * 业务异常的错误码区间定义。
 *
 * <p>用于避免各模块错误码重复定义，在此声明各模块的错误码范围。
 * </p>
 *
 * <p>错误码格式：1-SSS-MMM-EEE
 * <ul>
 *   <li>SSS：系统类型</li>
 *   <li>MMM：模块细分</li>
 *   <li>EEE：错误序号</li>
 * </ul>
 * </p>
 *
 * @author haowl
 * @see ErrorCode
 * @since 2024
 */
public final class ServiceErrorCodeRange {

    private ServiceErrorCodeRange() {
    }

    // ==================== 系统类型定义 ====================

    /**
     * 用户系统类型编码。
     */
    public static final int USER_SYSTEM = 1;

    /**
     * 小说系统类型编码。
     */
    public static final int NOVEL_SYSTEM = 2;

    /**
     * AI Agent 系统类型编码。
     */
    public static final int AGENT_SYSTEM = 3;

    /**
     * AI 配置系统类型编码。
     */
    public static final int AI_SYSTEM = 4;

    /**
     * 基础设施系统类型编码。
     */
    public static final int INFRA_SYSTEM = 5;

    // ==================== 各系统错误码区间 ====================

    // ==================== User 用户系统 [1-001-000-000 ~ 1-002-000-000) ====================

    /**
     * User 用户系统错误码区间起始值。
     */
    public static final int USER_SYSTEM_ERROR_CODE_MIN = 1_001_000_000;

    /**
     * User 用户系统错误码区间结束值（不包含）。
     */
    public static final int USER_SYSTEM_ERROR_CODE_MAX = 1_002_000_000;

    // ==================== Novel 小说系统 [1-002-000-000 ~ 1-003-000-000) ====================

    /**
     * Novel 小说系统错误码区间起始值。
     */
    public static final int NOVEL_SYSTEM_ERROR_CODE_MIN = 1_002_000_000;

    /**
     * Novel 小说系统错误码区间结束值（不包含）。
     */
    public static final int NOVEL_SYSTEM_ERROR_CODE_MAX = 1_003_000_000;

    // ==================== Agent AI Agent 系统 [1-003-000-000 ~ 1-004-000-000) ====================

    /**
     * Agent AI Agent 系统错误码区间起始值。
     */
    public static final int AGENT_SYSTEM_ERROR_CODE_MIN = 1_003_000_000;

    /**
     * Agent AI Agent 系统错误码区间结束值（不包含）。
     */
    public static final int AGENT_SYSTEM_ERROR_CODE_MAX = 1_004_000_000;

    // ==================== AI AI 配置系统 [1-004-000-000 ~ 1-005-000-000) ====================

    /**
     * AI AI 配置系统错误码区间起始值。
     */
    public static final int AI_SYSTEM_ERROR_CODE_MIN = 1_004_000_000;

    /**
     * AI AI 配置系统错误码区间结束值（不包含）。
     */
    public static final int AI_SYSTEM_ERROR_CODE_MAX = 1_005_000_000;

    // ==================== Infra 基础设施系统 [1-005-000-000 ~ 1-006-000-000) ====================

    /**
     * Infra 基础设施系统错误码区间起始值。
     */
    public static final int INFRA_SYSTEM_ERROR_CODE_MIN = 1_005_000_000;

    /**
     * Infra 基础设施系统错误码区间结束值（不包含）。
     */
    public static final int INFRA_SYSTEM_ERROR_CODE_MAX = 1_006_000_000;

    // ==================== 各系统内模块错误码区间 ====================

    // ==================== User 系统模块 ====================

    /**
     * User - OAuth 模块错误码区间 [1-001-001-000 ~ 1-001-002-000)。
     */
    public static final int USER_OAUTH_ERROR_CODE_MIN = 1_001_001_000;
    public static final int USER_OAUTH_ERROR_CODE_MAX = 1_001_002_000;

    /**
     * User - User 模块错误码区间 [1-001-002-000 ~ 1-001-003-000)。
     */
    public static final int USER_USER_ERROR_CODE_MIN = 1_001_002_000;
    public static final int USER_USER_ERROR_CODE_MAX = 1_001_003_000;

    // ==================== Novel 系统模块 ====================

    /**
     * Novel - 小说模块错误码区间 [1-002-001-000 ~ 1-002-002-000)。
     */
    public static final int NOVEL_NOVEL_ERROR_CODE_MIN = 1_002_001_000;
    public static final int NOVEL_NOVEL_ERROR_CODE_MAX = 1_002_002_000;

    /**
     * Novel - 章节模块错误码区间 [1-002-002-000 ~ 1-002-003-000)。
     */
    public static final int NOVEL_CHAPTER_ERROR_CODE_MIN = 1_002_002_000;
    public static final int NOVEL_CHAPTER_ERROR_CODE_MAX = 1_002_003_000;

    /**
     * Novel - 大纲模块错误码区间 [1-002-003-000 ~ 1-002-004-000)。
     */
    public static final int NOVEL_OUTLINE_ERROR_CODE_MIN = 1_002_003_000;
    public static final int NOVEL_OUTLINE_ERROR_CODE_MAX = 1_002_004_000;

    /**
     * Novel - 角色模块错误码区间 [1-002-004-000 ~ 1-002-005-000)。
     */
    public static final int NOVEL_CHARACTER_ERROR_CODE_MIN = 1_002_004_000;
    public static final int NOVEL_CHARACTER_ERROR_CODE_MAX = 1_002_005_000;

    // ==================== Agent 系统模块 ====================

    /**
     * Agent - AI 审核模块错误码区间 [1-003-001-000 ~ 1-003-002-000)。
     */
    public static final int AGENT_AUDIT_ERROR_CODE_MIN = 1_003_001_000;
    public static final int AGENT_AUDIT_ERROR_CODE_MAX = 1_003_002_000;

    /**
     * Agent - AI 改写模块错误码区间 [1-003-002-000 ~ 1-003-003-000)。
     */
    public static final int AGENT_REWRITE_ERROR_CODE_MIN = 1_003_002_000;
    public static final int AGENT_REWRITE_ERROR_CODE_MAX = 1_003_003_000;

    /**
     * Agent - AI 问答模块错误码区间 [1-003-003-000 ~ 1-003-004-000)。
     */
    public static final int AGENT_CHAT_ERROR_CODE_MIN = 1_003_003_000;
    public static final int AGENT_CHAT_ERROR_CODE_MAX = 1_003_004_000;

    /**
     * Agent - Agent 配置模块错误码区间 [1-003-010-000 ~ 1-003-011-000)。
     */
    public static final int AGENT_CONFIG_ERROR_CODE_MIN = 1_003_010_000;
    public static final int AGENT_CONFIG_ERROR_CODE_MAX = 1_003_011_000;

    // ==================== AI 系统模块 ====================

    /**
     * AI - LLM Provider 模块错误码区间 [1-004-001-000 ~ 1-004-002-000)。
     */
    public static final int AI_LLM_PROVIDER_ERROR_CODE_MIN = 1_004_001_000;
    public static final int AI_LLM_PROVIDER_ERROR_CODE_MAX = 1_004_002_000;

    /**
     * AI - Embedding 配置模块错误码区间 [1-004-002-000 ~ 1-004-003-000)。
     */
    public static final int AI_EMBEDDING_ERROR_CODE_MIN = 1_004_002_000;
    public static final int AI_EMBEDDING_ERROR_CODE_MAX = 1_004_003_000;

    // ==================== Infra 系统模块 ====================

    /**
     * Infra - OSS 存储模块错误码区间 [1-005-001-000 ~ 1-005-002-000)。
     */
    public static final int INFRA_OSS_ERROR_CODE_MIN = 1_005_001_000;
    public static final int INFRA_OSS_ERROR_CODE_MAX = 1_005_002_000;

    /**
     * Infra - MQ 消息队列模块错误码区间 [1-005-002-000 ~ 1-005-003-000)。
     */
    public static final int INFRA_MQ_ERROR_CODE_MIN = 1_005_002_000;
    public static final int INFRA_MQ_ERROR_CODE_MAX = 1_005_003_000;
}
