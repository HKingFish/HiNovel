package cn.haowl.hinovel.novel.interfaces.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 小说 Agent 配置请求 DTO
 */
@Data
public class NovelAgentConfigRequest {

    /**
     * 作者 Agent ID
     */
    @NotNull(message = "作者Agent ID不能为空")
    private Long authorAgentId;

    /**
     * 编辑 Agent ID
     */
    @NotNull(message = "编辑Agent ID不能为空")
    private Long editorAgentId;

    /**
     * 问答 Agent ID（可选）
     */
    private Long qaAgentId;
}
