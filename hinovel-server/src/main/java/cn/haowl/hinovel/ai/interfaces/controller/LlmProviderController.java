package cn.haowl.hinovel.ai.interfaces.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.haowl.hinovel.ai.constant.LlmProviderType;
import cn.haowl.hinovel.ai.domain.entity.LlmProvider;
import cn.haowl.hinovel.ai.domain.repository.LlmProviderRepository;
import cn.haowl.hinovel.ai.domain.service.LlmProviderDomainService;
import cn.haowl.hinovel.common.response.ApiResponse;
import cn.haowl.hinovel.common.service.ApiKeyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@RestController
@RequestMapping("/api/ai/llm-providers")
@RequiredArgsConstructor
public class LlmProviderController {

    private final LlmProviderDomainService llmProviderDomainService;
    private final LlmProviderRepository llmProviderRepository;
    private final ApiKeyService apiKeyService;

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
        Long userId = StpUtil.getLoginIdAsLong();
        List<LlmProvider> providers = llmProviderRepository.findAllByUserId(userId);
        providers.forEach(this::maskProviderApiKey);
        return ApiResponse.success(providers);
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
        LlmProvider provider = llmProviderDomainService.getById(id);
        maskProviderApiKey(provider);
        return ApiResponse.success(provider);
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
        Long userId = StpUtil.getLoginIdAsLong();
        log.info("创建 LLM 提供方，名称：{}，userId={}", request.getName(), userId);
        LlmProvider provider = llmProviderDomainService.create(
                request.getName(),
                request.getProviderType(),
                request.getBaseUrl(),
                request.getApiKey(),
                request.getModels(),
                userId
        );
        maskProviderApiKey(provider);
        return ApiResponse.success(provider);
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
        log.info("更新 LLM 提供方，id={}，名称：{}", id, request.getName());
        LlmProvider provider = llmProviderDomainService.getById(id);
        provider.setName(request.getName());
        provider.setProviderType(request.getProviderType());
        provider.updateBaseUrl(request.getBaseUrl());
        provider.updateModels(request.getModels());

        // 仅当前端传入了新的明文 apiKey 时才重新加密
        String newApiKey = request.getApiKey();
        if (isNewApiKey(newApiKey)) {
            provider.updateApiKey(apiKeyService.encrypt(newApiKey));
        }

        LlmProvider saved = llmProviderRepository.save(provider);
        maskProviderApiKey(saved);
        return ApiResponse.success(saved);
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
        log.info("删除 LLM 提供方，id={}", id);
        llmProviderDomainService.delete(id);
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
        log.info("激活 LLM 提供方，id={}", id);
        llmProviderDomainService.activate(id);
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
        log.info("停用 LLM 提供方，id={}", id);
        llmProviderDomainService.deactivate(id);
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

    /**
     * 提供方类型 VO。
     */
    @Data
    @AllArgsConstructor
    public static class ProviderTypeVO {

        /**
         * 类型编码
         */
        private String code;

        /**
         * 显示名称
         */
        private String displayName;

        /**
         * 官方 API 地址
         */
        private String officialBaseUrl;
    }

    /**
     * LLM 提供方请求 DTO。
     */
    @Data
    public static class LlmProviderRequest {

        /**
         * 提供方名称
         */
        private String name;

        /**
         * 提供方类型（OPENAI / QWEN / DEEPSEEK）
         */
        private String providerType;

        /**
         * API 基础 URL
         */
        private String baseUrl;

        /**
         * API 密钥
         */
        private String apiKey;

        /**
         * 支持的模型列表
         */
        private List<String> models;
    }

    // ==================== 内部辅助方法 ====================

    /**
     * 对提供方实体的 apiKey 进行脱敏处理。
     *
     * @param provider 提供方实体
     */
    private void maskProviderApiKey(LlmProvider provider) {
        if (provider != null && provider.getApiKey() != null) {
            provider.setApiKey(apiKeyService.decryptAndMask(provider.getApiKey()));
        }
    }

    /**
     * 判断前端传入的 apiKey 是否为新的明文密钥。
     *
     * <p>如果为空、包含脱敏标记 "****"，则认为未修改。</p>
     *
     * @param apiKey 前端传入的 apiKey
     * @return 是否为新的明文密钥
     */
    private boolean isNewApiKey(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            return false;
        }
        // 包含脱敏标记的不是新密钥
        return !apiKey.contains("****");
    }
}
