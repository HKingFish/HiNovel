package cn.haowl.hinovel.agent.domain.service;

import cn.haowl.hinovel.agent.domain.entity.Agent;
import cn.haowl.hinovel.agent.domain.repository.AgentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static cn.haowl.hinovel.common.exception.enums.GlobalErrorCodeConstants.FORBIDDEN;
import static cn.haowl.hinovel.common.exception.enums.GlobalErrorCodeConstants.NOT_FOUND;
import static cn.haowl.hinovel.common.exception.util.ServiceExceptionUtil.exception;

/**
 * Agent 领域服务。
 *
 * <p>处理 Agent 相关的业务逻辑。</p>
 *
 * @Author : haowl
 * @Date : 2026/3/12 23:30
 */
@Service
@RequiredArgsConstructor
public class AgentDomainService {

    private final AgentRepository agentRepository;

    /**
     * 创建用户 Agent。
     *
     * @param userId       用户ID
     * @param name         名称
     * @param description  描述
     * @param systemPrompt 系统提示词
     * @return Agent 实体
     */
    public Agent createUserAgent(Long userId, String name, String description, String systemPrompt) {
        Agent agent = Agent.createUserAgent(userId, name, description, systemPrompt);
        return agentRepository.save(agent);
    }

    /**
     * 验证 Agent 归属。
     *
     * @param agentId Agent ID
     * @param userId  用户ID
     * @return Agent 实体
     */
    public Agent validateOwnership(Long agentId, Long userId) {
        Agent agent = agentRepository.findById(agentId)
            .orElseThrow(() -> exception(NOT_FOUND));
        if (!agent.belongsTo(userId)) {
            throw exception(FORBIDDEN);
        }
        return agent;
    }

    /**
     * 配置 Agent 模型。
     *
     * @param agentId       Agent ID
     * @param userId        用户ID
     * @param llmProviderId LLM 提供方 ID
     * @param modelName     模型名称
     */
    public void configureModel(Long agentId, Long userId, Long llmProviderId, String modelName) {
        Agent agent = validateOwnership(agentId, userId);
        agent.configureModel(llmProviderId, modelName);
        agentRepository.save(agent);
    }

    /**
     * 删除 Agent。
     *
     * @param agentId Agent ID
     * @param userId  用户ID
     */
    public void deleteAgent(Long agentId, Long userId) {
        validateOwnership(agentId, userId);
        agentRepository.deleteById(agentId);
    }
}
