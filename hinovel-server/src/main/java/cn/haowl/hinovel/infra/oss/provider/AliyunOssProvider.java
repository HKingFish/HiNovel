package cn.haowl.hinovel.infra.oss.provider;

import cn.haowl.hinovel.infra.oss.config.OssProperties;
import cn.haowl.hinovel.infra.oss.constant.OssConstants;
import cn.haowl.hinovel.infra.oss.util.OssFileUtils;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

import static cn.haowl.hinovel.common.exception.util.ServiceExceptionUtil.exception;
import static cn.haowl.hinovel.infra.enums.InfraErrorCodeConstants.OSS_DELETE_FAILED;
import static cn.haowl.hinovel.infra.enums.InfraErrorCodeConstants.OSS_UPLOAD_FAILED;

/**
 * 阿里云 OSS 存储实现。
 *
 * 集成阿里云 OSS SDK，支持配置 Bucket/AccessKey/SecretKey/Endpoint。
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
        String extension = OssFileUtils.getExtension(file.getOriginalFilename(), OssConstants.DEFAULT_FILE_EXTENSION);
        String objectKey = bizType + "/" + UUID.randomUUID() + "." + extension;
        String bucketName = ossProperties.getAliyun().getBucketName();

        try {
            ossClient.putObject(bucketName, objectKey, file.getInputStream());
            log.info("[upload][阿里云 OSS 文件上传成功，bucket={}，bytes={}]", bucketName, file.getSize());
        } catch (OSSException e) {
            log.error("[upload][阿里云 OSS 上传失败，code={}，requestId={}]", e.getErrorCode(), e.getRequestId());
            throw exception(OSS_UPLOAD_FAILED);
        } catch (IOException e) {
            log.error("[upload][读取上传文件流失败，type={}]", e.getClass().getSimpleName());
            throw exception(OSS_UPLOAD_FAILED);
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
            log.info("[delete][阿里云 OSS 文件删除成功，bucket={}]", bucketName);
        } catch (OSSException e) {
            log.error("[delete][阿里云 OSS 删除失败，code={}，requestId={}]", e.getErrorCode(), e.getRequestId());
            throw exception(OSS_DELETE_FAILED);
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
}
