package cn.haowl.hinovel.infra.oss.infrastructure.repository;

import cn.haowl.hinovel.infra.oss.constant.OssUploadStatusEnum;
import cn.haowl.hinovel.infra.oss.domain.entity.OssUploadLog;
import cn.haowl.hinovel.infra.oss.domain.mapper.OssUploadLogMapper;
import cn.haowl.hinovel.infra.oss.domain.query.OssUploadLogQuery;
import cn.haowl.hinovel.infra.oss.domain.repository.OssUploadLogRepository;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * OSS 上传日志仓储实现。
 *
 * 基于 MyBatis-Plus 实现上传日志仓储接口。
 *
 * @Author : haowl
 * @Date : 2026/3/12 22:30
 */
@Repository
public class OssUploadLogRepositoryImpl implements OssUploadLogRepository {

    /**
     * 上传日志数据访问。
     */
    @Autowired
    private OssUploadLogMapper ossUploadLogMapper;

    @Override
    public OssUploadLog save(OssUploadLog log) {
        if (log.getId() == null) {
            ossUploadLogMapper.insert(log);
        } else {
            ossUploadLogMapper.updateById(log);
        }
        return log;
    }

    @Override
    public Optional<OssUploadLog> findById(Long id) {
        return Optional.ofNullable(ossUploadLogMapper.selectById(id));
    }

    @Override
    public Optional<OssUploadLog> findByFileUrl(String fileUrl) {
        LambdaQueryWrapper<OssUploadLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OssUploadLog::getFileUrl, fileUrl)
            .eq(OssUploadLog::getStatus, OssUploadStatusEnum.ACTIVE.name());
        return Optional.ofNullable(ossUploadLogMapper.selectOne(wrapper));
    }

    @Override
    public List<OssUploadLog> findActiveByCondition(OssUploadLogQuery query) {
        LambdaQueryWrapper<OssUploadLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OssUploadLog::getUserId, query.getUserId())
            .eq(OssUploadLog::getStatus, OssUploadStatusEnum.ACTIVE.name())
            .eq(query.getBizType() != null, OssUploadLog::getBizType, query.getBizType())
                .orderByDesc(OssUploadLog::getCreateTime);
        return ossUploadLogMapper.selectList(wrapper);
    }

    @Override
    public void updateStatusByFileUrl(String fileUrl, OssUploadStatusEnum status) {
        OssUploadLog updateLog = new OssUploadLog();
        updateLog.setStatus(status.name());
        LambdaQueryWrapper<OssUploadLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OssUploadLog::getFileUrl, fileUrl)
            .eq(OssUploadLog::getStatus, OssUploadStatusEnum.ACTIVE.name());
        ossUploadLogMapper.update(updateLog, wrapper);
    }

    @Override
    public void deleteById(Long id) {
        ossUploadLogMapper.deleteById(id);
    }
}
