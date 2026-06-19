package cn.haowl.hinovel.novel.domain.repository;

import cn.haowl.hinovel.novel.domain.entity.NovelChapterVersion;

import java.util.List;
import java.util.Optional;

/**
 * 章节版本仓储接口。
 *
 * <p>定义章节版本的存储和查询操作。</p>
 *
 * @author haowl
 */
public interface NovelChapterVersionRepository {

    /**
     * 保存版本。
     *
     * @param version 版本实体
     * @return 保存后的版本
     */
    NovelChapterVersion save(NovelChapterVersion version);

    /**
     * 根据 ID 查询版本。
     *
     * @param id 版本 ID
     * @return 版本实体
     */
    Optional<NovelChapterVersion> findById(Long id);

    /**
     * 查询章节的所有版本（按创建时间倒序）。
     *
     * @param chapterId 章节 ID
     * @return 版本列表
     */
    List<NovelChapterVersion> findByChapterId(Long chapterId);

    /**
     * 查询章节的版本（带数量限制）。
     *
     * @param chapterId 章节 ID
     * @param limit     限制数量
     * @return 版本列表
     */
    List<NovelChapterVersion> findByChapterIdWithLimit(Long chapterId, int limit);

    /**
     * 查询章节最新的版本。
     *
     * @param chapterId 章节 ID
     * @return 最新版本（不存在时返回 null）
     */
    NovelChapterVersion findLatestByChapterId(Long chapterId);

    /**
     * 清理旧版本，保留最近指定数量的版本。
     *
     * @param chapterId 章节 ID
     * @param keepCount 保留数量
     */
    void cleanupOldVersions(Long chapterId, int keepCount);
}
