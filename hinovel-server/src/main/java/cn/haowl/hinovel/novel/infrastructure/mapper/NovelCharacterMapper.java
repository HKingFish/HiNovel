package cn.haowl.hinovel.novel.infrastructure.mapper;

import cn.haowl.hinovel.common.constant.CommonConstants;
import cn.haowl.hinovel.novel.domain.entity.NovelCharacter;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 小说人物 Mapper。
 *
 * <p>基于 MyBatis-Plus 实现小说人物的数据访问操作。</p>
 *
 * @author haowl
 */
@Mapper
public interface NovelCharacterMapper extends BaseMapper<NovelCharacter> {

    /**
     * 查询小说的所有人物（按排序号升序、ID 升序）。
     *
     * @param novelId 小说 ID
     * @return 人物列表
     */
    default List<NovelCharacter> selectByNovelId(Long novelId) {
        return selectList(new LambdaQueryWrapper<NovelCharacter>()
                .eq(NovelCharacter::getNovelId, novelId)
                .orderByAsc(NovelCharacter::getSortOrder)
                .orderByAsc(NovelCharacter::getId));
    }

    /**
     * 获取小说最大排序号。
     *
     * @param novelId 小说 ID
     * @return 最大排序号，无数据时返回 0
     */
    default Integer selectMaxSortOrder(Long novelId) {
        LambdaQueryWrapper<NovelCharacter> wrapper = new LambdaQueryWrapper<NovelCharacter>()
                .eq(NovelCharacter::getNovelId, novelId)
                .orderByDesc(NovelCharacter::getSortOrder)
                .last(CommonConstants.LIMIT_ONE);
        NovelCharacter character = selectOne(wrapper);
        return character != null ? character.getSortOrder() : 0;
    }
}
