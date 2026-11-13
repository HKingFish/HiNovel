package cn.haowl.hinovel.novel.interfaces.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.haowl.hinovel.agent.application.service.ChapterPublishService;
import cn.haowl.hinovel.common.response.ApiResponse;
import cn.haowl.hinovel.novel.application.service.NovelChapterService;
import cn.haowl.hinovel.novel.domain.entity.NovelChapter;
import cn.haowl.hinovel.novel.domain.entity.NovelChapterVersion;
import cn.haowl.hinovel.novel.domain.entity.NovelOutline;
import cn.haowl.hinovel.novel.interfaces.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 小说章节管理接口。
 *
 * <p>提供章节 CRUD、历史版本、小说大纲等功能。</p>
 *
 * @author haowl
 * @since 2024
 */
@Tag(name = "小说章节管理", description = "章节CRUD、历史版本、小说大纲")
@RestController
@RequestMapping("/api/novel/chapters")
public class NovelChapterController {

    /**
     * 章节应用服务。
     */
    @Autowired
    private NovelChapterService chapterService;

    /**
     * 章节发布服务。
     */
    @Autowired
    private ChapterPublishService chapterPublishService;

    /**
     * 获取小说的所有章节。
     *
     * @param novelId 小说 ID
     * @return 章节列表
     */
    @Operation(summary = "获取小说章节列表")
    @GetMapping("/novel/{novelId}")
    public ApiResponse<List<NovelChapter>> getNovelChapters(@PathVariable Long novelId) {
        List<NovelChapter> chapters = chapterService.getNovelChapters(novelId);
        return ApiResponse.success(chapters);
    }

    /**
     * 分页获取小说的章节（按章节号倒序）。
     *
     * @param novelId 小说 ID
     * @param query   分页与筛选条件
     * @return 章节列表
     */
    @Operation(summary = "分页获取小说章节列表")
    @GetMapping("/novel/{novelId}/page")
    public ApiResponse<ChapterPageResponse> getNovelChaptersPage(
            @PathVariable Long novelId,
            ChapterPageQuery query) {
        query.setNovelId(novelId);
        return ApiResponse.success(chapterService.getNovelChaptersPage(query));
    }

    /**
     * 获取章节详情。
     *
     * @param chapterId 章节 ID
     * @return 章节详情
     */
    @Operation(summary = "获取章节详情")
    @GetMapping("/{chapterId}")
    public ApiResponse<NovelChapter> getChapter(@PathVariable Long chapterId) {
        NovelChapter chapter = chapterService.getChapter(chapterId);
        return ApiResponse.success(chapter);
    }

    /**
     * 创建章节。
     *
     * @param chapter 章节信息
     * @return 创建的章节
     */
    @Operation(summary = "创建章节")
    @PostMapping
    public ApiResponse<NovelChapter> createChapter(@RequestBody NovelChapter chapter) {
        NovelChapter created = chapterService.createChapter(chapter);
        return ApiResponse.success(created);
    }

    /**
     * 更新章节。
     *
     * @param chapterId 章节 ID
     * @param chapter   章节信息
     * @return 更新后的章节
     */
    @Operation(summary = "更新章节")
    @PutMapping("/{chapterId}")
    public ApiResponse<NovelChapter> updateChapter(
            @PathVariable Long chapterId,
            @RequestBody NovelChapter chapter) {
        chapter.setId(chapterId);
        NovelChapter updated = chapterService.updateChapter(chapter);
        return ApiResponse.success(updated);
    }

    /**
     * 删除章节。
     *
     * @param chapterId 章节 ID
     * @return 无
     */
    @Operation(summary = "删除章节")
    @DeleteMapping("/{chapterId}")
    public ApiResponse<Void> deleteChapter(@PathVariable Long chapterId) {
        chapterService.deleteChapter(chapterId);
        return ApiResponse.success();
    }

    /**
     * 发布章节。
     *
     * <p>发布后异步触发 AI 总结大纲和向量存储。</p>
     *
     * @param chapterId 章节 ID
     * @return 发布结果
     */
    @Operation(summary = "发布章节", description = "发布章节，异步触发AI总结大纲和向量存储")
    @PostMapping("/{chapterId}/publish")
    public ApiResponse<PublishChapterResponse> publishChapter(@PathVariable Long chapterId) {
        return ApiResponse.success(
                chapterPublishService.publishChapter(chapterId, StpUtil.getLoginIdAsLong()));
    }

    /**
     * 手动同步章节内容到向量数据库。
     *
     * <p>适用于未自动入库或需要重新同步的场景，异步执行。</p>
     *
     * @param chapterId 章节 ID
     * @return 同步触发结果
     */
    @Operation(summary = "手动同步章节到向量数据库")
    @PostMapping("/{chapterId}/sync-vector")
    public ApiResponse<Void> syncChapterVector(@PathVariable Long chapterId) {
        chapterPublishService.syncChapterVector(chapterId, StpUtil.getLoginIdAsLong());
        return ApiResponse.success();
    }

    /**
     * 获取章节的 AI 生成大纲。
     *
     * @param chapterId 章节 ID
     * @return AI 大纲内容
     */
    @Operation(summary = "获取AI生成的章节大纲")
    @GetMapping("/{chapterId}/ai-outline")
    public ApiResponse<AiOutlineResponse> getAiOutline(@PathVariable Long chapterId) {
        return ApiResponse.success(chapterService.getAiOutline(chapterId));
    }

    /**
     * 将 AI 大纲替换为用户大纲。
     *
     * <p>用户确认后，将 aiOutlineContent 覆盖到 outlineContent。</p>
     *
     * @param chapterId 章节 ID
     * @return 替换后的大纲
     */
    @Operation(summary = "用AI大纲替换用户大纲")
    @PostMapping("/{chapterId}/replace-outline")
    public ApiResponse<Void> replaceOutlineWithAi(@PathVariable Long chapterId) {
        chapterService.replaceOutlineWithAi(chapterId);
        return ApiResponse.success();
    }

    /**
     * 获取章节历史版本。
     *
     * @param chapterId 章节 ID
     * @return 版本列表
     */
    @Operation(summary = "获取章节历史版本")
    @GetMapping("/{chapterId}/versions")
    public ApiResponse<List<NovelChapterVersion>> getChapterVersions(@PathVariable Long chapterId) {
        List<NovelChapterVersion> versions = chapterService.getChapterVersions(chapterId);
        return ApiResponse.success(versions);
    }

    /**
     * 恢复章节到指定版本。
     *
     * @param chapterId 章节 ID
     * @param versionId 版本 ID
     * @return 恢复后的章节
     */
    @Operation(summary = "恢复章节到指定版本")
    @PostMapping("/{chapterId}/restore/{versionId}")
    public ApiResponse<NovelChapter> restoreChapterVersion(
            @PathVariable Long chapterId,
            @PathVariable Long versionId) {
        NovelChapter chapter = chapterService.restoreChapterVersion(chapterId, versionId);
        return ApiResponse.success(chapter);
    }

    /**
     * 更新版本备注。
     *
     * @param chapterId 章节 ID
     * @param versionId 版本 ID
     * @param request   备注内容
     * @return 无
     */
    @Operation(summary = "更新版本备注")
    @PutMapping("/{chapterId}/versions/{versionId}/remark")
    public ApiResponse<Void> updateVersionRemark(
            @PathVariable Long chapterId,
            @PathVariable Long versionId,
            @RequestBody UpdateRemarkRequest request) {
        chapterService.updateVersionRemark(chapterId, versionId, request.getRemark());
        return ApiResponse.success();
    }

    /**
     * 获取小说大纲。
     *
     * @param novelId 小说 ID
     * @return 小说大纲
     */
    @Operation(summary = "获取小说大纲")
    @GetMapping("/outline/novel/{novelId}")
    public ApiResponse<NovelOutline> getNovelOutline(@PathVariable Long novelId) {
        NovelOutline outline = chapterService.getNovelOutline(novelId);
        return ApiResponse.success(outline);
    }

    /**
     * 保存小说大纲。
     *
     * @param outline 大纲信息
     * @return 保存后的大纲
     */
    @Operation(summary = "保存小说大纲")
    @PostMapping("/outline")
    public ApiResponse<NovelOutline> saveNovelOutline(@RequestBody NovelOutline outline) {
        NovelOutline saved = chapterService.saveNovelOutline(outline);
        return ApiResponse.success(saved);
    }

    /**
     * 修改章节号。
     *
     * @param chapterId 章节 ID
     * @param request   包含新章节号的请求体
     * @return 更新后的章节
     */
    @Operation(summary = "修改章节号")
    @PutMapping("/{chapterId}/chapter-number")
    public ApiResponse<NovelChapter> updateChapterNumber(
            @PathVariable Long chapterId,
            @RequestBody UpdateChapterNumberRequest request) {
        NovelChapter updated = chapterService.updateChapterNumber(chapterId, request.getChapterNumber());
        return ApiResponse.success(updated);
    }

    /**
     * 一键重排章节号，使章节号连续。
     *
     * @param novelId 小说 ID
     * @return 无
     */
    @Operation(summary = "一键重排章节号")
    @PostMapping("/novel/{novelId}/reorder")
    public ApiResponse<Void> reorderChapters(@PathVariable Long novelId) {
        chapterService.reorderChapters(novelId);
        return ApiResponse.success();
    }

    /**
     * 批量更新章节排序（拖拽排序）。
     *
     * @param novelId 小说 ID
     * @param request 排序请求（包含章节 ID 有序列表）
     * @return 无
     */
    @Operation(summary = "批量更新章节排序")
    @PutMapping("/novel/{novelId}/sort")
    public ApiResponse<Void> batchUpdateSort(
            @PathVariable Long novelId,
            @RequestBody BatchSortRequest request) {
        chapterService.batchUpdateSort(novelId, request.getChapterIds());
        return ApiResponse.success();
    }
}
