package cn.haowl.hinovel.ai.infrastructure.vector;

import cn.haowl.hinovel.ai.application.embedding.EmbeddingConfigService;
import cn.haowl.hinovel.ai.application.vector.VectorStoreFactory;
import cn.haowl.hinovel.ai.application.vector.VectorStorePort;
import cn.haowl.hinovel.ai.domain.entity.EmbeddingConfig;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Qdrant 向量存储工厂。
 *
 * <p>管理 {@link QdrantVectorStoreAdapter} 实例的生命周期，支持按集合名称动态创建并缓存实例，
 * 每本小说对应独立的 Qdrant Collection，实现数据隔离。</p>
 *
 * <p>用户必须在数据库中配置 Embedding 模型后才能获取向量存储实例。
 * 未配置时 {@link #getByCollection(String, Long)} 返回 null，表示不启用 RAG 功能。</p>
 *
 * @author haowl
 */
@Slf4j
@Component
public class QdrantVectorStoreFactory implements VectorStoreFactory {

    private final QdrantClient qdrantClient;
    private final EmbeddingConfigService embeddingConfigService;

    /**
     * 集合名称:userId -> 适配器实例缓存（用户自定义模型），避免重复创建
     */
    private final ConcurrentHashMap<String, VectorStorePort> userStoreCache = new ConcurrentHashMap<>();

    public QdrantVectorStoreFactory(QdrantClient qdrantClient,
                                    @Lazy EmbeddingConfigService embeddingConfigService) {
        this.qdrantClient = qdrantClient;
        this.embeddingConfigService = embeddingConfigService;
    }

    @Override
    public VectorStorePort getByCollection(String collectionName, Long userId) {
        if (userId == null) {
            log.warn("获取向量存储失败：userId 为空，集合：{}", collectionName);
            return null;
        }
        // 查询用户激活的 Embedding 配置
        EmbeddingConfig userConfig = embeddingConfigService.findActiveByUserId(userId);
        if (userConfig == null) {
            // 用户未配置 Embedding 模型，不启用 RAG
            log.info("用户 {} 未配置 Embedding 模型，不启用 RAG 功能，集合：{}", userId, collectionName);
            return null;
        }
        // 用户有自定义配置，按 collectionName:userId 缓存
        String cacheKey = collectionName + ":" + userId;
        return userStoreCache.computeIfAbsent(cacheKey,
                k -> createAdapterWithUserConfig(collectionName, userConfig));
    }

    /**
     * 使用用户自定义 Embedding 配置创建向量存储适配器。
     *
     * @param collectionName 集合名称
     * @param userConfig     用户 Embedding 配置
     * @return 向量存储适配器
     */
    private VectorStorePort createAdapterWithUserConfig(String collectionName,
                                                         EmbeddingConfig userConfig) {
        ensureCollectionExists(collectionName, userConfig.getDimensions());
        EmbeddingModel userEmbeddingModel = buildUserEmbeddingModel(userConfig);
        QdrantEmbeddingStore embeddingStore = QdrantEmbeddingStore.builder()
                .client(qdrantClient)
                .collectionName(collectionName)
                .build();
        log.info("创建用户自定义 Qdrant 向量存储适配器，集合：{}，userId={}，模型：{}",
                collectionName, userConfig.getUserId(), userConfig.getModelName());
        return new QdrantVectorStoreAdapter(embeddingStore, userEmbeddingModel, collectionName);
    }

    /**
     * 根据用户 Embedding 配置构建 EmbeddingModel。
     *
     * @param userConfig 用户 Embedding 配置
     * @return EmbeddingModel 实例
     */
    private EmbeddingModel buildUserEmbeddingModel(EmbeddingConfig userConfig) {
        String decryptedKey = embeddingConfigService.decryptApiKey(userConfig.getApiKey());
        log.info("使用用户自定义 Embedding 模型，模型：{}，维度：{}",
                userConfig.getModelName(), userConfig.getDimensions());
        return OpenAiEmbeddingModel.builder()
                .apiKey(decryptedKey)
                .baseUrl(userConfig.getBaseUrl())
                .modelName(userConfig.getModelName())
                .dimensions(userConfig.getDimensions())
                .build();
    }

    /** 集合初始化最大重试次数 */
    private static final int MAX_RETRY_TIMES = 3;

    /** 重试间隔（毫秒） */
    private static final long RETRY_INTERVAL_MS = 2000L;

    /**
     * 确保 Qdrant 集合存在，不存在则自动创建（含重试机制）。
     *
     * <p>集合维度由用户 Embedding 配置决定，确保与用户使用的模型维度一致。</p>
     *
     * @param collectionName 集合名称
     * @param dimensions     向量维度（来自用户 Embedding 配置）
     */
    private void ensureCollectionExists(String collectionName, int dimensions) {
        for (int attempt = 1; attempt <= MAX_RETRY_TIMES; attempt++) {
            try {
                boolean exists = qdrantClient.collectionExistsAsync(collectionName).get();
                if (!exists) {
                    qdrantClient.createCollectionAsync(
                            collectionName,
                            Collections.VectorParams.newBuilder()
                                .setSize(dimensions)
                                    .setDistance(Collections.Distance.Cosine)
                                    .build()
                    ).get();
                    log.info("Qdrant 集合创建成功，集合：{}，维度：{}", collectionName, dimensions);
                }
                return;
            } catch (Exception e) {
                log.warn("Qdrant 集合检查/创建失败（第 {}/{} 次），集合：{}，异常：{}",
                        attempt, MAX_RETRY_TIMES, collectionName, e.getMessage());
                if (attempt >= MAX_RETRY_TIMES) {
                    log.error("Qdrant 集合初始化重试耗尽，集合：{}", collectionName);
                    throw new RuntimeException("Qdrant 集合初始化失败：" + collectionName, e);
                }
                try {
                    Thread.sleep(RETRY_INTERVAL_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Qdrant 集合初始化被中断：" + collectionName, ie);
                }
            }
        }
    }
}
