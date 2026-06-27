package cn.haowl.hinovel.user.domain.service;

import cn.haowl.hinovel.user.domain.entity.User;
import cn.haowl.hinovel.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static cn.haowl.hinovel.common.exception.enums.GlobalErrorCodeConstants.NOT_FOUND;
import static cn.haowl.hinovel.common.exception.util.ServiceExceptionUtil.exception;
import static cn.haowl.hinovel.user.enums.UserErrorCodeConstants.*;

/**
 * 认证领域服务。
 *
 * <p>处理用户认证相关的业务逻辑。</p>
 *
 * @Author : haowl
 * @Date : 2026/3/12 22:30
 */
@Service
@RequiredArgsConstructor
public class AuthDomainService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 注册新用户。
     *
     * @param username 用户名
     * @param email    邮箱
     * @param password 密码
     * @return 用户实体
     */
    public User register(String username, String email, String password) {
        if (userRepository.existsByEmail(email)) {
            throw exception(EMAIL_ALREADY_EXISTS);
        }
        if (userRepository.existsByUsername(username)) {
            throw exception(USERNAME_ALREADY_EXISTS);
        }
        User user = User.create(username, email, password, passwordEncoder);
        return userRepository.save(user);
    }

    /**
     * 用户登录验证。
     *
     * @param account  账号（用户名或邮箱）
     * @param password 密码
     * @return 用户实体
     */
    public User authenticate(String account, String password) {
        User user = userRepository.findByUsernameOrEmail(account)
            .orElseThrow(() -> exception(LOGIN_FAILED));

        if (!user.verifyPassword(password, passwordEncoder)) {
            throw exception(LOGIN_FAILED);
        }

        if (user.isDisabled()) {
            throw exception(ACCOUNT_DISABLED);
        }

        return user;
    }

    /**
     * 修改用户密码。
     *
     * @param userId      用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> exception(NOT_FOUND));
        user.changePassword(oldPassword, newPassword, passwordEncoder);
        userRepository.save(user);
    }
}
