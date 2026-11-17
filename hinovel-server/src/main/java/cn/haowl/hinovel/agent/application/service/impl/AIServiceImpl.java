package cn.haowl.hinovel.agent.application.service.impl;

import cn.haowl.hinovel.agent.application.service.AIService;
import cn.haowl.hinovel.agent.application.service.agent.AuthorAgent;
import cn.haowl.hinovel.agent.application.service.agent.EditorAgent;
import cn.haowl.hinovel.agent.constant.AgentConstants;
import cn.haowl.hinovel.agent.constant.AuditAssessmentEnum;
import cn.haowl.hinovel.agent.interfaces.dto.*;
import cn.haowl.hinovel.novel.application.service.ChapterOutlineService;
import cn.haowl.hinovel.novel.application.service.ChapterService;
import cn.haowl.hinovel.novel.application.service.NovelChapterService;
import cn.haowl.hinovel.novel.application.service.NovelSettingsService;
import cn.haowl.hinovel.novel.domain.entity.ChapterOutline;
import cn.haowl.hinovel.novel.domain.entity.NovelChapter;
import cn.haowl.hinovel.novel.domain.entity.NovelOutline;
import cn.haowl.hinovel.novel.domain.entity.NovelSettings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.util.List;

import static cn.haowl.hinovel.agent.enums.AgentErrorCodeConstants.AUDIT_CHAPTER_NOT_EXISTS;
import static cn.haowl.hinovel.agent.enums.AgentErrorCodeConstants.REWRITE_CHAPTER_NOT_EXISTS;
import static cn.haowl.hinovel.common.constant.CommonConstants.ENABLED;
import static cn.haowl.hinovel.common.exception.util.ServiceExceptionUtil.exception;
import static cn.haowl.hinovel.common.exception.util.ServiceExceptionUtil.exception0;

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
     * 章节不存在的统一提示。
     */
    private static final String CHAPTER_NOT_EXISTS_MESSAGE = "章节不存在";

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

    @Override
    public Flux<String> aiRewrite(AIAuthorRequest request, Long userId) {
        Long chapterId = request.getChapterId();
        NovelChapter chapter = chapterService.getById(chapterId);
        if (chapter == null) {
            log.warn("[aiRewrite][章节不存在，chapterId={}]", chapterId);
            throw exception(REWRITE_CHAPTER_NOT_EXISTS);
        }

        Long novelId = chapter.getNovelId();
        ChapterOutline outline = buildRewriteOutline(request, chapterId, novelId);

        // 改写的上下文范围使用章节创建者的小说配置。
        NovelSettings settings =
            novelSettingsService.getEffectiveSettings(
                novelId, chapter.getCreator());

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

        return authorAgentService.streamRewriteChapterByOutline(
            ChapterRewriteCommand.builder()
                .chapter(chapter)
                .outline(outline)
                .previousChapters(previousChapters)
                .novelOutlineContent(novelOutline)
                .userRequirement(request.getUserRequirement())
                .build(),
            userId);
    }

    @Override
    public AIAuditorResponse aiAudit(AIAuditorRequest request, Long userId) {
        NovelChapter chapter = chapterService.getById(request.getChapterId());
        if (chapter == null) {
            throw exception(AUDIT_CHAPTER_NOT_EXISTS);
        }

        // 编辑器传来非空白正文时，审核这份尚未保存的版本。
        if (StringUtils.hasText(request.getContent())) {
            chapter.setContent(request.getContent());
        }

        ChapterOutline outline = chapterOutlineService.getByChapterId(request.getChapterId());
        NovelSettings settings =
            novelSettingsService.getEffectiveSettings(
                chapter.getNovelId(), chapter.getCreator());

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
    public AIAuditBatchResponse aiBatchAudit(AIAuditBatchRequest request) {
        if (request.getChapterId() == null) {
            throw exception0(AUDIT_CHAPTER_NOT_EXISTS.getCode(), CHAPTER_NOT_EXISTS_MESSAGE);
        }

        AIAuditBatchResponse response = new AIAuditBatchResponse();
        response.setId(AgentConstants.MOCK_BATCH_AUDIT_ID_PREFIX + AgentConstants.MOCK_RESPONSE_ID_START);
        response.setOverallAssessment(AuditAssessmentEnum.PASS);
        response.setSummary(AgentConstants.MOCK_BATCH_AUDIT_SUMMARY);
        response.setAudits(List.of());
        return response;
    }

    @Override
    public AIAuthorResponse aiRegenerate(AIAuthorRequest request) {
        if (request.getChapterId() == null) {
            throw exception0(REWRITE_CHAPTER_NOT_EXISTS.getCode(), CHAPTER_NOT_EXISTS_MESSAGE);
        }

        AIAuthorResponse response = new AIAuthorResponse();
        response.setId(AgentConstants.MOCK_REGENERATE_ID_PREFIX + AgentConstants.MOCK_RESPONSE_ID_START);
        response.setContent(AgentConstants.MOCK_REGENERATE_CONTENT);
        response.setWordCount(response.getContent().length());
        response.setProcessingTime(AgentConstants.MOCK_REGENERATE_PROCESSING_TIME_MS);
        return response;
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
        if (novelOutline == null || novelOutline.getOutlineContent() == null
            || novelOutline.getOutlineContent().isBlank()) {
            return null;
        }
        return novelOutline.getOutlineContent();
    }
}
