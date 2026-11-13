package cn.haowl.hinovel.agent.interfaces.dto;

import lombok.Data;

import java.util.List;

/**
 * @author : haowl
 * @date : 2026/11/12 18:39
 * @desc : AI 审核的请求参数
 */
@Data
public class AIAuditorRequest {

    /**
     * 章节 ID。
     */
    private Long chapterId;

    /**
     * 章节正文。
     */
    private String content;

    /**
     * 全文大纲。
     */
    private String novelOutline;

    /**
     * 前几章内容。
     */
    private List<AIAuditorPreviousChapter> previousChapters;
}
