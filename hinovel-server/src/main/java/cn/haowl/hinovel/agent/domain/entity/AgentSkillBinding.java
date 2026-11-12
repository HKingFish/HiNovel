package cn.haowl.hinovel.agent.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author : haowl
 * @date : 2026/11/12 16:02
 * @desc : Agent 与内置 Skill 的挂载关系
 */
@Data
@TableName("agent_skill_binding")
public class AgentSkillBinding {

    /** Agent ID，复合主键第一列。 */
    private Long agentId;

    /** 内置 Skill ID，复合主键第二列。 */
    private Long skillId;
}
