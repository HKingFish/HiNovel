package cn.haowl.hinovel.agent.infrastructure.mapper;

import cn.haowl.hinovel.agent.domain.entity.ChatMessage;
import cn.haowl.hinovel.common.constant.CommonConstants;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * AI 问答消息 Mapper。
 *
 * @author haowl
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    /**
     * 根据会话 ID 查询所有消息（含撤回标记，前端展示用，按创建时间正序）。
     *
     * @param sessionId 会话 ID
     * @return 消息列表
     */
    default List<ChatMessage> selectBySessionId(Long sessionId) {
        return selectList(new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getSessionId, sessionId)
                .orderByAsc(ChatMessage::getCreateTime));
    }

    /**
     * 根据会话 ID 分页查询消息（游标分页，按时间倒序取最近的，返回时正序排列）。
     *
     * <p>首次加载传 cursorId=null，加载更早消息时传当前最早消息的 ID。</p>
     *
     * @param sessionId 会话 ID
     * @param cursorId  游标消息 ID（加载该 ID 之前的消息，null 表示从最新开始）
     * @param pageSize  每页条数
     * @return 消息列表（按时间正序）
     */
    default List<ChatMessage> selectPageByCursor(Long sessionId, Long cursorId, int pageSize) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getSessionId, sessionId);
        if (cursorId != null) {
            wrapper.lt(ChatMessage::getId, cursorId);
        }
        wrapper.orderByDesc(ChatMessage::getId)
                .last(CommonConstants.limitOf(pageSize));
        List<ChatMessage> messages = selectList(wrapper);
        java.util.Collections.reverse(messages);
        return messages;
    }

    /**
     * 根据会话 ID 查询最近 N 条未撤回消息（用于构建 LLM 上下文）。
     *
     * <p>已撤回的消息不会发送给 LLM，确保撤回功能生效。</p>
     *
     * @param sessionId 会话 ID
     * @param limit     最大条数
     * @return 消息列表（按时间正序）
     */
    default List<ChatMessage> selectRecentMessages(Long sessionId, int limit) {
        // 先倒序取最近 N 条未撤回消息，再正序排列
        List<ChatMessage> messages = selectList(new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getSessionId, sessionId)
                .and(w -> w.eq(ChatMessage::getRevoked, CommonConstants.DISABLED)
                        .or().isNull(ChatMessage::getRevoked))
                .orderByDesc(ChatMessage::getCreateTime)
                .last(CommonConstants.limitOf(limit)));
        // 反转为正序
        java.util.Collections.reverse(messages);
        return messages;
    }
}
