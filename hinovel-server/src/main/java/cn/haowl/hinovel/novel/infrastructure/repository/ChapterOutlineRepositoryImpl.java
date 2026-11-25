package cn.haowl.hinovel.novel.infrastructure.repository;

import cn.haowl.hinovel.novel.domain.entity.ChapterOutline;
import cn.haowl.hinovel.novel.domain.repository.ChapterOutlineRepository;
import cn.haowl.hinovel.novel.infrastructure.mapper.ChapterOutlineMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static cn.haowl.hinovel.common.exception.util.ServiceExceptionUtil.exception;
import static cn.haowl.hinovel.novel.enums.NovelErrorCodeConstants.OUTLINE_SAVE_FAILED;

/**
 * 章节大纲仓储实现。
 *
 * 基于 MyBatis-Plus 实现章节大纲仓储接口。
 *
 * @author haowl
 */
@Repository
public class ChapterOutlineRepositoryImpl implements ChapterOutlineRepository {

    /**
     * 章节大纲数据访问。
     */
    @Autowired
    private ChapterOutlineMapper outlineMapper;

    @Override
    public ChapterOutline save(ChapterOutline outline) {
        if (outline.getId() == null) {
            outlineMapper.insert(outline);
        } else if (0 == outlineMapper.updateById(outline)) {
            throw exception(OUTLINE_SAVE_FAILED);
        }
        return outline;
    }

    @Override
    public void updateAiOutlineContent(Long outlineId, String content) {
        outlineMapper.updateAiOutlineContent(outlineId, content);
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
