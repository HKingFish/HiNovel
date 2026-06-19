package cn.haowl.hinovel.novel.interfaces.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.haowl.hinovel.common.response.ApiResponse;
import cn.haowl.hinovel.novel.domain.entity.NovelMemo;
import cn.haowl.hinovel.novel.infrastructure.mapper.NovelMemoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class NovelMemoController {

    private final NovelMemoMapper memoMapper;

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
        NovelMemo memo = memoMapper.selectByNovelIdAndUserId(novelId, userId);
        return ApiResponse.success(memo);
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
        NovelMemo memo = memoMapper.selectByNovelIdAndUserId(novelId, userId);
        if (memo == null) {
            memo = NovelMemo.builder()
                    .novelId(novelId)
                    .userId(userId)
                    .content(request.getContent())
                    .build();
            memoMapper.insert(memo);
        } else {
            memo.setContent(request.getContent());
            memoMapper.updateById(memo);
        }
        return ApiResponse.success();
    }

    /**
     * 保存手记请求。
     */
    @Data
    public static class SaveMemoRequest {

        /**
         * 手记内容（Markdown 格式）
         */
        private String content;
    }
}
