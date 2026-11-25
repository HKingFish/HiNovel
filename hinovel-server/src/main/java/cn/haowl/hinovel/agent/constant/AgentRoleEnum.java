package cn.haowl.hinovel.agent.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author : haowl
 * @date : 2026/11/17 21:03
 * @desc : 小说 Agent 角色
 */
@Getter
@AllArgsConstructor
public enum AgentRoleEnum {
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

    /**
     * 角色显示名称。
     */
    private final String name;
}
