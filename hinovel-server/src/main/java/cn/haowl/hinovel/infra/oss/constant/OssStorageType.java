package cn.haowl.hinovel.infra.oss.constant;

/**
 * OSS 存储类型枚举。
 *
 * <p>定义支持的 OSS 存储类型。</p>
 *
 * @author haowl
 * @since 2024
 */
public enum OssStorageType {

    /**
     * 本地存储。
     */
    LOCAL,

    /**
     * MinIO 存储。
     */
    MINIO,

    /**
     * 阿里云 OSS 存储。
     */
    ALIYUN,

    /**
     * 七牛云存储。
     */
    QINIU
}
