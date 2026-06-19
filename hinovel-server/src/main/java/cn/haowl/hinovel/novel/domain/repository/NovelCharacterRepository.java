package cn.haowl.hinovel.novel.domain.repository;

import cn.haowl.hinovel.novel.domain.entity.NovelCharacter;
import cn.haowl.hinovel.novel.domain.entity.NovelCharacterRelation;

import java.util.List;
import java.util.Optional;

/**
 * 小说人物仓储接口。
 *
 * <p>定义小说人物及人物关系的存储与查询操作。</p>
 *
 * @author haowl
 */
public interface NovelCharacterRepository {

    /**
     * 保存人物。
     *
     * @param character 人物实体
     * @return 保存后的人物
     */
    NovelCharacter save(NovelCharacter character);

    /**
     * 根据 ID 查询人物。
     *
     * @param id 人物 ID
     * @return 人物实体
     */
    Optional<NovelCharacter> findById(Long id);

    /**
     * 根据小说 ID 查询所有人物。
     *
     * @param novelId 小说 ID
     * @return 人物列表
     */
    List<NovelCharacter> findByNovelId(Long novelId);

    /**
     * 查询小说最大排序号。
     *
     * @param novelId 小说 ID
     * @return 最大排序号
     */
    Integer findMaxSortOrder(Long novelId);

    /**
     * 删除人物。
     *
     * @param id 人物 ID
     */
    void deleteById(Long id);

    // ==================== 人物关系操作 ====================

    /**
     * 保存人物关系。
     *
     * @param relation 关系实体
     * @return 保存后的关系
     */
    NovelCharacterRelation saveRelation(NovelCharacterRelation relation);

    /**
     * 根据 ID 查询关系。
     *
     * @param id 关系 ID
     * @return 关系实体
     */
    Optional<NovelCharacterRelation> findRelationById(Long id);

    /**
     * 根据人物 ID 查询所有关系。
     *
     * @param characterId 人物 ID
     * @return 关系列表
     */
    List<NovelCharacterRelation> findRelationsByCharacterId(Long characterId);

    /**
     * 根据小说 ID 查询所有关系。
     *
     * @param novelId 小说 ID
     * @return 关系列表
     */
    List<NovelCharacterRelation> findRelationsByNovelId(Long novelId);

    /**
     * 删除关系。
     *
     * @param id 关系 ID
     */
    void deleteRelationById(Long id);
}
