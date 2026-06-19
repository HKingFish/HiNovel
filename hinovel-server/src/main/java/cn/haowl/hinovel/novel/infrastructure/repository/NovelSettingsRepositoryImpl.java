package cn.haowl.hinovel.novel.infrastructure.repository;

import cn.haowl.hinovel.novel.domain.entity.NovelSettings;
import cn.haowl.hinovel.novel.domain.repository.NovelSettingsRepository;
import cn.haowl.hinovel.novel.infrastructure.mapper.NovelSettingsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 小说配置仓储实现。
 *
 * <p>基于 MyBatis-Plus 实现小说配置仓储接口。</p>
 *
 * @author haowl
 */
@Repository
@RequiredArgsConstructor
public class NovelSettingsRepositoryImpl implements NovelSettingsRepository {

    private final NovelSettingsMapper settingsMapper;

    @Override
    public NovelSettings save(NovelSettings settings) {
        if (settings.getId() == null) {
            settingsMapper.insert(settings);
        } else {
            settingsMapper.updateById(settings);
        }
        return settings;
    }

    @Override
    public Optional<NovelSettings> findByNovelId(Long novelId) {
        return Optional.ofNullable(settingsMapper.selectByNovelId(novelId));
    }

    @Override
    public Optional<NovelSettings> findUserDefault(Long userId) {
        return Optional.ofNullable(settingsMapper.selectUserDefault(userId));
    }
}
