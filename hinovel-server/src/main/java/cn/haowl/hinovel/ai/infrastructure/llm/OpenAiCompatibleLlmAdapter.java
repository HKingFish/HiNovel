package cn.haowl.hinovel.ai.infrastructure.llm;

import cn.haowl.hinovel.ai.application.llm.LlmProviderPort;
import cn.haowl.hinovel.ai.constant.AiConstants;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

/**
 * 兼容 OpenAI 协议的 LLM 适配器。
 *
 * <p>OpenAI、DeepSeek、通义千问等均兼容 OpenAI 协议，
 * 通过不同的 baseUrl 和 apiKey 即可接入，无需为每个提供商单独实现适配器。</p>
 *
 * @author haowl
 */
@Slf4j
public class OpenAiCompatibleLlmAdapter implements LlmProviderPort {

    private final StreamingChatModel streamingChatModel;
    private final ChatModel chatModel;
    private final String providerType;
    private final String modelName;

    public OpenAiCompatibleLlmAdapter(String providerType, String baseUrl,
                                      String apiKey, String modelName) {
        this.providerType = providerType;
        this.modelName = modelName;
        this.streamingChatModel = buildStreamingModel(baseUrl, apiKey, modelName);
        this.chatModel = buildChatModel(baseUrl, apiKey, modelName);
        log.info("LLM 适配器创建成功，提供方：{}，模型：{}", providerType, modelName);
    }

    @Override
    public StreamingChatModel getStreamingChatModel() {
        return streamingChatModel;
    }

    @Override
    public ChatModel getChatModel() {
        return chatModel;
    }

    @Override
    public String getProviderType() {
        return providerType;
    }

    @Override
    public String getModelName() {
        return modelName;
    }

    /**
     * 构建流式聊天模型。
     *
     * <p>流式模型不支持 maxRetries（流式连接中断后无法简单重试），
     * 仅配置超时时间。</p>
     *
     * @param baseUrl   API 基础 URL
     * @param apiKey    API 密钥
     * @param modelName 模型名称
     * @return 流式聊天模型实例
     */
    private StreamingChatModel buildStreamingModel(String baseUrl, String apiKey,
                                                   String modelName) {
        return OpenAiStreamingChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .timeout(Duration.ofSeconds(AiConstants.LLM_REQUEST_TIMEOUT_SECONDS))
                .build();
    }

    /**
     * 构建同步聊天模型。
     *
     * @param baseUrl   API 基础 URL
     * @param apiKey    API 密钥
     * @param modelName 模型名称
     * @return 同步聊天模型实例
     */
    private ChatModel buildChatModel(String baseUrl, String apiKey,
                                     String modelName) {
        return OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .timeout(Duration.ofSeconds(AiConstants.LLM_REQUEST_TIMEOUT_SECONDS))
                .build();
    }
}
