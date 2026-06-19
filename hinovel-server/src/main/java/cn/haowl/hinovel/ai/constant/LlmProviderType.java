package cn.haowl.hinovel.ai.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * LLM 提供方类型枚举。
 *
 * <p>定义支持的 LLM 提供方类型及其官方 API 调用地址。</p>
 *
 * @Author : haowl
 * @Date : 2026/3/14 11:45
 */
@Getter
@AllArgsConstructor
public enum LlmProviderType {

    OPENAI("OpenAI", "https://api.openai.com/v1"),
    QWEN("通义千问", "https://dashscope.aliyuncs.com/compatible-mode/v1"),
    DEEPSEEK("DeepSeek", "https://api.deepseek.com/v1"),
    GLM("智谱AI", "https://open.bigmodel.cn/api/paas/v4");

    /**
     * 提供方显示名称。
     */
    private final String displayName;

    /**
     * 官方 API 调用地址。
     */
    private final String officialBaseUrl;
}
