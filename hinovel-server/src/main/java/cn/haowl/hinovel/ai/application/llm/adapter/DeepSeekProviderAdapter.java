package cn.haowl.hinovel.ai.application.llm.adapter;

import cn.haowl.hinovel.ai.application.llm.LlmProviderAdapter;
import cn.haowl.hinovel.ai.constant.LlmProviderType;
import dev.langchain4j.http.client.spring.restclient.SpringRestClientBuilderFactory;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class DeepSeekProviderAdapter implements LlmProviderAdapter {

    @Override
    public String supportedProviderType() {
        return LlmProviderType.DEEPSEEK.name();
    }

    @Override
    public StreamingChatModel buildStreamingModel(String baseUrl, String apiKey,
                                                  String modelName, double temperature,
                                                  int maxTokens, double topP) {
        return OpenAiStreamingChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .topP(topP)
                .timeout(Duration.ofSeconds(60))
                .httpClientBuilder(new SpringRestClientBuilderFactory().create())
                .build();
    }
}
