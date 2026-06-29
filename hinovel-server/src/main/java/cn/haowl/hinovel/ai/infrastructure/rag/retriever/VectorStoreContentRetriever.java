package cn.haowl.hinovel.ai.infrastructure.rag.retriever;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author : haowl
 * @Date : 2026/6/29 21:35
 * @Desc : 向量存储内容检索器
 */
@Slf4j
@Component
@ConditionalOnBean(value = {EmbeddingModel.class, EmbeddingStore.class})
public class VectorStoreContentRetriever extends EmbeddingStoreContentRetriever {

    public VectorStoreContentRetriever(EmbeddingStore<TextSegment> embeddingStore,
                                       EmbeddingModel embeddingModel) {
        // TODO : 结果和分数是否要配置？
        super(embeddingStore, embeddingModel, 5, 0.6);
    }

}
