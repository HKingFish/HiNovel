package cn.haowl.hinovel.novel.domain.repository;

import cn.haowl.hinovel.novel.domain.entity.NovelAgentConfig;

import java.util.Optional;

/**
 * 小说 Agent 配置仓储接口。
 *
 * @author haowl
 */
public interface NovelAgentConfigRepository {

    NovelAgentConfig save(NovelAgentConfig config);

    Optional<NovelAgentConfig> findByNovelId(Long novelId);
}
