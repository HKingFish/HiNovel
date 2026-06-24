package cn.haowl.hinovel.novel.domain.service;

import cn.haowl.hinovel.common.exception.BusinessException;
import cn.haowl.hinovel.novel.domain.entity.Novel;
import cn.haowl.hinovel.novel.domain.entity.NovelChapter;
import cn.haowl.hinovel.novel.domain.entity.NovelOutline;
import cn.haowl.hinovel.novel.domain.repository.NovelChapterRepository;
import cn.haowl.hinovel.novel.domain.repository.NovelOutlineRepository;
import cn.haowl.hinovel.novel.domain.repository.NovelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static cn.haowl.hinovel.common.exception.enums.GlobalErrorCodeConstants.FORBIDDEN;
import static cn.haowl.hinovel.common.exception.enums.GlobalErrorCodeConstants.NOT_FOUND;
import static cn.haowl.hinovel.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 小说领域服务。
 *
 * <p>封装小说聚合根相关的核心业务逻辑，包括小说的创建、归属校验、
 * 状态变更、统计刷新以及大纲管理。</p>
 *
 * @author haowl
 */
@Service
@RequiredArgsConstructor
public class NovelDomainService {

    private final NovelRepository novelRepository;
    private final NovelChapterRepository novelChapterRepository;
    private final NovelOutlineRepository novelOutlineRepository;

    // ==================== 小说核心操作 ====================

    /**
     * 创建小说（通过充血模型工厂方法）。
     *
     * @param userId      用户 ID
     * @param title       标题
     * @param description 简介
     * @return 持久化后的小说实体
     */
    public Novel createNovel(Long userId, String title, String description) {
        Novel novel = Novel.create(userId, title, description);
        return novelRepository.save(novel);
    }

    /**
     * 验证小说归属并返回实体。
     *
     * @param novelId 小说 ID
     * @param userId  用户 ID
     * @return 小说实体
     * @throws BusinessException 小说不存在或不属于该用户时抛出
     */
    public Novel validateOwnership(Long novelId, Long userId) {
        Novel novel = novelRepository.findById(novelId)
            .orElseThrow(() -> exception(NOT_FOUND));
        if (!novel.belongsTo(userId)) {
            throw exception(FORBIDDEN);
        }
        return novel;
    }

    /**
     * 更新小说基本信息（标题、简介、封面）。
     *
     * <p>通过实体的充血方法完成字段更新，确保业务规则在实体内部校验。</p>
     *
     * @param novel       小说实体（已通过归属校验）
     * @param title       新标题（为 null 时不更新）
     * @param description 新简介（为 null 时不更新）
     * @param coverUrl    新封面（为 null 时不更新）
     * @return 更新后的小说实体
     */
    public Novel updateNovelInfo(Novel novel, String title, String description, String coverUrl) {
        if (title != null) {
            novel.updateTitle(title);
        }
        if (description != null) {
            novel.updateDescription(description);
        }
        if (coverUrl != null) {
            novel.updateCover(coverUrl);
        }
        return novelRepository.save(novel);
    }

    /**
     * 变更小说状态。
     *
     * @param novel  小说实体
     * @param status 目标状态字符串
     * @return 更新后的小说实体
     */
    public Novel changeStatus(Novel novel, String status) {
        switch (Novel.NovelStatus.valueOf(status)) {
            case COMPLETED -> novel.complete();
            case PAUSED -> novel.pause();
            case ONGOING -> novel.resume();
        }
        return novelRepository.save(novel);
    }

    /**
     * 完结小说。
     *
     * @param novelId 小说 ID
     * @param userId  用户 ID
     */
    public void completeNovel(Long novelId, Long userId) {
        Novel novel = validateOwnership(novelId, userId);
        novel.complete();
        novelRepository.save(novel);
    }

    /**
     * 删除小说及其关联章节。
     *
     * @param novelId 小说 ID
     * @param userId  用户 ID
     */
    public void deleteNovel(Long novelId, Long userId) {
        validateOwnership(novelId, userId);
        novelChapterRepository.deleteByNovelId(novelId);
        novelRepository.deleteById(novelId);
    }

    /**
     * 获取用户的小说列表。
     *
     * @param userId 用户 ID
     * @return 小说列表
     */
    public List<Novel> listUserNovels(Long userId) {
        return novelRepository.findByUserId(userId);
    }

    // ==================== 统计刷新 ====================

    /**
     * 刷新小说统计信息（字数、章节数）。
     *
     * <p>从章节仓储重新统计，通过聚合根方法更新冗余字段。</p>
     *
     * @param novelId 小说 ID
     */
    public void refreshNovelStats(Long novelId) {
        Novel novel = novelRepository.findById(novelId)
            .orElseThrow(() -> exception(NOT_FOUND));

        List<NovelChapter> chapters = novelChapterRepository.findByNovelIdWithoutContent(novelId);

        long totalWords = chapters.stream()
                .mapToLong(c -> c.getWordCount() != null ? c.getWordCount() : 0)
                .sum();

        novel.setWordCount(totalWords);
        novel.setChapterCount(chapters.size());
        novelRepository.save(novel);
    }

    // ==================== 大纲管理 ====================

    /**
     * 获取小说大纲（不存在时自动创建空白大纲）。
     *
     * @param novelId 小说 ID
     * @return 大纲实体
     */
    public NovelOutline getOrCreateOutline(Long novelId) {
        return novelOutlineRepository.findByNovelId(novelId)
                .orElseGet(() -> {
                    NovelOutline empty = NovelOutline.createEmpty(novelId);
                    return novelOutlineRepository.save(empty);
                });
    }

    /**
     * 更新大纲内容。
     *
     * @param novelId 小说 ID
     * @param content 新的大纲内容
     * @return 更新后的大纲
     */
    public NovelOutline updateOutline(Long novelId, String content) {
        NovelOutline outline = getOrCreateOutline(novelId);
        outline.updateContent(content);
        return novelOutlineRepository.save(outline);
    }
}
