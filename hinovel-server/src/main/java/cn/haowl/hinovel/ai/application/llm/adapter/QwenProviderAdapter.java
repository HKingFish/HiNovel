package cn.haowl.hinovel.ai.application.llm.adapter;

import cn.haowl.hinovel.ai.application.llm.LlmProviderAdapter;
import cn.haowl.hinovel.ai.application.llm.LlmStreamingModelOptions;
import cn.haowl.hinovel.ai.constant.AiConstants;
import cn.haowl.hinovel.ai.constant.LlmProviderTypeEnum;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

/**
 * 通义千问流式聊天模型适配器。
 */
@Component
@ConditionalOnClass(name = "dev.langchain4j.community.model.dashscope.QwenStreamingChatModel")
public class QwenProviderAdapter implements LlmProviderAdapter {

    @Override
    public LlmProviderTypeEnum supportedProviderType() {
        return LlmProviderTypeEnum.QWEN;
    }

    @Override
    public StreamingChatModel buildStreamingModel(LlmStreamingModelOptions options) {
        // 通义千问适配器使用部署环境账号和预设模型。
        return OpenAiStreamingChatModel.builder()
                .apiKey(System.getenv(AiConstants.QWEN_ENV_API_KEY))
                .modelName(AiConstants.QWEN_DEFAULT_MODEL)
                .baseUrl(AiConstants.QWEN_COMPATIBLE_BASE_URL)
                .build();
    }
}
