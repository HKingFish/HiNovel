package cn.haowl.hinovel.ai.domain.entity;

import cn.haowl.hinovel.ai.constant.LlmCallSceneEnum;
import cn.haowl.hinovel.ai.constant.LlmCallStatusEnum;
import cn.haowl.hinovel.common.constant.CommonConstants;
import cn.haowl.hinovel.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.EnumUtils;

import java.util.List;

/**
 * LLM 调用记录实体。
 *
 * 记录每次 LLM 调用的请求、响应、Token 消耗、耗时等信息，
 * 用于调用追踪、成本分析和问题排查。
 *
 * @author haowl
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "ai_llm_call_log", autoResultMap = true)
public class LlmCallLog extends BaseEntity {

    /**
     * 毫秒转秒的除数。
     */
    private static final double MILLIS_PER_SECOND = 1000.0;

    /**
     * 调用场景编码。
     *
     * @see LlmCallSceneEnum
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
     * 调用状态编码。
     *
     * @see LlmCallStatusEnum
     */
    private String status;

    /**
     * 错误信息（调用失败时记录）。
     */
    private String errorMessage;

    /**
     * 当次 Skill 读取的有序记录。
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<SkillReadCall> skillReadCalls;

    /**
     * 兼容上线前的空记录。
     */
    public List<SkillReadCall> getSkillReadCalls() {
        return skillReadCalls == null ? List.of() : skillReadCalls;
    }

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
    public static LlmCallLog createSuccess(
        LlmCallSceneEnum callScene, Long agentId, String agentName, Long llmProviderId,
        String llmProviderName, String modelName) {
        LlmCallLog log = new LlmCallLog();
        log.setCallScene(callScene == null ? null : callScene.name());
        log.setAgentId(agentId);
        log.setAgentName(agentName);
        log.setLlmProviderId(llmProviderId);
        log.setLlmProviderName(llmProviderName);
        log.setModelName(modelName);
        log.setStatus(LlmCallStatusEnum.SUCCESS.name());
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
    public static LlmCallLog createFailed(LlmCallSceneEnum callScene, String modelName, String errorMessage) {
        LlmCallLog log = new LlmCallLog();
        log.setCallScene(callScene == null ? null : callScene.name());
        log.setModelName(modelName);
        log.setStatus(LlmCallStatusEnum.FAILED.name());
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
        this.status = LlmCallStatusEnum.FAILED.name();
        this.errorMessage = errorMessage;
    }

    // ==================== 状态判断 ====================

    /**
     * 判断调用是否成功。
     *
     * @return 成功返回 true
     */
    public boolean isSuccess() {
        return LlmCallStatusEnum.SUCCESS == EnumUtils.getEnum(LlmCallStatusEnum.class, this.status);
    }

    /**
     * 判断调用是否失败。
     *
     * @return 失败返回 true
     */
    public boolean isFailed() {
        return LlmCallStatusEnum.FAILED == EnumUtils.getEnum(LlmCallStatusEnum.class, this.status);
    }

    /**
     * 判断是否为流式调用。
     *
     * @return 流式调用返回 true
     */
    public boolean isStreamingCall() {
        return Integer.valueOf(CommonConstants.ENABLED).equals(this.isStreaming);
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
