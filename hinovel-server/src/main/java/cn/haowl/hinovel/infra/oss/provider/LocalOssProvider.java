package cn.haowl.hinovel.infra.oss.provider;

import cn.haowl.hinovel.infra.oss.config.OssProperties;
import cn.haowl.hinovel.infra.oss.constant.OssConstants;
import cn.haowl.hinovel.infra.oss.util.OssFileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import static cn.haowl.hinovel.common.exception.util.ServiceExceptionUtil.exception;
import static cn.haowl.hinovel.infra.enums.InfraErrorCodeConstants.OSS_DELETE_FAILED;
import static cn.haowl.hinovel.infra.enums.InfraErrorCodeConstants.OSS_UPLOAD_FAILED;

/**
 * 本地文件存储实现。
 *
 * 将文件存储至配置的本地目录，通过静态资源接口提供访问。
 *
 * @author haowl
 * @since 2024
 */
@Slf4j
@RequiredArgsConstructor
public class LocalOssProvider implements OssProvider {

    /**
     * OSS 配置属性。
     */
    private final OssProperties ossProperties;

    @Override
    public String upload(MultipartFile file, String bizType) {
        String extension = OssFileUtils.getExtension(file.getOriginalFilename(), OssConstants.DEFAULT_FILE_EXTENSION);
        // 使用 UUID 避免文件名冲突
        String objectKey = bizType + "/" + UUID.randomUUID() + "." + extension;
        // 转为绝对路径，避免 Windows 下 /data/... 被解析为相对路径
        Path absolutePath = Paths.get(ossProperties.getLocal().getBasePath(), objectKey).toAbsolutePath();
        try {
            Files.createDirectories(absolutePath.getParent());
            // 使用 Files.copy 替代 transferTo，避免 Tomcat 工作目录干扰
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, absolutePath, StandardCopyOption.REPLACE_EXISTING);
            }
            log.info("[upload][本地文件上传成功，bytes={}]", file.getSize());
        } catch (IOException e) {
            log.error("[upload][本地文件上传失败，type={}]", e.getClass().getSimpleName());
            throw exception(OSS_UPLOAD_FAILED);
        }

        return getAccessUrl(objectKey);
    }

    @Override
    public void delete(String fileUrl) {
        String prefix = ossProperties.getLocal().getAccessUrlPrefix();
        // 从访问 URL 中提取相对路径
        String objectKey = fileUrl.replace(prefix + "/", "");
        Path targetPath = Paths.get(ossProperties.getLocal().getBasePath(), objectKey).toAbsolutePath();

        try {
            if (Files.deleteIfExists(targetPath)) {
                log.info("[delete][本地文件删除成功]");
            } else {
                log.warn("[delete][本地文件不存在，跳过删除]");
            }
        } catch (IOException e) {
            log.error("[delete][本地文件删除失败，type={}]", e.getClass().getSimpleName());
            throw exception(OSS_DELETE_FAILED);
        }
    }

    @Override
    public String getAccessUrl(String objectKey) {
        return ossProperties.getLocal().getAccessUrlPrefix() + "/" + objectKey;
    }
}
