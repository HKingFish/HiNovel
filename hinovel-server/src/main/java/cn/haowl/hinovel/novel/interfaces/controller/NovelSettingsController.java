package cn.haowl.hinovel.novel.interfaces.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import cn.haowl.hinovel.common.response.ApiResponse;
import cn.haowl.hinovel.novel.application.service.NovelSettingsService;
import cn.haowl.hinovel.novel.domain.entity.NovelSettings;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 小说配置控制器。
 *
 * <p>提供小说级别和用户级别的 AI 功能配置管理接口。</p>
 *
 * @author haowl
 */
@Tag(name = "小说配置", description = "管理小说 AI 功能配置")
@RestController
@RequestMapping("/api/novel/settings")
@RequiredArgsConstructor
@SaCheckLogin
public class NovelSettingsController {

    private final NovelSettingsService novelSettingsService;

    /**
     * 获取小说的有效配置（合并后）
     */
    @Operation(summary = "获取小说有效配置", description = "获取小说级别与用户默认合并后的有效配置")
    @GetMapping("/{novelId}")
    public ApiResponse<NovelSettings> getEffectiveSettings(@PathVariable Long novelId) {
        Long userId = StpUtil.getLoginIdAsLong();
        NovelSettings settings = novelSettingsService.getEffectiveSettings(novelId, userId);
        return ApiResponse.success(settings);
    }

    /**
     * 获取小说级别配置
     */
    @Operation(summary = "获取小说级别配置", description = "仅获取小说级别的配置（不含用户默认）")
    @GetMapping("/{novelId}/novel-level")
    public ApiResponse<NovelSettings> getNovelSettings(@PathVariable Long novelId) {
        NovelSettings settings = novelSettingsService.getNovelSettings(novelId);
        return ApiResponse.success(settings);
    }

    /**
     * 保存小说级别配置
     */
    @Operation(summary = "保存小说级别配置", description = "保存或更新小说级别的 AI 功能配置")
    @PutMapping("/{novelId}")
    public ApiResponse<NovelSettings> saveNovelSettings(
            @PathVariable Long novelId,
            @RequestBody NovelSettings settings) {
        NovelSettings saved = novelSettingsService.saveNovelSettings(novelId, settings);
        return ApiResponse.success(saved);
    }

    /**
     * 获取用户默认配置
     */
    @Operation(summary = "获取用户默认配置", description = "获取当前用户的默认 AI 功能配置")
    @GetMapping("/user-default")
    public ApiResponse<NovelSettings> getUserDefaultSettings() {
        Long userId = StpUtil.getLoginIdAsLong();
        NovelSettings settings = novelSettingsService.getUserDefaultSettings(userId);
        return ApiResponse.success(settings);
    }

    /**
     * 保存用户默认配置
     */
    @Operation(summary = "保存用户默认配置", description = "保存或更新当前用户的默认 AI 功能配置")
    @PutMapping("/user-default")
    public ApiResponse<NovelSettings> saveUserDefaultSettings(@RequestBody NovelSettings settings) {
        Long userId = StpUtil.getLoginIdAsLong();
        NovelSettings saved = novelSettingsService.saveUserDefaultSettings(userId, settings);
        return ApiResponse.success(saved);
    }
}
