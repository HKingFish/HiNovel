package cn.haowl.hinovel.agent.interfaces.dto;

import lombok.Data;

import java.util.List;

/**
 * @author : haowl
 * @date : 2026/11/12 18:39
 * @desc : AI 审核的响应结果
 */
@Data
public class AIAuditorResponse {

    /** 审核结果 ID。 */
    private String id;

    /** 总体评估结论。 */
    private String overallAssessment;

    /** 问题列表。 */
    private List<AIAuditIssue> issues;

    /** 审核总结。 */
    private String summary;

    /** 处理耗时（毫秒）。 */
    private long processingTime;

    /** 审核 Agent ID。 */
    private String auditorId;

    /** 已覆盖的章节序号。 */
    private List<Integer> coveredChapters;
}
