package cn.haowl.hinovel.ai.interfaces.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.haowl.hinovel.ai.application.embedding.EmbeddingConfigService;
import cn.haowl.hinovel.ai.domain.entity.EmbeddingConfig;
import cn.haowl.hinovel.common.response.ApiResponse;
import cn.haowl.hinovel.common.service.ApiKeyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 嵌入式模型配置控制器。
 *
 * <p>提供嵌入式模型配置的增删改查、激活/停用接口，用户数据隔离。</p>
 *
 * @author wylon
 * @date 2026/3/23 14:56
 */
@Tag(name = "嵌入式模型配置", description = "Embedding 模型配置的创建、查询、更新、删除、激活/停用")
@Slf4j
@RestController
@RequestMapping("/api/ai/embedding-configs")
@RequiredArgsConstructor
public class EmbeddingConfigController {

    private final EmbeddingConfigService embeddingConfigService;
    private final ApiKeyService apiKeyService;

    /**
     * 获取当前用户的嵌入式模型配置列表。
     *
     * @return 配置列表（apiKey 已脱敏）
     */
    @Operation(summary = "获取配置列表", description = "返回当前用户的所有嵌入式模型配置")
    @GetMapping
    public ApiResponse<List<EmbeddingConfig>> list() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<EmbeddingConfig> configs = embeddingConfigService.listByUserId(userId);
        configs.forEach(this::maskApiKey);
        return ApiResponse.success(configs);
    }

    /**
     * 创建嵌入式模型配置。
     *
     * @param request 创建请求（apiKey 为明文，自动加密存储）
     * @return 创建后的配置（apiKey 已脱敏）
     */
    @Operation(summary = "创建配置", description = "创建新的嵌入式模型配置")
    @PostMapping
    public ApiResponse<EmbeddingConfig> create(@RequestBody EmbeddingConfigRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info("创建嵌入式模型配置，userId={}，name={}", userId, request.getName());
        EmbeddingConfig config = embeddingConfigService.create(
                userId,
                request.getName(),
                request.getModelType(),
                request.getBaseUrl(),
                request.getApiKey(),
                request.getModelName(),
                request.getDimensions()
        );
        maskApiKey(config);
        return ApiResponse.success(config);
    }

    /**
     * 更新嵌入式模型配置。
     *
     * @param id      配置 ID
     * @param request 更新请求
     * @return 更新后的配置（apiKey 已脱敏）
     */
    @Operation(summary = "更新配置", description = "更新嵌入式模型配置信息")
    @PutMapping("/{id}")
    public ApiResponse<EmbeddingConfig> update(@PathVariable Long id,
                                                @RequestBody EmbeddingConfigRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info("更新嵌入式模型配置，id={}，userId={}", id, userId);
        EmbeddingConfig config = embeddingConfigService.update(
                id, userId,
                request.getName(),
                request.getModelType(),
                request.getBaseUrl(),
                request.getApiKey(),
                request.getModelName(),
                request.getDimensions()
        );
        maskApiKey(config);
        return ApiResponse.success(config);
    }

    /**
     * 删除嵌入式模型配置。
     *
     * @param id 配置 ID
     * @return 空响应
     */
    @Operation(summary = "删除配置", description = "删除指定的嵌入式模型配置")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info("删除嵌入式模型配置，id={}，userId={}", id, userId);
        embeddingConfigService.delete(id, userId);
        return ApiResponse.success();
    }

    /**
     * 激活嵌入式模型配置。
     *
     * @param id 配置 ID
     * @return 空响应
     */
    @Operation(summary = "激活配置", description = "激活指定的嵌入式模型配置")
    @PutMapping("/{id}/activate")
    public ApiResponse<Void> activate(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info("激活嵌入式模型配置，id={}，userId={}", id, userId);
        embeddingConfigService.activate(id, userId);
        return ApiResponse.success();
    }

    /**
     * 停用嵌入式模型配置。
     *
     * @param id 配置 ID
     * @return 空响应
     */
    @Operation(summary = "停用配置", description = "停用指定的嵌入式模型配置")
    @PutMapping("/{id}/deactivate")
    public ApiResponse<Void> deactivate(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info("停用嵌入式模型配置，id={}，userId={}", id, userId);
        embeddingConfigService.deactivate(id, userId);
        return ApiResponse.success();
    }

    /**
     * 嵌入式模型配置请求 DTO。
     */
    @Data
    public static class EmbeddingConfigRequest {

        /** 配置名称 */
        private String name;

        /** 模型类型（openai/onnx） */
        private String modelType;

        /** API 基础地址 */
        private String baseUrl;

        /** API 密钥 */
        private String apiKey;

        /** 模型名称 */
        private String modelName;

        /** 向量维度 */
        private Integer dimensions;
    }

    // ==================== 内部辅助方法 ====================

    /**
     * 对配置实体的 apiKey 进行脱敏处理。
     *
     * @param config 配置实体
     */
    private void maskApiKey(EmbeddingConfig config) {
        if (config != null && config.getApiKey() != null) {
            config.setApiKey(apiKeyService.decryptAndMask(config.getApiKey()));
        }
    }
}
