package cn.haowl.hinovel.agent.interfaces.dto;

import cn.haowl.hinovel.agent.constant.AuditIssueTypeEnum;
import cn.haowl.hinovel.agent.constant.AuditSeverityEnum;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String id;

    /**
     * 问题位置。
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private AIAuditIssuePosition position;

    /**
     * 有问题的原文片段。
     */
    private String originalText = "";

    /**
     * 问题类型。
     *
     * @see AuditIssueTypeEnum
     */
    private AuditIssueTypeEnum issueType = AuditIssueTypeEnum.LOGIC;

    /**
     * 问题原因。
     */
    private String reason = "";

    /**
     * 严重程度。
     *
     * @see AuditSeverityEnum
     */
    private AuditSeverityEnum severity = AuditSeverityEnum.LOW;

    /**
     * 修改建议。
     */
    private String suggestion = "";
}
