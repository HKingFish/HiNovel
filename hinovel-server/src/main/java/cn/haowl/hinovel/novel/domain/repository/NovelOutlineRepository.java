package cn.haowl.hinovel.novel.domain.repository;

import cn.haowl.hinovel.novel.domain.entity.NovelOutline;

import java.util.Optional;

/**
 * 小说大纲仓储接口。
 *
 * <p>定义小说大纲实体的存储和查询操作。</p>
 */
public interface NovelOutlineRepository {

    /**
     * 保存大纲（新增或更新）。
     *
     * @param outline 大纲实体
     * @return 保存后的大纲
     */
    NovelOutline save(NovelOutline outline);

    /**
     * 根据小说 ID 查询大纲。
     *
     * @param novelId 小说 ID
     * @return 大纲实体
     */
    Optional<NovelOutline> findByNovelId(Long novelId);
}
