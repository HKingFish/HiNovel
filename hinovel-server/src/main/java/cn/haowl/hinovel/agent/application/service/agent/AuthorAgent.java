package cn.haowl.hinovel.agent.application.service.agent;

import cn.haowl.hinovel.agent.enums.AgentRole;
import cn.haowl.hinovel.agent.infrastructure.agent.AgentPromptTemplates;
import cn.haowl.hinovel.agent.infrastructure.agent.AiServiceFactory;
import cn.haowl.hinovel.agent.infrastructure.agent.AiServiceFactory.CallContext;
import cn.haowl.hinovel.agent.infrastructure.agent.GenericAiAssistant;
import cn.haowl.hinovel.agent.infrastructure.tool.NovelContentRetrievalTool;
import cn.haowl.hinovel.ai.application.vector.VectorStoreFactory;
import cn.haowl.hinovel.novel.domain.entity.ChapterOutline;
import cn.haowl.hinovel.novel.domain.entity.NovelChapter;
import dev.langchain4j.service.TokenStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 作者 Agent 服务。
 *
 * <p>通过 {@link AiServiceFactory} 动态构建 {@link GenericAiAssistant} 实例，
 * 以声明式方式完成章节改写。支持按小说绑定的作者 Agent 配置选择不同的模型提供方。</p>
 *
 * @author haowl
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorAgent {

    private final AiServiceFactory aiServiceFactory;

    private final VectorStoreFactory vectorStoreFactory;
    /**
     * 调用场景常量：改写
     */
    private static final String CALL_SCENE_REWRITE = "REWRITE";

    /**
     * 调用场景常量：根据建议重写
     */
    private static final String CALL_SCENE_REGENERATE = "REGENERATE";

    /**
     * 作者 Agent 默认系统提示词（引用统一模板）
     */
    private static final String DEFAULT_SYSTEM_PROMPT = AgentPromptTemplates.AUTHOR_SYSTEM_PROMPT;

    /**
     * 根据大纲同步改写章节。
     *
     * @param chapter 章节信息
     * @param outline 章节大纲
     * @param userId  当前用户 ID
     * @return 生成的章节内容
     */
    public String rewriteChapterByOutline(NovelChapter chapter, ChapterOutline outline, Long userId) {
        log.info("作者 Agent 改写章节，chapterId={}，title={}", chapter.getId(), chapter.getTitle());
        long startTime = System.currentTimeMillis();

        CallContext callContext = aiServiceFactory.resolveCallContext(
                chapter.getNovelId(), AgentRole.AUTHOR, userId);
        GenericAiAssistant assistant = aiServiceFactory.buildByNovel(
                chapter.getNovelId(), AgentRole.AUTHOR, GenericAiAssistant.class);
        String systemPrompt = aiServiceFactory.resolveSystemPrompt(
                chapter.getNovelId(), AgentRole.AUTHOR, DEFAULT_SYSTEM_PROMPT);
        String userPrompt = buildRewritePrompt(chapter, outline);

        try {
            String content = assistant.chat(systemPrompt, userPrompt);
            long processingTimeMs = System.currentTimeMillis() - startTime;
            log.info("作者 Agent 改写完成，chapterId={}，生成字数={}，耗时={}ms",
                    chapter.getId(), content.length(), processingTimeMs);
            // 记录成功调用日志
            aiServiceFactory.logSyncCall(CALL_SCENE_REWRITE, callContext,
                    userPrompt, content, processingTimeMs);
            return content;
        } catch (Exception e) {
            long processingTimeMs = System.currentTimeMillis() - startTime;
            log.error("作者 Agent 改写失败，chapterId={}，耗时={}ms，异常信息：{}",
                    chapter.getId(), processingTimeMs, e.getMessage());
            aiServiceFactory.logSyncCallFailed(CALL_SCENE_REWRITE, callContext,
                    userPrompt, e.getMessage(), processingTimeMs);
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
    public Flux<String> streamRewriteChapterByOutline(NovelChapter chapter, ChapterOutline outline,
                                                      Long userId) {
        return streamRewriteChapterByOutline(chapter, outline, List.of(), null, null, userId);
    }

    /**
     * 流式改写章节（SSE），支持携带前几章内容、全文大纲和用户临时需求。
     *
     * @param chapter             章节信息
     * @param outline             章节大纲
     * @param previousChapters    前几章内容（用于前情提要，可为空列表）
     * @param novelOutlineContent 全文大纲内容（可为 null）
     * @param userRequirement     用户临时需求（可为 null）
     * @param userId              当前用户 ID
     * @return 逐 Token 推送的 Flux 流
     */
    public Flux<String> streamRewriteChapterByOutline(NovelChapter chapter, ChapterOutline outline,
                                                      List<NovelChapter> previousChapters,
                                                      String novelOutlineContent,
                                                      String userRequirement,
                                                      Long userId) {
        log.info("作者 Agent 流式改写章节，chapterId={}，title={}，前情章数={}，携带全文大纲={}，携带用户需求={}",
                chapter.getId(), chapter.getTitle(),
                previousChapters.size(), novelOutlineContent != null,
                userRequirement != null && !userRequirement.isBlank());

        CallContext callContext = aiServiceFactory.resolveCallContext(
                chapter.getNovelId(), AgentRole.AUTHOR, userId);
        NovelContentRetrievalTool retrievalTool =
                NovelContentRetrievalTool.create(vectorStoreFactory, chapter.getNovelId(), userId);
        GenericAiAssistant assistant = aiServiceFactory.buildStreamingByNovelWithTools(
                chapter.getNovelId(), AgentRole.AUTHOR, GenericAiAssistant.class, retrievalTool);
        String systemPrompt = aiServiceFactory.resolveSystemPrompt(
                chapter.getNovelId(), AgentRole.AUTHOR, DEFAULT_SYSTEM_PROMPT);
        String userPrompt = buildRewritePrompt(chapter, outline,
                previousChapters, novelOutlineContent, userRequirement);

        TokenStream tokenStream = assistant.streamChat(systemPrompt, userPrompt);
        // 使用带日志记录的 tokenStreamToFlux
        return aiServiceFactory.tokenStreamToFlux(tokenStream,
                "chapterId=" + chapter.getId(),
                CALL_SCENE_REWRITE, callContext, userPrompt);
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
    public String rewriteBySuggestions(NovelChapter chapter, ChapterOutline outline,
                                       List<String> suggestions, Long userId) {
        log.info("作者 Agent 根据建议重写，chapterId={}，建议数={}", chapter.getId(), suggestions.size());
        long startTime = System.currentTimeMillis();

        CallContext callContext = aiServiceFactory.resolveCallContext(
                chapter.getNovelId(), AgentRole.AUTHOR, userId);
        GenericAiAssistant assistant = aiServiceFactory.buildByNovel(
                chapter.getNovelId(), AgentRole.AUTHOR, GenericAiAssistant.class);
        String systemPrompt = aiServiceFactory.resolveSystemPrompt(
                chapter.getNovelId(), AgentRole.AUTHOR, DEFAULT_SYSTEM_PROMPT);
        String userPrompt = buildSuggestionRewritePrompt(chapter, outline, suggestions);

        try {
            String content = assistant.chat(systemPrompt, userPrompt);
            long processingTimeMs = System.currentTimeMillis() - startTime;
            log.info("作者 Agent 建议重写完成，chapterId={}，生成字数={}，耗时={}ms",
                    chapter.getId(), content.length(), processingTimeMs);
            aiServiceFactory.logSyncCall(CALL_SCENE_REGENERATE, callContext,
                    userPrompt, content, processingTimeMs);
            return content;
        } catch (Exception e) {
            long processingTimeMs = System.currentTimeMillis() - startTime;
            log.error("作者 Agent 建议重写失败，chapterId={}，耗时={}ms，异常信息：{}",
                    chapter.getId(), processingTimeMs, e.getMessage());
            aiServiceFactory.logSyncCallFailed(CALL_SCENE_REGENERATE, callContext,
                    userPrompt, e.getMessage(), processingTimeMs);
            throw e;
        }
    }

    /**
     * 构建大纲改写 Prompt（无上下文版本，兼容旧调用）。
     *
     * @param chapter 章节信息
     * @param outline 章节大纲
     * @return 用户 Prompt
     */
    private String buildRewritePrompt(NovelChapter chapter, ChapterOutline outline) {
        return AgentPromptTemplates.buildRewritePrompt(chapter, outline, List.of(), null, null);
    }

    /**
     * 构建大纲改写 Prompt（完整版本，支持前情提要、全文大纲和用户临时需求）。
     *
     * @param chapter             章节信息
     * @param outline             章节大纲
     * @param previousChapters    前几章内容（用于前情提要）
     * @param novelOutlineContent 全文大纲内容（可为 null）
     * @param userRequirement     用户临时需求（可为 null）
     * @return 用户 Prompt
     */
    private String buildRewritePrompt(NovelChapter chapter, ChapterOutline outline,
                                      List<NovelChapter> previousChapters,
                                      String novelOutlineContent,
                                      String userRequirement) {
        return AgentPromptTemplates.buildRewritePrompt(
                chapter, outline, previousChapters, novelOutlineContent, userRequirement);
    }

    /**
     * 构建基于建议的重写 Prompt。
     *
     * @param chapter     章节信息
     * @param outline     章节大纲
     * @param suggestions 修改建议
     * @return 用户 Prompt
     */
    private String buildSuggestionRewritePrompt(NovelChapter chapter, ChapterOutline outline,
                                                List<String> suggestions) {
        return AgentPromptTemplates.buildSuggestionRewritePrompt(chapter, outline, suggestions);
    }

    // ==================== 批注改写 ====================

    /**
     * 调用场景常量：批注改写
     */
    private static final String CALL_SCENE_ANNOTATION_REWRITE = "ANNOTATION_REWRITE";

    /**
     * 流式批注改写（SSE）。
     *
     * <p>根据批注意见对指定文本片段进行改写，同时传入整章内容保证连贯性。
     * 只改写被批注的文本片段，不改动其他内容。</p>
     *
     * @param novelId            小说 ID
     * @param fullChapterContent 整章正文内容（用于保证改写连贯性）
     * @param originalText       被批注的原文片段
     * @param rewriteRequirement 改写要求（用户输入的提示词）
     * @param userId             当前用户 ID
     * @return 逐 Token 推送的 Flux 流
     */
    public Flux<String> streamAnnotationRewrite(Long novelId, String fullChapterContent,
                                                String originalText, String rewriteRequirement,
                                                Long userId) {
        log.info("作者 Agent 批注改写，novelId={}，原文长度={}，改写要求长度={}",
                novelId, originalText.length(), rewriteRequirement.length());

        CallContext callContext = aiServiceFactory.resolveCallContext(
                novelId, AgentRole.AUTHOR, userId);
        GenericAiAssistant assistant = aiServiceFactory.buildStreamingByNovel(
                novelId, AgentRole.AUTHOR, GenericAiAssistant.class);
        String systemPrompt = aiServiceFactory.resolveSystemPrompt(
                novelId, AgentRole.AUTHOR, DEFAULT_SYSTEM_PROMPT);
        String userPrompt = buildAnnotationRewritePrompt(
                fullChapterContent, originalText, rewriteRequirement);

        TokenStream tokenStream = assistant.streamChat(systemPrompt, userPrompt);
        return aiServiceFactory.tokenStreamToFlux(tokenStream,
                "annotationRewrite-novelId=" + novelId,
                CALL_SCENE_ANNOTATION_REWRITE, callContext, userPrompt);
    }

    /**
     * 构建批注改写 Prompt。
     *
     * @param fullChapterContent 整章正文内容
     * @param originalText       被批注的原文片段
     * @param rewriteRequirement 改写要求
     * @return 用户 Prompt
     */
    private String buildAnnotationRewritePrompt(String fullChapterContent,
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
