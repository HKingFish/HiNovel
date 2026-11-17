package cn.haowl.hinovel.ai.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * LLM 提供方类型枚举。
 *
 * 定义支持的 LLM 提供方类型及其官方 API 调用地址。
 *
 * @Author : haowl
 * @Date : 2026/3/14 11:45
 */
@Getter
@AllArgsConstructor
public enum LlmProviderTypeEnum {

    /**
     * OpenAI。
     */
    OPENAI("OpenAI", "https://api.openai.com/v1"),

    /**
     * 阿里云通义千问。
     */
    QWEN("通义千问", AiConstants.QWEN_COMPATIBLE_BASE_URL),

    /**
     * DeepSeek。
     */
    DEEPSEEK("DeepSeek", "https://api.deepseek.com/v1"),

    /**
     * 智谱 GLM。
     */
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
