package cn.haowl.hinovel.ai.infrastructure.rag;

import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(AuthorAgentV2.class)
    public AuthorAgentV2 authorAgent() {
        return AiServices.builder(AuthorAgentV2.class)
            .chatModel(authorAgentAssister.chatModel())
            .chatMemoryProvider(authorAgentAssister.chatMemoryProvider())
            .retrievalAugmentor(authorAgentAssister.retrievalAugmentor())
            .toolProvider(authorAgentAssister.toolProvider())
            .build();
    }


    /**
     * 审核智能体 Agent
     * - ContentAggregator：LLM 打分重排序
     * - ContentInjector：将检索内容以带序号格式注入 UserMessage
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(AuditAgentV2.class)
    public AuditAgentV2 auditAgentV2() {
        return AiServices.builder(AuditAgentV2.class)
            .chatModel(auditAgentAssister.chatModel())
            .chatMemoryProvider(auditAgentAssister.chatMemoryProvider())
            .retrievalAugmentor(auditAgentAssister.retrievalAugmentor())
            .toolProvider(auditAgentAssister.toolProvider())
            .build();
    }


    public interface AuthorAgentV2 {

    }


    public interface AuditAgentV2 {

    }
}
