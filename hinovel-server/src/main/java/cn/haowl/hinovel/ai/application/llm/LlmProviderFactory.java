package cn.haowl.hinovel.ai.application.llm;

import cn.haowl.hinovel.ai.domain.entity.LlmProvider;

/**
 * LLM 提供方工厂接口。
 *
 * <p>参考 VectorStoreFactory 设计理念，支持按提供方配置动态获取并缓存 LLM 实例，
 * 避免重复创建，同时支持配置变更后刷新实例。</p>
 *
 * @author haowl
 */
public interface LlmProviderFactory {

    /**
     * 根据数据库中的 LLM 提供方 ID 获取实例（自动查询配置并缓存）。
     *
     * @param providerId 提供方主键 ID
     * @return LLM 提供方端口实例
     */
    LlmProviderPort getByProviderId(Long providerId);

    /**
     * 根据 LLM 提供方实体直接获取实例（适用于已查询到配置的场景）。
     *
     * @param provider 提供方实体
     * @return LLM 提供方端口实例
     */
    LlmProviderPort getByProvider(LlmProvider provider);

    /**
     * 获取可用的 LLM 提供方实例（支持 failover）。
     *
     * <p>优先使用指定的 providerId，若该 Provider 不健康，
     * 自动切换到其他可用的 Provider，避免单点故障导致全站不可用。</p>
     *
     * @param preferredProviderId 优先使用的提供方 ID
     * @return 可用的 LLM 提供方端口实例
     */
    LlmProviderPort getAvailableProvider(Long preferredProviderId);

    /**
     * 记录 Provider 调用成功。
     *
     * @param providerId 提供方 ID
     */
    void reportSuccess(Long providerId);

    /**
     * 记录 Provider 调用失败。
     *
     * @param providerId 提供方 ID
     */
    void reportFailure(Long providerId);

    /**
     * 移除指定提供方的缓存实例（配置变更时调用，下次获取时重新创建）。
     *
     * @param providerId 提供方主键 ID
     */
    void evict(Long providerId);

    /**
     * 清空所有缓存的 LLM 实例。
     */
    void evictAll();
}
