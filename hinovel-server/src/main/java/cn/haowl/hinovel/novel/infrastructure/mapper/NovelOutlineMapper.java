package cn.haowl.hinovel.novel.infrastructure.mapper;

import cn.haowl.hinovel.common.constant.CommonConstants;
import cn.haowl.hinovel.novel.domain.entity.NovelOutline;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 小说大纲 Mapper。
 *
 * <p>基于 MyBatis-Plus 实现小说大纲的数据访问操作。</p>
 */
@Mapper
public interface NovelOutlineMapper extends BaseMapper<NovelOutline> {

    /**
     * 根据小说 ID 查询大纲。
     *
     * @param novelId 小说 ID
     * @return 大纲实体（不存在时返回 null）
     */
    default NovelOutline selectByNovelId(Long novelId) {
        return selectOne(new LambdaQueryWrapper<NovelOutline>()
                .eq(NovelOutline::getNovelId, novelId)
                .last(CommonConstants.LIMIT_ONE));
    }
}
