package cn.haowl.hinovel.ai.application.llm;

import cn.haowl.hinovel.ai.constant.LlmProviderType;
import dev.langchain4j.model.chat.StreamingChatModel;

/**
 * LLM Provider 适配器接口。
 *
 * <p>定义了不同 LLM 提供商（如 OpenAI、DeepSeek 等）的适配器规范，
 * 用于统一构建各提供商的流式聊天模型。</p>
 *
 * @author haowl
 * @since 2024
 */
public interface LlmProviderAdapter {

    /**
     * 获取该适配器支持的提供方类型。
     *
     * @return 提供方类型标识，如 "OPENAI"、"DEEPSEEK" 等
     * @see LlmProviderType
     */
    String supportedProviderType();

    /**
     * 构建流式聊天模型。
     *
     * <p>根据提供的配置参数创建对应 LLM 提供商的流式聊天模型实例。</p>
     *
     * @param baseUrl     API 基础 URL
     * @param apiKey      API 密钥
     * @param modelName   模型名称
     * @param temperature 温度参数，控制输出的随机性
     * @param maxTokens   最大生成 Token 数
     * @param topP        Top-P 采样参数
     * @return 配置好的流式聊天模型实例
     */
    StreamingChatModel buildStreamingModel(String baseUrl, String apiKey,
                                           String modelName, double temperature,
                                           int maxTokens, double topP);
}
