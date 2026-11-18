package cn.haowl.hinovel.agent.interfaces.dto;

import cn.haowl.hinovel.agent.constant.AuditAssessmentEnum;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * @author : haowl
 * @date : 2026/11/12 18:39
 * @desc : AI 审核的响应结果
 */
@Data
public class AIAuditorResponse {

    /**
     * 审核结果 ID。
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String id;

    /**
     * 总体评估结论。
     *
     * @see AuditAssessmentEnum
     */
    private AuditAssessmentEnum overallAssessment;

    /**
     * 问题列表。
     */
    private List<AIAuditIssue> issues = List.of();

    /**
     * 审核总结。
     */
    private String summary = "审核完成";

    /**
     * 处理耗时（毫秒）。
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private long processingTime;

    /**
     * 审核 Agent ID。
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String auditorId;

    /**
     * 已覆盖的章节序号。
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private List<Integer> coveredChapters;
}
