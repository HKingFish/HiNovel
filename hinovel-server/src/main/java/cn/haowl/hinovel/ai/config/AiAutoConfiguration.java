package cn.haowl.hinovel.ai.config;

import cn.haowl.hinovel.ai.constant.AiConstants;
import cn.haowl.hinovel.ai.infrastructure.llm.DefaultLlmProviderFactory;
import cn.haowl.hinovel.ai.infrastructure.vector.QdrantVectorStoreFactory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * AI 模块自动配置类。
 *
 * <p>负责注册 AI 相关核心 Bean，包括 Qdrant 客户端、聊天记忆等。
 * 向量存储的动态管理由 {@link QdrantVectorStoreFactory} 负责，
 * LLM 模型的动态管理由 {@link DefaultLlmProviderFactory} 负责。</p>
 *
 * <p>Embedding 模型不再通过 yml 配置全局 Bean，而是由用户在数据库中配置后，
 * 由 {@link QdrantVectorStoreFactory} 按用户维度动态构建。</p>
 *
 * @author haowl
 */
@Slf4j
@Configuration
@ComponentScan(basePackages = "cn.haowl.hinovel.ai")
@MapperScan(basePackages = "cn.haowl.hinovel.ai.infrastructure.mapper")
public class AiAutoConfiguration {

    @Resource
    private AiProperties aiProperties;

    /**
     * 注册 Qdrant 客户端（gRPC 连接）。
     *
     * <p>客户端为单例，由 {@link QdrantVectorStoreFactory}
     * 统一管理各集合的向量存储实例，支持每本小说独立集合。</p>
     *
     * @return QdrantClient 实例
     */
    @Bean
    public QdrantClient qdrantClient() {
        AiProperties.Qdrant qdrant = aiProperties.getQdrant();
        return new QdrantClient(
                QdrantGrpcClient.newBuilder(qdrant.getHost(), qdrant.getPort(), false)
                        .build());
    }

    @Bean
    public ChatMemoryProvider chatMemoryProvider(RedisChatMemoryStore redisChatMemoryStore) {
        return memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(AiConstants.CHAT_MEMORY_MAX_MESSAGES)
                .chatMemoryStore(redisChatMemoryStore)
                .build();
    }
}
