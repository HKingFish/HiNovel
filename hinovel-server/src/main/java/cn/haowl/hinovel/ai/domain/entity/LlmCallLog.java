package cn.haowl.hinovel.ai.domain.entity;

import cn.haowl.hinovel.common.constant.CommonConstants;
import cn.haowl.hinovel.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * LLM 调用记录实体。
 *
 * <p>记录每次 LLM 调用的请求、响应、Token 消耗、耗时等信息，
 * 用于调用追踪、成本分析和问题排查。</p>
 *
 * @author haowl
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("ai_llm_call_log")
public class LlmCallLog extends BaseEntity {

    /**
     * 调用状态常量：成功。
     */
    public static final String STATUS_SUCCESS = "SUCCESS";

    /**
     * 调用状态常量：失败。
     */
    public static final String STATUS_FAILED = "FAILED";

    /**
     * 毫秒转秒的除数。
     */
    private static final double MILLIS_PER_SECOND = 1000.0;

    /**
     * 调用场景（如 REWRITE、AUDIT、REGENERATE）。
     */
    private String callScene;

    /**
     * Agent ID。
     */
    private Long agentId;

    /**
     * Agent 名称。
     */
    private String agentName;

    /**
     * LLM 提供方 ID。
     */
    private Long llmProviderId;

    /**
     * LLM 提供方名称。
     */
    private String llmProviderName;

    /**
     * 模型名称。
     */
    private String modelName;

    /**
     * 请求内容（系统提示词 + 用户提示词摘要）。
     */
    private String requestContent;

    /**
     * 响应内容（LLM 返回内容摘要）。
     */
    private String responseContent;

    /**
     * 输入 Token 数。
     */
    private Integer promptTokens;

    /**
     * 输出 Token 数。
     */
    private Integer completionTokens;

    /**
     * 总 Token 数。
     */
    private Integer totalTokens;

    /**
     * 处理耗时（毫秒）。
     */
    private Long processingTimeMs;

    /**
     * 所属用户 ID（数据隔离字段）。
     */
    private Long userId;

    /**
     * 调用状态：SUCCESS / FAILED。
     */
    private String status;

    /**
     * 错误信息（调用失败时记录）。
     */
    private String errorMessage;

    /**
     * 是否流式调用。
     *
     * @see CommonConstants#ENABLED
     * @see CommonConstants#DISABLED
     */
    private Integer isStreaming;

    // ==================== 工厂方法 ====================

    /**
     * 创建成功的调用记录。
     *
     * @param callScene       调用场景
     * @param agentId         Agent ID
     * @param agentName       Agent 名称
     * @param llmProviderId   提供方 ID
     * @param llmProviderName 提供方名称
     * @param modelName       模型名称
     * @return 调用记录实体
     */
    public static LlmCallLog createSuccess(String callScene, Long agentId, String agentName,
                                           Long llmProviderId, String llmProviderName,
                                           String modelName) {
        LlmCallLog log = new LlmCallLog();
        log.setCallScene(callScene);
        log.setAgentId(agentId);
        log.setAgentName(agentName);
        log.setLlmProviderId(llmProviderId);
        log.setLlmProviderName(llmProviderName);
        log.setModelName(modelName);
        log.setStatus(STATUS_SUCCESS);
        return log;
    }

    /**
     * 创建失败的调用记录。
     *
     * @param callScene    调用场景
     * @param modelName    模型名称
     * @param errorMessage 错误信息
     * @return 调用记录实体
     */
    public static LlmCallLog createFailed(String callScene, String modelName, String errorMessage) {
        LlmCallLog log = new LlmCallLog();
        log.setCallScene(callScene);
        log.setModelName(modelName);
        log.setStatus(STATUS_FAILED);
        log.setErrorMessage(errorMessage);
        return log;
    }

    // ==================== 业务方法 ====================

    /**
     * 填充 Token 消耗信息。
     *
     * @param promptTokens     输入 Token 数
     * @param completionTokens 输出 Token 数
     */
    public void fillTokenUsage(int promptTokens, int completionTokens) {
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.totalTokens = promptTokens + completionTokens;
    }

    /**
     * 记录处理耗时。
     *
     * @param startTimeMillis 开始时间戳（毫秒）
     */
    public void recordProcessingTime(long startTimeMillis) {
        this.processingTimeMs = System.currentTimeMillis() - startTimeMillis;
    }

    /**
     * 标记为流式调用。
     */
    public void markAsStreaming() {
        this.isStreaming = CommonConstants.ENABLED;
    }

    /**
     * 标记调用失败。
     *
     * @param errorMessage 错误信息
     */
    public void markAsFailed(String errorMessage) {
        this.status = STATUS_FAILED;
        this.errorMessage = errorMessage;
    }

    // ==================== 状态判断 ====================

    /**
     * 判断调用是否成功。
     *
     * @return 成功返回 true
     */
    public boolean isSuccess() {
        return STATUS_SUCCESS.equals(this.status);
    }

    /**
     * 判断调用是否失败。
     *
     * @return 失败返回 true
     */
    public boolean isFailed() {
        return STATUS_FAILED.equals(this.status);
    }

    /**
     * 判断是否为流式调用。
     *
     * @return 流式调用返回 true
     */
    public boolean isStreamingCall() {
        return this.isStreaming != null && this.isStreaming == CommonConstants.ENABLED;
    }

    /**
     * 获取处理耗时（秒）。
     *
     * @return 耗时秒数，未记录时返回 0
     */
    public double getProcessingTimeSeconds() {
        if (this.processingTimeMs == null) {
            return 0;
        }
        return this.processingTimeMs / MILLIS_PER_SECOND;
    }
}
