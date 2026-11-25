package cn.haowl.hinovel.infra.oss.service;

import cn.haowl.hinovel.infra.oss.config.OssProperties;
import cn.haowl.hinovel.infra.oss.constant.OssConstants;
import cn.haowl.hinovel.infra.oss.constant.OssStorageTypeEnum;
import lombok.Builder;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * OSS 配置服务。
 *
 * 提供 OSS 配置查询与更新功能。
 *
 * @author haowl
 * @since 2024
 */
@Service
public class OssConfigService {

    /**
     * OSS 配置属性。
     */
    @Autowired
    private OssProperties ossProperties;

    /**
     * 获取 OSS 配置快照。
     */
    public OssConfigView getOssConfig() {
        OssProperties.Local local = ossProperties.getLocal();
        OssProperties.Aliyun aliyun = ossProperties.getAliyun();
        return OssConfigView.builder()
            .type(ossProperties.getType())
            .maxFileSize(ossProperties.getMaxFileSize())
            .allowedTypes(ossProperties.getAllowedTypes())
            .localBasePath(local.getBasePath())
            .localAccessUrlPrefix(local.getAccessUrlPrefix())
            .aliyunEndpoint(aliyun.getEndpoint())
            .aliyunBucketName(aliyun.getBucketName())
            .aliyunAccessKeyId(maskSecret(aliyun.getAccessKeyId()))
            .build();
    }

    /**
     * 更新 OSS 配置。
     *
     * @param update 仅覆盖已提供的配置项
     */
    public void updateOssConfig(OssConfigUpdate update) {
        if (update.getType() != null) {
            ossProperties.setType(update.getType());
        }
        if (update.getMaxFileSize() != null) {
            ossProperties.setMaxFileSize(update.getMaxFileSize());
        }
        if (update.getAllowedTypes() != null) {
            ossProperties.setAllowedTypes(update.getAllowedTypes());
        }
        if (update.getLocalBasePath() != null) {
            ossProperties.getLocal().setBasePath(update.getLocalBasePath());
        }
        if (update.getLocalAccessUrlPrefix() != null) {
            ossProperties.getLocal().setAccessUrlPrefix(update.getLocalAccessUrlPrefix());
        }
        if (update.getAliyunEndpoint() != null) {
            ossProperties.getAliyun().setEndpoint(update.getAliyunEndpoint());
        }
        if (update.getAliyunBucketName() != null) {
            ossProperties.getAliyun().setBucketName(update.getAliyunBucketName());
        }
        if (update.getAliyunAccessKeyId() != null) {
            ossProperties.getAliyun().setAccessKeyId(update.getAliyunAccessKeyId());
        }
        if (update.getAliyunAccessKeySecret() != null) {
            ossProperties.getAliyun().setAccessKeySecret(update.getAliyunAccessKeySecret());
        }
    }

    /**
     * 对敏感字符串脱敏。
     *
     * 仅保留前 4 位，其余替换为 *。
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

    /**
     * 脱敏后的 OSS 配置快照。
     */
    @Getter
    @Builder
    public static class OssConfigView {

        /**
         * 存储类型。
         *
         * @see OssStorageTypeEnum
         */
        private final String type;

        /**
         * 文件大小上限（字节）。
         */
        private final long maxFileSize;

        /**
         * 允许上传的文件扩展名。
         */
        private final String allowedTypes;

        /**
         * 本地存储根目录。
         */
        private final String localBasePath;

        /**
         * 本地访问 URL 前缀。
         */
        private final String localAccessUrlPrefix;

        /**
         * 阿里云 OSS Endpoint。
         */
        private final String aliyunEndpoint;

        /**
         * 阿里云存储桶名称。
         */
        private final String aliyunBucketName;

        /**
         * 脱敏后的阿里云 AccessKey ID。
         */
        private final String aliyunAccessKeyId;
    }

    /**
     * OSS 配置的增量修改项。
     */
    @Getter
    @Builder
    public static class OssConfigUpdate {

        /**
         * 存储类型。
         *
         * @see OssStorageTypeEnum
         */
        private final String type;

        /**
         * 文件大小上限（字节）。
         */
        private final Long maxFileSize;

        /**
         * 允许上传的文件扩展名。
         */
        private final String allowedTypes;

        /**
         * 本地存储根目录。
         */
        private final String localBasePath;

        /**
         * 本地访问 URL 前缀。
         */
        private final String localAccessUrlPrefix;

        /**
         * 阿里云 OSS Endpoint。
         */
        private final String aliyunEndpoint;

        /**
         * 阿里云存储桶名称。
         */
        private final String aliyunBucketName;

        /**
         * 阿里云 AccessKey ID。
         */
        private final String aliyunAccessKeyId;

        /**
         * 阿里云 AccessKey Secret。
         */
        private final String aliyunAccessKeySecret;
    }
}
