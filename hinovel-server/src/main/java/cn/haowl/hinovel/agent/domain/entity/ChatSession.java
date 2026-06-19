package cn.haowl.hinovel.agent.domain.entity;

import cn.haowl.hinovel.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static cn.haowl.hinovel.common.constant.CommonConstants.DISABLED;
import static cn.haowl.hinovel.common.constant.CommonConstants.ENABLED;

/**
 * AI 问答会话实体。
 *
 * <p>每个会话归属于一本小说，记录用户与 AI 的多轮对话。</p>
 *
 * @author haowl
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("agent_chat_session")
public class ChatSession extends BaseEntity {

    /**
     * 默认会话标题。
     */
    private static final String DEFAULT_TITLE = "新对话";

    /**
     * 小说 ID。
     */
    private Long novelId;

    /**
     * 会话标题。
     */
    private String title;

    /**
     * 是否收藏。
     *
     * @see cn.haowl.hinovel.common.constant.CommonConstants#ENABLED
     * @see cn.haowl.hinovel.common.constant.CommonConstants#DISABLED
     */
    private Integer favorite;

    // ==================== 工厂方法 ====================

    /**
     * 创建新会话。
     *
     * @param novelId 小说 ID
     * @param title   会话标题（为空时使用默认标题）
     * @return 会话实体
     */
    public static ChatSession create(Long novelId, String title) {
        ChatSession session = new ChatSession();
        session.setNovelId(novelId);
        session.setTitle(title != null && !title.isBlank() ? title : DEFAULT_TITLE);
        session.setFavorite(DISABLED);
        return session;
    }

    // ==================== 业务方法 ====================

    /**
     * 更新会话标题。
     *
     * @param title 新标题
     */
    public void updateTitle(String title) {
        if (title != null && !title.isBlank()) {
            this.title = title;
        }
    }

    /**
     * 切换收藏状态。
     */
    public void toggleFavorite() {
        this.favorite = hasFavorited() ? DISABLED : ENABLED;
    }

    // ==================== 状态判断 ====================

    /**
     * 判断会话是否归属于指定小说。
     *
     * @param novelId 小说 ID
     * @return 归属返回 true
     */
    public boolean belongsToNovel(Long novelId) {
        return this.novelId != null && this.novelId.equals(novelId);
    }

    /**
     * 判断会话是否已收藏。
     *
     * @return 已收藏返回 true
     */
    public boolean hasFavorited() {
        return ENABLED == (this.favorite != null ? this.favorite : DISABLED);
    }
}
