package cn.haowl.hinovel.agent.infrastructure.agent;

import cn.haowl.hinovel.agent.domain.entity.AgentSkill;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 初始 System Prompt 的 Skill 信息。
 */
class AgentSkillPromptsTest {

    @Test
    void exposesMountedMetadataAndReadInstructions() {
        AgentSkill skill = new AgentSkill();
        skill.setId(1L);
        skill.setName("冷峻悬疑");
        skill.setDescription("短句、动作与留白");

        String section = AgentSkillPrompts.buildSkillSection(List.of(skill));

        assertTrue(section.contains("冷峻悬疑：短句、动作与留白"));
        assertTrue(section.contains("loadSkill"));
        assertFalse(section.contains("未解决的事实"));
    }

    @Test
    void requestsAFallbackEvenWithoutAnyMountedSkills() {
        String section = AgentSkillPrompts.buildSkillSection(List.of());

        assertTrue(section.contains("继续生成"));
        assertTrue(section.contains("未加载"));
        assertFalse(section.contains("loadSkill"));
    }
}
