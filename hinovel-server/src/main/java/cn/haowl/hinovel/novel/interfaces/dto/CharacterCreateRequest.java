package cn.haowl.hinovel.novel.interfaces.dto;

import cn.haowl.hinovel.common.constant.CommonConstants;
import cn.haowl.hinovel.novel.constant.CharacterGenderEnum;
import cn.haowl.hinovel.novel.constant.CharacterRoleTypeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建人物请求
 */
@Data
public class CharacterCreateRequest {

    /**
     * 小说 ID。
     */
    @NotNull(message = "小说ID不能为空")
    private Long novelId;

    /**
     * 人物姓名。
     */
    @NotBlank(message = "人物姓名不能为空")
    @Size(max = CommonConstants.LENGTH_100, message = "姓名长度不能超过100字符")
    private String name;

    /**
     * 人物别名。
     */
    @Size(max = CommonConstants.LENGTH_200, message = "别名长度不能超过200字符")
    private String alias;

    /**
     * 人物头像 URL。
     */
    private String avatarUrl;

    /**
     * 性别编码。
     *
     * @see CharacterGenderEnum
     */
    private String gender;

    /**
     * 年龄。
     */
    private Integer age;

    /**
     * 外貌特征。
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
     * 人物目标。
     */
    private String goals;

    /**
     * 人物能力。
     */
    private String abilities;

    /**
     * 备注。
     */
    private String notes;

    /**
     * 角色类型编码。
     *
     * @see CharacterRoleTypeEnum
     */
    private String roleType;

    /**
     * 图谱代表色。
     */
    private String color;

    /**
     * 身份或职业。
     */
    private String identity;
}
