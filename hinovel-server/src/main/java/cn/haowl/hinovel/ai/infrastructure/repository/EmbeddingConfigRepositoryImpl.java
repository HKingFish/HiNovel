package cn.haowl.hinovel.ai.infrastructure.repository;

import cn.haowl.hinovel.ai.domain.entity.EmbeddingConfig;
import cn.haowl.hinovel.ai.domain.repository.EmbeddingConfigRepository;
import cn.haowl.hinovel.ai.infrastructure.mapper.EmbeddingConfigMapper;
import cn.haowl.hinovel.common.constant.CommonConstants;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 嵌入式模型配置仓储实现。
 *
 * @author wylon
 * @date 2026/3/23 14:56
 */
@Repository
@RequiredArgsConstructor
public class EmbeddingConfigRepositoryImpl implements EmbeddingConfigRepository {

    private final EmbeddingConfigMapper embeddingConfigMapper;

    @Override
    public EmbeddingConfig save(EmbeddingConfig config) {
        if (config.getId() == null) {
            embeddingConfigMapper.insert(config);
        } else {
            embeddingConfigMapper.updateById(config);
        }
        return config;
    }

    @Override
    public Optional<EmbeddingConfig> findById(Long id) {
        return Optional.ofNullable(embeddingConfigMapper.selectById(id));
    }

    @Override
    public List<EmbeddingConfig> findAllByUserId(Long userId) {
        LambdaQueryWrapper<EmbeddingConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EmbeddingConfig::getUserId, userId)
                .orderByAsc(EmbeddingConfig::getCreateTime);
        return embeddingConfigMapper.selectList(wrapper);
    }

    @Override
    public Optional<EmbeddingConfig> findActiveByUserId(Long userId) {
        LambdaQueryWrapper<EmbeddingConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EmbeddingConfig::getUserId, userId)
                .eq(EmbeddingConfig::getIsActive, CommonConstants.ENABLED)
                .orderByAsc(EmbeddingConfig::getCreateTime)
                .last(CommonConstants.LIMIT_ONE);
        return Optional.ofNullable(embeddingConfigMapper.selectOne(wrapper));
    }

    @Override
    public void deleteById(Long id) {
        embeddingConfigMapper.deleteById(id);
    }
}
