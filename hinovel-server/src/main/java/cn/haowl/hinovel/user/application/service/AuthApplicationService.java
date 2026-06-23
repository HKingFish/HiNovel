package cn.haowl.hinovel.user.application.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.haowl.hinovel.common.exception.BusinessException;
import cn.haowl.hinovel.common.exception.enums.GlobalErrorCodeConstants;
import cn.haowl.hinovel.user.application.command.LoginCommand;
import cn.haowl.hinovel.user.application.command.RegisterCommand;
import cn.haowl.hinovel.user.application.query.UserQuery;
import cn.haowl.hinovel.user.constant.AuthConstants;
import cn.haowl.hinovel.user.domain.entity.User;
import cn.haowl.hinovel.user.domain.repository.UserRepository;
import cn.haowl.hinovel.user.domain.service.AuthDomainService;
import cn.haowl.hinovel.user.interfaces.dto.TokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证应用服务。
 *
 * <p>协调用户认证相关的业务用例，负责流程编排和事务管理。</p>
 *
 * @Author : haowl
 * @Date : 2026/3/12 22:30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthApplicationService {

    private final AuthDomainService authDomainService;
    private final UserRepository userRepository;

    /**
     * 用户注册。
     *
     * @param command 注册命令
     */
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterCommand command) {
        log.info("用户注册: username={}, email={}", command.getUsername(), command.getEmail());
        authDomainService.register(command.getUsername(), command.getEmail(), command.getPassword());
        log.info("用户注册成功: username={}", command.getUsername());
    }

    /**
     * 用户登录。
     *
     * @param command 登录命令
     * @return Token 响应
     */
    public TokenResponse login(LoginCommand command) {
        log.info("用户登录: account={}", command.getAccount());

        User user = authDomainService.authenticate(command.getAccount(), command.getPassword());

        StpUtil.login(user.getId());
        String token = StpUtil.getTokenValue();

        log.info("用户登录成功: userId={}, username={}", user.getId(), user.getUsername());
        return new TokenResponse(
                token,
                AuthConstants.DEFAULT_TOKEN_EXPIRES_SECONDS,
                new TokenResponse.UserInfo(
                        user.getId(), user.getUsername(),
                        user.getEmail(), user.getRole(), user.getAvatarUrl()
                )
        );
    }

    /**
     * 用户登出。
     */
    public void logout() {
        Long userId = StpUtil.getLoginIdAsLong();
        StpUtil.logout();
        log.info("用户登出成功: userId={}", userId);
    }

    /**
     * 获取当前用户信息。
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    public TokenResponse.UserInfo getCurrentUserInfo(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(GlobalErrorCodeConstants.RESOURCE_NOT_FOUND));
        return new TokenResponse.UserInfo(
                user.getId(), user.getUsername(),
                user.getEmail(), user.getRole(), user.getAvatarUrl()
        );
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
                .orElseThrow(() -> new BusinessException(GlobalErrorCodeConstants.RESOURCE_NOT_FOUND));
        }
        if (query.getUsername() != null) {
            return userRepository.findByUsername(query.getUsername())
                .orElseThrow(() -> new BusinessException(GlobalErrorCodeConstants.RESOURCE_NOT_FOUND));
        }
        if (query.getEmail() != null) {
            return userRepository.findByEmail(query.getEmail())
                .orElseThrow(() -> new BusinessException(GlobalErrorCodeConstants.RESOURCE_NOT_FOUND));
        }
        throw new BusinessException(GlobalErrorCodeConstants.PARAM_ERROR);
    }

    /**
     * 根据邮箱查询用户（供 OAuth2.0 密码模式使用）。
     *
     * @param email 邮箱
     * @return 用户实体
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    /**
     * 校验密码（供 OAuth2.0 密码模式使用）。
     *
     * @param rawPassword     明文密码
     * @param encodedPassword 加密密码
     * @return 是否匹配
     */
    public boolean checkPassword(String rawPassword, String encodedPassword) {
        return authDomainService.authenticate("", rawPassword) != null;
    }
}
