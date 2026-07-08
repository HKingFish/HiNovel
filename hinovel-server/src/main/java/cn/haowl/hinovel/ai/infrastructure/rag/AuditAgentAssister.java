package cn.haowl.hinovel.ai.infrastructure.rag;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.aggregator.ContentAggregator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author : haowl
 * @Date : 2026/7/8 21:35
 * @Desc : 审核 Agent 辅助类
 */
@Component
@Slf4j
public class AuditAgentAssister extends AgentAssister {

    @Override
    protected ContentAggregator buildContentAggregator() {

    }


    @Override
    protected ChatMessage buildContentInjector(List<Content> contents, ChatMessage userMessage) {

    }
}
