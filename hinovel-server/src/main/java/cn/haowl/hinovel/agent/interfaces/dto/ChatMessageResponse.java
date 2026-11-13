package cn.haowl.hinovel.agent.interfaces.dto;

import cn.haowl.hinovel.agent.domain.entity.ChatMessage;
import lombok.Data;

import java.util.List;

/**
 * @author : haowl
 * @date : 2026/11/12 18:39
 * @desc : 问答消息的响应结果
 */
@Data
public class ChatMessageResponse {

    /**
     * 消息 ID。
     */
    private Long id;

    /**
     * 会话 ID。
     */
    private Long sessionId;

    /**
     * 消息角色。
     */
    private String role;

    /**
     * 消息正文。
     */
    private String content;

    /**
     * 对应回复首次成功加载的 Skill 名称。
     */
    private List<String> loadedSkillNames;

    /**
     * 撤回状态。
     */
    private Integer revoked;

    /**
     * 创建时间。
     */
    private String createTime;

    /**
     * 从实体转换为响应对象。
     *
     * @param message 消息实体
     * @return 消息响应
     */
    public static ChatMessageResponse fromEntity(ChatMessage message) {
        ChatMessageResponse response = new ChatMessageResponse();
        response.setId(message.getId());
        response.setSessionId(message.getSessionId());
        response.setRole(message.getRole());
        response.setContent(message.getContent());
        response.setLoadedSkillNames(message.getLoadedSkillNames());
        response.setRevoked(message.getRevoked());
        response.setCreateTime(message.getCreateTime() != null
                ? message.getCreateTime().toString() : null);
        return response;
    }
}
