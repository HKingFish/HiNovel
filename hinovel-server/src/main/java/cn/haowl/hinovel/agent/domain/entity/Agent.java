package cn.haowl.hinovel.agent.domain.entity;

import cn.haowl.hinovel.agent.constant.AgentConstants;
import cn.haowl.hinovel.common.constant.CommonConstants;
import cn.haowl.hinovel.common.entity.BaseEntity;
import cn.haowl.hinovel.common.exception.BusinessException;
import cn.haowl.hinovel.common.exception.enums.GlobalErrorCodeConstants;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Agent 实体。
 *
 * <p>AI Agent 配置实体，包含模型参数和系统提示词。</p>
 *
 * @Author : haowl
 * @Date : 2026/3/12 23:30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("agent_config")
public class Agent extends BaseEntity {

    /**
     * 所属用户 ID。
     */
    private Long userId;

    /**
     * 关联角色模板 ID（可选）。
     */
    private Long roleId;

    /**
     * Agent 名称。
     */
    private String name;

    /**
     * Agent 描述。
     */
    private String description;

    /**
     * 系统提示词。
     */
    private String systemPrompt;

    /**
     * LLM 提供方 ID。
     */
    private Long llmProviderId;

    /**
     * 模型名称。
     */
    private String modelName;

    /**
     * 自定义模型 API 地址（覆盖 LLM 提供方默认地址）。
     */
    private String customBaseUrl;

    /**
     * 温度参数。
     */
    private BigDecimal temperature;

    /**
     * 最大 Token 数。
     */
    private Integer maxTokens;

    /**
     * Top-P 参数。
     */
    private BigDecimal topP;

    /**
     * 最大执行步骤数（ReAct 循环上限）。
     */
    private Integer maxIterations;

    /**
     * 是否内置 Agent。
     *
     * @see CommonConstants#ENABLED
     * @see CommonConstants#DISABLED
     */
    private Integer isBuiltin;

    /**
     * 排序序号（越小越靠前，仅内置 Agent 使用）。
     */
    private Integer sortOrder;

    /**
     * 创建用户自定义 Agent。
     *
     * @param userId       用户ID
     * @param name         名称
     * @param description  描述
     * @param systemPrompt 系统提示词
     * @return Agent 实体
     */
    public static Agent createUserAgent(Long userId, String name, String description, String systemPrompt) {
        if (userId == null || name == null || name.isBlank()) {
            throw new BusinessException(GlobalErrorCodeConstants.PARAM_ERROR);
        }
        Agent agent = new Agent();
        agent.setUserId(userId);
        agent.setName(name);
        agent.setDescription(description);
        agent.setSystemPrompt(systemPrompt);
        agent.setIsBuiltin(CommonConstants.DISABLED);
        agent.setTemperature(BigDecimal.valueOf(AgentConstants.DEFAULT_TEMPERATURE));
        agent.setMaxTokens(AgentConstants.DEFAULT_MAX_TOKENS);
        agent.setTopP(BigDecimal.ONE);
        agent.setMaxIterations(AgentConstants.DEFAULT_MAX_ITERATIONS);
        return agent;
    }

    /**
     * 创建内置 Agent。
     *
     * @param name         名称
     * @param description  描述
     * @param systemPrompt 系统提示词
     * @return Agent 实体
     */
    public static Agent createBuiltinAgent(String name, String description, String systemPrompt) {
        Agent agent = new Agent();
        agent.setUserId(0L);
        agent.setName(name);
        agent.setDescription(description);
        agent.setSystemPrompt(systemPrompt);
        agent.setIsBuiltin(CommonConstants.ENABLED);
        agent.setTemperature(BigDecimal.valueOf(AgentConstants.DEFAULT_TEMPERATURE));
        agent.setMaxTokens(AgentConstants.DEFAULT_MAX_TOKENS);
        agent.setTopP(BigDecimal.ONE);
        agent.setMaxIterations(AgentConstants.DEFAULT_MAX_ITERATIONS);
        return agent;
    }

    /**
     * 配置 LLM 模型。
     *
     * @param llmProviderId 提供方ID
     * @param modelName     模型名称
     */
    public void configureModel(Long llmProviderId, String modelName) {
        this.llmProviderId = llmProviderId;
        this.modelName = modelName;
    }

    /**
     * 温度参数最大值
     */
    private static final BigDecimal TEMPERATURE_MAX = new BigDecimal("2");

    /**
     * 配置温度参数。
     *
     * @param temperature 温度
     */
    public void configureTemperature(BigDecimal temperature) {
        if (temperature != null && (temperature.compareTo(BigDecimal.ZERO) < 0
                || temperature.compareTo(TEMPERATURE_MAX) > 0)) {
            throw new BusinessException(GlobalErrorCodeConstants.PARAM_ERROR);
        }
        this.temperature = temperature;
    }

    /**
     * 配置最大 Token 数。
     *
     * @param maxTokens 最大Token数
     */
    public void configureMaxTokens(Integer maxTokens) {
        if (maxTokens != null && maxTokens < 1) {
            throw new BusinessException(GlobalErrorCodeConstants.PARAM_ERROR);
        }
        this.maxTokens = maxTokens;
    }

    /**
     * 更新系统提示词。
     *
     * @param systemPrompt 系统提示词
     */
    public void updateSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    /**
     * 更新名称。
     *
     * @param name 名称
     */
    public void updateName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessException(GlobalErrorCodeConstants.PARAM_ERROR);
        }
        this.name = name;
    }

    /**
     * 更新描述。
     *
     * @param description 描述
     */
    public void updateDescription(String description) {
        this.description = description;
    }

    /**
     * 判断是否属于用户。
     *
     * @param userId 用户ID
     * @return 是否属于用户
     */
    public boolean belongsTo(Long userId) {
        return Objects.equals(this.userId, userId);
    }

    /**
     * 判断是否为内置 Agent。
     *
     * @return 是否内置
     */
    public boolean isBuiltin() {
        return Integer.valueOf(CommonConstants.ENABLED).equals(this.isBuiltin);
    }

    /**
     * 判断是否为用户自定义 Agent。
     *
     * @return 是否用户自定义
     */
    public boolean isUserDefined() {
        return Integer.valueOf(CommonConstants.DISABLED).equals(this.isBuiltin);
    }

    /**
     * 判断是否已配置模型。
     *
     * @return 是否已配置
     */
    public boolean hasModelConfigured() {
        return this.llmProviderId != null && this.modelName != null;
    }
}
