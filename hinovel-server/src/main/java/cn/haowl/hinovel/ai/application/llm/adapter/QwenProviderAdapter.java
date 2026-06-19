package cn.haowl.hinovel.ai.application.llm.adapter;

import cn.haowl.hinovel.ai.application.llm.LlmProviderAdapter;
import cn.haowl.hinovel.ai.constant.AiConstants;
import cn.haowl.hinovel.ai.constant.LlmProviderType;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnClass(name = "dev.langchain4j.community.model.dashscope.QwenStreamingChatModel")
public class QwenProviderAdapter implements LlmProviderAdapter {

    @Override
    public String supportedProviderType() {
        return LlmProviderType.QWEN.name();
    }

    @Override
    public StreamingChatModel buildStreamingModel(String baseUrl, String apiKey,
                                                  String modelName, double temperature,
                                                  int maxTokens, double topP) {
        String effectiveBaseUrl = (baseUrl != null && !baseUrl.isBlank())
                ? baseUrl : AiConstants.QWEN_COMPATIBLE_BASE_URL;

        return OpenAiStreamingChatModel
                .builder()
                .apiKey(System.getenv(AiConstants.QWEN_ENV_API_KEY))
                .modelName(AiConstants.QWEN_DEFAULT_MODEL)
                .baseUrl(AiConstants.QWEN_COMPATIBLE_BASE_URL)
                .build();
    }
}
