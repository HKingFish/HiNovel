package cn.haowl.hinovel.agent.application.service.agent;

import cn.haowl.hinovel.agent.domain.entity.ChatMessage;
import cn.haowl.hinovel.agent.domain.entity.ChatSession;
import cn.haowl.hinovel.agent.enums.AgentRole;
import cn.haowl.hinovel.agent.infrastructure.agent.AgentPromptTemplates;
import cn.haowl.hinovel.agent.infrastructure.agent.AiServiceFactory;
import cn.haowl.hinovel.agent.infrastructure.agent.AiServiceFactory.CallContext;
import cn.haowl.hinovel.agent.infrastructure.agent.GenericAiAssistant;
import cn.haowl.hinovel.agent.infrastructure.mapper.ChatMessageMapper;
import cn.haowl.hinovel.agent.infrastructure.mapper.ChatSessionMapper;
import cn.haowl.hinovel.agent.infrastructure.tool.NovelCharacterTool;
import cn.haowl.hinovel.agent.infrastructure.tool.NovelContentRetrievalTool;
import cn.haowl.hinovel.agent.infrastructure.tool.NovelCreationTool;
import cn.haowl.hinovel.agent.infrastructure.tool.SkillTool;
import cn.haowl.hinovel.agent.interfaces.dto.*;
import cn.haowl.hinovel.ai.application.vector.VectorStoreFactory;
import cn.haowl.hinovel.novel.application.service.ChapterOutlineService;
import cn.haowl.hinovel.novel.application.service.NovelChapterService;
import cn.haowl.hinovel.novel.application.service.NovelService;
import cn.haowl.hinovel.novel.application.service.NovelSettingsService;
import cn.haowl.hinovel.novel.domain.entity.NovelOutline;
import cn.haowl.hinovel.novel.domain.entity.NovelSettings;
import cn.haowl.hinovel.novel.domain.repository.NovelCharacterRepository;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import dev.langchain4j.service.TokenStream;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cn.haowl.hinovel.agent.enums.AgentErrorCodeConstants.CHAT_SESSION_NOT_EXISTS;
import static cn.haowl.hinovel.common.constant.CommonConstants.ENABLED;
import static cn.haowl.hinovel.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 问答 Agent 服务。
 *
 * <p>通过 {@link AiServiceFactory} 动态构建带向量检索工具的 AiService 实例，
 * 实现基于小说内容的智能问答。支持会话管理和历史消息上下文。</p>
 *
 * <p>核心流程：
 * 1. 接收用户问题，创建或复用会话
 * 2. 加载历史对话上下文
 * 3. 构建带向量检索 Tool 的问答 Agent
 * 4. 流式调用 LLM，逐 Token 推送回答
 * 5. 完成后保存用户消息和 AI 回复到数据库</p>
 *
 * @author haowl
 */
@Slf4j
@Service
public class QaAgent {

    /**
     * 问答写作判定的调用场景。
     */
    private static final String CALL_SCENE_QA_WRITING_INTENT = "QA_WRITING_INTENT";
    /**
     * 在日志中标识写作判定，不重复保存用户文本。
     */
    private static final String WRITING_INTENT_REQUEST = "判断当前问答是否要求写作或润色";
    /**
     * 写作判定中表示非写作意图的标签。
     */
    private static final String WRITING_INTENT_OTHER = "OTHER";
    /**
     * 问答中显式指定风格的常见表达。
     */
    private static final Pattern EXPLICIT_STYLE = Pattern.compile(
        "(?:使用|采用|模仿|用|以|按)\\s*[“「《]?([^，。！？；\\r\\n]{1,32}?)[”」》]?\\s*(?:的)?风格"
            + "(?=[\\s，,]*(?:来)?(?:写|续写|改写|重写|润色|生成|创作|重来|再来|描写|叙述|讲述|[。！!]?$))"
            + "|(?:使用|采用|用|以|按)\\s*[“「《]?(冷峻悬疑|温暖细腻)[”」》]?(?=写|续写|改写|润色)");
    /**
     * 构建带向量检索工具的问答 Agent。
     */
    @Autowired
    private AiServiceFactory aiServiceFactory;
    /**
     * 按用户配置获取小说向量库。
     */
    @Autowired
    private VectorStoreFactory vectorStoreFactory;
    /**
     * 问答会话持久化。
     */
    @Autowired
    private ChatSessionMapper chatSessionMapper;
    /**
     * 问答消息持久化。
     */
    @Autowired
    private ChatMessageMapper chatMessageMapper;
    /**
     * 小说有效配置。
     */
    @Autowired
    private NovelSettingsService novelSettingsService;

    /**
     * 调用场景常量：问答
     */
    private static final String CALL_SCENE_QA = "QA";
    /**
     * 章节与大纲查询。
     */
    @Autowired
    private NovelChapterService novelChapterService;
    /**
     * 章节大纲查询。
     */
    @Autowired
    private ChapterOutlineService chapterOutlineService;
    /**
     * 人物图谱查询。
     */
    @Autowired
    private NovelCharacterRepository novelCharacterRepository;
    /**
     * 小说归属校验。
     */
    @Autowired
    private NovelService novelService;

    /**
     * 问答 Agent 默认系统提示词（引用统一模板）
     */
    private static final String DEFAULT_SYSTEM_PROMPT = AgentPromptTemplates.QA_SYSTEM_PROMPT;

    // ==================== 会话管理 ====================

    /**
     * 获取小说的会话列表。
     *
     * @param novelId 小说 ID
     * @return 会话列表（按创建时间倒序）
     */
    public List<ChatSession> listSessions(Long novelId) {
        return chatSessionMapper.selectByNovelId(novelId);
    }

    /**
     * 创建新会话。
     *
     * @param novelId 小说 ID
     * @param title   会话标题
     * @return 新创建的会话
     */
    @Transactional(rollbackFor = Exception.class)
    public ChatSession createSession(Long novelId, String title) {
        ChatSession session = new ChatSession();
        session.setNovelId(novelId);
        session.setTitle(title != null && !title.isBlank() ? title : "新对话");
        chatSessionMapper.insert(session);
        log.info("创建问答会话，小说ID={}，会话ID={}", novelId, session.getId());
        return session;
    }

    /**
     * 删除会话及其所有消息。
     *
     * @param sessionId 会话 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteSession(Long sessionId) {
        chatSessionMapper.deleteById(sessionId);
        log.info("删除问答会话，会话ID={}", sessionId);
    }

    /**
     * 重命名会话。
     *
     * @param sessionId 会话 ID
     * @param title     新标题
     */
    @Transactional(rollbackFor = Exception.class)
    public void renameSession(Long sessionId, String title) {
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("会话不存在，ID=" + sessionId);
        }
        session.updateTitle(title);
        chatSessionMapper.updateById(session);
        log.info("重命名问答会话，会话ID={}，新标题={}", sessionId, title);
    }

    /**
     * 切换会话收藏状态。
     *
     * @param sessionId 会话 ID
     * @return 切换后的收藏状态（true=已收藏）
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleFavorite(Long sessionId) {
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("会话不存在，ID=" + sessionId);
        }
        session.toggleFavorite();
        chatSessionMapper.updateById(session);
        log.info("切换问答会话收藏状态，会话ID={}，收藏={}", sessionId, session.hasFavorited());
        return session.hasFavorited();
    }

    /**
     * 确认旧版客户端的中断请求。回复由流本身保存。
     *
     * @param sessionId 会话 ID
     * @param novelId   小说 ID
     * @param userId    当前用户 ID
     */
    public void confirmPartialResponse(Long sessionId, Long novelId, Long userId) {
        requireSession(sessionId, novelId, userId);
    }

    /**
     * 获取会话的消息列表。
     *
     * @param query 消息查询条件
     * @return 消息列表（按时间正序）
     */
    public List<ChatMessage> listMessages(ChatMessageQuery query) {
        requireSession(query.getSessionId(), null, query.getUserId());
        return chatMessageMapper.selectBySessionId(query.getSessionId());
    }

    /**
     * 分页获取会话的消息列表（游标分页，用于前端滚动加载）。
     *
     * @param query 游标分页查询条件
     * @return 分页结果（按时间正序）
     */
    public ChatMessagePageResponse listMessagesPage(ChatMessagePageQuery query) {
        requireSession(query.getSessionId(), null, query.getUserId());
        List<ChatMessage> messages = chatMessageMapper.selectPageByCursor(
            query.getSessionId(), query.getCursorId(), query.getPageSize());
        List<ChatMessageResponse> responseList = messages.stream()
            .map(ChatMessageResponse::fromEntity)
            .toList();
        // 返回条数达到每页条数时认为还有更早的消息
        boolean hasMore = messages.size() >= query.getPageSize();
        return new ChatMessagePageResponse(responseList, hasMore);
    }

    /**
     * 撤回消息。
     *
     * <p>将指定消息标记为已撤回，撤回后的消息在后续问答中不会发送给 LLM。</p>
     *
     * @param messageId 消息 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void revokeMessage(Long messageId) {
        ChatMessage message = chatMessageMapper.selectById(messageId);
        if (message == null) {
            throw new IllegalArgumentException("消息不存在，ID=" + messageId);
        }
        message.revoke();
        chatMessageMapper.updateById(message);
        log.info("撤回问答消息，消息ID={}，会话ID={}", messageId, message.getSessionId());
    }

    // ==================== 问答核心逻辑 ====================

    /**
     * 流式问答（SSE）。
     *
     * <p>接收用户问题，通过向量检索工具查询相关内容，
     * 结合历史对话上下文和全文大纲，流式生成回答。
     * 完成后自动保存用户消息和 AI 回复。</p>
     *
     * @param command 问答命令
     * @return 逐 Token 推送的 Flux 流
     */
    public Flux<ChatEvent> streamChat(QaChatCommand command) {
        return Flux.defer(() -> {
            Long novelId = command.getNovelId();
            Long sessionId = command.getSessionId();
            String question = command.getQuestion();
            Long userId = command.getUserId();
            requireSession(sessionId, novelId, userId);
            NovelSettings settings = novelSettingsService.getEffectiveSettings(novelId, userId);
            int contextLength = settings.getQaContextLength() == null ? 0 : settings.getQaContextLength();
            List<String> chatHistory = buildChatHistory(sessionId, contextLength);
            String outline = Integer.valueOf(ENABLED).equals(settings.getQaIncludeOutline())
                ? loadNovelOutlineContent(novelId) : null;
            String userPrompt = AgentPromptTemplates.buildQaPrompt(question, chatHistory, outline);
            CallContext callContext = aiServiceFactory.prepareNovelCall(novelId, AgentRole.QA, userId, DEFAULT_SYSTEM_PROMPT);
            SkillTool skillTool = callContext.getSkillTool();
            GenericAiAssistant assistant = aiServiceFactory.buildNovelAssistant(callContext, true,
                NovelContentRetrievalTool.create(vectorStoreFactory, novelId, userId),
                NovelCreationTool.create(novelId, novelChapterService, chapterOutlineService),
                NovelCharacterTool.create(novelId, novelCharacterRepository));

            saveMessage(ChatMessage.createUserMessage(sessionId, novelId, question), userId, null);
            String modelPrompt = needsDefaultSkill(callContext, question, chatHistory)
                ? aiServiceFactory.writingPrompt(callContext, userPrompt) : userPrompt;
            ChatEvent defaultLoad = skillLoadedEvent(skillTool);
            TokenStream tokenStream = assistant.streamChat(callContext.getSystemPrompt(), modelPrompt);
            StringBuffer response = new StringBuffer();
            AtomicBoolean saved = new AtomicBoolean();

            Flux<ChatEvent> events = aiServiceFactory.tokenStreamToFlux(
                    tokenStream, "novelId=" + novelId + ",sessionId=" + sessionId,
                    CALL_SCENE_QA, callContext, userPrompt,
                    token -> new ChatEvent(null, token),
                    execution -> {
                        String toolName = execution.request().name();
                        if (SkillTool.TOOL_NAME_LOAD.equals(toolName)) {
                            return skillLoadedEvent(skillTool);
                        }
                        return new ChatEvent("tool-executed", Map.of(
                            "toolName", toolName, "novelId", JsonNodeFactory.instance.numberNode(novelId)));
                    }, defaultLoad)
                .concatWith(Flux.defer(() -> styleNotice(question, skillTool, response.toString())))
                .doOnNext(event -> {
                    if (event.getName() == null) {
                        response.append(event.getData());
                    }
                });
            Runnable saveReply = () -> {
                String content = response.toString();
                if (!content.isBlank() && saved.compareAndSet(false, true)) {
                    saveMessage(ChatMessage.createAssistantMessage(sessionId, novelId, content), userId,
                        skillTool == null ? List.of() : skillTool.loadedNames());
                }
            };
            return events.doOnComplete(saveReply).doOnCancel(() -> {
                if (skillTool != null) {
                    skillTool.cancel();
                }
                saveReply.run();
            });
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 按当前请求及可用对话判断默认读取，分类不确定时先读取。
     */
    private boolean needsDefaultSkill(CallContext context, String question, List<String> chatHistory) {
        if (context.getSkillTool() == null || !context.getSkillTool().hasDefaultSkill()) {
            return false;
        }
        try {
            String result = aiServiceFactory.judgeWritingIntent(context,
                AgentPromptTemplates.buildQaPrompt(question, chatHistory, null),
                CALL_SCENE_QA_WRITING_INTENT, WRITING_INTENT_REQUEST);
            return result == null || !WRITING_INTENT_OTHER.equalsIgnoreCase(
                result.strip().replaceFirst("[。.!！]+$", ""));
        } catch (RuntimeException e) {
            return true;
        }
    }

    /**
     * 明确指定的风格未成功读取时补充聊天提示。
     */
    private Flux<ChatEvent> styleNotice(String question, SkillTool tool, String answer) {
        Matcher matcher = EXPLICIT_STYLE.matcher(question);
        if (!matcher.find()) {
            return Flux.empty();
        }
        String style = (matcher.group(1) == null ? matcher.group(2) : matcher.group(1)).strip();
        if ((tool != null && tool.loadedNames().contains(style))
            || (answer.contains("未加载") && answer.contains(style))) {
            return Flux.empty();
        }
        return Flux.just(new ChatEvent(null, "\n\n本次未加载「" + style + "」风格 Skill。"));
    }

    /**
     * 读取并构造尚未推送的成功事件。
     */
    private ChatEvent skillLoadedEvent(SkillTool skillTool) {
        String name = skillTool == null ? null : skillTool.pollLoadedName();
        return name == null ? null : new ChatEvent("skill-loaded", Map.of("skillName", name));
    }

    /**
     * 校验会话属于当前用户可访问的小说。
     */
    private void requireSession(Long sessionId, Long novelId, Long userId) {
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null || (novelId != null && !novelId.equals(session.getNovelId()))) {
            throw exception(CHAT_SESSION_NOT_EXISTS);
        }
        novelService.getNovelById(userId, session.getNovelId());
    }

    /**
     * 保存消息正文和该次回复的成功读取快照。
     */
    private void saveMessage(ChatMessage message, Long userId, List<String> loadedSkillNames) {
        message.setCreator(userId);
        message.setUpdater(userId);
        message.setLoadedSkillNames(loadedSkillNames);
        chatMessageMapper.insert(message);
    }

    /**
     * 构建历史对话上下文。
     *
     * @param sessionId     会话 ID
     * @param contextLength 携带的历史消息条数
     * @return 格式化的历史对话列表
     */
    private List<String> buildChatHistory(Long sessionId, int contextLength) {
        if (contextLength <= 0) {
            return List.of();
        }
        List<ChatMessage> recentMessages =
            chatMessageMapper.selectRecentMessages(sessionId, contextLength);
        List<String> history = new ArrayList<>();
        for (ChatMessage msg : recentMessages) {
            String roleLabel = ChatMessage.ROLE_USER.equals(msg.getRole()) ? "用户" : "AI";
            history.add(roleLabel + "：" + msg.getContent());
        }
        return history;
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

    /**
     * 聊天流中的文本或结构化事件。
     */
    @Value
    public static class ChatEvent {
        /**
         * SSE 事件名称，正文帧不设名称。
         */
        String name;

        /**
         * 正文或事件数据。
         */
        Object data;
    }
}
