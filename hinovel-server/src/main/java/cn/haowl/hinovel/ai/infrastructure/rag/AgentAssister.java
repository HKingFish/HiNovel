package cn.haowl.hinovel.ai.infrastructure.rag;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.aggregator.ContentAggregator;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.service.tool.ToolProvider;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * @Author : haowl
 * @Date : 2026/7/8 21:13
 * @Desc :
 */
public abstract class AgentAssister {

    // TODO : 是否要补个上下文？

    // TODO : 填充或校验默认值

    /**
     * 构建查询转化器
     *
     * @param query
     * @return
     */
    protected Collection<Query> buildQueryTransformer(Query query) {
        return null;
    }

    /**
     * 意图识别，路由转换
     *
     * @param query
     * @return
     */
    protected Collection<ContentRetriever> buildQueryRouter(Query query) {
        return null;
    }


    /**
     * 构建内容聚合器
     *
     * @return
     */
    protected ContentAggregator buildContentAggregator() {
        return null;
    }


    /**
     * 构建内容注入器
     *
     * @param contents
     * @param userMessage
     * @return
     */
    protected ChatMessage buildContentInjector(List<Content> contents, ChatMessage userMessage) {
        return null;
    }

    /**
     * 构建执行器
     *
     * @return
     */
    protected Executor buildExecutor() {
        return null;
    }


    /**
     * 构建检索增强器
     *
     * @return
     */
    public RetrievalAugmentor retrievalAugmentor() {
        return DefaultRetrievalAugmentor.builder()
            // 1. QueryTransformer：短查询补充"产品"关键词，提升向量检索召回率
            .queryTransformer(this::buildQueryTransformer)
            // 2. QueryRouter：根据用户意图路由到对应的 ContentRetriever
            .queryRouter(this::buildQueryRouter)
            // 3. ContentAggregator：LLM 打分重排序
            .contentAggregator(buildContentAggregator())
            // 4. ContentInjector：将检索内容以带序号格式注入 UserMessage
            .contentInjector(this::buildContentInjector)
            // 5. Executor：多路检索时可指定线程池并行执行，降低延迟
            .executor(buildExecutor())
            .build();
    }


    /**
     * 构建工具提供器
     *
     * @return
     */
    public ToolProvider toolProvider() {
        return null;
    }


    /**
     * 模型
     *
     * @return
     */
    public abstract ChatModel chatModel();

    /**
     * 内存提供器
     *
     * @return
     */
    public ChatMemoryProvider chatMemoryProvider() {
        return null;
    }
}
