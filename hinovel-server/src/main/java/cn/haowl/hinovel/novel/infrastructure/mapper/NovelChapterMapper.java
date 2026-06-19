package cn.haowl.hinovel.novel.infrastructure.mapper;

import cn.haowl.hinovel.common.constant.CommonConstants;
import cn.haowl.hinovel.novel.domain.entity.NovelChapter;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 小说章节 Mapper。
 *
 * <p>基于 MyBatis-Plus 实现章节的数据访问操作，
 * 复杂 SQL（如前几章子查询）定义在对应的 XML 映射文件中。</p>
 *
 * @author haowl
 */
@Mapper
public interface NovelChapterMapper extends BaseMapper<NovelChapter> {

    /**
     * 查询小说的所有章节（按排序号升序）。
     *
     * @param novelId 小说 ID
     * @return 章节列表
     */
    default List<NovelChapter> selectByNovelIdWithoutContent(Long novelId) {
        return selectList(withoutContentWrapper()
                .eq(NovelChapter::getNovelId, novelId)
                .orderByAsc(NovelChapter::getSortOrder));
    }

    /**
     * 查询小说是否存在相同标题的章节。
     *
     * @param novelId 小说 ID
     * @param title   章节标题
     * @return 是否存在相同标题的章节
     */
    default boolean existSameTitle(Long novelId, Long id, String title) {
        return exists(new LambdaQueryWrapper<NovelChapter>()
                .eq(NovelChapter::getNovelId, novelId)
                .ne(id != null, NovelChapter::getId, id)
                .eq(NovelChapter::getTitle, title)
                .orderByAsc(NovelChapter::getSortOrder));
    }


    /**
     * 分页查询小说的章节（按章节号倒序）。
     *
     * @param novelId  小说 ID
     * @param offset   偏移量
     * @param pageSize 每页大小
     * @return 章节列表
     */
    default List<NovelChapter> selectByNovelIdPageWithoutContent(Long novelId, int offset, int pageSize) {
        return selectList(withoutContentWrapper()
                .eq(NovelChapter::getNovelId, novelId)
                .orderByDesc(NovelChapter::getChapterNumber)
                .last(CommonConstants.limitOf(offset, pageSize)));
    }

    /**
     * 查询小说的章节总数。
     *
     * @param novelId 小说 ID
     * @return 章节总数
     */
    default int countByNovelId(Long novelId) {
        Long count = selectCount(new LambdaQueryWrapper<NovelChapter>()
                .eq(NovelChapter::getNovelId, novelId));
        return count != null ? count.intValue() : 0;
    }

    /**
     * 查询小说最大的排序号。
     *
     * @param novelId 小说 ID
     * @return 最大排序号，无数据时返回 null
     */
    default Integer selectMaxSortOrder(Long novelId) {
        NovelChapter chapter = selectOne(new LambdaQueryWrapper<NovelChapter>()
                .eq(NovelChapter::getNovelId, novelId)
                .orderByDesc(NovelChapter::getSortOrder)
                .last(CommonConstants.LIMIT_ONE));
        return chapter != null ? chapter.getSortOrder() : null;
    }

    /**
     * 查询小说最大的章节号。
     *
     * @param novelId 小说 ID
     * @return 最大章节号，无数据时返回 null
     */
    default Integer selectMaxChapterNumber(Long novelId) {
        NovelChapter chapter = selectOne(new LambdaQueryWrapper<NovelChapter>()
                .eq(NovelChapter::getNovelId, novelId)
                .orderByDesc(NovelChapter::getChapterNumber)
                .last(CommonConstants.LIMIT_ONE));
        return chapter != null ? chapter.getChapterNumber() : null;
    }

    /**
     * 查询指定章节之前的若干章（按章节号倒序取 limit 条，再正序返回）。
     *
     * <p>SQL 定义在 XML 映射文件中，用于 AI 改写时携带前情提要。</p>
     *
     * @param novelId       小说 ID
     * @param chapterNumber 当前章节号（不包含自身）
     * @param limit         最多获取几章
     * @return 前几章列表（按章节号正序排列）
     */
    List<NovelChapter> selectPreviousChapters(
            @Param("novelId") Long novelId,
            @Param("chapterNumber") Integer chapterNumber,
            @Param("limit") int limit
    );


    /**
     * 查询小说的所有章节（按排序号升序）（不包含正文内容）。
     *
     * @param novelId 小说 ID
     * @return 章节列表
     */
    default List<NovelChapter> findByNovelIdWithoutContent(Long novelId) {
        // 排除 content 字段，章节列表不需要正文内容
        return selectList(withoutContentWrapper()
                .eq(NovelChapter::getNovelId, novelId)
                .eq(NovelChapter::getDeleted, CommonConstants.DELETED_FALSE)
                .orderByAsc(NovelChapter::getSortOrder)
                .orderByAsc(NovelChapter::getChapterNumber));
    }

    /**
     * 无正文内容的查询包装器。
     *
     * @return 查询包装器
     */
    static LambdaQueryWrapper<NovelChapter> withoutContentWrapper() {
        return new LambdaQueryWrapper<NovelChapter>()
                .select(NovelChapter.class, field -> !NovelChapter.CONTENT_FIELD.equals(field.getColumn()));
    }
}
