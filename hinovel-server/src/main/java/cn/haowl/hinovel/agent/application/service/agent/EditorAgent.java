package cn.haowl.hinovel.agent.application.service.agent;

import cn.haowl.hinovel.agent.constant.AgentRoleEnum;
import cn.haowl.hinovel.agent.infrastructure.agent.AgentPromptTemplates;
import cn.haowl.hinovel.agent.infrastructure.agent.AiServiceFactory;
import cn.haowl.hinovel.agent.infrastructure.agent.AiServiceFactory.CallContext;
import cn.haowl.hinovel.agent.infrastructure.agent.AiServiceFactory.CallLogContext;
import cn.haowl.hinovel.agent.infrastructure.agent.AuditResultParser;
import cn.haowl.hinovel.agent.infrastructure.agent.GenericAiAssistant;
import cn.haowl.hinovel.agent.interfaces.dto.AIAuditorResponse;
import cn.haowl.hinovel.agent.interfaces.dto.ChapterAuditCommand;
import cn.haowl.hinovel.ai.constant.LlmCallSceneEnum;
import cn.haowl.hinovel.novel.domain.entity.NovelChapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 编辑 Agent 服务，按小说配置审核章节并记录模型调用。
 *
 * @author haowl
 */
@Slf4j
@Service
public class EditorAgent {
    /**
     * 构建编辑 Agent 并记录调用。
     */
    @Autowired
    private AiServiceFactory aiServiceFactory;

    /**
     * 转换模型返回的审核结果。
     */
    @Autowired
    private AuditResultParser auditResultParser;

    /**
     * 调用编辑 Agent 审核章节正文。
     *
     * 从一致性、逻辑性、人物和剧情四个维度审核。章节大纲和全文大纲按命令中提供的内容纳入审核。
     * 模型结果无法解析时，返回 {@code warning}，以模型原文作为摘要，问题列表为空。
     *
     * @param command 待审核章节、可选大纲和发起审核的用户 ID
     * @return 总体评估、摘要、问题列表和处理耗时
     */
    public AIAuditorResponse auditChapter(ChapterAuditCommand command) {
        NovelChapter chapter = command.getChapter();
        Long chapterId = chapter.getId();
        log.info("[auditChapter][开始审核，chapterId={}]", chapterId);
        long startTime = System.currentTimeMillis();

        CallContext callContext =
            aiServiceFactory.prepareNovelCall(
                chapter.getNovelId(), AgentRoleEnum.EDITOR, command.getUserId(),
                AgentPromptTemplates.EDITOR_AUDIT_SYSTEM_PROMPT);
        GenericAiAssistant assistant = aiServiceFactory.buildNovelAssistant(callContext, false);
        String userPrompt =
            AgentPromptTemplates.buildAuditPrompt(
                chapter, command.getOutline(), command.getNovelOutline());

        CallLogContext logContext =
            CallLogContext.builder()
                .callScene(LlmCallSceneEnum.AUDIT)
                .callContext(callContext)
                .requestContent(userPrompt)
                .build();

        try {
            String modelOutput = assistant.chat(callContext.getSystemPrompt(), userPrompt);
            long processingTimeMs = System.currentTimeMillis() - startTime;

            AIAuditorResponse response = auditResultParser.parse(modelOutput, chapterId);
            response.setProcessingTime(processingTimeMs);

            log.info("[auditChapter][审核完成，chapterId={}，评估={}，耗时={}ms]",
                chapterId, response.getOverallAssessment(), processingTimeMs);
            aiServiceFactory.logSyncCall(logContext, modelOutput, processingTimeMs);
            return response;
        } catch (RuntimeException error) {
            long processingTimeMs = System.currentTimeMillis() - startTime;
            log.error("[auditChapter][审核失败，chapterId={}，耗时={}ms，异常类型={}]",
                chapterId, processingTimeMs, error.getClass().getSimpleName());
            aiServiceFactory.logSyncCallFailed(logContext, error.getMessage(), processingTimeMs);
            throw error;
        }
    }
}
