package cn.haowl.hinovel.user.infrastructure.config;

import cn.dev33.satoken.oauth2.config.SaOAuth2ServerConfig;
import cn.dev33.satoken.stp.StpUtil;
import cn.haowl.hinovel.user.application.service.AuthApplicationService;
import cn.haowl.hinovel.user.domain.entity.User;
import cn.haowl.hinovel.user.domain.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Sa-Token OAuth2.0 授权服务器配置。
 *
 * <p>支持授权码模式（authorization_code）和密码模式（password）。
 * 注意：不重新定义 SaOAuth2ServerConfig Bean，避免与 Sa-Token 自动配置冲突。</p>
 *
 * @Author : haowl
 * @Date : 2026/3/12 22:30
 */
@Configuration
@RequiredArgsConstructor
public class SaOAuth2Config {

    private final AuthApplicationService authApplicationService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SaOAuth2ServerConfig saOAuth2ServerConfig;

    @PostConstruct
    public void configureOAuth2() {
        saOAuth2ServerConfig.doLoginHandle = (name, pwd) -> {
            User user = userRepository.findByEmail(name).orElse(null);
            if (user == null) {
                return null;
            }
            if (!user.verifyPassword(pwd, passwordEncoder)) {
                return null;
            }
            StpUtil.login(user.getId());
            return StpUtil.getTokenValue();
        };
    }
}
