package cn.haowl.hinovel.novel.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建人物关系请求
 */
@Data
public class CharacterRelationCreateRequest {

    @NotNull(message = "小说ID不能为空")
    private Long novelId;

    @NotNull(message = "人物ID不能为空")
    private Long characterId;

    @NotNull(message = "目标人物ID不能为空")
    private Long targetId;

    @NotBlank(message = "关系类型不能为空")
    private String relationType;

    @Size(max = 500, message = "关系描述长度不能超过500字符")
    private String description;
}
