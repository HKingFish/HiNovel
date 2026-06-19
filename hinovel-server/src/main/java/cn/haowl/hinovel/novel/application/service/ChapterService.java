package cn.haowl.hinovel.novel.application.service;

import cn.haowl.hinovel.common.constant.CommonConstants;
import cn.haowl.hinovel.common.exception.BusinessException;
import cn.haowl.hinovel.common.response.ErrorCode;
import cn.haowl.hinovel.novel.constant.NovelConstants;
import cn.haowl.hinovel.novel.domain.entity.NovelChapter;
import cn.haowl.hinovel.novel.domain.entity.NovelChapterVersion;
import cn.haowl.hinovel.novel.domain.repository.NovelChapterRepository;
import cn.haowl.hinovel.novel.domain.repository.NovelChapterVersionRepository;
import cn.haowl.hinovel.novel.domain.service.ChapterDomainService;
import cn.haowl.hinovel.novel.interfaces.dto.ChapterAutoSaveRequest;
import cn.haowl.hinovel.novel.interfaces.dto.ChapterCreateRequest;
import cn.haowl.hinovel.novel.interfaces.dto.ChapterUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 章节应用服务。
 *
 * <p>协调领域服务完成章节的创建、更新、删除、自动保存及版本管理，
 * 核心业务逻辑委托给 {@link ChapterDomainService}。</p>
 */
@Service
@RequiredArgsConstructor
public class ChapterService {

    private final ChapterDomainService chapterDomainService;
    private final NovelChapterRepository chapterRepository;
    private final NovelChapterVersionRepository versionRepository;
    private final NovelService novelService;
    private final NovelSettingsService novelSettingsService;

    /**
     * 创建章节。
     *
     * @param userId  用户 ID
     * @param request 创建请求
     * @return 创建的章节
     */
    @Transactional(rollbackFor = Exception.class)
    public NovelChapter createChapter(Long userId, ChapterCreateRequest request) {
        // 验证小说权限
        novelService.getNovelById(userId, request.getNovelId());

        // 委托领域服务创建章节
        NovelChapter chapter = chapterDomainService.createChapter(
                request.getNovelId(),
                request.getTitle(),
                request.getContent(),
                request.getSortOrder()
        );

        // 刷新小说统计
        novelService.refreshNovelStats(request.getNovelId());
        return chapter;
    }

    /**
     * 更新章节。
     *
     * @param userId    用户 ID
     * @param chapterId 章节 ID
     * @param request   更新请求
     * @return 更新后的章节
     */
    @Transactional(rollbackFor = Exception.class)
    public NovelChapter updateChapter(Long userId, Long chapterId, ChapterUpdateRequest request) {
        NovelChapter chapter = getChapterById(userId, chapterId);

        // 保存章节时始终创建历史版本（作为内容安全保障）
        boolean createVersion = true;

        // 委托领域服务更新章节
        chapter = chapterDomainService.updateChapter(
                chapter, request.getTitle(), request.getContent(), createVersion);

        // 处理排序号和状态（应用层关注点）
        if (request.getSortOrder() != null) {
            chapter.setSortOrder(request.getSortOrder());
            chapterRepository.save(chapter);
        }
        if (request.getStatus() != null) {
            chapter.setStatus(request.getStatus());
            chapterRepository.save(chapter);
        }

        // 刷新小说统计
        novelService.refreshNovelStats(chapter.getNovelId());
        return chapter;
    }

    /**
     * 自动保存章节（diff 方式）。
     *
     * @param userId  用户 ID
     * @param request 自动保存请求
     * @return 保存后的章节
     */
    @Transactional(rollbackFor = Exception.class)
    public NovelChapter autoSaveChapter(Long userId, ChapterAutoSaveRequest request) {
        NovelChapter chapter = getChapterById(userId, request.getChapterId());

        if (request.getContent() != null) {
            chapter.updateContent(request.getContent());
            if (request.getWordCount() != null) {
                chapter.setWordCount(request.getWordCount());
            }
        }

        chapter = chapterRepository.save(chapter);

        // 自动保存创建版本，保留 diff 信息
        NovelChapterVersion version = NovelChapterVersion.createFromChapter(
                chapter.getId(), chapter.getContent(), null,
                NovelChapterVersion.UNPUBLISHED
        );
        version.setChangeDiff(request.getDiff());
        versionRepository.save(version);

        return chapter;
    }

    /**
     * 删除章节。
     *
     * @param userId    用户 ID
     * @param chapterId 章节 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteChapter(Long userId, Long chapterId) {
        NovelChapter chapter = getChapterById(userId, chapterId);
        Long novelId = chapter.getNovelId();

        chapterDomainService.deleteChapter(chapterId);
        novelService.refreshNovelStats(novelId);
    }

    /**
     * 获取章节详情（验证权限）。
     *
     * @param userId    用户 ID
     * @param chapterId 章节 ID
     * @return 章节实体
     */
    public NovelChapter getChapterById(Long userId, Long chapterId) {
        NovelChapter chapter = chapterDomainService.getChapter(chapterId);

        if (chapter.getDeleted() != null
                && chapter.getDeleted().equals(CommonConstants.DELETED_TRUE)) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        // 验证权限
        novelService.getNovelById(userId, chapter.getNovelId());
        return chapter;
    }

    /**
     * 根据 ID 获取章节（内部使用，不验证权限）。
     *
     * @param chapterId 章节 ID
     * @return 章节实体
     */
    public NovelChapter getById(Long chapterId) {
        return chapterRepository.findById(chapterId).orElse(null);
    }

    /**
     * 获取小说的所有章节。
     *
     * @param userId  用户 ID
     * @param novelId 小说 ID
     * @return 章节列表
     */
    public List<NovelChapter> listChaptersByNovel(Long userId, Long novelId) {
        novelService.getNovelById(userId, novelId);
        return chapterDomainService.listChaptersByNovelId(novelId);
    }

    /**
     * 获取章节版本历史。
     *
     * @param userId    用户 ID
     * @param chapterId 章节 ID
     * @param limit     限制数量
     * @return 版本列表
     */
    public List<NovelChapterVersion> listChapterVersions(Long userId, Long chapterId, Integer limit) {
        getChapterById(userId, chapterId);
        int effectiveLimit = limit != null ? limit : NovelConstants.DEFAULT_VERSION_LIMIT;
        return chapterDomainService.listVersionsWithLimit(chapterId, effectiveLimit);
    }

    /**
     * 恢复到指定版本。
     *
     * @param userId    用户 ID
     * @param chapterId 章节 ID
     * @param versionId 版本 ID
     * @return 恢复后的章节
     */
    @Transactional(rollbackFor = Exception.class)
    public NovelChapter restoreVersion(Long userId, Long chapterId, Long versionId) {
        getChapterById(userId, chapterId);
        return chapterDomainService.restoreVersion(chapterId, versionId);
    }
}
