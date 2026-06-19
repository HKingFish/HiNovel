package cn.haowl.hinovel.novel.interfaces.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新大纲请求
 */
@Data
public class OutlineUpdateRequest {

    @NotNull(message = "小说ID不能为空")
    private Long novelId;

    /**
     * 大纲内容（支持Markdown）
     */
    private String content;
}
