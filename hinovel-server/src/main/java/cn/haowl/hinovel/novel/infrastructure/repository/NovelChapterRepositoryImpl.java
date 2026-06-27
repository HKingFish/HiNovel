package cn.haowl.hinovel.novel.infrastructure.repository;

import cn.haowl.hinovel.common.constant.CommonConstants;
import cn.haowl.hinovel.novel.domain.entity.NovelChapter;
import cn.haowl.hinovel.novel.domain.repository.NovelChapterRepository;
import cn.haowl.hinovel.novel.infrastructure.mapper.NovelChapterMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static cn.haowl.hinovel.common.exception.util.ServiceExceptionUtil.exception;
import static cn.haowl.hinovel.novel.enums.NovelErrorCodeConstants.CHAPTER_TITLE_EXISTS;
import static cn.haowl.hinovel.novel.infrastructure.mapper.NovelChapterMapper.withoutContentWrapper;

/**
 * 小说章节仓储实现。
 *
 * <p>基于 MyBatis-Plus 实现小说章节仓储接口。</p>
 *
 * @author haowl
 */
@Repository
@RequiredArgsConstructor
public class NovelChapterRepositoryImpl implements NovelChapterRepository {

    private final NovelChapterMapper novelChapterMapper;

    @Override
    public NovelChapter save(NovelChapter chapter) {
        if (novelChapterMapper.existSameTitle(
                chapter.getNovelId(), chapter.getId(), chapter.getTitle())) {
            throw exception(CHAPTER_TITLE_EXISTS);
        }

        if (chapter.getId() == null) {
            novelChapterMapper.insert(chapter);
        } else {
            novelChapterMapper.updateById(chapter);
        }
        return chapter;
    }

    @Override
    public Optional<NovelChapter> findById(Long id) {
        return Optional.ofNullable(novelChapterMapper.selectById(id));
    }

    @Override
    public List<NovelChapter> findByNovelIdWithoutContent(Long novelId) {
        return novelChapterMapper.findByNovelIdWithoutContent(novelId);
    }

    @Override
    public Optional<NovelChapter> findByNovelIdAndChapterNumber(Long novelId, Integer chapterNumber) {
        LambdaQueryWrapper<NovelChapter> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NovelChapter::getNovelId, novelId)
                .eq(NovelChapter::getChapterNumber, chapterNumber);
        return Optional.ofNullable(novelChapterMapper.selectOne(wrapper));
    }

    @Override
    public long countByNovelId(Long novelId) {
        LambdaQueryWrapper<NovelChapter> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NovelChapter::getNovelId, novelId)
                .eq(NovelChapter::getDeleted, CommonConstants.DELETED_FALSE);
        return novelChapterMapper.selectCount(wrapper);
    }

    @Override
    public void deleteById(Long id) {
        novelChapterMapper.deleteById(id);
    }

    @Override
    public void deleteByNovelId(Long novelId) {
        LambdaQueryWrapper<NovelChapter> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NovelChapter::getNovelId, novelId);
        novelChapterMapper.delete(wrapper);
    }

    @Override
    public Integer findMaxSortOrder(Long novelId) {
        return novelChapterMapper.selectMaxSortOrder(novelId);
    }

    @Override
    public Integer findMaxChapterNumber(Long novelId) {
        return novelChapterMapper.selectMaxChapterNumber(novelId);
    }

    @Override
    public List<NovelChapter> findByNovelIdPageWithoutContent(Long novelId, int pageNum, int pageSize,
                                                              Integer status, Integer vectorStored, String title) {
        return novelChapterMapper.selectList(withoutContentWrapper()
                .eq(NovelChapter::getNovelId, novelId)
                .eq(NovelChapter::getDeleted, CommonConstants.DELETED_FALSE)
                .eq(Objects.nonNull(status), NovelChapter::getStatus, status)
                .eq(Objects.nonNull(vectorStored), NovelChapter::getVectorStored, vectorStored)
                .like(StringUtils.isNotBlank(title), NovelChapter::getTitle, StringUtils.trim(title))
                .last(CommonConstants.limitOf((pageNum - 1) * pageSize, pageSize))
                .orderByDesc(NovelChapter::getSortOrder));
    }

    @Override
    public int countByNovelIdWithFilter(Long novelId, Integer status, Integer vectorStored, String title) {
        LambdaQueryWrapper<NovelChapter> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NovelChapter::getNovelId, novelId)
                .eq(NovelChapter::getDeleted, CommonConstants.DELETED_FALSE);

        if (status != null) {
            wrapper.eq(NovelChapter::getStatus, status);
        }
        if (vectorStored != null) {
            wrapper.eq(NovelChapter::getVectorStored, vectorStored);
        }
        if (title != null && !title.trim().isEmpty()) {
            wrapper.like(NovelChapter::getTitle, title.trim());
        }

        return Math.toIntExact(novelChapterMapper.selectCount(wrapper));
    }

    @Override
    public List<NovelChapter> findPreviousChapters(Long novelId, Integer chapterNumber, int limit) {
        return novelChapterMapper.selectPreviousChapters(novelId, chapterNumber, limit);
    }

    @Override
    public List<NovelChapter> findAllByNovelIdOrderBySortOrder(Long novelId) {
        return novelChapterMapper.selectList(withoutContentWrapper()
                .eq(NovelChapter::getNovelId, novelId)
                .eq(NovelChapter::getDeleted, CommonConstants.DELETED_FALSE)
                .orderByAsc(NovelChapter::getSortOrder)
                .orderByAsc(NovelChapter::getChapterNumber));
    }
}
