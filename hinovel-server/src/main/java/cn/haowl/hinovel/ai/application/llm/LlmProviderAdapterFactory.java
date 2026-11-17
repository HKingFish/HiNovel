package cn.haowl.hinovel.ai.application.llm;

import cn.haowl.hinovel.common.exception.BusinessException;
import dev.langchain4j.model.chat.StreamingChatModel;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.haowl.hinovel.ai.enums.AiErrorCodeConstants.LLM_PROVIDER_TYPE_NOT_SUPPORTED;
import static cn.haowl.hinovel.common.exception.util.ServiceExceptionUtil.exception;

/**
 * LLM Provider 适配器工厂类。
 *
 * 用于管理和创建不同 LLM 提供商的适配器实例，根据提供方类型
 * 创建对应的流式聊天模型。
 *
 * @author haowl
 * @since 2024
 */
@Slf4j
@Component
public class LlmProviderAdapterFactory {

    /**
     * 所有注册的 LLM Provider 适配器列表。
     */
    @Autowired
    private List<LlmProviderAdapter> adapters;

    /**
     * 提供方类型到适配器的映射缓存。
     */
    private Map<String, LlmProviderAdapter> adapterMap;

    /**
     * 初始化适配器映射。
     *
     * 在 Spring 容器启动后执行，将所有适配器按提供方类型建立索引映射，
     * 便于后续快速查找。
     */
    @PostConstruct
    public void init() {
        adapterMap = adapters.stream()
                .collect(Collectors.toMap(
                    adapter -> adapter.supportedProviderType().name(),
                    Function.identity()));
        log.info("[init][LLM 适配器工厂初始化完成，已注册提供方类型：{}]", adapterMap.keySet());
    }

    /**
     * 构建流式聊天模型。
     *
     * 根据提供方类型查找对应的适配器，并使用给定参数构建流式聊天模型。
     * 如果找不到对应的适配器，则抛出业务异常。
     *
     * @param providerType 提供方类型，如 "OPENAI"、"DEEPSEEK" 等
     * @param options      连接和采样参数
     * @return 配置好的流式聊天模型实例
     * @throws BusinessException 当找不到对应提供方类型的适配器时抛出
     */
    public StreamingChatModel buildStreamingModel(String providerType, LlmStreamingModelOptions options) {
        LlmProviderAdapter adapter = adapterMap.get(providerType);
        if (adapter == null) {
            log.error("[buildStreamingModel][未找到对应适配器，已注册类型={}]", adapterMap.keySet());
            throw exception(LLM_PROVIDER_TYPE_NOT_SUPPORTED, providerType);
        }
        return adapter.buildStreamingModel(options);
    }
}
