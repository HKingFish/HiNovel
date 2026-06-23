package cn.haowl.hinovel.common.exception.enums;

import cn.haowl.hinovel.common.exception.ErrorCode;

/**
 * 全局通用错误码常量接口。
 *
 * <p>定义跨模块使用的通用错误码，如认证、授权、系统错误等。
 * 错误码范围：1-000-xxx-xxx</p>
 *
 * @author haowl
 * @see ErrorCode
 * @since 2024
 */
public interface GlobalErrorCodeConstants {

    // ==================== 通用错误码 [1-000-000-000 ~ 1-001-000-000) ====================

    // ==================== 系统级错误 ====================

    /**
     * 系统内部错误。
     */
    ErrorCode INTERNAL_ERROR = new ErrorCode(1_000_000_001, "系统内部错误，请稍后重试");

    /**
     * 系统繁忙，请稍后重试。
     */
    ErrorCode SYSTEM_BUSY = new ErrorCode(1_000_000_002, "系统繁忙，请稍后重试");

    // ==================== 参数校验错误 ====================

    /**
     * 参数错误。
     */
    ErrorCode PARAM_ERROR = new ErrorCode(1_000_001_001, "参数错误");

    /**
     * 参数不能为空。
     */
    ErrorCode PARAM_NOT_NULL = new ErrorCode(1_000_001_002, "参数 {0} 不能为空");

    /**
     * 参数格式错误。
     */
    ErrorCode PARAM_FORMAT_ERROR = new ErrorCode(1_000_001_003, "参数 {0} 格式错误");

    // ==================== 认证授权错误 [1-000-010-xxx] ====================

    /**
     * 未登录或登录已过期。
     */
    ErrorCode UNAUTHORIZED = new ErrorCode(1_000_010_001, "未登录或登录已过期");

    /**
     * Token 无效。
     */
    ErrorCode TOKEN_INVALID = new ErrorCode(1_000_010_002, "Token 无效");

    /**
     * Token 已过期。
     */
    ErrorCode TOKEN_EXPIRED = new ErrorCode(1_000_010_003, "Token 已过期");

    /**
     * 登录失败，邮箱或密码错误。
     */
    ErrorCode LOGIN_FAILED = new ErrorCode(1_000_010_004, "邮箱或密码错误");

    /**
     * 账号已被禁用。
     */
    ErrorCode ACCOUNT_DISABLED = new ErrorCode(1_000_010_005, "账号已被禁用");

    /**
     * Refresh Token 无效或已过期。
     */
    ErrorCode REFRESH_TOKEN_INVALID = new ErrorCode(1_000_010_006, "Refresh Token 无效或已过期");

    // ==================== 权限错误 [1-000-011-xxx] ====================

    /**
     * 无权限访问。
     */
    ErrorCode FORBIDDEN = new ErrorCode(1_000_011_001, "无权限访问");

    /**
     * 无权操作此资源。
     */
    ErrorCode FORBIDDEN_OPERATION = new ErrorCode(1_000_011_002, "无权操作此资源");

    // ==================== 资源错误 [1-000-020-xxx] ====================

    /**
     * 资源不存在。
     */
    ErrorCode NOT_FOUND = new ErrorCode(1_000_020_001, "资源不存在");

    /**
     * 资源不存在（兼容旧代码）。
     */
    ErrorCode RESOURCE_NOT_FOUND = new ErrorCode(1_000_020_001, "资源不存在");

    /**
     * 资源已存在。
     */
    ErrorCode ALREADY_EXISTS = new ErrorCode(1_000_020_002, "资源已存在");

    /**
     * 资源正在被使用，无法删除。
     */
    ErrorCode RESOURCE_IN_USE = new ErrorCode(1_000_020_003, "资源正在被使用，无法删除");

    // ==================== LLM/AI 错误 [1-000-030-xxx] ====================

    /**
     * AI 模型调用失败。
     */
    ErrorCode LLM_CALL_FAILED = new ErrorCode(1_000_030_001, "AI 模型调用失败，请稍后重试");

    /**
     * AI 模型服务不可用。
     */
    ErrorCode LLM_PROVIDER_UNAVAILABLE = new ErrorCode(1_000_030_002, "AI 模型服务不可用");

    /**
     * 请求频率超限。
     */
    ErrorCode LLM_RATE_LIMIT_EXCEEDED = new ErrorCode(1_000_030_003, "请求频率超限，请稍后重试");

    // ==================== 工具调用错误 [1-000-031-xxx] ====================

    /**
     * MCP 工具调用失败。
     */
    ErrorCode MCP_CALL_FAILED = new ErrorCode(1_000_031_001, "MCP 工具调用失败");

    /**
     * MCP Server 不可用。
     */
    ErrorCode MCP_SERVER_UNAVAILABLE = new ErrorCode(1_000_031_002, "MCP Server 不可用");

    /**
     * MCP Server 调用超时。
     */
    ErrorCode MCP_SERVER_TIMEOUT = new ErrorCode(1_000_031_003, "MCP Server 调用超时");

    // ==================== 基础设施错误 [1-000-040-xxx] ====================

    /**
     * 消息队列异常。
     */
    ErrorCode MESSAGE_QUEUE_ERROR = new ErrorCode(1_000_040_001, "消息队列异常");

    /**
     * 文件上传失败。
     */
    ErrorCode OSS_UPLOAD_FAILED = new ErrorCode(1_000_040_002, "文件上传失败，请稍后重试");

    /**
     * 文件删除失败。
     */
    ErrorCode OSS_DELETE_FAILED = new ErrorCode(1_000_040_003, "文件删除失败");

    /**
     * 文件大小超出限制。
     */
    ErrorCode OSS_FILE_TOO_LARGE = new ErrorCode(1_000_040_004, "文件大小超出限制");

    /**
     * 文件类型不允许上传。
     */
    ErrorCode OSS_FILE_TYPE_NOT_ALLOWED = new ErrorCode(1_000_040_005, "文件类型不允许上传");

    // ==================== 用户相关错误 [1-000-100-xxx] ====================

    /**
     * 邮箱已被注册。
     */
    ErrorCode EMAIL_ALREADY_EXISTS = new ErrorCode(1_000_100_001, "邮箱已被注册");

    /**
     * 用户名已被使用。
     */
    ErrorCode USERNAME_ALREADY_EXISTS = new ErrorCode(1_000_100_002, "用户名已被使用");

    /**
     * 原密码不正确。
     */
    ErrorCode ORIGINAL_PASSWORD_WRONG = new ErrorCode(1_000_100_003, "原密码不正确");

    /**
     * 密码长度不合规。
     */
    ErrorCode PASSWORD_FORMAT_ERROR = new ErrorCode(1_000_100_004, "密码长度不合规（6-20个字符）");

    /**
     * 用户名格式不合规。
     */
    ErrorCode USERNAME_FORMAT_ERROR = new ErrorCode(1_000_100_005, "用户名格式不合规（仅允许字母、数字、下划线，长度2-50）");

    /**
     * 邮箱格式不正确。
     */
    ErrorCode EMAIL_FORMAT_ERROR = new ErrorCode(1_000_100_006, "邮箱格式不正确");

    /**
     * 用户已被禁用。
     */
    ErrorCode USER_ALREADY_DISABLED = new ErrorCode(1_000_100_007, "用户已被禁用");

    /**
     * 用户已激活。
     */
    ErrorCode USER_ALREADY_ENABLED = new ErrorCode(1_000_100_008, "用户已激活");
}
