package cn.haowl.hinovel.ai.application.vector;

/**
 * 向量存储工厂接口。
 *
 * <p>支持按集合名称动态获取向量存储实例，每本小说对应独立的向量集合，实现数据隔离。
 * 用户必须在数据库中配置 Embedding 模型后才能获取向量存储实例，未配置时返回 null。</p>
 *
 * @author haowl
 */
public interface VectorStoreFactory {

    /**
     * 根据集合名称和用户 ID 获取向量存储实例。
     *
     * <p>使用用户在数据库中配置的 Embedding 模型。若用户未配置 Embedding 模型，
     * 返回 null 表示不启用 RAG 功能。</p>
     *
     * @param collectionName 集合名称（如 novel_123）
     * @param userId         用户 ID
     * @return 向量存储端口；用户未配置 Embedding 模型时返回 null
     */
    VectorStorePort getByCollection(String collectionName, Long userId);
}
