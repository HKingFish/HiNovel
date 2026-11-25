package cn.haowl.hinovel.ai.infrastructure.llm;

import cn.haowl.hinovel.ai.application.llm.LlmProviderFactory;
import cn.haowl.hinovel.ai.application.llm.LlmProviderPort;
import cn.haowl.hinovel.ai.application.llm.LlmProviderQueryPort;
import cn.haowl.hinovel.ai.domain.entity.LlmProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static cn.haowl.hinovel.ai.enums.AiErrorCodeConstants.LLM_PROVIDER_DISABLED;
import static cn.haowl.hinovel.ai.enums.AiErrorCodeConstants.LLM_PROVIDER_NOT_FOUND;
import static cn.haowl.hinovel.common.exception.enums.GlobalErrorCodeConstants.PARAM_NOT_NULL;
import static cn.haowl.hinovel.common.exception.util.ServiceExceptionUtil.exception;

/**
 * LLM 提供方工厂默认实现。
 *
 * 使用 ConcurrentHashMap 缓存 LLM 实例，按提供方 ID 隔离，避免重复创建。
 * 集成 {@link ProviderHealthTracker} 实现健康状态追踪和自动 failover，
 * 当首选 Provider 不健康时自动切换到其他可用 Provider。
 *
 * @author haowl
 */
@Slf4j
@Component
public class DefaultLlmProviderFactory implements LlmProviderFactory {

    /**
     * LLM 提供方配置查询端口。
     */
    @Autowired
    private LlmProviderQueryPort llmProviderQueryPort;

    /**
     * 提供方健康状态追踪器。
     */
    @Autowired
    private ProviderHealthTracker healthTracker;

    /**
     * 按提供方 ID 复用模型实例。创建失败时不缓存，可通过 evict 和 evictAll 失效。
     */
    private final ConcurrentHashMap<Long, LlmProviderPort> providerCache = new ConcurrentHashMap<>();

    @Override
    public LlmProviderPort getByProviderId(Long providerId) {
        if (providerId == null) {
            throw exception(PARAM_NOT_NULL, "提供方 ID");
        }
        return providerCache.computeIfAbsent(providerId, this::createByProviderId);
    }

    @Override
    public LlmProviderPort getByProvider(LlmProvider provider) {
        if (provider == null || provider.getId() == null) {
            throw exception(PARAM_NOT_NULL, "提供方配置");
        }
        return providerCache.computeIfAbsent(provider.getId(), id -> createAdapter(provider));
    }

    @Override
    public LlmProviderPort getAvailableProvider(Long preferredProviderId) {
        if (preferredProviderId == null) {
            throw exception(PARAM_NOT_NULL, "提供方 ID");
        }
        // 优先使用指定的 Provider
        if (healthTracker.isHealthy(preferredProviderId)) {
            return getByProviderId(preferredProviderId);
        }
        log.warn("[getAvailableProvider][首选提供方不健康，查找备选，providerId={}]", preferredProviderId);
        LlmProvider preferred = llmProviderQueryPort.getWithDecryptedKey(preferredProviderId);
        if (preferred == null) {
            throw exception(LLM_PROVIDER_NOT_FOUND, preferredProviderId);
        }
        // 备选提供方必须属于首选提供方的用户。
        List<LlmProvider> activeProviders = llmProviderQueryPort.listActiveProviders(preferred.getUserId());
        for (LlmProvider candidate : activeProviders) {
            if (!preferredProviderId.equals(candidate.getId())
                    && healthTracker.isHealthy(candidate.getId())) {
                log.info("[getAvailableProvider][切换到备选提供方，providerId={}]", candidate.getId());
                return getByProvider(candidate);
            }
        }
        // 所有 Provider 都不健康，仍然尝试首选（可能冷却期已过）
        log.warn("[getAvailableProvider][无健康的备选提供方，尝试首选，providerId={}]", preferredProviderId);
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
            log.info("[evict][已移除 LLM 提供方缓存，providerId={}]", providerId);
        }
    }

    @Override
    public void evictAll() {
        providerCache.clear();
        log.info("[evictAll][已清空所有 LLM 提供方缓存]");
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
            throw exception(LLM_PROVIDER_NOT_FOUND, providerId);
        }
        if (!provider.isActive()) {
            throw exception(LLM_PROVIDER_DISABLED, providerId);
        }
        return createAdapter(provider);
    }

    /**
     * 根据提供方实体创建适配器实例。
     *
     * 当前所有提供方（OpenAI、DeepSeek、通义千问）均兼容 OpenAI 协议，
     * 统一使用 OpenAiCompatibleLlmAdapter。后续如有不兼容协议的提供方，
     * 可在此处按 providerType 分发到不同的适配器实现。
     *
     * @param provider 提供方实体
     * @return LLM 提供方端口实例
     */
    private LlmProviderPort createAdapter(LlmProvider provider) {
        return new OpenAiCompatibleLlmAdapter(provider);
    }
}
