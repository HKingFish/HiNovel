package cn.haowl.hinovel.user.infrastructure.config;

import cn.dev33.satoken.oauth2.config.SaOAuth2ServerConfig;
import cn.haowl.hinovel.user.application.service.AuthApplicationService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

/**
 * Sa-Token OAuth2.0 授权服务器配置。
 *
 * 支持授权码模式（authorization_code）和密码模式（password）。
 * 注意：不重新定义 SaOAuth2ServerConfig Bean，避免与 Sa-Token 自动配置冲突。
 *
 * @Author : haowl
 * @Date : 2026/3/12 22:30
 */
@Configuration
public class SaOAuth2Config {

    /**
     * 认证应用服务。
     */
    @Autowired
    private AuthApplicationService authApplicationService;

    /**
     * Sa-Token OAuth2 服务端配置。
     */
    @Autowired
    private SaOAuth2ServerConfig saOAuth2ServerConfig;

    /**
     * 注册 OAuth2 密码授权回调。
     */
    @PostConstruct
    public void configureOAuth2() {
        saOAuth2ServerConfig.doLoginHandle = authApplicationService::authorizeWithPassword;
    }
}
