package cn.haowl.hinovel.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 安全配置属性。
 *
 * <p>管理 API 密钥加密等安全相关的配置项。</p>
 *
 * @author haowl
 */
@Data
@ConfigurationProperties(prefix = "hinovel.security")
public class SecurityProperties {

    /**
     * API 密钥加密密钥（AES-128，至少 16 个字符）。
     */
    private String apiKeySecret = "hinovel-default!";
}
