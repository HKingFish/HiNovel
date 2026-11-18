package cn.haowl.hinovel.agent.application.service.agent;

import cn.haowl.hinovel.agent.constant.AgentRoleEnum;
import cn.haowl.hinovel.agent.infrastructure.agent.AgentPromptTemplates;
import cn.haowl.hinovel.agent.infrastructure.agent.AiServiceFactory;
import cn.haowl.hinovel.agent.infrastructure.agent.AiServiceFactory.CallContext;
import cn.haowl.hinovel.agent.infrastructure.agent.AiServiceFactory.CallLogContext;
import cn.haowl.hinovel.agent.infrastructure.agent.AiServiceFactory.TokenStreamOptions;
import cn.haowl.hinovel.agent.infrastructure.agent.GenericAiAssistant;
import cn.haowl.hinovel.agent.infrastructure.tool.NovelContentRetrievalTool;
import cn.haowl.hinovel.agent.interfaces.dto.ChapterRewriteCommand;
import cn.haowl.hinovel.ai.application.vector.VectorStoreFactory;
import cn.haowl.hinovel.ai.constant.LlmCallSceneEnum;
import cn.haowl.hinovel.novel.application.service.ChapterAnnotationService;
import cn.haowl.hinovel.novel.domain.entity.ChapterAnnotation;
import cn.haowl.hinovel.novel.domain.entity.ChapterOutline;
import cn.haowl.hinovel.novel.domain.entity.NovelChapter;
import cn.haowl.hinovel.novel.domain.service.NovelDomainService;
import cn.haowl.hinovel.novel.interfaces.dto.AnnotationRewriteRequest;
import dev.langchain4j.service.TokenStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.function.Function;

import static cn.haowl.hinovel.agent.enums.AgentErrorCodeConstants.REWRITE_RESULT_EMPTY;
import static cn.haowl.hinovel.common.exception.enums.GlobalErrorCodeConstants.NOT_FOUND;
import static cn.haowl.hinovel.common.exception.util.ServiceExceptionUtil.exception;
import static cn.haowl.hinovel.common.exception.util.ServiceExceptionUtil.exception0;

/**
 * 作者 Agent 服务。
 *
 * 通过 {@link AiServiceFactory} 动态构建 {@link GenericAiAssistant} 实例，
 * 以声明式方式完成章节改写。支持按小说绑定的作者 Agent 配置选择不同的模型提供方。
 *
 * @author haowl
 */
@Slf4j
@Service
public class AuthorAgent {

    /**
     * 构建作者 Agent 并记录调用。
     */
    @Autowired
    private AiServiceFactory aiServiceFactory;

    /**
     * 按用户配置获取小说向量库。
     */
    @Autowired
    private VectorStoreFactory vectorStoreFactory;

    /**
     * 章节批注应用服务。
     */
    @Autowired
    private ChapterAnnotationService chapterAnnotationService;

    /**
     * 校验临时批注所属小说的归属。
     */
    @Autowired
    private NovelDomainService novelDomainService;
    /**
     * 作者 Agent 默认系统提示词（引用统一模板）
     */
    private static final String DEFAULT_SYSTEM_PROMPT = AgentPromptTemplates.AUTHOR_SYSTEM_PROMPT;

    /**
     * 根据大纲同步改写章节。
     *
     * @param command 改写内容与前情
     * @param userId  当前用户 ID
     * @return 生成的章节内容
     */
    public String rewriteChapterByOutline(ChapterRewriteCommand command, Long userId) {
        NovelChapter chapter = command.getChapter();
        log.info("[rewriteChapterByOutline][作者 Agent 改写章节，chapterId={}]", chapter.getId());
        long startTime = System.currentTimeMillis();

        CallContext callContext = aiServiceFactory.prepareNovelCall(
            chapter.getNovelId(), AgentRoleEnum.AUTHOR, userId, DEFAULT_SYSTEM_PROMPT);
        GenericAiAssistant assistant = aiServiceFactory.buildNovelAssistant(callContext, false);
        String userPrompt = AgentPromptTemplates.buildRewritePrompt(command);

        CallLogContext logContext =
            CallLogContext.builder()
                .callScene(LlmCallSceneEnum.REGENERATE)
                .callContext(callContext)
                .requestContent(userPrompt)
                .build();

        try {
            String content = assistant.chat(callContext.getSystemPrompt(),
                aiServiceFactory.writingPrompt(callContext, userPrompt));
            if (content == null || content.isBlank()) {
                throw exception(REWRITE_RESULT_EMPTY);
            }
            long processingTimeMs = System.currentTimeMillis() - startTime;
            log.info("[rewriteChapterByOutline][作者 Agent 改写完成，chapterId={}，生成字数={}，耗时={}ms]",
                chapter.getId(), content.length(), processingTimeMs);
            aiServiceFactory.logSyncCall(logContext, content, processingTimeMs);
            return content;
        } catch (Exception e) {
            long processingTimeMs = System.currentTimeMillis() - startTime;
            log.error("[rewriteChapterByOutline][作者 Agent 改写失败，chapterId={}，耗时={}ms，异常类型={}]",
                chapter.getId(), processingTimeMs, e.getClass().getSimpleName());
            aiServiceFactory.logSyncCallFailed(logContext, e.getMessage(), processingTimeMs);
            throw e;
        }
    }

    /**
     * 流式改写章节（SSE）。
     *
     * @param chapter 章节信息
     * @param outline 章节大纲
     * @param userId  当前用户 ID
     * @return 逐 Token 推送的 Flux 流
     */
    public Flux<String> streamRewriteChapterByOutline(
        NovelChapter chapter,
        ChapterOutline outline,
        Long userId) {
        return streamRewriteChapterByOutline(
            ChapterRewriteCommand.builder().chapter(chapter).outline(outline).build(), userId);
    }

    /**
     * 流式改写章节（SSE），支持携带前几章内容、全文大纲和用户临时需求。
     *
     * @param command 改写内容和上下文
     * @param userId  当前用户 ID
     * @return 改写文本流
     */
    public Flux<String> streamRewriteChapterByOutline(ChapterRewriteCommand command, Long userId) {
        NovelChapter chapter = command.getChapter();
        log.info("[streamRewriteChapterByOutline][作者 Agent 流式改写章节，chapterId={}，前情章数={}，携带全文大纲={}，携带用户需求={}]",
            chapter.getId(),
            command.getPreviousChapters().size(), command.getNovelOutlineContent() != null,
            command.getUserRequirement() != null && !command.getUserRequirement().isBlank());

        CallContext callContext = aiServiceFactory.prepareNovelCall(
            chapter.getNovelId(), AgentRoleEnum.AUTHOR, userId, DEFAULT_SYSTEM_PROMPT);
        NovelContentRetrievalTool retrievalTool =
            NovelContentRetrievalTool.create(vectorStoreFactory, chapter.getNovelId(), userId);
        GenericAiAssistant assistant = aiServiceFactory.buildNovelAssistant(callContext, true, retrievalTool);
        String userPrompt = AgentPromptTemplates.buildRewritePrompt(command);

        TokenStream tokenStream = assistant.streamChat(callContext.getSystemPrompt(),
            aiServiceFactory.writingPrompt(callContext, userPrompt));
        TokenStreamOptions<String> streamOptions =
            TokenStreamOptions.<String>builder()
                .logContext("chapterId=" + chapter.getId())
                .callScene(LlmCallSceneEnum.REWRITE)
                .callContext(callContext)
                .requestContent(userPrompt)
                .textEvent(Function.identity())
                .build();
        return aiServiceFactory.tokenStreamToFlux(tokenStream, streamOptions);
    }

    /**
     * 根据审核建议重写章节。
     *
     * @param chapter     章节信息
     * @param outline     章节大纲
     * @param suggestions 编辑 Agent 的建议列表
     * @param userId      当前用户 ID
     * @return 重写后的章节内容
     */
    public String rewriteBySuggestions(
        NovelChapter chapter,
        ChapterOutline outline,
        List<String> suggestions,
        Long userId) {
        log.info("[rewriteBySuggestions][作者 Agent 根据建议重写，chapterId={}，建议数={}]", chapter.getId(), suggestions.size());
        long startTime = System.currentTimeMillis();

        CallContext callContext = aiServiceFactory.prepareNovelCall(
            chapter.getNovelId(), AgentRoleEnum.AUTHOR, userId, DEFAULT_SYSTEM_PROMPT);
        GenericAiAssistant assistant = aiServiceFactory.buildNovelAssistant(callContext, false);
        String userPrompt = AgentPromptTemplates.buildSuggestionRewritePrompt(chapter, outline, suggestions);

        CallLogContext logContext =
            CallLogContext.builder()
                .callScene(LlmCallSceneEnum.REGENERATE)
                .callContext(callContext)
                .requestContent(userPrompt)
                .build();

        try {
            String content = assistant.chat(callContext.getSystemPrompt(),
                aiServiceFactory.writingPrompt(callContext, userPrompt));
            long processingTimeMs = System.currentTimeMillis() - startTime;
            log.info("[rewriteBySuggestions][作者 Agent 建议重写完成，chapterId={}，生成字数={}，耗时={}ms]",
                chapter.getId(), content.length(), processingTimeMs);
            aiServiceFactory.logSyncCall(logContext, content, processingTimeMs);
            return content;
        } catch (Exception e) {
            long processingTimeMs = System.currentTimeMillis() - startTime;
            log.error("[rewriteBySuggestions][作者 Agent 建议重写失败，chapterId={}，耗时={}ms，异常类型={}]",
                chapter.getId(), processingTimeMs, e.getClass().getSimpleName());
            aiServiceFactory.logSyncCallFailed(logContext, e.getMessage(), processingTimeMs);
            throw e;
        }
    }

    // ==================== 批注改写 ====================

    /**
     * 按批注归属或指定小说执行流式改写。
     *
     * @param request 批注改写请求
     * @param userId  当前用户 ID
     * @return 改写文本流
     */
    public Flux<String> streamAnnotationRewrite(AnnotationRewriteRequest request, Long userId) {
        Long novelId = request.getNovelId();
        Long annotationId = request.getAnnotationId();
        if (annotationId != null && 0L < annotationId) {
            ChapterAnnotation annotation = chapterAnnotationService.getAnnotationById(annotationId, userId);
            if (annotation == null) {
                return Flux.error(exception0(NOT_FOUND.getCode(), "批注不存在"));
            }
            novelId = annotation.getNovelId();
        } else {
            novelDomainService.validateOwnership(novelId, userId);
        }

        String originalText = request.getOriginalText();
        String rewriteRequirement = request.getAnnotationContent();
        log.info("[streamAnnotationRewrite][批注改写，novelId={}，原文长度={}，改写要求长度={}]",
            novelId, originalText.length(), rewriteRequirement.length());

        CallContext callContext = aiServiceFactory.prepareNovelCall(
            novelId, AgentRoleEnum.AUTHOR, userId, DEFAULT_SYSTEM_PROMPT);
        GenericAiAssistant assistant = aiServiceFactory.buildNovelAssistant(callContext, true);
        String userPrompt = buildAnnotationRewritePrompt(
            request.getFullChapterContent(), originalText, rewriteRequirement);

        TokenStream tokenStream = assistant.streamChat(callContext.getSystemPrompt(),
            aiServiceFactory.writingPrompt(callContext, userPrompt));
        TokenStreamOptions<String> streamOptions =
            TokenStreamOptions.<String>builder()
                .logContext("annotationRewrite-novelId=" + novelId)
                .callScene(LlmCallSceneEnum.ANNOTATION_REWRITE)
                .callContext(callContext)
                .requestContent(userPrompt)
                .textEvent(Function.identity())
                .build();
        return aiServiceFactory.tokenStreamToFlux(tokenStream, streamOptions);
    }

    /**
     * 构建批注改写 Prompt。
     *
     * @param fullChapterContent 整章正文内容
     * @param originalText       被批注的原文片段
     * @param rewriteRequirement 改写要求
     * @return 用户 Prompt
     */
    private String buildAnnotationRewritePrompt(
        String fullChapterContent,
        String originalText,
        String rewriteRequirement) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("以下是一章小说的完整正文内容，请仔细阅读理解上下文：\n\n");
        prompt.append("【整章正文】\n").append(fullChapterContent).append("\n\n");
        prompt.append("现在需要你对其中的一段文字进行改写。\n\n");
        prompt.append("【需要改写的原文】\n").append(originalText).append("\n\n");
        prompt.append("【改写要求】\n").append(rewriteRequirement).append("\n\n");
        prompt.append("请注意：\n");
        prompt.append("1. 只输出改写后的文本，不要输出其他内容（不要加标题、不要加解释）\n");
        prompt.append("2. 改写后的文本需要与上下文保持连贯，风格一致\n");
        prompt.append("3. 保持原文的叙事视角和时态\n");
        prompt.append("4. 改写长度可以适当调整，但不要偏离太多\n");
        return prompt.toString();
    }
}
