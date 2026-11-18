package cn.haowl.hinovel.novel.interfaces.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.haowl.hinovel.common.response.ApiResponse;
import cn.haowl.hinovel.novel.application.service.ChapterOutlineService;
import cn.haowl.hinovel.novel.domain.entity.ChapterOutline;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 章节大纲控制器
 */
@RestController
@RequestMapping("/api/novel/chapter-outline")
public class NovelChapterOutlineController {

    /**
     * 章节大纲应用服务。
     */
    @Autowired
    private ChapterOutlineService chapterOutlineService;

    /**
     * 获取章节大纲
     */
    @GetMapping("/chapter/{chapterId}")
    public ApiResponse<ChapterOutline> getByChapterId(@PathVariable Long chapterId) {
        return ApiResponse.success(chapterOutlineService.getByChapterId(chapterId, StpUtil.getLoginIdAsLong()));
    }

    /**
     * 获取小说的所有章节大纲
     */
    @GetMapping("/novel/{novelId}")
    public ApiResponse<List<ChapterOutline>> getByNovelId(@PathVariable Long novelId) {
        return ApiResponse.success(chapterOutlineService.getByNovelId(novelId, StpUtil.getLoginIdAsLong()));
    }

    /**
     * 保存或更新章节大纲
     */
    @PostMapping
    public ApiResponse<ChapterOutline> saveOrUpdate(@RequestBody ChapterOutline outline) {
        return ApiResponse.success(
            chapterOutlineService.saveOrUpdateOutline(outline, StpUtil.getLoginIdAsLong()));
    }

    /**
     * 删除章节大纲
     */
    @DeleteMapping("/chapter/{chapterId}")
    public ApiResponse<Void> deleteByChapterId(@PathVariable Long chapterId) {
        chapterOutlineService.deleteByChapterId(chapterId, StpUtil.getLoginIdAsLong());
        return ApiResponse.success();
    }
}
