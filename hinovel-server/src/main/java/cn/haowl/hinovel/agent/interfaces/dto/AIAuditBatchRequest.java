package cn.haowl.hinovel.agent.interfaces.dto;

import lombok.Data;

import java.util.List;

/**
 * @author : haowl
 * @date : 2026/11/12 18:39
 * @desc : AI 批量审核的请求参数
 */
@Data
public class AIAuditBatchRequest {

    /** 章节 ID。 */
    private Long chapterId;

    /** 章节正文。 */
    private String content;

    /** 全文大纲。 */
    private String novelOutline;

    /** 需要审核的章节范围。 */
    private List<AIAuditChapterRange> chapterRanges;
}
