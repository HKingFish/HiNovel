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

    @Schema(description = "面试题目总数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "题目数量不能为空")
    @Min(value = 1, message = "题目数量最少 1 题")
    @Max(value = 20, message = "题目数量最多 20 题")
    private Integer totalQuestions;

    @Schema(description = "指定使用的 Agent ID，不传则使用内置面试助手")
    private Long agentId;

    @Schema(description = "关联知识库 ID 列表，AI 将结合知识库内容出题")
    private List<Long> knowledgeBaseIds;

    @Schema(description = "面试方向（如 Java 后端、前端、算法等）")
    private String topic;

    @Schema(description = "面试方向模式：knowledge 表示知识库模式，其他值为普通模式")
    private String topicMode;

    @Schema(description = "难度偏好：EASY / MEDIUM / HARD，不传则由 AI 自行决定")
    private String difficulty;
}
