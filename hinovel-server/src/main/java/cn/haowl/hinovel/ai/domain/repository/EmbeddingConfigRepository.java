package cn.haowl.hinovel.ai.domain.repository;

import cn.haowl.hinovel.ai.domain.entity.EmbeddingConfig;

import java.util.List;
import java.util.Optional;

/**
 * 嵌入式模型配置仓储接口。
 *
 * @author wylon
 * @date 2026/3/23 14:56
 */
public interface EmbeddingConfigRepository {

    /**
     * 保存配置。
     *
     * @param config 配置实体
     * @return 保存后的实体
     */
    EmbeddingConfig save(EmbeddingConfig config);

    /**
     * 根据 ID 查询。
     *
     * @param id 配置 ID
     * @return 配置实体
     */
    Optional<EmbeddingConfig> findById(Long id);

    /**
     * 查询指定用户的所有配置。
     *
     * @param userId 用户 ID
     * @return 配置列表
     */
    List<EmbeddingConfig> findAllByUserId(Long userId);

    /**
     * 查询指定用户的激活配置。
     *
     * @param userId 用户 ID
     * @return 激活的配置，不存在返回 empty
     */
    Optional<EmbeddingConfig> findActiveByUserId(Long userId);

    /**
     * 删除配置。
     *
     * @param id 配置 ID
     */
    void deleteById(Long id);
}
