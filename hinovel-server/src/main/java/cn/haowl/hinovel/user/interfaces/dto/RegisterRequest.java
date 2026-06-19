package cn.haowl.hinovel.user.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册请求 DTO。
 *
 * <p>包含用户注册所需的基本信息。</p>
 *
 * @author haowl
 * @since 2024
 */
@Schema(description = "用户注册请求")
@Data
public class RegisterRequest {

    /**
     * 用户名（仅允许字母、数字、下划线，长度2-50）。
     */
    @Schema(description = "用户名（仅允许字母、数字、下划线，长度2-50）", example = "john_doe", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_]{2,50}$",
            message = "用户名格式不合规（仅允许字母、数字、下划线，长度2-50）")
    private String username;

    /**
     * 邮箱地址。
     */
    @Schema(description = "邮箱地址", example = "john@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 登录密码（至少8位）。
     */
    @Schema(description = "登录密码（至少8位）", example = "MyPassword123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, message = "密码长度不能少于8个字符")
    private String password;
}
