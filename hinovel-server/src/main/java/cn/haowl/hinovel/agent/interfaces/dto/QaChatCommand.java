package cn.haowl.hinovel.agent.interfaces.dto;

import lombok.Data;

/**
 * @author : haowl
 * @date : 2026/11/12 18:39
 * @desc : 问答流式对话的命令参数
 */
@Data
public class QaChatCommand {

    /**
     * 小说 ID。
     */
    private Long novelId;

    /**
     * 会话 ID。
     */
    private Long sessionId;

    /**
     * 用户问题。
     */
    private String question;

    /**
     * 当前用户 ID。
     */
    private Long userId;
}
