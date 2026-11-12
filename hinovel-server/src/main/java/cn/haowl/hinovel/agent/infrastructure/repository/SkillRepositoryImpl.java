package cn.haowl.hinovel.agent.infrastructure.repository;

import cn.haowl.hinovel.agent.domain.entity.AgentSkillBinding;
import cn.haowl.hinovel.agent.domain.repository.SkillRepository;
import cn.haowl.hinovel.agent.infrastructure.mapper.AgentSkillBindingMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author : haowl
 * @date : 2026/11/12 16:02
 * @desc : Agent 的 Skill 挂载持久化
 */
@Repository
public class SkillRepositoryImpl implements SkillRepository {

    /** 挂载表访问。 */
    @Autowired
    private AgentSkillBindingMapper agentSkillBindingMapper;

    @Override
    public List<Long> findSkillIdsByAgentId(Long agentId) {
        return agentSkillBindingMapper.selectByAgentId(agentId).stream().map(AgentSkillBinding::getSkillId).toList();
    }

    @Override
    public Map<Long, List<Long>> findSkillIdsByAgentIds(Collection<Long> agentIds) {
        Map<Long, List<Long>> result = new HashMap<>();
        for (AgentSkillBinding binding : agentSkillBindingMapper.selectByAgentIds(agentIds)) {
            result.computeIfAbsent(binding.getAgentId(), key -> new ArrayList<>()).add(binding.getSkillId());
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replaceBindings(Long agentId, Collection<Long> skillIds) {
        agentSkillBindingMapper.deleteByAgentId(agentId);
        for (Long skillId : skillIds) {
            agentSkillBindingMapper.insertBinding(agentId, skillId);
        }
    }
}
