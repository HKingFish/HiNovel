package cn.haowl.hinovel.ai.config;

import cn.haowl.hinovel.ai.constant.AiConstants;
import cn.haowl.hinovel.ai.constant.EmbeddingModelType;
import cn.haowl.hinovel.ai.infrastructure.llm.DefaultLlmProviderFactory;
import cn.haowl.hinovel.ai.infrastructure.vector.QdrantVectorStoreFactory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.OnnxEmbeddingModel;
import dev.langchain4j.model.embedding.onnx.PoolingMode;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * AI 模块自动配置类。
 *
 * <p>负责注册 AI 相关核心 Bean，包括 Qdrant 客户端、嵌入模型等。
 * 向量存储的动态管理由 {@link QdrantVectorStoreFactory} 负责，
 * LLM 模型的动态管理由 {@link DefaultLlmProviderFactory} 负责。</p>
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

    @Resource
    private EmbeddingProperties embeddingProperties;

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

    /**
     * 注册嵌入模型，根据配置类型动态选择实现。
     *
     * <p>支持两种模式：
     * <ul>
     *   <li>onnx：本地 ONNX 模型（如 BGE-M3），进程内推理，无需网络调用</li>
     *   <li>openai：OpenAI 兼容接口（阿里云百炼 text-embedding-v3 等远程服务）</li>
     * </ul>
     * 标记为 @Primary，优先于 langchain4j-open-ai-spring-boot-starter 自动配置的 Bean。</p>
     *
     * @return EmbeddingModel 实例
     * @see EmbeddingModelType
     */
    @Bean
    @Primary
    public EmbeddingModel embeddingModel() {
        String type = embeddingProperties.getType();
        if (EmbeddingModelType.ONNX.getValue().equals(type)) {
            return createOnnxEmbeddingModel();
        }
        return createOpenAiEmbeddingModel();
    }

    /**
     * 创建 OpenAI 兼容接口的嵌入模型实例。
     *
     * @return OpenAiEmbeddingModel 实例
     */
    private EmbeddingModel createOpenAiEmbeddingModel() {
        EmbeddingProperties.OpenAi openaiConfig = embeddingProperties.getOpenai();
        log.info("初始化 OpenAI 兼容 Embedding 模型，模型：{}，维度：{}",
                openaiConfig.getModelName(), openaiConfig.getDimensions());
        return OpenAiEmbeddingModel.builder()
                .apiKey(openaiConfig.getApiKey())
                .baseUrl(openaiConfig.getBaseUrl())
                .modelName(openaiConfig.getModelName())
                .dimensions(openaiConfig.getDimensions())
                .build();
    }

    /**
     * 创建本地 ONNX 嵌入模型实例（如 BGE-M3）。
     *
     * <p>需要预先下载 ONNX 模型文件和分词器文件到本地路径，
     * BGE-M3 使用 CLS Pooling 模式。</p>
     *
     * @return OnnxEmbeddingModel 实例
     */
    private EmbeddingModel createOnnxEmbeddingModel() {
        EmbeddingProperties.Onnx onnxConfig = embeddingProperties.getOnnx();
        log.info("初始化本地 ONNX Embedding 模型，模型路径：{}，分词器路径：{}，维度：{}",
                onnxConfig.getModelPath(), onnxConfig.getTokenizerPath(), onnxConfig.getDimensions());
        return new OnnxEmbeddingModel(
                onnxConfig.getModelPath(),
                onnxConfig.getTokenizerPath(),
                PoolingMode.CLS
        );
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
