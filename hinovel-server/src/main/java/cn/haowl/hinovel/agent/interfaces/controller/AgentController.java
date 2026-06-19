package cn.haowl.hinovel.agent.interfaces.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.haowl.hinovel.agent.application.service.AgentService;
import cn.haowl.hinovel.agent.domain.entity.Agent;
import cn.haowl.hinovel.agent.interfaces.dto.AgentRequest;
import cn.haowl.hinovel.ai.domain.entity.LlmProvider;
import cn.haowl.hinovel.ai.domain.service.LlmProviderDomainService;
import cn.haowl.hinovel.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Agent 管理接口
 */
@Tag(name = "Agent 管理", description = "Agent 的创建、查询、更新、删除")
@RestController
@RequestMapping("/api/agent/agents")
@RequiredArgsConstructor
public class AgentController {

    private final AgentService agentService;
    private final LlmProviderDomainService llmProviderDomainService;

    /**
     * 创建 Agent
     */
    @Operation(summary = "创建 Agent", description = "创建新的 Agent 配置，关联 LLM 提供方和 MCP Server")
    @PostMapping
    public ApiResponse<Agent> create(@Valid @RequestBody AgentRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        return ApiResponse.success(agentService.create(userId, request));
    }

    /**
     * 获取当前用户可用的所有 Agent（自建 + 内置）
     */
    @Operation(summary = "获取可用 Agent 列表", description = "返回当前用户自建 Agent 和所有内置 Agent")
    @GetMapping("/available")
    public ApiResponse<List<Agent>> listAvailable() {
        Long userId = StpUtil.getLoginIdAsLong();
        return ApiResponse.success(agentService.listAvailable(userId));
    }

    /**
     * 更新 Agent 配置
     */
    @Operation(summary = "更新 Agent 配置", description = "更新 Agent 的提示词、模型参数等配置，仅限创建者操作")
    @PutMapping("/{id}")
    public ApiResponse<Agent> update(@PathVariable Long id,
                                     @Valid @RequestBody AgentRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        return ApiResponse.success(agentService.update(id, userId, request));
    }

    /**
     * 删除 Agent
     */
    @Operation(summary = "删除 Agent", description = "逻辑删除 Agent，内置 Agent 不可删除，仅限创建者操作")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        agentService.delete(id, userId);
        return ApiResponse.success();
    }

    /**
     * 获取激活的 LLM 提供方列表（供 Agent 创建时选择）
     */
    @Operation(summary = "获取 LLM 提供方列表", description = "返回所有激活的 LLM 提供方，供创建 Agent 时选择")
    @GetMapping("/llm-providers")
    public ApiResponse<List<LlmProvider>> listLlmProviders() {
        return ApiResponse.success(llmProviderDomainService.getActiveProviders());
    }

    /**
     * 获取 Agent 默认配置模板列表（内置 Agent 作为模板，供新建 Agent 时快速填充）
     */
    @Operation(summary = "获取默认配置模板", description = "返回所有内置 Agent 作为预设模板，供新建 Agent 时快速填充")
    @GetMapping("/default-configs")
    public ApiResponse<List<Agent>> listDefaultConfigs() {
        return ApiResponse.success(agentService.listBuiltin());
    }
}
