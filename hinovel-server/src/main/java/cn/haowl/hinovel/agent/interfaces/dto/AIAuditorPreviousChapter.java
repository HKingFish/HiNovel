package cn.haowl.hinovel.agent.interfaces.dto;

import lombok.Data;

/**
 * @author : haowl
 * @date : 2026/11/12 18:39
 * @desc : AI 审核请求中的前情章节
 */
@Data
public class AIAuditorPreviousChapter {

    /**
     * 章节 ID。
     */
    private Long chapterId;

    /**
     * 章节序号。
     */
    private int chapterNumber;

    /**
     * 章节正文。
     */
    private String content;
}
