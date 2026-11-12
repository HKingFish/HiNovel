package cn.haowl.hinovel.agent.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * @author : haowl
 * @date : 2026/11/12 18:39
 * @desc : 问答消息的游标分页结果
 */
@Data
@AllArgsConstructor
public class ChatMessagePageResponse {

    /** 消息列表（按时间正序）。 */
    private List<ChatMessageResponse> messages;

    /** 是否还有更早的消息。 */
    private boolean hasMore;
}
