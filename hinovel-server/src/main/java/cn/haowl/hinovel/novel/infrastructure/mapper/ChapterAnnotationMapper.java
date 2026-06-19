package cn.haowl.hinovel.novel.infrastructure.mapper;

import cn.haowl.hinovel.novel.domain.entity.ChapterAnnotation;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 章节批注 Mapper。
 *
 * <p>基于 MyBatis-Plus 实现章节批注的数据访问操作。</p>
 *
 * @author haowl
 */
@Mapper
public interface ChapterAnnotationMapper extends BaseMapper<ChapterAnnotation> {

    /**
     * 根据章节 ID 查询所有批注（按创建时间升序）。
     *
     * @param chapterId 章节 ID
     * @return 批注列表
     */
    default List<ChapterAnnotation> selectByChapterId(Long chapterId) {
        return selectList(new LambdaQueryWrapper<ChapterAnnotation>()
                .eq(ChapterAnnotation::getChapterId, chapterId)
                .orderByAsc(ChapterAnnotation::getCreateTime));
    }

    /**
     * 根据章节 ID 查询待处理的批注。
     *
     * @param chapterId 章节 ID
     * @return 待处理批注列表
     */
    default List<ChapterAnnotation> selectPendingByChapterId(Long chapterId) {
        return selectList(new LambdaQueryWrapper<ChapterAnnotation>()
                .eq(ChapterAnnotation::getChapterId, chapterId)
                .eq(ChapterAnnotation::getStatus, "PENDING")
                .orderByAsc(ChapterAnnotation::getCreateTime));
    }
}
