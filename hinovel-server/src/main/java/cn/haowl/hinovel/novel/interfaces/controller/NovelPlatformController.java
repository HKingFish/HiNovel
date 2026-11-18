package cn.haowl.hinovel.novel.interfaces.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.haowl.hinovel.common.response.ApiResponse;
import cn.haowl.hinovel.novel.application.service.ChapterService;
import cn.haowl.hinovel.novel.application.service.NovelListService;
import cn.haowl.hinovel.novel.application.service.NovelService;
import cn.haowl.hinovel.novel.domain.entity.Novel;
import cn.haowl.hinovel.novel.domain.entity.NovelChapter;
import cn.haowl.hinovel.novel.domain.entity.NovelChapterVersion;
import cn.haowl.hinovel.novel.domain.entity.NovelOutline;
import cn.haowl.hinovel.novel.interfaces.dto.ChapterAutoSaveRequest;
import cn.haowl.hinovel.novel.interfaces.dto.ChapterCreateRequest;
import cn.haowl.hinovel.novel.interfaces.dto.ChapterUpdateRequest;
import cn.haowl.hinovel.novel.interfaces.dto.NovelCreateRequest;
import cn.haowl.hinovel.novel.interfaces.dto.NovelListResponse;
import cn.haowl.hinovel.novel.interfaces.dto.NovelUpdateRequest;
import cn.haowl.hinovel.novel.interfaces.dto.OutlineUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 小说平台接口。
 *
 * 提供小说、章节、大纲的完整管理功能。
 *
 * @author haowl
 * @since 2024
 */
@Tag(name = "小说平台", description = "小说创作平台，支持小说管理、章节编辑、大纲编写")
@RestController
@RequestMapping("/api/novel/platform")
public class NovelPlatformController {

    /**
     * 小说应用服务。
     */
    @Autowired
    private NovelService novelService;

    /**
     * 章节应用服务。
     */
    @Autowired
    private ChapterService chapterService;

    /**
     * 小说列表服务。
     */
    @Autowired
    private NovelListService novelListService;

    // ==================== 小说管理 ====================

    /**
     * 创建小说。
     *
     * @param request 创建请求
     * @return 创建的小说
     */
    @Operation(summary = "创建小说", description = "创建一本新小说")
    @PostMapping("/novels")
    public ApiResponse<Novel> createNovel(@Valid @RequestBody NovelCreateRequest request) {
        return ApiResponse.success(novelService.createNovel(StpUtil.getLoginIdAsLong(), request));
    }

    /**
     * 更新小说。
     *
     * @param novelId 小说 ID
     * @param request 更新请求
     * @return 更新后的小说
     */
    @Operation(summary = "更新小说", description = "更新小说信息")
    @PutMapping("/novels/{novelId}")
    public ApiResponse<Novel> updateNovel(
            @PathVariable Long novelId,
            @Valid @RequestBody NovelUpdateRequest request) {
        return ApiResponse.success(novelService.updateNovel(StpUtil.getLoginIdAsLong(), novelId, request));
    }

    /**
     * 删除小说。
     *
     * @param novelId 小说 ID
     * @return 无
     */
    @Operation(summary = "删除小说", description = "删除小说及其所有章节")
    @DeleteMapping("/novels/{novelId}")
    public ApiResponse<Void> deleteNovel(@PathVariable Long novelId) {
        novelService.deleteNovel(StpUtil.getLoginIdAsLong(), novelId);
        return ApiResponse.success();
    }

    /**
     * 获取小说详情。
     *
     * @param novelId 小说 ID
     * @return 小说详情
     */
    @Operation(summary = "获取小说详情", description = "获取小说基本信息")
    @GetMapping("/novels/{novelId}")
    public ApiResponse<Novel> getNovel(@PathVariable Long novelId) {
        return ApiResponse.success(novelService.getNovelById(StpUtil.getLoginIdAsLong(), novelId));
    }

    /**
     * 获取我的小说列表。
     *
     * @return 小说列表
     */
    @Operation(summary = "获取我的小说列表", description = "获取当前用户的所有小说")
    @GetMapping("/novels")
    public ApiResponse<List<NovelListResponse>> listMyNovels() {
        return ApiResponse.success(novelListService.listUserNovelsWithAgentConfig(StpUtil.getLoginIdAsLong()));
    }

    // ==================== 大纲管理 ====================

    /**
     * 获取大纲。
     *
     * @param novelId 小说 ID
     * @return 小说大纲
     */
    @Operation(summary = "获取大纲", description = "获取小说大纲")
    @GetMapping("/novels/{novelId}/outline")
    public ApiResponse<NovelOutline> getOutline(@PathVariable Long novelId) {
        return ApiResponse.success(novelService.getOutline(StpUtil.getLoginIdAsLong(), novelId));
    }

    /**
     * 更新大纲。
     *
     * @param novelId 小说 ID
     * @param request 更新请求
     * @return 更新后的大纲
     */
    @Operation(summary = "更新大纲", description = "更新小说大纲")
    @PutMapping("/novels/{novelId}/outline")
    public ApiResponse<NovelOutline> updateOutline(
            @PathVariable Long novelId,
            @Valid @RequestBody OutlineUpdateRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        request.setNovelId(novelId);
        return ApiResponse.success(novelService.updateOutline(userId, request));
    }

    // ==================== 章节管理 ====================

    /**
     * 创建章节。
     *
     * @param request 创建请求
     * @return 创建的章节
     */
    @Operation(summary = "创建章节", description = "创建新章节")
    @PostMapping("/chapters")
    public ApiResponse<NovelChapter> createChapter(@Valid @RequestBody ChapterCreateRequest request) {
        return ApiResponse.success(
            chapterService.createChapter(StpUtil.getLoginIdAsLong(), request.toCommand()));
    }

    /**
     * 更新章节。
     *
     * @param chapterId 章节 ID
     * @param request   更新请求
     * @return 更新后的章节
     */
    @Operation(summary = "更新章节", description = "更新章节内容和标题")
    @PutMapping("/chapters/{chapterId}")
    public ApiResponse<NovelChapter> updateChapter(
            @PathVariable Long chapterId,
            @Valid @RequestBody ChapterUpdateRequest request) {
        return ApiResponse.success(chapterService.updateChapter(StpUtil.getLoginIdAsLong(), chapterId, request));
    }

    /**
     * 自动保存章节。
     *
     * @param chapterId 章节 ID
     * @param request   保存请求
     * @return 保存后的章节
     */
    @Operation(summary = "自动保存章节", description = "自动保存章节内容，支持diff")
    @PostMapping("/chapters/{chapterId}/auto-save")
    public ApiResponse<NovelChapter> autoSaveChapter(
            @PathVariable Long chapterId,
            @Valid @RequestBody ChapterAutoSaveRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        request.setChapterId(chapterId);
        return ApiResponse.success(chapterService.autoSaveChapter(userId, request));
    }

    /**
     * 删除章节。
     *
     * @param chapterId 章节 ID
     * @return 无
     */
    @Operation(summary = "删除章节", description = "删除指定章节")
    @DeleteMapping("/chapters/{chapterId}")
    public ApiResponse<Void> deleteChapter(@PathVariable Long chapterId) {
        chapterService.deleteChapter(StpUtil.getLoginIdAsLong(), chapterId);
        return ApiResponse.success();
    }

    /**
     * 获取章节详情。
     *
     * @param chapterId 章节 ID
     * @return 章节详情
     */
    @Operation(summary = "获取章节详情", description = "获取章节完整内容")
    @GetMapping("/chapters/{chapterId}")
    public ApiResponse<NovelChapter> getChapter(@PathVariable Long chapterId) {
        return ApiResponse.success(chapterService.getChapterById(StpUtil.getLoginIdAsLong(), chapterId));
    }

    /**
     * 获取小说章节列表。
     *
     * @param novelId 小说 ID
     * @return 章节列表
     */
    @Operation(summary = "获取小说章节列表", description = "获取小说的所有章节")
    @GetMapping("/novels/{novelId}/chapters")
    public ApiResponse<List<NovelChapter>> listChapters(@PathVariable Long novelId) {
        return ApiResponse.success(chapterService.listChaptersByNovel(StpUtil.getLoginIdAsLong(), novelId));
    }

    // ==================== 版本管理 ====================

    /**
     * 获取章节版本历史。
     *
     * @param chapterId 章节 ID
     * @param limit     限制数量
     * @return 版本列表
     */
    @Operation(summary = "获取章节版本历史", description = "获取章节的保存历史")
    @GetMapping("/chapters/{chapterId}/versions")
    public ApiResponse<List<NovelChapterVersion>> listChapterVersions(
            @PathVariable Long chapterId,
            @RequestParam(required = false) Integer limit) {
        return ApiResponse.success(chapterService.listChapterVersions(StpUtil.getLoginIdAsLong(), chapterId, limit));
    }

    /**
     * 恢复到指定版本。
     *
     * @param chapterId 章节 ID
     * @param versionId 版本 ID
     * @return 恢复后的章节
     */
    @Operation(summary = "恢复到指定版本", description = "将章节恢复到历史版本")
    @PostMapping("/chapters/{chapterId}/versions/{versionId}/restore")
    public ApiResponse<NovelChapter> restoreVersion(
            @PathVariable Long chapterId,
            @PathVariable Long versionId) {
        return ApiResponse.success(chapterService.restoreVersion(StpUtil.getLoginIdAsLong(), chapterId, versionId));
    }
}
