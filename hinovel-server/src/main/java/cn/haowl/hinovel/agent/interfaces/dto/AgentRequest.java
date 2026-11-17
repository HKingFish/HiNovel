package cn.haowl.hinovel.agent.interfaces.dto;

import cn.haowl.hinovel.agent.constant.AgentConstants;
import cn.haowl.hinovel.common.constant.CommonConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Agent 创建/更新请求 DTO
 */
@Schema(description = "Agent 创建/更新请求")
@Data
public class AgentRequest {

    /**
     * 最低采样温度。
     */
    private static final String MIN_TEMPERATURE = "0.0";

    /**
     * 最高采样温度。
     */
    private static final String MAX_TEMPERATURE = "2.0";

    /**
     * 未指定时使用的采样温度。
     */
    private static final String DEFAULT_TEMPERATURE = "0.70";

    /**
     * 最低核采样概率。
     */
    private static final String MIN_TOP_P = "0.0";

    /**
     * 最高核采样概率。
     */
    private static final String MAX_TOP_P = "1.0";

    /**
     * 未指定时使用的核采样概率。
     */
    private static final String DEFAULT_TOP_P = "1.00";

    /**
     * 生成 Token 数的上限。
     */
    private static final int MAX_ALLOWED_TOKENS = 32768;

    /**
     * 创建 Agent 时默认允许的生成 Token 数。
     */
    private static final int DEFAULT_MAX_TOKENS = 2048;

    /**
     * 工具调用迭代次数的上限。
     */
    private static final int MAX_ALLOWED_ITERATIONS = 50;

    /**
     * Agent 名称。
     */
    @Schema(description = "Agent 名称（最多100字符）", example = "我的助手", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Agent 名称不能为空")
    @Size(max = CommonConstants.LENGTH_100, message = "Agent 名称不能超过100个字符")
    private String name;

    /**
     * Agent 简介。
     */
    @Schema(description = "Agent 描述（最多500字符）", example = "一个通用助手")
    @Size(max = CommonConstants.LENGTH_500, message = "描述不能超过500个字符")
    private String description;

    /**
     * 系统提示词。
     */
    @Schema(description = "系统提示词（System Prompt）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "系统提示词不能为空")
    private String systemPrompt;

    /**
     * 模型提供方 ID。
     */
    @Schema(description = "LLM 提供方 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "LLM 提供方 ID 不能为空")
    private Long llmProviderId;

    /**
     * 模型名称。
     */
    @Schema(description = "模型名称", example = "gpt-4o", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "模型名称不能为空")
    private String modelName;

    /**
     * 自定义模型接口地址。
     */
    @Schema(description = "自定义模型 API 地址（可选，覆盖 LLM 提供方默认地址）", example = "https://api.example.com/v1")
    @Size(max = CommonConstants.LENGTH_500, message = "自定义模型地址不能超过500个字符")
    private String customBaseUrl;

    /**
     * 采样温度。
     */
    @Schema(description = "温度参数（0-2，值越高输出越随机）",
        example = DEFAULT_TEMPERATURE, defaultValue = DEFAULT_TEMPERATURE)
    @DecimalMin(value = MIN_TEMPERATURE, message = "temperature 最小值为 0")
    @DecimalMax(value = MAX_TEMPERATURE, message = "temperature 最大值为 2")
    private BigDecimal temperature = new BigDecimal(DEFAULT_TEMPERATURE);

    /**
     * 生成 Token 上限。
     */
    @Schema(description = "最大 Token 数（1-32768）", example = "2048", defaultValue = "2048")
    @Min(value = 1, message = "maxTokens 最小值为 1")
    @Max(value = MAX_ALLOWED_TOKENS, message = "maxTokens 最大值为 32768")
    private Integer maxTokens = DEFAULT_MAX_TOKENS;

    /**
     * 核采样概率。
     */
    @Schema(description = "Top-P 参数（0-1，核采样概率）", example = DEFAULT_TOP_P, defaultValue = DEFAULT_TOP_P)
    @DecimalMin(value = MIN_TOP_P, message = "topP 最小值为 0")
    @DecimalMax(value = MAX_TOP_P, message = "topP 最大值为 1")
    private BigDecimal topP = new BigDecimal(DEFAULT_TOP_P);

    /**
     * 工具调用迭代上限。
     */
    @Schema(description = "ReAct 最大迭代次数（1-50，防止无限循环）", example = "10", defaultValue = "10")
    @Min(value = 1, message = "maxIterations 最小值为 1")
    @Max(value = MAX_ALLOWED_ITERATIONS, message = "maxIterations 最大值为 50")
    private Integer maxIterations = AgentConstants.DEFAULT_MAX_ITERATIONS;

    /**
     * Agent 模板 ID。
     */
    @Schema(description = "角色模板 ID（可选，选择后自动预填充系统提示词）")
    private Long roleId;

    /**
     * 挂载的 MCP 服务 ID。
     */
    @Schema(description = "绑定的 MCP Server ID 列表")
    private List<Long> mcpServerIds;

    /**
     * 内置 Skill ID，更新时 null 表示保留原挂载。
     */
    @Schema(description = "绑定的 Skill ID 列表；更新时 null 表示不修改绑定，空数组表示清空绑定")
    private List<Long> skillIds;
}
