package cn.haowl.hinovel.infra.oss.config;

import cn.haowl.hinovel.infra.oss.constant.OssConstants;
import cn.haowl.hinovel.infra.oss.constant.OssStorageType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.List;

/**
 * OSS 配置属性。
 *
 * <p>对应 application.yml 中 hinovel.oss.* 配置项。</p>
 *
 * @author haowl
 * @since 2024
 */
@Data
@ConfigurationProperties(prefix = "hinovel.oss")
public class OssProperties {

    /**
     * 存储类型。
     *
     * @see OssStorageType
     */
    private String type = OssStorageType.LOCAL.name();

    /**
     * 文件大小上限（字节）。
     */
    private long maxFileSize = OssConstants.MAX_FILE_SIZE_50MB;

    /**
     * 允许上传的文件类型（后缀名，逗号分隔）。
     */
    private String allowedTypes = "pdf,txt,md,png,jpg,jpeg";

    /**
     * 本地存储配置。
     */
    private Local local = new Local();

    /**
     * 阿里云 OSS 配置。
     */
    private Aliyun aliyun = new Aliyun();

    /**
     * 获取允许的文件类型列表。
     *
     * @return 文件类型列表
     */
    public List<String> getAllowedTypeList() {
        return Arrays.asList(allowedTypes.split(","));
    }

    /**
     * 本地存储配置。
     *
     * @author haowl
     * @since 2024
     */
    @Data
    public static class Local {
        /**
         * 本地存储根目录。
         */
        private String basePath = "/data/hinovel/uploads";
        /**
         * 访问 URL 前缀。
         */
        private String accessUrlPrefix = "http://localhost:8080/files";
    }

    /**
     * 阿里云 OSS 配置。
     *
     * @author haowl
     * @since 2024
     */
    @Data
    public static class Aliyun {
        /**
         * OSS Endpoint。
         */
        private String endpoint = "oss-cn-hangzhou.aliyuncs.com";
        /**
         * AccessKey ID。
         */
        private String accessKeyId = "";
        /**
         * AccessKey Secret。
         */
        private String accessKeySecret = "";
        /**
         * Bucket 名称。
         */
        private String bucketName = "hinovel-files";
    }
}
