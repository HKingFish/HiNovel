package cn.haowl.hinovel.novel.application.service;

import cn.haowl.hinovel.novel.domain.entity.NovelCharacter;
import cn.haowl.hinovel.novel.domain.entity.NovelCharacterRelation;
import cn.haowl.hinovel.novel.domain.repository.NovelCharacterRepository;
import cn.haowl.hinovel.novel.interfaces.dto.CharacterCreateRequest;
import cn.haowl.hinovel.novel.interfaces.dto.CharacterRelationCreateRequest;
import cn.haowl.hinovel.novel.interfaces.dto.CharacterUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static cn.haowl.hinovel.common.exception.enums.GlobalErrorCodeConstants.NOT_FOUND;
import static cn.haowl.hinovel.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 小说人物应用服务。
 *
 * <p>协调领域层完成人物的创建、更新、删除、查询及人物关系管理，
 * 通过仓储接口访问数据，使用领域实体的工厂方法和富方法操作业务逻辑。</p>
 */
@Service
@RequiredArgsConstructor
public class NovelCharacterService {

    private final NovelCharacterRepository characterRepository;
    private final NovelService novelService;

    /**
     * 创建人物。
     *
     * @param userId  用户 ID
     * @param request 创建请求
     * @return 创建的人物
     */
    @Transactional(rollbackFor = Exception.class)
    public NovelCharacter createCharacter(Long userId, CharacterCreateRequest request) {
        // 验证小说权限
        novelService.getNovelById(userId, request.getNovelId());

        // 通过工厂方法创建人物
        Integer maxOrder = characterRepository.findMaxSortOrder(request.getNovelId());
        int sortOrder = maxOrder != null ? maxOrder + 1 : 1;

        NovelCharacter character = NovelCharacter.create(
                request.getNovelId(), request.getName(),
                request.getRoleType(), sortOrder
        );

        // 补充详细信息
        character.updateBasicInfo(
                request.getName(), request.getAlias(),
                request.getGender(), request.getAge(), request.getIdentity()
        );
        character.updateCharacterProfile(
                request.getAppearance(), request.getPersonality(),
                request.getBackground(), request.getGoals(), request.getAbilities()
        );
        character.updateAvatar(request.getAvatarUrl());
        character.updateColor(request.getColor());

        if (request.getNotes() != null) {
            character.setNotes(request.getNotes());
        }

        return characterRepository.save(character);
    }

    /**
     * 更新人物。
     *
     * @param userId      用户 ID
     * @param characterId 人物 ID
     * @param request     更新请求
     * @return 更新后的人物
     */
    @Transactional(rollbackFor = Exception.class)
    public NovelCharacter updateCharacter(Long userId, Long characterId, CharacterUpdateRequest request) {
        NovelCharacter character = getCharacterById(userId, characterId);

        // 通过富方法更新基本信息
        character.updateBasicInfo(
                request.getName(), request.getAlias(),
                request.getGender(), request.getAge(), request.getIdentity()
        );
        character.updateCharacterProfile(
                request.getAppearance(), request.getPersonality(),
                request.getBackground(), request.getGoals(), request.getAbilities()
        );

        if (request.getAvatarUrl() != null) {
            character.updateAvatar(request.getAvatarUrl());
        }
        if (request.getColor() != null) {
            character.updateColor(request.getColor());
        }
        if (request.getNotes() != null) {
            character.setNotes(request.getNotes());
        }
        if (request.getRoleType() != null) {
            character.setRoleType(request.getRoleType());
        }

        return characterRepository.save(character);
    }

    /**
     * 删除人物。
     *
     * @param userId      用户 ID
     * @param characterId 人物 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteCharacter(Long userId, Long characterId) {
        NovelCharacter character = getCharacterById(userId, characterId);

        // 删除人物的所有关系（作为主体或客体）
        List<NovelCharacterRelation> relations = characterRepository.findRelationsByCharacterId(characterId);
        for (NovelCharacterRelation relation : relations) {
            characterRepository.deleteRelationById(relation.getId());
        }

        // 删除作为目标人物的关系
        List<NovelCharacterRelation> novelRelations = characterRepository.findRelationsByNovelId(character.getNovelId());
        novelRelations.stream()
                .filter(r -> r.involvesCharacter(characterId))
                .forEach(r -> characterRepository.deleteRelationById(r.getId()));

        characterRepository.deleteById(characterId);
    }

    /**
     * 获取人物详情。
     *
     * @param userId      用户 ID
     * @param characterId 人物 ID
     * @return 人物实体
     */
    public NovelCharacter getCharacterById(Long userId, Long characterId) {
        NovelCharacter character = characterRepository.findById(characterId)
            .orElseThrow(() -> exception(NOT_FOUND));

        // 验证权限
        novelService.getNovelById(userId, character.getNovelId());
        return character;
    }

    /**
     * 获取小说的所有人物。
     *
     * @param userId  用户 ID
     * @param novelId 小说 ID
     * @return 人物列表
     */
    public List<NovelCharacter> listCharactersByNovel(Long userId, Long novelId) {
        novelService.getNovelById(userId, novelId);
        return characterRepository.findByNovelId(novelId);
    }

    /**
     * 创建人物关系。
     *
     * @param userId  用户 ID
     * @param request 创建请求
     * @return 创建的关系
     */
    @Transactional(rollbackFor = Exception.class)
    public NovelCharacterRelation createRelation(Long userId, CharacterRelationCreateRequest request) {
        // 验证小说权限
        novelService.getNovelById(userId, request.getNovelId());

        // 验证人物存在
        characterRepository.findById(request.getCharacterId())
            .orElseThrow(() -> exception(NOT_FOUND));
        characterRepository.findById(request.getTargetId())
            .orElseThrow(() -> exception(NOT_FOUND));

        // 通过工厂方法创建关系（内部校验自关联）
        NovelCharacterRelation relation = NovelCharacterRelation.create(
                request.getNovelId(), request.getCharacterId(),
                request.getTargetId(), request.getRelationType(),
                request.getDescription()
        );

        return characterRepository.saveRelation(relation);
    }

    /**
     * 删除人物关系。
     *
     * @param userId     用户 ID
     * @param relationId 关系 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteRelation(Long userId, Long relationId) {
        NovelCharacterRelation relation = characterRepository.findRelationById(relationId)
            .orElseThrow(() -> exception(NOT_FOUND));

        // 验证权限
        novelService.getNovelById(userId, relation.getNovelId());
        characterRepository.deleteRelationById(relationId);
    }

    /**
     * 获取人物的所有关系。
     *
     * @param userId      用户 ID
     * @param characterId 人物 ID
     * @return 关系列表
     */
    public List<NovelCharacterRelation> listRelationsByCharacter(Long userId, Long characterId) {
        NovelCharacter character = getCharacterById(userId, characterId);
        return characterRepository.findRelationsByCharacterId(character.getId());
    }

    /**
     * 获取小说的所有关系（用于构建关系图谱）。
     *
     * @param userId  用户 ID
     * @param novelId 小说 ID
     * @return 关系列表
     */
    public List<NovelCharacterRelation> listRelationsByNovel(Long userId, Long novelId) {
        novelService.getNovelById(userId, novelId);
        return characterRepository.findRelationsByNovelId(novelId);
    }
}
