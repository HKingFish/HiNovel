package cn.haowl.hinovel.agent.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 频率限制配置属性
 * 对应 application.yml 中 hinovel.rate-limit.* 配置项
 */
@Data
@ConfigurationProperties(prefix = "hinovel.rate-limit")
public class RateLimitProperties {

    /**
     * 未配置时每用户每分钟允许的 LLM 请求数。
     */
    private static final int DEFAULT_LLM_REQUESTS_PER_MINUTE = 30;

    /**
     * 每用户每分钟最大 LLM 请求次数。
     */
    private int llmPerMinute = DEFAULT_LLM_REQUESTS_PER_MINUTE;
}
