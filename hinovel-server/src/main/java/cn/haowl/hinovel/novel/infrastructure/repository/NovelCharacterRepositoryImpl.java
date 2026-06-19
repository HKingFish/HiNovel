package cn.haowl.hinovel.novel.infrastructure.repository;

import cn.haowl.hinovel.novel.domain.entity.NovelCharacter;
import cn.haowl.hinovel.novel.domain.entity.NovelCharacterRelation;
import cn.haowl.hinovel.novel.domain.repository.NovelCharacterRepository;
import cn.haowl.hinovel.novel.infrastructure.mapper.NovelCharacterMapper;
import cn.haowl.hinovel.novel.infrastructure.mapper.NovelCharacterRelationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 小说人物仓储实现。
 *
 * <p>基于 MyBatis-Plus 实现小说人物及人物关系仓储接口。</p>
 *
 * @author haowl
 */
@Repository
@RequiredArgsConstructor
public class NovelCharacterRepositoryImpl implements NovelCharacterRepository {

    private final NovelCharacterMapper characterMapper;
    private final NovelCharacterRelationMapper relationMapper;

    @Override
    public NovelCharacter save(NovelCharacter character) {
        if (character.getId() == null) {
            characterMapper.insert(character);
        } else {
            characterMapper.updateById(character);
        }
        return character;
    }

    @Override
    public Optional<NovelCharacter> findById(Long id) {
        return Optional.ofNullable(characterMapper.selectById(id));
    }

    @Override
    public List<NovelCharacter> findByNovelId(Long novelId) {
        return characterMapper.selectByNovelId(novelId);
    }

    @Override
    public Integer findMaxSortOrder(Long novelId) {
        return characterMapper.selectMaxSortOrder(novelId);
    }

    @Override
    public void deleteById(Long id) {
        characterMapper.deleteById(id);
    }

    @Override
    public NovelCharacterRelation saveRelation(NovelCharacterRelation relation) {
        if (relation.getId() == null) {
            relationMapper.insert(relation);
        } else {
            relationMapper.updateById(relation);
        }
        return relation;
    }

    @Override
    public Optional<NovelCharacterRelation> findRelationById(Long id) {
        return Optional.ofNullable(relationMapper.selectById(id));
    }

    @Override
    public List<NovelCharacterRelation> findRelationsByCharacterId(Long characterId) {
        return relationMapper.selectByCharacterId(characterId);
    }

    @Override
    public List<NovelCharacterRelation> findRelationsByNovelId(Long novelId) {
        return relationMapper.selectByNovelId(novelId);
    }

    @Override
    public void deleteRelationById(Long id) {
        relationMapper.deleteById(id);
    }
}
