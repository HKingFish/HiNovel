package cn.haowl.hinovel.ai.infrastructure.vector;

import cn.haowl.hinovel.ai.application.vector.VectorStorePort;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.filter.MetadataFilterBuilder;
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Qdrant 向量存储适配器。
 *
 * 基于 LangChain4J 的 {@link QdrantEmbeddingStore} 实现 {@link VectorStorePort}，
 * 封装 Qdrant 的向量存储与检索操作，后续替换向量数据库只需新增适配器实现即可。
 *
 * @author haowl
 */
@Slf4j
public class QdrantVectorStoreAdapter implements VectorStorePort {

    /**
     * 当前集合的向量存储。
     */
    private final QdrantEmbeddingStore embeddingStore;

    /**
     * 用于生成向量的嵌入模型。
     */
    private final EmbeddingModel embeddingModel;

    /**
     * 当前 Qdrant 集合名称。
     */
    private final String collectionName;

    /**
     * 初始化指定集合的向量存储适配器。
     */
    public QdrantVectorStoreAdapter(
        QdrantEmbeddingStore embeddingStore, EmbeddingModel embeddingModel, String collectionName) {
        this.embeddingStore = embeddingStore;
        this.embeddingModel = embeddingModel;
        this.collectionName = collectionName;
    }

    @Override
    public void store(List<TextSegment> segments) {
        if (segments == null || segments.isEmpty()) {
            return;
        }
        // 批量向量化后存储，减少网络往返次数
        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
        embeddingStore.addAll(embeddings, segments);
        log.info("[store][向量存储成功，集合：{}，切片数量：{}]", collectionName, segments.size());
    }

    @Override
    public EmbeddingSearchResult<TextSegment> search(EmbeddingSearchRequest request) {
        return embeddingStore.search(request);
    }

    @Override
    public EmbeddingSearchResult<TextSegment> search(String query, int topK) {
        return embeddingStore.search(EmbeddingSearchRequest.builder()
            .queryEmbedding(embeddingModel.embed(query).content())
                .maxResults(topK)
            .build());
    }

    @Override
    public void delete(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        embeddingStore.removeAll(ids);
        log.info("[delete][向量删除成功，集合：{}，ID 数量：{}]", collectionName, ids.size());
    }

    @Override
    public void clear() {
        embeddingStore.removeAll();
        log.info("[clear][向量集合已清空，集合：{}]", collectionName);
    }

    @Override
    public void deleteByMetadata(String key, String value) {
        embeddingStore.removeAll(MetadataFilterBuilder.metadataKey(key).isEqualTo(value));
        log.info("[deleteByMetadata][按元数据删除向量成功，集合={}]", collectionName);
    }

    @Override
    public String getCollectionName() {
        return collectionName;
    }
}
