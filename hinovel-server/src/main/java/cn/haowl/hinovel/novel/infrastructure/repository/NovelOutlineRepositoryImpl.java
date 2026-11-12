package cn.haowl.hinovel.novel.infrastructure.repository;

import cn.haowl.hinovel.novel.domain.entity.NovelOutline;
import cn.haowl.hinovel.novel.domain.repository.NovelOutlineRepository;
import cn.haowl.hinovel.novel.infrastructure.mapper.NovelOutlineMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 小说大纲仓储实现。
 *
 * <p>基于 MyBatis-Plus 实现小说大纲仓储接口。</p>
 */
@Repository
public class NovelOutlineRepositoryImpl implements NovelOutlineRepository {

    /** 小说大纲数据访问。 */
    @Autowired
    private NovelOutlineMapper novelOutlineMapper;

    @Override
    public NovelOutline save(NovelOutline outline) {
        if (outline.getId() == null) {
            novelOutlineMapper.insert(outline);
        } else {
            novelOutlineMapper.updateById(outline);
        }
        return outline;
    }

    @Override
    public Optional<NovelOutline> findByNovelId(Long novelId) {
        return Optional.ofNullable(novelOutlineMapper.selectByNovelId(novelId));
    }
}
