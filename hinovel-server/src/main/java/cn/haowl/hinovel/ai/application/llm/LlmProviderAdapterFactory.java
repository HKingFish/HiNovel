package cn.haowl.hinovel.ai.application.llm;

import cn.haowl.hinovel.ai.enums.AiErrorCodeConstants;
import cn.haowl.hinovel.common.exception.BusinessException;
import dev.langchain4j.model.chat.StreamingChatModel;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * LLM Provider 适配器工厂类。
 *
 * <p>用于管理和创建不同 LLM 提供商的适配器实例，根据提供方类型
 * 创建对应的流式聊天模型。</p>
 *
 * @author haowl
 * @since 2024
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LlmProviderAdapterFactory {

    /**
     * 所有注册的 LLM Provider 适配器列表。
     */
    private final List<LlmProviderAdapter> adapters;

    /**
     * 提供方类型到适配器的映射缓存。
     */
    private Map<String, LlmProviderAdapter> adapterMap;

    /**
     * 初始化适配器映射。
     *
     * <p>在 Spring 容器启动后执行，将所有适配器按提供方类型建立索引映射，
     * 便于后续快速查找。</p>
     */
    @PostConstruct
    public void init() {
        adapterMap = adapters.stream()
                .collect(Collectors.toMap(
                        LlmProviderAdapter::supportedProviderType,
                        Function.identity()
                ));
        log.info("LLM 适配器工厂初始化完成，已注册提供方类型：{}", adapterMap.keySet());
    }

    /**
     * 构建流式聊天模型。
     *
     * <p>根据提供方类型查找对应的适配器，并使用给定参数构建流式聊天模型。
     * 如果找不到对应的适配器，则抛出业务异常。</p>
     *
     * @param providerType 提供方类型，如 "OPENAI"、"DEEPSEEK" 等
     * @param baseUrl      API 基础 URL
     * @param apiKey       API 密钥
     * @param modelName    模型名称
     * @param temperature  温度参数，控制输出的随机性
     * @param maxTokens    最大生成 Token 数
     * @param topP         Top-P 采样参数
     * @return 配置好的流式聊天模型实例
     * @throws BusinessException 当找不到对应提供方类型的适配器时抛出
     */
    public StreamingChatModel buildStreamingModel(String providerType, String baseUrl,
                                                  String apiKey, String modelName,
                                                  double temperature, int maxTokens,
                                                  double topP) {
        LlmProviderAdapter adapter = adapterMap.get(providerType);
        if (adapter == null) {
            log.error("未找到 providerType={} 对应的适配器，已注册类型：{}", providerType, adapterMap.keySet());
            throw new BusinessException(AiErrorCodeConstants.LLM_PROVIDER_UNAVAILABLE,
                    "不支持的 LLM 提供方类型：" + providerType);
        }
        return adapter.buildStreamingModel(baseUrl, apiKey, modelName, temperature, maxTokens, topP);
    }
}
