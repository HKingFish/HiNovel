package cn.haowl.hinovel.agent.application.service.impl;

import cn.haowl.hinovel.agent.application.service.AIService;
import cn.haowl.hinovel.agent.application.service.agent.AuthorAgent;
import cn.haowl.hinovel.agent.application.service.agent.EditorAgent;
import cn.haowl.hinovel.agent.interfaces.dto.AIAuditBatchRequest;
import cn.haowl.hinovel.agent.interfaces.dto.AIAuditBatchResponse;
import cn.haowl.hinovel.agent.interfaces.dto.AIAuditorRequest;
import cn.haowl.hinovel.agent.interfaces.dto.AIAuditorResponse;
import cn.haowl.hinovel.agent.interfaces.dto.AIAuthorRequest;
import cn.haowl.hinovel.agent.interfaces.dto.AIAuthorResponse;
import cn.haowl.hinovel.agent.interfaces.dto.ChapterAuditCommand;
import cn.haowl.hinovel.agent.interfaces.dto.ChapterRewriteCommand;
import cn.haowl.hinovel.novel.application.service.ChapterOutlineService;
import cn.haowl.hinovel.novel.application.service.ChapterService;
import cn.haowl.hinovel.novel.application.service.NovelChapterService;
import cn.haowl.hinovel.novel.application.service.NovelSettingsService;
import cn.haowl.hinovel.novel.domain.entity.ChapterOutline;
import cn.haowl.hinovel.novel.domain.entity.NovelChapter;
import cn.haowl.hinovel.novel.domain.entity.NovelOutline;
import cn.haowl.hinovel.novel.domain.entity.NovelSettings;
import cn.haowl.hinovel.novel.domain.service.NovelDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

import static cn.haowl.hinovel.agent.enums.AgentErrorCodeConstants.AUDIT_BATCH_UNAVAILABLE;
import static cn.haowl.hinovel.agent.enums.AgentErrorCodeConstants.AUDIT_CHAPTER_NOT_EXISTS;
import static cn.haowl.hinovel.agent.enums.AgentErrorCodeConstants.AUDIT_CONTENT_EMPTY;
import static cn.haowl.hinovel.agent.enums.AgentErrorCodeConstants.REWRITE_CHAPTER_NOT_EXISTS;
import static cn.haowl.hinovel.common.constant.CommonConstants.ENABLED;
import static cn.haowl.hinovel.common.exception.util.ServiceExceptionUtil.exception;

/**
 * AI 功能应用服务实现类。
 *
 * 封装 AI 改写、审核等业务逻辑，从 Controller 层下沉至此。
 *
 * @author haowl
 * @since 2024
 */
@Slf4j
@Service
public class AIServiceImpl implements AIService {

    /**
     * 作者 Agent。
     */
    @Autowired
    private AuthorAgent authorAgentService;

    /**
     * 编辑 Agent。
     */
    @Autowired
    private EditorAgent editorAgentService;

    /**
     * 章节查询。
     */
    @Autowired
    private ChapterService chapterService;

    /**
     * 章节大纲查询。
     */
    @Autowired
    private ChapterOutlineService chapterOutlineService;

    /**
     * 小说有效配置。
     */
    @Autowired
    private NovelSettingsService novelSettingsService;

    /**
     * 相邻章节查询。
     */
    @Autowired
    private NovelChapterService novelChapterService;

    /**
     * 校验 AI 操作所用小说的归属。
     */
    @Autowired
    private NovelDomainService novelDomainService;

    @Override
    public Flux<String> aiRewrite(AIAuthorRequest request, Long userId) {
        return authorAgentService.streamRewriteChapterByOutline(buildRewriteCommand(request, userId), userId);
    }

    @Override
    public AIAuthorResponse aiRegenerate(AIAuthorRequest request, Long userId) {
        long startTime = System.currentTimeMillis();
        String content = authorAgentService.rewriteChapterByOutline(buildRewriteCommand(request, userId), userId);
        AIAuthorResponse response = new AIAuthorResponse();
        response.setId(UUID.randomUUID().toString());
        response.setContent(content);
        response.setWordCount(content.length());
        response.setProcessingTime(System.currentTimeMillis() - startTime);
        return response;
    }

    /**
     * 加载改写和重新生成共用的章节上下文。
     */
    private ChapterRewriteCommand buildRewriteCommand(AIAuthorRequest request, Long userId) {
        Long chapterId = request.getChapterId();
        if (chapterId == null) {
            throw exception(REWRITE_CHAPTER_NOT_EXISTS);
        }
        NovelChapter chapter = chapterService.getById(chapterId);
        if (chapter == null) {
            log.warn("[buildRewriteCommand][章节不存在，chapterId={}]", chapterId);
            throw exception(REWRITE_CHAPTER_NOT_EXISTS);
        }

        Long novelId = chapter.getNovelId();
        novelDomainService.validateOwnership(novelId, userId);
        ChapterOutline outline = buildRewriteOutline(request, chapterId, novelId);

        NovelSettings settings = novelSettingsService.getEffectiveSettings(novelId, userId);

        Integer contextChapters = settings.getRewriteContextChapters();
        List<NovelChapter> previousChapters = List.of();
        if (contextChapters != null && contextChapters > 0) {
            previousChapters =
                novelChapterService.getPreviousChapters(
                    novelId, chapter.getChapterNumber(), contextChapters);
        }

        String novelOutline =
            Integer.valueOf(ENABLED).equals(settings.getRewriteIncludeOutline())
                ? loadNovelOutlineContent(novelId) : null;

        return ChapterRewriteCommand.builder()
            .chapter(chapter)
            .outline(outline)
            .previousChapters(previousChapters)
            .novelOutlineContent(novelOutline)
            .userRequirement(request.getUserRequirement())
            .build();
    }

    @Override
    public AIAuditorResponse aiAudit(AIAuditorRequest request, Long userId) {
        if (request.getChapterId() == null) {
            throw exception(AUDIT_CHAPTER_NOT_EXISTS);
        }
        NovelChapter chapter = chapterService.getById(request.getChapterId());
        if (chapter == null) {
            throw exception(AUDIT_CHAPTER_NOT_EXISTS);
        }
        novelDomainService.validateOwnership(chapter.getNovelId(), userId);

        // 编辑器传来非空白正文时，审核这份尚未保存的版本。
        if (StringUtils.hasText(request.getContent())) {
            chapter.setContent(request.getContent());
        }
        if (!StringUtils.hasText(chapter.getContent())) {
            throw exception(AUDIT_CONTENT_EMPTY);
        }

        ChapterOutline outline = chapterOutlineService.getByChapterId(request.getChapterId());
        NovelSettings settings =
            novelSettingsService.getEffectiveSettings(
                chapter.getNovelId(), userId);

        // 开启全文大纲后，优先审核编辑器传来的版本，缺省时读取小说已保存的大纲。
        String novelOutline = null;
        if (Integer.valueOf(ENABLED).equals(settings.getAuditIncludeOutline())) {
            novelOutline = StringUtils.hasText(request.getNovelOutline())
                ? request.getNovelOutline() : loadNovelOutlineContent(chapter.getNovelId());
        }

        return editorAgentService.auditChapter(
            ChapterAuditCommand.builder()
                .chapter(chapter)
                .outline(outline)
                .novelOutline(novelOutline)
                .userId(userId)
                .build());
    }

    @Override
    public AIAuditBatchResponse aiBatchAudit(AIAuditBatchRequest request, Long userId) {
        if (request.getChapterId() == null) {
            throw exception(AUDIT_CHAPTER_NOT_EXISTS);
        }
        chapterService.getChapterById(userId, request.getChapterId());
        throw exception(AUDIT_BATCH_UNAVAILABLE);
    }

    /**
     * 合并本次改写使用的章节大纲。
     */
    private ChapterOutline buildRewriteOutline(AIAuthorRequest request, Long chapterId, Long novelId) {
        ChapterOutline outline = chapterOutlineService.getByChapterId(chapterId);
        if (outline == null) {
            outline = new ChapterOutline();
            outline.setChapterId(chapterId);
            outline.setNovelId(novelId);
        }

        // 请求中的非空白大纲字段覆盖已保存的值，省略的字段沿用原值。
        if (StringUtils.hasText(request.getOutline())) {
            outline.setOutlineContent(request.getOutline());
        }
        if (StringUtils.hasText(request.getPlotPoints())) {
            outline.setPlotPoints(request.getPlotPoints());
        }
        if (StringUtils.hasText(request.getEmotionTone())) {
            outline.setEmotionTone(request.getEmotionTone());
        }
        if (StringUtils.hasText(request.getSceneSetting())) {
            outline.setSceneSetting(request.getSceneSetting());
        }

        // 正文允许显式传入空串，只有 null 才沿用原值。
        if (request.getContent() != null) {
            outline.setChapterContent(request.getContent());
        }
        return outline;
    }

    /**
     * 加载小说全文大纲内容。
     *
     * @param novelId 小说 ID
     * @return 全文大纲内容，不存在或为空时返回 null
     */
    private String loadNovelOutlineContent(Long novelId) {
        NovelOutline novelOutline = novelChapterService.getNovelOutline(novelId);
        return novelOutline.isEmpty() ? null : novelOutline.getOutlineContent();
    }
}
