package cn.haowl.hinovel.user.constant;

/**
 * 认证模块常量。
 *
 * <p>定义认证相关的常量配置。</p>
 *
 * @author haowl
 * @since 2024
 */
public final class AuthConstants {

    private AuthConstants() {
    }

    /**
     * 默认 Token 有效期（秒）。
     */
    public static final long DEFAULT_TOKEN_EXPIRES_SECONDS = 7200L;

    /**
     * 默认管理员用户名。
     */
    public static final String DEFAULT_ADMIN_USERNAME = "admin";

    /**
     * 默认管理员邮箱。
     */
    public static final String DEFAULT_ADMIN_EMAIL = "admin@hinovel.com";

    /**
     * 默认管理员密码。
     */
    public static final String DEFAULT_ADMIN_PASSWORD = "Admin@123456";
}
