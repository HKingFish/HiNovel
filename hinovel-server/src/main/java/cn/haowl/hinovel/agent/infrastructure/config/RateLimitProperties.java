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
     * 每用户每分钟最大 LLM 请求次数，默认 60 次
     */
    private int llmPerMinute = 30;
}
