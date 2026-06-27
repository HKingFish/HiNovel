package cn.haowl.hinovel.novel.application.service;

import cn.haowl.hinovel.novel.domain.entity.NovelSettings;
import cn.haowl.hinovel.novel.infrastructure.mapper.NovelSettingsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 小说配置服务。
 *
 * <p>提供小说级别和用户级别的 AI 功能配置管理，
 * 通过 {@link NovelSettings#merge(NovelSettings, NovelSettings)} 实现两级配置合并，
 * 小说级别配置优先于用户默认配置。</p>
 *
 * @author haowl
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NovelSettingsService {

    private final NovelSettingsMapper novelSettingsMapper;

    /**
     * 获取指定小说的有效配置（小说级别 + 用户默认合并后的结果）
     *
     * @param novelId 小说 ID
     * @param userId  用户 ID
     * @return 合并后的有效配置
     */
    public NovelSettings getEffectiveSettings(Long novelId, Long userId) {
        NovelSettings novelLevel = novelSettingsMapper.selectByNovelId(novelId);
        NovelSettings userDefault = novelSettingsMapper.selectUserDefault(userId);
        return NovelSettings.merge(novelLevel, userDefault);
    }

    /**
     * 获取小说级别配置
     *
     * @param novelId 小说 ID
     * @return 小说配置（不存在时返回 null）
     */
    public NovelSettings getNovelSettings(Long novelId) {
        return novelSettingsMapper.selectByNovelId(novelId);
    }

    /**
     * 获取用户级别默认配置。
     *
     * <p>若用户未保存过配置，返回系统默认配置（{@link NovelSettings#createDefault()}），
     * 保证前端始终拿到一份完整的默认值，无需在前端硬编码兜底。</p>
     *
     * @param userId 用户 ID
     * @return 用户默认配置（未保存过时返回系统默认配置，不返回 null）
     */
    public NovelSettings getUserDefaultSettings(Long userId) {
        NovelSettings settings = novelSettingsMapper.selectUserDefault(userId);
        return settings != null ? settings : NovelSettings.createDefault();
    }

    /**
     * 获取系统默认配置（不查数据库）。
     *
     * <p>供前端「恢复默认」按钮调用，确保默认值的唯一权威来源在后端。</p>
     *
     * @return 系统默认配置
     */
    public NovelSettings getSystemDefaultSettings() {
        return NovelSettings.createDefault();
    }

    /**
     * 保存或更新小说级别配置
     *
     * @param novelId  小说 ID
     * @param settings 配置内容
     * @return 保存后的配置
     */
    @Transactional(rollbackFor = Exception.class)
    public NovelSettings saveNovelSettings(Long novelId, NovelSettings settings) {
        NovelSettings existing = novelSettingsMapper.selectByNovelId(novelId);

        if (existing == null) {
            settings.setNovelId(novelId);
            novelSettingsMapper.insert(settings);
            log.info("创建小说配置成功，novelId={}", novelId);
        } else {
            copySettingsFields(settings, existing);
            novelSettingsMapper.updateById(existing);
            log.info("更新小说配置成功，novelId={}", novelId);
            return existing;
        }
        return settings;
    }

    /**
     * 保存或更新用户级别默认配置
     *
     * @param userId   用户 ID
     * @param settings 配置内容
     * @return 保存后的配置
     */
    @Transactional(rollbackFor = Exception.class)
    public NovelSettings saveUserDefaultSettings(Long userId, NovelSettings settings) {
        NovelSettings existing = novelSettingsMapper.selectUserDefault(userId);

        if (existing == null) {
            settings.setNovelId(NovelSettings.USER_DEFAULT_NOVEL_ID);
            settings.setUserId(userId);
            novelSettingsMapper.insert(settings);
            log.info("创建用户默认配置成功，userId={}", userId);
        } else {
            copySettingsFields(settings, existing);
            novelSettingsMapper.updateById(existing);
            log.info("更新用户默认配置成功，userId={}", userId);
            return existing;
        }
        return settings;
    }

    /**
     * 将源配置的业务字段复制到目标配置
     *
     * @param source 源配置
     * @param target 目标配置
     */
    private void copySettingsFields(NovelSettings source, NovelSettings target) {
        target.setAutoAuditAfterRewrite(source.getAutoAuditAfterRewrite());
        target.setAutoOutlineAfterPublish(source.getAutoOutlineAfterPublish());
        target.setAutoVectorAfterPublish(source.getAutoVectorAfterPublish());
        target.setRewriteContextChapters(source.getRewriteContextChapters());
        target.setRewriteIncludeOutline(source.getRewriteIncludeOutline());
        target.setAuditIncludeOutline(source.getAuditIncludeOutline());
        target.setQaIncludeOutline(source.getQaIncludeOutline());
        target.setQaContextLength(source.getQaContextLength());
        target.setAutoSaveContent(source.getAutoSaveContent());
    }
}
