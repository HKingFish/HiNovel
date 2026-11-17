package cn.haowl.hinovel.agent.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author : wylon
 * @date : 2026/11/17 17:43
 * @desc :
 */
@Getter
@Slf4j
@AllArgsConstructor
public enum ChatEventEnum {
    TEXT("text", "正文"),
    SKILL_LOADED("skill-loaded", "技能加载"),
    TOOL_EXECUTED("tool-executed", "工具执行");

    /**
     * 事件代码。
     */
    private final String code;

    /**
     * 事件描述。
     */
    private final String description;

    /**
     * 根据事件代码获取事件枚举。
     *
     * @param code 事件代码。
     * @return 事件枚举。
     */
    public ChatEventEnum getByCode(String code) {
        for (ChatEventEnum event : values()) {
            if (event.getCode().equals(code)) {
                return event;
            }
        }
        return null;
    }

}
