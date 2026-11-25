package cn.haowl.hinovel.novel.application.service;

import cn.haowl.hinovel.novel.constant.NovelConstants;
import cn.haowl.hinovel.novel.domain.command.CreateChapterCommand;
import cn.haowl.hinovel.novel.domain.entity.NovelChapter;
import cn.haowl.hinovel.novel.domain.entity.NovelChapterVersion;
import cn.haowl.hinovel.novel.domain.repository.NovelChapterRepository;
import cn.haowl.hinovel.novel.domain.service.ChapterDomainService;
import cn.haowl.hinovel.novel.domain.service.NovelDomainService;
import cn.haowl.hinovel.novel.interfaces.dto.ChapterAutoSaveRequest;
import cn.haowl.hinovel.novel.interfaces.dto.ChapterUpdateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 章节应用服务。
 *
 * 协调领域服务完成章节的创建、更新、删除、自动保存及版本管理，
 * 核心业务逻辑委托给 {@link ChapterDomainService}。
 */
@Service
public class ChapterService {

    /**
     * 章节领域服务。
     */
    @Autowired
    private ChapterDomainService chapterDomainService;

    /**
     * 章节仓储。
     */
    @Autowired
    private NovelChapterRepository chapterRepository;

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
     * 创建章节。
     *
     * @param userId  用户 ID
     * @param command 创建参数
     * @return 创建的章节
     */
    @Transactional(rollbackFor = Exception.class)
    public NovelChapter createChapter(Long userId, CreateChapterCommand command) {
        novelDomainService.validateOwnership(command.getNovelId(), userId);
        NovelChapter chapter = chapterDomainService.createChapter(command);
        novelService.refreshNovelStats(chapter.getNovelId());
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

        chapter = chapterDomainService.updateChapter(
            chapter, request.getTitle(), request.getContent(), !Boolean.FALSE.equals(request.getCreateVersion()));

        if (chapter.updateMetadata(request.getSortOrder(), request.getStatus())) {
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
            if (!request.getContent().equals(chapter.getContent())) {
                chapter.updateContent(request.getContent());
            }
            if (request.getWordCount() != null) {
                chapter.setWordCount(request.getWordCount());
            }
        }

        chapter = chapterDomainService.saveChapterWithDiff(chapter, request.getDiff());
        novelService.refreshNovelStats(chapter.getNovelId());
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
        chapterDomainService.deleteChapter(chapter);
        novelService.refreshNovelStats(chapter.getNovelId());
    }

    /**
     * 获取章节详情（验证权限）。
     *
     * @param userId    用户 ID
     * @param chapterId 章节 ID
     * @return 章节实体
     */
    public NovelChapter getChapterById(Long userId, Long chapterId) {
        return chapterDomainService.getOwnedChapter(chapterId, userId);
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
        novelDomainService.validateOwnership(novelId, userId);
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
        NovelChapter chapter = chapterDomainService.restoreVersion(getChapterById(userId, chapterId), versionId);
        novelService.refreshNovelStats(chapter.getNovelId());
        return chapter;
    }
}
