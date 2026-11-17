package cn.haowl.hinovel.infra.oss.domain.repository;

import cn.haowl.hinovel.infra.oss.constant.OssUploadStatusEnum;
import cn.haowl.hinovel.infra.oss.domain.entity.OssUploadLog;
import cn.haowl.hinovel.infra.oss.domain.query.OssUploadLogQuery;

import java.util.List;
import java.util.Optional;

/**
 * OSS 上传日志仓储接口。
 *
 * 定义上传日志实体的存储和查询操作。
 *
 * @Author : haowl
 * @Date : 2026/3/12 22:30
 */
public interface OssUploadLogRepository {

    /**
     * 保存上传日志。
     *
     * @param log 上传日志实体
     * @return 保存后的实体
     */
    OssUploadLog save(OssUploadLog log);

    /**
     * 根据ID查询上传日志。
     *
     * @param id 日志ID
     * @return 上传日志实体
     */
    Optional<OssUploadLog> findById(Long id);

    /**
     * 根据文件URL查询上传日志。
     *
     * @param fileUrl 文件URL
     * @return 上传日志实体
     */
    Optional<OssUploadLog> findByFileUrl(String fileUrl);

    /**
     * 按用户和可选业务类型查询有效上传记录。
     *
     * @param query 查询条件
     * @return 上传日志列表
     */
    List<OssUploadLog> findActiveByCondition(OssUploadLogQuery query);

    /**
     * 更新上传日志状态。
     *
     * @param fileUrl 文件URL
     * @param status  新状态
     */
    void updateStatusByFileUrl(String fileUrl, OssUploadStatusEnum status);

    /**
     * 删除上传日志。
     *
     * @param id 日志ID
     */
    void deleteById(Long id);
}
