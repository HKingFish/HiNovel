package cn.haowl.hinovel.agent.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 开始面试请求 DTO
 */
@Schema(description = "开始面试请求")
@Data
public class InterviewStartRequest {

    /**
     * 每次面试允许的最多题数。
     */
    private static final int MAX_INTERVIEW_QUESTIONS = 20;

    /**
     * 问题总数。
     */
    @Schema(description = "面试题目总数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "题目数量不能为空")
    @Min(value = 1, message = "题目数量最少 1 题")
    @Max(value = MAX_INTERVIEW_QUESTIONS, message = "题目数量最多 20 题")
    private Integer totalQuestions;

    /**
     * 所选 Agent ID。
     */
    @Schema(description = "指定使用的 Agent ID，不传则使用内置面试助手")
    private Long agentId;

    /**
     * 关联知识库 ID。
     */
    @Schema(description = "关联知识库 ID 列表，AI 将结合知识库内容出题")
    private List<Long> knowledgeBaseIds;

    /**
     * 面试主题。
     */
    @Schema(description = "面试方向（如 Java 后端、前端、算法等）")
    private String topic;

    /**
     * 面试主题模式。
     */
    @Schema(description = "面试方向模式：knowledge 表示知识库模式，其他值为普通模式")
    private String topicMode;

    /**
     * 面试难度。
     */
    @Schema(description = "难度偏好：EASY / MEDIUM / HARD，不传则由 AI 自行决定")
    private String difficulty;
}
