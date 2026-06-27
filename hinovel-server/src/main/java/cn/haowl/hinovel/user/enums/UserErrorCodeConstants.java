package cn.haowl.hinovel.user.enums;

import cn.haowl.hinovel.common.exception.ErrorCode;

import static cn.haowl.hinovel.common.exception.ServiceErrorCodeRange.USER_OAUTH_ERROR_CODE_MIN;
import static cn.haowl.hinovel.common.exception.ServiceErrorCodeRange.USER_USER_ERROR_CODE_MIN;

/**
 * User 用户模块错误码常量接口。
 *
 * <p>定义 User 用户模块相关的错误码。
 * 错误码范围：[1-001-000-000 ~ 1-002-000-000)</p>
 *
 * @author haowl
 * @see ErrorCode
 * @since 2024
 */
public interface UserErrorCodeConstants {

    // ==================== User OAuth 模块 [1-001-001-xxx] ====================

    /**
     * 登录失败，邮箱或密码错误。
     */
    ErrorCode LOGIN_FAILED = new ErrorCode(
        USER_OAUTH_ERROR_CODE_MIN + 1,
        "邮箱或密码错误"
    );

    /**
     * 账号已被禁用。
     */
    ErrorCode ACCOUNT_DISABLED = new ErrorCode(
        USER_OAUTH_ERROR_CODE_MIN + 2,
        "账号已被禁用"
    );

    /**
     * Refresh Token 无效或已过期。
     */
    ErrorCode REFRESH_TOKEN_INVALID = new ErrorCode(
        USER_OAUTH_ERROR_CODE_MIN + 3,
        "Refresh Token 无效或已过期"
    );

    /**
     * 登录已过期，请重新登录。
     */
    ErrorCode LOGIN_EXPIRED = new ErrorCode(
        USER_OAUTH_ERROR_CODE_MIN + 4,
        "登录已过期，请重新登录"
    );

    /**
     * Token 无效。
     */
    ErrorCode TOKEN_INVALID = new ErrorCode(
        USER_OAUTH_ERROR_CODE_MIN + 5,
        "Token 无效"
    );

    // ==================== User User 模块 [1-001-002-xxx] ====================

    /**
     * 用户不存在。
     */
    ErrorCode USER_NOT_EXISTS = new ErrorCode(
        USER_USER_ERROR_CODE_MIN + 1,
        "用户不存在"
    );

    /**
     * 邮箱已被注册。
     */
    ErrorCode EMAIL_ALREADY_EXISTS = new ErrorCode(
        USER_USER_ERROR_CODE_MIN + 2,
        "邮箱已被注册"
    );

    /**
     * 用户名已被使用。
     */
    ErrorCode USERNAME_ALREADY_EXISTS = new ErrorCode(
        USER_USER_ERROR_CODE_MIN + 3,
        "用户名已被使用"
    );

    /**
     * 原密码不正确。
     */
    ErrorCode ORIGINAL_PASSWORD_WRONG = new ErrorCode(
        USER_USER_ERROR_CODE_MIN + 4,
        "原密码不正确"
    );

    /**
     * 密码长度不合规（6-20个字符）。
     */
    ErrorCode PASSWORD_FORMAT_ERROR = new ErrorCode(
        USER_USER_ERROR_CODE_MIN + 5,
        "密码长度不合规（6-20个字符）"
    );

    /**
     * 用户名格式不合规。
     */
    ErrorCode USERNAME_FORMAT_ERROR = new ErrorCode(
        USER_USER_ERROR_CODE_MIN + 6,
        "用户名格式不合规（仅允许字母、数字、下划线，长度2-50）"
    );

    /**
     * 邮箱格式不正确。
     */
    ErrorCode EMAIL_FORMAT_ERROR = new ErrorCode(
        USER_USER_ERROR_CODE_MIN + 7,
        "邮箱格式不正确"
    );

    /**
     * 用户已被禁用。
     */
    ErrorCode USER_ALREADY_DISABLED = new ErrorCode(
        USER_USER_ERROR_CODE_MIN + 8,
        "用户已被禁用"
    );

    /**
     * 用户已激活。
     */
    ErrorCode USER_ALREADY_ENABLED = new ErrorCode(
        USER_USER_ERROR_CODE_MIN + 9,
        "用户已激活"
    );

    /**
     * 两次密码输入不一致。
     */
    ErrorCode PASSWORD_NOT_MATCH = new ErrorCode(
        USER_USER_ERROR_CODE_MIN + 10,
        "两次密码输入不一致"
    );

    /**
     * 无权操作此用户。
     */
    ErrorCode USER_NOT_OWNED = new ErrorCode(
        USER_USER_ERROR_CODE_MIN + 11,
        "无权操作此用户"
    );
}
