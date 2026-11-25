package cn.haowl.hinovel.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.EnumUtils;

/**
 * @author : haowl
 * @date : 2026/11/17 21:04
 * @desc : 小说人物性别
 */
@Getter
@AllArgsConstructor
public enum CharacterGenderEnum {

    /**
     * 男。
     */
    MALE("男"),

    /**
     * 女。
     */
    FEMALE("女"),

    /**
     * 其他。
     */
    OTHER("其他");

    /**
     * 展示名称。
     */
    private final String description;

    /**
     * 按编码查找，空值或未知编码返回 null。
     */
    public static CharacterGenderEnum fromCode(String code) {
        return EnumUtils.getEnum(CharacterGenderEnum.class, code);
    }
}
