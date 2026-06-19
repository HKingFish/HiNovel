package cn.haowl.hinovel.user.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 刷新 Token 请求 DTO。
 *
 * <p>用于通过 refresh token 获取新的访问令牌。</p>
 *
 * @author haowl
 * @since 2024
 */
@Data
public class RefreshTokenRequest {

    /**
     * 刷新令牌。
     */
    @NotBlank(message = "refreshToken 不能为空")
    private String refreshToken;
}
