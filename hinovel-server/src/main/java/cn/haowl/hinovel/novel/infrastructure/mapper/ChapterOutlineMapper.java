package cn.haowl.hinovel.novel.infrastructure.mapper;

import cn.haowl.hinovel.common.constant.CommonConstants;
import cn.haowl.hinovel.novel.domain.entity.ChapterOutline;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 章节大纲 Mapper。
 *
 * <p>基于 MyBatis-Plus 实现章节大纲的数据访问操作。</p>
 *
 * @author haowl
 */
@Mapper
public interface ChapterOutlineMapper extends BaseMapper<ChapterOutline> {

    /**
     * 根据章节 ID 查询大纲。
     *
     * @param chapterId 章节 ID
     * @return 章节大纲，不存在时返回 null
     */
    default ChapterOutline selectByChapterId(Long chapterId) {
        return selectOne(new LambdaQueryWrapper<ChapterOutline>()
                .eq(ChapterOutline::getChapterId, chapterId));
    }

    /**
     * 根据小说 ID 查询所有章节大纲（按创建时间升序）。
     *
     * @param novelId 小说 ID
     * @return 章节大纲列表
     */
    default List<ChapterOutline> selectByNovelId(Long novelId) {
        return selectList(new LambdaQueryWrapper<ChapterOutline>()
                .eq(ChapterOutline::getNovelId, novelId)
                .orderByAsc(ChapterOutline::getCreateTime));
    }

    /**
     * 根据章节 ID 逻辑删除大纲。
     *
     * @param chapterId 章节 ID
     */
    default void deleteByChapterId(Long chapterId) {
        update(null, new LambdaUpdateWrapper<ChapterOutline>()
                .eq(ChapterOutline::getChapterId, chapterId)
                .set(ChapterOutline::getDeleted, CommonConstants.DELETED_TRUE));
    }
}
