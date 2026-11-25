package cn.haowl.hinovel.novel.domain.repository;

import cn.haowl.hinovel.novel.domain.entity.NovelAgentConfig;

import java.util.Optional;

/**
 * 小说 Agent 配置仓储接口。
 *
 * @author haowl
 */
public interface NovelAgentConfigRepository {

    /**
     * 保存小说 Agent 配置。
     */
    NovelAgentConfig save(NovelAgentConfig config);

    /**
     * 按小说 ID 查询 Agent 配置。
     */
    Optional<NovelAgentConfig> findByNovelId(Long novelId);
}
