package cn.haowl.hinovel.novel.interfaces.controller;

import cn.haowl.hinovel.common.response.ApiResponse;
import cn.haowl.hinovel.novel.application.service.ChapterOutlineService;
import cn.haowl.hinovel.novel.domain.entity.ChapterOutline;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 章节大纲控制器
 */
@RestController
@RequestMapping("/api/novel/chapter-outline")
@RequiredArgsConstructor
public class NovelChapterOutlineController {

    private final ChapterOutlineService chapterOutlineService;

    /**
     * 获取章节大纲
     */
    @GetMapping("/chapter/{chapterId}")
    public ApiResponse<ChapterOutline> getByChapterId(@PathVariable Long chapterId) {
        ChapterOutline outline = chapterOutlineService.getByChapterId(chapterId);
        return ApiResponse.success(outline);
    }

    /**
     * 获取小说的所有章节大纲
     */
    @GetMapping("/novel/{novelId}")
    public ApiResponse<List<ChapterOutline>> getByNovelId(@PathVariable Long novelId) {
        List<ChapterOutline> outlines = chapterOutlineService.getByNovelId(novelId);
        return ApiResponse.success(outlines);
    }

    /**
     * 保存或更新章节大纲
     */
    @PostMapping
    public ApiResponse<ChapterOutline> saveOrUpdate(@RequestBody ChapterOutline outline) {
        ChapterOutline saved = chapterOutlineService.saveOrUpdateOutline(outline);
        return ApiResponse.success(saved);
    }

    /**
     * 删除章节大纲
     */
    @DeleteMapping("/chapter/{chapterId}")
    public ApiResponse<Void> deleteByChapterId(@PathVariable Long chapterId) {
        chapterOutlineService.deleteByChapterId(chapterId);
        return ApiResponse.success();
    }
}
