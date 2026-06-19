package cn.haowl.hinovel.infra.oss.provider;

import cn.haowl.hinovel.common.exception.BusinessException;
import cn.haowl.hinovel.common.response.ErrorCode;
import cn.haowl.hinovel.infra.oss.config.OssProperties;
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

/**
 * 本地文件存储实现。
 *
 * <p>将文件存储至配置的本地目录，通过静态资源接口提供访问。</p>
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
        String originalFilename = file.getOriginalFilename();
        String extension = extractExtension(originalFilename);
        // 使用 UUID 避免文件名冲突
        String objectKey = bizType + "/" + UUID.randomUUID() + "." + extension;
        Path targetPath = Paths.get(ossProperties.getLocal().getBasePath(), objectKey);

        // 转为绝对路径，避免 Windows 下 /data/... 被解析为相对路径
        Path absolutePath = targetPath.toAbsolutePath();
        try {
            Files.createDirectories(absolutePath.getParent());
            // 使用 Files.copy 替代 transferTo，避免 Tomcat 工作目录干扰
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, absolutePath, StandardCopyOption.REPLACE_EXISTING);
            }
            log.info("本地文件上传成功，路径：{}", absolutePath);
        } catch (IOException e) {
            log.error("本地文件上传失败，路径：{}，异常：{}", absolutePath, e.getMessage());
            throw new BusinessException(ErrorCode.OSS_UPLOAD_FAILED);
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
            boolean deleted = Files.deleteIfExists(targetPath);
            if (deleted) {
                log.info("本地文件删除成功，路径：{}", targetPath);
            } else {
                log.warn("本地文件不存在，跳过删除，路径：{}", targetPath);
            }
        } catch (IOException e) {
            log.error("本地文件删除失败，路径：{}，异常：{}", targetPath, e.getMessage());
            throw new BusinessException(ErrorCode.OSS_DELETE_FAILED);
        }
    }

    @Override
    public String getAccessUrl(String objectKey) {
        return ossProperties.getLocal().getAccessUrlPrefix() + "/" + objectKey;
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
