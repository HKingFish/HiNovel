package cn.haowl.hinovel.agent.constant;

/**
 * Agent 模块常量类。
 *
 * <p>定义 Agent 模块相关的常量配置。</p>
 *
 * @author haowl
 * @since 2024
 */
public final class AgentConstants {

    private AgentConstants() {
    }

    /**
     * Redis Agent 配置键前缀。
     */
    public static final String REDIS_KEY_PREFIX_AGENT_CONFIG = "agent:config:";

    /**
     * Redis 限流键前缀。
     */
    public static final String REDIS_KEY_PREFIX_RATE_LIMIT = "rate:limit:";

    /**
     * Agent 缓存 TTL（秒）。
     */
    public static final long AGENT_CACHE_TTL_SECONDS = 1800L;

    /**
     * 默认最大迭代次数。
     */
    public static final int DEFAULT_MAX_ITERATIONS = 10;

    /**
     * 默认历史消息限制。
     */
    public static final int DEFAULT_HISTORY_MESSAGE_LIMIT = 20;

    /**
     * 默认标题最大长度。
     */
    public static final int DEFAULT_TITLE_MAX_LENGTH = 50;

    /**
     * SSE 超时时间（毫秒）。
     */
    public static final long SSE_TIMEOUT_MS = 60_000L;

    /**
     * SSE 扩展超时时间（毫秒）。
     */
    public static final long SSE_TIMEOUT_EXTENDED_MS = 120_000L;

    /**
     * 内置 Agent：小说创作助手。
     */
    public static final String BUILTIN_AGENT_NOVEL_ASSISTANT = "小说创作助手";

    /**
     * 内置 Agent：面试助手。
     */
    public static final String BUILTIN_AGENT_INTERVIEW_ASSISTANT = "面试助手";

    /**
     * 默认温度参数。
     */
    public static final double DEFAULT_TEMPERATURE = 0.7;

    /**
     * 编辑 Agent 默认温度参数。
     */
    public static final double EDITOR_DEFAULT_TEMPERATURE = 0.3;

    /**
     * 默认最大 Token 数。
     */
    public static final int DEFAULT_MAX_TOKENS = 4000;

    /**
     * 编辑 Agent 默认最大 Token 数。
     */
    public static final int EDITOR_DEFAULT_MAX_TOKENS = 2000;

    /**
     * 默认版本查询限制。
     */
    public static final int DEFAULT_VERSION_LIMIT = 50;

    /**
     * 大纲预览长度。
     */
    public static final int OUTLINE_PREVIEW_LENGTH = 100;

    /**
     * 场景预览长度。
     */
    public static final int SCENE_PREVIEW_LENGTH = 30;

    /**
     * 剧情要点最大数量。
     */
    public static final int MAX_PLOT_POINTS = 3;

    /**
     * 剧情要点最小长度。
     */
    public static final int MIN_PLOT_POINT_LENGTH = 10;

    /**
     * 剧情要点预览长度。
     */
    public static final int PLOT_POINT_PREVIEW_LENGTH = 50;

    /**
     * 审核通过状态。
     */
    public static final String AUDIT_PASS = "pass";

    /**
     * 审核失败状态。
     */
    public static final String AUDIT_FAIL = "fail";

    // ==================== 小说配置默认值 ====================

    /**
     * AI 改写时默认携带的前几章数量。
     */
    public static final int DEFAULT_REWRITE_CONTEXT_CHAPTERS = 2;

    /**
     * AI 问答时默认携带的历史消息条数。
     */
    public static final int DEFAULT_QA_CONTEXT_LENGTH = 10;

    // ==================== 频率限制相关常量 ====================

    /**
     * 频率限制窗口时长（分钟）。
     */
    public static final int RATE_LIMIT_WINDOW_MINUTES = 1;

    /**
     * 首次请求计数值（用于判断是否需要设置过期时间）。
     */
    public static final long FIRST_REQUEST_COUNT = 1L;
}
