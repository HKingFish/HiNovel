package cn.haowl.hinovel.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.EnumUtils;

/**
 * @author : haowl
 * @date : 2026/11/17 21:04
 * @desc : 小说人物角色类型
 */
@Getter
@AllArgsConstructor
public enum CharacterRoleTypeEnum {

    /**
     * 主角。
     */
    PROTAGONIST("主角"),

    /**
     * 配角。
     */
    SUPPORTING("配角"),

    /**
     * 反派。
     */
    ANTAGONIST("反派"),

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
    public static CharacterRoleTypeEnum fromCode(String code) {
        return EnumUtils.getEnum(CharacterRoleTypeEnum.class, code);
    }
}
