package cn.haowl.hinovel.novel.domain.service;

import cn.haowl.hinovel.common.exception.BusinessException;
import cn.haowl.hinovel.common.response.ErrorCode;
import cn.haowl.hinovel.novel.domain.entity.NovelChapter;
import cn.haowl.hinovel.novel.domain.entity.NovelChapterVersion;
import cn.haowl.hinovel.novel.domain.repository.NovelChapterRepository;
import cn.haowl.hinovel.novel.domain.repository.NovelChapterVersionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * 章节领域服务。
 *
 * <p>封装章节聚合相关的核心业务逻辑，包括章节的创建、更新、
 * 发布、版本管理以及查询操作。</p>
 *
 * @author haowl
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChapterDomainService {

    private final NovelChapterRepository chapterRepository;
    private final NovelChapterVersionRepository versionRepository;

    /**
     * 版本保留上限。
     */
    private static final int MAX_VERSION_KEEP_COUNT = 50;

    // ==================== 章节核心操作 ====================

    /**
     * 创建草稿章节（通过充血模型工厂方法）。
     *
     * @param novelId   小说 ID
     * @param title     章节标题
     * @param content   初始内容（可为 null）
     * @param sortOrder 排序号（可为 null，自动计算）
     * @return 持久化后的章节实体
     */
    public NovelChapter createChapter(Long novelId, String title, String content, Integer sortOrder) {
        // 自动计算章节号和排序号
        Integer maxChapterNumber = chapterRepository.findMaxChapterNumber(novelId);
        int chapterNumber = maxChapterNumber != null ? maxChapterNumber + 1 : 1;

        if (sortOrder == null) {
            Integer maxSortOrder = chapterRepository.findMaxSortOrder(novelId);
            sortOrder = maxSortOrder != null ? maxSortOrder + 1 : 1;
        }

        String chapterTitle = (title != null && !title.isBlank()) ? title : "未命名章节";
        NovelChapter chapter = NovelChapter.createDraft(novelId, chapterNumber, chapterTitle);
        chapter.setSortOrder(sortOrder);

        // 设置初始内容
        if (content != null && !content.isEmpty()) {
            chapter.updateContent(content);
        }

        chapter = chapterRepository.save(chapter);

        // 创建初始版本
        createVersionRecord(chapter, null, NovelChapterVersion.UNPUBLISHED);

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
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "章节不存在"));
    }

    /**
     * 更新章节内容。
     *
     * <p>通过实体充血方法更新，自动处理已发布状态下的重新发布标记。</p>
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
     * 更新章节内容（可控制是否创建版本）。
     *
     * <p>通过实体充血方法更新，自动处理已发布状态下的重新发布标记。
     * AI 改写过程中可传入 createVersion=false 跳过版本创建，改写完成后再统一创建。</p>
     *
     * @param chapter       章节实体
     * @param title         新标题（为 null 时不更新）
     * @param content       新内容（为 null 时不更新）
     * @param createVersion 是否创建历史版本
     * @return 更新后的章节
     */
    public NovelChapter updateChapter(NovelChapter chapter, String title,
                                      String content, boolean createVersion) {
        boolean contentChanged = false;

        if (title != null) {
            chapter.updateTitle(title);
        }
        if (content != null && !content.equals(chapter.getContent())) {
            chapter.updateContent(content);
            contentChanged = true;
        }

        chapter = chapterRepository.save(chapter);

        // 内容变更且允许创建版本时，保存历史版本
        if (contentChanged && createVersion) {
            createVersionRecord(chapter, null, NovelChapterVersion.UNPUBLISHED);
        }

        return chapter;
    }

    /**
     * 发布章节。
     *
     * <p>通过实体充血方法执行发布，创建带发布标记的版本。</p>
     *
     * @param chapterId 章节 ID
     * @return 发布后的章节
     */
    public NovelChapter publishChapter(Long chapterId) {
        NovelChapter chapter = getChapter(chapterId);
        chapter.publish();
        chapter = chapterRepository.save(chapter);

        // 创建发布版本
        createVersionRecord(chapter, "章节发布", NovelChapterVersion.PUBLISHED);

        return chapter;
    }

    /**
     * 删除章节。
     *
     * @param chapterId 章节 ID
     */
    public void deleteChapter(Long chapterId) {
        getChapter(chapterId);
        chapterRepository.deleteById(chapterId);
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
     * 分页查询章节（支持筛选条件）。
     *
     * @param novelId      小说 ID
     * @param pageNum      页码
     * @param pageSize     每页大小
     * @param status       状态筛选
     * @param vectorStored 入库状态筛选
     * @param title        标题关键词筛选
     * @return 章节列表
     */
    public List<NovelChapter> listChaptersPage(Long novelId, int pageNum, int pageSize,
                                               Integer status, Integer vectorStored, String title) {
        return chapterRepository.findByNovelIdPageWithoutContent(novelId, pageNum, pageSize, status, vectorStored, title);
    }

    /**
     * 统计章节数（支持筛选条件）。
     *
     * @param novelId      小说 ID
     * @param status       状态筛选
     * @param vectorStored 入库状态筛选
     * @param title        标题关键词筛选
     * @return 章节总数
     */
    public int countChapters(Long novelId, Integer status, Integer vectorStored, String title) {
        return chapterRepository.countByNovelIdWithFilter(novelId, status, vectorStored, title);
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
     * <p>校验目标章节号在该小说中不重复，然后更新。</p>
     *
     * @param chapterId     章节 ID
     * @param chapterNumber 新章节号（必须 >= 1）
     * @return 更新后的章节
     */
    public NovelChapter updateChapterNumber(Long chapterId, Integer chapterNumber) {
        if (chapterNumber == null || chapterNumber < 1) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "Chapter number must be >= 1");
        }
        NovelChapter chapter = getChapter(chapterId);
        // 检查同一小说中是否已存在该章节号（排除自身）
        chapterRepository.findByNovelIdAndChapterNumber(chapter.getNovelId(), chapterNumber)
                .ifPresent(existing -> {
                    if (!existing.getId().equals(chapterId)) {
                        throw new BusinessException(ErrorCode.PARAM_ERROR,
                                "Chapter number " + chapterNumber + " already exists");
                    }
                });
        chapter.setChapterNumber(chapterNumber);
        return chapterRepository.save(chapter);
    }

    /**
     * 一键重排章节号。
     *
     * <p>按 sortOrder 升序对该小说所有章节重新连续编号（1, 2, 3...）。</p>
     *
     * @param novelId 小说 ID
     */
    public void reorderChapters(Long novelId) {
        List<NovelChapter> chapters = chapterRepository.findAllByNovelIdOrderBySortOrder(novelId);
        log.info("开始重排小说 {} 的章节号，共 {} 章", novelId, chapters.size());
        for (int i = 0; i < chapters.size(); i++) {
            NovelChapter chapter = chapters.get(i);
            int newNumber = i + 1;
            if (!Integer.valueOf(newNumber).equals(chapter.getChapterNumber())) {
                chapter.setChapterNumber(newNumber);
                chapterRepository.save(chapter);
            }
        }
        log.info("小说 {} 章节号重排完成", novelId);
    }

    /**
     * 批量更新章节排序（拖拽排序场景）。
     *
     * <p>按传入的章节 ID 顺序更新 sortOrder 和 chapterNumber，
     * 使章节号与排序顺序保持一致。</p>
     *
     * @param novelId    小说 ID
     * @param chapterIds 按期望顺序排列的章节 ID 列表
     */
    public void batchUpdateSort(Long novelId, List<Long> chapterIds) {
        if (chapterIds == null || chapterIds.isEmpty()) {
            return;
        }
        log.info("开始批量更新小说 {} 的章节排序，共 {} 章", novelId, chapterIds.size());
        for (int i = 0; i < chapterIds.size(); i++) {
            Long chapterId = chapterIds.get(i);
            int newOrder = i + 1;
            NovelChapter chapter = getChapter(chapterId);
            boolean changed = false;
            if (!Integer.valueOf(newOrder).equals(chapter.getSortOrder())) {
                chapter.setSortOrder(newOrder);
                changed = true;
            }
            if (!Integer.valueOf(newOrder).equals(chapter.getChapterNumber())) {
                chapter.setChapterNumber(newOrder);
                changed = true;
            }
            if (changed) {
                chapterRepository.save(chapter);
            }
        }
        log.info("小说 {} 章节排序更新完成", novelId);
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
     * <p>将章节内容恢复为指定版本的内容，并创建一条新的版本记录。</p>
     *
     * @param chapterId 章节 ID
     * @param versionId 目标版本 ID
     * @return 恢复后的章节
     */
    public NovelChapter restoreVersion(Long chapterId, Long versionId) {
        NovelChapter chapter = getChapter(chapterId);
        NovelChapterVersion targetVersion = versionRepository.findById(versionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "版本不存在"));

        if (!targetVersion.belongsToChapter(chapterId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "版本不属于该章节");
        }

        chapter.updateContent(targetVersion.getContent());
        chapter = chapterRepository.save(chapter);

        // 创建恢复版本记录
        createVersionRecord(chapter, "从版本 [" + versionId + "] 恢复",
                NovelChapterVersion.UNPUBLISHED);

        return chapter;
    }

    // ==================== 内部方法 ====================

    /**
     * 创建版本记录。
     *
     * <p>根据章节当前内容创建版本快照，自动清理超出上限的旧版本。</p>
     *
     * @param chapter   章节实体
     * @param remark    版本备注（可为 null）
     * @param published 是否为发布版本
     * @see NovelChapterVersion#UNPUBLISHED
     * @see NovelChapterVersion#PUBLISHED
     */
    private void createVersionRecord(NovelChapter chapter, String remark, int published) {
        String content = chapter.getContent() != null ? chapter.getContent() : "";
        String contentMd5 = calculateMd5(content);

        // 检查内容是否与最新版本相同，避免重复创建（仅非发布版本去重）
        NovelChapterVersion latestVersion = versionRepository.findLatestByChapterId(chapter.getId());
        if (latestVersion != null && contentMd5.equals(latestVersion.getContentMd5())
                && published == NovelChapterVersion.UNPUBLISHED) {
            return;
        }

        NovelChapterVersion version = NovelChapterVersion.createFromChapter(
                chapter.getId(), content, remark, published
        );
        versionRepository.save(version);

        // 清理超出上限的旧版本
        versionRepository.cleanupOldVersions(chapter.getId(), MAX_VERSION_KEEP_COUNT);
    }

    /**
     * 计算字符串的 MD5 值。
     *
     * @param input 输入字符串
     * @return MD5 十六进制字符串
     */
    private String calculateMd5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return String.format("%032x", new BigInteger(1, digest));
        } catch (NoSuchAlgorithmException e) {
            log.error("MD5 算法不可用，异常信息：{}", e.getMessage());
            throw new IllegalStateException("MD5 算法不可用", e);
        }
    }
}
