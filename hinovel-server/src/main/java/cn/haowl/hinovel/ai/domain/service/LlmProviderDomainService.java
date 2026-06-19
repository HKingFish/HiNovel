package cn.haowl.hinovel.ai.domain.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.haowl.hinovel.ai.domain.entity.LlmProvider;
import cn.haowl.hinovel.ai.domain.repository.LlmProviderRepository;
import cn.haowl.hinovel.common.exception.BusinessException;
import cn.haowl.hinovel.common.response.ErrorCode;
import cn.haowl.hinovel.common.service.ApiKeyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * LLM 提供方领域服务。
 *
 * <p>处理 LLM 提供方相关的业务逻辑。</p>
 *
 * @Author : haowl
 * @Date : 2026/3/12 23:30
 */
@Service
@RequiredArgsConstructor
public class LlmProviderDomainService {

    private final LlmProviderRepository llmProviderRepository;
    private final ApiKeyService apiKeyService;

    /**
     * 创建 LLM 提供方。
     *
     * @param name         名称
     * @param providerType 类型
     * @param baseUrl      基础URL
     * @param apiKey       API密钥
     * @param models       模型列表
     * @param userId       所属用户 ID
     * @return LLM 提供方实体
     */
    public LlmProvider create(String name, String providerType, String baseUrl,
                              String apiKey, List<String> models, Long userId) {
        if (llmProviderRepository.existsByNameAndUserId(name, userId)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        // 加密 API 密钥后再存储
        String encryptedApiKey = apiKeyService.encrypt(apiKey);
        LlmProvider provider = LlmProvider.create(
                name, providerType, baseUrl, encryptedApiKey, models, userId);
        return llmProviderRepository.save(provider);
    }

    /**
     * 获取 LLM 提供方。
     *
     * @param id 提供方ID
     * @return LLM 提供方实体
     */
    public LlmProvider getById(Long id) {
        return llmProviderRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    /**
     * 获取激活的 LLM 提供方列表。
     *
     * @return 激活的提供方列表
     */
    public List<LlmProvider> getActiveProviders() {
        return llmProviderRepository.findActiveProvidersByUserId(StpUtil.getLoginIdAsLong());
    }

    /**
     * 激活提供方。
     *
     * @param id 提供方ID
     */
    public void activate(Long id) {
        LlmProvider provider = getById(id);
        provider.activate();
        llmProviderRepository.save(provider);
    }

    /**
     * 停用提供方。
     *
     * @param id 提供方ID
     */
    public void deactivate(Long id) {
        LlmProvider provider = getById(id);
        provider.deactivate();
        llmProviderRepository.save(provider);
    }

    /**
     * 更新 API 密钥。
     *
     * @param id     提供方ID
     * @param apiKey 新的API密钥（明文，将自动加密存储）
     */
    public void updateApiKey(Long id, String apiKey) {
        LlmProvider provider = getById(id);
        // 加密后存储
        provider.updateApiKey(apiKeyService.encrypt(apiKey));
        llmProviderRepository.save(provider);
    }

    /**
     * 删除提供方。
     *
     * @param id 提供方ID
     */
    public void delete(Long id) {
        llmProviderRepository.deleteById(id);
    }
}
