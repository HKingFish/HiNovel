package cn.haowl.hinovel.ai.infrastructure.llm;

import cn.haowl.hinovel.ai.application.llm.LlmProviderFactory;
import cn.haowl.hinovel.ai.application.llm.LlmProviderPort;
import cn.haowl.hinovel.ai.application.llm.LlmProviderQueryPort;
import cn.haowl.hinovel.ai.domain.entity.LlmProvider;
import cn.haowl.hinovel.ai.enums.AiErrorCodeConstants;
import cn.haowl.hinovel.common.exception.BusinessException;
import cn.haowl.hinovel.common.exception.enums.GlobalErrorCodeConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * LLM 提供方工厂默认实现。
 *
 * <p>使用 ConcurrentHashMap 缓存 LLM 实例，按提供方 ID 隔离，避免重复创建。
 * 集成 {@link ProviderHealthTracker} 实现健康状态追踪和自动 failover，
 * 当首选 Provider 不健康时自动切换到其他可用 Provider。</p>
 *
 * @author haowl
 */
@Slf4j
@Component
public class DefaultLlmProviderFactory implements LlmProviderFactory {

    private final LlmProviderQueryPort llmProviderQueryPort;
    private final ProviderHealthTracker healthTracker;

    /** 提供方 ID -> LLM 实例缓存，避免重复创建 */
    private final ConcurrentHashMap<Long, LlmProviderPort> providerCache = new ConcurrentHashMap<>();

    public DefaultLlmProviderFactory(LlmProviderQueryPort llmProviderQueryPort,
                                     ProviderHealthTracker healthTracker) {
        this.llmProviderQueryPort = llmProviderQueryPort;
        this.healthTracker = healthTracker;
    }

    @Override
    public LlmProviderPort getByProviderId(Long providerId) {
        if (providerId == null) {
            throw new BusinessException(GlobalErrorCodeConstants.PARAM_ERROR, "提供方 ID 不能为空");
        }
        return providerCache.computeIfAbsent(providerId, this::createByProviderId);
    }

    @Override
    public LlmProviderPort getByProvider(LlmProvider provider) {
        if (provider == null || provider.getId() == null) {
            throw new BusinessException(GlobalErrorCodeConstants.PARAM_ERROR, "提供方配置不能为空");
        }
        return providerCache.computeIfAbsent(provider.getId(), id -> createAdapter(provider));
    }

    @Override
    public LlmProviderPort getAvailableProvider(Long preferredProviderId) {
        if (preferredProviderId == null) {
            throw new BusinessException(GlobalErrorCodeConstants.PARAM_ERROR, "提供方 ID 不能为空");
        }
        // 优先使用指定的 Provider
        if (healthTracker.isHealthy(preferredProviderId)) {
            return getByProviderId(preferredProviderId);
        }
        log.warn("首选 Provider 不健康，尝试 failover，ID：{}", preferredProviderId);
        // 查询所有激活的 Provider，尝试找到健康的替代
        List<LlmProvider> activeProviders = llmProviderQueryPort.listActiveProviders();
        for (LlmProvider candidate : activeProviders) {
            if (!candidate.getId().equals(preferredProviderId)
                    && healthTracker.isHealthy(candidate.getId())) {
                log.info("failover 到备选 Provider，ID：{}，名称：{}",
                        candidate.getId(), candidate.getName());
                return getByProvider(candidate);
            }
        }
        // 所有 Provider 都不健康，仍然尝试首选（可能冷却期已过）
        log.warn("所有 Provider 均不健康，强制使用首选 Provider，ID：{}", preferredProviderId);
        return getByProviderId(preferredProviderId);
    }

    @Override
    public void reportSuccess(Long providerId) {
        if (providerId != null) {
            healthTracker.recordSuccess(providerId);
        }
    }

    @Override
    public void reportFailure(Long providerId) {
        if (providerId != null) {
            healthTracker.recordFailure(providerId);
        }
    }

    @Override
    public void evict(Long providerId) {
        if (providerId != null) {
            providerCache.remove(providerId);
            log.info("已移除 LLM 提供方缓存，ID：{}", providerId);
        }
    }

    @Override
    public void evictAll() {
        providerCache.clear();
        log.info("已清空所有 LLM 提供方缓存");
    }

    /**
     * 根据提供方 ID 查询配置并创建适配器。
     *
     * @param providerId 提供方主键 ID
     * @return LLM 提供方端口实例
     */
    private LlmProviderPort createByProviderId(Long providerId) {
        LlmProvider provider = llmProviderQueryPort.getWithDecryptedKey(providerId);
        if (provider == null) {
            throw new BusinessException(AiErrorCodeConstants.LLM_PROVIDER_UNAVAILABLE,
                    "未找到 LLM 提供方配置，ID：" + providerId);
        }
        if (!provider.isActive()) {
            throw new BusinessException(AiErrorCodeConstants.LLM_PROVIDER_UNAVAILABLE,
                    "LLM 提供方已停用，ID：" + providerId);
        }
        return createAdapter(provider);
    }

    /**
     * 根据提供方实体创建适配器实例。
     *
     * <p>当前所有提供方（OpenAI、DeepSeek、通义千问）均兼容 OpenAI 协议，
     * 统一使用 OpenAiCompatibleLlmAdapter。后续如有不兼容协议的提供方，
     * 可在此处按 providerType 分发到不同的适配器实现。</p>
     *
     * @param provider 提供方实体
     * @return LLM 提供方端口实例
     */
    private LlmProviderPort createAdapter(LlmProvider provider) {
        log.info("创建 LLM 适配器，提供方：{}，类型：{}，模型：{}",
                provider.getName(), provider.getProviderType(), provider.getModels());
        return new OpenAiCompatibleLlmAdapter(
                provider.getProviderType(),
                provider.getBaseUrl(),
                provider.getApiKey(),
                provider.getModelList().isEmpty() ? null : provider.getModelList().get(0)
        );
    }
}
