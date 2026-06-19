package cn.haowl.hinovel.agent.application.service;

import cn.haowl.hinovel.agent.domain.entity.Agent;
import cn.haowl.hinovel.ai.application.llm.LlmProviderFactory;
import cn.haowl.hinovel.ai.application.llm.LlmProviderPort;
import dev.langchain4j.model.chat.StreamingChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequiredArgsConstructor
public class AgentExecutorService {

    private final LlmProviderFactory llmProviderFactory;

    /**
     * 根据 Agent 配置获取流式聊天模型。
     *
     * <p>通过工厂按提供方 ID 获取缓存实例，同一提供方配置复用同一模型客户端。</p>
     *
     * @param agent Agent 实体
     * @return 流式聊天模型实例
     */
    public StreamingChatModel buildStreamingModel(Agent agent) {
        LlmProviderPort providerPort = llmProviderFactory.getByProviderId(agent.getLlmProviderId());
        return providerPort.getStreamingChatModel();
    }

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
