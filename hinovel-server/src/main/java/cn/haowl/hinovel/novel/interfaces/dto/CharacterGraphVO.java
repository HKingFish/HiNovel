package cn.haowl.hinovel.novel.interfaces.dto;

import cn.haowl.hinovel.novel.domain.entity.NovelCharacter;
import cn.haowl.hinovel.novel.domain.entity.NovelCharacterRelation;
import lombok.Data;

import java.util.List;

/**
 * 人物关系图谱响应。
 *
 * @author haowl
 * @date 2026-11-12
 * @desc 返回小说的全部人物及人物关系
 */
@Data
public class CharacterGraphVO {

    /**
     * 人物列表
     */
    private List<NovelCharacter> characters;

    /**
     * 人物关系列表
     */
    private List<NovelCharacterRelation> relations;
}
