package cn.haowl.hinovel.agent.infrastructure.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * 通用 AI 助手接口。
 *
 * <p>利用 LangChain4J AiService 声明式能力，提供统一的系统提示词 + 用户提示词调用模式。
 * 所有 Agent 场景（改写、审核、总结等）均可复用此接口，
 * 通过不同的系统提示词和用户提示词实现不同的功能。</p>
 *
 * <p>该接口通过 {@link dev.langchain4j.service.AiServices#builder(Class)} 在运行时动态构建，
 * 不注册为 Spring Bean，以支持不同小说绑定不同 LLM 提供方的场景。</p>
 *
 * @author haowl
 */
public interface GenericAiAssistant {

    /**
     * 同步调用 LLM。
     *
     * @param systemPrompt 系统提示词（Agent 角色设定）
     * @param userPrompt   用户提示词（具体任务内容）
     * @return LLM 生成的完整响应文本
     */
    @SystemMessage("{{systemPrompt}}")
    @UserMessage("{{userPrompt}}")
    String chat(@V("systemPrompt") String systemPrompt,
                @V("userPrompt") String userPrompt);

    /**
     * 流式调用 LLM。
     *
     * <p>返回 {@link TokenStream}，由调用方转换为 {@code Flux<String>} 实现 SSE 推送。</p>
     *
     * @param systemPrompt 系统提示词（Agent 角色设定）
     * @param userPrompt   用户提示词（具体任务内容）
     * @return Token 流
     */
    @SystemMessage("{{systemPrompt}}")
    @UserMessage("{{userPrompt}}")
    TokenStream streamChat(@V("systemPrompt") String systemPrompt,
                           @V("userPrompt") String userPrompt);
}
