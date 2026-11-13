package cn.haowl.hinovel.agent.interfaces.dto;

import lombok.Data;

/**
 * @author : haowl
 * @date : 2026/11/12 18:39
 * @desc : 批量审核的章节范围
 */
@Data
public class AIAuditChapterRange {

    /**
     * 起始章节序号。
     */
    private int startChapter;

    /**
     * 结束章节序号。
     */
    private int endChapter;
}
