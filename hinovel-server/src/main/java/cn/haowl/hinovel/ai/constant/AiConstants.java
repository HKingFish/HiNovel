package cn.haowl.hinovel.ai.constant;

/**
 * AI 模块通用常量。
 *
 * <p>集中管理 AI 模块中使用的常量值，避免魔法值散落在代码各处。</p>
 *
 * @author haowl
 */
public final class AiConstants {

    private AiConstants() {
    }

    // ==================== LLM 相关常量 ====================

    /**
     * LLM 请求超时时间（秒）
     */
    public static final int LLM_REQUEST_TIMEOUT_SECONDS = 120;

    /**
     * LLM 请求最大重试次数（网络异常、服务端 5xx 时自动重试）
     */
    public static final int LLM_MAX_RETRIES = 2;

    /**
     * Provider 连续失败次数阈值，超过后标记为不健康
     */
    public static final int PROVIDER_FAILURE_THRESHOLD = 3;

    /**
     * Provider 不健康状态恢复冷却时间（秒），冷却期过后自动尝试恢复
     */
    public static final int PROVIDER_RECOVERY_COOLDOWN_SECONDS = 120;

    /** 通义千问 OpenAI 兼容接口基础地址 */
    public static final String QWEN_COMPATIBLE_BASE_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1";

    /** 通义千问默认模型 */
    public static final String QWEN_DEFAULT_MODEL = "qwen2.5-omni-7b";

    /** 通义千问环境变量 API Key 标识 */
    public static final String QWEN_ENV_API_KEY = "aliQwen-api";

    // ==================== Embedding 相关常量 ====================

    /**
     * 阿里云百炼默认 Embedding 模型名称
     */
    public static final String DEFAULT_EMBEDDING_MODEL = "text-embedding-v3";

    /**
     * 阿里云百炼 text-embedding-v3 默认向量维度
     */
    public static final int DEFAULT_EMBEDDING_DIMENSIONS = 1024;

    /**
     * BGE-M3 ONNX 模型文件默认名称
     */
    public static final String BGE_M3_ONNX_MODEL_FILE = "model.onnx";

    /**
     * BGE-M3 ONNX 分词器文件默认名称
     */
    public static final String BGE_M3_ONNX_TOKENIZER_FILE = "tokenizer.json";

    /**
     * BGE-M3 默认向量维度
     */
    public static final int BGE_M3_EMBEDDING_DIMENSIONS = 1024;

    // ==================== 聊天记忆相关常量 ====================

    /**
     * 聊天记忆窗口最大消息数
     */
    public static final int CHAT_MEMORY_MAX_MESSAGES = 1000;

    // ==================== 内容长度限制 ====================

    /**
     * 请求/响应内容最大存储长度
     */
    public static final int MAX_CONTENT_LENGTH = 5000;
}
