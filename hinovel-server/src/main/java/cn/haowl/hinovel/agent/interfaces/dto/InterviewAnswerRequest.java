package cn.haowl.hinovel.agent.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 面试回答提交请求 DTO
 */
@Schema(description = "面试回答提交请求")
@Data
public class InterviewAnswerRequest {

    @Schema(description = "对话 ID（面试会话标识，由 /start 接口返回）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "对话 ID 不能为空")
    private Long conversationId;

    @Schema(description = "候选人的回答内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "回答内容不能为空")
    private String answer;

    @Schema(description = "当前是第几题（从 1 开始）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "当前题目序号不能为空")
    @Min(value = 1, message = "题目序号最小为 1")
    private Integer currentIndex;

    @Schema(description = "本次面试总题数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "总题数不能为空")
    @Min(value = 1, message = "总题数最小为 1")
    private Integer totalQuestions;

    @Schema(description = "关联知识库 ID 列表，与开始面试时保持一致")
    private List<Long> knowledgeBaseIds;

    @Schema(description = "面试方向（与开始面试时保持一致，用于知识库精准检索）")
    private String topic;

    @Schema(description = "面试方向模式：knowledge 表示知识库模式，其他值为普通模式")
    private String topicMode;
}
