package cn.haowl.hinovel.agent.interfaces.dto;

import cn.haowl.hinovel.common.constant.CommonConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 小说创作对话请求 DTO
 */
@Schema(description = "小说创作对话请求")
@Data
public class NovelChatRequest {

    @Schema(description = "对话 ID（首次创作时为 null，系统自动创建）")
    private Long conversationId;

    @Schema(description = "用户输入（创作指令或已有内容）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "输入内容不能为空")
    @Size(max = CommonConstants.LENGTH_5000, message = "输入内容不能超过5000个字符")
    private String message;

    @Schema(description = "创作模式", example = "CONTINUE",
            allowableValues = {"CONTINUE", "REWRITE", "DIALOGUE", "EXPAND"})
    private String mode = "CONTINUE";
}
