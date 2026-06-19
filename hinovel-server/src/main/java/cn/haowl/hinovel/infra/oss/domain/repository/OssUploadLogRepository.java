package cn.haowl.hinovel.infra.oss.domain.repository;

import cn.haowl.hinovel.infra.oss.domain.entity.OssUploadLog;

import java.util.List;
import java.util.Optional;

/**
 * OSS 上传日志仓储接口。
 *
 * <p>定义上传日志实体的存储和查询操作。</p>
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
     * 根据用户ID和业务类型查询上传日志列表。
     *
     * @param userId  用户ID
     * @param bizType 业务类型
     * @return 上传日志列表
     */
    List<OssUploadLog> findByUserIdAndBizType(Long userId, String bizType);

    /**
     * 更新上传日志状态。
     *
     * @param fileUrl 文件URL
     * @param status  新状态
     */
    void updateStatusByFileUrl(String fileUrl, String status);

    /**
     * 删除上传日志。
     *
     * @param id 日志ID
     */
    void deleteById(Long id);
}
