package cn.haowl.hinovel.ai.application.llm;

import cn.haowl.hinovel.ai.domain.entity.LlmProvider;
import cn.haowl.hinovel.ai.infrastructure.mapper.LlmProviderMapper;
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
     * LLM Provider 数据访问映射器。
     */
    @Autowired
    private LlmProviderMapper llmProviderMapper;

    /**
     * API 密钥加解密服务。
     */
    @Autowired
    private ApiKeyService apiKeyService;

    /**
     * 根据ID获取 LLM Provider 配置（包含解密的 API Key）。
     *
     * 从数据库读取加密的 API Key 后自动解密，供 LLM 调用使用。
     * 如果传入的ID为null，则直接返回null。
     *
     * @param id LLM Provider 的主键ID
     * @return LLM Provider 实体对象（apiKey 已解密），如果ID为空则返回 null
     */
    @Override
    public LlmProvider getWithDecryptedKey(Long id) {
        if (id == null) {
            return null;
        }
        LlmProvider provider = llmProviderMapper.selectById(id);
        if (provider != null && provider.getApiKey() != null) {
            // 解密 API 密钥
            provider.setApiKey(apiKeyService.decrypt(provider.getApiKey()));
        }
        return provider;
    }

    /**
     * 查询所有已激活的 LLM 提供方（包含解密的 API Key）。
     *
     * @return 已激活的提供方列表（apiKey 已解密）
     */
    @Override
    public List<LlmProvider> listActiveProviders() {
        List<LlmProvider> providers = llmProviderMapper.selectActiveProviders();
        // 批量解密 API Key
        for (LlmProvider provider : providers) {
            if (provider.getApiKey() != null) {
                provider.setApiKey(apiKeyService.decrypt(provider.getApiKey()));
            }
        }
        return providers;
    }
}
