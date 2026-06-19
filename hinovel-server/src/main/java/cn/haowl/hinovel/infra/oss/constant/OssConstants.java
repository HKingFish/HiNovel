package cn.haowl.hinovel.infra.oss.constant;

/**
 * OSS 模块常量类。
 *
 * <p>定义 OSS 相关的状态、存储类型、文件类型、路径前缀等常量。</p>
 *
 * @author haowl
 * @since 2024
 */
public final class OssConstants {

    private OssConstants() {
    }

    /**
     * 脱敏显示长度。
     */
    public static final int MASK_SHOW_LENGTH = 4;

    /**
     * 脱敏占位符。
     */
    public static final String MASK_PLACEHOLDER = "****";

    /**
     * 脱敏字符。
     */
    public static final String MASK_CHAR = "*";

    /**
     * 默认业务类型。
     */
    public static final String DEFAULT_BIZ_TYPE = "common";

    /**
     * 状态：正常。
     */
    public static final String STATUS_ACTIVE = "ACTIVE";

    /**
     * 状态：未激活。
     */
    public static final String STATUS_INACTIVE = "INACTIVE";

    /**
     * 状态：已删除。
     */
    public static final String STATUS_DELETED = "DELETED";

    /**
     * 存储类型：本地存储。
     */
    public static final String STORAGE_TYPE_LOCAL = "LOCAL";

    /**
     * 存储类型：MinIO。
     */
    public static final String STORAGE_TYPE_MINIO = "MINIO";

    /**
     * 存储类型：阿里云 OSS。
     */
    public static final String STORAGE_TYPE_ALIYUN = "ALIYUN";

    /**
     * 存储类型：七牛云。
     */
    public static final String STORAGE_TYPE_QINIU = "QINIU";

    /**
     * 文件类型：图片。
     */
    public static final String FILE_TYPE_IMAGE = "IMAGE";

    /**
     * 文件类型：文档。
     */
    public static final String FILE_TYPE_DOCUMENT = "DOCUMENT";

    /**
     * 文件类型：视频。
     */
    public static final String FILE_TYPE_VIDEO = "VIDEO";

    /**
     * 文件类型：音频。
     */
    public static final String FILE_TYPE_AUDIO = "AUDIO";

    /**
     * 文件类型：其他。
     */
    public static final String FILE_TYPE_OTHER = "OTHER";

    /**
     * 路径前缀：知识库。
     */
    public static final String PATH_PREFIX_KNOWLEDGE = "knowledge";

    /**
     * 路径前缀：头像。
     */
    public static final String PATH_PREFIX_AVATAR = "avatar";

    /**
     * 路径前缀：临时文件。
     */
    public static final String PATH_PREFIX_TEMP = "temp";

    /**
     * 文件大小限制：1MB。
     */
    public static final long MAX_FILE_SIZE_1MB = 1024 * 1024L;

    /**
     * 文件大小限制：5MB。
     */
    public static final long MAX_FILE_SIZE_5MB = 5 * MAX_FILE_SIZE_1MB;

    /**
     * 文件大小限制：10MB。
     */
    public static final long MAX_FILE_SIZE_10MB = 10 * MAX_FILE_SIZE_1MB;

    /**
     * 文件大小限制：50MB。
     */
    public static final long MAX_FILE_SIZE_50MB = 50 * MAX_FILE_SIZE_1MB;

    /**
     * 文件大小限制：100MB。
     */
    public static final long MAX_FILE_SIZE_100MB = 100 * MAX_FILE_SIZE_1MB;

    /**
     * 图片最大宽度。
     */
    public static final int MAX_IMAGE_WIDTH = 4096;

    /**
     * 图片最大高度。
     */
    public static final int MAX_IMAGE_HEIGHT = 4096;

    /**
     * URL 有效期：1小时。
     */
    public static final int URL_EXPIRY_1_HOUR = 3600;

    /**
     * URL 有效期：1天。
     */
    public static final int URL_EXPIRY_1_DAY = 86400;

    /**
     * URL 有效期：7天。
     */
    public static final int URL_EXPIRY_7_DAYS = 7 * URL_EXPIRY_1_DAY;
}
