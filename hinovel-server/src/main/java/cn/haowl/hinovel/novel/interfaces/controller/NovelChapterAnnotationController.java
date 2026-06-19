package cn.haowl.hinovel.novel.interfaces.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.haowl.hinovel.agent.application.service.agent.AuthorAgent;
import cn.haowl.hinovel.common.response.ApiResponse;
import cn.haowl.hinovel.novel.application.service.ChapterAnnotationService;
import cn.haowl.hinovel.novel.domain.entity.ChapterAnnotation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 章节批注管理接口。
 *
 * <p>提供批注的增删改查及状态流转操作。</p>
 *
 * @author haowl
 */
@Tag(name = "章节批注管理", description = "批注 CRUD、状态流转、AI 改写结果管理")
@Slf4j
@RestController
@RequestMapping("/api/novel/annotations")
@RequiredArgsConstructor
public class NovelChapterAnnotationController {

    private final ChapterAnnotationService annotationService;
    private final AuthorAgent authorAgent;

    /**
     * 获取章节的所有批注。
     *
     * @param chapterId 章节 ID
     * @return 批注列表
     */
    @Operation(summary = "获取章节批注列表")
    @GetMapping("/chapter/{chapterId}")
    public ApiResponse<List<ChapterAnnotation>> getAnnotations(@PathVariable Long chapterId) {
        return ApiResponse.success(annotationService.getAnnotationsByChapterId(chapterId));
    }

    /**
     * 创建批注。
     *
     * @param request 创建批注请求
     * @return 创建后的批注
     */
    @Operation(summary = "创建批注")
    @PostMapping
    public ApiResponse<ChapterAnnotation> createAnnotation(@RequestBody CreateAnnotationRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        ChapterAnnotation annotation = ChapterAnnotation.createSelfAnnotation(
                request.getChapterId(),
                request.getNovelId(),
                userId,
                request.getStartOffset(),
                request.getEndOffset(),
                request.getOriginalText(),
                request.getAnnotationContent()
        );
        return ApiResponse.success(annotationService.createAnnotation(annotation));
    }

    /**
     * 更新批注内容。
     *
     * @param annotationId 批注 ID
     * @param request      更新请求
     * @return 操作结果
     */
    @Operation(summary = "更新批注内容")
    @PutMapping("/{annotationId}")
    public ApiResponse<Void> updateAnnotation(
            @PathVariable Long annotationId,
            @RequestBody UpdateAnnotationRequest request) {
        ChapterAnnotation annotation = annotationService.getAnnotationById(annotationId);
        if (annotation == null) {
            return ApiResponse.error(404, "批注不存在");
        }
        annotation.setAnnotationContent(request.getAnnotationContent());
        annotationService.updateAnnotation(annotation);
        return ApiResponse.success();
    }

    /**
     * 保存 AI 改写结果。
     *
     * @param annotationId 批注 ID
     * @param request      改写结果请求
     * @return 操作结果
     */
    @Operation(summary = "保存 AI 改写结果")
    @PutMapping("/{annotationId}/ai-rewrite")
    public ApiResponse<Void> saveAiRewriteResult(
            @PathVariable Long annotationId,
            @RequestBody AiRewriteResultRequest request) {
        annotationService.saveAiRewriteResult(annotationId, request.getRewriteResult());
        return ApiResponse.success();
    }

    /**
     * 采纳批注（确认使用 AI 改写结果替换原文）。
     *
     * @param annotationId 批注 ID
     * @return 操作结果
     */
    @Operation(summary = "采纳批注")
    @PostMapping("/{annotationId}/accept")
    public ApiResponse<Void> acceptAnnotation(@PathVariable Long annotationId) {
        annotationService.acceptAnnotation(annotationId);
        return ApiResponse.success();
    }

    /**
     * 删除批注。
     *
     * @param annotationId 批注 ID
     * @return 操作结果
     */
    @Operation(summary = "删除批注")
    @DeleteMapping("/{annotationId}")
    public ApiResponse<Void> deleteAnnotation(@PathVariable Long annotationId) {
        annotationService.deleteAnnotation(annotationId);
        return ApiResponse.success();
    }

    /**
     * 标记批注为已查看。
     *
     * @param annotationId 批注 ID
     * @return 操作结果
     */
    @Operation(summary = "标记批注已查看")
    @PostMapping("/{annotationId}/view")
    public ApiResponse<Void> markAsViewed(@PathVariable Long annotationId) {
        annotationService.markAsViewed(annotationId);
        return ApiResponse.success();
    }

    /**
     * 标记批注为已处理。
     *
     * @param annotationId 批注 ID
     * @return 操作结果
     */
    @Operation(summary = "标记批注已处理")
    @PostMapping("/{annotationId}/resolve")
    public ApiResponse<Void> resolveAnnotation(@PathVariable Long annotationId) {
        annotationService.resolveAnnotation(annotationId);
        return ApiResponse.success();
    }

    /**
     * 批注 AI 改写（流式 SSE）。
     *
     * <p>将整章内容 + 被批注原文 + 改写要求发送给作者 Agent，
     * 流式返回改写结果，保证与上下文的连贯性。</p>
     *
     * @param request 批注改写请求
     * @return SSE 流式响应
     */
    @Operation(summary = "批注 AI 改写（流式）", description = "根据批注意见流式改写指定文本片段")
    @PostMapping(value = "/ai-rewrite-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> annotationAiRewriteStream(
            @RequestBody AnnotationRewriteRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();

        // 快捷改写模式：不关联批注，直接使用请求参数
        Long novelId;
        if (request.getAnnotationId() != null && request.getAnnotationId() > 0) {
            ChapterAnnotation annotation = annotationService.getAnnotationById(request.getAnnotationId());
            if (annotation == null) {
                log.warn("批注不存在，annotationId={}", request.getAnnotationId());
                return Flux.just(ServerSentEvent.<String>builder()
                        .event("error")
                        .data("批注不存在")
                        .build());
            }
            novelId = annotation.getNovelId();
        } else {
            // 快捷改写：通过 chapterId 获取 novelId
            novelId = request.getNovelId();
        }

        return authorAgent.streamAnnotationRewrite(
                        novelId,
                        request.getFullChapterContent(),
                        request.getOriginalText(),
                        request.getAnnotationContent(),
                        userId)
                .map(token -> ServerSentEvent.<String>builder().data(token).build())
                .concatWith(Flux.just(ServerSentEvent.<String>builder().data("[DONE]").build()))
                .onErrorResume(e -> {
                    log.error("批注 AI 改写流式异常，annotationId={}，异常信息：{}",
                            request.getAnnotationId(), e.getMessage());
                    return Flux.just(ServerSentEvent.<String>builder()
                            .event("error")
                            .data(e.getMessage() != null ? e.getMessage() : "AI 改写异常")
                            .build());
                });
    }

    // ==================== 请求 DTO ====================

    /**
     * 创建批注请求。
     */
    @Data
    public static class CreateAnnotationRequest {

        /**
         * 章节 ID
         */
        private Long chapterId;

        /**
         * 小说 ID
         */
        private Long novelId;

        /**
         * 批注起始字符偏移量
         */
        private Integer startOffset;

        /**
         * 批注结束字符偏移量
         */
        private Integer endOffset;

        /**
         * 被批注的原文内容
         */
        private String originalText;

        /**
         * 批注内容
         */
        private String annotationContent;
    }

    /**
     * 更新批注请求。
     */
    @Data
    public static class UpdateAnnotationRequest {

        /**
         * 批注内容
         */
        private String annotationContent;
    }

    /**
     * AI 改写结果请求。
     */
    @Data
    public static class AiRewriteResultRequest {

        /**
         * AI 改写结果文本
         */
        private String rewriteResult;
    }

    /**
     * 批注 AI 改写请求。
     */
    @Data
    public static class AnnotationRewriteRequest {

        /**
         * 批注 ID
         */
        private Long annotationId;

        /**
         * 章节 ID
         */
        private Long chapterId;

        /**
         * 整章正文内容（保证改写连贯性）
         */
        private String fullChapterContent;

        /**
         * 被批注的原文片段
         */
        private String originalText;

        /**
         /** 改写要求（用户输入的提示词） */
        private String annotationContent;

        /**
         * 小说 ID（快捷改写模式使用，不关联批注时需要传入）
         */
        private Long novelId;
    }
}
