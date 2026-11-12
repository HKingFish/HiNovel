package cn.haowl.hinovel.agent.infrastructure.agent;

import cn.haowl.hinovel.agent.application.service.SkillService;
import cn.haowl.hinovel.agent.application.service.agent.QaAgent;
import cn.haowl.hinovel.agent.domain.entity.Agent;
import cn.haowl.hinovel.agent.domain.entity.AgentSkill;
import cn.haowl.hinovel.agent.domain.entity.ChatMessage;
import cn.haowl.hinovel.agent.domain.entity.ChatSession;
import cn.haowl.hinovel.agent.enums.AgentRole;
import cn.haowl.hinovel.agent.infrastructure.mapper.AgentMapper;
import cn.haowl.hinovel.agent.infrastructure.mapper.ChatMessageMapper;
import cn.haowl.hinovel.agent.infrastructure.mapper.ChatSessionMapper;
import cn.haowl.hinovel.agent.interfaces.controller.AIController.ChatMessageResponse;
import cn.haowl.hinovel.ai.application.llm.LlmProviderFactory;
import cn.haowl.hinovel.ai.application.llm.LlmProviderPort;
import cn.haowl.hinovel.ai.application.log.LlmCallLogService;
import cn.haowl.hinovel.ai.application.vector.VectorStoreFactory;
import cn.haowl.hinovel.ai.domain.entity.LlmCallLog;
import cn.haowl.hinovel.ai.domain.entity.SkillReadCall;
import cn.haowl.hinovel.common.exception.BusinessException;
import cn.haowl.hinovel.novel.application.service.NovelService;
import cn.haowl.hinovel.novel.application.service.NovelSettingsService;
import cn.haowl.hinovel.novel.domain.entity.NovelAgentConfig;
import cn.haowl.hinovel.novel.domain.entity.NovelSettings;
import cn.haowl.hinovel.novel.infrastructure.mapper.NovelAgentConfigMapper;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author : haowl
 * @date : 2026/11/12 16:00
 * @desc : 使用可控模型验证 Skill 读取与聊天记录
 */
class AgentSkillExecutionTest {

    private static final long NOVEL_ID = 10L;
    private static final long SESSION_ID = 20L;
    private static final long USER_ID = 30L;
    private static final long AGENT_ID = 40L;
    private static final long PROVIDER_ID = 50L;
    private static final String STYLE = "冷峻悬疑";
    private static final String DEFAULT_SKILL = "去 AI 味";
    private static final String STYLE_BODY = "留白示例正文";
    private static final String DEFAULT_BODY = "去 AI 味正文";

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void chatKeepsOnlySuccessesInHistoryAndEveryReadInTheLog(boolean styleSucceeds) {
        SkillService skills = mock(SkillService.class);
        when(skills.listBoundSkills(AGENT_ID)).thenReturn(List.of(skill(1L, STYLE), skill(3L, DEFAULT_SKILL)));
        when(skills.readContent(3L)).thenReturn(DEFAULT_BODY);
        if (styleSucceeds) {
            when(skills.readContent(1L)).thenReturn(STYLE_BODY);
        } else {
            when(skills.readContent(1L)).thenThrow(new IllegalStateException("资源不可读"));
        }

        String text = "夜里，她推开了门。";
        LlmProviderPort provider = mock(LlmProviderPort.class);
        when(provider.getChatModel()).thenReturn(new ChatModel() {
            @Override
            public ChatResponse doChat(ChatRequest request) {
                if (!styleSucceeds) {
                    throw new IllegalStateException("写作用途暂不可判定");
                }
                return response(AiMessage.from("WRITING。"));
            }
        });
        when(provider.getStreamingChatModel()).thenReturn(new StreamingChatModel() {
            @Override
            public void doChat(ChatRequest request, StreamingChatResponseHandler handler) {
                if (request.messages().getLast() instanceof ToolExecutionResultMessage result) {
                    assertTrue(result.text().contains(styleSucceeds ? STYLE_BODY : "本次未加载"));
                    handler.onPartialResponse(text);
                    handler.onCompleteResponse(response(AiMessage.from(text)));
                    return;
                }
                SystemMessage system = assertInstanceOf(SystemMessage.class, request.messages().getFirst());
                assertTrue(system.text().contains(STYLE));
                assertFalse(system.text().contains(STYLE_BODY));
                assertFalse(system.text().contains(DEFAULT_BODY));
                UserMessage user = assertInstanceOf(UserMessage.class, request.messages().getLast());
                assertTrue(user.singleText().contains(DEFAULT_BODY));
                handler.onCompleteResponse(response(AiMessage.from(readStyle())));
            }
        });
        LlmCallLogService logs = mock(LlmCallLogService.class);
        ChatSessionMapper sessions = mock(ChatSessionMapper.class);
        when(sessions.selectById(SESSION_ID)).thenReturn(ChatSession.create(NOVEL_ID, "会话"));
        ChatMessageMapper messages = mock(ChatMessageMapper.class);
        NovelSettingsService settings = mock(NovelSettingsService.class);
        when(settings.getEffectiveSettings(NOVEL_ID, USER_ID)).thenReturn(new NovelSettings());
        QaAgent qa = new QaAgent(factory(skills, provider, logs), mock(VectorStoreFactory.class),
                sessions, messages, settings, null, null, null, mock(NovelService.class));

        List<QaAgent.ChatEvent> events = qa.streamChat(NOVEL_ID, SESSION_ID, "用冷峻悬疑风格写一段夜间重逢", USER_ID)
                .collectList().block(Duration.ofSeconds(5));

        List<String> expectedNames = styleSucceeds ? List.of(DEFAULT_SKILL, STYLE) : List.of(DEFAULT_SKILL);
        assertEquals(expectedNames.stream().map(name -> Map.of("skillName", name)).toList(),
                events.stream().filter(event -> "skill-loaded".equals(event.getName())).map(QaAgent.ChatEvent::getData).toList());
        String expectedText = styleSucceeds ? text : text + "\n\n本次未加载「冷峻悬疑」风格 Skill。";
        assertEquals(expectedText, events.stream().filter(event -> event.getName() == null)
                .map(event -> event.getData().toString()).collect(java.util.stream.Collectors.joining()));
        ArgumentCaptor<ChatMessage> saved = ArgumentCaptor.forClass(ChatMessage.class);
        verify(messages, times(2)).insert(saved.capture());
        assertEquals(expectedText, saved.getAllValues().getLast().getContent());
        assertEquals(expectedNames, ChatMessageResponse.fromEntity(saved.getAllValues().getLast()).getLoadedSkillNames());
        assertEquals(List.of(), ChatMessageResponse.fromEntity(saved.getAllValues().getFirst()).getLoadedSkillNames());
        ArgumentCaptor<LlmCallLog> logged = ArgumentCaptor.forClass(LlmCallLog.class);
        verify(logs, times(2)).saveAsync(logged.capture());
        assertEquals("QA_WRITING_INTENT", logged.getAllValues().getFirst().getCallScene());
        List<SkillReadCall> calls = logged.getAllValues().getLast().getSkillReadCalls();
        assertEquals(List.of(DEFAULT_SKILL, STYLE), calls.stream().map(SkillReadCall::getSkillName).toList());
        assertEquals(List.of(SkillReadCall.Status.SUCCESS,
                        styleSucceeds ? SkillReadCall.Status.SUCCESS : SkillReadCall.Status.FAILED),
                calls.stream().map(SkillReadCall::getStatus).toList());
    }

    @Test
    void unknownSessionRejectsChatBeforeReadingAnySkillsOrWritingMessages() {
        AiServiceFactory factory = mock(AiServiceFactory.class);
        ChatMessageMapper messages = mock(ChatMessageMapper.class);
        QaAgent qa = new QaAgent(factory, null, mock(ChatSessionMapper.class), messages,
                null, null, null, null, null);

        assertEquals(1003003002, assertThrows(BusinessException.class, () ->
                qa.streamChat(NOVEL_ID, SESSION_ID, "提问", USER_ID).blockLast(Duration.ofSeconds(5))).getCode());
        verifyNoInteractions(factory, messages);
    }

    @Test
    void editorCanUseTheSameReadToolInASynchronousExecution() {
        SkillService skills = mock(SkillService.class);
        when(skills.listBoundSkills(AGENT_ID)).thenReturn(List.of(skill(1L, STYLE)));
        when(skills.readContent(1L)).thenReturn(STYLE_BODY);
        LlmProviderPort provider = mock(LlmProviderPort.class);
        when(provider.getChatModel()).thenReturn(new ChatModel() {
            @Override
            public ChatResponse doChat(ChatRequest request) {
                if (request.messages().getLast() instanceof ToolExecutionResultMessage result) {
                    assertEquals(STYLE_BODY, result.text());
                    return response(AiMessage.from("审核结果"));
                }
                return response(AiMessage.from(readStyle()));
            }
        });
        AiServiceFactory factory = factory(skills, provider, mock(LlmCallLogService.class));
        var call = factory.prepareNovelCall(NOVEL_ID, AgentRole.EDITOR, USER_ID, "审核");

        assertEquals("审核结果", factory.buildNovelAssistant(call, false).chat(call.getSystemPrompt(), "审核文本"));
        assertEquals(List.of(STYLE), call.getSkillTool().loadedNames());
    }

    @Test
    void cancellationAfterDefaultLoadStillRecordsTheRead() {
        SkillService skills = mock(SkillService.class);
        when(skills.listBoundSkills(AGENT_ID)).thenReturn(List.of(skill(3L, DEFAULT_SKILL)));
        when(skills.readContent(3L)).thenReturn(DEFAULT_BODY);
        LlmProviderPort provider = mock(LlmProviderPort.class);
        StreamingChatModel streaming = mock(StreamingChatModel.class);
        when(provider.getStreamingChatModel()).thenReturn(streaming);
        when(provider.getChatModel()).thenReturn(new ChatModel() {
            @Override
            public ChatResponse doChat(ChatRequest request) {
                return response(AiMessage.from("WRITING"));
            }
        });
        LlmCallLogService logs = mock(LlmCallLogService.class);
        QaAgent qa = qa(factory(skills, provider, logs));

        assertEquals(Map.of("skillName", DEFAULT_SKILL),
                qa.streamChat(NOVEL_ID, SESSION_ID, "写一段重逢", USER_ID)
                        .take(1).blockLast(Duration.ofSeconds(5)).getData());

        ArgumentCaptor<LlmCallLog> calls = ArgumentCaptor.forClass(LlmCallLog.class);
        verify(logs, timeout(1000).times(2)).saveAsync(calls.capture());
        assertEquals(List.of(DEFAULT_SKILL), calls.getAllValues().getLast().getSkillReadCalls()
                .stream().map(SkillReadCall::getSkillName).toList());
        verifyNoInteractions(streaming);
    }

    @Test
    void unmountedStyleGetsANoticeEvenWhenTheModelOnlyGeneratesProse() {
        SkillService skills = mock(SkillService.class);
        when(skills.listBoundSkills(AGENT_ID)).thenReturn(List.of());
        LlmProviderPort provider = mock(LlmProviderPort.class);
        when(provider.getStreamingChatModel()).thenReturn(new StreamingChatModel() {
            @Override
            public void doChat(ChatRequest request, StreamingChatResponseHandler handler) {
                String text = "她推开了门。";
                handler.onPartialResponse(text);
                handler.onCompleteResponse(response(AiMessage.from(text)));
            }
        });
        QaAgent qa = qa(factory(skills, provider, mock(LlmCallLogService.class)));

        List<QaAgent.ChatEvent> events = qa.streamChat(NOVEL_ID, SESSION_ID, "以温暖细腻风格写一段重逢", USER_ID)
                .collectList().block(Duration.ofSeconds(5));

        assertEquals(List.of("她推开了门。", "\n\n本次未加载「温暖细腻」风格 Skill。"),
                events.stream().map(QaAgent.ChatEvent::getData).toList());
    }

    @Test
    void historicalForeignAgentAssignmentCannotLoadPrivateBindings() {
        SkillService skills = mock(SkillService.class);
        LlmProviderPort provider = mock(LlmProviderPort.class);
        AiServiceFactory factory = factory(skills, provider, mock(LlmCallLogService.class));

        assertEquals(1003010003, assertThrows(BusinessException.class, () ->
                factory.prepareNovelCall(NOVEL_ID, AgentRole.QA, USER_ID + 1, "问答")).getCode());
        verifyNoInteractions(skills, provider);
    }

    private QaAgent qa(AiServiceFactory factory) {
        ChatSessionMapper sessions = mock(ChatSessionMapper.class);
        when(sessions.selectById(SESSION_ID)).thenReturn(ChatSession.create(NOVEL_ID, "会话"));
        NovelSettingsService settings = mock(NovelSettingsService.class);
        when(settings.getEffectiveSettings(NOVEL_ID, USER_ID)).thenReturn(new NovelSettings());
        return new QaAgent(factory, mock(VectorStoreFactory.class), sessions, mock(ChatMessageMapper.class),
                settings, null, null, null, mock(NovelService.class));
    }

    private AiServiceFactory factory(SkillService skills, LlmProviderPort provider, LlmCallLogService logs) {
        LlmProviderFactory providers = mock(LlmProviderFactory.class);
        when(providers.getByProviderId(PROVIDER_ID)).thenReturn(provider);
        NovelAgentConfigMapper configs = mock(NovelAgentConfigMapper.class);
        when(configs.selectByNovelId(NOVEL_ID)).thenReturn(NovelAgentConfig.builder()
                .qaAgentId(AGENT_ID).editorAgentId(AGENT_ID).build());
        AgentMapper agents = mock(AgentMapper.class);
        Agent agent = Agent.builder().name("测试 Agent").userId(USER_ID).llmProviderId(PROVIDER_ID).build();
        agent.setId(AGENT_ID);
        when(agents.selectById(AGENT_ID)).thenReturn(agent);
        return new AiServiceFactory(providers, configs, agents, logs, skills);
    }

    private AgentSkill skill(Long id, String name) {
        AgentSkill skill = new AgentSkill();
        skill.setId(id);
        skill.setName(name);
        skill.setDescription("用途");
        return skill;
    }

    private ToolExecutionRequest readStyle() {
        return ToolExecutionRequest.builder().id("style-1").name("loadSkill")
                .arguments("{\"name\":\"冷峻悬疑\"}").build();
    }

    private ChatResponse response(AiMessage message) {
        return ChatResponse.builder().aiMessage(message).build();
    }
}
