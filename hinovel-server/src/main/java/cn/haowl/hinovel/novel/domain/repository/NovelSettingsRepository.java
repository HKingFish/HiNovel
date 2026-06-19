package cn.haowl.hinovel.novel.domain.repository;

import cn.haowl.hinovel.novel.domain.entity.NovelSettings;

import java.util.Optional;

/**
 * 小说配置仓储接口。
 *
 * <p>定义小说配置和用户默认配置的存储与查询操作。</p>
 *
 * @author haowl
 */
public interface NovelSettingsRepository {

    /**
     * 保存配置。
     *
     * @param settings 配置实体
     * @return 保存后的配置
     */
    NovelSettings save(NovelSettings settings);

    /**
     * 根据小说 ID 查询小说级别配置。
     *
     * @param novelId 小说 ID
     * @return 小说配置
     */
    Optional<NovelSettings> findByNovelId(Long novelId);

    /**
     * 根据用户 ID 查询用户级别默认配置。
     *
     * @param userId 用户 ID
     * @return 用户默认配置
     */
    Optional<NovelSettings> findUserDefault(Long userId);
}
