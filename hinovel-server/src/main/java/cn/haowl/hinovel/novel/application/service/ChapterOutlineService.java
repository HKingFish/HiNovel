package cn.haowl.hinovel.novel.application.service;

import cn.haowl.hinovel.novel.domain.entity.ChapterOutline;
import cn.haowl.hinovel.novel.domain.repository.ChapterOutlineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 章节大纲应用服务。
 *
 * <p>协调领域层完成章节大纲的查询、保存和删除功能，
 * 通过仓储接口访问数据，不再直接依赖 Mapper。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChapterOutlineService {

    private final ChapterOutlineRepository chapterOutlineRepository;

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
     * @return 章节大纲列表
     */
    public List<ChapterOutline> getByNovelId(Long novelId) {
        return chapterOutlineRepository.findByNovelId(novelId);
    }

    /**
     * 保存或更新章节大纲。
     *
     * @param outline 章节大纲实体
     * @return 保存后的大纲
     */
    @Transactional(rollbackFor = Exception.class)
    public ChapterOutline saveOrUpdateOutline(ChapterOutline outline) {
        Optional<ChapterOutline> existing = chapterOutlineRepository.findByChapterId(outline.getChapterId());
        if (existing.isPresent()) {
            ChapterOutline current = existing.get();
            // 通过富方法更新内容
            current.updateOutlineContent(outline.getOutlineContent());
            if (outline.getAiOutlineContent() != null) {
                current.updateAiOutlineContent(outline.getAiOutlineContent());
            }
            if (outline.getPlotPoints() != null || outline.getInvolvedCharacters() != null
                    || outline.getEmotionTone() != null || outline.getSceneSetting() != null) {
                current.updatePlotAnalysis(
                        outline.getPlotPoints(), outline.getInvolvedCharacters(),
                        outline.getEmotionTone(), outline.getSceneSetting()
                );
            }
            return chapterOutlineRepository.save(current);
        }
        return chapterOutlineRepository.save(outline);
    }

    /**
     * 删除章节大纲。
     *
     * @param chapterId 章节 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteByChapterId(Long chapterId) {
        chapterOutlineRepository.deleteByChapterId(chapterId);
    }
}
