package cn.haowl.hinovel.agent.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 知识库生成面试题请求 DTO
 */
@Schema(description = "基于知识库生成面试题请求")
@Data
public class InterviewQuestionRequest {

    /**
     * 所选 Agent ID。
     */
    @Schema(description = "指定使用的 Agent ID，不传则使用内置面试助手")
    private Long agentId;

    /**
     * 关联知识库 ID。
     */
    @Schema(description = "关联知识库 ID 列表，从中检索内容生成题目")
    private List<Long> knowledgeBaseIds;

    /**
     * 面试难度。
     */
    @Schema(description = "题目难度（EASY/MEDIUM/HARD），不传则由 AI 自行决定")
    private String difficulty;

    /**
     * 面试主题。
     */
    @Schema(description = "题目方向提示，如 Java 并发、Spring Boot 等")
    private String topic;
}
