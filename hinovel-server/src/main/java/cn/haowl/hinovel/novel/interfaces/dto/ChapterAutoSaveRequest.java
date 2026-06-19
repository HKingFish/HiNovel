package cn.haowl.hinovel.novel.interfaces.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 章节自动保存请求（用于diff保存）
 */
@Data
public class ChapterAutoSaveRequest {

    @NotNull(message = "章节ID不能为空")
    private Long chapterId;

    /**
     * 完整内容
     */
    private String content;

    /**
     * 与上次保存的diff（前端计算）
     */
    private String diff;

    /**
     * 字数
     */
    private Integer wordCount;

    /**
     * 客户端时间戳，用于冲突检测
     */
    private Long clientTimestamp;
}
