package cn.haowl.hinovel.novel.infrastructure.mapper;

import cn.haowl.hinovel.novel.domain.entity.NovelMemo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 小说创作手记 Mapper。
 *
 * @author haowl
 */
@Mapper
public interface NovelMemoMapper extends BaseMapper<NovelMemo> {

    /**
     * 根据小说 ID 和用户 ID 查询手记。
     *
     * @param novelId 小说 ID
     * @param userId  用户 ID
     * @return 手记实体，不存在时返回 null
     */
    default NovelMemo selectByNovelIdAndUserId(Long novelId, Long userId) {
        return selectOne(new LambdaQueryWrapper<NovelMemo>()
                .eq(NovelMemo::getNovelId, novelId)
                .eq(NovelMemo::getUserId, userId));
    }
}
