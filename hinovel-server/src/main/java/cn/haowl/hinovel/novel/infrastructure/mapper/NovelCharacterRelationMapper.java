package cn.haowl.hinovel.novel.infrastructure.mapper;

import cn.haowl.hinovel.novel.domain.entity.NovelCharacterRelation;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 人物关系 Mapper。
 *
 * <p>基于 MyBatis-Plus 实现人物关系的数据访问操作。</p>
 *
 * @author haowl
 */
@Mapper
public interface NovelCharacterRelationMapper extends BaseMapper<NovelCharacterRelation> {

    /**
     * 查询人物的所有关系。
     *
     * @param characterId 人物 ID
     * @return 关系列表
     */
    default List<NovelCharacterRelation> selectByCharacterId(Long characterId) {
        return selectList(new LambdaQueryWrapper<NovelCharacterRelation>()
                .eq(NovelCharacterRelation::getCharacterId, characterId));
    }

    /**
     * 查询小说的所有关系。
     *
     * @param novelId 小说 ID
     * @return 关系列表
     */
    default List<NovelCharacterRelation> selectByNovelId(Long novelId) {
        return selectList(new LambdaQueryWrapper<NovelCharacterRelation>()
                .eq(NovelCharacterRelation::getNovelId, novelId));
    }

    /**
     * 查询两个人物之间的关系。
     *
     * @param characterId 人物 ID
     * @param targetId    目标人物 ID
     * @return 关系列表
     */
    default List<NovelCharacterRelation> selectBetweenCharacters(Long characterId, Long targetId) {
        return selectList(new LambdaQueryWrapper<NovelCharacterRelation>()
                .eq(NovelCharacterRelation::getCharacterId, characterId)
                .eq(NovelCharacterRelation::getTargetId, targetId));
    }
}
