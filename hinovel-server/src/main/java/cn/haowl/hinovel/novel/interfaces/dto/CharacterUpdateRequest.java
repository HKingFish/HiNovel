package cn.haowl.hinovel.novel.interfaces.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新人物请求
 */
@Data
public class CharacterUpdateRequest {

    @Size(max = 100, message = "姓名长度不能超过100字符")
    private String name;

    @Size(max = 200, message = "别名长度不能超过200字符")
    private String alias;

    private String avatarUrl;

    private String gender;

    private Integer age;

    private String appearance;

    private String personality;

    private String background;

    private String goals;

    private String abilities;

    private String notes;

    private String roleType;

    private String color;

    private String identity;
}
