package cn.haowl.hinovel.ai.infrastructure.rag;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.aggregator.ContentAggregator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author : haowl
 * @Date : 2026/7/8 21:21
 * @Desc : 作者 Agent 辅助类
 */
@Component
@Slf4j
public class AuthorAgentAssister extends AgentAssister {

    @Override
    protected ContentAggregator buildContentAggregator() {

    }


    @Override
    protected ChatMessage buildContentInjector(List<Content> contents, ChatMessage userMessage) {
        // TODO :
//        if (contents.isEmpty()) {
//            return userMessage;
//        }
//        StringBuilder sb = new StringBuilder(((UserMessage) userMessage).singleText());
//        sb.append("\n\n请参考以下知识库内容作答：\n");
//        for (int i = 0; i < contents.size(); i++) {
//            sb.append(i + 1).append(". ").append(contents.get(i).textSegment().text()).append("\n");
//        }
//        log.info("userMessage={}", sb.toString());
//        return UserMessage.from(sb.toString());
    }
}
