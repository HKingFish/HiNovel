package cn.haowl.hinovel.novel.infrastructure.repository;

import cn.haowl.hinovel.novel.domain.entity.NovelAgentConfig;
import cn.haowl.hinovel.novel.domain.repository.NovelAgentConfigRepository;
import cn.haowl.hinovel.novel.infrastructure.mapper.NovelAgentConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 小说 Agent 配置仓储实现。
 *
 * @author haowl
 */
@Repository
@RequiredArgsConstructor
public class NovelAgentConfigRepositoryImpl implements NovelAgentConfigRepository {

    private final NovelAgentConfigMapper configMapper;

    @Override
    public NovelAgentConfig save(NovelAgentConfig config) {
        if (config.getId() == null) {
            configMapper.insert(config);
        } else {
            configMapper.updateById(config);
        }
        return config;
    }

    @Override
    public Optional<NovelAgentConfig> findByNovelId(Long novelId) {
        return Optional.ofNullable(configMapper.selectByNovelId(novelId));
    }
}
