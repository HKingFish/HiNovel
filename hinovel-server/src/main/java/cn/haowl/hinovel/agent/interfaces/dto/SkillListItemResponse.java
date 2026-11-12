package cn.haowl.hinovel.agent.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;

/**
 * @author : haowl
 * @date : 2026/11/12 16:02
 * @desc : 内置 Skill 目录项
 */
@Value
@Schema(description = "内置 Skill 目录项")
public class SkillListItemResponse {

    /** 随资源发布的稳定 ID。 */
    int id;

    /** 显示名称。 */
    String name;

    /** 使用场景描述。 */
    String description;
}
