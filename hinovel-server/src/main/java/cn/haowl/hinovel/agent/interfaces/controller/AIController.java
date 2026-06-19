package cn.haowl.hinovel.agent.interfaces.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.haowl.hinovel.agent.application.service.agent.AuthorAgent;
import cn.haowl.hinovel.agent.application.service.agent.EditorAgent;
import cn.haowl.hinovel.agent.application.service.agent.QaAgent;
import cn.haowl.hinovel.agent.constant.AgentConstants;
import cn.haowl.hinovel.agent.domain.entity.ChatMessage;
import cn.haowl.hinovel.agent.domain.entity.ChatSession;
import cn.haowl.hinovel.common.response.ApiResponse;
import cn.haowl.hinovel.novel.application.service.ChapterOutlineService;
import cn.haowl.hinovel.novel.application.service.ChapterService;
import cn.haowl.hinovel.novel.application.service.NovelChapterService;
import cn.haowl.hinovel.novel.application.service.NovelSettingsService;
import cn.haowl.hinovel.novel.domain.entity.ChapterOutline;
import cn.haowl.hinovel.novel.domain.entity.NovelChapter;
import cn.haowl.hinovel.novel.domain.entity.NovelOutline;
import cn.haowl.hinovel.novel.domain.entity.NovelSettings;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

import static cn.haowl.hinovel.common.constant.CommonConstants.ENABLED;

/**
 * AI 功能控制器。
 *
 * <p>提供 AI 改写、审核等 SubAgents 系统功能。</p>
 *
 * @author haowl
 * @since 2024
 */
@Tag(name = "AI 功能", description = "AI 改写、审核、大纲生成等功能")
@Slf4j
@RestController
@RequestMapping("/api/agent/ai")
@RequiredArgsConstructor
public class AIController {

    private final AuthorAgent authorAgentService;
    private final EditorAgent editorAgentService;
    private final QaAgent qaAgentService;
    private final ChapterService chapterService;
    private final ChapterOutlineService chapterOutlineService;
    private final NovelSettingsService novelSettingsService;
    private final NovelChapterService novelChapterService;


    /**
     * AI 流式改写功能（SSE）。
     *
     * <p>根据章节大纲调用作者 Agent 流式改写章节内容，
     * 通过 SSE 逐 Token 推送生成结果，前端实时展示。
     * 使用 {@link ServerSentEvent} 精确控制 SSE 帧格式，避免 String 被 JSON 序列化加引号。</p>
     *
     * @param request 改写请求
     * @return SSE 流式响应，逐 Token 推送生成内容
     */
    @Operation(summary = "AI 流式改写", description = "根据大纲流式改写章节内容（SSE）")
    @PostMapping(value = "/rewrite", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> aiRewrite(@RequestBody AIAuthorRequest request) {
        NovelChapter chapter = chapterService.getById(request.getChapterId());
        if (chapter == null) {
            log.warn("AI 改写失败，章节不存在，chapterId={}", request.getChapterId());
            return Flux.just(ServerSentEvent.<String>builder()
                    .event("error")
                    .data("章节不存在")
                    .build());
        }

        // 获取章节大纲（优先使用请求中的大纲内容）
        ChapterOutline outline = chapterOutlineService.getByChapterId(request.getChapterId());
        if (outline == null) {
            outline = new ChapterOutline();
            outline.setChapterId(request.getChapterId());
            outline.setNovelId(chapter.getNovelId());
        }
        // 请求中携带了大纲内容时，覆盖数据库中的大纲
        if (request.getOutline() != null && !request.getOutline().isBlank()) {
            outline.setOutlineContent(request.getOutline());
        }
        if (request.getPlotPoints() != null && !request.getPlotPoints().isBlank()) {
            outline.setPlotPoints(request.getPlotPoints());
        }
        if (request.getEmotionTone() != null && !request.getEmotionTone().isBlank()) {
            outline.setEmotionTone(request.getEmotionTone());
        }
        if (request.getSceneSetting() != null && !request.getSceneSetting().isBlank()) {
            outline.setSceneSetting(request.getSceneSetting());
        }
        // 章节内容以前端传过来的为准
        if (request.getContent() != null) {
            outline.setChapterContent(request.getContent());
        }

        // 读取小说有效配置，决定改写时携带的上下文
        NovelSettings settings = novelSettingsService.getEffectiveSettings(
                chapter.getNovelId(), chapter.getCreator());

        // 根据配置加载前几章内容作为前情提要
        List<NovelChapter> previousChapters = List.of();
        int contextChapters = settings.getRewriteContextChapters() != null
                ? settings.getRewriteContextChapters() : 0;
        if (contextChapters > 0) {
            previousChapters = novelChapterService.getPreviousChapters(
                    chapter.getNovelId(), chapter.getChapterNumber(), contextChapters);
        }

        // 根据配置决定是否携带全文大纲
        String novelOutlineContent = null;
        if (ENABLED == settings.getRewriteIncludeOutline()) {
            novelOutlineContent = loadNovelOutlineContent(chapter.getNovelId());
        }

        // 将 Flux<String> 包装为 Flux<ServerSentEvent>，避免 String 被 JSON 序列化加引号
        Long userId = StpUtil.getLoginIdAsLong();
        return authorAgentService.streamRewriteChapterByOutline(
                        chapter, outline, previousChapters,
                        novelOutlineContent, request.getUserRequirement(), userId)
                .map(token -> ServerSentEvent.<String>builder().data(token).build())
                .onErrorResume(e -> {
                    log.error("AI 改写流式异常，异常信息：{}", e.getMessage());
                    return Flux.just(ServerSentEvent.<String>builder()
                            .event("error")
                            .data(e.getMessage() != null ? e.getMessage() : "AI 改写异常")
                            .build());
                });
    }

    /**
     * AI 审核功能。
     *
     * <p>调用编辑 Agent 对章节内容进行多维度审核（一致性、逻辑性、人物、剧情），
     * 返回结构化的审核结果，包含问题列表和改进建议。</p>
     *
     * @param request 审核请求
     * @return 审核结果
     */
    @Operation(summary = "AI 审核", description = "审核章节内容的一致性和质量")
    @PostMapping("/audit")
    public ApiResponse<AIAuditorResponse> aiAudit(@RequestBody AIAuditorRequest request) {
        NovelChapter chapter = chapterService.getById(request.getChapterId());
        if (chapter == null) {
            return ApiResponse.error(404, "章节不存在");
        }

        // 使用请求中的内容覆盖数据库中的章节内容（前端可能传入编辑中的最新内容）
        if (request.getContent() != null && !request.getContent().isBlank()) {
            chapter.setContent(request.getContent());
        }

        // 获取章节大纲
        ChapterOutline outline = chapterOutlineService.getByChapterId(request.getChapterId());

        // 根据配置决定审核时是否携带全文大纲
        NovelSettings settings = novelSettingsService.getEffectiveSettings(
                chapter.getNovelId(), chapter.getCreator());
        String novelOutline = null;
        if (ENABLED == settings.getAuditIncludeOutline()) {
            // 优先使用前端传入的全文大纲，其次从数据库加载
            if (request.getNovelOutline() != null && !request.getNovelOutline().isBlank()) {
                novelOutline = request.getNovelOutline();
            } else {
                novelOutline = loadNovelOutlineContent(chapter.getNovelId());
            }
        }

        // 调用编辑 Agent 审核
        Long userId = StpUtil.getLoginIdAsLong();
        AIAuditorResponse response =
                editorAgentService.auditChapter(chapter, outline, novelOutline, userId);

        return ApiResponse.success(response);
    }

    /**
     * 批量审核功能。
     *
     * @param request 批量审核请求
     * @return 批量审核结果
     */
    @Operation(summary = "批量审核", description = "批量审核章节内容")
    @PostMapping("/audit/batch")
    public ApiResponse<AIAuditBatchResponse> aiBatchAudit(@RequestBody AIAuditBatchRequest request) {
        NovelChapter chapter = chapterService.getById(request.getChapterId());
        if (chapter == null) {
            return ApiResponse.error(404, "章节不存在");
        }

        AIAuditBatchResponse response = new AIAuditBatchResponse();
        response.setId("mock-batch-audit-id-1");
        response.setOverallAssessment(AgentConstants.AUDIT_PASS);
        response.setSummary("所有审核通过");
        response.setAudits(List.of());

        return ApiResponse.success(response);
    }

    /**
     * 重新生成内容。
     *
     * @param request 重新生成请求
     * @return 重新生成后的内容
     */
    @Operation(summary = "重新生成", description = "重新生成章节内容")
    @PostMapping("/regenerate")
    public ApiResponse<AIAuthorResponse> aiRegenerate(@RequestBody AIAuthorRequest request) {
        NovelChapter chapter = chapterService.getById(request.getChapterId());
        if (chapter == null) {
            return ApiResponse.error(404, "章节不存在");
        }

        AIAuthorResponse response = new AIAuthorResponse();
        response.setId("mock-regenerate-id-1");
        response.setContent("重新生成的内容...");
        response.setWordCount(response.getContent().length());
        response.setProcessingTime(1800);

        return ApiResponse.success(response);
    }

    /**
     * 加载小说全文大纲内容。
     *
     * @param novelId 小说 ID
     * @return 全文大纲内容，不存在或为空时返回 null
     */
    private String loadNovelOutlineContent(Long novelId) {
        NovelOutline novelOutline = novelChapterService.getNovelOutline(novelId);
        if (novelOutline == null || novelOutline.getOutlineContent() == null
                || novelOutline.getOutlineContent().isBlank()) {
            return null;
        }
        return novelOutline.getOutlineContent();
    }

    // ==================== AI 问答接口 ====================

    /**
     * 获取小说的问答会话列表。
     *
     * @param novelId 小说 ID
     * @return 会话列表
     */
    @Operation(summary = "获取问答会话列表", description = "获取指定小说的所有问答会话")
    @GetMapping("/chat/sessions/{novelId}")
    public ApiResponse<List<ChatSessionResponse>> listChatSessions(@PathVariable Long novelId) {
        List<ChatSession> sessions = qaAgentService.listSessions(novelId);
        List<ChatSessionResponse> responseList = sessions.stream()
                .map(ChatSessionResponse::fromEntity)
                .toList();
        return ApiResponse.success(responseList);
    }

    /**
     * 创建新的问答会话。
     *
     * @param request 创建会话请求
     * @return 新创建的会话
     */
    @Operation(summary = "创建问答会话", description = "为指定小说创建新的问答会话")
    @PostMapping("/chat/sessions")
    public ApiResponse<ChatSessionResponse> createChatSession(
            @RequestBody CreateSessionRequest request) {
        ChatSession session = qaAgentService.createSession(
                request.getNovelId(), request.getTitle());
        return ApiResponse.success(ChatSessionResponse.fromEntity(session));
    }

    /**
     * 删除问答会话。
     *
     * @param sessionId 会话 ID
     * @return 操作结果
     */
    @Operation(summary = "删除问答会话", description = "删除指定的问答会话及其所有消息")
    @DeleteMapping("/chat/sessions/{sessionId}")
    public ApiResponse<Void> deleteChatSession(@PathVariable Long sessionId) {
        qaAgentService.deleteSession(sessionId);
        return ApiResponse.success(null);
    }

    /**
     * 重命名问答会话。
     *
     * @param sessionId 会话 ID
     * @param request   重命名请求
     * @return 操作结果
     */
    @Operation(summary = "重命名问答会话", description = "修改指定会话的标题")
    @PutMapping("/chat/sessions/{sessionId}/rename")
    public ApiResponse<Void> renameChatSession(
            @PathVariable Long sessionId,
            @RequestBody RenameSessionRequest request) {
        qaAgentService.renameSession(sessionId, request.getTitle());
        return ApiResponse.success(null);
    }

    /**
     * 切换问答会话收藏状态。
     *
     * @param sessionId 会话 ID
     * @return 切换后的收藏状态
     */
    @Operation(summary = "切换会话收藏", description = "切换指定会话的收藏状态")
    @PutMapping("/chat/sessions/{sessionId}/favorite")
    public ApiResponse<Boolean> toggleChatSessionFavorite(@PathVariable Long sessionId) {
        boolean favorite = qaAgentService.toggleFavorite(sessionId);
        return ApiResponse.success(favorite);
    }

    /**
     * 保存中断的 AI 回复（用于用户主动停止问答场景）。
     *
     * @param request 保存请求
     * @return 操作结果
     */
    @Operation(summary = "保存中断回复", description = "保存流式问答中断时已生成的部分回复")
    @PostMapping("/chat/save-partial")
    public ApiResponse<Void> savePartialResponse(@RequestBody SavePartialRequest request) {
        qaAgentService.savePartialResponse(
                request.getSessionId(), request.getNovelId(), request.getContent());
        return ApiResponse.success(null);
    }

    /**
     * 获取会话的消息列表（全量，兼容旧接口）。
     */
    @Operation(summary = "获取会话消息", description = "获取指定会话的所有消息记录")
    @GetMapping("/chat/messages/{sessionId}")
    public ApiResponse<List<ChatMessageResponse>> listChatMessages(
            @PathVariable Long sessionId) {
        List<ChatMessage> messages = qaAgentService.listMessages(sessionId);
        List<ChatMessageResponse> responseList = messages.stream()
                .map(ChatMessageResponse::fromEntity)
                .toList();
        return ApiResponse.success(responseList);
    }

    /**
     * 分页获取会话的消息列表（游标分页，用于前端滚动加载）。
     *
     * @param sessionId 会话 ID
     * @param cursorId  游标消息 ID（null 表示从最新开始）
     * @param pageSize  每页条数（默认 30）
     * @return 消息列表（按时间正序）
     */
    @Operation(summary = "分页获取会话消息", description = "游标分页获取消息，用于滚动加载历史消息")
    @GetMapping("/chat/messages/{sessionId}/page")
    public ApiResponse<ChatMessagePageResponse> listChatMessagesPage(
            @PathVariable Long sessionId,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "30") int pageSize) {
        List<ChatMessage> messages = qaAgentService.listMessagesByCursor(
                sessionId, cursorId, pageSize);
        List<ChatMessageResponse> responseList = messages.stream()
                .map(ChatMessageResponse::fromEntity)
                .toList();
        // 如果返回的条数小于 pageSize，说明没有更多数据
        boolean hasMore = messages.size() >= pageSize;
        return ApiResponse.success(new ChatMessagePageResponse(responseList, hasMore));
    }

    /**
     * 撤回问答消息。
     *
     * <p>撤回后的消息在后续问答中不会发送给 LLM。</p>
     *
     * @param messageId 消息 ID
     * @return 操作结果
     */
    @Operation(summary = "撤回消息", description = "撤回指定的问答消息，撤回后不再参与 LLM 上下文构建")
    @PutMapping("/chat/messages/{messageId}/revoke")
    public ApiResponse<Void> revokeChatMessage(@PathVariable Long messageId) {
        qaAgentService.revokeMessage(messageId);
        return ApiResponse.success(null);
    }

    /**
     * AI 流式问答（SSE）。
     *
     * <p>接收用户问题，通过向量检索查询相关内容后流式生成回答。
     * 完成后自动保存用户消息和 AI 回复到数据库。</p>
     *
     * @param request 问答请求
     * @return SSE 流式响应
     */
    @Operation(summary = "AI 流式问答", description = "基于小说内容的智能问答（SSE 流式）")
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> aiChat(@RequestBody AiChatRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        return qaAgentService.streamChat(
                        request.getNovelId(), request.getSessionId(),
                        request.getQuestion(), userId)
                .map(token -> {
                    // 识别工具执行事件标记，发送不同类型的 SSE 事件
                    if (token.startsWith(QaAgent.TOOL_EXECUTED_EVENT_PREFIX)) {
                        String eventData = token.substring(
                                QaAgent.TOOL_EXECUTED_EVENT_PREFIX.length());
                        return ServerSentEvent.<String>builder()
                                .event("tool-executed")
                                .data(eventData)
                                .build();
                    }
                    return ServerSentEvent.<String>builder().data(token).build();
                })
                .onErrorResume(e -> {
                    log.error("AI 问答流式异常，异常信息：{}", e.getMessage());
                    return Flux.just(ServerSentEvent.<String>builder()
                            .event("error")
                            .data(e.getMessage() != null ? e.getMessage() : "AI 问答异常")
                            .build());
                });
    }

    // ========== 请求/响应类 ==========

    @Data
    public static class AIAuthorRequest {
        private Long chapterId;
        private String content;
        private String outline;
        private String plotPoints;
        private List<Long> characters;
        private String emotionTone;
        private String sceneSetting;
        /**
         * 用户临时需求（用户对改写结果不满意时，可补充具体要求）
         */
        private String userRequirement;
    }

    @Data
    public static class AIAuthorResponse {
        private String id;
        private String content;
        private List<String> suggestions;
        private int wordCount;
        private long processingTime;
    }

    @Data
    public static class AIAuditorRequest {
        private Long chapterId;
        private String content;
        private String novelOutline;
        private List<PreviousChapter> previousChapters;

        @Data
        public static class PreviousChapter {
            private Long chapterId;
            private int chapterNumber;
            private String content;
        }
    }

    @Data
    public static class AIAuditIssue {
        private String id;
        private Position position;
        private String originalText;
        private String issueType;
        private String reason;
        private String severity;
        private String suggestion;

        @Data
        public static class Position {
            private int start;
            private int end;
        }
    }

    @Data
    public static class AIAuditorResponse {
        private String id;
        private String overallAssessment;
        private List<AIAuditIssue> issues;
        private String summary;
        private long processingTime;
        private String auditorId;
        private List<Integer> coveredChapters;
    }

    @Data
    public static class AIAuditBatchRequest {
        private Long chapterId;
        private String content;
        private String novelOutline;
        private List<ChapterRange> chapterRanges;

        @Data
        public static class ChapterRange {
            private int startChapter;
            private int endChapter;
        }
    }

    @Data
    public static class AIAuditBatchResponse {
        private String id;
        private List<AIAuditorResponse> audits;
        private String overallAssessment;
        private String summary;
    }

    // ========== 问答请求/响应类 ==========

    @Data
    public static class AiChatRequest {
        /**
         * 小说 ID
         */
        private Long novelId;

        /**
         * 会话 ID
         */
        private Long sessionId;

        /**
         * 用户问题
         */
        private String question;
    }

    @Data
    public static class CreateSessionRequest {
        /**
         * 小说 ID
         */
        private Long novelId;

        /**
         * 会话标题（可选）
         */
        private String title;
    }

    @Data
    public static class RenameSessionRequest {
        /**
         * 新标题
         */
        private String title;
    }

    @Data
    public static class SavePartialRequest {
        /**
         * 会话 ID
         */
        private Long sessionId;

        /**
         * 小说 ID
         */
        private Long novelId;

        /**
         * 已生成的部分回复内容
         */
        private String content;
    }

    @Data
    public static class ChatSessionResponse {
        private Long id;
        private Long novelId;
        private String title;
        private Integer favorite;
        private String createTime;
        private String updateTime;

        /**
         * 从实体转换为响应对象。
         */
        public static ChatSessionResponse fromEntity(ChatSession session) {
            ChatSessionResponse response = new ChatSessionResponse();
            response.setId(session.getId());
            response.setNovelId(session.getNovelId());
            response.setTitle(session.getTitle());
            response.setFavorite(session.getFavorite());
            response.setCreateTime(session.getCreateTime() != null
                    ? session.getCreateTime().toString() : null);
            response.setUpdateTime(session.getUpdateTime() != null
                    ? session.getUpdateTime().toString() : null);
            return response;
        }
    }

    @Data
    public static class ChatMessageResponse {
        private Long id;
        private Long sessionId;
        private String role;
        private String content;
        private Integer revoked;
        private String createTime;

        /**
         * 从实体转换为响应对象。
         */
        public static ChatMessageResponse fromEntity(ChatMessage message) {
            ChatMessageResponse response = new ChatMessageResponse();
            response.setId(message.getId());
            response.setSessionId(message.getSessionId());
            response.setRole(message.getRole());
            response.setContent(message.getContent());
            response.setRevoked(message.getRevoked());
            response.setCreateTime(message.getCreateTime() != null
                    ? message.getCreateTime().toString() : null);
            return response;
        }
    }

    /**
     * 消息分页响应。
     */
    @Data
    @AllArgsConstructor
    public static class ChatMessagePageResponse {
        /**
         * 消息列表（按时间正序）
         */
        private List<ChatMessageResponse> messages;

        /**
         * 是否还有更早的消息
         */
        private boolean hasMore;
    }

}
