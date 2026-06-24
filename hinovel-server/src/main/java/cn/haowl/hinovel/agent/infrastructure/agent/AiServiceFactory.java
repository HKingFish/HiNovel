package cn.haowl.hinovel.agent.infrastructure.agent;

import cn.haowl.hinovel.agent.domain.entity.Agent;
import cn.haowl.hinovel.agent.enums.AgentRole;
import cn.haowl.hinovel.agent.infrastructure.mapper.AgentMapper;
import cn.haowl.hinovel.ai.application.llm.LlmProviderFactory;
import cn.haowl.hinovel.ai.application.llm.LlmProviderPort;
import cn.haowl.hinovel.ai.application.log.LlmCallLogService;
import cn.haowl.hinovel.ai.domain.entity.LlmCallLog;
import cn.haowl.hinovel.common.constant.CommonConstants;
import cn.haowl.hinovel.novel.domain.entity.NovelAgentConfig;
import cn.haowl.hinovel.novel.infrastructure.mapper.NovelAgentConfigMapper;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.output.TokenUsage;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolExecution;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import static cn.haowl.hinovel.agent.enums.AgentErrorCodeConstants.*;
import static cn.haowl.hinovel.common.exception.util.ServiceExceptionUtil.exception;

/**
 * AiService 构建工厂。
 *
 * <p>封装 LangChain4J {@link AiServices} 的动态构建逻辑，
 * 提供统一的 AiService 实例创建、Agent 配置解析、系统提示词获取、
 * 以及 {@link TokenStream} 到 {@link Flux} 的桥接能力。</p>
 *
 * <p>集成 LLM 调用记录功能，在每次调用完成后异步记录请求、响应、
 * Token 消耗和耗时等信息。</p>
 *
 * @author haowl
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiServiceFactory {

    private final LlmProviderFactory llmProviderFactory;
    private final NovelAgentConfigMapper novelAgentConfigMapper;
    private final AgentMapper agentMapper;
    private final LlmCallLogService llmCallLogService;


    /**
     * 调用上下文，封装 Agent 和 LLM 提供方的元信息，用于调用记录。
     */
    @Data
    @Builder
    public static class CallContext {

        /**
         * Agent ID
         */
        private Long agentId;

        /**
         * Agent 名称
         */
        private String agentName;

        /**
         * LLM 提供方 ID
         */
        private Long llmProviderId;

        /**
         * LLM 提供方名称
         */
        private String llmProviderName;

        /**
         * 模型名称
         */
        private String modelName;

        /**
         * 所属用户 ID（用于调用记录数据隔离）
         */
        private Long userId;
    }

    // ==================== AiService 构建 ====================

    /**
     * 根据 Agent ID 构建同步模式的 AiService 实例。
     *
     * @param agentId      Agent 主键 ID
     * @param serviceClass AiService 接口类型
     * @param <T>          AiService 接口泛型
     * @return AiService 实例
     */
    public <T> T buildByAgentId(Long agentId, Class<T> serviceClass) {
        LlmProviderPort providerPort = resolveProviderByAgentId(agentId);
        return AiServices.builder(serviceClass)
                .chatModel(providerPort.getChatModel())
                .build();
    }

    /**
     * 根据 Agent ID 构建流式模式的 AiService 实例。
     *
     * @param agentId      Agent 主键 ID
     * @param serviceClass AiService 接口类型
     * @param <T>          AiService 接口泛型
     * @return AiService 实例（流式）
     */
    public <T> T buildStreamingByAgentId(Long agentId, Class<T> serviceClass) {
        LlmProviderPort providerPort = resolveProviderByAgentId(agentId);
        return AiServices.builder(serviceClass)
                .streamingChatModel(providerPort.getStreamingChatModel())
                .build();
    }

    /**
     * 根据小说 ID 和 Agent 角色构建同步模式的 AiService 实例。
     *
     * @param novelId      小说 ID
     * @param role         Agent 角色（作者/编辑）
     * @param serviceClass AiService 接口类型
     * @param <T>          AiService 接口泛型
     * @return AiService 实例
     */
    public <T> T buildByNovel(Long novelId, AgentRole role, Class<T> serviceClass) {
        LlmProviderPort providerPort = resolveProviderByNovel(novelId, role);
        return AiServices.builder(serviceClass)
                .chatModel(providerPort.getChatModel())
                .build();
    }

    /**
     * 根据小说 ID 和 Agent 角色构建流式模式的 AiService 实例。
     *
     * @param novelId      小说 ID
     * @param role         Agent 角色（作者/编辑）
     * @param serviceClass AiService 接口类型
     * @param <T>          AiService 接口泛型
     * @return AiService 实例（流式）
     */
    public <T> T buildStreamingByNovel(Long novelId, AgentRole role, Class<T> serviceClass) {
        LlmProviderPort providerPort = resolveProviderByNovel(novelId, role);
        return AiServices.builder(serviceClass)
                .streamingChatModel(providerPort.getStreamingChatModel())
                .build();
    }

    /**
     * 根据小说 ID 和 Agent 角色构建流式模式的 AiService 实例（带 Tool 支持）。
     *
     * <p>用于需要绑定外部工具的场景（如问答 Agent 绑定向量检索工具）。</p>
     *
     * @param novelId      小说 ID
     * @param role         Agent 角色
     * @param serviceClass AiService 接口类型
     * @param tools        工具实例列表
     * @param <T>          AiService 接口泛型
     * @return AiService 实例（流式，带 Tool）
     */
    public <T> T buildStreamingByNovelWithTools(Long novelId, AgentRole role,
                                                Class<T> serviceClass, Object... tools) {
        LlmProviderPort providerPort = resolveProviderByNovel(novelId, role);
        return AiServices.builder(serviceClass)
                .streamingChatModel(providerPort.getStreamingChatModel())
                .tools(tools)
                .build();
    }

    // ==================== 调用上下文解析 ====================

    /**
     * 根据小说 ID 和 Agent 角色解析调用上下文。
     *
     * <p>返回包含 Agent 和 LLM 提供方元信息的上下文对象，
     * 供调用方在记录调用日志时使用。</p>
     *
     * @param novelId 小说 ID
     * @param role    Agent 角色
     * @return 调用上下文
     */
    public CallContext resolveCallContext(Long novelId, AgentRole role) {
        Agent agent = resolveAgent(novelId, role);
        if (agent == null) {
            return CallContext.builder().build();
        }
        LlmProviderPort providerPort = null;
        if (agent.getLlmProviderId() != null) {
            try {
                providerPort = llmProviderFactory.getByProviderId(agent.getLlmProviderId());
            } catch (Exception e) {
                log.warn("解析调用上下文时获取 LLM 提供方失败，agentId={}，异常信息：{}",
                        agent.getId(), e.getMessage());
            }
        }
        return CallContext.builder()
                .agentId(agent.getId())
                .agentName(agent.getName())
                .llmProviderId(agent.getLlmProviderId())
                .llmProviderName(providerPort != null ? providerPort.getProviderType() : null)
                .modelName(providerPort != null ? providerPort.getModelName() : agent.getModelName())
                .build();
    }

    /**
     * 根据小说 ID、Agent 角色和用户 ID 解析调用上下文（含用户隔离）。
     *
     * @param novelId Agent 所属小说 ID
     * @param role    Agent 角色
     * @param userId  当前用户 ID
     * @return 调用上下文
     */
    public CallContext resolveCallContext(Long novelId, AgentRole role, Long userId) {
        CallContext ctx = resolveCallContext(novelId, role);
        ctx.setUserId(userId);
        return ctx;
    }

    // ==================== 系统提示词解析 ====================

    /**
     * 根据小说 ID 和 Agent 角色获取系统提示词。
     *
     * <p>优先使用 Agent 自定义的系统提示词，未配置时返回调用方提供的默认值。</p>
     *
     * @param novelId             小说 ID
     * @param role                Agent 角色
     * @param defaultSystemPrompt 默认系统提示词
     * @return 系统提示词
     */
    public String resolveSystemPrompt(Long novelId, AgentRole role, String defaultSystemPrompt) {
        Agent agent = resolveAgent(novelId, role);
        if (agent != null && agent.getSystemPrompt() != null && !agent.getSystemPrompt().isBlank()) {
            return agent.getSystemPrompt();
        }
        return defaultSystemPrompt;
    }

    // ==================== TokenStream 转 Flux ====================

    /**
     * 将 LangChain4J {@link TokenStream} 转换为 Reactor {@link Flux}。
     *
     * <p>使用 {@link Sinks.Many} 作为桥接，将 TokenStream 的回调事件推送到 Flux 流中。
     * 适用于 SSE 流式推送场景。</p>
     *
     * @param tokenStream LangChain4J Token 流
     * @param logContext  日志上下文描述（如 "chapterId=123"），用于异常日志
     * @return Flux 流
     */
    public Flux<String> tokenStreamToFlux(TokenStream tokenStream, String logContext) {
        return tokenStreamToFlux(tokenStream, logContext, null, null, null);
    }

    /**
     * 将 LangChain4J {@link TokenStream} 转换为 Reactor {@link Flux}，并记录调用日志。
     *
     * @param tokenStream    LangChain4J Token 流
     * @param logContext     日志上下文描述
     * @param callScene      调用场景（如 REWRITE、AUDIT）
     * @param callContext    调用上下文（Agent 和提供方信息）
     * @param requestContent 请求内容摘要
     * @return Flux 流
     */
    public Flux<String> tokenStreamToFlux(TokenStream tokenStream, String logContext,
                                          String callScene, CallContext callContext,
                                          String requestContent) {
        Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();
        long startTime = System.currentTimeMillis();
        // 收集完整响应内容
        StringBuilder responseBuffer = new StringBuilder();

        tokenStream
                .onPartialResponse(token -> {
                    responseBuffer.append(token);
                    sink.tryEmitNext(token);
                })
                .onCompleteResponse(response -> {
                    long processingTimeMs = System.currentTimeMillis() - startTime;
                    log.info("流式调用完成，{}，耗时={}ms", logContext, processingTimeMs);
                    // 异步记录调用日志
                    if (callScene != null && callContext != null) {
                        saveCallLog(callScene, callContext, requestContent,
                                responseBuffer.toString(), response, processingTimeMs, true);
                    }
                    sink.tryEmitComplete();
                })
                .onError(error -> {
                    long processingTimeMs = System.currentTimeMillis() - startTime;
                    log.error("流式调用异常，{}，耗时={}ms，异常信息：{}",
                            logContext, processingTimeMs, error.getMessage());
                    // 记录失败日志
                    if (callScene != null && callContext != null) {
                        saveFailedCallLog(callScene, callContext, requestContent,
                                error.getMessage(), processingTimeMs, true);
                    }
                    sink.tryEmitError(error);
                })
                .start();

        return sink.asFlux();
    }

    /**
     * 将 LangChain4J {@link TokenStream} 转换为 Reactor {@link Flux}，
     * 支持工具执行事件注入和调用日志记录。
     *
     * <p>在工具执行完成后，通过 {@code toolExecutedEventBuilder} 构建事件标记字符串，
     * 注入到 SSE 流中，前端据此识别工具执行事件并触发 UI 更新。
     * 事件标记不会计入 AI 回复内容（不影响消息保存）。</p>
     *
     * @param tokenStream              LangChain4J Token 流
     * @param logContext               日志上下文描述
     * @param callScene                调用场景
     * @param callContext              调用上下文
     * @param requestContent           请求内容摘要
     * @param toolExecutedEventBuilder 工具执行事件构建器，接收 {@link ToolExecution}，返回注入到 SSE 流的事件标记字符串
     * @return Flux 流
     */
    public Flux<String> tokenStreamToFlux(TokenStream tokenStream, String logContext,
                                          String callScene, CallContext callContext,
                                          String requestContent,
                                          java.util.function.Function<ToolExecution, String> toolExecutedEventBuilder) {
        Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();
        long startTime = System.currentTimeMillis();
        StringBuilder responseBuffer = new StringBuilder();

        tokenStream
                .onPartialResponse(token -> {
                    responseBuffer.append(token);
                    sink.tryEmitNext(token);
                })
                .onToolExecuted(toolExecution -> {
                    log.info("工具执行完成，{}，工具名={}",
                            logContext, toolExecution.request().name());
                    if (toolExecutedEventBuilder != null) {
                        // 构建事件标记并注入 SSE 流（不计入 responseBuffer）
                        String eventMarker = toolExecutedEventBuilder.apply(toolExecution);
                        if (eventMarker != null) {
                            sink.tryEmitNext(eventMarker);
                        }
                    }
                })
                .onCompleteResponse(response -> {
                    long processingTimeMs = System.currentTimeMillis() - startTime;
                    log.info("流式调用完成，{}，耗时={}ms", logContext, processingTimeMs);
                    if (callScene != null && callContext != null) {
                        saveCallLog(callScene, callContext, requestContent,
                                responseBuffer.toString(), response, processingTimeMs, true);
                    }
                    sink.tryEmitComplete();
                })
                .onError(error -> {
                    long processingTimeMs = System.currentTimeMillis() - startTime;
                    log.error("流式调用异常，{}，耗时={}ms，异常信息：{}",
                            logContext, processingTimeMs, error.getMessage());
                    if (callScene != null && callContext != null) {
                        saveFailedCallLog(callScene, callContext, requestContent,
                                error.getMessage(), processingTimeMs, true);
                    }
                    sink.tryEmitError(error);
                })
                .start();

        return sink.asFlux();
    }

    // ==================== 同步调用日志记录 ====================

    /**
     * 记录同步调用的成功日志。
     *
     * <p>供 {@code AuthorAgentService}、{@code EditorAgentService} 等业务服务在
     * 同步调用完成后调用。</p>
     *
     * @param callScene        调用场景
     * @param callContext      调用上下文
     * @param requestContent   请求内容摘要
     * @param responseContent  响应内容
     * @param processingTimeMs 处理耗时（毫秒）
     */
    public void logSyncCall(String callScene, CallContext callContext,
                            String requestContent, String responseContent,
                            long processingTimeMs) {
        saveCallLog(callScene, callContext, requestContent, responseContent,
                null, processingTimeMs, false);
    }

    /**
     * 记录同步调用的失败日志。
     *
     * @param callScene        调用场景
     * @param callContext      调用上下文
     * @param requestContent   请求内容摘要
     * @param errorMessage     错误信息
     * @param processingTimeMs 处理耗时（毫秒）
     */
    public void logSyncCallFailed(String callScene, CallContext callContext,
                                  String requestContent, String errorMessage,
                                  long processingTimeMs) {
        saveFailedCallLog(callScene, callContext, requestContent,
                errorMessage, processingTimeMs, false);
    }

    // ==================== 内部方法：调用日志 ====================

    /**
     * 保存成功的调用日志。
     *
     * @param callScene        调用场景
     * @param callContext      调用上下文
     * @param requestContent   请求内容
     * @param responseContent  响应内容
     * @param chatResponse     LangChain4J 响应对象（流式调用时可获取 Token 信息，同步调用传 null）
     * @param processingTimeMs 处理耗时
     * @param isStreaming      是否流式调用
     */
    private void saveCallLog(String callScene, CallContext callContext,
                             String requestContent, String responseContent,
                             ChatResponse chatResponse, long processingTimeMs,
                             boolean isStreaming) {
        LlmCallLog callLog = LlmCallLog.builder()
                .callScene(callScene)
                .agentId(callContext.getAgentId())
                .agentName(callContext.getAgentName())
                .llmProviderId(callContext.getLlmProviderId())
                .llmProviderName(callContext.getLlmProviderName())
                .modelName(callContext.getModelName())
                .requestContent(requestContent)
                .responseContent(responseContent)
                .processingTimeMs(processingTimeMs)
                .status(LlmCallLog.STATUS_SUCCESS)
                .isStreaming(isStreaming ? CommonConstants.ENABLED : CommonConstants.DISABLED)
                .userId(callContext.getUserId())
                .build();

        // 从 ChatResponse 中提取 Token 使用信息
        if (chatResponse != null && chatResponse.tokenUsage() != null) {
            TokenUsage tokenUsage = chatResponse.tokenUsage();
            callLog.setPromptTokens(tokenUsage.inputTokenCount());
            callLog.setCompletionTokens(tokenUsage.outputTokenCount());
            callLog.setTotalTokens(tokenUsage.totalTokenCount());
        }

        llmCallLogService.saveAsync(callLog);
    }

    /**
     * 保存失败的调用日志。
     *
     * @param callScene        调用场景
     * @param callContext      调用上下文
     * @param requestContent   请求内容
     * @param errorMessage     错误信息
     * @param processingTimeMs 处理耗时
     * @param isStreaming      是否流式调用
     */
    private void saveFailedCallLog(String callScene, CallContext callContext,
                                   String requestContent, String errorMessage,
                                   long processingTimeMs, boolean isStreaming) {
        LlmCallLog callLog = LlmCallLog.builder()
                .callScene(callScene)
                .agentId(callContext.getAgentId())
                .agentName(callContext.getAgentName())
                .llmProviderId(callContext.getLlmProviderId())
                .llmProviderName(callContext.getLlmProviderName())
                .modelName(callContext.getModelName())
                .requestContent(requestContent)
                .status(LlmCallLog.STATUS_FAILED)
                .errorMessage(errorMessage)
                .processingTimeMs(processingTimeMs)
                .isStreaming(isStreaming ? CommonConstants.ENABLED : CommonConstants.DISABLED)
                .userId(callContext.getUserId())
                .build();

        llmCallLogService.saveAsync(callLog);
    }

    // ==================== 内部方法：Provider 解析 ====================

    /**
     * 根据 Agent ID 解析 LLM 提供方。
     *
     * @param agentId Agent 主键 ID
     * @return LLM 提供方端口
     */
    private LlmProviderPort resolveProviderByAgentId(Long agentId) {
        Agent agent = agentMapper.selectById(agentId);
        if (agent == null) {
            throw exception(AGENT_NOT_EXISTS, agentId);
        }
        if (agent.getLlmProviderId() == null) {
            throw exception(AGENT_LLM_PROVIDER_NOT_CONFIGURED, agentId);
        }
        return llmProviderFactory.getByProviderId(agent.getLlmProviderId());
    }

    /**
     * 根据小说 ID 和 Agent 角色解析 LLM 提供方。
     *
     * @param novelId 小说 ID
     * @param role    Agent 角色
     * @return LLM 提供方端口
     */
    private LlmProviderPort resolveProviderByNovel(Long novelId, AgentRole role) {
        Agent agent = resolveAgent(novelId, role);
        if (agent == null) {
            throw exception(NOVEL_AGENT_NOT_CONFIGURED, getRoleName(role));
        }
        if (agent.getLlmProviderId() == null) {
            throw exception(AGENT_ROLE_PROVIDER_NOT_CONFIGURED, getRoleName(role));
        }
        return llmProviderFactory.getByProviderId(agent.getLlmProviderId());
    }

    /**
     * 根据小说 ID 和角色解析 Agent 实体。
     *
     * @param novelId 小说 ID
     * @param role    Agent 角色
     * @return Agent 实体，未找到时返回 null
     */
    private Agent resolveAgent(Long novelId, AgentRole role) {
        NovelAgentConfig config = novelAgentConfigMapper.selectByNovelId(novelId);
        if (config == null) {
            return null;
        }
        Long agentId = extractAgentId(config, role);
        if (agentId == null) {
            return null;
        }
        return agentMapper.selectById(agentId);
    }

    /**
     * 从配置中提取对应角色的 Agent ID。
     *
     * @param config 小说 Agent 配置
     * @param role   Agent 角色
     * @return Agent ID
     */
    private Long extractAgentId(NovelAgentConfig config, AgentRole role) {
        return switch (role) {
            case AUTHOR -> config.getAuthorAgentId();
            case EDITOR -> config.getEditorAgentId();
            case QA -> config.getQaAgentId();
        };
    }

    /**
     * 获取角色的中文名称（用于异常提示）。
     *
     * @param role Agent 角色
     * @return 中文名称
     */
    private String getRoleName(AgentRole role) {
        return role.getName();
    }
}
