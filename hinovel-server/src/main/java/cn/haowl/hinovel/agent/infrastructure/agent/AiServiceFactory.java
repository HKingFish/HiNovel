package cn.haowl.hinovel.agent.infrastructure.agent;

import cn.haowl.hinovel.agent.application.service.SkillService;
import cn.haowl.hinovel.agent.constant.AgentConstants;
import cn.haowl.hinovel.agent.domain.entity.AgentSkill;
import cn.haowl.hinovel.agent.domain.entity.Agent;
import cn.haowl.hinovel.agent.enums.AgentRole;
import cn.haowl.hinovel.agent.infrastructure.mapper.AgentMapper;
import cn.haowl.hinovel.agent.infrastructure.tool.SkillTool;
import cn.haowl.hinovel.ai.application.llm.LlmProviderFactory;
import cn.haowl.hinovel.ai.application.llm.LlmProviderPort;
import cn.haowl.hinovel.ai.application.log.LlmCallLogService;
import cn.haowl.hinovel.ai.domain.entity.LlmCallLog;
import cn.haowl.hinovel.ai.domain.entity.SkillReadCall;
import cn.haowl.hinovel.common.constant.CommonConstants;
import cn.haowl.hinovel.novel.domain.entity.NovelAgentConfig;
import cn.haowl.hinovel.novel.infrastructure.mapper.NovelAgentConfigMapper;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingHandle;
import dev.langchain4j.model.output.TokenUsage;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolExecution;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

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
    /** 内置 Skill 挂载和正文。 */
    private final SkillService skillService;

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

        /** 当次执行使用的模型提供方。 */
        private LlmProviderPort provider;

        /** 仅含目录元数据的系统提示词。 */
        private String systemPrompt;

        /** 当次执行的工具调用上限。 */
        private int maxIterations;

        /** 当次执行的 Read Tool。 */
        private SkillTool skillTool;

        /** 返回当前执行的读取快照。 */
        public List<SkillReadCall> skillReadCalls() {
            return skillTool == null ? List.of() : skillTool.readCalls();
        }
    }

    /** 解析一次执行所用的 Agent、模型、提示词及 Skill 挂载。 */
    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public CallContext prepareNovelCall(Long novelId, AgentRole role, Long userId, String defaultSystemPrompt) {
        Agent agent = resolveAgent(novelId, role);
        if (agent == null) {
            throw exception(NOVEL_AGENT_NOT_CONFIGURED, getRoleName(role));
        }
        if (!agent.isAvailableTo(userId)) {
            throw exception(AGENT_NOT_OWNED);
        }
        if (agent.getLlmProviderId() == null) {
            throw exception(AGENT_ROLE_PROVIDER_NOT_CONFIGURED, getRoleName(role));
        }
        List<AgentSkill> skills = skillService.listBoundSkills(agent.getId());
        LlmProviderPort provider = llmProviderFactory.getByProviderId(agent.getLlmProviderId());
        String systemPrompt = agent.getSystemPrompt();
        String modelName = provider.getModelName();
        if (systemPrompt == null || systemPrompt.isBlank()) {
            systemPrompt = defaultSystemPrompt;
        }
        return CallContext.builder()
                .agentId(agent.getId())
                .agentName(agent.getName())
                .llmProviderId(agent.getLlmProviderId())
                .llmProviderName(provider.getProviderType())
                .modelName(modelName == null ? agent.getModelName() : modelName)
                .userId(userId)
                .provider(provider)
                .systemPrompt(systemPrompt + AgentSkillPrompts.buildSkillSection(skills))
                .maxIterations(agent.getMaxIterations() == null
                        ? AgentConstants.DEFAULT_MAX_ITERATIONS : agent.getMaxIterations())
                .skillTool(skills.isEmpty() ? null : new SkillTool(skills, skillService::readContent))
                .build();
    }

    /** 按本次执行的模式注册挂载工具及现有场景工具。 */
    public GenericAiAssistant buildNovelAssistant(CallContext context, boolean streaming, Object... sceneTools) {
        AiServices<GenericAiAssistant> builder = AiServices.builder(GenericAiAssistant.class)
                .maxSequentialToolsInvocations(context.getMaxIterations());
        if (streaming) {
            builder.streamingChatModel(context.getProvider().getStreamingChatModel());
        } else {
            builder.chatModel(context.getProvider().getChatModel());
        }
        List<Object> tools = new ArrayList<>();
        for (Object tool : sceneTools) {
            if (tool != null) {
                tools.add(tool);
            }
        }
        if (context.getSkillTool() != null) {
            tools.add(context.getSkillTool());
        }
        if (!tools.isEmpty()) {
            builder.tools(tools.toArray());
        }
        return builder.build();
    }

    /** 将服务端通过 Read Tool 得到的正文交给当前写作或润色执行。 */
    public String writingPrompt(CallContext context, String userPrompt) {
        SkillTool skillTool = context.getSkillTool();
        if (skillTool == null || !skillTool.hasDefaultSkill()) {
            return userPrompt;
        }
        return "【本次执行的去 AI 味 Skill 读取结果】\n" + skillTool.loadDefaultSkill()
                + "\n\n【本次请求】\n" + userPrompt;
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

    /** 将流式文本映射到调用方的协议，并记录调用结果。 */
    public Flux<String> tokenStreamToFlux(TokenStream tokenStream, String logContext,
                                          String callScene, CallContext callContext,
                                          String requestContent) {
        return tokenStreamToFlux(tokenStream, logContext, callScene, callContext, requestContent,
                Function.identity(), null, null);
    }

    /** 将文本与 Tool 结果以独立事件发给同一个订阅者。 */
    public <T> Flux<T> tokenStreamToFlux(TokenStream tokenStream, String logContext,
                                        String callScene, CallContext callContext, String requestContent,
                                        Function<String, T> textEvent, Function<ToolExecution, T> toolEvent, T firstEvent) {
        return Flux.create(sink -> {
            long startTime = System.currentTimeMillis();
            StringBuffer responseBuffer = new StringBuffer();
            AtomicBoolean logged = new AtomicBoolean();
            AtomicReference<StreamingHandle> upstream = new AtomicReference<>();
            Consumer<StreamingHandle> observeHandle = handle -> {
                upstream.set(handle);
                if (sink.isCancelled()) {
                    cancelStreamingHandle(handle);
                }
            };
            sink.onCancel(() -> {
                if (callContext != null && callContext.getSkillTool() != null) {
                    callContext.getSkillTool().cancel();
                }
                cancelStreamingHandle(upstream.get());
                if (callContext != null && callScene != null && logged.compareAndSet(false, true)) {
                    saveFailedCallLog(callScene, callContext, requestContent, "调用已取消",
                            System.currentTimeMillis() - startTime, true);
                }
            });
            if (firstEvent != null) {
                sink.next(firstEvent);
            }
            if (sink.isCancelled()) {
                return;
            }

            TokenStream stream = tokenStream.onPartialResponseWithContext((partial, context) -> {
                        observeHandle.accept(context.streamingHandle());
                        if (!sink.isCancelled()) {
                            responseBuffer.append(partial.text());
                            sink.next(textEvent.apply(partial.text()));
                        }
                    })
                    .onPartialToolCallWithContext((partial, context) -> observeHandle.accept(context.streamingHandle()))
                    .beforeToolExecution(execution -> {
                        if (sink.isCancelled()) {
                            throw new CancellationException("调用已取消");
                        }
                    });
            if (toolEvent != null) {
                stream = stream.onToolExecuted(execution -> {
                    T event = toolEvent.apply(execution);
                    if (event != null) {
                        sink.next(event);
                    }
                });
            }
            stream.onCompleteResponse(response -> {
                        if (callContext != null && callScene != null && logged.compareAndSet(false, true)) {
                            saveCallLog(callScene, callContext, requestContent, responseBuffer.toString(),
                                    response, System.currentTimeMillis() - startTime, true);
                        }
                        sink.complete();
                    })
                    .onError(error -> {
                        if (sink.isCancelled()) {
                            return;
                        }
                        log.error("[tokenStreamToFlux][流式调用异常，{}]", logContext, error);
                        if (callContext != null && callScene != null && logged.compareAndSet(false, true)) {
                            saveFailedCallLog(callScene, callContext, requestContent, error.getMessage(),
                                    System.currentTimeMillis() - startTime, true);
                        }
                        sink.error(error);
                    })
                    .start();
        });
    }

    /** 将客户端取消传给支持取消的模型流。 */
    private void cancelStreamingHandle(StreamingHandle handle) {
        if (handle != null) {
            try {
                handle.cancel();
            } catch (UnsupportedOperationException e) {
                log.debug("[cancelStreamingHandle][提供方未暴露取消能力]");
            }
        }
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

    /** 记录带 Token 用量的同步模型调用。 */
    public void logSyncResponse(String callScene, CallContext callContext, String requestContent,
                                ChatResponse response, long processingTimeMs) {
        saveCallLog(callScene, callContext, requestContent, response.aiMessage().text(),
                response, processingTimeMs, false);
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
                .skillReadCalls(callContext.skillReadCalls())
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
                .skillReadCalls(callContext.skillReadCalls())
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
