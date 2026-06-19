package cn.haowl.hinovel.novel.domain.entity;

import cn.haowl.hinovel.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 小说人物实体。
 *
 * <p>对应 novel_character 表，封装人物基本信息、人设描述
 * 以及角色类型等核心业务逻辑。</p>
 *
 * @author haowl
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("novel_character")
public class NovelCharacter extends BaseEntity {

    /**
     * 角色类型：主角。
     */
    public static final String ROLE_TYPE_PROTAGONIST = "PROTAGONIST";

    /**
     * 角色类型：配角。
     */
    public static final String ROLE_TYPE_SUPPORTING = "SUPPORTING";

    /**
     * 角色类型：反派。
     */
    public static final String ROLE_TYPE_ANTAGONIST = "ANTAGONIST";

    /**
     * 角色类型：其他。
     */
    public static final String ROLE_TYPE_OTHER = "OTHER";

    /**
     * 所属小说 ID。
     */
    private Long novelId;

    /**
     * 人物姓名。
     */
    private String name;

    /**
     * 别名/绰号。
     */
    private String alias;

    /**
     * 人物头像 URL。
     */
    private String avatarUrl;

    /**
     * 性别：MALE-男，FEMALE-女，OTHER-其他。
     */
    private String gender;

    /**
     * 年龄。
     */
    private Integer age;

    /**
     * 外貌特征描述。
     */
    private String appearance;

    /**
     * 性格特点。
     */
    private String personality;

    /**
     * 背景故事。
     */
    private String background;

    /**
     * 目标/动机。
     */
    private String goals;

    /**
     * 能力/技能。
     */
    private String abilities;

    /**
     * 备注。
     */
    private String notes;

    /**
     * 角色类型：PROTAGONIST-主角，SUPPORTING-配角，ANTAGONIST-反派，OTHER-其他。
     */
    private String roleType;

    /**
     * 代表色，用于图谱显示，如 #ff6b6b。
     */
    private String color;

    /**
     * 身份/职业，如"学生"、"医生"。
     */
    private String identity;

    /**
     * 排序序号。
     */
    private Integer sortOrder;

    // ==================== 工厂方法 ====================

    /**
     * 创建人物。
     *
     * @param novelId   小说 ID
     * @param name      人物姓名
     * @param roleType  角色类型
     * @param sortOrder 排序序号
     * @return 人物实体
     */
    public static NovelCharacter create(Long novelId, String name, String roleType, Integer sortOrder) {
        NovelCharacter character = new NovelCharacter();
        character.setNovelId(novelId);
        character.setName(name);
        character.setRoleType(roleType != null ? roleType : ROLE_TYPE_OTHER);
        character.setSortOrder(sortOrder != null ? sortOrder : 0);
        return character;
    }

    // ==================== 业务方法 ====================

    /**
     * 更新基本信息。
     *
     * @param name     姓名（为 null 时不更新）
     * @param alias    别名（为 null 时不更新）
     * @param gender   性别（为 null 时不更新）
     * @param age      年龄（为 null 时不更新）
     * @param identity 身份/职业（为 null 时不更新）
     */
    public void updateBasicInfo(String name, String alias, String gender, Integer age, String identity) {
        if (name != null) {
            this.name = name;
        }
        if (alias != null) {
            this.alias = alias;
        }
        if (gender != null) {
            this.gender = gender;
        }
        if (age != null) {
            this.age = age;
        }
        if (identity != null) {
            this.identity = identity;
        }
    }

    /**
     * 更新人设描述。
     *
     * @param appearance  外貌特征（为 null 时不更新）
     * @param personality 性格特点（为 null 时不更新）
     * @param background  背景故事（为 null 时不更新）
     * @param goals       目标/动机（为 null 时不更新）
     * @param abilities   能力/技能（为 null 时不更新）
     */
    public void updateCharacterProfile(String appearance, String personality,
                                       String background, String goals, String abilities) {
        if (appearance != null) {
            this.appearance = appearance;
        }
        if (personality != null) {
            this.personality = personality;
        }
        if (background != null) {
            this.background = background;
        }
        if (goals != null) {
            this.goals = goals;
        }
        if (abilities != null) {
            this.abilities = abilities;
        }
    }

    /**
     * 更新头像。
     *
     * @param avatarUrl 头像 URL
     */
    public void updateAvatar(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    /**
     * 更新代表色。
     *
     * @param color 代表色（十六进制颜色值）
     */
    public void updateColor(String color) {
        this.color = color;
    }

    // ==================== 状态判断 ====================

    /**
     * 判断是否为主角。
     *
     * @return 主角返回 true
     */
    public boolean isProtagonist() {
        return ROLE_TYPE_PROTAGONIST.equals(this.roleType);
    }

    /**
     * 判断是否为配角。
     *
     * @return 配角返回 true
     */
    public boolean isSupporting() {
        return ROLE_TYPE_SUPPORTING.equals(this.roleType);
    }

    /**
     * 判断是否为反派。
     *
     * @return 反派返回 true
     */
    public boolean isAntagonist() {
        return ROLE_TYPE_ANTAGONIST.equals(this.roleType);
    }

    /**
     * 判断人物是否归属于指定小说。
     *
     * @param novelId 小说 ID
     * @return 归属返回 true
     */
    public boolean belongsToNovel(Long novelId) {
        return this.novelId != null && this.novelId.equals(novelId);
    }
}
