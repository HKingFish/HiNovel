package cn.haowl.hinovel.agent.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Agent 角色枚举，用于从 NovelAgentConfig 中选取对应的 Agent ID。
 */
@Getter
@AllArgsConstructor
public enum AgentRole {
    /**
     * 作者 Agent
     */
    AUTHOR("作者 Agent"),
    /**
     * 编辑 Agent
     */
    EDITOR("编辑 Agent"),
    /**
     * 问答 Agent
     */
    QA("问答 Agent");

    private final String name;
}
