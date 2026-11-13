package cn.haowl.hinovel.agent.interfaces.dto;

import lombok.Data;

import java.util.List;

/**
 * @author : haowl
 * @date : 2026/11/12 18:39
 * @desc : AI 批量审核的响应结果
 */
@Data
public class AIAuditBatchResponse {

    /**
     * 批量审核结果 ID。
     */
    private String id;

    /**
     * 各章节审核结果。
     */
    private List<AIAuditorResponse> audits;

    /**
     * 总体评估结论。
     */
    private String overallAssessment;

    /**
     * 批量审核总结。
     */
    private String summary;
}
