package cn.haowl.hinovel.common.util;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * API 密钥加解密工具类。
 *
 * <p>使用 AES-GCM 对称加密算法对 API 密钥进行加密存储和解密读取，
 * 防止数据库泄露导致密钥明文暴露。</p>
 *
 * <p>加密后的密文格式为 Base64 编码的 [IV(12字节) + 密文 + Tag(16字节)]。</p>
 *
 * @author haowl
 */
@Slf4j
public final class ApiKeyEncryptor {

    private ApiKeyEncryptor() {
    }

    /**
     * AES-GCM 算法标识。
     */
    private static final String ALGORITHM = "AES/GCM/NoPadding";

    /**
     * AES 密钥算法。
     */
    private static final String KEY_ALGORITHM = "AES";

    /**
     * GCM 初始化向量长度（字节）。
     */
    private static final int GCM_IV_LENGTH = 12;

    /**
     * GCM 认证标签长度（位）。
     */
    private static final int GCM_TAG_LENGTH = 128;

    /**
     * AES 密钥长度要求（字节），对应 AES-128。
     */
    private static final int AES_KEY_LENGTH = 16;

    /**
     * 加密 API 密钥。
     *
     * @param plainText 明文 API 密钥
     * @param secretKey 加密密钥（至少 16 个字符）
     * @return Base64 编码的密文，加密失败返回 null
     */
    public static String encrypt(String plainText, String secretKey) {
        if (plainText == null || plainText.isBlank()) {
            return plainText;
        }
        try {
            byte[] keyBytes = normalizeKey(secretKey);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, KEY_ALGORITHM);

            // 生成随机 IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);

            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // 拼接 IV + 密文
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            log.error("API 密钥加密失败，异常信息：{}", e.getMessage());
            return null;
        }
    }

    /**
     * 解密 API 密钥。
     *
     * @param cipherText Base64 编码的密文
     * @param secretKey  加密密钥（与加密时一致）
     * @return 明文 API 密钥，解密失败返回 null
     */
    public static String decrypt(String cipherText, String secretKey) {
        if (cipherText == null || cipherText.isBlank()) {
            return cipherText;
        }
        try {
            byte[] keyBytes = normalizeKey(secretKey);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, KEY_ALGORITHM);

            byte[] combined = Base64.getDecoder().decode(cipherText);

            // 提取 IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH);

            // 提取密文
            byte[] encrypted = new byte[combined.length - GCM_IV_LENGTH];
            System.arraycopy(combined, GCM_IV_LENGTH, encrypted, 0, encrypted.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);

            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("API 密钥解密失败，异常信息：{}", e.getMessage());
            return null;
        }
    }

    /**
     * 对 API 密钥进行脱敏处理（仅保留前 4 位和后 4 位）。
     *
     * @param apiKey 原始或加密后的 API 密钥
     * @return 脱敏后的字符串，如 sk-0c****8e6f
     */
    public static String mask(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            return "";
        }
        int visibleLength = 4;
        if (apiKey.length() <= visibleLength * 2) {
            return "****";
        }
        return apiKey.substring(0, visibleLength) + "****"
                + apiKey.substring(apiKey.length() - visibleLength);
    }

    /**
     * 将密钥字符串标准化为 AES-128 所需的 16 字节。
     *
     * <p>如果密钥不足 16 字节则右侧补零，超过则截断。</p>
     *
     * @param secretKey 原始密钥字符串
     * @return 标准化后的 16 字节密钥
     */
    private static byte[] normalizeKey(String secretKey) {
        byte[] keyBytes = new byte[AES_KEY_LENGTH];
        byte[] rawBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(rawBytes, 0, keyBytes, 0,
                Math.min(rawBytes.length, AES_KEY_LENGTH));
        return keyBytes;
    }
}
