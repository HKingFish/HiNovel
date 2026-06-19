package cn.haowl.hinovel.ai.application.vector;

/**
 * 向量存储工厂接口。
 *
 * <p>支持按集合名称动态获取向量存储实例，每本小说对应独立的向量集合，实现数据隔离。</p>
 *
 * @author haowl
 */
public interface VectorStoreFactory {


    /**
     * 根据集合名称获取向量存储实例（不存在时自动创建集合）。
     *
     * @param collectionName 集合名称（如小说 ID）
     * @return 向量存储端口
     */
    VectorStorePort getByCollection(String collectionName);

    /**
     * 根据集合名称和用户 ID 获取向量存储实例。
     *
     * <p>优先使用用户自定义的 Embedding 配置，若未配置则 fallback 到系统默认配置。</p>
     *
     * @param collectionName 集合名称
     * @param userId         用户 ID
     * @return 向量存储端口
     */
    default VectorStorePort getByCollection(String collectionName, Long userId) {
        return getByCollection(collectionName);
    }
}
