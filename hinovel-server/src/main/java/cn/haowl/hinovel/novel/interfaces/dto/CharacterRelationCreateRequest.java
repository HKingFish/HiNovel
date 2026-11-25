package cn.haowl.hinovel.novel.interfaces.dto;

import cn.haowl.hinovel.common.constant.CommonConstants;
import cn.haowl.hinovel.novel.constant.CharacterRelationTypeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建人物关系请求
 */
@Data
public class CharacterRelationCreateRequest {

    /**
     * 小说 ID。
     */
    @NotNull(message = "小说ID不能为空")
    private Long novelId;

    /**
     * 主体人物 ID。
     */
    @NotNull(message = "人物ID不能为空")
    private Long characterId;

    /**
     * 目标人物 ID。
     */
    @NotNull(message = "目标人物ID不能为空")
    private Long targetId;

    /**
     * 关系类型编码。
     *
     * @see CharacterRelationTypeEnum
     */
    @NotBlank(message = "关系类型不能为空")
    private String relationType;

    /**
     * 关系描述。
     */
    @Size(max = CommonConstants.LENGTH_500, message = "关系描述长度不能超过500字符")
    private String description;
}
