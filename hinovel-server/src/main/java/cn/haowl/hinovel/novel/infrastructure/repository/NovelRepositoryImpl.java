package cn.haowl.hinovel.novel.infrastructure.repository;

import cn.haowl.hinovel.novel.domain.entity.Novel;
import cn.haowl.hinovel.novel.domain.repository.NovelRepository;
import cn.haowl.hinovel.novel.infrastructure.mapper.NovelMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 小说仓储实现。
 *
 * <p>基于 MyBatis-Plus 实现小说仓储接口。</p>
 *
 * @Author : haowl
 * @Date : 2026/3/12 23:30
 */
@Repository
@RequiredArgsConstructor
public class NovelRepositoryImpl implements NovelRepository {

    private final NovelMapper novelMapper;

    @Override
    public Novel save(Novel novel) {
        if (novel.getId() == null) {
            novelMapper.insert(novel);
        } else {
            novelMapper.updateById(novel);
        }
        return novel;
    }

    @Override
    public Optional<Novel> findById(Long id) {
        return Optional.ofNullable(novelMapper.selectById(id));
    }

    @Override
    public List<Novel> findByUserId(Long userId) {
        LambdaQueryWrapper<Novel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Novel::getUserId, userId)
                .orderByDesc(Novel::getCreateTime);
        return novelMapper.selectList(wrapper);
    }

    @Override
    public List<Novel> findByUserIdAndStatus(Long userId, String status) {
        LambdaQueryWrapper<Novel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Novel::getUserId, userId)
                .eq(Novel::getStatus, status)
                .orderByDesc(Novel::getCreateTime);
        return novelMapper.selectList(wrapper);
    }

    @Override
    public void deleteById(Long id) {
        novelMapper.deleteById(id);
    }

    @Override
    public boolean existsByIdAndUserId(Long novelId, Long userId) {
        LambdaQueryWrapper<Novel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Novel::getId, novelId)
                .eq(Novel::getUserId, userId);
        return novelMapper.selectCount(wrapper) > 0;
    }
}
