package cn.haowl.hinovel.user.domain.entity;

import cn.haowl.hinovel.common.entity.BaseEntity;
import cn.haowl.hinovel.common.exception.BusinessException;
import cn.haowl.hinovel.common.response.ErrorCode;
import cn.haowl.hinovel.user.constant.UserRole;
import cn.haowl.hinovel.user.constant.UserStatus;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Objects;

/**
 * 用户实体。
 *
 * <p>用户领域模型，包含用户基本信息和相关业务行为。</p>
 *
 * @Author : haowl
 * @Date : 2026/3/12 22:30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("user")
public class User extends BaseEntity {

    /**
     * 用户名，唯一标识用户。
     */
    private String username;

    /**
     * 邮箱地址，用于登录和通知。
     */
    private String email;

    /**
     * BCrypt 加密存储的密码。
     */
    private String password;

    /**
     * 用户头像 URL。
     */
    private String avatarUrl;

    /**
     * 用户角色。
     *
     * @see UserRole
     */
    private String role;

    /**
     * 账号状态。
     *
     * @see UserStatus
     */
    private String status;

    /**
     * 创建新用户。
     *
     * @param username        用户名
     * @param email           邮箱
     * @param rawPassword     明文密码
     * @param passwordEncoder 密码编码器
     * @return 用户实体
     */
    public static User create(String username, String email, String rawPassword, PasswordEncoder passwordEncoder) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(UserRole.USER.name());
        user.setStatus(UserStatus.ACTIVE.name());
        return user;
    }

    /**
     * 修改密码。
     *
     * @param oldPassword     旧密码
     * @param newPassword     新密码
     * @param passwordEncoder 密码编码器
     * @return 是否修改成功
     */
    public boolean changePassword(String oldPassword, String newPassword, PasswordEncoder passwordEncoder) {
        if (!passwordEncoder.matches(oldPassword, this.password)) {
            throw new BusinessException(ErrorCode.OLD_PASSWORD_ERROR);
        }
        this.password = passwordEncoder.encode(newPassword);
        return true;
    }

    /**
     * 禁用用户。
     */
    public void disable() {
        if (UserStatus.DISABLED.name().equals(this.status)) {
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
        }
        this.status = UserStatus.DISABLED.name();
    }

    /**
     * 启用用户。
     */
    public void enable() {
        if (UserStatus.ACTIVE.name().equals(this.status)) {
            return;
        }
        this.status = UserStatus.ACTIVE.name();
    }

    /**
     * 验证密码。
     *
     * @param rawPassword     明文密码
     * @param passwordEncoder 密码编码器
     * @return 是否匹配
     */
    public boolean verifyPassword(String rawPassword, PasswordEncoder passwordEncoder) {
        return passwordEncoder.matches(rawPassword, this.password);
    }

    /**
     * 判断用户是否被禁用。
     *
     * @return 是否被禁用
     */
    public boolean isDisabled() {
        return UserStatus.DISABLED.name().equals(this.status);
    }

    /**
     * 判断用户是否激活。
     *
     * @return 是否激活
     */
    public boolean isActive() {
        return UserStatus.ACTIVE.name().equals(this.status);
    }

    /**
     * 判断用户是否为管理员。
     *
     * @return 是否为管理员
     */
    public boolean isAdmin() {
        return UserRole.ADMIN.name().equals(this.role);
    }

    /**
     * 更新头像。
     *
     * @param avatarUrl 头像URL
     */
    public void updateAvatar(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    /**
     * 更新邮箱。
     *
     * @param email 新邮箱
     */
    public void updateEmail(String email) {
        this.email = email;
    }

    /**
     * 判断两个用户是否相同。
     *
     * @param obj 比较对象
     * @return 是否相同
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        User that = (User) obj;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
