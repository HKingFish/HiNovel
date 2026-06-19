package cn.haowl.hinovel.agent.constant;

/**
 * Agent 类型枚举。
 *
 * <p>定义 Agent 的类型。</p>
 *
 * @author haowl
 * @since 2024
 */
public enum AgentType {

    /**
     * 作者类型，负责创作内容。
     */
    AUTHOR("author", "作者"),

    /**
     * 编辑类型，负责审核内容。
     */
    EDITOR("editor", "编辑");

    private final String code;
    private final String displayName;

    AgentType(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }
}
