package cn.haowl.hinovel.agent.interfaces.dto;

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

    @Schema(description = "Agent 名称（最多100字符）", example = "我的助手", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Agent 名称不能为空")
    @Size(max = CommonConstants.LENGTH_100, message = "Agent 名称不能超过100个字符")
    private String name;

    @Schema(description = "Agent 描述（最多500字符）", example = "一个通用助手")
    @Size(max = CommonConstants.LENGTH_500, message = "描述不能超过500个字符")
    private String description;

    @Schema(description = "系统提示词（System Prompt）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "系统提示词不能为空")
    private String systemPrompt;

    @Schema(description = "LLM 提供方 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "LLM 提供方 ID 不能为空")
    private Long llmProviderId;

    @Schema(description = "模型名称", example = "gpt-4o", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "模型名称不能为空")
    private String modelName;

    @Schema(description = "自定义模型 API 地址（可选，覆盖 LLM 提供方默认地址）", example = "https://api.example.com/v1")
    @Size(max = CommonConstants.LENGTH_500, message = "自定义模型地址不能超过500个字符")
    private String customBaseUrl;

    @Schema(description = "温度参数（0-2，值越高输出越随机）", example = "0.70", defaultValue = "0.70")
    @DecimalMin(value = "0.0", message = "temperature 最小值为 0")
    @DecimalMax(value = "2.0", message = "temperature 最大值为 2")
    private BigDecimal temperature = new BigDecimal("0.70");

    @Schema(description = "最大 Token 数（1-32768）", example = "2048", defaultValue = "2048")
    @Min(value = 1, message = "maxTokens 最小值为 1")
    @Max(value = 32768, message = "maxTokens 最大值为 32768")
    private Integer maxTokens = 2048;

    @Schema(description = "Top-P 参数（0-1，核采样概率）", example = "1.00", defaultValue = "1.00")
    @DecimalMin(value = "0.0", message = "topP 最小值为 0")
    @DecimalMax(value = "1.0", message = "topP 最大值为 1")
    private BigDecimal topP = new BigDecimal("1.00");

    @Schema(description = "ReAct 最大迭代次数（1-50，防止无限循环）", example = "10", defaultValue = "10")
    @Min(value = 1, message = "maxIterations 最小值为 1")
    @Max(value = 50, message = "maxIterations 最大值为 50")
    private Integer maxIterations = 10;

    @Schema(description = "角色模板 ID（可选，选择后自动预填充系统提示词）")
    private Long roleId;

    @Schema(description = "绑定的 MCP Server ID 列表")
    private List<Long> mcpServerIds;
}
