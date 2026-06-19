package cn.haowl.hinovel.infra.oss.application.service;

import cn.haowl.hinovel.infra.oss.application.command.UploadCommand;
import cn.haowl.hinovel.infra.oss.application.query.OssUploadLogQuery;
import cn.haowl.hinovel.infra.oss.domain.entity.OssUploadLog;
import cn.haowl.hinovel.infra.oss.domain.repository.OssUploadLogRepository;
import cn.haowl.hinovel.infra.oss.domain.service.OssDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * OSS 应用服务。
 *
 * <p>协调文件上传相关的业务用例。</p>
 *
 * @Author : haowl
 * @Date : 2026/3/12 22:30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OssApplicationService {

    private final OssDomainService ossDomainService;
    private final OssUploadLogRepository ossUploadLogRepository;

    /**
     * 上传文件。
     *
     * @param command 上传命令
     * @return 文件URL
     */
    @Transactional(rollbackFor = Exception.class)
    public String upload(UploadCommand command) {
        log.info("上传文件: userId={}, bizType={}, filename={}",
                command.getUserId(), command.getBizType(), command.getFile().getOriginalFilename());

        OssUploadLog uploadLog = ossDomainService.upload(
                command.getFile(),
                command.getBizType(),
                command.getBizId(),
                command.getUserId()
        );

        log.info("文件上传成功: fileUrl={}", uploadLog.getFileUrl());
        return uploadLog.getFileUrl();
    }

    /**
     * 删除文件。
     *
     * @param fileUrl 文件URL
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(String fileUrl) {
        log.info("删除文件: fileUrl={}", fileUrl);
        ossDomainService.delete(fileUrl);
        log.info("文件删除成功: fileUrl={}", fileUrl);
    }

    /**
     * 查询上传日志列表。
     *
     * @param query 查询条件
     * @return 上传日志列表
     */
    public List<OssUploadLog> listUploadLogs(OssUploadLogQuery query) {
        return ossUploadLogRepository.findByUserIdAndBizType(query.getUserId(), query.getBizType());
    }

    /**
     * 上传文件（便捷方法）。
     *
     * @param file    文件
     * @param bizType 业务类型
     * @param bizId   业务ID
     * @param userId  用户ID
     * @return 文件URL
     */
    @Transactional(rollbackFor = Exception.class)
    public String upload(MultipartFile file, String bizType, Long bizId, Long userId) {
        UploadCommand command = UploadCommand.builder()
                .file(file)
                .bizType(bizType)
                .bizId(bizId)
                .userId(userId)
                .build();
        return upload(command);
    }
}
