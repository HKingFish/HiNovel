package cn.haowl.hinovel.agent.infrastructure.interceptor;

import cn.dev33.satoken.stp.StpUtil;
import cn.haowl.hinovel.agent.constant.AgentConstants;
import cn.haowl.hinovel.agent.infrastructure.config.RateLimitProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

import static cn.haowl.hinovel.ai.enums.AiErrorCodeConstants.LLM_RATE_LIMIT_EXCEEDED;
import static cn.haowl.hinovel.common.exception.util.ServiceExceptionUtil.exception;

/**
 * LLM API 频率限制拦截器
 * 每用户每分钟最多 N 次请求，超限返回 429
 */
@Slf4j
@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(RateLimitProperties.class)
public class RateLimitInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate redisTemplate;
    private final RateLimitProperties rateLimitProperties;

    private static final String RATE_LIMIT_KEY_PREFIX = "rate:limit:";

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        // 未登录请求直接放行（由 Sa-Token 拦截器统一处理鉴权）
        if (!StpUtil.isLogin()) {
            return true;
        }

        Long userId = StpUtil.getLoginIdAsLong();
        String redisKey = RATE_LIMIT_KEY_PREFIX + userId;

        Long requestCount = redisTemplate.opsForValue().increment(redisKey);
        if (requestCount != null && requestCount == AgentConstants.FIRST_REQUEST_COUNT) {
            // 首次请求时设置过期窗口
            redisTemplate.expire(redisKey, AgentConstants.RATE_LIMIT_WINDOW_MINUTES,
                    TimeUnit.MINUTES);
        }

        int maxRequests = rateLimitProperties.getLlmPerMinute();
        if (requestCount != null && requestCount > maxRequests) {
            log.warn("用户 {} 超过频率限制: {}/{} 次/分钟", userId, requestCount, maxRequests);
            throw exception(LLM_RATE_LIMIT_EXCEEDED);
        }

        return true;
    }
}
