package cn.haowl.hinovel.infra.oss.application.service;

import cn.haowl.hinovel.infra.oss.domain.command.UploadCommand;
import cn.haowl.hinovel.infra.oss.domain.entity.OssUploadLog;
import cn.haowl.hinovel.infra.oss.domain.query.OssUploadLogQuery;
import cn.haowl.hinovel.infra.oss.domain.repository.OssUploadLogRepository;
import cn.haowl.hinovel.infra.oss.domain.service.OssDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * OSS 应用服务。
 *
 * 协调文件上传相关的业务用例。
 *
 * @Author : haowl
 * @Date : 2026/3/12 22:30
 */
@Slf4j
@Service
public class OssApplicationService {

    /**
     * OSS 领域服务。
     */
    @Autowired
    private OssDomainService ossDomainService;

    /**
     * 上传日志仓储。
     */
    @Autowired
    private OssUploadLogRepository ossUploadLogRepository;

    /**
     * 上传文件。
     *
     * @param command 上传命令
     * @return 文件URL
     */
    @Transactional(rollbackFor = Exception.class)
    public String upload(UploadCommand command) {
        OssUploadLog uploadLog = ossDomainService.upload(command);
        log.info("[upload][文件上传成功，uploadLogId={}，userId={}]", uploadLog.getId(), command.getUserId());
        return uploadLog.getFileUrl();
    }

    /**
     * 删除文件。
     *
     * @param fileUrl 文件 URL
     * @param userId 当前用户 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(String fileUrl, Long userId) {
        ossDomainService.delete(fileUrl, userId);
        log.info("[delete][文件删除成功，userId={}]", userId);
    }

    /**
     * 查询上传日志列表。
     *
     * @param query 查询条件
     * @return 上传日志列表
     */
    public List<OssUploadLog> listUploadLogs(OssUploadLogQuery query) {
        return ossUploadLogRepository.findActiveByCondition(query);
    }
}
