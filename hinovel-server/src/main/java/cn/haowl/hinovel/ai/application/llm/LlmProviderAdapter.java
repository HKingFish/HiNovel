package cn.haowl.hinovel.ai.application.llm;

import cn.haowl.hinovel.ai.constant.LlmProviderTypeEnum;
import dev.langchain4j.model.chat.StreamingChatModel;

/**
 * LLM Provider 适配器接口。
 *
 * 定义了不同 LLM 提供商（如 OpenAI、DeepSeek 等）的适配器规范，
 * 用于统一构建各提供商的流式聊天模型。
 *
 * @author haowl
 * @since 2024
 */
public interface LlmProviderAdapter {

    /**
     * 获取该适配器支持的提供方类型。
     *
     * @return 提供方类型
     */
    LlmProviderTypeEnum supportedProviderType();

    /**
     * 构建流式聊天模型。
     *
     * 根据提供的配置参数创建对应 LLM 提供商的流式聊天模型实例。
     *
     * @param options 连接和采样参数
     * @return 配置好的流式聊天模型实例
     */
    StreamingChatModel buildStreamingModel(LlmStreamingModelOptions options);
}
