package cn.haowl.hinovel.novel.application.service;

import cn.haowl.hinovel.common.exception.enums.GlobalErrorCodeConstants;
import cn.haowl.hinovel.novel.domain.entity.ChapterOutline;
import cn.haowl.hinovel.novel.domain.entity.NovelChapter;
import cn.haowl.hinovel.novel.domain.entity.NovelChapterVersion;
import cn.haowl.hinovel.novel.domain.entity.NovelOutline;
import cn.haowl.hinovel.novel.domain.query.ChapterPageQuery;
import cn.haowl.hinovel.novel.domain.repository.NovelOutlineRepository;
import cn.haowl.hinovel.novel.domain.service.ChapterDomainService;
import cn.haowl.hinovel.novel.domain.service.NovelDomainService;
import cn.haowl.hinovel.novel.interfaces.dto.AiOutlineResponse;
import cn.haowl.hinovel.novel.interfaces.dto.ChapterPageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static cn.haowl.hinovel.common.exception.util.ServiceExceptionUtil.exception0;

/**
 * 小说章节应用服务。
 *
 * 协调领域服务完成章节的 CRUD、分页查询、版本管理、大纲管理等功能，
 * 核心业务逻辑委托给 {@link ChapterDomainService}。
 */
@Service
public class NovelChapterService {

    /**
     * 章节领域服务。
     */
    @Autowired
    private ChapterDomainService chapterDomainService;

    /**
     * 小说大纲仓储。
     */
    @Autowired
    private NovelOutlineRepository outlineRepository;

    /**
     * 小说应用服务。
     */
    @Autowired
    private NovelService novelService;

    /**
     * 小说归属校验。
     */
    @Autowired
    private NovelDomainService novelDomainService;

    /**
     * 章节大纲应用服务。
     */
    @Autowired
    private ChapterOutlineService chapterOutlineService;

    /**
     * 按排序号倒序查询章节。
     *
     * @param query 分页与筛选条件
     * @param userId 操作者 ID
     * @return 章节分页响应
     */
    public ChapterPageResponse getNovelChaptersPage(ChapterPageQuery query, Long userId) {
        novelDomainService.validateOwnership(query.getNovelId(), userId);

        List<NovelChapter> chapters = chapterDomainService.listChaptersPage(query);
        int total = chapterDomainService.countChapters(query);

        ChapterPageResponse response = new ChapterPageResponse();
        response.setList(chapters);
        response.setTotal(total);
        response.setPageNum(query.getPageNum());
        response.setPageSize(query.getPageSize());
        response.setHasMore((long) query.getPageNum() * query.getPageSize() < total);
        return response;
    }

    /**
     * 获取当前用户有权查看的小说大纲。
     *
     * @param novelId 小说 ID
     * @param userId 操作者 ID
     * @return 小说大纲，尚未保存时返回空白大纲
     */
    public NovelOutline getNovelOutline(Long novelId, Long userId) {
        novelDomainService.validateOwnership(novelId, userId);
        return getNovelOutline(novelId);
    }

    /**
     * 获取小说大纲，缺失时返回空白大纲。
     *
     * @param novelId 小说 ID
     * @return 小说大纲，尚未保存时返回空白大纲
     */
    public NovelOutline getNovelOutline(Long novelId) {
        return outlineRepository.findByNovelId(novelId)
            .orElseGet(() -> NovelOutline.createEmpty(novelId));
    }

    /**
     * 获取章节的 AI 大纲。
     *
     * @param chapterId 章节 ID
     * @param userId 操作者 ID
     * @return AI 大纲响应，无 AI 大纲时返回空内容
     */
    public AiOutlineResponse getAiOutline(Long chapterId, Long userId) {
        chapterDomainService.getOwnedChapter(chapterId, userId);
        ChapterOutline outline = chapterOutlineService.getByChapterId(chapterId);
        AiOutlineResponse response = new AiOutlineResponse();
        if (outline != null && outline.hasAiOutlineContent()) {
            response.setAiOutlineContent(outline.getAiOutlineContent());
            response.setHasAiOutline(true);
        } else {
            response.setAiOutlineContent("");
            response.setHasAiOutline(false);
        }
        return response;
    }

    /**
     * 用 AI 大纲替换用户大纲。
     *
     * @param chapterId 章节 ID
     * @param userId 操作者 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void replaceOutlineWithAi(Long chapterId, Long userId) {
        NovelChapter chapter = chapterDomainService.getOwnedChapter(chapterId, userId);
        ChapterOutline outline = chapterOutlineService.getByChapterId(chapterId);
        if (outline == null || !outline.hasAiOutlineContent()) {
            throw exception0(GlobalErrorCodeConstants.PARAM_ERROR.getCode(), "暂无 AI 生成的大纲可替换");
        }
        chapterOutlineService.saveUserOutline(chapter, outline.getAiOutlineContent());
    }

    /**
     * 更新章节。
     *
     * @param userId 当前用户 ID
     * @param chapterId 章节 ID
     * @param changes 标题与正文
     * @return 更新后的章节
     */
    @Transactional(rollbackFor = Exception.class)
    public NovelChapter updateChapter(Long userId, Long chapterId, NovelChapter changes) {
        NovelChapter chapter = chapterDomainService.getOwnedChapter(chapterId, userId);
        chapter = chapterDomainService.updateChapter(chapter, changes.getTitle(), changes.getContent());
        novelService.refreshNovelStats(chapter.getNovelId());
        return chapter;
    }

    /**
     * 更新指定小说内的章节内容并保留历史版本。
     *
     * @param novelId 目标小说 ID
     * @param chapterId 章节 ID
     * @param userId 操作者 ID
     * @param content 新正文，为 null 时保留原正文
     * @return 更新后的章节
     */
    @Transactional(rollbackFor = Exception.class)
    public NovelChapter updateChapterContent(Long novelId, Long chapterId, Long userId, String content) {
        novelDomainService.validateOwnership(novelId, userId);
        NovelChapter chapter = chapterDomainService.getChapterInNovel(novelId, chapterId);
        chapter = chapterDomainService.updateChapter(chapter, null, content);
        novelService.refreshNovelStats(novelId);
        return chapter;
    }

    /**
     * 发布章节。
     *
     * @param chapterId 章节 ID
     * @param userId 操作者 ID
     * @return 已发布的章节
     */
    @Transactional(rollbackFor = Exception.class)
    public NovelChapter publishChapter(Long chapterId, Long userId) {
        NovelChapter chapter =
            chapterDomainService.publishChapter(chapterDomainService.getOwnedChapter(chapterId, userId));
        novelService.refreshNovelStats(chapter.getNovelId());
        return chapter;
    }

    /**
     * 获取章节历史版本。
     *
     * @param chapterId 章节 ID
     * @param userId 操作者 ID
     * @return 版本列表
     */
    public List<NovelChapterVersion> getChapterVersions(Long chapterId, Long userId) {
        chapterDomainService.getOwnedChapter(chapterId, userId);
        return chapterDomainService.listVersions(chapterId);
    }

    /**
     * 保存小说大纲。
     *
     * @param novelId 小说 ID
     * @param content 大纲内容
     * @param userId 当前用户 ID
     * @return 保存后的大纲
     */
    @Transactional(rollbackFor = Exception.class)
    public NovelOutline saveNovelOutline(Long novelId, String content, Long userId) {
        novelDomainService.validateOwnership(novelId, userId);
        return novelDomainService.updateOutline(novelId, content);
    }

    /**
     * 获取指定章节之前的若干章内容（用于前情提要）。
     *
     * @param novelId       小说 ID
     * @param chapterNumber 当前章节号（不包含自身）
     * @param limit         最多获取几章
     * @return 前几章列表（按章节号正序排列）
     */
    public List<NovelChapter> getPreviousChapters(Long novelId, Integer chapterNumber, int limit) {
        return chapterDomainService.getPreviousChapters(novelId, chapterNumber, limit);
    }

    /**
     * 更新版本备注。
     *
     * @param chapterId 章节 ID
     * @param versionId 版本 ID
     * @param remark    备注内容
     * @param userId 操作者 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateVersionRemark(Long chapterId, Long versionId, String remark, Long userId) {
        chapterDomainService.getOwnedChapter(chapterId, userId);
        chapterDomainService.updateVersionRemark(chapterId, versionId, remark);
    }

    /**
     * 修改章节号。
     *
     * @param chapterId     章节 ID
     * @param chapterNumber 新章节号
     * @param userId 操作者 ID
     * @return 更新后的章节
     */
    @Transactional(rollbackFor = Exception.class)
    public NovelChapter updateChapterNumber(Long chapterId, Integer chapterNumber, Long userId) {
        NovelChapter chapter = chapterDomainService.getOwnedChapter(chapterId, userId);
        return chapterDomainService.updateChapterNumber(chapter, chapterNumber);
    }

    /**
     * 一键重排章节号，使章节号连续。
     *
     * @param novelId 小说 ID
     * @param userId 操作者 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void reorderChapters(Long novelId, Long userId) {
        novelDomainService.validateOwnership(novelId, userId);
        chapterDomainService.reorderChapters(novelId);
    }

    /**
     * 批量更新章节排序（拖拽排序场景）。
     *
     * 按传入顺序更新排序号和章节号，任一章节归属校验失败时整批回滚。
     *
     * @param novelId    小说 ID
     * @param chapterIds 按期望顺序排列的章节 ID 列表
     * @param userId 操作者 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateSort(Long novelId, List<Long> chapterIds, Long userId) {
        novelDomainService.validateOwnership(novelId, userId);
        chapterDomainService.batchUpdateSort(novelId, chapterIds);
    }
}
