package cn.haowl.hinovel.agent.interfaces.dto;

import cn.haowl.hinovel.agent.domain.entity.ChatSession;
import lombok.Data;

/**
 * @author : haowl
 * @date : 2026/11/12 18:39
 * @desc : 问答会话的响应结果
 */
@Data
public class ChatSessionResponse {

    /**
     * 会话 ID。
     */
    private Long id;

    /**
     * 小说 ID。
     */
    private Long novelId;

    /**
     * 会话标题。
     */
    private String title;

    /**
     * 收藏状态。
     */
    private Integer favorite;

    /**
     * 创建时间。
     */
    private String createTime;

    /**
     * 更新时间。
     */
    private String updateTime;

    /**
     * 从实体转换为响应对象。
     *
     * @param session 会话实体
     * @return 会话响应
     */
    public static ChatSessionResponse fromEntity(ChatSession session) {
        ChatSessionResponse response = new ChatSessionResponse();
        response.setId(session.getId());
        response.setNovelId(session.getNovelId());
        response.setTitle(session.getTitle());
        response.setFavorite(session.getFavorite());
        response.setCreateTime(session.getCreateTime() != null
                ? session.getCreateTime().toString() : null);
        response.setUpdateTime(session.getUpdateTime() != null
                ? session.getUpdateTime().toString() : null);
        return response;
    }
}
