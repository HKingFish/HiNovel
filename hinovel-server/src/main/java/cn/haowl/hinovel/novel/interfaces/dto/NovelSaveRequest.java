package cn.haowl.hinovel.novel.interfaces.dto;

import cn.haowl.hinovel.common.constant.CommonConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 小说片段保存请求 DTO
 */
@Schema(description = "小说片段保存请求")
@Data
public class NovelSaveRequest {

    /**
     * 未指定模式时续写。
     */
    private static final String DEFAULT_MODE = "CONTINUE";

    /**
     * 片段标题。
     */
    @Schema(description = "片段标题（最多200字符）", example = "第一章：初遇")
    @Size(max = CommonConstants.LENGTH_200, message = "标题不能超过200个字符")
    private String title;

    /**
     * 片段正文。
     */
    @Schema(description = "片段内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "内容不能为空")
    private String content;

    /**
     * 创作模式编码。
     */
    @Schema(description = "创作模式", example = DEFAULT_MODE)
    private String mode = DEFAULT_MODE;
}
