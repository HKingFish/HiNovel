package cn.haowl.hinovel.novel.infrastructure.repository;

import cn.haowl.hinovel.common.constant.CommonConstants;
import cn.haowl.hinovel.novel.constant.NovelChapterStatusEnum;
import cn.haowl.hinovel.novel.domain.entity.NovelChapter;
import cn.haowl.hinovel.novel.domain.query.ChapterPageQuery;
import cn.haowl.hinovel.novel.domain.repository.NovelChapterRepository;
import cn.haowl.hinovel.novel.infrastructure.mapper.NovelChapterMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static cn.haowl.hinovel.common.exception.util.ServiceExceptionUtil.exception;
import static cn.haowl.hinovel.novel.enums.NovelErrorCodeConstants.CHAPTER_TITLE_EXISTS;
import static cn.haowl.hinovel.novel.infrastructure.mapper.NovelChapterMapper.withoutContentWrapper;

/**
 * 小说章节仓储实现。
 *
 * 基于 MyBatis-Plus 实现小说章节仓储接口。
 *
 * @author haowl
 */
@Repository
public class NovelChapterRepositoryImpl implements NovelChapterRepository {

    /**
     * 章节数据访问。
     */
    @Autowired
    private NovelChapterMapper novelChapterMapper;

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
    public List<NovelChapter> findByNovelIdPageWithoutContent(ChapterPageQuery query) {
        return novelChapterMapper.selectList(applyPageFilters(withoutContentWrapper(), query)
            .last(CommonConstants.limitOf((query.getPageNum() - 1) * query.getPageSize(), query.getPageSize()))
                .orderByDesc(NovelChapter::getSortOrder));
    }

    @Override
    public int countByNovelIdWithFilter(ChapterPageQuery query) {
        return Math.toIntExact(
            novelChapterMapper.selectCount(applyPageFilters(new LambdaQueryWrapper<>(), query)));
    }

    /**
     * 为章节分页和计数应用同一份筛选条件。
     */
    private LambdaQueryWrapper<NovelChapter> applyPageFilters(
        LambdaQueryWrapper<NovelChapter> wrapper, ChapterPageQuery query) {
        NovelChapterStatusEnum status = NovelChapterStatusEnum.fromCode(query.getStatus());
        return wrapper.eq(NovelChapter::getNovelId, query.getNovelId())
            .eq(NovelChapter::getDeleted, CommonConstants.DELETED_FALSE)
            // 接口使用数字编码，数据库按枚举名称存储章节状态。
            .eq(status != null, NovelChapter::getStatus, status)
            .isNull(query.getStatus() != null && status == null, NovelChapter::getStatus)
            .eq(query.getVectorStored() != null, NovelChapter::getVectorStored, query.getVectorStored())
            .like(StringUtils.isNotBlank(query.getTitle()), NovelChapter::getTitle,
                StringUtils.trim(query.getTitle()));
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
