package cn.haowl.hinovel.ai.application.embedding;

import cn.haowl.hinovel.ai.domain.entity.EmbeddingConfig;
import cn.haowl.hinovel.ai.domain.repository.EmbeddingConfigRepository;
import cn.haowl.hinovel.ai.interfaces.dto.EmbeddingConfigRequest;
import cn.haowl.hinovel.common.service.ApiKeyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static cn.haowl.hinovel.common.exception.enums.GlobalErrorCodeConstants.FORBIDDEN;
import static cn.haowl.hinovel.common.exception.enums.GlobalErrorCodeConstants.NOT_FOUND;
import static cn.haowl.hinovel.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 嵌入式模型配置应用服务。
 *
 * <p>提供嵌入式模型配置的 CRUD 操作，apiKey 加密存储，用户数据隔离。
 * 面向展示的返回值统一做密钥脱敏。</p>
 *
 * @author wylon
 * @date 2026/3/23 14:56
 */
@Slf4j
@Service
public class EmbeddingConfigService {

    /**
     * 嵌入式模型配置仓储。
     */
    @Autowired
    private EmbeddingConfigRepository embeddingConfigRepository;

    /**
     * API 密钥加解密服务。
     */
    @Autowired
    private ApiKeyService apiKeyService;

    /**
     * 查询指定用户的所有嵌入式模型配置（apiKey 已脱敏，用于前端展示）。
     *
     * @param userId 用户 ID
     * @return 配置列表
     */
    public List<EmbeddingConfig> listByUserId(Long userId) {
        List<EmbeddingConfig> configs = embeddingConfigRepository.findAllByUserId(userId);
        configs.forEach(this::maskApiKey);
        return configs;
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
            .orElseThrow(() -> exception(NOT_FOUND));
        if (!config.getUserId().equals(userId)) {
            throw exception(FORBIDDEN);
        }
        return config;
    }

    /**
     * 创建嵌入式模型配置。
     *
     * @param userId  用户 ID
     * @param request 创建请求（apiKey 为明文）
     * @return 创建后的配置实体（apiKey 已脱敏）
     */
    public EmbeddingConfig create(Long userId, EmbeddingConfigRequest request) {
        log.info("[create][创建嵌入式模型配置，userId={}，name={}]", userId, request.getName());
        String apiKey = request.getApiKey();
        String encryptedKey = (apiKey != null && !apiKey.isBlank())
                ? apiKeyService.encrypt(apiKey) : null;
        EmbeddingConfig config = EmbeddingConfig.create(
                userId, request.getName(), request.getModelType(), request.getBaseUrl(),
                encryptedKey, request.getModelName(), request.getDimensions());
        return maskApiKey(embeddingConfigRepository.save(config));
    }

    /**
     * 更新嵌入式模型配置。
     *
     * @param id      配置 ID
     * @param userId  当前用户 ID
     * @param request 更新请求
     * @return 更新后的配置实体（apiKey 已脱敏）
     */
    public EmbeddingConfig update(Long id, Long userId, EmbeddingConfigRequest request) {
        log.info("[update][更新嵌入式模型配置，id={}，userId={}]", id, userId);
        EmbeddingConfig config = getById(id, userId);
        config.setName(request.getName());
        config.setModelType(request.getModelType());
        config.setBaseUrl(request.getBaseUrl());
        config.setModelName(request.getModelName());
        config.setDimensions(request.getDimensions());
        // 仅当传入了新的明文 apiKey 时才重新加密
        if (apiKeyService.isNewApiKey(request.getApiKey())) {
            config.setApiKey(apiKeyService.encrypt(request.getApiKey()));
        }
        return maskApiKey(embeddingConfigRepository.save(config));
    }

    /**
     * 删除嵌入式模型配置。
     *
     * @param id     配置 ID
     * @param userId 当前用户 ID
     */
    public void delete(Long id, Long userId) {
        log.info("[delete][删除嵌入式模型配置，id={}，userId={}]", id, userId);
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
        log.info("[activate][激活嵌入式模型配置，id={}，userId={}]", id, userId);
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
        log.info("[deactivate][停用嵌入式模型配置，id={}，userId={}]", id, userId);
        EmbeddingConfig config = getById(id, userId);
        config.deactivate();
        embeddingConfigRepository.save(config);
    }

    /**
     * 查询指定用户的激活配置（用于向量存储时动态选择模型）。
     *
     * @param userId 用户 ID
     * @return 激活的配置（apiKey 为密文），不存在返回 null
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
     * 对配置的 apiKey 脱敏，避免前端拿到完整密钥。
     *
     * @param config 配置实体
     * @return 脱敏后的同一实例
     */
    private EmbeddingConfig maskApiKey(EmbeddingConfig config) {
        if (config != null && config.getApiKey() != null) {
            config.setApiKey(apiKeyService.decryptAndMask(config.getApiKey()));
        }
        return config;
    }
}
