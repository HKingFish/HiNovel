package cn.haowl.hinovel.agent.application.service.impl;

import cn.haowl.hinovel.agent.application.service.AIService;
import cn.haowl.hinovel.agent.application.service.agent.AuthorAgent;
import cn.haowl.hinovel.agent.application.service.agent.EditorAgent;
import cn.haowl.hinovel.agent.interfaces.controller.AIController;
import cn.haowl.hinovel.novel.application.service.ChapterOutlineService;
import cn.haowl.hinovel.novel.application.service.ChapterService;
import cn.haowl.hinovel.novel.application.service.NovelChapterService;
import cn.haowl.hinovel.novel.application.service.NovelSettingsService;
import cn.haowl.hinovel.novel.domain.entity.ChapterOutline;
import cn.haowl.hinovel.novel.domain.entity.NovelChapter;
import cn.haowl.hinovel.novel.domain.entity.NovelOutline;
import cn.haowl.hinovel.novel.domain.entity.NovelSettings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

import static cn.haowl.hinovel.agent.enums.AgentErrorCodeConstants.AUDIT_CHAPTER_NOT_EXISTS;
import static cn.haowl.hinovel.agent.enums.AgentErrorCodeConstants.REWRITE_CHAPTER_NOT_EXISTS;
import static cn.haowl.hinovel.common.constant.CommonConstants.ENABLED;
import static cn.haowl.hinovel.common.exception.util.ServiceExceptionUtil.exception;

/**
 * AI 功能应用服务实现类。
 *
 * <p>封装 AI 改写、审核等业务逻辑，从 Controller 层下沉至此。</p>
 *
 * @author haowl
 * @since 2024
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {

    private final AuthorAgent authorAgentService;
    private final EditorAgent editorAgentService;
    private final ChapterService chapterService;
    private final ChapterOutlineService chapterOutlineService;
    private final NovelSettingsService novelSettingsService;
    private final NovelChapterService novelChapterService;

    @Override
    public Flux<ServerSentEvent<String>> aiRewrite(AIController.AIAuthorRequest request, Long userId) {
        NovelChapter chapter = chapterService.getById(request.getChapterId());
        if (chapter == null) {
            log.warn("AI 改写失败，章节不存在，chapterId={}", request.getChapterId());
            throw exception(REWRITE_CHAPTER_NOT_EXISTS);
        }

        // 获取章节大纲（优先使用请求中的大纲内容）
        ChapterOutline outline = chapterOutlineService.getByChapterId(request.getChapterId());
        if (outline == null) {
            outline = new ChapterOutline();
            outline.setChapterId(request.getChapterId());
            outline.setNovelId(chapter.getNovelId());
        }
        // 请求中携带了大纲内容时，覆盖数据库中的大纲
        if (request.getOutline() != null && !request.getOutline().isBlank()) {
            outline.setOutlineContent(request.getOutline());
        }
        if (request.getPlotPoints() != null && !request.getPlotPoints().isBlank()) {
            outline.setPlotPoints(request.getPlotPoints());
        }
        if (request.getEmotionTone() != null && !request.getEmotionTone().isBlank()) {
            outline.setEmotionTone(request.getEmotionTone());
        }
        if (request.getSceneSetting() != null && !request.getSceneSetting().isBlank()) {
            outline.setSceneSetting(request.getSceneSetting());
        }
        // 章节内容以前端传过来的为准
        if (request.getContent() != null) {
            outline.setChapterContent(request.getContent());
        }

        // 读取小说有效配置，决定改写时携带的上下文
        NovelSettings settings = novelSettingsService.getEffectiveSettings(
            chapter.getNovelId(), chapter.getCreator());

        // 根据配置加载前几章内容作为前情提要
        List<NovelChapter> previousChapters = List.of();
        int contextChapters = settings.getRewriteContextChapters() != null
            ? settings.getRewriteContextChapters() : 0;
        if (contextChapters > 0) {
            previousChapters = novelChapterService.getPreviousChapters(
                chapter.getNovelId(), chapter.getChapterNumber(), contextChapters);
        }

        // 根据配置决定是否携带全文大纲
        String novelOutlineContent = null;
        if (ENABLED == settings.getRewriteIncludeOutline()) {
            novelOutlineContent = loadNovelOutlineContent(chapter.getNovelId());
        }

        // 将 Flux<String> 包装为 Flux<ServerSentEvent>，避免 String 被 JSON 序列化加引号
        return authorAgentService.streamRewriteChapterByOutline(
                chapter, outline, previousChapters,
                novelOutlineContent, request.getUserRequirement(), userId)
            .map(token -> ServerSentEvent.<String>builder().data(token).build())
            .onErrorResume(e -> {
                log.error("AI 改写流式异常，异常信息：{}", e.getMessage());
                return Flux.just(ServerSentEvent.<String>builder()
                    .event("error")
                    .data(e.getMessage() != null ? e.getMessage() : "AI 改写异常")
                    .build());
            });
    }

    @Override
    public AIController.AIAuditorResponse aiAudit(AIController.AIAuditorRequest request, Long userId) {
        NovelChapter chapter = chapterService.getById(request.getChapterId());
        if (chapter == null) {
            throw exception(AUDIT_CHAPTER_NOT_EXISTS);
        }

        // 使用请求中的内容覆盖数据库中的章节内容（前端可能传入编辑中的最新内容）
        if (request.getContent() != null && !request.getContent().isBlank()) {
            chapter.setContent(request.getContent());
        }

        // 获取章节大纲
        ChapterOutline outline = chapterOutlineService.getByChapterId(request.getChapterId());

        // 根据配置决定审核时是否携带全文大纲
        NovelSettings settings = novelSettingsService.getEffectiveSettings(
            chapter.getNovelId(), chapter.getCreator());
        String novelOutline = null;
        if (ENABLED == settings.getAuditIncludeOutline()) {
            // 优先使用前端传入的全文大纲，其次从数据库加载
            if (request.getNovelOutline() != null && !request.getNovelOutline().isBlank()) {
                novelOutline = request.getNovelOutline();
            } else {
                novelOutline = loadNovelOutlineContent(chapter.getNovelId());
            }
        }

        // 调用编辑 Agent 审核
        return editorAgentService.auditChapter(chapter, outline, novelOutline, userId);
    }

    /**
     * 加载小说全文大纲内容。
     *
     * @param novelId 小说 ID
     * @return 全文大纲内容，不存在或为空时返回 null
     */
    private String loadNovelOutlineContent(Long novelId) {
        NovelOutline novelOutline = novelChapterService.getNovelOutline(novelId);
        if (novelOutline == null || novelOutline.getOutlineContent() == null
            || novelOutline.getOutlineContent().isBlank()) {
            return null;
        }
        return novelOutline.getOutlineContent();
    }
}
