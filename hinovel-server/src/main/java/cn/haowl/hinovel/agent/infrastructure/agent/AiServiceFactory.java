package cn.haowl.hinovel.agent.infrastructure.agent;

import cn.haowl.hinovel.agent.application.service.SkillService;
import cn.haowl.hinovel.agent.application.service.agent.WritingIntentAssistant;
import cn.haowl.hinovel.agent.constant.AgentConstants;
import cn.haowl.hinovel.agent.constant.AgentRoleEnum;
import cn.haowl.hinovel.agent.domain.entity.Agent;
import cn.haowl.hinovel.agent.domain.entity.AgentSkill;
import cn.haowl.hinovel.agent.domain.repository.AgentRepository;
import cn.haowl.hinovel.agent.infrastructure.tool.SkillTool;
import cn.haowl.hinovel.ai.application.llm.LlmProviderFactory;
import cn.haowl.hinovel.ai.application.llm.LlmProviderPort;
import cn.haowl.hinovel.ai.application.llm.LlmProviderService;
import cn.haowl.hinovel.ai.application.log.LlmCallLogService;
import cn.haowl.hinovel.ai.constant.LlmCallSceneEnum;
import cn.haowl.hinovel.ai.constant.LlmCallStatusEnum;
import cn.haowl.hinovel.ai.domain.entity.LlmCallLog;
import cn.haowl.hinovel.ai.domain.entity.SkillReadCall;
import cn.haowl.hinovel.common.constant.CommonConstants;
import cn.haowl.hinovel.novel.domain.entity.NovelAgentConfig;
import cn.haowl.hinovel.novel.domain.repository.NovelAgentConfigRepository;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingHandle;
import dev.langchain4j.model.output.TokenUsage;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolExecution;
import lombok.Builder;
import lombok.Data;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

import static cn.haowl.hinovel.agent.enums.AgentErrorCodeConstants.AGENT_NOT_OWNED;
import static cn.haowl.hinovel.agent.enums.AgentErrorCodeConstants.AGENT_ROLE_PROVIDER_NOT_CONFIGURED;
import static cn.haowl.hinovel.agent.enums.AgentErrorCodeConstants.NOVEL_AGENT_NOT_CONFIGURED;
import static cn.haowl.hinovel.common.exception.util.ServiceExceptionUtil.exception;

/**
 * AiService 构建工厂。
 *
 * 封装 LangChain4J {@link AiServices} 的动态构建逻辑，
 * 提供统一的 AiService 实例创建、Agent 配置解析、系统提示词获取、
 * 以及 {@link TokenStream} 到 {@link Flux} 的桥接能力。
 *
 * 集成 LLM 调用记录功能，在每次调用完成后异步记录请求、响应、
 * Token 消耗和耗时等信息。
 *
 * @author haowl
 */
@Slf4j
@Component
public class AiServiceFactory {

    /**
     * 意图识别记录使用的请求摘要。
     */
    private static final String WRITING_INTENT_REQUEST = "判断当前问答是否要求写作或润色";

    /**
     * 按用户配置解析模型提供方。
     */
    @Autowired
    private LlmProviderFactory llmProviderFactory;

    /**
     * 校验模型提供方归属。
     */
    @Autowired
    private LlmProviderService llmProviderService;

    /**
     * 小说绑定的 Agent 配置。
     */
    @Autowired
    private NovelAgentConfigRepository novelAgentConfigRepository;

    /**
     * Agent 元数据。
     */
    @Autowired
    private AgentRepository agentRepository;

    /**
     * LLM 调用记录。
     */
    @Autowired
    private LlmCallLogService llmCallLogService;

    /**
     * 内置 Skill 挂载和正文。
     */
    @Autowired
    private SkillService skillService;

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

        /**
         * 当次执行使用的模型提供方。
         */
        private LlmProviderPort provider;

        /**
         * 仅含目录元数据的系统提示词。
         */
        private String systemPrompt;

        /**
         * 当次执行的工具调用上限。
         */
        private int maxIterations;

        /**
         * 当次执行的 Read Tool。
         */
        private SkillTool skillTool;

        /**
         * 返回当前执行的读取快照。
         */
        public List<SkillReadCall> skillReadCalls() {
            return skillTool == null ? List.of() : skillTool.readCalls();
        }
    }

    /**
     * 解析一次执行所用的 Agent、模型、提示词及 Skill 挂载。
     */
    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public CallContext prepareNovelCall(Long novelId, AgentRoleEnum role, Long userId, String defaultSystemPrompt) {
        Agent agent = resolveAgent(novelId, role);
        if (agent == null) {
            throw exception(NOVEL_AGENT_NOT_CONFIGURED, role.getName());
        }
        if (!agent.isAvailableTo(userId)) {
            throw exception(AGENT_NOT_OWNED);
        }
        Long providerId = agent.getLlmProviderId();
        if (providerId == null) {
            throw exception(AGENT_ROLE_PROVIDER_NOT_CONFIGURED, role.getName());
        }
        llmProviderService.validateOwnership(providerId, userId);
        List<AgentSkill> skills = skillService.listBoundSkills(agent.getId());
        LlmProviderPort provider = llmProviderFactory.getByProviderId(providerId);
        String systemPrompt = agent.getSystemPrompt();
        String modelName = provider.getModelName();
        if (systemPrompt == null || systemPrompt.isBlank()) {
            systemPrompt = defaultSystemPrompt;
        }
        return CallContext.builder()
                .agentId(agent.getId())
                .agentName(agent.getName())
                .llmProviderId(providerId)
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

    /**
     * 按本次执行的模式注册挂载工具及现有场景工具。
     */
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

    /**
     * 将服务端通过 Read Tool 得到的正文交给当前写作或润色执行。
     */
    public String writingPrompt(CallContext context, String userPrompt) {
        SkillTool skillTool = context.getSkillTool();
        if (skillTool == null || !skillTool.hasDefaultSkill()) {
            return userPrompt;
        }
        return "【本次执行的去 AI 味 Skill 读取结果】\n" + skillTool.loadDefaultSkill()
                + "\n\n【本次请求】\n" + userPrompt;
    }

    /**
     * 判断问答请求是否要求直接写作或润色，并记录本次同步调用。
     *
     * @param context        当前执行的调用上下文
     * @param userPrompt     用于判定的用户提示词
     * @return 模型返回的意图标签
     */
    public String judgeWritingIntent(CallContext context, String userPrompt) {
        WritingIntentAssistant assistant = AiServices.builder(WritingIntentAssistant.class)
                .chatModel(context.getProvider().getChatModel())
                .build();
        long startTime = System.currentTimeMillis();
        CallLogContext logContext =
            CallLogContext.builder()
                .callScene(LlmCallSceneEnum.QA_WRITING_INTENT)
                .callContext(context)
                .requestContent(WRITING_INTENT_REQUEST)
                .build();
        try {
            Result<String> result = assistant.judgeWritingIntent(userPrompt);
            logSyncResponse(logContext, result.finalResponse(), System.currentTimeMillis() - startTime);
            return result.content();
        } catch (RuntimeException e) {
            logSyncCallFailed(logContext, e.getClass().getSimpleName(), System.currentTimeMillis() - startTime);
            throw e;
        }
    }

    // ==================== TokenStream 转 Flux ====================

    /**
     * 将 LangChain4J {@link TokenStream} 转换为 Reactor {@link Flux}。
     *
     * @param tokenStream LangChain4J Token 流
     * @param options 流式事件和调用日志配置
     * @return 文本和工具执行结果组成的事件流
     */
    public <T> Flux<T> tokenStreamToFlux(TokenStream tokenStream, TokenStreamOptions<T> options) {
        CallContext callContext = options.getCallContext();
        CallLogContext logContext =
            callContext == null || options.getCallScene() == null ? null : CallLogContext.builder()
                .callScene(options.getCallScene())
                .callContext(callContext)
                .requestContent(options.getRequestContent())
                .streaming(true)
                .build();
        Function<ToolExecution, T> toolEvent = options.getToolEvent();

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
                if (logContext != null && logged.compareAndSet(false, true)) {
                    saveFailedCallLog(logContext, "调用已取消", System.currentTimeMillis() - startTime);
                }
            });
            if (options.getFirstEvent() != null) {
                sink.next(options.getFirstEvent());
            }
            if (sink.isCancelled()) {
                return;
            }

            TokenStream stream = tokenStream.onPartialResponseWithContext((partial, context) -> {
                        observeHandle.accept(context.streamingHandle());
                        if (!sink.isCancelled()) {
                            responseBuffer.append(partial.text());
                            sink.next(options.getTextEvent().apply(partial.text()));
                        }
                    })
                .onPartialToolCallWithContext(
                    (partial, context) -> observeHandle.accept(context.streamingHandle()))
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
                    if (logContext != null && logged.compareAndSet(false, true)) {
                        saveCallLog(logContext, responseBuffer.toString(), response,
                            System.currentTimeMillis() - startTime);
                        }
                        sink.complete();
                    })
                    .onError(error -> {
                        if (sink.isCancelled()) {
                            return;
                        }
                        log.error("[tokenStreamToFlux][流式调用异常，{}，异常类型={}]",
                            options.getLogContext(), error.getClass().getSimpleName());
                        if (logContext != null && logged.compareAndSet(false, true)) {
                            saveFailedCallLog(logContext, error.getMessage(), System.currentTimeMillis() - startTime);
                        }
                        sink.error(error);
                    })
                    .start();
        });
    }

    /**
     * 记录同步调用的成功日志。
     */
    public void logSyncCall(CallLogContext logContext, String responseContent, long processingTimeMs) {
        saveCallLog(logContext, responseContent, null, processingTimeMs);
    }

    /**
     * 将客户端取消传给支持取消的模型流。
     */
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
     * 记录带 Token 用量的同步模型调用。
     */
    private void logSyncResponse(CallLogContext logContext, ChatResponse response, long processingTimeMs) {
        saveCallLog(logContext, response.aiMessage().text(), response, processingTimeMs);
    }

    /**
     * 记录同步调用的失败日志。
     */
    public void logSyncCallFailed(CallLogContext logContext, String errorMessage, long processingTimeMs) {
        saveFailedCallLog(logContext, errorMessage, processingTimeMs);
    }

    /**
     * 流式转换使用的日志信息和事件配置。
     */
    @Value
    @Builder
    public static class TokenStreamOptions<T> {
        /**
         * 流式异常日志的上下文。
         */
        String logContext;

        /**
         * 调用日志所属场景。
         */
        LlmCallSceneEnum callScene;

        /**
         * 记录调用日志所用的上下文。
         */
        CallContext callContext;

        /**
         * 记录到调用日志的请求正文。
         */
        String requestContent;

        /**
         * 将文本片段转换为下游事件。
         */
        Function<String, T> textEvent;

        /**
         * 将工具执行结果转换为下游事件，空值表示跳过该事件。
         */
        Function<ToolExecution, T> toolEvent;

        /**
         * 模型流启动前推送的可选事件。
         */
        T firstEvent;
    }

    /**
     * 一次模型调用的日志上下文。
     */
    @Value
    @Builder
    public static class CallLogContext {
        /**
         * 调用日志所属场景。
         */
        LlmCallSceneEnum callScene;

        /**
         * 调用使用的模型、Agent 和用户信息。
         */
        CallContext callContext;

        /**
         * 记录到日志的请求内容。
         */
        String requestContent;

        /**
         * 是否以流式方式调用模型。
         */
        boolean streaming;
    }

    // ==================== 内部方法：调用日志 ====================

    /**
     * 保存成功的调用记录和 Token 用量。
     */
    private void saveCallLog(
        CallLogContext context, String responseContent, ChatResponse chatResponse, long processingTimeMs) {
        LlmCallLog callLog =
            buildCallLog(context, processingTimeMs)
                .responseContent(responseContent)
                .status(LlmCallStatusEnum.SUCCESS.name())
                .build();

        if (chatResponse != null && chatResponse.tokenUsage() != null) {
            TokenUsage tokenUsage = chatResponse.tokenUsage();
            callLog.setPromptTokens(tokenUsage.inputTokenCount());
            callLog.setCompletionTokens(tokenUsage.outputTokenCount());
            callLog.setTotalTokens(tokenUsage.totalTokenCount());
        }
        llmCallLogService.saveAsync(callLog);
    }

    /**
     * 保存失败的调用记录。
     */
    private void saveFailedCallLog(CallLogContext context, String errorMessage, long processingTimeMs) {
        llmCallLogService.saveAsync(buildCallLog(context, processingTimeMs)
            .status(LlmCallStatusEnum.FAILED.name())
            .errorMessage(errorMessage)
            .build());
    }

    /**
     * 组装成功和失败记录共有的调用字段。
     */
    private LlmCallLog.LlmCallLogBuilder buildCallLog(CallLogContext context, long processingTimeMs) {
        CallContext call = context.getCallContext();
        return LlmCallLog.builder()
            .callScene(context.getCallScene() == null ? null : context.getCallScene().name())
            .agentId(call.getAgentId())
            .agentName(call.getAgentName())
            .llmProviderId(call.getLlmProviderId())
            .llmProviderName(call.getLlmProviderName())
            .modelName(call.getModelName())
            .requestContent(context.getRequestContent())
            .processingTimeMs(processingTimeMs)
            .isStreaming(context.isStreaming() ? CommonConstants.ENABLED : CommonConstants.DISABLED)
            .skillReadCalls(call.skillReadCalls())
            .userId(call.getUserId());
    }

    // ==================== 内部方法：Agent 解析 ====================

    /**
     * 根据小说 ID 和角色解析 Agent 实体。
     *
     * @param novelId 小说 ID
     * @param role    Agent 角色
     * @return Agent 实体，未找到时返回 null
     */
    private Agent resolveAgent(Long novelId, AgentRoleEnum role) {
        NovelAgentConfig config = novelAgentConfigRepository.findByNovelId(novelId).orElse(null);
        if (config == null) {
            return null;
        }
        Long agentId = extractAgentId(config, role);
        if (agentId == null) {
            return null;
        }
        return agentRepository.findById(agentId).orElse(null);
    }

    /**
     * 从配置中提取对应角色的 Agent ID。
     *
     * @param config 小说 Agent 配置
     * @param role   Agent 角色
     * @return Agent ID
     */
    private Long extractAgentId(NovelAgentConfig config, AgentRoleEnum role) {
        return switch (role) {
            case AUTHOR -> config.getAuthorAgentId();
            case EDITOR -> config.getEditorAgentId();
            case QA -> config.getQaAgentId();
        };
    }

}
