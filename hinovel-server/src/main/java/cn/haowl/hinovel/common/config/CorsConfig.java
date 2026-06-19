package cn.haowl.hinovel.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * 全局跨域配置
 * 通过 CorsFilter 在 Sa-Token 过滤器之前处理预检请求，避免 OPTIONS 请求被鉴权拦截
 */
@Configuration
public class CorsConfig {

    /**
     * 注册全局 CorsFilter，优先级高于 Sa-Token 过滤器
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        // 使用 allowedOriginPatterns 允许所有来源，与 allowCredentials=true 兼容
        corsConfig.addAllowedOriginPattern("*");
        corsConfig.addAllowedHeader("*");
        corsConfig.addAllowedMethod("*");
        corsConfig.setAllowCredentials(true);
        // 预检请求缓存 1 小时，减少 OPTIONS 请求次数
        corsConfig.setMaxAge(3600L);
        // 暴露 Sa-Token 响应头，供前端读取
        corsConfig.setExposedHeaders(List.of("satoken", "Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsFilter(source);
    }
}
