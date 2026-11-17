package cn.haowl.hinovel.agent.infrastructure.agent;

import cn.haowl.hinovel.agent.constant.AuditAssessmentEnum;
import cn.haowl.hinovel.agent.interfaces.dto.AIAuditorResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author : haowl
 * @date : 2026/11/17 19:23
 * @desc : 转换模型返回的章节审核结果
 */
@Slf4j
@Component
public class AuditResultParser {
    /**
     * 模型可能加上的 Markdown 代码围栏。
     */
    private static final String CODE_FENCE = "```";

    /**
     * JSON 代码块的起始标记。
     */
    private static final String JSON_CODE_FENCE = CODE_FENCE + "json";

    /**
     * 将模型的 JSON 转为审核响应。
     */
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 读取审核结果并分配应用生成的 ID。
     */
    public AIAuditorResponse parse(String modelOutput, Long chapterId) {
        AIAuditorResponse response = readResponse(modelOutput, chapterId);
        response.setId(UUID.randomUUID().toString());

        if (response.getIssues() == null) {
            response.setIssues(List.of());
        } else {
            response.setIssues(response.getIssues().stream().filter(Objects::nonNull).toList());
        }
        response.getIssues().forEach(issue -> issue.setId(UUID.randomUUID().toString()));
        return response;
    }

    /**
     * 解析失败时保留模型原文供用户查看。
     */
    private AIAuditorResponse readResponse(String modelOutput, Long chapterId) {
        try {
            AIAuditorResponse response =
                objectMapper.readerFor(AIAuditorResponse.class)
                    .without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                    .with(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS)
                    .readValue(stripCodeFence(modelOutput));
            if (response != null) {
                return response;
            }
            log.warn("[readResponse][模型审核结果为空，chapterId={}]", chapterId);
        } catch (JsonProcessingException error) {
            log.warn("[readResponse][模型审核结果解析失败，chapterId={}，异常类型={}]",
                chapterId, error.getClass().getSimpleName());
        }

        AIAuditorResponse fallback = new AIAuditorResponse();
        fallback.setOverallAssessment(AuditAssessmentEnum.WARNING);
        fallback.setSummary(modelOutput);
        return fallback;
    }

    /**
     * 去掉模型输出首尾的代码围栏。
     */
    private String stripCodeFence(String modelOutput) {
        String cleaned = modelOutput == null ? "" : modelOutput.trim();
        if (cleaned.startsWith(JSON_CODE_FENCE)) {
            cleaned = cleaned.substring(JSON_CODE_FENCE.length());
        } else if (cleaned.startsWith(CODE_FENCE)) {
            cleaned = cleaned.substring(CODE_FENCE.length());
        }
        if (cleaned.endsWith(CODE_FENCE)) {
            cleaned = cleaned.substring(0, cleaned.length() - CODE_FENCE.length());
        }
        return cleaned.trim();
    }
}
