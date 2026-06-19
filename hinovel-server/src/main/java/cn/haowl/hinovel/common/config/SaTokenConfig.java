package cn.haowl.hinovel.common.config;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.filter.SaServletFilter;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import cn.haowl.hinovel.common.constant.CommonConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * Sa-Token 路由鉴权配置
 * 统一管理接口白名单，避免分散在各模块中难以维护
 */
@Configuration
public class SaTokenConfig {

    /**
     * 注册 Sa-Token 全局过滤器
     * 优先级设为 -90，低于 CorsFilter（Integer.MIN_VALUE），确保跨域头在鉴权前写入响应
     */
    @Bean
    @Order(-90)
    public SaServletFilter saServletFilter() {
        return new SaServletFilter()
                .addInclude("/**")
                .addExclude(
                        "/api/user/auth/login",
                        "/api/user/auth/register",
                        "/api/user/auth/refresh",
                        "/files/**",
                        "/actuator/**"
                )
                .setAuth(obj -> {
                    // OPTIONS 预检请求直接放行，避免干扰 CorsFilter 的跨域头写入
                    if ("OPTIONS".equals(SaHolder.getRequest().getMethod())) {
                        return;
                    }
                    SaRouter.match("/**").check(r -> StpUtil.checkLogin());
                })
                .setError(e -> {
                    String message = e instanceof NotLoginException ? "未登录或登录已过期" : e.getMessage();
                    return "{\"code\":" + CommonConstants.HTTP_STATUS_UNAUTHORIZED + ",\"message\":\"" + message + "\"}";
                });
    }
}
