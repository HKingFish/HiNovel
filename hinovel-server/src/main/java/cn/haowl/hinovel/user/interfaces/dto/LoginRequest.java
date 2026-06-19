package cn.haowl.hinovel.user.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求 DTO。
 *
 * <p>account 字段兼容用户名和邮箱两种登录方式。</p>
 *
 * @author haowl
 * @since 2024
 */
@Schema(description = "用户登录请求")
@Data
public class LoginRequest {

    /**
     * 账号（用户名或邮箱）。
     */
    @Schema(description = "账号（用户名或邮箱）", example = "admin 或 admin@hinovel.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "账号不能为空")
    private String account;

    /**
     * 登录密码（明文，传输层加密）。
     */
    @Schema(description = "登录密码（明文，传输层加密）", example = "Admin@123456", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "密码不能为空")
    private String password;
}
