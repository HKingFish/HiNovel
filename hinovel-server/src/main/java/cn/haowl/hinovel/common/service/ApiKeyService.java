package cn.haowl.hinovel.common.service;

import cn.haowl.hinovel.common.config.SecurityProperties;
import cn.haowl.hinovel.common.util.ApiKeyEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

/**
 * API 密钥加解密服务。
 *
 * <p>封装 API 密钥的加密、解密和脱敏操作，
 * 统一管理加密密钥的获取方式。</p>
 *
 * @author haowl
 */
@Service
@EnableConfigurationProperties(SecurityProperties.class)
public class ApiKeyService {

    /**
     * 脱敏值中的掩码标记，前端回传该标记表示未修改密钥。
     */
    private static final String MASK_MARKER = "****";

    /**
     * 安全配置。
     */
    @Autowired
    private SecurityProperties securityProperties;

    /**
     * 加密 API 密钥。
     *
     * @param plainApiKey 明文 API 密钥
     * @return 加密后的密文
     */
    public String encrypt(String plainApiKey) {
        if (plainApiKey == null || plainApiKey.isBlank()) {
            return plainApiKey;
        }
        return ApiKeyEncryptor.encrypt(plainApiKey, securityProperties.getApiKeySecret());
    }

    /**
     * 解密 API 密钥。
     *
     * @param encryptedApiKey 加密后的密文
     * @return 明文 API 密钥
     */
    public String decrypt(String encryptedApiKey) {
        if (encryptedApiKey == null || encryptedApiKey.isBlank()) {
            return encryptedApiKey;
        }
        return ApiKeyEncryptor.decrypt(encryptedApiKey, securityProperties.getApiKeySecret());
    }

    /**
     * 对 API 密钥进行脱敏处理。
     *
     * <p>先解密再脱敏，返回类似 sk-0c****8e6f 的格式。</p>
     *
     * @param encryptedApiKey 加密后的密文
     * @return 脱敏后的字符串
     */
    public String decryptAndMask(String encryptedApiKey) {
        String plainKey = decrypt(encryptedApiKey);
        return ApiKeyEncryptor.mask(plainKey);
    }

    /**
     * 判断前端传入的 apiKey 是否为新的明文密钥。
     *
     * @param apiKey 前端传入的 apiKey
     * @return 为空或包含脱敏标记时视为未修改，返回 false
     */
    public boolean isNewApiKey(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            return false;
        }
        return !apiKey.contains(MASK_MARKER);
    }
}
