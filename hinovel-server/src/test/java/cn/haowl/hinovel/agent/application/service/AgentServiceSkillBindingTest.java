package cn.haowl.hinovel.agent.application.service;

import cn.haowl.hinovel.agent.domain.entity.Agent;
import cn.haowl.hinovel.agent.domain.repository.AgentRepository;
import cn.haowl.hinovel.agent.interfaces.dto.AgentRequest;
import cn.haowl.hinovel.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AgentService 与 Skill 绑定的单元测试。
 *
 * <p>覆盖 skillIds 的三种语义：未传表示不修改、空数组表示清空、有效列表表示替换。</p>
 */
@ExtendWith(MockitoExtension.class)
class AgentServiceSkillBindingTest {

    private static final Long USER_ID = 1L;

    private static final Long AGENT_ID = 10L;

    @Mock
    private AgentRepository agentRepository;

    @Mock
    private SkillService skillService;

    @Mock
    private StringRedisTemplate redisTemplate;

    @InjectMocks
    private AgentService agentService;

    @Test
    void createFromWritingTemplateDefaultsToDeAiSkill() {
        AgentRequest request = request(null);
        request.setRoleId(101L);
        when(agentRepository.save(any(Agent.class))).thenAnswer(invocation -> {
            Agent saved = invocation.getArgument(0);
            saved.setId(AGENT_ID);
            return saved;
        });
        when(skillService.listBoundSkillIds(AGENT_ID)).thenReturn(List.of(3L));

        Agent result = agentService.create(USER_ID, request);

        verify(skillService).replaceAgentBindings(AGENT_ID, List.of(3L));
        assertEquals(List.of(3), result.getSkillIds());
    }

    @Test
    void explicitEmptyListOverridesWritingTemplateDefault() {
        AgentRequest request = request(List.of());
        request.setRoleId(101L);
        when(agentRepository.save(any(Agent.class))).thenReturn(existingAgent());

        assertTrue(agentService.create(USER_ID, request).getSkillIds().isEmpty());
        verify(skillService).replaceAgentBindings(AGENT_ID, List.of());
    }

    @Test
    void createWithoutRecognizedTemplateLeavesBindingEmpty() {
        AgentRequest request = request(null);
        when(agentRepository.save(any(Agent.class))).thenReturn(existingAgent());

        assertTrue(agentService.create(USER_ID, request).getSkillIds().isEmpty());
        verify(skillService, never()).replaceAgentBindings(anyLong(), any());
    }

    @Test
    void viewerCannotChangeAnotherUsersBindings() {
        when(agentRepository.findByIdForUpdate(AGENT_ID)).thenReturn(Optional.of(existingAgent()));

        assertEquals(1003010003, assertThrows(BusinessException.class,
                () -> agentService.update(AGENT_ID, 2L, request(List.of(1L)))).getCode());
        verify(skillService, never()).replaceAgentBindings(anyLong(), any());
        verify(agentRepository, never()).save(any(Agent.class));
    }

    @Test
    void listBuiltinFillsTemplateBindings() {
        Agent template = existingAgent();
        when(agentRepository.findBuiltinAgents()).thenReturn(List.of(template));
        when(skillService.listBoundSkillIds(List.of(AGENT_ID))).thenReturn(Map.of(AGENT_ID, List.of(3L)));

        assertEquals(List.of(3), agentService.listBuiltin().getFirst().getSkillIds());
    }

    @Test
    void updateWithoutSkillIdsKeepsBindings() {
        Agent agent = existingAgent();
        when(agentRepository.findByIdForUpdate(AGENT_ID)).thenReturn(Optional.of(agent));
        when(agentRepository.save(any(Agent.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(skillService.listBoundSkillIds(AGENT_ID)).thenReturn(List.of(1L));

        Agent result = agentService.update(AGENT_ID, USER_ID, request(null));

        verify(skillService, never()).replaceAgentBindings(anyLong(), any());
        assertEquals(List.of(1), result.getSkillIds());
    }

    @Test
    void updateWithEmptySkillIdsClearsBindings() {
        Agent agent = existingAgent();
        when(agentRepository.findByIdForUpdate(AGENT_ID)).thenReturn(Optional.of(agent));
        when(agentRepository.save(any(Agent.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(skillService.listBoundSkillIds(AGENT_ID)).thenReturn(List.of());

        Agent result = agentService.update(AGENT_ID, USER_ID, request(List.of()));

        verify(skillService).replaceAgentBindings(AGENT_ID, List.of());
        assertTrue(result.getSkillIds().isEmpty());
    }

    @Test
    void updateWithSkillIdsReplacesBindings() {
        Agent agent = existingAgent();
        when(agentRepository.findByIdForUpdate(AGENT_ID)).thenReturn(Optional.of(agent));
        when(agentRepository.save(any(Agent.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(skillService.listBoundSkillIds(AGENT_ID)).thenReturn(List.of(1L, 2L));

        Agent result = agentService.update(AGENT_ID, USER_ID, request(List.of(1L, 2L)));

        verify(skillService).replaceAgentBindings(AGENT_ID, List.of(1L, 2L));
        assertEquals(List.of(1, 2), result.getSkillIds());
    }

    @Test
    void listAvailableFillsSkillIdsPerAgent() {
        Agent first = existingAgent();
        Agent second = existingAgent();
        second.setId(11L);
        when(agentRepository.findAvailableByUserId(USER_ID)).thenReturn(List.of(first, second));
        when(skillService.listBoundSkillIds(List.of(AGENT_ID, 11L)))
                .thenReturn(Map.of(AGENT_ID, List.of(1L)));

        List<Agent> agents = agentService.listAvailable(USER_ID);

        assertEquals(List.of(1), agents.get(0).getSkillIds());
        assertTrue(agents.get(1).getSkillIds().isEmpty());
    }

    private Agent existingAgent() {
        Agent agent = Agent.createUserAgent(USER_ID, "我的写作助手", "描述", "你是写作助手");
        agent.setId(AGENT_ID);
        return agent;
    }

    private AgentRequest request(List<Long> skillIds) {
        AgentRequest request = new AgentRequest();
        request.setName("我的写作助手");
        request.setDescription("描述");
        request.setSystemPrompt("你是写作助手");
        request.setLlmProviderId(202L);
        request.setModelName("deepseek-chat");
        request.setSkillIds(skillIds);
        return request;
    }
}
