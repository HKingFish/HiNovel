package cn.haowl.hinovel.novel.interfaces.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.haowl.hinovel.common.response.ApiResponse;
import cn.haowl.hinovel.novel.application.service.NovelMemoService;
import cn.haowl.hinovel.novel.domain.entity.NovelMemo;
import cn.haowl.hinovel.novel.interfaces.dto.SaveMemoRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 小说创作手记接口。
 *
 * <p>提供创作手记的读取和保存功能，按小说 + 用户维度存储。</p>
 *
 * @author haowl
 */
@Tag(name = "创作手记", description = "小说创作手记管理")
@RestController
@RequestMapping("/api/novel/memo")
public class NovelMemoController {

    /**
     * 创作手记应用服务。
     */
    @Autowired
    private NovelMemoService novelMemoService;

    /**
     * 获取小说的创作手记。
     *
     * @param novelId 小说 ID
     * @return 手记内容
     */
    @Operation(summary = "获取创作手记")
    @GetMapping("/{novelId}")
    public ApiResponse<NovelMemo> getMemo(@PathVariable Long novelId) {
        Long userId = StpUtil.getLoginIdAsLong();
        return ApiResponse.success(novelMemoService.getMemo(novelId, userId));
    }

    /**
     * 保存创作手记（不存在则创建，存在则更新）。
     *
     * @param novelId 小说 ID
     * @param request 保存请求
     * @return 操作结果
     */
    @Operation(summary = "保存创作手记")
    @PutMapping("/{novelId}")
    public ApiResponse<Void> saveMemo(@PathVariable Long novelId, @RequestBody SaveMemoRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        novelMemoService.saveMemo(novelId, userId, request.getContent());
        return ApiResponse.success();
    }
}
