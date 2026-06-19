package cn.haowl.hinovel.user.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 认证模块基础配置。
 *
 * <p>提供密码编码器 Bean，供 AuthService 注入使用。</p>
 *
 * @author haowl
 * @since 2024
 */
@Configuration
public class AuthConfig {

    /**
     * 创建 BCrypt 密码编码器 Bean。
     *
     * @return BCryptPasswordEncoder 实例
     */
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
