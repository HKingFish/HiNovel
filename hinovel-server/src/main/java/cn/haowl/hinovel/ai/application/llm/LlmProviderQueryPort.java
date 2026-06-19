package cn.haowl.hinovel.ai.application.llm;

import cn.haowl.hinovel.ai.domain.entity.LlmProvider;

import java.util.List;

/**
 * LLM Provider 查询端口接口。
 *
 * <p>定义了 LLM 提供方配置的查询操作，作为应用层与基础设施层的桥梁，
 * 用于获取 LLM 提供方的配置信息。</p>
 *
 * @author haowl
 * @since 2024
 */
public interface LlmProviderQueryPort {

    /**
     * 根据ID获取 LLM Provider 配置（包含解密的 API Key）。
     *
     * @param id LLM Provider 的主键ID
     * @return LLM Provider 实体对象，如果ID为空则返回 null
     */
    LlmProvider getWithDecryptedKey(Long id);

    /**
     * 查询所有已激活的 LLM 提供方（包含解密的 API Key）。
     *
     * <p>用于 failover 场景，当首选 Provider 不可用时，
     * 从激活列表中选择备选 Provider。</p>
     *
     * @return 已激活的提供方列表
     */
    List<LlmProvider> listActiveProviders();
}
