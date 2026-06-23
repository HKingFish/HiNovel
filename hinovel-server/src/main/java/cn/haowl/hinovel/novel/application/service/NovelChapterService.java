package cn.haowl.hinovel.novel.application.service;

import cn.haowl.hinovel.common.exception.BusinessException;
import cn.haowl.hinovel.common.exception.enums.GlobalErrorCodeConstants;
import cn.haowl.hinovel.novel.domain.entity.NovelChapter;
import cn.haowl.hinovel.novel.domain.entity.NovelChapterVersion;
import cn.haowl.hinovel.novel.domain.entity.NovelOutline;
import cn.haowl.hinovel.novel.domain.repository.NovelChapterVersionRepository;
import cn.haowl.hinovel.novel.domain.repository.NovelOutlineRepository;
import cn.haowl.hinovel.novel.domain.service.ChapterDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 小说章节应用服务。
 *
 * <p>协调领域服务完成章节的 CRUD、分页查询、版本管理、大纲管理等功能，
 * 核心业务逻辑委托给 {@link ChapterDomainService}。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NovelChapterService {

    private final ChapterDomainService chapterDomainService;
    private final NovelChapterVersionRepository versionRepository;
    private final NovelOutlineRepository outlineRepository;
    private final NovelService novelService;

    /**
     * 获取小说的所有章节。
     *
     * @param novelId 小说 ID
     * @return 章节列表
     */
    public List<NovelChapter> getNovelChapters(Long novelId) {
        return chapterDomainService.listChaptersByNovelId(novelId);
    }

    /**
     * 分页获取小说的章节（按章节号倒序）。
     *
     * @param novelId      小说 ID
     * @param pageNum      页码（从1开始）
     * @param pageSize     每页大小
     * @param status       发布状态筛选（可选）
     * @param vectorStored 入库状态筛选（可选）
     * @param title        标题关键词筛选（可选）
     * @return 章节列表
     */
    public List<NovelChapter> getNovelChaptersPage(Long novelId, int pageNum, int pageSize,
                                                   Integer status, Integer vectorStored, String title) {
        return chapterDomainService.listChaptersPage(novelId, pageNum, pageSize, status, vectorStored, title);
    }

    /**
     * 获取小说的章节总数（支持筛选）。
     *
     * @param novelId      小说 ID
     * @param status       发布状态筛选（可选）
     * @param vectorStored 入库状态筛选（可选）
     * @param title        标题关键词筛选（可选）
     * @return 章节总数
     */
    public int getNovelChapterCount(Long novelId, Integer status, Integer vectorStored, String title) {
        return chapterDomainService.countChapters(novelId, status, vectorStored, title);
    }

    /**
     * 获取章节详情。
     *
     * @param chapterId 章节 ID
     * @return 章节实体
     */
    public NovelChapter getChapter(Long chapterId) {
        return chapterDomainService.getChapter(chapterId);
    }

    /**
     * 创建章节。
     *
     * @param chapter 章节实体（携带 novelId、title、content 等基本信息）
     * @return 创建的章节
     */
    @Transactional(rollbackFor = Exception.class)
    public NovelChapter createChapter(NovelChapter chapter) {
        NovelChapter created = chapterDomainService.createChapter(
                chapter.getNovelId(),
                chapter.getTitle(),
                chapter.getContent(),
                chapter.getSortOrder()
        );
        novelService.refreshNovelStats(chapter.getNovelId());
        return created;
    }

    /**
     * 更新章节。
     *
     * @param chapter 章节实体
     * @return 更新后的章节
     */
    @Transactional(rollbackFor = Exception.class)
    public NovelChapter updateChapter(NovelChapter chapter) {
        NovelChapter existing = chapterDomainService.getChapter(chapter.getId());
        return chapterDomainService.updateChapter(existing, chapter.getTitle(), chapter.getContent());
    }

    /**
     * 删除章节。
     *
     * @param chapterId 章节 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteChapter(Long chapterId) {
        chapterDomainService.deleteChapter(chapterId);
    }

    /**
     * 发布章节。
     *
     * @param chapterId 章节 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void publishChapter(Long chapterId) {
        NovelChapter chapter = chapterDomainService.publishChapter(chapterId);
        novelService.refreshNovelStats(chapter.getNovelId());
    }

    /**
     * 获取章节历史版本。
     *
     * @param chapterId 章节 ID
     * @return 版本列表
     */
    public List<NovelChapterVersion> getChapterVersions(Long chapterId) {
        return chapterDomainService.listVersions(chapterId);
    }

    /**
     * 恢复章节到指定版本。
     *
     * @param chapterId 章节 ID
     * @param versionId 版本 ID
     * @return 恢复后的章节
     */
    @Transactional(rollbackFor = Exception.class)
    public NovelChapter restoreChapterVersion(Long chapterId, Long versionId) {
        return chapterDomainService.restoreVersion(chapterId, versionId);
    }

    /**
     * 获取小说大纲。
     *
     * @param novelId 小说 ID
     * @return 小说大纲
     */
    public NovelOutline getNovelOutline(Long novelId) {
        return outlineRepository.findByNovelId(novelId)
                .orElseGet(() -> {
                    NovelOutline empty = NovelOutline.createEmpty(novelId);
                    empty.setOutlineContent("");
                    return empty;
                });
    }

    /**
     * 保存小说大纲。
     *
     * @param outline 大纲实体
     * @return 保存后的大纲
     */
    @Transactional(rollbackFor = Exception.class)
    public NovelOutline saveNovelOutline(NovelOutline outline) {
        return outlineRepository.findByNovelId(outline.getNovelId())
                .map(existing -> {
                    existing.updateContent(outline.getOutlineContent());
                    return outlineRepository.save(existing);
                })
                .orElseGet(() -> outlineRepository.save(outline));
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
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateVersionRemark(Long chapterId, Long versionId, String remark) {
        chapterDomainService.getChapter(chapterId);

        NovelChapterVersion version = versionRepository.findById(versionId)
            .orElseThrow(() -> new BusinessException(GlobalErrorCodeConstants.RESOURCE_NOT_FOUND, "版本不存在"));

        if (!version.belongsToChapter(chapterId)) {
            throw new BusinessException(GlobalErrorCodeConstants.RESOURCE_NOT_FOUND, "版本不属于该章节");
        }

        version.updateRemark(remark);
        versionRepository.save(version);
    }

    /**
     * 修改章节号。
     *
     * @param chapterId     章节 ID
     * @param chapterNumber 新章节号
     * @return 更新后的章节
     */
    @Transactional(rollbackFor = Exception.class)
    public NovelChapter updateChapterNumber(Long chapterId, Integer chapterNumber) {
        return chapterDomainService.updateChapterNumber(chapterId, chapterNumber);
    }

    /**
     * 一键重排章节号，使章节号连续。
     *
     * @param novelId 小说 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void reorderChapters(Long novelId) {
        chapterDomainService.reorderChapters(novelId);
    }

    /**
     * 批量更新章节排序（拖拽排序场景）。
     *
     * <p>按传入的章节 ID 顺序更新 sortOrder，同时同步更新 chapterNumber。</p>
     *
     * @param novelId    小说 ID
     * @param chapterIds 按期望顺序排列的章节 ID 列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateSort(Long novelId, List<Long> chapterIds) {
        chapterDomainService.batchUpdateSort(novelId, chapterIds);
    }
}
