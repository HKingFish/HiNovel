package cn.haowl.hinovel.agent.application.service;

import cn.haowl.hinovel.agent.domain.entity.Agent;
import cn.haowl.hinovel.ai.application.llm.LlmProviderFactory;
import cn.haowl.hinovel.ai.application.llm.LlmProviderPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Agent 执行服务。
 *
 * <p>根据 Agent 配置，通过 {@link LlmProviderFactory} 动态获取缓存的 LLM 实例，
 * 避免每次请求重复创建模型客户端。</p>
 *
 * @author haowl
 */
@Slf4j
@Service
public class AgentExecutorService {

    /** 按 Agent 配置获取缓存的模型实例。 */
    @Autowired
    private LlmProviderFactory llmProviderFactory;

    /**
     * 根据 Agent 配置获取 LLM 提供方端口（需要同时使用流式和同步模型时使用）。
     *
     * @param agent Agent 实体
     * @return LLM 提供方端口实例
     */
    public LlmProviderPort getLlmProvider(Agent agent) {
        return llmProviderFactory.getByProviderId(agent.getLlmProviderId());
    }
}
