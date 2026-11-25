package cn.haowl.hinovel.infra.oss.constant;

/**
 * OSS 存储类型枚举。
 *
 * 定义支持的 OSS 存储类型。
 *
 * @author haowl
 * @since 2024
 */
public enum OssStorageTypeEnum {

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
