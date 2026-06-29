package cn.haowl.hinovel.ai.infrastructure.rag.retriever;

import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;

import java.util.List;

/**
 * @Author : haowl
 * @Date : 2026/6/29 20:46
 * @Desc : 关键字检索 bm2.5 实现
 */
public class KeywordContentRetriever implements ContentRetriever {
    @Override
    public List<Content> retrieve(Query query) {
        // TODO :
        return List.of();
    }
}
