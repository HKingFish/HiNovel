package cn.haowl.hinovel.agent.application.service;

import cn.haowl.hinovel.agent.infrastructure.agent.AgentPromptTemplates;
import cn.haowl.hinovel.agent.infrastructure.agent.AiServiceFactory;
import cn.haowl.hinovel.agent.infrastructure.agent.AiServiceFactory.AgentRole;
import cn.haowl.hinovel.agent.infrastructure.agent.AiServiceFactory.CallContext;
import cn.haowl.hinovel.agent.infrastructure.agent.GenericAiAssistant;
import cn.haowl.hinovel.common.constant.CommonConstants;
import cn.haowl.hinovel.novel.application.service.ChapterOutlineService;
import cn.haowl.hinovel.novel.application.service.NovelChapterService;
import cn.haowl.hinovel.novel.application.service.NovelSettingsService;
import cn.haowl.hinovel.novel.domain.entity.ChapterOutline;
import cn.haowl.hinovel.novel.domain.entity.NovelChapter;
import cn.haowl.hinovel.novel.domain.entity.NovelOutline;
import cn.haowl.hinovel.novel.domain.entity.NovelSettings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 章节发布服务。
 *
 * <p>提供章节发布功能，发布后根据 {@link NovelSettings} 配置异步触发：
 * 1. AI 总结章节大纲（存入 aiOutlineContent 字段，不覆盖用户大纲）
 * 2. 将章节内容和大纲存入向量数据库</p>
 *
 * @author haowl
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChapterPublishService {

    private final NovelChapterService chapterService;
    private final ChapterOutlineService chapterOutlineService;
    private final AiServiceFactory aiServiceFactory;
    private final ChapterVectorService chapterVectorService;
    private final NovelSettingsService novelSettingsService;

    /**
     * 调用场景常量：AI 总结大纲
     */
    private static final String CALL_SCENE_SUMMARIZE_OUTLINE = "SUMMARIZE_OUTLINE";

    /**
     * AI 总结大纲默认系统提示词（引用统一模板）
     */
    private static final String SUMMARIZE_SYSTEM_PROMPT =
            AgentPromptTemplates.SUMMARIZE_OUTLINE_SYSTEM_PROMPT;


    /**
     * 发布章节。
     *
     * <p>发布后根据 {@link NovelSettings} 配置决定是否异步触发 AI 大纲和向量存储。</p>
     *
     * @param chapterId 章节 ID
     * @return 发布结果
     */
    public PublishResult publishChapter(Long chapterId) {
        NovelChapter chapter = chapterService.getChapter(chapterId);

        // 执行发布（状态变更为 PUBLISHED）
        chapterService.publishChapter(chapterId);

        // 确保大纲记录存在
        ChapterOutline outline = chapterOutlineService.getByChapterId(chapterId);
        if (outline == null) {
            outline = createEmptyOutline(chapter);
            chapterOutlineService.saveOrUpdateOutline(outline);
        }

        // 读取小说有效配置，决定发布后的异步行为
        Long novelId = chapter.getNovelId();
        Long userId = chapter.getCreator();
        NovelSettings settings = novelSettingsService.getEffectiveSettings(novelId, userId);

        // 根据配置决定是否触发 AI 总结大纲
        if (CommonConstants.ENABLED == settings.getAutoOutlineAfterPublish()) {
            asyncSummarizeOutline(chapter, outline);
        } else {
            log.info("小说配置关闭了发布后自动生成 AI 大纲，跳过，novelId={}", novelId);
        }

        // 根据配置决定是否触发向量存储
        if (CommonConstants.ENABLED == settings.getAutoVectorAfterPublish()) {
            String outlineContent = outline.getOutlineContent();
            chapterVectorService.storeChapterVectors(
                    novelId, chapterId,
                    chapter.getTitle(), chapter.getContent(), outlineContent, userId);
        } else {
            log.info("小说配置关闭了发布后自动存储向量，跳过，novelId={}", novelId);
        }

        return new PublishResult(true, "章节发布成功");
    }

    /**
     * 异步调用 AI 总结章节大纲。
     *
     * <p>生成的大纲存入 aiOutlineContent 字段，不覆盖用户自己的 outlineContent。</p>
     *
     * @param chapter 章节信息
     * @param outline 章节大纲实体
     */
    @Async
    public void asyncSummarizeOutline(NovelChapter chapter, ChapterOutline outline) {
        Long chapterId = chapter.getId();
        Long novelId = chapter.getNovelId();
        log.info("开始 AI 总结章节大纲，章节ID={}", chapterId);
        long startTime = System.currentTimeMillis();

        CallContext callContext = aiServiceFactory.resolveCallContext(novelId, AgentRole.EDITOR, chapter.getCreator());

        try {
            GenericAiAssistant assistant = aiServiceFactory.buildByNovel(
                    novelId, AgentRole.EDITOR, GenericAiAssistant.class);
            String systemPrompt = aiServiceFactory.resolveSystemPrompt(
                    novelId, AgentRole.EDITOR, SUMMARIZE_SYSTEM_PROMPT);
            String userPrompt = buildSummarizePrompt(chapter);

            String aiOutline = assistant.chat(systemPrompt, userPrompt);
            long processingTimeMs = System.currentTimeMillis() - startTime;

            // 将 AI 大纲存入 aiOutlineContent 字段
            outline.setAiOutlineContent(aiOutline);
            chapterOutlineService.saveOrUpdateOutline(outline);

            log.info("AI 总结大纲完成，章节ID={}，耗时={}ms", chapterId, processingTimeMs);
            aiServiceFactory.logSyncCall(CALL_SCENE_SUMMARIZE_OUTLINE, callContext,
                    userPrompt, aiOutline, processingTimeMs);
        } catch (Exception e) {
            long processingTimeMs = System.currentTimeMillis() - startTime;
            log.error("AI 总结大纲失败，章节ID={}，耗时={}ms，异常信息：{}",
                    chapterId, processingTimeMs, e.getMessage(), e);
            aiServiceFactory.logSyncCallFailed(CALL_SCENE_SUMMARIZE_OUTLINE, callContext,
                    "总结章节大纲", e.getMessage(), processingTimeMs);
        }
    }

    /**
     * 构建 AI 总结大纲的用户 Prompt（委托给统一模板类）。
     *
     * @param chapter 章节信息
     * @return 用户 Prompt
     */
    private String buildSummarizePrompt(NovelChapter chapter) {
        // 尝试获取全文大纲作为参考
        String novelOutlineContent = null;
        try {
            NovelOutline novelOutline = chapterService.getNovelOutline(chapter.getNovelId());
            if (novelOutline != null && novelOutline.getOutlineContent() != null
                    && !novelOutline.getOutlineContent().isBlank()) {
                novelOutlineContent = novelOutline.getOutlineContent();
            }
        } catch (Exception e) {
            log.debug("获取全文大纲失败，跳过，异常信息：{}", e.getMessage());
        }
        return AgentPromptTemplates.buildSummarizeOutlinePrompt(chapter, novelOutlineContent);
    }

    /**
     * 创建空白大纲记录（通过工厂方法）。
     *
     * @param chapter 章节信息
     * @return 大纲实体
     */
    private ChapterOutline createEmptyOutline(NovelChapter chapter) {
        return ChapterOutline.createEmpty(chapter.getNovelId(), chapter.getId());
    }

    /**
     * 手动同步章节内容到向量数据库（由 Controller 调用）。
     *
     * @param novelId        小说 ID
     * @param chapterId      章节 ID
     * @param chapterTitle   章节标题
     * @param chapterContent 章节正文
     * @param outlineContent 章节大纲（可为空）
     * @param userId         用户 ID（用于选择 Embedding 模型）
     */
    public void syncChapterVector(Long novelId, Long chapterId,
                                  String chapterTitle, String chapterContent,
                                  String outlineContent, Long userId) {
        chapterVectorService.storeChapterVectors(
                novelId, chapterId, chapterTitle, chapterContent, outlineContent, userId);
    }

    /**
     * 发布结果。
     *
     * @param published 是否发布成功
     * @param message   结果消息
     */
    public record PublishResult(boolean published, String message) {
    }
}
