package cn.haowl.hinovel.ai.application.llm;

import cn.haowl.hinovel.ai.domain.entity.LlmProvider;
import cn.haowl.hinovel.ai.domain.repository.LlmProviderRepository;
import cn.haowl.hinovel.common.service.ApiKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * LLM Provider 查询服务实现类。
 *
 * 实现了 {@link LlmProviderQueryPort} 接口，提供 LLM 提供方配置的查询功能。
 * 查询时自动解密 API 密钥，供内部 LLM 调用使用。
 *
 * @author haowl
 * @since 2024
 */
@Service
public class LlmProviderQueryService implements LlmProviderQueryPort {

    /**
     * LLM 提供方仓储。
     */
    @Autowired
    private LlmProviderRepository llmProviderRepository;

    /**
     * API 密钥加解密服务。
     */
    @Autowired
    private ApiKeyService apiKeyService;

    @Override
    public LlmProvider getWithDecryptedKey(Long id) {
        if (id == null) {
            return null;
        }
        return llmProviderRepository.findById(id).map(this::decryptApiKey).orElse(null);
    }

    @Override
    public List<LlmProvider> listActiveProviders(Long userId) {
        if (userId == null) {
            return List.of();
        }
        List<LlmProvider> providers = llmProviderRepository.findActiveProvidersByUserId(userId);
        providers.forEach(this::decryptApiKey);
        return providers;
    }

    /**
     * 解密内部模型调用使用的密钥。
     */
    private LlmProvider decryptApiKey(LlmProvider provider) {
        provider.setApiKey(apiKeyService.decrypt(provider.getApiKey()));
        return provider;
    }
}
