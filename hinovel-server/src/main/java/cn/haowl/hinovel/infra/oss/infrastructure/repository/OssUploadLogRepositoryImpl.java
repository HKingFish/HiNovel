package cn.haowl.hinovel.infra.oss.infrastructure.repository;

import cn.haowl.hinovel.infra.oss.constant.OssConstants;
import cn.haowl.hinovel.infra.oss.domain.entity.OssUploadLog;
import cn.haowl.hinovel.infra.oss.domain.mapper.OssUploadLogMapper;
import cn.haowl.hinovel.infra.oss.domain.repository.OssUploadLogRepository;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * OSS 上传日志仓储实现。
 *
 * <p>基于 MyBatis-Plus 实现上传日志仓储接口。</p>
 *
 * @Author : haowl
 * @Date : 2026/3/12 22:30
 */
@Repository
@RequiredArgsConstructor
public class OssUploadLogRepositoryImpl implements OssUploadLogRepository {

    private final OssUploadLogMapper ossUploadLogMapper;

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
                .eq(OssUploadLog::getStatus, OssConstants.STATUS_ACTIVE);
        return Optional.ofNullable(ossUploadLogMapper.selectOne(wrapper));
    }

    @Override
    public List<OssUploadLog> findByUserIdAndBizType(Long userId, String bizType) {
        LambdaQueryWrapper<OssUploadLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OssUploadLog::getUserId, userId)
                .eq(OssUploadLog::getStatus, OssConstants.STATUS_ACTIVE)
                .eq(bizType != null, OssUploadLog::getBizType, bizType)
                .orderByDesc(OssUploadLog::getCreateTime);
        return ossUploadLogMapper.selectList(wrapper);
    }

    @Override
    public void updateStatusByFileUrl(String fileUrl, String status) {
        OssUploadLog updateLog = new OssUploadLog();
        updateLog.setStatus(status);
        LambdaQueryWrapper<OssUploadLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OssUploadLog::getFileUrl, fileUrl)
                .eq(OssUploadLog::getStatus, OssConstants.STATUS_ACTIVE);
        ossUploadLogMapper.update(updateLog, wrapper);
    }

    @Override
    public void deleteById(Long id) {
        ossUploadLogMapper.deleteById(id);
    }
}
