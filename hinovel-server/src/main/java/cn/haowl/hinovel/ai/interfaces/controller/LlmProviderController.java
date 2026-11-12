package cn.haowl.hinovel.ai.interfaces.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.haowl.hinovel.ai.application.llm.LlmProviderService;
import cn.haowl.hinovel.ai.constant.LlmProviderType;
import cn.haowl.hinovel.ai.domain.entity.LlmProvider;
import cn.haowl.hinovel.ai.interfaces.dto.LlmProviderRequest;
import cn.haowl.hinovel.ai.interfaces.dto.ProviderTypeVO;
import cn.haowl.hinovel.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * LLM 提供方管理控制器。
 *
 * <p>提供 LLM 提供方的增删改查、激活/停用等管理接口，
 * 供前端 LLM Provider 管理页面调用。</p>
 *
 * @author haowl
 */
@Tag(name = "LLM 提供方管理", description = "LLM 提供方的创建、查询、更新、删除、激活/停用")
@RestController
@RequestMapping("/api/ai/llm-providers")
public class LlmProviderController {

    /** LLM 提供方应用服务。 */
    @Autowired
    private LlmProviderService llmProviderService;

    /**
     * 获取所有 LLM 提供方列表。
     *
     * <p>返回的 apiKey 已脱敏处理，不暴露完整密钥。</p>
     *
     * @return 提供方列表（apiKey 已脱敏）
     */
    @Operation(summary = "获取提供方列表", description = "返回当前用户的所有 LLM 提供方（含激活和未激活）")
    @GetMapping
    public ApiResponse<List<LlmProvider>> list() {
        return ApiResponse.success(llmProviderService.listByUserId(StpUtil.getLoginIdAsLong()));
    }

    /**
     * 获取 LLM 提供方详情。
     *
     * <p>返回的 apiKey 已脱敏处理，不暴露完整密钥。</p>
     *
     * @param id 提供方 ID
     * @return 提供方详情（apiKey 已脱敏）
     */
    @Operation(summary = "获取提供方详情", description = "根据 ID 查询 LLM 提供方详情")
    @GetMapping("/{id}")
    public ApiResponse<LlmProvider> getById(@PathVariable Long id) {
        return ApiResponse.success(llmProviderService.getByIdForView(id));
    }

    /**
     * 创建 LLM 提供方。
     *
     * @param request 创建请求（apiKey 为明文，将自动加密存储）
     * @return 创建后的提供方实体（apiKey 已脱敏）
     */
    @Operation(summary = "创建提供方", description = "创建新的 LLM 提供方配置")
    @PostMapping
    public ApiResponse<LlmProvider> create(@RequestBody LlmProviderRequest request) {
        return ApiResponse.success(
                llmProviderService.create(StpUtil.getLoginIdAsLong(), request));
    }

    /**
     * 更新 LLM 提供方。
     *
     * <p>如果前端未修改 apiKey（传入空值或脱敏值），则保留原有加密密钥不变。</p>
     *
     * @param id      提供方 ID
     * @param request 更新请求
     * @return 更新后的提供方实体（apiKey 已脱敏）
     */
    @Operation(summary = "更新提供方", description = "更新 LLM 提供方的配置信息")
    @PutMapping("/{id}")
    public ApiResponse<LlmProvider> update(@PathVariable Long id,
                                           @RequestBody LlmProviderRequest request) {
        return ApiResponse.success(llmProviderService.update(id, request));
    }

    /**
     * 删除 LLM 提供方。
     *
     * @param id 提供方 ID
     * @return 空响应
     */
    @Operation(summary = "删除提供方", description = "删除指定的 LLM 提供方")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        llmProviderService.delete(id);
        return ApiResponse.success();
    }

    /**
     * 激活 LLM 提供方。
     *
     * @param id 提供方 ID
     * @return 空响应
     */
    @Operation(summary = "激活提供方", description = "激活指定的 LLM 提供方")
    @PutMapping("/{id}/activate")
    public ApiResponse<Void> activate(@PathVariable Long id) {
        llmProviderService.activate(id);
        return ApiResponse.success();
    }

    /**
     * 停用 LLM 提供方。
     *
     * @param id 提供方 ID
     * @return 空响应
     */
    @Operation(summary = "停用提供方", description = "停用指定的 LLM 提供方")
    @PutMapping("/{id}/deactivate")
    public ApiResponse<Void> deactivate(@PathVariable Long id) {
        llmProviderService.deactivate(id);
        return ApiResponse.success();
    }

    /**
     * 获取支持的提供方类型列表。
     *
     * @return 提供方类型信息列表
     */
    @Operation(summary = "获取提供方类型", description = "返回所有支持的 LLM 提供方类型及其官方 API 地址")
    @GetMapping("/types")
    public ApiResponse<List<ProviderTypeVO>> listProviderTypes() {
        List<ProviderTypeVO> types = Arrays.stream(LlmProviderType.values())
                .map(type -> new ProviderTypeVO(
                        type.name(),
                        type.getDisplayName(),
                        type.getOfficialBaseUrl()
                ))
                .toList();
        return ApiResponse.success(types);
    }
}
