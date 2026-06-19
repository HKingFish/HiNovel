package cn.haowl.hinovel.ai.application.llm;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;

/**
 * LLM 提供方端口接口。
 *
 * <p>定义 LLM 模型的统一访问规范，屏蔽底层提供商差异，
 * 支持后续替换为其他 LLM 提供商（如 Anthropic、Gemini 等）。</p>
 *
 * @author haowl
 */
public interface LlmProviderPort {

    /**
     * 获取流式聊天模型。
     *
     * @return 流式聊天模型实例
     */
    StreamingChatModel getStreamingChatModel();

    /**
     * 获取同步聊天模型（部分场景需要同步调用）。
     *
     * @return 同步聊天模型实例，不支持时返回 null
     */
    ChatModel getChatModel();

    /**
     * 获取提供方类型标识。
     *
     * @return 提供方类型，如 "OPENAI"、"DEEPSEEK"、"QWEN"
     */
    String getProviderType();

    /**
     * 获取当前使用的模型名称。
     *
     * @return 模型名称
     */
    String getModelName();
}
