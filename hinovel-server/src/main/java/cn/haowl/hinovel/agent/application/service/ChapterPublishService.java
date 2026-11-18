package cn.haowl.hinovel.agent.application.service;

import cn.haowl.hinovel.agent.constant.AgentRoleEnum;
import cn.haowl.hinovel.agent.infrastructure.agent.AgentPromptTemplates;
import cn.haowl.hinovel.agent.infrastructure.agent.AiServiceFactory;
import cn.haowl.hinovel.agent.infrastructure.agent.AiServiceFactory.CallContext;
import cn.haowl.hinovel.agent.infrastructure.agent.AiServiceFactory.CallLogContext;
import cn.haowl.hinovel.agent.infrastructure.agent.GenericAiAssistant;
import cn.haowl.hinovel.ai.constant.LlmCallSceneEnum;
import cn.haowl.hinovel.common.constant.CommonConstants;
import cn.haowl.hinovel.novel.application.service.ChapterOutlineService;
import cn.haowl.hinovel.novel.application.service.NovelChapterService;
import cn.haowl.hinovel.novel.application.service.NovelSettingsService;
import cn.haowl.hinovel.novel.domain.entity.ChapterOutline;
import cn.haowl.hinovel.novel.domain.entity.NovelChapter;
import cn.haowl.hinovel.novel.domain.entity.NovelOutline;
import cn.haowl.hinovel.novel.domain.entity.NovelSettings;
import cn.haowl.hinovel.novel.domain.service.ChapterDomainService;
import cn.haowl.hinovel.novel.interfaces.dto.PublishChapterResponse;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 章节发布服务。
 *
 * 提供章节发布功能，发布后根据 {@link NovelSettings} 配置异步触发：
 * 1. AI 总结章节大纲（存入 aiOutlineContent 字段，不覆盖用户大纲）
 * 2. 将章节内容和大纲存入向量数据库
 *
 * @author haowl
 */
@Slf4j
@Service
public class ChapterPublishService {

    /**
     * 章节查询与发布。
     */
    @Autowired
    private NovelChapterService chapterService;

    /**
     * 章节大纲查询。
     */
    @Autowired
    private ChapterOutlineService chapterOutlineService;

    /**
     * 构建总结大纲所用 Agent。
     */
    @Autowired
    private AiServiceFactory aiServiceFactory;

    /**
     * 章节向量存储。
     */
    @Autowired
    private ChapterVectorService chapterVectorService;

    /**
     * 小说有效配置。
     */
    @Autowired
    private NovelSettingsService novelSettingsService;

    /**
     * 发布前校验小说归属。
     */
    @Autowired
    private ChapterDomainService chapterDomainService;

    /**
     * 发布后任务的事务事件。
     */
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    /**
     * 发布章节。
     *
     * 发布后根据 {@link NovelSettings} 配置决定是否异步触发 AI 大纲和向量存储。
     *
     * @param chapterId 章节 ID
     * @param userId 当前发布用户 ID
     * @return 发布结果
     */
    @Transactional(rollbackFor = Exception.class)
    public PublishChapterResponse publishChapter(Long chapterId, Long userId) {
        NovelChapter chapter = chapterService.publishChapter(chapterId, userId);
        ChapterOutline outline = chapterOutlineService.getOrCreateOutline(chapter);
        NovelSettings settings = novelSettingsService.getEffectiveSettings(chapter.getNovelId(), userId);
        eventPublisher.publishEvent(new ChapterPublished(chapter, outline, userId, settings));

        PublishChapterResponse response = new PublishChapterResponse();
        response.setPublished(true);
        response.setMessage("章节发布成功");
        return response;
    }

    /**
     * 发布提交后按配置执行异步任务。
     *
     * @param event 发布时的章节、配置和用户快照
     */
    @Async
    @TransactionalEventListener
    public void afterChapterPublished(ChapterPublished event) {
        NovelChapter chapter = event.getChapter();
        ChapterOutline outline = event.getOutline();
        NovelSettings settings = event.getSettings();
        if (Integer.valueOf(CommonConstants.ENABLED).equals(settings.getAutoVectorAfterPublish())) {
            chapterVectorService.storeChapterVectors(chapter, outline.getOutlineContent(), event.getUserId());
        } else {
            log.info("[afterChapterPublished][自动存储向量已关闭，novelId={}]", chapter.getNovelId());
        }
        if (Integer.valueOf(CommonConstants.ENABLED).equals(settings.getAutoOutlineAfterPublish())) {
            summarizeOutline(chapter, outline.getId(), event.getUserId());
        } else {
            log.info("[afterChapterPublished][自动生成 AI 大纲已关闭，novelId={}]", chapter.getNovelId());
        }
    }

    /**
     * 调用 AI 总结章节大纲。
     *
     * 生成的大纲存入 aiOutlineContent 字段，不覆盖用户自己的 outlineContent。
     *
     * @param chapter 章节信息
     * @param outlineId 章节大纲 ID
     * @param userId 已验证的发布用户 ID
     */
    private void summarizeOutline(NovelChapter chapter, Long outlineId, Long userId) {
        Long chapterId = chapter.getId();
        Long novelId = chapter.getNovelId();
        log.info("[summarizeOutline][开始 AI 总结章节大纲，章节ID={}]", chapterId);
        long startTime = System.currentTimeMillis();

        CallContext callContext = null;

        try {
            callContext =
                aiServiceFactory.prepareNovelCall(
                    novelId, AgentRoleEnum.EDITOR, userId, AgentPromptTemplates.SUMMARIZE_OUTLINE_SYSTEM_PROMPT);
            GenericAiAssistant assistant = aiServiceFactory.buildNovelAssistant(callContext, false);
            String userPrompt = buildSummarizePrompt(chapter);

            String aiOutline = assistant.chat(callContext.getSystemPrompt(), userPrompt);
            long processingTimeMs = System.currentTimeMillis() - startTime;

            // 生成期间用户可能编辑原大纲，仅更新 AI 大纲列。
            chapterOutlineService.saveAiOutlineContent(outlineId, aiOutline);

            log.info("[summarizeOutline][AI 总结大纲完成，章节ID={}，耗时={}ms]", chapterId, processingTimeMs);
            CallLogContext logContext =
                CallLogContext.builder()
                    .callScene(LlmCallSceneEnum.SUMMARIZE_OUTLINE)
                    .callContext(callContext)
                    .requestContent(userPrompt)
                    .build();
            aiServiceFactory.logSyncCall(logContext, aiOutline, processingTimeMs);
        } catch (Exception e) {
            long processingTimeMs = System.currentTimeMillis() - startTime;
            log.error("[summarizeOutline][AI 总结大纲失败，章节ID={}，耗时={}ms，异常类型={}]",
                chapterId, processingTimeMs, e.getClass().getSimpleName());
            if (callContext != null) {
                CallLogContext logContext =
                    CallLogContext.builder()
                        .callScene(LlmCallSceneEnum.SUMMARIZE_OUTLINE)
                        .callContext(callContext)
                        .requestContent("总结章节大纲")
                        .build();
                aiServiceFactory.logSyncCallFailed(logContext, e.getMessage(), processingTimeMs);
            }
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
            if (!novelOutline.isEmpty()) {
                novelOutlineContent = novelOutline.getOutlineContent();
            }
        } catch (Exception e) {
            log.debug("[buildSummarizePrompt][获取全文大纲失败，跳过，异常类型={}]", e.getClass().getSimpleName());
        }
        return AgentPromptTemplates.buildSummarizeOutlinePrompt(chapter, novelOutlineContent);
    }

    /**
     * 手动同步章节内容到向量数据库。
     *
     * @param chapterId 章节 ID
     * @param userId    用户 ID（用于选择 Embedding 模型）
     */
    public void syncChapterVector(Long chapterId, Long userId) {
        NovelChapter chapter = chapterDomainService.getOwnedChapter(chapterId, userId);
        ChapterOutline outline = chapterOutlineService.getByChapterId(chapterId);
        chapterVectorService.storeChapterVectors(
            chapter, outline != null ? outline.getOutlineContent() : null, userId);
    }

    /**
     * 章节发布时的任务输入快照。
     */
    @Value
    static class ChapterPublished {

        /**
         * 已发布的章节。
         */
        NovelChapter chapter;

        /**
         * 发布时的章节大纲。
         */
        ChapterOutline outline;

        /**
         * 已验证的发布者 ID。
         */
        Long userId;

        /**
         * 发布时生效的小说配置。
         */
        NovelSettings settings;
    }
}
