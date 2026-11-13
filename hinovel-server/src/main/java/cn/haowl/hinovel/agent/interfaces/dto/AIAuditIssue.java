package cn.haowl.hinovel.agent.interfaces.dto;

import lombok.Data;

/**
 * @author : haowl
 * @date : 2026/11/12 18:39
 * @desc : AI 审核发现的单个问题
 */
@Data
public class AIAuditIssue {

    /**
     * 问题 ID。
     */
    private String id;

    /**
     * 问题位置。
     */
    private AIAuditIssuePosition position;

    /**
     * 有问题的原文片段。
     */
    private String originalText;

    /**
     * 问题类型。
     */
    private String issueType;

    /**
     * 问题原因。
     */
    private String reason;

    /**
     * 严重程度。
     */
    private String severity;

    /**
     * 修改建议。
     */
    private String suggestion;
}
