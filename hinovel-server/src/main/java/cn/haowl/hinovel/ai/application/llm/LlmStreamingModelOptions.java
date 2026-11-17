package cn.haowl.hinovel.ai.application.llm;

import lombok.Builder;
import lombok.Getter;

/**
 * @author : haowl
 * @date : 2026/11/17 21:05
 * @desc : 构建流式聊天模型的连接和采样参数
 */
@Getter
@Builder
public class LlmStreamingModelOptions {

    /**
     * API 基础 URL。
     */
    private final String baseUrl;

    /**
     * API 密钥。
     */
    private final String apiKey;

    /**
     * 模型名称。
     */
    private final String modelName;

    /**
     * 采样温度。
     */
    private final double temperature;

    /**
     * 最大输出 Token 数。
     */
    private final int maxTokens;

    /**
     * Top-P 采样参数。
     */
    private final double topP;
}
