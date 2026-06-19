package cn.haowl.hinovel.agent.infrastructure.repository;

import cn.haowl.hinovel.common.constant.CommonConstants;
import cn.haowl.hinovel.agent.domain.entity.Agent;
import cn.haowl.hinovel.agent.domain.repository.AgentRepository;
import cn.haowl.hinovel.agent.infrastructure.mapper.AgentMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Agent 仓储实现。
 *
 * <p>基于 MyBatis-Plus 实现 Agent 仓储接口。</p>
 *
 * @author haowl
 */
@Repository
@RequiredArgsConstructor
public class AgentRepositoryImpl implements AgentRepository {

    private final AgentMapper agentMapper;

    @Override
    public Agent save(Agent agent) {
        if (agent.getId() == null) {
            agentMapper.insert(agent);
        } else {
            // 兼容旧数据 version 为 null 的情况
            if (agent.getVersion() == null) {
                agent.setVersion(0);
            }
            agentMapper.updateById(agent);
        }
        return agent;
    }

    @Override
    public Optional<Agent> findById(Long id) {
        return Optional.ofNullable(agentMapper.selectById(id));
    }

    @Override
    public List<Agent> findByUserId(Long userId) {
        LambdaQueryWrapper<Agent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Agent::getUserId, userId)
                .orderByDesc(Agent::getCreateTime);
        return agentMapper.selectList(wrapper);
    }

    @Override
    public List<Agent> findBuiltinAgents() {
        LambdaQueryWrapper<Agent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Agent::getIsBuiltin, CommonConstants.ENABLED)
                .orderByAsc(Agent::getSortOrder)
                .orderByAsc(Agent::getCreateTime);
        return agentMapper.selectList(wrapper);
    }

    @Override
    public void deleteById(Long id) {
        agentMapper.deleteById(id);
    }

    @Override
    public boolean existsByIdAndUserId(Long agentId, Long userId) {
        LambdaQueryWrapper<Agent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Agent::getId, agentId)
                .eq(Agent::getUserId, userId);
        return agentMapper.exists(wrapper);
    }

    @Override
    public List<Agent> findAvailableByUserId(Long userId) {
        LambdaQueryWrapper<Agent> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w ->
                        w.eq(Agent::getUserId, userId).or()
                                .eq(Agent::getIsBuiltin, CommonConstants.ENABLED))
                .orderByDesc(Agent::getIsBuiltin)
                .orderByDesc(Agent::getCreateTime);
        return agentMapper.selectList(wrapper);
    }

    @Override
    public Optional<Agent> findBuiltinByName(String name) {
        LambdaQueryWrapper<Agent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Agent::getIsBuiltin, CommonConstants.ENABLED)
                .eq(Agent::getName, name)
                .last(CommonConstants.LIMIT_ONE);
        return Optional.ofNullable(agentMapper.selectOne(wrapper));
    }
}
