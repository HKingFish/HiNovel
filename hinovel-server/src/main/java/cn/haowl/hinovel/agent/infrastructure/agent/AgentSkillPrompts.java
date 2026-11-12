package cn.haowl.hinovel.agent.infrastructure.agent;

import cn.haowl.hinovel.agent.domain.entity.AgentSkill;

import java.util.List;

import static cn.haowl.hinovel.agent.constant.SkillConstants.DE_AI_SKILL_ID;

/**
 * @author : haowl
 * @date : 2026/11/12 16:02
 * @desc : 拼装 Agent 的 Skill 指令与目录元数据
 */
public final class AgentSkillPrompts {

    private AgentSkillPrompts() {
    }

    /** 初始 System Prompt 仅公开挂载名称和描述。 */
    public static String buildSkillSection(List<AgentSkill> skills) {
        StringBuilder section = new StringBuilder("\n\n【Skill 使用规则】\n");
        section.append("若用户明确指定写作风格，按名称查找匹配的已挂载 Skill。")
                .append("无匹配或读取失败时继续生成，并简洁说明本次未加载用户指定的风格 Skill。");
        if (!skills.isEmpty()) {
            section.append("\n已挂载的 Skill：\n");
            for (AgentSkill skill : skills) {
                section.append("- ").append(skill.getName()).append("：").append(skill.getDescription()).append('\n');
            }
            section.append("需要作者风格时先用 loadSkill 读取匹配的 Skill 正文，再生成内容。")
                    .append("已随当前执行提供的 Skill 无需再次读取。");
            if (skills.stream().anyMatch(skill -> DE_AI_SKILL_ID.equals(skill.getId()))) {
                section.append("直接写作或润色时，如本次执行尚未提供去 AI 味的读取结果，先用 loadSkill 读取。");
            }
        }
        return section.toString();
    }
}
