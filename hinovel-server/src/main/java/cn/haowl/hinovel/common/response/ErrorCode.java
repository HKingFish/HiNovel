package cn.haowl.hinovel.common.response;

import lombok.Getter;

/**
 * 错误码枚举。
 *
 * <p>messageKey 对应 i18n/messages.properties 中的键，用于国际化消息查找。</p>
 *
 * @author haowl
 * @since 2024
 */
@Getter
public enum ErrorCode {

    // ==================== 业务参数错误 40001-40099 ====================

    NOT_FOUND(40000, "error.40000", "资源不存在"),
    /**
     * 参数错误。
     */
    PARAM_ERROR(40001, "error.40001", "参数错误"),

    /**
     * 邮箱已被注册。
     */
    EMAIL_ALREADY_EXISTS(40002, "error.40002", "邮箱已被注册"),

    /**
     * 用户名已被使用。
     */
    USERNAME_ALREADY_EXISTS(40003, "error.40003", "用户名已被使用"),

    /**
     * 密码长度不能少于6个字符。
     */
    PASSWORD_TOO_SHORT(40004, "error.40004", "密码长度不能少于6个字符"),

    /**
     * 用户名格式不合规。
     */
    USERNAME_FORMAT_ERROR(40005, "error.40005", "用户名格式不合规（仅允许字母、数字、下划线，长度2-50）"),

    /**
     * 邮箱格式不正确。
     */
    EMAIL_FORMAT_ERROR(40006, "error.40006", "邮箱格式不正确"),

    /**
     * 资源不存在。
     */
    RESOURCE_NOT_FOUND(40007, "error.40007", "资源不存在"),

    /**
     * 原密码不正确。
     */
    ORIGINAL_PASSWORD_WRONG(40008, "error.40008", "原密码不正确"),

    /**
     * 原密码错误。
     */
    OLD_PASSWORD_ERROR(40009, "error.40009", "原密码错误"),

    /**
     * 用户已被禁用。
     */
    USER_ALREADY_DISABLED(40010, "error.40010", "用户已被禁用"),

    /**
     * 用户已激活。
     */
    USER_ALREADY_ENABLED(40011, "error.40011", "用户已激活"),

    /**
     * 邮箱无效。
     */
    EMAIL_INVALID(40012, "error.40012", "邮箱无效"),

    /**
     * 密码无效。
     */
    PASSWORD_INVALID(40013, "error.40013", "密码无效"),

    /**
     * 密码长度不能超过20个字符。
     */
    PASSWORD_TOO_LONG(40014, "error.40014", "密码长度不能超过20个字符"),

    // ==================== 认证授权错误 40101-40199 ====================

    /**
     * 未登录或登录已过期。
     */
    UNAUTHORIZED(40101, "error.40101", "未登录或登录已过期"),

    /**
     * Token无效。
     */
    TOKEN_INVALID(40102, "error.40102", "Token无效"),

    /**
     * Token已过期。
     */
    TOKEN_EXPIRED(40103, "error.40103", "Token已过期"),

    /**
     * 邮箱或密码错误。
     */
    LOGIN_FAILED(40104, "error.40104", "邮箱或密码错误"),

    /**
     * 账号已被禁用。
     */
    ACCOUNT_DISABLED(40105, "error.40105", "账号已被禁用"),

    /**
     * Refresh Token无效或已过期。
     */
    REFRESH_TOKEN_INVALID(40106, "error.40106", "Refresh Token无效或已过期"),

    // ==================== 权限不足 40301-40399 ====================

    /**
     * 无权限访问。
     */
    FORBIDDEN(40301, "error.40301", "无权限访问"),

    /**
     * 无权操作此Agent。
     */
    AGENT_NOT_OWNED(40302, "error.40302", "无权操作此Agent"),

    // ==================== LLM 调用错误 50001-50099 ====================

    /**
     * AI模型调用失败。
     */
    LLM_CALL_FAILED(50001, "error.50001", "AI模型调用失败，请稍后重试"),

    /**
     * AI模型服务不可用。
     */
    LLM_PROVIDER_UNAVAILABLE(50002, "error.50002", "AI模型服务不可用"),

    /**
     * 请求频率超限。
     */
    LLM_RATE_LIMIT_EXCEEDED(50003, "error.50003", "请求频率超限，每分钟最多60次，请稍后重试"),

    // ==================== MCP 工具调用错误 50101-50199 ====================

    /**
     * MCP工具调用失败。
     */
    MCP_CALL_FAILED(50101, "error.50101", "MCP工具调用失败"),

    /**
     * MCP Server不可用。
     */
    MCP_SERVER_UNAVAILABLE(50102, "error.50102", "MCP Server不可用"),

    /**
     * MCP Server调用超时。
     */
    MCP_SERVER_TIMEOUT(50103, "error.50103", "MCP Server调用超时"),

    // ==================== 系统内部错误 50201-50299 ====================

    /**
     * 系统内部错误。
     */
    INTERNAL_ERROR(50201, "error.50201", "系统内部错误，请联系管理员"),

    /**
     * 消息队列异常。
     */
    MESSAGE_QUEUE_ERROR(50202, "error.50202", "消息队列异常"),

    // ==================== OSS 文件存储错误 50301-50399 ====================

    /**
     * 文件上传失败。
     */
    OSS_UPLOAD_FAILED(50301, "error.50301", "文件上传失败，请稍后重试"),

    /**
     * 文件删除失败。
     */
    OSS_DELETE_FAILED(50302, "error.50302", "文件删除失败"),

    /**
     * 文件大小超出限制。
     */
    OSS_FILE_TOO_LARGE(50303, "error.50303", "文件大小超出限制"),

    /**
     * 文件类型不允许上传。
     */
    OSS_FILE_TYPE_NOT_ALLOWED(50304, "error.50304", "文件类型不允许上传");

    /**
     * 错误码。
     */
    private final int code;

    /**
     * 国际化消息键，对应 i18n/messages.properties。
     */
    private final String messageKey;

    /**
     * 默认消息（无法加载 i18n 时的回退值）。
     */
    private final String defaultMessage;

    ErrorCode(int code, String messageKey, String defaultMessage) {
        this.code = code;
        this.messageKey = messageKey;
        this.defaultMessage = defaultMessage;
    }

    /**
     * 兼容旧代码，返回默认消息。
     *
     * @return 默认消息
     */
    public String getMessage() {
        return defaultMessage;
    }
}
