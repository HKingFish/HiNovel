package cn.haowl.hinovel.infra.oss.provider;

import cn.haowl.hinovel.common.exception.BusinessException;
import cn.haowl.hinovel.common.response.ErrorCode;
import cn.haowl.hinovel.infra.oss.config.OssProperties;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * 阿里云 OSS 存储实现。
 *
 * <p>集成阿里云 OSS SDK，支持配置 Bucket/AccessKey/SecretKey/Endpoint。</p>
 *
 * @author haowl
 * @since 2024
 */
@Slf4j
public class AliyunOssProvider implements OssProvider {

    /**
     * OSS 配置属性。
     */
    private final OssProperties ossProperties;

    /**
     * 阿里云 OSS 客户端。
     */
    private final OSS ossClient;

    /**
     * 构造函数，初始化阿里云 OSS 客户端。
     *
     * @param ossProperties OSS 配置属性
     */
    public AliyunOssProvider(OssProperties ossProperties) {
        this.ossProperties = ossProperties;
        OssProperties.Aliyun aliyun = ossProperties.getAliyun();
        this.ossClient = new OSSClientBuilder().build(
                aliyun.getEndpoint(),
                aliyun.getAccessKeyId(),
                aliyun.getAccessKeySecret()
        );
    }

    @Override
    public String upload(MultipartFile file, String bizType) {
        String originalFilename = file.getOriginalFilename();
        String extension = extractExtension(originalFilename);
        String objectKey = bizType + "/" + UUID.randomUUID() + "." + extension;
        String bucketName = ossProperties.getAliyun().getBucketName();

        try {
            ossClient.putObject(bucketName, objectKey, file.getInputStream());
            log.info("阿里云 OSS 文件上传成功，Bucket：{}，Key：{}", bucketName, objectKey);
        } catch (OSSException e) {
            log.error("阿里云 OSS 上传失败，错误码：{}，异常：{}", e.getErrorCode(), e.getMessage());
            throw new BusinessException(ErrorCode.OSS_UPLOAD_FAILED);
        } catch (IOException e) {
            log.error("读取上传文件流失败，异常：{}", e.getMessage());
            throw new BusinessException(ErrorCode.OSS_UPLOAD_FAILED);
        }

        return getAccessUrl(objectKey);
    }

    @Override
    public void delete(String fileUrl) {
        String bucketName = ossProperties.getAliyun().getBucketName();
        // 从访问 URL 中提取 objectKey
        String objectKey = extractObjectKey(fileUrl);

        try {
            ossClient.deleteObject(bucketName, objectKey);
            log.info("阿里云 OSS 文件删除成功，Bucket：{}，Key：{}", bucketName, objectKey);
        } catch (OSSException e) {
            log.error("阿里云 OSS 删除失败，错误码：{}，异常：{}", e.getErrorCode(), e.getMessage());
            throw new BusinessException(ErrorCode.OSS_DELETE_FAILED);
        }
    }

    @Override
    public String getAccessUrl(String objectKey) {
        OssProperties.Aliyun aliyun = ossProperties.getAliyun();
        return "https://" + aliyun.getBucketName() + "." + aliyun.getEndpoint() + "/" + objectKey;
    }

    /**
     * 从完整访问 URL 中提取 objectKey
     */
    private String extractObjectKey(String fileUrl) {
        OssProperties.Aliyun aliyun = ossProperties.getAliyun();
        String prefix = "https://" + aliyun.getBucketName() + "." + aliyun.getEndpoint() + "/";
        return fileUrl.replace(prefix, "");
    }

    /**
     * 从文件名中提取后缀名
     */
    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "bin";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
