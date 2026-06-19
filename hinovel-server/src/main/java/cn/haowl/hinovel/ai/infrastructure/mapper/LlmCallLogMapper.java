package cn.haowl.hinovel.ai.infrastructure.mapper;

import cn.haowl.hinovel.ai.domain.entity.LlmCallLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * LLM 调用记录数据访问映射器接口。
 *
 * <p>继承自 MyBatis-Plus 的 {@link BaseMapper}，提供对 {@code ai_llm_call_log} 表的
 * CRUD 操作能力。</p>
 *
 * @author haowl
 */
@Mapper
public interface LlmCallLogMapper extends BaseMapper<LlmCallLog> {
}
