package cn.haowl.hinovel.agent.application.service;

import cn.haowl.hinovel.agent.constant.AgentConstants;
import cn.haowl.hinovel.agent.domain.entity.Agent;
import cn.haowl.hinovel.agent.domain.repository.AgentRepository;
import cn.haowl.hinovel.agent.interfaces.dto.AgentRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static cn.haowl.hinovel.agent.enums.AgentErrorCodeConstants.AGENT_NOT_OWNED;
import static cn.haowl.hinovel.agent.constant.SkillConstants.DE_AI_SKILL_ID;
import static cn.haowl.hinovel.agent.constant.SkillConstants.WRITING_TEMPLATE_ID;
import static cn.haowl.hinovel.common.exception.enums.GlobalErrorCodeConstants.NOT_FOUND;
import static cn.haowl.hinovel.common.exception.util.ServiceExceptionUtil.exception;

/**
 * Agent 管理应用服务。
 *
 * <p>协调领域层完成 Agent 的创建、更新、删除、查询及缓存管理，
 * 通过仓储接口访问数据，使用领域实体的工厂方法和富方法操作业务逻辑。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentService {

    private final AgentRepository agentRepository;
    private final SkillService skillService;
    private final StringRedisTemplate redisTemplate;

    /**
     * 创建 Agent。
     *
     * @param userId  用户 ID
     * @param request 创建请求
     * @return 创建的 Agent
     */
    @Transactional(rollbackFor = Exception.class)
    public Agent create(Long userId, AgentRequest request) {
        // 通过工厂方法创建用户自定义 Agent
        Agent agent = Agent.createUserAgent(
                userId, request.getName(),
                request.getDescription(), request.getSystemPrompt()
        );

        // 通过富方法配置模型参数
        agent.configureModel(request.getLlmProviderId(), request.getModelName());
        agent.setCustomBaseUrl(request.getCustomBaseUrl());
        agent.configureTemperature(request.getTemperature());
        agent.configureMaxTokens(request.getMaxTokens());
        agent.setTopP(request.getTopP());
        agent.setRoleId(request.getRoleId());
        if (request.getMaxIterations() != null) {
            agent.setMaxIterations(request.getMaxIterations());
        }

        agent = agentRepository.save(agent);
        List<Long> skillIds = request.getSkillIds();
        if (skillIds == null && WRITING_TEMPLATE_ID.equals(request.getRoleId())) {
            skillIds = List.of(DE_AI_SKILL_ID);
        }
        applySkillBindings(agent, skillIds);
        log.info("Agent 创建成功: id={}, userId={}", agent.getId(), userId);
        return agent;
    }

    /**
     * 获取用户的 Agent 列表（用户自建 + 内置，内置 Agent 对所有用户可见）。
     *
     * @param userId 用户 ID
     * @return Agent 列表
     */
    public List<Agent> listByUser(Long userId) {
        return agentRepository.findAvailableByUserId(userId);
    }

    /**
     * 获取所有内置 Agent 列表。
     *
     * @return 内置 Agent 列表
     */
    public List<Agent> listBuiltin() {
        List<Agent> agents = agentRepository.findBuiltinAgents();
        fillSkillIds(agents);
        return agents;
    }

    /**
     * 获取当前用户可用的所有 Agent（自建 + 内置）。
     *
     * @param userId 用户 ID
     * @return Agent 列表
     */
    public List<Agent> listAvailable(Long userId) {
        List<Agent> agents = agentRepository.findAvailableByUserId(userId);
        fillSkillIds(agents);
        return agents;
    }

    /**
     * 获取 Agent 详情。
     *
     * @param agentId Agent ID
     * @param userId  用户 ID
     * @return Agent 实体
     */
    public Agent getById(Long agentId, Long userId) {
        Agent agent = agentRepository.findById(agentId)
            .orElseThrow(() -> exception(NOT_FOUND));

        // 权限校验：非内置 Agent 只能被创建者访问
        if (agent.isUserDefined() && !agent.belongsTo(userId)) {
            throw exception(AGENT_NOT_OWNED);
        }
        agent.setSkillIds(skillService.listBoundSkillIds(agent.getId()).stream().map(Long::intValue).toList());
        return agent;
    }

    /**
     * 更新 Agent（内置 Agent 允许修改，但不校验 userId）。
     *
     * @param agentId Agent ID
     * @param userId  用户 ID
     * @param request 更新请求
     * @return 更新后的 Agent
     */
    @Transactional(rollbackFor = Exception.class)
    public Agent update(Long agentId, Long userId, AgentRequest request) {
        Agent agent = agentRepository.findByIdForUpdate(agentId)
            .orElseThrow(() -> exception(NOT_FOUND));

        // 内置 Agent 所有用户均可修改；用户自建 Agent 仅限创建者修改
        if (agent.isUserDefined() && !agent.belongsTo(userId)) {
            throw exception(AGENT_NOT_OWNED);
        }

        // 通过富方法更新属性
        agent.updateName(request.getName());
        agent.updateDescription(request.getDescription());
        agent.updateSystemPrompt(request.getSystemPrompt());
        agent.configureModel(request.getLlmProviderId(), request.getModelName());
        agent.setCustomBaseUrl(request.getCustomBaseUrl());
        agent.configureTemperature(request.getTemperature());
        agent.configureMaxTokens(request.getMaxTokens());
        agent.setTopP(request.getTopP());
        agent.setRoleId(request.getRoleId());
        if (request.getMaxIterations() != null) {
            agent.setMaxIterations(request.getMaxIterations());
        }

        agent = agentRepository.save(agent);

        // 按传入的 skillIds 替换绑定（null 表示不修改）
        applySkillBindings(agent, request.getSkillIds());

        // 主动失效 Redis 缓存
        evictCache(agentId);
        log.info("Agent 更新成功: id={}", agentId);
        return agent;
    }

    /**
     * 删除 Agent（内置 Agent 允许删除）。
     *
     * @param agentId Agent ID
     * @param userId  用户 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long agentId, Long userId) {
        Agent agent = agentRepository.findByIdForUpdate(agentId)
            .orElseThrow(() -> exception(NOT_FOUND));

        // 内置 Agent 所有用户均可删除；用户自建 Agent 仅限创建者删除
        if (agent.isUserDefined() && !agent.belongsTo(userId)) {
            throw exception(AGENT_NOT_OWNED);
        }

        agentRepository.deleteById(agentId);
        skillService.replaceAgentBindings(agentId, Collections.emptyList());
        evictCache(agentId);
        log.info("Agent 删除成功: id={}", agentId);
    }

    /**
     * 获取 Agent（带 Redis 缓存）。
     *
     * @param agentId Agent ID
     * @return Agent 实体
     */
    public Agent getWithCache(Long agentId) {
        String cacheKey = AgentConstants.REDIS_KEY_PREFIX_AGENT_CONFIG + agentId;
        Agent agent = agentRepository.findById(agentId)
            .orElseThrow(() -> exception(NOT_FOUND));

        redisTemplate.opsForValue().set(
                cacheKey, String.valueOf(agentId),
                AgentConstants.AGENT_CACHE_TTL_SECONDS, TimeUnit.SECONDS
        );
        return agent;
    }

    /**
     * 主动失效 Agent 缓存。
     *
     * @param agentId Agent ID
     */
    public void evictCache(Long agentId) {
        redisTemplate.delete(AgentConstants.REDIS_KEY_PREFIX_AGENT_CONFIG + agentId);
    }

    /**
     * 获取内置小说创作 Agent。
     *
     * @return 内置小说 Agent 实体
     */
    public Agent getBuiltinNovelAgent() {
        return agentRepository.findBuiltinByName(AgentConstants.BUILTIN_AGENT_NOVEL_ASSISTANT)
            .orElseThrow(() -> exception(NOT_FOUND, "内置小说 Agent 未初始化，请联系管理员"));
    }

    /**
     * 获取内置面试助手 Agent。
     *
     * @return 内置面试助手 Agent 实体
     */
    public Agent getBuiltinInterviewAgent() {
        return agentRepository.findBuiltinByName(AgentConstants.BUILTIN_AGENT_INTERVIEW_ASSISTANT)
            .orElseThrow(() -> exception(NOT_FOUND, "内置面试助手 Agent 未初始化，请联系管理员"));
    }

    /**
     * 按请求中的 skillIds 替换 Agent 绑定，并把最终绑定回填到返回体。
     *
     * @param agent    Agent 实体
     * @param skillIds 请求中的 Skill ID 列表，null 表示不修改
     */
    private void applySkillBindings(Agent agent, List<Long> skillIds) {
        if (skillIds != null) {
            skillService.replaceAgentBindings(agent.getId(), skillIds);
        }
        agent.setSkillIds(skillService.listBoundSkillIds(agent.getId()).stream().map(Long::intValue).toList());
    }

    /**
     * 批量填充 Agent 列表的 skillIds。
     *
     * @param agents Agent 列表
     */
    private void fillSkillIds(List<Agent> agents) {
        if (agents == null || agents.isEmpty()) {
            return;
        }
        List<Long> agentIds = agents.stream().map(Agent::getId).toList();
        Map<Long, List<Long>> bindings = skillService.listBoundSkillIds(agentIds);
        for (Agent agent : agents) {
            agent.setSkillIds(bindings.getOrDefault(agent.getId(), List.of()).stream().map(Long::intValue).toList());
        }
    }
}
