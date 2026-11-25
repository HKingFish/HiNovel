package cn.haowl.hinovel.novel.domain.repository;

import cn.haowl.hinovel.novel.domain.entity.NovelChapter;
import cn.haowl.hinovel.novel.domain.query.ChapterPageQuery;

import java.util.List;
import java.util.Optional;

/**
 * 小说章节仓储接口。
 *
 * 定义小说章节实体的存储和查询操作。
 *
 * @author haowl
 */
public interface NovelChapterRepository {

    /**
     * 保存章节。
     *
     * @param chapter 章节实体
     * @return 保存后的章节
     */
    NovelChapter save(NovelChapter chapter);

    /**
     * 正文仍与入库快照一致时，标记章节已完成向量入库。
     */
    boolean markVectorStored(Long chapterId, String indexedContent);

    /**
     * 根据 ID 查询章节。
     *
     * @param id 章节 ID
     * @return 章节实体
     */
    Optional<NovelChapter> findById(Long id);

    /**
     * 根据小说 ID 查询章节列表。
     *
     * @param novelId 小说 ID
     * @return 章节列表
     */
    List<NovelChapter> findByNovelIdWithoutContent(Long novelId);

    /**
     * 根据小说 ID 和章节号查询章节。
     *
     * @param novelId       小说 ID
     * @param chapterNumber 章节号
     * @return 章节实体
     */
    Optional<NovelChapter> findByNovelIdAndChapterNumber(Long novelId, Integer chapterNumber);

    /**
     * 统计小说的章节数。
     *
     * @param novelId 小说 ID
     * @return 章节数
     */
    long countByNovelId(Long novelId);

    /**
     * 删除章节。
     *
     * @param id 章节 ID
     */
    void deleteById(Long id);

    /**
     * 根据小说 ID 删除所有章节。
     *
     * @param novelId 小说 ID
     */
    void deleteByNovelId(Long novelId);

    /**
     * 查询小说最大排序号。
     *
     * @param novelId 小说 ID
     * @return 最大排序号（不存在时返回 null）
     */
    Integer findMaxSortOrder(Long novelId);

    /**
     * 查询小说最大章节号。
     *
     * @param novelId 小说 ID
     * @return 最大章节号（不存在时返回 null）
     */
    Integer findMaxChapterNumber(Long novelId);

    /**
     * 按条件分页查询章节列表，不加载正文。
     */
    List<NovelChapter> findByNovelIdPageWithoutContent(ChapterPageQuery query);

    /**
     * 按同一分页筛选条件统计章节总数。
     */
    int countByNovelIdWithFilter(ChapterPageQuery query);

    /**
     * 查询指定章节之前的若干章。
     *
     * @param novelId       小说 ID
     * @param chapterNumber 当前章节号（不包含自身）
     * @param limit         最多获取几章
     * @return 前几章列表（按章节号正序排列）
     */
    List<NovelChapter> findPreviousChapters(Long novelId, Integer chapterNumber, int limit);

    /**
     * 查询小说所有章节，按 sortOrder 升序排列（用于重排）。
     *
     * @param novelId 小说 ID
     * @return 章节列表（按 sortOrder 升序）
     */
    List<NovelChapter> findAllByNovelIdOrderBySortOrder(Long novelId);
}
