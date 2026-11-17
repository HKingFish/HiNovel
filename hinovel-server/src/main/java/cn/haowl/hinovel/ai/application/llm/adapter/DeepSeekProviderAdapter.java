package cn.haowl.hinovel.ai.application.llm.adapter;

import cn.haowl.hinovel.ai.application.llm.LlmProviderAdapter;
import cn.haowl.hinovel.ai.application.llm.LlmStreamingModelOptions;
import cn.haowl.hinovel.ai.constant.LlmProviderTypeEnum;
import dev.langchain4j.http.client.spring.restclient.SpringRestClientBuilderFactory;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * DeepSeek 流式聊天模型适配器。
 */
@Component
public class DeepSeekProviderAdapter implements LlmProviderAdapter {

    /**
     * DeepSeek 流式请求超时时间（秒）。
     */
    private static final int REQUEST_TIMEOUT_SECONDS = 60;

    @Override
    public LlmProviderTypeEnum supportedProviderType() {
        return LlmProviderTypeEnum.DEEPSEEK;
    }

    @Override
    public StreamingChatModel buildStreamingModel(LlmStreamingModelOptions options) {
        return OpenAiStreamingChatModel.builder()
            .baseUrl(options.getBaseUrl())
            .apiKey(options.getApiKey())
            .modelName(options.getModelName())
            .temperature(options.getTemperature())
            .maxTokens(options.getMaxTokens())
            .topP(options.getTopP())
            .timeout(Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS))
                .httpClientBuilder(new SpringRestClientBuilderFactory().create())
                .build();
    }
}
