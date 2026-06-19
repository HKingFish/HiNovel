package cn.haowl.hinovel.novel.infrastructure.repository;

import cn.haowl.hinovel.novel.domain.entity.NovelChapterVersion;
import cn.haowl.hinovel.novel.domain.repository.NovelChapterVersionRepository;
import cn.haowl.hinovel.novel.infrastructure.mapper.NovelChapterVersionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 章节版本仓储实现。
 *
 * <p>基于 MyBatis-Plus 实现章节版本仓储接口。</p>
 *
 * @author haowl
 */
@Repository
@RequiredArgsConstructor
public class NovelChapterVersionRepositoryImpl implements NovelChapterVersionRepository {

    private final NovelChapterVersionMapper versionMapper;

    @Override
    public NovelChapterVersion save(NovelChapterVersion version) {
        if (version.getId() == null) {
            versionMapper.insert(version);
        } else {
            versionMapper.updateById(version);
        }
        return version;
    }

    @Override
    public Optional<NovelChapterVersion> findById(Long id) {
        return Optional.ofNullable(versionMapper.selectById(id));
    }

    @Override
    public List<NovelChapterVersion> findByChapterId(Long chapterId) {
        return versionMapper.selectByChapterId(chapterId);
    }

    @Override
    public List<NovelChapterVersion> findByChapterIdWithLimit(Long chapterId, int limit) {
        return versionMapper.selectByChapterIdWithLimit(chapterId, limit);
    }

    @Override
    public NovelChapterVersion findLatestByChapterId(Long chapterId) {
        return versionMapper.selectLatestByChapterId(chapterId);
    }

    @Override
    public void cleanupOldVersions(Long chapterId, int keepCount) {
        versionMapper.deleteOldVersions(chapterId, keepCount);
    }
}
