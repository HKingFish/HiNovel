package cn.haowl.hinovel.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * HTTP 日志配置属性。
 *
 * <p>对应 application.yml 中 hinovel.http-log.* 配置项。</p>
 *
 * @author haowl
 * @since 2024
 */
@Data
@Component
@ConfigurationProperties(prefix = "hinovel.http-log")
public class HttpLogProperties {

    /**
     * 是否启用 HTTP 日志记录。
     */
    private boolean enabled = true;

    /**
     * 日志保留天数。
     */
    private int retentionDays = 7;

    /**
     * 是否记录请求体。
     */
    private boolean logRequestBody = true;

    /**
     * 是否记录响应体。
     */
    private boolean logResponseBody = true;

    /**
     * 最大请求体记录长度（字节）。
     */
    private int maxRequestBodyLength = 4096;

    /**
     * 最大响应体记录长度（字节）。
     */
    private int maxResponseBodyLength = 4096;

    /**
     * 排除的 URL 模式（Ant 风格）。
     */
    private String[] excludePatterns = new String[0];
}
