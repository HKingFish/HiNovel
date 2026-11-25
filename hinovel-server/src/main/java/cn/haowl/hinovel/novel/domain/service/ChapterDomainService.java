package cn.haowl.hinovel.novel.domain.service;

import cn.haowl.hinovel.common.exception.BusinessException;
import cn.haowl.hinovel.novel.constant.NovelConstants;
import cn.haowl.hinovel.novel.domain.command.CreateChapterCommand;
import cn.haowl.hinovel.novel.domain.entity.NovelChapter;
import cn.haowl.hinovel.novel.domain.entity.NovelChapterVersion;
import cn.haowl.hinovel.novel.domain.query.ChapterPageQuery;
import cn.haowl.hinovel.novel.domain.repository.NovelChapterRepository;
import cn.haowl.hinovel.novel.domain.repository.NovelChapterVersionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static cn.haowl.hinovel.common.exception.enums.GlobalErrorCodeConstants.FORBIDDEN;
import static cn.haowl.hinovel.common.exception.enums.GlobalErrorCodeConstants.NOT_FOUND;
import static cn.haowl.hinovel.common.exception.enums.GlobalErrorCodeConstants.PARAM_INVALID;
import static cn.haowl.hinovel.common.exception.util.ServiceExceptionUtil.exception;
import static cn.haowl.hinovel.novel.enums.NovelErrorCodeConstants.CHAPTER_NUMBER_EXISTS;
import static cn.haowl.hinovel.novel.enums.NovelErrorCodeConstants.VERSION_NOT_BELONG_TO_CHAPTER;

/**
 * 章节领域服务。
 *
 * 封装章节聚合相关的核心业务逻辑，包括章节的创建、更新、
 * 发布、版本管理以及查询操作。
 *
 * @author haowl
 */
@Slf4j
@Service
public class ChapterDomainService {

    /**
     * 章节仓储。
     */
    @Autowired
    private NovelChapterRepository chapterRepository;

    /**
     * 章节版本仓储。
     */
    @Autowired
    private NovelChapterVersionRepository versionRepository;

    /**
     * 小说归属校验。
     */
    @Autowired
    private NovelDomainService novelDomainService;

    // ==================== 章节核心操作 ====================

    /**
     * 创建草稿章节（通过充血模型工厂方法）。
     *
     * @param command 章节创建参数
     * @return 持久化后的章节实体
     */
    public NovelChapter createChapter(CreateChapterCommand command) {
        Long novelId = command.getNovelId();
        Integer sortOrder = command.getSortOrder();
        // 自动计算章节号和排序号
        Integer maxChapterNumber = chapterRepository.findMaxChapterNumber(novelId);
        int chapterNumber = maxChapterNumber != null ? maxChapterNumber + 1 : 1;

        if (sortOrder == null) {
            Integer maxSortOrder = chapterRepository.findMaxSortOrder(novelId);
            sortOrder = maxSortOrder != null ? maxSortOrder + 1 : 1;
        }

        String title = command.getTitle();
        String chapterTitle = title != null && !title.isBlank() ? title : NovelConstants.DEFAULT_CHAPTER_TITLE;
        NovelChapter chapter = NovelChapter.createDraft(novelId, chapterNumber, chapterTitle);
        chapter.setSortOrder(sortOrder);

        // 正文为空时只保存草稿基础信息
        String content = command.getContent();
        if (content != null && !content.isEmpty()) {
            chapter.updateContent(content);
        }

        chapter = chapterRepository.save(chapter);

        // 创建初始版本
        createVersionRecord(chapter, null, NovelChapterVersion.UNPUBLISHED, null);

        return chapter;
    }

    /**
     * 获取章节（带存在性校验）。
     *
     * @param chapterId 章节 ID
     * @return 章节实体
     * @throws BusinessException 章节不存在时抛出
     */
    public NovelChapter getChapter(Long chapterId) {
        return chapterRepository.findById(chapterId)
            .orElseThrow(() -> exception(NOT_FOUND, "章节不存在"));
    }

    /**
     * 获取当前用户有权操作的章节。
     *
     * @param chapterId 章节 ID
     * @param userId 操作者 ID
     * @return 已校验小说归属的章节
     * @throws BusinessException 章节或小说不存在，或小说不属于该用户
     */
    public NovelChapter getOwnedChapter(Long chapterId, Long userId) {
        NovelChapter chapter = getChapter(chapterId);
        novelDomainService.validateOwnership(chapter.getNovelId(), userId);
        return chapter;
    }

    /**
     * 获取指定小说中的章节。
     *
     * @param novelId 目标小说 ID
     * @param chapterId 章节 ID
     * @return 属于目标小说的章节
     * @throws BusinessException 章节不存在或不属于目标小说
     */
    public NovelChapter getChapterInNovel(Long novelId, Long chapterId) {
        NovelChapter chapter = getChapter(chapterId);
        if (!chapter.belongsToNovel(novelId)) {
            throw exception(FORBIDDEN);
        }
        return chapter;
    }

    /**
     * 更新章节内容。
     *
     * 通过实体充血方法更新，自动处理已发布状态下的重新发布标记。
     *
     * @param chapter      章节实体
     * @param title        新标题（为 null 时不更新）
     * @param content      新内容（为 null 时不更新）
     * @return 更新后的章节
     */
    public NovelChapter updateChapter(NovelChapter chapter, String title, String content) {
        return updateChapter(chapter, title, content, true);
    }

    /**
     * 更新章节，可由调用方控制是否保留正文快照。
     */
    public NovelChapter updateChapter(NovelChapter chapter, String title, String content, boolean createVersion) {
        boolean contentChanged = content != null && !content.equals(chapter.getContent());

        if (title != null) {
            chapter.updateTitle(title);
        }
        if (contentChanged) {
            chapter.updateContent(content);
        }

        chapter = chapterRepository.save(chapter);

        if (createVersion && contentChanged) {
            createVersionRecord(chapter, null, NovelChapterVersion.UNPUBLISHED, null);
        }

        return chapter;
    }

    /**
     * 保存自动保存后的章节与差异快照。
     */
    public NovelChapter saveChapterWithDiff(NovelChapter chapter, String diff) {
        chapter = chapterRepository.save(chapter);
        createVersionRecord(chapter, null, NovelChapterVersion.UNPUBLISHED, diff);
        return chapter;
    }

    /**
     * 发布章节。
     *
     * 通过实体充血方法执行发布，创建带发布标记的版本。
     *
     * @param chapter 已校验归属的章节
     * @return 发布后的章节
     */
    public NovelChapter publishChapter(NovelChapter chapter) {
        chapter.publish();
        chapter = chapterRepository.save(chapter);

        // 创建发布版本
        createVersionRecord(chapter, "章节发布", NovelChapterVersion.PUBLISHED, null);

        return chapter;
    }

    /**
     * 删除章节。
     *
     * @param chapter 已校验归属的章节
     */
    public void deleteChapter(NovelChapter chapter) {
        chapterRepository.deleteById(chapter.getId());
    }

    // ==================== 查询操作 ====================

    /**
     * 获取小说的所有章节。
     *
     * @param novelId 小说 ID
     * @return 章节列表
     */
    public List<NovelChapter> listChaptersByNovelId(Long novelId) {
        return chapterRepository.findByNovelIdWithoutContent(novelId);
    }

    /**
     * 按条件分页查询章节。
     */
    public List<NovelChapter> listChaptersPage(ChapterPageQuery query) {
        return chapterRepository.findByNovelIdPageWithoutContent(query);
    }

    /**
     * 按同一分页筛选条件统计章节总数。
     */
    public int countChapters(ChapterPageQuery query) {
        return chapterRepository.countByNovelIdWithFilter(query);
    }

    /**
     * 查询指定章节之前的若干章。
     *
     * @param novelId       小说 ID
     * @param chapterNumber 当前章节号
     * @param limit         最多获取几章
     * @return 前几章列表
     */
    public List<NovelChapter> getPreviousChapters(Long novelId, Integer chapterNumber, int limit) {
        if (chapterNumber == null || chapterNumber <= 1 || limit <= 0) {
            return List.of();
        }
        return chapterRepository.findPreviousChapters(novelId, chapterNumber, limit);
    }

    /**
     * 修改章节号。
     *
     * 校验目标章节号在该小说中不重复，然后更新。
     *
     * @param chapter       已校验归属的章节
     * @param chapterNumber 新章节号（必须 >= 1）
     * @return 更新后的章节
     */
    public NovelChapter updateChapterNumber(NovelChapter chapter, Integer chapterNumber) {
        if (chapterNumber == null || chapterNumber < 1) {
            throw exception(PARAM_INVALID, "章节号");
        }
        // 检查同一小说中是否已存在该章节号（排除自身）
        chapterRepository.findByNovelIdAndChapterNumber(chapter.getNovelId(), chapterNumber)
            .filter(existing -> !chapter.getId().equals(existing.getId()))
            .ifPresent(existing -> {
                throw exception(CHAPTER_NUMBER_EXISTS);
            });
        chapter.setChapterNumber(chapterNumber);
        return chapterRepository.save(chapter);
    }

    /**
     * 一键重排章节号。
     *
     * 按 sortOrder 升序对该小说所有章节重新连续编号（1, 2, 3...）。
     *
     * @param novelId 小说 ID
     */
    public void reorderChapters(Long novelId) {
        List<NovelChapter> chapters = chapterRepository.findAllByNovelIdOrderBySortOrder(novelId);
        log.info("[reorderChapters][开始重排章节号，novelId={}，chapterCount={}]", novelId, chapters.size());
        for (int i = 0; i < chapters.size(); i++) {
            NovelChapter chapter = chapters.get(i);
            int newNumber = i + 1;
            if (!Integer.valueOf(newNumber).equals(chapter.getChapterNumber())) {
                chapter.setChapterNumber(newNumber);
                chapterRepository.save(chapter);
            }
        }
        log.info("[reorderChapters][章节号重排完成，novelId={}]", novelId);
    }

    /**
     * 批量更新章节排序（拖拽排序场景）。
     *
     * 按列表展示顺序重新分配所选章节占用的排序号与章节号。
     *
     * @param novelId    小说 ID
     * @param chapterIds 按期望顺序排列的章节 ID 列表
     * @throws BusinessException 任一章节不存在或不属于目标小说
     */
    public void batchUpdateSort(Long novelId, List<Long> chapterIds) {
        if (chapterIds == null || chapterIds.isEmpty()) {
            return;
        }
        Set<Long> uniqueIds = new HashSet<>(chapterIds);
        if (uniqueIds.contains(null) || uniqueIds.size() != chapterIds.size()) {
            throw exception(PARAM_INVALID, "章节 ID 列表");
        }
        List<NovelChapter> chapters = chapterIds.stream()
            .map(chapterId -> getChapterInNovel(novelId, chapterId))
            .toList();

        // 分页或筛选结果只交换当前列表占用的位置。
        List<NovelChapter> originalOrder = chapters.stream()
            .sorted(Comparator.comparing(NovelChapter::getSortOrder)
                .thenComparing(NovelChapter::getChapterNumber)
                .thenComparing(NovelChapter::getId)
                .reversed())
            .toList();
        List<Integer> sortOrders = originalOrder.stream().map(NovelChapter::getSortOrder).toList();
        List<Integer> chapterNumbers = originalOrder.stream().map(NovelChapter::getChapterNumber).toList();
        for (int i = 0; i < chapters.size(); i++) {
            NovelChapter chapter = chapters.get(i);
            if (chapter.applySortOrder(sortOrders.get(i), chapterNumbers.get(i))) {
                chapterRepository.save(chapter);
            }
        }
        log.info("[batchUpdateSort][章节排序更新完成，novelId={}，chapterCount={}]", novelId, chapters.size());
    }

    // ==================== 版本管理 ====================

    /**
     * 获取章节版本历史。
     *
     * @param chapterId 章节 ID
     * @return 版本列表
     */
    public List<NovelChapterVersion> listVersions(Long chapterId) {
        return versionRepository.findByChapterId(chapterId);
    }

    /**
     * 获取章节版本历史（带数量限制）。
     *
     * @param chapterId 章节 ID
     * @param limit     限制数量
     * @return 版本列表
     */
    public List<NovelChapterVersion> listVersionsWithLimit(Long chapterId, int limit) {
        return versionRepository.findByChapterIdWithLimit(chapterId, limit);
    }

    /**
     * 恢复到指定版本。
     *
     * 将章节内容恢复为指定版本的内容，并创建一条新的版本记录。
     *
     * @param chapter   已校验归属的章节
     * @param versionId 目标版本 ID
     * @return 恢复后的章节
     */
    public NovelChapter restoreVersion(NovelChapter chapter, Long versionId) {
        NovelChapterVersion targetVersion = getChapterVersion(chapter.getId(), versionId);

        chapter.updateContent(targetVersion.getContent());
        chapter = chapterRepository.save(chapter);

        // 创建恢复版本记录
        createVersionRecord(chapter, "从版本 [" + versionId + "] 恢复", NovelChapterVersion.UNPUBLISHED, null);

        return chapter;
    }

    /**
     * 修改指定章节的版本备注。
     *
     * @param chapterId 章节 ID
     * @param versionId 版本 ID
     * @param remark 新备注
     * @throws BusinessException 版本不存在或不属于该章节
     */
    public void updateVersionRemark(Long chapterId, Long versionId, String remark) {
        NovelChapterVersion version = getChapterVersion(chapterId, versionId);
        version.updateRemark(remark);
        versionRepository.save(version);
    }

    // ==================== 内部方法 ====================

    /**
     * 查询版本并校验章节归属。
     *
     * @param chapterId 章节 ID
     * @param versionId 版本 ID
     * @return 属于指定章节的版本
     * @throws BusinessException 版本不存在或不属于该章节
     */
    private NovelChapterVersion getChapterVersion(Long chapterId, Long versionId) {
        NovelChapterVersion version = versionRepository.findById(versionId)
            .orElseThrow(() -> exception(NOT_FOUND));
        if (!version.belongsToChapter(chapterId)) {
            throw exception(VERSION_NOT_BELONG_TO_CHAPTER);
        }
        return version;
    }

    /**
     * 创建版本记录。
     *
     * 根据章节当前内容创建版本快照，自动清理超出上限的旧版本。
     *
     * @param chapter   章节实体
     * @param remark    版本备注（可为 null）
     * @param published 是否为发布版本
     * @param diff 自动保存传入的正文差异
     * @see NovelChapterVersion#UNPUBLISHED
     * @see NovelChapterVersion#PUBLISHED
     */
    private void createVersionRecord(NovelChapter chapter, String remark, int published, String diff) {
        NovelChapterVersion version =
            NovelChapterVersion.createFromChapter(chapter.getId(), chapter.getContent(), remark, published);
        if (NovelChapterVersion.UNPUBLISHED == published && diff == null && remark == null) {
            NovelChapterVersion latestVersion = versionRepository.findLatestByChapterId(chapter.getId());
            if (latestVersion != null && latestVersion.contentEquals(version.getContentMd5())) {
                return;
            }
        }
        version.setChangeDiff(diff);
        versionRepository.save(version);

        // 清理超出上限的旧版本
        versionRepository.cleanupOldVersions(chapter.getId(), NovelConstants.MAX_CHAPTER_VERSION_KEEP_COUNT);
    }
}
