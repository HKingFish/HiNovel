package cn.haowl.hinovel.ai.infrastructure.rag;

import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author : haowl
 * @Date : 2026/6/29 21:44
 * @Desc :
 */
@Slf4j
@Configuration
public class AgentConfig {


    @Resource
    private AuthorAgentAssister authorAgentAssister;

    @Resource
    private AuditAgentAssister auditAgentAssister;
    // TODO : 待定，不应放在该目录下

    /**
     * 作者智能体 Agent
     * - ContentAggregator：LLM 打分重排序
     * - ContentInjector：将检索内容以带序号格式注入 UserMessage
     *
     * @param chatModel
     * @param chatMemoryProvider
     * @return
     */
    @Bean
    public AuthorAgentV2 authorAgent(ChatModel chatModel, ChatMemoryProvider chatMemoryProvider) {
        return AiServices.builder(AuthorAgentV2.class)
            .chatModel(chatModel)
            .chatMemoryProvider(chatMemoryProvider)
            .retrievalAugmentor(authorAgentAssister.buildRetrievalAugmentor())
            .toolProvider(authorAgentAssister.buildToolProvider())
            .build();
    }


    /**
     * 审核智能体 Agent
     * - ContentAggregator：LLM 打分重排序
     * - ContentInjector：将检索内容以带序号格式注入 UserMessage
     *
     * @param chatModel
     * @param chatMemoryProvider
     * @return
     */
    @Bean
    public AuditAgentV2 auditAgentV2(ChatModel chatModel, ChatMemoryProvider chatMemoryProvider) {
        return AiServices.builder(AuditAgentV2.class)
            .chatModel(chatModel)
            .chatMemoryProvider(chatMemoryProvider)
            .retrievalAugmentor(auditAgentAssister.buildRetrievalAugmentor())
            .toolProvider(auditAgentAssister.buildToolProvider())
            .build();
    }


    public interface AuthorAgentV2 {

    }


    public interface AuditAgentV2 {

    }
}
