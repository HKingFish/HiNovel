package cn.haowl.hinovel.ai.infrastructure.rag.retriever;

import cn.hutool.core.lang.Assert;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;

import java.util.List;

/**
 * @Author : haowl
 * @Date : 2026/6/29 20:46
 * @Desc : 混合内容检索器 （多个检索器组合）
 */
public class ComplexContentRetriever implements ContentRetriever {

    private final List<ContentRetriever> contentRetrievers;

    public ComplexContentRetriever(List<ContentRetriever> contentRetrievers) {
        Assert.notEmpty(contentRetrievers);
        this.contentRetrievers = contentRetrievers;
    }

    @Override
    public List<Content> retrieve(Query query) {
        return contentRetrievers
            .stream()
            .flatMap(c -> c.retrieve(query).stream())
            .toList();
    }
}
