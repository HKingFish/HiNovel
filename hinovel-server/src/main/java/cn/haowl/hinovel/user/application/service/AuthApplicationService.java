package cn.haowl.hinovel.user.application.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.haowl.hinovel.user.application.command.LoginCommand;
import cn.haowl.hinovel.user.application.command.RegisterCommand;
import cn.haowl.hinovel.user.application.query.UserQuery;
import cn.haowl.hinovel.user.constant.AuthConstants;
import cn.haowl.hinovel.user.domain.entity.User;
import cn.haowl.hinovel.user.domain.repository.UserRepository;
import cn.haowl.hinovel.user.domain.service.AuthDomainService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static cn.haowl.hinovel.common.exception.enums.GlobalErrorCodeConstants.NOT_FOUND;
import static cn.haowl.hinovel.common.exception.enums.GlobalErrorCodeConstants.PARAM_ERROR;
import static cn.haowl.hinovel.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 认证应用服务。
 *
 * 协调用户认证相关的业务用例，负责流程编排和事务管理。
 *
 * @Author : haowl
 * @Date : 2026/3/12 22:30
 */
@Slf4j
@Service
public class AuthApplicationService {

    /**
     * 认证领域服务。
     */
    @Autowired
    private AuthDomainService authDomainService;

    /**
     * 用户仓储。
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * 用户注册。
     *
     * @param command 注册命令
     */
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterCommand command) {
        User user = authDomainService.register(command.getUsername(), command.getEmail(), command.getPassword());
        log.info("[register][用户注册成功，userId={}]", user.getId());
    }

    /**
     * 用户登录。
     *
     * @param command 登录命令
     * @return Token 响应
     */
    public LoginResult login(LoginCommand command) {
        User user = authDomainService.authenticate(command.getAccount(), command.getPassword());

        String accessToken = issueAccessToken(user.getId());

        log.info("[login][用户登录成功，userId={}]", user.getId());
        return new LoginResult(accessToken, AuthConstants.DEFAULT_TOKEN_EXPIRES_SECONDS, user);
    }

    /**
     * 使用邮箱密码授权并签发令牌。
     *
     * @param email    授权账号邮箱
     * @param password 明文密码
     * @return 访问令牌，凭据无效时为空
     */
    public String authorizeWithPassword(String email, String password) {
        return authDomainService.authenticateByEmail(email, password)
            .map(user -> issueAccessToken(user.getId()))
            .orElse(null);
    }

    /**
     * 为通过认证的用户创建会话并返回令牌。
     */
    private String issueAccessToken(Long userId) {
        StpUtil.login(userId);
        return StpUtil.getTokenValue();
    }

    /**
     * 用户登出。
     */
    public void logout() {
        Long userId = StpUtil.getLoginIdAsLong();
        StpUtil.logout();
        log.info("[logout][用户登出成功: userId={}]", userId);
    }

    /**
     * 获取当前用户信息。
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    public User getCurrentUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> exception(NOT_FOUND));
    }

    /**
     * 根据查询条件获取用户。
     *
     * @param query 查询条件
     * @return 用户实体
     */
    public User getUser(UserQuery query) {
        if (query.getUserId() != null) {
            return userRepository.findById(query.getUserId())
                .orElseThrow(() -> exception(NOT_FOUND));
        }
        if (query.getUsername() != null) {
            return userRepository.findByUsername(query.getUsername())
                .orElseThrow(() -> exception(NOT_FOUND));
        }
        if (query.getEmail() != null) {
            return userRepository.findByEmail(query.getEmail())
                .orElseThrow(() -> exception(NOT_FOUND));
        }
        throw exception(PARAM_ERROR);
    }

    /**
     * 用户登录结果。
     */
    @Getter
    @AllArgsConstructor
    public static class LoginResult {

        /**
         * 已签发的访问令牌。
         */
        private final String accessToken;

        /**
         * 访问令牌有效期（秒）。
         */
        private final long expiresIn;

        /**
         * 通过认证的用户。
         */
        private final User user;
    }
}
