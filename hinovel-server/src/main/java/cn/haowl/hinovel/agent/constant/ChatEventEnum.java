package cn.haowl.hinovel.agent.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author : wylon
 * @date : 2026/11/17 17:43
 * @desc : 问答流式响应的事件类型
 */
@Getter
@AllArgsConstructor
public enum ChatEventEnum {
    /**
     * 文本片段。
     */
    TEXT("text", "正文"),
    /**
     * 已读取的 Skill。
     */
    SKILL_LOADED("skill-loaded", "技能加载"),
    /**
     * 工具执行结果。
     */
    TOOL_EXECUTED("tool-executed", "工具执行"),
    /**
     * 流式响应异常。
     */
    ERROR("error", "流式响应异常");

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
