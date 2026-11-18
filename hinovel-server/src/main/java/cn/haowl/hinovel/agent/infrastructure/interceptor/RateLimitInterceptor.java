package cn.haowl.hinovel.agent.infrastructure.interceptor;

import cn.dev33.satoken.stp.StpUtil;
import cn.haowl.hinovel.agent.infrastructure.config.RateLimitProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static cn.haowl.hinovel.agent.constant.AgentConstants.RATE_LIMIT_WINDOW_MINUTES;
import static cn.haowl.hinovel.agent.constant.AgentConstants.REDIS_KEY_PREFIX_RATE_LIMIT;
import static cn.haowl.hinovel.ai.enums.AiErrorCodeConstants.LLM_RATE_LIMIT_EXCEEDED;
import static cn.haowl.hinovel.common.exception.util.ServiceExceptionUtil.exception;

/**
 * LLM API 频率限制拦截器
 * 每用户每分钟最多 N 次请求，超限返回 429
 */
@Slf4j
@Component
@EnableConfigurationProperties(RateLimitProperties.class)
public class RateLimitInterceptor implements HandlerInterceptor {

    /**
     * 原子推进限流计数，并为首次或历史无过期键设置固定窗口。
     */
    private static final DefaultRedisScript<Long> INCREMENT_REQUEST_COUNT = new DefaultRedisScript<>("""
        local count = redis.call('INCR', KEYS[1])
        if count == 1 or redis.call('TTL', KEYS[1]) == -1 then
            redis.call('EXPIRE', KEYS[1], ARGV[1])
        end
        return count
        """, Long.class);

    /**
     * 频率计数存储。
     */
    @Autowired
    private StringRedisTemplate redisTemplate;
    /**
     * 频率限制配置。
     */
    @Autowired
    private RateLimitProperties rateLimitProperties;

    @Override
    public boolean preHandle(
        HttpServletRequest request,
        HttpServletResponse response,
        Object handler) {
        // 未登录请求直接放行（由 Sa-Token 拦截器统一处理鉴权）
        if (!StpUtil.isLogin()) {
            return true;
        }

        Long userId = StpUtil.getLoginIdAsLong();
        String redisKey = REDIS_KEY_PREFIX_RATE_LIMIT + userId;

        Long requestCount =
            redisTemplate.execute(
                INCREMENT_REQUEST_COUNT,
                List.of(redisKey),
                Long.toString(TimeUnit.MINUTES.toSeconds(RATE_LIMIT_WINDOW_MINUTES)));
        int maxRequests = rateLimitProperties.getLlmPerMinute();
        if (requestCount != null && requestCount > maxRequests) {
            log.warn("[preHandle][用户 {} 超过频率限制: {}/{} 次/分钟]", userId, requestCount, maxRequests);
            throw exception(LLM_RATE_LIMIT_EXCEEDED);
        }

        return true;
    }
}
