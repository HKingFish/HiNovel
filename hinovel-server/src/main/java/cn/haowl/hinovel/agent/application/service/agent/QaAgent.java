package cn.haowl.hinovel.agent.application.service.agent;

import cn.haowl.hinovel.agent.domain.entity.ChatMessage;
import cn.haowl.hinovel.agent.domain.entity.ChatSession;
import cn.haowl.hinovel.agent.infrastructure.agent.AgentPromptTemplates;
import cn.haowl.hinovel.agent.infrastructure.agent.AiServiceFactory;
import cn.haowl.hinovel.agent.infrastructure.agent.AiServiceFactory.AgentRole;
import cn.haowl.hinovel.agent.infrastructure.agent.AiServiceFactory.CallContext;
import cn.haowl.hinovel.agent.infrastructure.agent.GenericAiAssistant;
import cn.haowl.hinovel.agent.infrastructure.mapper.ChatMessageMapper;
import cn.haowl.hinovel.agent.infrastructure.mapper.ChatSessionMapper;
import cn.haowl.hinovel.agent.infrastructure.tool.NovelCharacterTool;
import cn.haowl.hinovel.agent.infrastructure.tool.NovelContentRetrievalTool;
import cn.haowl.hinovel.agent.infrastructure.tool.NovelCreationTool;
import cn.haowl.hinovel.ai.application.vector.VectorStoreFactory;
import cn.haowl.hinovel.novel.application.service.ChapterOutlineService;
import cn.haowl.hinovel.novel.application.service.NovelChapterService;
import cn.haowl.hinovel.novel.application.service.NovelSettingsService;
import cn.haowl.hinovel.novel.domain.entity.NovelOutline;
import cn.haowl.hinovel.novel.domain.entity.NovelSettings;
import cn.haowl.hinovel.novel.domain.repository.NovelCharacterRepository;
import dev.langchain4j.service.TokenStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

import static cn.haowl.hinovel.common.constant.CommonConstants.ENABLED;

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
@RequiredArgsConstructor
public class QaAgent {

    private final AiServiceFactory aiServiceFactory;
    private final VectorStoreFactory vectorStoreFactory;
    private final ChatSessionMapper chatSessionMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final NovelSettingsService novelSettingsService;
    private final NovelChapterService novelChapterService;
    private final ChapterOutlineService chapterOutlineService;
    private final NovelCharacterRepository novelCharacterRepository;

    /**
     * 调用场景常量：问答
     */
    private static final String CALL_SCENE_QA = "QA";

    /**
     * SSE 工具执行事件前缀标记。
     *
     * <p>当 Agent 执行工具后，向 SSE 流注入此前缀 + JSON 数据，
     * 前端据此识别工具执行事件并触发相应的 UI 更新（如刷新章节列表）。</p>
     */
    public static final String TOOL_EXECUTED_EVENT_PREFIX = "__TOOL_EXECUTED__:";

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
     * 手动保存 AI 回复消息（用于流式中断场景）。
     *
     * @param sessionId 会话 ID
     * @param novelId   小说 ID
     * @param content   已生成的回复内容
     */
    @Transactional(rollbackFor = Exception.class)
    public void savePartialResponse(Long sessionId, Long novelId, String content) {
        if (content == null || content.isBlank()) {
            return;
        }
        saveMessage(sessionId, novelId, ChatMessage.ROLE_ASSISTANT, content);
        log.info("保存中断的 AI 回复，会话ID={}，内容字数={}", sessionId, content.length());
    }

    /**
     * 获取会话的消息列表。
     *
     * @param sessionId 会话 ID
     * @return 消息列表（按时间正序）
     */
    public List<ChatMessage> listMessages(Long sessionId) {
        return chatMessageMapper.selectBySessionId(sessionId);
    }

    /**
     * 分页获取会话的消息列表（游标分页，用于前端滚动加载）。
     *
     * @param sessionId 会话 ID
     * @param cursorId  游标消息 ID（null 表示从最新开始）
     * @param pageSize  每页条数
     * @return 消息列表（按时间正序）
     */
    public List<ChatMessage> listMessagesByCursor(Long sessionId, Long cursorId, int pageSize) {
        return chatMessageMapper.selectPageByCursor(sessionId, cursorId, pageSize);
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
     * @param novelId   小说 ID
     * @param sessionId 会话 ID
     * @param question  用户问题
     * @param userId    用户 ID（用于获取配置）
     * @return 逐 Token 推送的 Flux 流
     */
    public Flux<String> streamChat(Long novelId, Long sessionId,
                                   String question, Long userId) {
        log.info("问答 Agent 流式回答，小说ID={}，会话ID={}，问题={}",
                novelId, sessionId, question);

        // 保存用户消息
        saveMessage(sessionId, novelId, ChatMessage.ROLE_USER, question);

        // 获取配置
        NovelSettings settings = novelSettingsService.getEffectiveSettings(novelId, userId);

        // 加载历史对话上下文
        int contextLength = settings.getQaContextLength() != null
                ? settings.getQaContextLength() : 0;
        List<String> chatHistory = buildChatHistory(sessionId, contextLength);

        // 根据配置决定是否携带全文大纲
        String novelOutlineContent = null;
        if (ENABLED == settings.getQaIncludeOutline()) {
            novelOutlineContent = loadNovelOutlineContent(novelId);
        }

        // 构建用户 Prompt
        String userPrompt = AgentPromptTemplates.buildQaPrompt(
                question, chatHistory, novelOutlineContent);

        // 解析调用上下文
        CallContext callContext = aiServiceFactory.resolveCallContext(novelId, AgentRole.QA, userId);

        // 构建带向量检索、创作和人物图谱工具的流式 AiService
        NovelContentRetrievalTool retrievalTool =
                NovelContentRetrievalTool.create(vectorStoreFactory, novelId, userId);
        NovelCreationTool creationTool =
                NovelCreationTool.create(novelId, novelChapterService, chapterOutlineService);
        NovelCharacterTool characterTool =
                NovelCharacterTool.create(novelId, novelCharacterRepository);
        GenericAiAssistant assistant = aiServiceFactory.buildStreamingByNovelWithTools(
                novelId, AgentRole.QA, GenericAiAssistant.class,
                retrievalTool, creationTool, characterTool);

        // 获取系统提示词
        String systemPrompt = aiServiceFactory.resolveSystemPrompt(
                novelId, AgentRole.QA, DEFAULT_SYSTEM_PROMPT);

        // 流式调用
        TokenStream tokenStream = assistant.streamChat(systemPrompt, userPrompt);

        // 收集完整回复内容，完成后保存到数据库
        StringBuilder responseBuffer = new StringBuilder();

        return aiServiceFactory.tokenStreamToFlux(
                        tokenStream,
                        "novelId=" + novelId + ",sessionId=" + sessionId,
                        CALL_SCENE_QA, callContext, userPrompt,
                        toolExecution -> {
                            // 构建工具执行事件标记，注入 SSE 流通知前端刷新
                            String toolName = toolExecution.request().name();
                            return TOOL_EXECUTED_EVENT_PREFIX
                                    + "{\"toolName\":\"" + toolName + "\""
                                    + ",\"novelId\":" + novelId + "}";
                        })
                .doOnNext(responseBuffer::append)
                .doOnComplete(() -> {
                    String fullResponse = responseBuffer.toString();
                    if (!fullResponse.isBlank()) {
                        saveMessage(sessionId, novelId,
                                ChatMessage.ROLE_ASSISTANT, fullResponse);
                        log.info("问答 Agent 回复已保存，会话ID={}，回复字数={}",
                                sessionId, fullResponse.length());
                    }
                })
                .doOnCancel(() -> {
                    // 流被取消时（用户中断或页面刷新），保存已生成的部分内容
                    String partialResponse = responseBuffer.toString();
                    if (!partialResponse.isBlank()) {
                        saveMessage(sessionId, novelId,
                                ChatMessage.ROLE_ASSISTANT, partialResponse);
                        log.info("问答 Agent 流被取消，已保存部分回复，会话ID={}，回复字数={}",
                                sessionId, partialResponse.length());
                    }
                });
    }

    // ==================== 内部方法 ====================

    /**
     * 保存消息到数据库。
     *
     * @param sessionId 会话 ID
     * @param novelId   小说 ID
     * @param role      消息角色
     * @param content   消息内容
     */
    private void saveMessage(Long sessionId, Long novelId, String role, String content) {
        ChatMessage message = new ChatMessage();
        message.setSessionId(sessionId);
        message.setNovelId(novelId);
        message.setRole(role);
        message.setContent(content);
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
}
