package cn.haowl.hinovel.novel.infrastructure.mapper;

import cn.haowl.hinovel.common.constant.CommonConstants;
import cn.haowl.hinovel.novel.domain.entity.NovelChapterVersion;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 小说章节历史版本 Mapper。
 *
 * <p>基于 MyBatis-Plus 实现章节版本的数据访问操作，
 * 复杂 SQL（如清理旧版本）定义在对应的 XML 映射文件中。</p>
 *
 * @author haowl
 */
@Mapper
public interface NovelChapterVersionMapper extends BaseMapper<NovelChapterVersion> {

    /**
     * 查询章节的所有历史版本（按创建时间倒序）。
     *
     * @param chapterId 章节 ID
     * @return 版本列表
     */
    default List<NovelChapterVersion> selectByChapterId(Long chapterId) {
        return selectList(new LambdaQueryWrapper<NovelChapterVersion>()
                .eq(NovelChapterVersion::getChapterId, chapterId)
                .orderByDesc(NovelChapterVersion::getCreateTime));
    }

    /**
     * 查询章节的历史版本（带数量限制，按创建时间倒序）。
     *
     * @param chapterId 章节 ID
     * @param limit     最大返回条数
     * @return 版本列表
     */
    default List<NovelChapterVersion> selectByChapterIdWithLimit(Long chapterId, int limit) {
        return selectList(new LambdaQueryWrapper<NovelChapterVersion>()
                .eq(NovelChapterVersion::getChapterId, chapterId)
                .orderByDesc(NovelChapterVersion::getCreateTime)
                .last(CommonConstants.limitOf(limit)));
    }

    /**
     * 清理章节超过指定数量的旧版本（保留最新的 N 个，已发布版本不删除）。
     *
     * <p>SQL 定义在 XML 映射文件中。</p>
     *
     * @param chapterId 章节 ID
     * @param keepCount 保留的版本数量
     * @return 删除的记录数
     */
    int deleteOldVersions(@Param("chapterId") Long chapterId, @Param("keepCount") int keepCount);

    /**
     * 查询章节的版本数量。
     *
     * @param chapterId 章节 ID
     * @return 版本数量
     */
    default int countByChapterId(Long chapterId) {
        Long count = selectCount(new LambdaQueryWrapper<NovelChapterVersion>()
                .eq(NovelChapterVersion::getChapterId, chapterId));
        return count != null ? count.intValue() : 0;
    }

    /**
     * 查询章节最新的版本。
     *
     * @param chapterId 章节 ID
     * @return 最新版本，不存在时返回 null
     */
    default NovelChapterVersion selectLatestByChapterId(Long chapterId) {
        return selectOne(new LambdaQueryWrapper<NovelChapterVersion>()
                .eq(NovelChapterVersion::getChapterId, chapterId)
                .orderByDesc(NovelChapterVersion::getCreateTime)
                .last(CommonConstants.LIMIT_ONE));
    }
}
