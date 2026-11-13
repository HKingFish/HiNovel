package cn.haowl.hinovel.ai.interfaces.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.haowl.hinovel.ai.application.embedding.EmbeddingConfigService;
import cn.haowl.hinovel.ai.domain.entity.EmbeddingConfig;
import cn.haowl.hinovel.ai.interfaces.dto.EmbeddingConfigRequest;
import cn.haowl.hinovel.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
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
@RestController
@RequestMapping("/api/ai/embedding-configs")
public class EmbeddingConfigController {

    /**
     * 嵌入式模型配置应用服务。
     */
    @Autowired
    private EmbeddingConfigService embeddingConfigService;

    /**
     * 获取当前用户的嵌入式模型配置列表。
     *
     * @return 配置列表（apiKey 已脱敏）
     */
    @Operation(summary = "获取配置列表", description = "返回当前用户的所有嵌入式模型配置")
    @GetMapping
    public ApiResponse<List<EmbeddingConfig>> list() {
        return ApiResponse.success(embeddingConfigService.listByUserId(StpUtil.getLoginIdAsLong()));
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
        return ApiResponse.success(
                embeddingConfigService.create(StpUtil.getLoginIdAsLong(), request));
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
        return ApiResponse.success(
                embeddingConfigService.update(id, StpUtil.getLoginIdAsLong(), request));
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
        embeddingConfigService.delete(id, StpUtil.getLoginIdAsLong());
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
        embeddingConfigService.activate(id, StpUtil.getLoginIdAsLong());
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
        embeddingConfigService.deactivate(id, StpUtil.getLoginIdAsLong());
        return ApiResponse.success();
    }
}
