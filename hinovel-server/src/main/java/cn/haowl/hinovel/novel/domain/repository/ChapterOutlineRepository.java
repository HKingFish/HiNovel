package cn.haowl.hinovel.novel.domain.repository;

import cn.haowl.hinovel.novel.domain.entity.ChapterOutline;

import java.util.List;
import java.util.Optional;

/**
 * 章节大纲仓储接口。
 *
 * <p>定义章节大纲的存储与查询操作。</p>
 *
 * @author haowl
 */
public interface ChapterOutlineRepository {

    /**
     * 保存大纲。
     *
     * @param outline 大纲实体
     * @return 保存后的大纲
     */
    ChapterOutline save(ChapterOutline outline);

    /**
     * 根据章节 ID 查询大纲。
     *
     * @param chapterId 章节 ID
     * @return 章节大纲
     */
    Optional<ChapterOutline> findByChapterId(Long chapterId);

    /**
     * 根据小说 ID 查询所有章节大纲。
     *
     * @param novelId 小说 ID
     * @return 章节大纲列表
     */
    List<ChapterOutline> findByNovelId(Long novelId);

    /**
     * 根据章节 ID 删除大纲。
     *
     * @param chapterId 章节 ID
     */
    void deleteByChapterId(Long chapterId);
}
