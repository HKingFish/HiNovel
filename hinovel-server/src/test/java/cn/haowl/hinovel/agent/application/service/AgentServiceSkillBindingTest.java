package cn.haowl.hinovel.agent.application.service;

import cn.haowl.hinovel.agent.domain.entity.Agent;
import cn.haowl.hinovel.agent.domain.entity.AgentSkillBinding;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * AgentService 与 Skill 绑定的单元测试。
 *
 * 覆盖 skillIds 的三种语义：未传表示不修改、空数组表示清空、有效列表表示替换。
 */
@ExtendWith(MockitoExtension.class)
class AgentServiceSkillBindingTest {

    /**
     * 测试用户 ID。
     */
    private static final Long USER_ID = 1L;

    /**
     * 测试 Agent ID。
     */
    private static final Long AGENT_ID = 10L;

    /**
     * Agent 仓储替身。
     */
    @Mock
    private AgentRepository agentRepository;

    /**
     * 待验证的 Skill 服务。
     */
    @Mock
    private SkillService skillService;

    /**
     * Redis 客户端替身。
     */
    @Mock
    private StringRedisTemplate redisTemplate;

    /**
     * 待验证的 Agent 服务。
     */
    @InjectMocks
    private AgentService agentService;

    /**
     * 验证创作模板默认挂载去 AI 味 Skill。
     */
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

    /**
     * 验证空列表能覆盖模板默认挂载。
     */
    @Test
    void explicitEmptyListOverridesWritingTemplateDefault() {
        AgentRequest request = request(List.of());
        request.setRoleId(101L);
        when(agentRepository.save(any(Agent.class))).thenReturn(existingAgent());

        assertTrue(agentService.create(USER_ID, request).getSkillIds().isEmpty());
        verify(skillService).replaceAgentBindings(AGENT_ID, List.of());
    }

    /**
     * 验证无匹配模板时保持挂载为空。
     */
    @Test
    void createWithoutRecognizedTemplateLeavesBindingEmpty() {
        AgentRequest request = request(null);
        when(agentRepository.save(any(Agent.class))).thenReturn(existingAgent());

        assertTrue(agentService.create(USER_ID, request).getSkillIds().isEmpty());
        verify(skillService, never()).replaceAgentBindings(anyLong(), any());
    }

    /**
     * 验证用户无法修改他人的挂载。
     */
    @Test
    void viewerCannotChangeAnotherUsersBindings() {
        when(agentRepository.findByIdForUpdate(AGENT_ID)).thenReturn(Optional.of(existingAgent()));

        assertEquals(1003010003, assertThrows(BusinessException.class,
                () -> agentService.update(AGENT_ID, 2L, request(List.of(1L)))).getCode());
        verify(skillService, never()).replaceAgentBindings(anyLong(), any());
        verify(agentRepository, never()).save(any(Agent.class));
    }

    /**
     * 验证内置模板列表回填挂载。
     */
    @Test
    void listBuiltinFillsTemplateBindings() {
        Agent template = existingAgent();
        when(agentRepository.findBuiltinAgents()).thenReturn(List.of(template));
        when(skillService.listBindings(List.of(AGENT_ID))).thenReturn(List.of(binding(3L)));

        assertEquals(List.of(3), agentService.listBuiltin().getFirst().getSkillIds());
    }

    /**
     * 验证省略 Skill ID 时保留挂载。
     */
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

    /**
     * 验证空列表会清除已有挂载。
     */
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

    /**
     * 验证新列表会替换已有挂载。
     */
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

    /**
     * 验证可用列表按 Agent 回填挂载。
     */
    @Test
    void listAvailableFillsSkillIdsPerAgent() {
        Agent first = existingAgent();
        Agent second = existingAgent();
        second.setId(11L);
        when(agentRepository.findAvailableByUserId(USER_ID)).thenReturn(List.of(first, second));
        when(skillService.listBindings(List.of(AGENT_ID, 11L)))
            .thenReturn(List.of(binding(1L)));

        List<Agent> agents = agentService.listAvailable(USER_ID);

        assertEquals(List.of(1), agents.get(0).getSkillIds());
        assertTrue(agents.get(1).getSkillIds().isEmpty());
    }

    /**
     * 构建测试 Agent 的挂载关系。
     */
    private AgentSkillBinding binding(Long skillId) {
        AgentSkillBinding binding = new AgentSkillBinding();
        binding.setAgentId(AGENT_ID);
        binding.setSkillId(skillId);
        return binding;
    }

    /**
     * 构建已保存的测试 Agent。
     */
    private Agent existingAgent() {
        Agent agent = Agent.createUserAgent(USER_ID, "我的写作助手", "描述", "你是写作助手");
        agent.setId(AGENT_ID);
        return agent;
    }

    /**
     * 构建指定 Skill 挂载的请求。
     */
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
