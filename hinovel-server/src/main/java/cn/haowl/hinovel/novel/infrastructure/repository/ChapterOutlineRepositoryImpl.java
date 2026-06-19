package cn.haowl.hinovel.novel.infrastructure.repository;

import cn.haowl.hinovel.novel.domain.entity.ChapterOutline;
import cn.haowl.hinovel.novel.domain.repository.ChapterOutlineRepository;
import cn.haowl.hinovel.novel.infrastructure.mapper.ChapterOutlineMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 章节大纲仓储实现。
 *
 * <p>基于 MyBatis-Plus 实现章节大纲仓储接口。</p>
 *
 * @author haowl
 */
@Repository
@RequiredArgsConstructor
public class ChapterOutlineRepositoryImpl implements ChapterOutlineRepository {

    private final ChapterOutlineMapper outlineMapper;

    @Override
    public ChapterOutline save(ChapterOutline outline) {
        if (outline.getId() == null) {
            outlineMapper.insert(outline);
        } else {
            outlineMapper.updateById(outline);
        }
        return outline;
    }

    @Override
    public Optional<ChapterOutline> findByChapterId(Long chapterId) {
        return Optional.ofNullable(outlineMapper.selectByChapterId(chapterId));
    }

    @Override
    public List<ChapterOutline> findByNovelId(Long novelId) {
        return outlineMapper.selectByNovelId(novelId);
    }

    @Override
    public void deleteByChapterId(Long chapterId) {
        outlineMapper.deleteByChapterId(chapterId);
    }
}
