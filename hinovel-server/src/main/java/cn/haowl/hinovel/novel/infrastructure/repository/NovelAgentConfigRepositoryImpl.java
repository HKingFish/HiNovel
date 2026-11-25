package cn.haowl.hinovel.novel.infrastructure.repository;

import cn.haowl.hinovel.novel.domain.entity.NovelAgentConfig;
import cn.haowl.hinovel.novel.domain.repository.NovelAgentConfigRepository;
import cn.haowl.hinovel.novel.infrastructure.mapper.NovelAgentConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 小说 Agent 配置仓储实现。
 *
 * @author haowl
 */
@Repository
public class NovelAgentConfigRepositoryImpl implements NovelAgentConfigRepository {

    /**
     * 小说 Agent 配置数据访问。
     */
    @Autowired
    private NovelAgentConfigMapper configMapper;

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
