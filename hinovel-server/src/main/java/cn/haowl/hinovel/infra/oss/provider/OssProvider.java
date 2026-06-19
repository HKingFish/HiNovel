package cn.haowl.hinovel.infra.oss.provider;

import org.springframework.web.multipart.MultipartFile;

/**
 * OSS 统一存储接口。
 *
 * <p>支持本地存储（LOCAL）和阿里云 OSS（ALIYUN），通过配置切换。</p>
 *
 * @author haowl
 * @since 2024
 */
public interface OssProvider {

    /**
     * 上传文件
     *
     * @param file    上传的文件（MultipartFile）
     * @param bizType 业务类型（如 avatar、knowledge），用于分目录存储
     * @return 文件访问 URL
     */
    String upload(MultipartFile file, String bizType);

    /**
     * 删除文件
     *
     * @param fileUrl 文件访问 URL
     */
    void delete(String fileUrl);

    /**
     * 获取文件访问 URL（用于已存储文件的 URL 生成）
     *
     * @param objectKey 文件对象 Key（相对路径）
     * @return 文件访问 URL
     */
    String getAccessUrl(String objectKey);
}
