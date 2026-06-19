package cn.haowl.hinovel.agent.application.service.agent;

import cn.haowl.hinovel.agent.infrastructure.agent.AgentPromptTemplates;
import cn.haowl.hinovel.agent.infrastructure.agent.AiServiceFactory;
import cn.haowl.hinovel.agent.infrastructure.agent.AiServiceFactory.AgentRole;
import cn.haowl.hinovel.agent.infrastructure.agent.AiServiceFactory.CallContext;
import cn.haowl.hinovel.agent.infrastructure.agent.GenericAiAssistant;
import cn.haowl.hinovel.agent.interfaces.controller.AIController;
import cn.haowl.hinovel.novel.application.service.ChapterOutlineService;
import cn.haowl.hinovel.novel.domain.entity.ChapterOutline;
import cn.haowl.hinovel.novel.domain.entity.NovelChapter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 编辑 Agent 服务。
 *
 * <p>通过 {@link AiServiceFactory} 动态构建 {@link GenericAiAssistant} 实例，
 * 以声明式方式完成章节审核。支持按小说绑定的编辑 Agent 配置选择不同的模型提供方。</p>
 *
 * <p>审核职责：
 * 1. 根据全文大纲和章节大纲判断当前章节内容是否合理
 * 2. 检查剧情一致性、逻辑连贯性、人物行为合理性
 * 3. 不合理时给出具体问题和改进建议
 * 4. 返回结构化的审核结果</p>
 *
 * @author haowl
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EditorAgent {

    private final AiServiceFactory aiServiceFactory;
    private final ChapterOutlineService chapterOutlineService;
    private final ObjectMapper objectMapper;

    /**
     * 调用场景常量：审核
     */
    private static final String CALL_SCENE_AUDIT = "AUDIT";

    /**
     * 编辑 Agent 默认系统提示词（引用统一模板）
     */
    private static final String DEFAULT_SYSTEM_PROMPT = AgentPromptTemplates.EDITOR_AUDIT_SYSTEM_PROMPT;

    /**
     * 审核章节内容。
     *
     * @param chapter      章节信息
     * @param outline      章节大纲
     * @param novelOutline 全文大纲
     * @param userId       当前用户 ID
     * @return 审核响应对象
     */
    public AIController.AIAuditorResponse auditChapter(NovelChapter chapter,
                                                       ChapterOutline outline,
                                                       String novelOutline,
                                                       Long userId) {
        log.info("编辑 Agent 审核章节，chapterId={}，title={}", chapter.getId(), chapter.getTitle());
        long startTime = System.currentTimeMillis();

        CallContext callContext = aiServiceFactory.resolveCallContext(
                chapter.getNovelId(), AgentRole.EDITOR, userId);
        GenericAiAssistant assistant = aiServiceFactory.buildByNovel(
                chapter.getNovelId(), AgentRole.EDITOR, GenericAiAssistant.class);
        String systemPrompt = aiServiceFactory.resolveSystemPrompt(
                chapter.getNovelId(), AgentRole.EDITOR, DEFAULT_SYSTEM_PROMPT);
        String userPrompt = buildAuditPrompt(chapter, outline, novelOutline);

        try {
            String rawResult = assistant.chat(systemPrompt, userPrompt);
            long processingTimeMs = System.currentTimeMillis() - startTime;
            log.debug("编辑 Agent 审核原始结果，chapterId={}，result={}", chapter.getId(), rawResult);

            AIController.AIAuditorResponse response = parseAuditResult(rawResult, chapter.getId());
            response.setProcessingTime(processingTimeMs);

            log.info("编辑 Agent 审核完成，chapterId={}，评估={}，耗时={}ms",
                    chapter.getId(), response.getOverallAssessment(), processingTimeMs);
            // 记录成功调用日志
            aiServiceFactory.logSyncCall(CALL_SCENE_AUDIT, callContext,
                    userPrompt, rawResult, processingTimeMs);
            return response;
        } catch (Exception e) {
            long processingTimeMs = System.currentTimeMillis() - startTime;
            log.error("编辑 Agent 审核失败，chapterId={}，耗时={}ms，异常信息：{}",
                    chapter.getId(), processingTimeMs, e.getMessage());
            aiServiceFactory.logSyncCallFailed(CALL_SCENE_AUDIT, callContext,
                    userPrompt, e.getMessage(), processingTimeMs);
            throw e;
        }
    }

    /**
     * 解析 LLM 返回的 JSON 审核结果。
     *
     * <p>若解析失败，返回包含原始文本的降级结果。</p>
     *
     * @param rawResult LLM 返回的原始 JSON 字符串
     * @param chapterId 章节 ID（用于日志）
     * @return 审核响应对象
     */
    private AIController.AIAuditorResponse parseAuditResult(String rawResult, Long chapterId) {
        AIController.AIAuditorResponse response = new AIController.AIAuditorResponse();
        response.setId(UUID.randomUUID().toString());

        try {
            String jsonStr = cleanJsonResponse(rawResult);
            Map<String, Object> resultMap = objectMapper.readValue(jsonStr,
                    new TypeReference<Map<String, Object>>() {
                    });

            response.setOverallAssessment(
                    (String) resultMap.getOrDefault("overallAssessment", "pass"));
            response.setSummary(
                    (String) resultMap.getOrDefault("summary", "审核完成"));
            response.setIssues(parseIssues(resultMap));

        } catch (JsonProcessingException e) {
            log.warn("编辑 Agent 审核结果 JSON 解析失败，chapterId={}，使用降级结果，异常信息：{}",
                    chapterId, e.getMessage());
            response.setOverallAssessment("warning");
            response.setSummary(rawResult);
            response.setIssues(List.of());
        }

        return response;
    }

    /**
     * 清理 LLM 返回内容中的 Markdown 代码块标记。
     *
     * @param raw 原始返回内容
     * @return 清理后的 JSON 字符串
     */
    private String cleanJsonResponse(String raw) {
        String cleaned = raw.trim();
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        return cleaned.trim();
    }

    /**
     * 从结果 Map 中解析问题列表。
     *
     * @param resultMap LLM 返回的结果 Map
     * @return 审核问题列表
     */
    @SuppressWarnings("unchecked")
    private List<AIController.AIAuditIssue> parseIssues(Map<String, Object> resultMap) {
        List<AIController.AIAuditIssue> issues = new ArrayList<>();
        Object issuesObj = resultMap.get("issues");
        if (!(issuesObj instanceof List<?>)) {
            return issues;
        }

        List<Map<String, Object>> issueList = (List<Map<String, Object>>) issuesObj;
        for (Map<String, Object> issueMap : issueList) {
            AIController.AIAuditIssue issue = new AIController.AIAuditIssue();
            issue.setId(UUID.randomUUID().toString());
            issue.setOriginalText((String) issueMap.getOrDefault("originalText", ""));
            issue.setIssueType((String) issueMap.getOrDefault("issueType", "logic"));
            issue.setReason((String) issueMap.getOrDefault("reason", ""));
            issue.setSeverity((String) issueMap.getOrDefault("severity", "low"));
            issue.setSuggestion((String) issueMap.getOrDefault("suggestion", ""));
            issues.add(issue);
        }
        return issues;
    }

    /**
     * 构建审核 Prompt（委托给统一模板类）。
     *
     * @param chapter      章节信息
     * @param outline      章节大纲
     * @param novelOutline 全文大纲
     * @return 用户 Prompt
     */
    private String buildAuditPrompt(NovelChapter chapter, ChapterOutline outline,
                                    String novelOutline) {
        return AgentPromptTemplates.buildAuditPrompt(chapter, outline, novelOutline);
    }

}
