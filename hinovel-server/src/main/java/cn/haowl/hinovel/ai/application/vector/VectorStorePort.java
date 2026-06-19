package cn.haowl.hinovel.ai.application.vector;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;

import java.util.List;

/**
 * 向量存储端口接口。
 *
 * <p>定义向量数据库的核心操作，屏蔽底层实现细节，支持后续替换为其他向量数据库（如 Milvus、Weaviate 等）。</p>
 *
 * @author haowl
 */
public interface VectorStorePort {

    /**
     * 将文档切片向量化并存储。
     *
     * @param segments 文本切片列表
     */
    void store(List<TextSegment> segments);

    /**
     * 语义搜索：根据查询文本返回最相似的文本切片。
     *
     * @param request 搜索请求（含查询向量、topK、最低分数等参数）
     * @return 搜索结果
     */
    EmbeddingSearchResult<TextSegment> search(EmbeddingSearchRequest request);

    /**
     * 根据查询文本进行语义搜索（便捷方法）。
     *
     * @param query  查询文本
     * @param topK   返回最相似结果数量
     * @return 搜索结果
     */
    EmbeddingSearchResult<TextSegment> search(String query, int topK);

    /**
     * 删除指定 ID 的向量数据。
     *
     * @param ids 向量 ID 列表
     */
    void delete(List<String> ids);

    /**
     * 清空当前集合中的所有向量数据。
     */
    void clear();

    /**
     * 根据 metadata 过滤条件删除向量数据。
     *
     * @param key   metadata 键
     * @param value metadata 值
     */
    void deleteByMetadata(String key, String value);

    /**
     * 获取当前向量存储对应的集合名称。
     *
     * @return 集合名称
     */
    String getCollectionName();
}
