package cn.haowl.hinovel.infra.oss.domain.service;

import cn.haowl.hinovel.common.exception.BusinessException;
import cn.haowl.hinovel.common.response.ErrorCode;
import cn.haowl.hinovel.infra.oss.config.OssProperties;
import cn.haowl.hinovel.infra.oss.constant.OssConstants;
import cn.haowl.hinovel.infra.oss.domain.entity.OssUploadLog;
import cn.haowl.hinovel.infra.oss.domain.repository.OssUploadLogRepository;
import cn.haowl.hinovel.infra.oss.provider.OssProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * OSS 领域服务。
 *
 * <p>处理文件上传相关的业务逻辑。</p>
 *
 * @Author : haowl
 * @Date : 2026/3/12 22:30
 */
@Service
@RequiredArgsConstructor
public class OssDomainService {

    private final OssProvider ossProvider;
    private final OssUploadLogRepository ossUploadLogRepository;
    private final OssProperties ossProperties;

    /**
     * 上传文件。
     *
     * @param file    文件
     * @param bizType 业务类型
     * @param bizId   业务ID
     * @param userId  用户ID
     * @return 上传日志
     */
    public OssUploadLog upload(MultipartFile file, String bizType, Long bizId, Long userId) {
        validateFile(file);

        String fileUrl = ossProvider.upload(file, bizType);

        OssUploadLog log = OssUploadLog.create(
                userId,
                bizType,
                bizId,
                file.getOriginalFilename(),
                file.getSize(),
                file.getContentType(),
                fileUrl,
                ossProperties.getType()
        );

        return ossUploadLogRepository.save(log);
    }

    /**
     * 删除文件。
     *
     * @param fileUrl 文件URL
     */
    public void delete(String fileUrl) {
        ossProvider.delete(fileUrl);

        ossUploadLogRepository.updateStatusByFileUrl(fileUrl, OssConstants.STATUS_DELETED);
    }

    /**
     * 验证文件。
     *
     * @param file 文件
     * @throws BusinessException 文件不符合要求时抛出
     */
    private void validateFile(MultipartFile file) {
        if (file.getSize() > ossProperties.getMaxFileSize()) {
            throw new BusinessException(ErrorCode.OSS_FILE_TOO_LARGE);
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new BusinessException(ErrorCode.OSS_FILE_TYPE_NOT_ALLOWED);
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
        if (!ossProperties.getAllowedTypeList().contains(extension)) {
            throw new BusinessException(ErrorCode.OSS_FILE_TYPE_NOT_ALLOWED);
        }
    }
}
