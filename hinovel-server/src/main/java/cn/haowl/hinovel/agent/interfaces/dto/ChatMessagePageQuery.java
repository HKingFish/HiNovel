package cn.haowl.hinovel.agent.interfaces.dto;

import lombok.Data;

/**
 * @author : haowl
 * @date : 2026/11/12 18:39
 * @desc : 问答会话消息的游标分页查询条件
 */
@Data
public class ChatMessagePageQuery {

    /** 会话 ID。 */
    private Long sessionId;

    /** 游标消息 ID（null 表示从最新开始）。 */
    private Long cursorId;

    /** 每页条数。 */
    private int pageSize;

    /** 当前用户 ID。 */
    private Long userId;
}
