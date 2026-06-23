package cn.haowl.hinovel.ai.application.embedding;

import cn.haowl.hinovel.ai.domain.entity.EmbeddingConfig;
import cn.haowl.hinovel.ai.domain.repository.EmbeddingConfigRepository;
import cn.haowl.hinovel.common.exception.BusinessException;
import cn.haowl.hinovel.common.exception.enums.GlobalErrorCodeConstants;
import cn.haowl.hinovel.common.service.ApiKeyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 嵌入式模型配置应用服务。
 *
 * <p>提供嵌入式模型配置的 CRUD 操作，apiKey 加密存储，用户数据隔离。</p>
 *
 * @author wylon
 * @date 2026/3/23 14:56
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingConfigService {

    private final EmbeddingConfigRepository embeddingConfigRepository;
    private final ApiKeyService apiKeyService;

    /**
     * 查询指定用户的所有嵌入式模型配置。
     *
     * @param userId 用户 ID
     * @return 配置列表
     */
    public List<EmbeddingConfig> listByUserId(Long userId) {
        return embeddingConfigRepository.findAllByUserId(userId);
    }

    /**
     * 根据 ID 查询配置（校验归属）。
     *
     * @param id     配置 ID
     * @param userId 当前用户 ID
     * @return 配置实体
     */
    public EmbeddingConfig getById(Long id, Long userId) {
        EmbeddingConfig config = embeddingConfigRepository.findById(id)
            .orElseThrow(() -> new BusinessException(GlobalErrorCodeConstants.NOT_FOUND));
        if (!config.getUserId().equals(userId)) {
            throw new BusinessException(GlobalErrorCodeConstants.FORBIDDEN);
        }
        return config;
    }

    /**
     * 创建嵌入式模型配置。
     *
     * @param userId     用户 ID
     * @param name       配置名称
     * @param modelType  模型类型
     * @param baseUrl    API 基础地址
     * @param apiKey     API 密钥（明文）
     * @param modelName  模型名称
     * @param dimensions 向量维度
     * @return 创建后的配置实体
     */
    public EmbeddingConfig create(Long userId, String name, String modelType,
                                   String baseUrl, String apiKey,
                                   String modelName, Integer dimensions) {
        log.info("创建嵌入式模型配置，userId={}，name={}", userId, name);
        String encryptedKey = (apiKey != null && !apiKey.isBlank())
                ? apiKeyService.encrypt(apiKey) : null;
        EmbeddingConfig config = EmbeddingConfig.create(
                userId, name, modelType, baseUrl, encryptedKey, modelName, dimensions);
        return embeddingConfigRepository.save(config);
    }

    /**
     * 更新嵌入式模型配置。
     *
     * @param id         配置 ID
     * @param userId     当前用户 ID
     * @param name       配置名称
     * @param modelType  模型类型
     * @param baseUrl    API 基础地址
     * @param apiKey     API 密钥（明文，为空则不修改）
     * @param modelName  模型名称
     * @param dimensions 向量维度
     * @return 更新后的配置实体
     */
    public EmbeddingConfig update(Long id, Long userId, String name, String modelType,
                                   String baseUrl, String apiKey,
                                   String modelName, Integer dimensions) {
        log.info("更新嵌入式模型配置，id={}，userId={}", id, userId);
        EmbeddingConfig config = getById(id, userId);
        config.setName(name);
        config.setModelType(modelType);
        config.setBaseUrl(baseUrl);
        config.setModelName(modelName);
        config.setDimensions(dimensions);
        // 仅当传入了新的明文 apiKey 时才重新加密
        if (isNewApiKey(apiKey)) {
            config.setApiKey(apiKeyService.encrypt(apiKey));
        }
        return embeddingConfigRepository.save(config);
    }

    /**
     * 删除嵌入式模型配置。
     *
     * @param id     配置 ID
     * @param userId 当前用户 ID
     */
    public void delete(Long id, Long userId) {
        log.info("删除嵌入式模型配置，id={}，userId={}", id, userId);
        // 校验归属
        getById(id, userId);
        embeddingConfigRepository.deleteById(id);
    }

    /**
     * 激活嵌入式模型配置。
     *
     * @param id     配置 ID
     * @param userId 当前用户 ID
     */
    public void activate(Long id, Long userId) {
        log.info("激活嵌入式模型配置，id={}，userId={}", id, userId);
        EmbeddingConfig config = getById(id, userId);
        config.activate();
        embeddingConfigRepository.save(config);
    }

    /**
     * 停用嵌入式模型配置。
     *
     * @param id     配置 ID
     * @param userId 当前用户 ID
     */
    public void deactivate(Long id, Long userId) {
        log.info("停用嵌入式模型配置，id={}，userId={}", id, userId);
        EmbeddingConfig config = getById(id, userId);
        config.deactivate();
        embeddingConfigRepository.save(config);
    }

    /**
     * 查询指定用户的激活配置（用于向量存储时动态选择模型）。
     *
     * @param userId 用户 ID
     * @return 激活的配置，不存在返回 null
     */
    public EmbeddingConfig findActiveByUserId(Long userId) {
        return embeddingConfigRepository.findActiveByUserId(userId).orElse(null);
    }

    /**
     * 解密 apiKey。
     *
     * @param encryptedKey 加密的 apiKey
     * @return 明文 apiKey
     */
    public String decryptApiKey(String encryptedKey) {
        if (encryptedKey == null || encryptedKey.isBlank()) {
            return null;
        }
        return apiKeyService.decrypt(encryptedKey);
    }

    /**
     * 判断是否为新的明文 apiKey（非空且不含脱敏标记）。
     *
     * @param apiKey 前端传入的 apiKey
     * @return 是否为新密钥
     */
    private boolean isNewApiKey(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            return false;
        }
        return !apiKey.contains("****");
    }
}
