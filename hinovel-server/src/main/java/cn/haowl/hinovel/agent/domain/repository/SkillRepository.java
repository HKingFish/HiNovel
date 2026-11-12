package cn.haowl.hinovel.agent.domain.repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author : haowl
 * @date : 2026/11/12 16:02
 * @desc : Agent 的 Skill 挂载仓储
 */
public interface SkillRepository {

    /** 查询 Agent 的 Skill ID。 */
    List<Long> findSkillIdsByAgentId(Long agentId);

    /** 批量查询 Agent 的 Skill ID。 */
    Map<Long, List<Long>> findSkillIdsByAgentIds(Collection<Long> agentIds);

    /** 整体替换挂载，空集合表示清空。 */
    void replaceBindings(Long agentId, Collection<Long> skillIds);
}
