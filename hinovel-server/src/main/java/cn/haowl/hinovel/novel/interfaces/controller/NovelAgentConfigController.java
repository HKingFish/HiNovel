package cn.haowl.hinovel.novel.interfaces.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.haowl.hinovel.common.response.ApiResponse;
import cn.haowl.hinovel.novel.application.service.NovelAgentConfigService;
import cn.haowl.hinovel.novel.interfaces.dto.NovelAgentConfigRequest;
import cn.haowl.hinovel.novel.interfaces.dto.NovelAgentConfigResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 小说 Agent 配置控制器
 */
@Tag(name = "小说Agent配置", description = "配置小说的作者和编辑Agent")
@RestController
@RequestMapping("/api/novel/agent-config")
@RequiredArgsConstructor
@SaCheckLogin
public class NovelAgentConfigController {

    private final NovelAgentConfigService novelAgentConfigService;

    /**
     * 获取小说的 Agent 配置
     */
    @Operation(summary = "获取小说Agent配置", description = "获取指定小说的作者和编辑Agent配置")
    @GetMapping("/{novelId}")
    public ApiResponse<NovelAgentConfigResponse> getConfig(@PathVariable Long novelId) {
        return ApiResponse.success(novelAgentConfigService.getConfig(novelId));
    }

    /**
     * 保存或更新小说的 Agent 配置
     */
    @Operation(summary = "保存小说Agent配置", description = "配置小说的作者和编辑Agent")
    @PutMapping("/{novelId}")
    public ApiResponse<NovelAgentConfigResponse> saveOrUpdateConfig(
            @PathVariable Long novelId,
            @Valid @RequestBody NovelAgentConfigRequest request) {
        return ApiResponse.success(novelAgentConfigService.saveOrUpdateConfig(novelId, request));
    }
}
