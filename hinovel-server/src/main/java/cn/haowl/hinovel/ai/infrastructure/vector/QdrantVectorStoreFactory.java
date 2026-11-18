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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

/**
 * Qdrant 向量存储工厂。
 *
 * 管理 {@link QdrantVectorStoreAdapter} 实例的生命周期，支持按集合名称动态创建并缓存实例，
 * 每本小说对应独立的 Qdrant Collection，实现数据隔离。
 *
 * 用户必须在数据库中配置 Embedding 模型后才能获取向量存储实例。
 * 未配置时 {@link #getByCollection(String, Long)} 返回 null，表示不启用 RAG 功能。
 *
 * @author haowl
 */
@Slf4j
@Component
public class QdrantVectorStoreFactory implements VectorStoreFactory {

    /**
     * 集合初始化最大重试次数。
     */
    private static final int MAX_RETRY_TIMES = 3;

    /**
     * 重试间隔（毫秒）。
     */
    private static final long RETRY_INTERVAL_MS = 2000L;

    /**
     * Qdrant 客户端。
     */
    @Autowired
    private QdrantClient qdrantClient;

    /**
     * 嵌入式模型配置应用服务。
     */
    @Autowired
    @Lazy
    private EmbeddingConfigService embeddingConfigService;

    /**
     * 按集合和用户保留当前配置的适配器。
     */
    private final ConcurrentHashMap<String, CachedStore> userStoreCache = new ConcurrentHashMap<>();

    @Override
    public VectorStorePort getByCollection(String collectionName, Long userId) {
        if (userId == null) {
            log.warn("[getByCollection][获取向量存储失败：userId 为空，集合：{}]", collectionName);
            return null;
        }
        String cacheKey = collectionName + ":" + userId;
        EmbeddingConfig userConfig = embeddingConfigService.findActiveByUserId(userId);
        if (userConfig == null) {
            userStoreCache.remove(cacheKey);
            log.info("[getByCollection][未配置 Embedding 模型，跳过 RAG，userId={}，集合={}]",
                userId, collectionName);
            return null;
        }
        return userStoreCache.compute(cacheKey, (key, current) -> {
            if (current != null && current.matches(userConfig)) {
                return current;
            }
            return new CachedStore(
                userConfig.getId(), userConfig.getVersion(), createAdapterWithUserConfig(collectionName, userConfig));
        }).store;
    }

    /**
     * 使用用户自定义 Embedding 配置创建向量存储适配器。
     *
     * @param collectionName 集合名称
     * @param userConfig     用户 Embedding 配置
     * @return 向量存储适配器
     */
    private VectorStorePort createAdapterWithUserConfig(String collectionName, EmbeddingConfig userConfig) {
        ensureCollectionExists(collectionName, userConfig.getDimensions());
        EmbeddingModel userEmbeddingModel = buildUserEmbeddingModel(userConfig);
        QdrantEmbeddingStore embeddingStore = QdrantEmbeddingStore.builder()
                .client(qdrantClient)
                .collectionName(collectionName)
                .build();
        log.info("[createAdapterWithUserConfig][创建 Qdrant 适配器，集合={}，userId={}，配置 ID={}]",
            collectionName, userConfig.getUserId(), userConfig.getId());
        return new QdrantVectorStoreAdapter(embeddingStore, userEmbeddingModel, collectionName);
    }

    /**
     * 根据用户 Embedding 配置构建 EmbeddingModel。
     *
     * @param userConfig 用户 Embedding 配置
     * @return EmbeddingModel 实例
     */
    private EmbeddingModel buildUserEmbeddingModel(EmbeddingConfig userConfig) {
        log.info("[buildUserEmbeddingModel][使用自定义 Embedding 模型，配置 ID={}，维度={}]",
            userConfig.getId(), userConfig.getDimensions());
        return OpenAiEmbeddingModel.builder()
            .apiKey(embeddingConfigService.decryptApiKey(userConfig.getApiKey()))
                .baseUrl(userConfig.getBaseUrl())
                .modelName(userConfig.getModelName())
                .dimensions(userConfig.getDimensions())
                .build();
    }

    /**
     * 确保 Qdrant 集合存在，不存在则自动创建（含重试机制）。
     *
     * 集合维度由用户 Embedding 配置决定，确保与用户使用的模型维度一致。
     *
     * @param collectionName 集合名称
     * @param dimensions     向量维度（来自用户 Embedding 配置）
     */
    private void ensureCollectionExists(String collectionName, int dimensions) {
        for (int attempt = 1; attempt <= MAX_RETRY_TIMES; attempt++) {
            try {
                if (!qdrantClient.collectionExistsAsync(collectionName).get()) {
                    qdrantClient.createCollectionAsync(
                            collectionName,
                            Collections.VectorParams.newBuilder()
                                .setSize(dimensions)
                                    .setDistance(Collections.Distance.Cosine)
                                .build()).get();
                    log.info("[ensureCollectionExists][Qdrant 集合创建成功，集合={}，维度={}]",
                        collectionName, dimensions);
                }
                return;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Qdrant 集合初始化被中断", e);
            } catch (ExecutionException e) {
                log.warn("[ensureCollectionExists][Qdrant 集合初始化失败（第 {}/{} 次），集合={}，异常类型={}]",
                    attempt, MAX_RETRY_TIMES, collectionName, e.getClass().getSimpleName());
                if (attempt >= MAX_RETRY_TIMES) {
                    log.error("[ensureCollectionExists][Qdrant 集合初始化重试耗尽，集合：{}]", collectionName);
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

    /**
     * 当前 Embedding 配置及对应的向量存储适配器。
     */
    @RequiredArgsConstructor
    private static class CachedStore {

        /**
         * 配置 ID。
         */
        private final Long configId;

        /**
         * 适配器创建时的配置版本。
         */
        private final Integer version;

        /**
         * 当前配置对应的向量存储适配器。
         */
        private final VectorStorePort store;

        /**
         * 判断适配器是否对应当前配置和版本。
         */
        private boolean matches(EmbeddingConfig config) {
            return Objects.equals(configId, config.getId()) && Objects.equals(version, config.getVersion());
        }
    }
}
