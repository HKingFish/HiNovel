package cn.haowl.hinovel.user.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录成功响应 DTO。
 *
 * <p>包含访问令牌和用户基本信息。</p>
 *
 * @author haowl
 * @since 2024
 */
@Schema(description = "登录成功响应")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenResponse {

    /**
     * Sa-Token 生成的访问令牌。
     */
    @Schema(description = "Sa-Token 生成的访问令牌")
    private String accessToken;

    /**
     * Token 有效期（秒）。
     */
    @Schema(description = "Token 有效期（秒）", example = "7200")
    private long expiresIn;

    /**
     * 当前登录用户信息。
     */
    @Schema(description = "当前登录用户信息")
    private UserInfo userInfo;

    /**
     * 用户基本信息（嵌套 DTO）。
     *
     * @author haowl
     * @since 2024
     */
    @Schema(description = "用户基本信息")
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserInfo {

        /**
         * 用户ID。
         */
        @Schema(description = "用户ID", example = "1")
        private Long id;

        /**
         * 用户名。
         */
        @Schema(description = "用户名", example = "john_doe")
        private String username;

        /**
         * 邮箱。
         */
        @Schema(description = "邮箱", example = "john@example.com")
        private String email;

        /**
         * 角色：USER/ADMIN。
         */
        @Schema(description = "角色：USER/ADMIN", example = "USER")
        private String role;

        /**
         * 头像URL。
         */
        @Schema(description = "头像URL")
        private String avatarUrl;
    }
}
