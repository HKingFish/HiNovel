package cn.haowl.hinovel.infra.oss.domain.service;

import cn.haowl.hinovel.infra.oss.config.OssProperties;
import cn.haowl.hinovel.infra.oss.constant.OssUploadStatusEnum;
import cn.haowl.hinovel.infra.oss.domain.command.UploadCommand;
import cn.haowl.hinovel.infra.oss.domain.entity.OssUploadLog;
import cn.haowl.hinovel.infra.oss.domain.repository.OssUploadLogRepository;
import cn.haowl.hinovel.infra.oss.provider.OssProvider;
import cn.haowl.hinovel.infra.oss.util.OssFileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

import static cn.haowl.hinovel.common.exception.enums.GlobalErrorCodeConstants.PARAM_ERROR;
import static cn.haowl.hinovel.common.exception.util.ServiceExceptionUtil.exception;
import static cn.haowl.hinovel.infra.enums.InfraErrorCodeConstants.OSS_FILE_NOT_EXISTS;
import static cn.haowl.hinovel.infra.enums.InfraErrorCodeConstants.OSS_FILE_TYPE_NOT_ALLOWED;
import static cn.haowl.hinovel.infra.enums.InfraErrorCodeConstants.OSS_FILE_TOO_LARGE;
import static cn.haowl.hinovel.infra.oss.constant.OssConstants.BIZ_TYPE_PATTERN;

/**
 * OSS 领域服务。
 *
 * 处理文件上传相关的业务逻辑。
 *
 * @Author : haowl
 * @Date : 2026/3/12 22:30
 */
@Service
public class OssDomainService {

    /**
     * OSS 存储提供方。
     */
    @Autowired
    private OssProvider ossProvider;

    /**
     * 上传日志仓储。
     */
    @Autowired
    private OssUploadLogRepository ossUploadLogRepository;

    /**
     * OSS 配置属性。
     */
    @Autowired
    private OssProperties ossProperties;

    /**
     * 上传文件。
     *
     * @param command 上传命令
     * @return 上传日志
     */
    public OssUploadLog upload(UploadCommand command) {
        MultipartFile file = command.getFile();
        validateFile(file);
        if (command.getBizType() == null || !BIZ_TYPE_PATTERN.matcher(command.getBizType()).matches()) {
            throw exception(PARAM_ERROR);
        }

        String fileUrl = ossProvider.upload(file, command.getBizType());
        LocalDateTime now = LocalDateTime.now();
        return ossUploadLogRepository.save(OssUploadLog.builder()
            .userId(command.getUserId())
            .bizType(command.getBizType())
            .bizId(command.getBizId())
            .originalFilename(file.getOriginalFilename())
            .fileSize(file.getSize())
            .contentType(file.getContentType())
            .fileUrl(fileUrl)
            .storageType(ossProperties.getType())
            .status(OssUploadStatusEnum.ACTIVE.name())
            .createTime(now)
            .updateTime(now)
            .build());
    }

    /**
     * 删除文件。
     *
     * @param fileUrl 文件 URL
     * @param userId 当前用户 ID
     */
    public void delete(String fileUrl, Long userId) {
        OssUploadLog uploadLog = ossUploadLogRepository.findActiveByFileUrlAndUserId(fileUrl, userId)
            .orElseThrow(() -> exception(OSS_FILE_NOT_EXISTS));
        ossProvider.delete(uploadLog.getFileUrl());
        ossUploadLogRepository.updateStatus(uploadLog.getId(), OssUploadStatusEnum.DELETED);
    }

    /**
     * 验证文件。
     *
     * @param file 文件
     * @throws cn.haowl.hinovel.common.exception.BusinessException 文件不符合要求时抛出
     */
    private void validateFile(MultipartFile file) {
        if (file.getSize() > ossProperties.getMaxFileSize()) {
            throw exception(OSS_FILE_TOO_LARGE);
        }

        String extension = OssFileUtils.getExtension(file.getOriginalFilename(), null);
        if (extension == null || !ossProperties.getAllowedTypeList().contains(extension)) {
            throw exception(OSS_FILE_TYPE_NOT_ALLOWED);
        }
    }
}
