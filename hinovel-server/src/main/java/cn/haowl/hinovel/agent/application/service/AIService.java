package cn.haowl.hinovel.agent.application.service;

import cn.haowl.hinovel.agent.interfaces.controller.AIController;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

/**
 * AI 功能应用服务。
 *
 * <p>封装 AI 改写、审核等业务逻辑，将 Controller 层的业务逻辑下沉至此。</p>
 */
public interface AIService {

    /**
     * AI 流式改写功能。
     *
     * @param request 改写请求
     * @param userId  用户 ID
     * @return SSE 流式响应
     */
    Flux<ServerSentEvent<String>> aiRewrite(AIController.AIAuthorRequest request, Long userId);

    /**
     * AI 审核功能。
     *
     * @param request 审核请求
     * @param userId  用户 ID
     * @return 审核结果
     */
    AIController.AIAuditorResponse aiAudit(AIController.AIAuditorRequest request, Long userId);
}
