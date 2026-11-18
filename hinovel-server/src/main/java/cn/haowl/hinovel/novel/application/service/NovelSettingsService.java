package cn.haowl.hinovel.novel.application.service;

import cn.haowl.hinovel.novel.domain.entity.NovelSettings;
import cn.haowl.hinovel.novel.domain.repository.NovelSettingsRepository;
import cn.haowl.hinovel.novel.domain.service.NovelDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 小说配置服务。
 *
 * 提供小说级别和用户级别的 AI 功能配置管理，
 * 通过 {@link NovelSettings#merge(NovelSettings, NovelSettings)} 实现两级配置合并，
 * 小说级别配置优先于用户默认配置。
 *
 * @author haowl
 */
@Slf4j
@Service
public class NovelSettingsService {

    /**
     * 小说配置仓储。
     */
    @Autowired
    private NovelSettingsRepository settingsRepository;

    /**
     * 小说归属校验。
     */
    @Autowired
    private NovelDomainService novelDomainService;

    /**
     * 读取当前用户有权查看的小说生效配置。
     *
     * @param novelId 小说 ID
     * @param userId 操作者 ID
     * @return 合并小说配置与用户默认配置后的结果
     */
    public NovelSettings getOwnedEffectiveSettings(Long novelId, Long userId) {
        novelDomainService.validateOwnership(novelId, userId);
        return getEffectiveSettings(novelId, userId);
    }

    /**
     * 获取指定小说的有效配置（小说级别 + 用户默认合并后的结果）
     *
     * @param novelId 小说 ID
     * @param userId  用户 ID
     * @return 合并后的有效配置
     */
    public NovelSettings getEffectiveSettings(Long novelId, Long userId) {
        NovelSettings novelLevel = settingsRepository.findByNovelId(novelId).orElse(null);
        NovelSettings userDefault = settingsRepository.findUserDefault(userId).orElse(null);
        return NovelSettings.merge(novelLevel, userDefault);
    }

    /**
     * 获取小说级别配置
     *
     * @param novelId 小说 ID
     * @param userId 操作者 ID
     * @return 小说配置（不存在时返回 null）
     */
    public NovelSettings getNovelSettings(Long novelId, Long userId) {
        novelDomainService.validateOwnership(novelId, userId);
        return settingsRepository.findByNovelId(novelId).orElse(null);
    }

    /**
     * 获取用户级别默认配置。
     *
     * 若用户未保存过配置，返回系统默认配置（{@link NovelSettings#createDefault()}），
     * 保证前端始终拿到一份完整的默认值，无需在前端硬编码兜底。
     *
     * @param userId 用户 ID
     * @return 用户默认配置（未保存过时返回系统默认配置，不返回 null）
     */
    public NovelSettings getUserDefaultSettings(Long userId) {
        return settingsRepository.findUserDefault(userId).orElseGet(NovelSettings::createDefault);
    }

    /**
     * 获取系统默认配置（不查数据库）。
     *
     * 供前端「恢复默认」按钮调用，确保默认值的唯一权威来源在后端。
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
     * @param userId 操作者 ID
     * @param settings 配置内容
     * @return 保存后的配置
     */
    @Transactional(rollbackFor = Exception.class)
    public NovelSettings saveNovelSettings(Long novelId, Long userId, NovelSettings settings) {
        novelDomainService.validateOwnership(novelId, userId);
        NovelSettings existing = settingsRepository.findByNovelId(novelId).orElse(null);
        if (existing == null) {
            NovelSettings created = new NovelSettings();
            created.setNovelId(novelId);
            created.updateOptions(settings);
            settingsRepository.insert(created);
            log.info("[saveNovelSettings][已创建小说配置，novelId={}]", novelId);
            return created;
        }

        existing.updateOptions(settings);
        settingsRepository.save(existing);
        log.info("[saveNovelSettings][已更新小说配置，novelId={}]", novelId);
        return existing;
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
        NovelSettings existing = settingsRepository.findUserDefault(userId).orElse(null);
        if (existing == null) {
            NovelSettings created = NovelSettings.createDefault();
            created.setNovelId(NovelSettings.USER_DEFAULT_NOVEL_ID);
            created.setUserId(userId);
            created.updateOptions(settings);
            settingsRepository.insert(created);
            log.info("[saveUserDefaultSettings][已创建用户默认配置，userId={}]", userId);
            return created;
        }

        existing.updateOptions(settings);
        settingsRepository.save(existing);
        log.info("[saveUserDefaultSettings][已更新用户默认配置，userId={}]", userId);
        return existing;
    }
}
