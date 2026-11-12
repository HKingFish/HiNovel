package cn.haowl.hinovel.agent.domain.entity;

import lombok.Data;

/**
 * @author : haowl
 * @date : 2026/11/12 16:02
 * @desc : 内置 Skill 的目录元数据
 */
@Data
public class AgentSkill {

    /** 随资源发布的稳定 ID。 */
    private Long id;

    /** 配置与执行时显示的名称。 */
    private String name;

    /** 帮助模型判断何时读取正文。 */
    private String description;
}
