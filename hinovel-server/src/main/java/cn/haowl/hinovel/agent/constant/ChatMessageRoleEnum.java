package cn.haowl.hinovel.agent.constant;

/**
 * @author : haowl
 * @date : 2026/11/17 21:08
 * @desc : 问答消息的角色及存储编码
 */
public enum ChatMessageRoleEnum {
    /**
     * 用户消息。
     */
    USER("user"),

    /**
     * 助手消息。
     */
    ASSISTANT("assistant");

    /**
     * 数据库与 API 使用的角色编码。
     */
    private final String code;

    /**
     * 声明角色编码。
     */
    ChatMessageRoleEnum(String code) {
        this.code = code;
    }

    /**
     * 返回角色编码。
     */
    public String getCode() {
        return code;
    }

    /**
     * 按历史编码解析角色，未知或空值返回 null。
     */
    public static ChatMessageRoleEnum fromCode(String code) {
        for (ChatMessageRoleEnum role : values()) {
            if (role.code.equals(code)) {
                return role;
            }
        }
        return null;
    }
}
