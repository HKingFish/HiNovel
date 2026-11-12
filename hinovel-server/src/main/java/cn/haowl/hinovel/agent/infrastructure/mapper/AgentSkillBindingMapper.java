package cn.haowl.hinovel.agent.infrastructure.mapper;

import cn.haowl.hinovel.agent.domain.entity.AgentSkillBinding;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * @author : haowl
 * @date : 2026/11/12 16:02
 * @desc : Agent 与内置 Skill 的挂载 Mapper
 */
@Mapper
public interface AgentSkillBindingMapper extends BaseMapper<AgentSkillBinding> {

    /** 查询 Agent 的挂载，按 Skill ID 排序。 */
    default List<AgentSkillBinding> selectByAgentId(Long agentId) {
        return selectList(new LambdaQueryWrapper<AgentSkillBinding>()
                .eq(AgentSkillBinding::getAgentId, agentId)
                .orderByAsc(AgentSkillBinding::getSkillId));
    }

    /** 批量查询挂载，按 Agent ID 和 Skill ID 排序。 */
    default List<AgentSkillBinding> selectByAgentIds(Collection<Long> agentIds) {
        if (agentIds.isEmpty()) {
            return List.of();
        }
        return selectList(new LambdaQueryWrapper<AgentSkillBinding>()
                .in(AgentSkillBinding::getAgentId, agentIds)
                .orderByAsc(AgentSkillBinding::getAgentId, AgentSkillBinding::getSkillId));
    }

    /** 删除 Agent 的全部挂载。 */
    default void deleteByAgentId(Long agentId) {
        delete(new LambdaQueryWrapper<AgentSkillBinding>()
                .eq(AgentSkillBinding::getAgentId, agentId));
    }

    /** 插入一个复合主键挂载。 */
    @Insert("INSERT INTO agent_skill_binding (agent_id, skill_id) VALUES (#{agentId}, #{skillId})")
    void insertBinding(@Param("agentId") Long agentId, @Param("skillId") Long skillId);
}
