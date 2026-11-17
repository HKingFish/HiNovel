package cn.haowl.hinovel.ai.application.llm.adapter;

import cn.haowl.hinovel.ai.application.llm.LlmProviderAdapter;
import cn.haowl.hinovel.ai.application.llm.LlmStreamingModelOptions;
import cn.haowl.hinovel.ai.constant.AiConstants;
import cn.haowl.hinovel.ai.constant.LlmProviderTypeEnum;
import dev.langchain4j.http.client.spring.restclient.SpringRestClientBuilderFactory;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * OpenAI 流式聊天模型适配器。
 */
@Component
public class OpenAiProviderAdapter implements LlmProviderAdapter {

    @Override
    public LlmProviderTypeEnum supportedProviderType() {
        return LlmProviderTypeEnum.OPENAI;
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
                .timeout(Duration.ofSeconds(AiConstants.LLM_REQUEST_TIMEOUT_SECONDS))
                .httpClientBuilder(new SpringRestClientBuilderFactory().create())
                .build();
    }
}
