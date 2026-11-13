package cn.haowl.hinovel.agent.interfaces.dto;

import lombok.Data;

/**
 * @author : haowl
 * @date : 2026/11/12 18:39
 * @desc : 问答会话消息查询条件
 */
@Data
public class ChatMessageQuery {

    /**
     * 会话 ID。
     */
    private Long sessionId;

    /**
     * 当前用户 ID。
     */
    private Long userId;
}
