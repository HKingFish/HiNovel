package cn.haowl.hinovel.novel.application.service;

import cn.haowl.hinovel.novel.domain.entity.ChapterOutline;
import cn.haowl.hinovel.novel.domain.entity.NovelChapter;
import cn.haowl.hinovel.novel.domain.repository.ChapterOutlineRepository;
import cn.haowl.hinovel.novel.domain.service.ChapterDomainService;
import cn.haowl.hinovel.novel.domain.service.NovelDomainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static cn.haowl.hinovel.common.exception.enums.GlobalErrorCodeConstants.FORBIDDEN;
import static cn.haowl.hinovel.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 章节大纲应用服务。
 *
 * 协调领域层完成章节大纲的查询、保存和删除功能，
 * 通过仓储接口访问数据，不再直接依赖 Mapper。
 */
@Service
public class ChapterOutlineService {

    /**
     * 章节大纲仓储。
     */
    @Autowired
    private ChapterOutlineRepository chapterOutlineRepository;

    /**
     * 章节归属查询。
     */
    @Autowired
    private ChapterDomainService chapterDomainService;

    /**
     * 小说归属校验。
     */
    @Autowired
    private NovelDomainService novelDomainService;

    /**
     * 获取当前用户有权查看的章节大纲。
     *
     * @param chapterId 章节 ID
     * @param userId 操作者 ID
     * @return 章节大纲，尚未保存时返回 null
     */
    public ChapterOutline getByChapterId(Long chapterId, Long userId) {
        chapterDomainService.getOwnedChapter(chapterId, userId);
        return getByChapterId(chapterId);
    }

    /**
     * 获取章节大纲。
     *
     * @param chapterId 章节 ID
     * @return 章节大纲（不存在时返回 null）
     */
    public ChapterOutline getByChapterId(Long chapterId) {
        return chapterOutlineRepository.findByChapterId(chapterId).orElse(null);
    }

    /**
     * 获取小说的所有章节大纲。
     *
     * @param novelId 小说 ID
     * @param userId 操作者 ID
     * @return 章节大纲列表
     */
    public List<ChapterOutline> getByNovelId(Long novelId, Long userId) {
        novelDomainService.validateOwnership(novelId, userId);
        return chapterOutlineRepository.findByNovelId(novelId);
    }

    /**
     * 读取章节大纲，首次发布时创建空白大纲。
     *
     * @param chapter 已校验归属的章节
     * @return 已有大纲或新建的空白大纲
     */
    public ChapterOutline getOrCreateOutline(NovelChapter chapter) {
        return chapterOutlineRepository.findByChapterId(chapter.getId())
            .orElseGet(() -> chapterOutlineRepository.save(
                ChapterOutline.createEmpty(chapter.getNovelId(), chapter.getId())));
    }

    /**
     * 保存或更新章节大纲。
     *
     * @param outline 章节大纲实体
     * @return 保存后的大纲
     */
    private ChapterOutline saveOrUpdateOutline(ChapterOutline outline) {
        ChapterOutline current =
            chapterOutlineRepository.findByChapterId(outline.getChapterId())
                .orElseGet(() -> ChapterOutline.createEmpty(outline.getNovelId(), outline.getChapterId()));
        current.setNovelId(outline.getNovelId());
        current.updateFrom(outline);
        return chapterOutlineRepository.save(current);
    }

    /**
     * 保存当前用户所属章节的大纲。
     *
     * @param outline 大纲内容及章节 ID，小说 ID 如有提供须与章节匹配
     * @param userId 操作者 ID
     * @return 保存后的章节大纲
     */
    @Transactional(rollbackFor = Exception.class)
    public ChapterOutline saveOrUpdateOutline(ChapterOutline outline, Long userId) {
        NovelChapter chapter = chapterDomainService.getOwnedChapter(outline.getChapterId(), userId);
        if (outline.getNovelId() != null && !chapter.belongsToNovel(outline.getNovelId())) {
            throw exception(FORBIDDEN);
        }
        outline.setNovelId(chapter.getNovelId());
        return saveOrUpdateOutline(outline);
    }

    /**
     * 保存指定小说内的用户大纲。
     *
     * @param novelId 目标小说 ID
     * @param chapterId 章节 ID
     * @param userId 操作者 ID
     * @param content 用户编写的大纲内容
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveUserOutline(Long novelId, Long chapterId, Long userId, String content) {
        novelDomainService.validateOwnership(novelId, userId);
        saveUserOutline(chapterDomainService.getChapterInNovel(novelId, chapterId), content);
    }

    /**
     * 保存已校验归属的章节的用户大纲。
     *
     * @param chapter 已校验归属的章节
     * @param content 用户编写的大纲内容
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveUserOutline(NovelChapter chapter, String content) {
        ChapterOutline outline = ChapterOutline.createEmpty(chapter.getNovelId(), chapter.getId());
        outline.updateOutlineContent(content);
        saveOrUpdateOutline(outline);
    }

    /**
     * 保存生成的大纲，保留用户大纲及剧情分析。
     *
     * @param outlineId 大纲 ID
     * @param content 生成的 AI 大纲，为 null 时略过更新
     */
    public void saveAiOutlineContent(Long outlineId, String content) {
        if (content != null) {
            chapterOutlineRepository.updateAiOutlineContent(outlineId, content);
        }
    }

    /**
     * 删除章节大纲。
     *
     * @param chapterId 章节 ID
     * @param userId 操作者 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteByChapterId(Long chapterId, Long userId) {
        chapterDomainService.getOwnedChapter(chapterId, userId);
        chapterOutlineRepository.deleteByChapterId(chapterId);
    }
}
