package cn.haowl.hinovel.infra.oss.service;

import cn.haowl.hinovel.infra.oss.config.OssProperties;
import cn.haowl.hinovel.infra.oss.constant.OssConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * OSS 配置服务。
 *
 * <p>提供 OSS 配置查询与更新功能。</p>
 *
 * @author haowl
 * @since 2024
 */
@Service
@RequiredArgsConstructor
public class OssConfigService {

    private final OssProperties ossProperties;

    /**
     * 获取 OSS 配置信息。
     *
     * @return 配置信息 Map
     */
    public Map<String, Object> getOssConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("type", ossProperties.getType());
        config.put("maxFileSize", ossProperties.getMaxFileSize());
        config.put("allowedTypes", ossProperties.getAllowedTypes());
        config.put("localBasePath", ossProperties.getLocal().getBasePath());
        config.put("localAccessUrlPrefix", ossProperties.getLocal().getAccessUrlPrefix());
        config.put("aliyunEndpoint", ossProperties.getAliyun().getEndpoint());
        config.put("aliyunBucketName", ossProperties.getAliyun().getBucketName());
        config.put("aliyunAccessKeyId", maskSecret(ossProperties.getAliyun().getAccessKeyId()));
        return config;
    }

    /**
     * 更新 OSS 配置。
     *
     * @param type                  存储类型
     * @param maxFileSize           文件大小上限
     * @param allowedTypes          允许的文件类型
     * @param localBasePath         本地存储根目录
     * @param localAccessUrlPrefix  本地访问 URL 前缀
     * @param aliyunEndpoint        阿里云 OSS Endpoint
     * @param aliyunAccessKeyId     阿里云 AccessKey ID
     * @param aliyunAccessKeySecret 阿里云 AccessKey Secret
     * @param aliyunBucketName      阿里云 Bucket 名称
     */
    public void updateOssConfig(String type, Long maxFileSize, String allowedTypes,
                                String localBasePath, String localAccessUrlPrefix,
                                String aliyunEndpoint, String aliyunAccessKeyId,
                                String aliyunAccessKeySecret, String aliyunBucketName) {
        if (type != null) {
            ossProperties.setType(type);
        }
        if (maxFileSize != null) {
            ossProperties.setMaxFileSize(maxFileSize);
        }
        if (allowedTypes != null) {
            ossProperties.setAllowedTypes(allowedTypes);
        }
        if (localBasePath != null) {
            ossProperties.getLocal().setBasePath(localBasePath);
        }
        if (localAccessUrlPrefix != null) {
            ossProperties.getLocal().setAccessUrlPrefix(localAccessUrlPrefix);
        }
        if (aliyunEndpoint != null) {
            ossProperties.getAliyun().setEndpoint(aliyunEndpoint);
        }
        if (aliyunAccessKeyId != null) {
            ossProperties.getAliyun().setAccessKeyId(aliyunAccessKeyId);
        }
        if (aliyunAccessKeySecret != null) {
            ossProperties.getAliyun().setAccessKeySecret(aliyunAccessKeySecret);
        }
        if (aliyunBucketName != null) {
            ossProperties.getAliyun().setBucketName(aliyunBucketName);
        }
    }

    /**
     * 对敏感字符串脱敏。
     *
     * <p>仅保留前 4 位，其余替换为 *。</p>
     *
     * @param secret 原始字符串
     * @return 脱敏后的字符串
     */
    private String maskSecret(String secret) {
        if (secret == null || secret.length() <= OssConstants.MASK_SHOW_LENGTH) {
            return OssConstants.MASK_PLACEHOLDER;
        }
        return secret.substring(0, OssConstants.MASK_SHOW_LENGTH)
                + OssConstants.MASK_CHAR.repeat(secret.length() - OssConstants.MASK_SHOW_LENGTH);
    }
}
