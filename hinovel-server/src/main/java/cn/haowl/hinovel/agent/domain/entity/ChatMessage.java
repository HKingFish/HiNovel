package cn.haowl.hinovel.agent.domain.entity;

import cn.haowl.hinovel.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI 问答消息实体。
 *
 * <p>记录会话中每条消息的角色和内容，支持多轮对话上下文。</p>
 *
 * @author haowl
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("agent_chat_message")
public class ChatMessage extends BaseEntity {

    /**
     * 消息角色：用户。
     */
    public static final String ROLE_USER = "user";

    /**
     * 消息角色：AI 助手。
     */
    public static final String ROLE_ASSISTANT = "assistant";

    /**
     * 会话 ID。
     */
    private Long sessionId;

    /**
     * 小说 ID。
     */
    private Long novelId;

    /**
     * 消息角色（user / assistant）。
     */
    private String role;

    /**
     * 消息内容。
     */
    private String content;

    /**
     * 是否已撤回。
     *
     * @see cn.haowl.hinovel.common.constant.CommonConstants#ENABLED
     * @see cn.haowl.hinovel.common.constant.CommonConstants#DISABLED
     */
    private Integer revoked;

    // ==================== 工厂方法 ====================

    /**
     * 创建用户消息。
     *
     * @param sessionId 会话 ID
     * @param novelId   小说 ID
     * @param content   消息内容
     * @return 用户消息实体
     */
    public static ChatMessage createUserMessage(Long sessionId, Long novelId, String content) {
        ChatMessage message = new ChatMessage();
        message.setSessionId(sessionId);
        message.setNovelId(novelId);
        message.setRole(ROLE_USER);
        message.setContent(content);
        message.setRevoked(cn.haowl.hinovel.common.constant.CommonConstants.DISABLED);
        return message;
    }

    /**
     * 创建 AI 助手消息。
     *
     * @param sessionId 会话 ID
     * @param novelId   小说 ID
     * @param content   消息内容
     * @return AI 助手消息实体
     */
    public static ChatMessage createAssistantMessage(Long sessionId, Long novelId, String content) {
        ChatMessage message = new ChatMessage();
        message.setSessionId(sessionId);
        message.setNovelId(novelId);
        message.setRole(ROLE_ASSISTANT);
        message.setContent(content);
        message.setRevoked(cn.haowl.hinovel.common.constant.CommonConstants.DISABLED);
        return message;
    }

    // ==================== 状态判断 ====================

    /**
     * 判断是否为用户消息。
     *
     * @return 用户消息返回 true
     */
    public boolean isUserMessage() {
        return ROLE_USER.equals(this.role);
    }

    /**
     * 判断是否为 AI 助手消息。
     *
     * @return AI 助手消息返回 true
     */
    public boolean isAssistantMessage() {
        return ROLE_ASSISTANT.equals(this.role);
    }

    /**
     * 判断消息是否归属于指定会话。
     *
     * @param sessionId 会话 ID
     * @return 归属返回 true
     */
    public boolean belongsToSession(Long sessionId) {
        return this.sessionId != null && this.sessionId.equals(sessionId);
    }

    /**
     * 撤回消息。
     */
    public void revoke() {
        this.revoked = cn.haowl.hinovel.common.constant.CommonConstants.ENABLED;
    }

    /**
     * 判断消息是否已撤回。
     *
     * @return 已撤回返回 true
     */
    public boolean hasRevoked() {
        return cn.haowl.hinovel.common.constant.CommonConstants.ENABLED == (
                this.revoked != null ? this.revoked
                        : cn.haowl.hinovel.common.constant.CommonConstants.DISABLED);
    }
}
