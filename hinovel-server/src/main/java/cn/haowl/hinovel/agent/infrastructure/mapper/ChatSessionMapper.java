package cn.haowl.hinovel.agent.infrastructure.mapper;

import cn.haowl.hinovel.agent.domain.entity.ChatSession;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * AI 问答会话 Mapper。
 *
 * @author haowl
 */
@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {

    /**
     * 根据小说 ID 查询会话列表（按创建时间倒序）。
     *
     * @param novelId 小说 ID
     * @return 会话列表
     */
    default List<ChatSession> selectByNovelId(Long novelId) {
        return selectList(new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getNovelId, novelId)
                .orderByDesc(ChatSession::getCreateTime));
    }
}
