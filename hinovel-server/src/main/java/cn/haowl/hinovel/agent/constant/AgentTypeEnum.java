package cn.haowl.hinovel.agent.constant;

/**
 * @author : haowl
 * @date : 2026/11/17 21:03
 * @desc : Agent 类型
 */
public enum AgentTypeEnum {

    /**
     * 作者类型，负责创作内容。
     */
    AUTHOR("author", "作者"),

    /**
     * 编辑类型，负责审核内容。
     */
    EDITOR("editor", "编辑");

    /**
     * 类型编码。
     */
    private final String code;
    /**
     * 类型显示名称。
     */
    private final String displayName;

    /**
     * 声明类型编码及其显示名称。
     */
    AgentTypeEnum(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    /**
     * 返回类型编码。
     */
    public String getCode() {
        return code;
    }

    /**
     * 返回类型显示名称。
     */
    public String getDisplayName() {
        return displayName;
    }
}
