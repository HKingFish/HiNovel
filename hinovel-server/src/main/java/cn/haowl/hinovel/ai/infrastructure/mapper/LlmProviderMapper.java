package cn.haowl.hinovel.ai.infrastructure.mapper;

import cn.haowl.hinovel.ai.domain.entity.LlmProvider;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * LLM Provider 数据访问映射器接口。
 *
 * <p>继承自 MyBatis-Plus 的 {@link BaseMapper}，提供对 {@code ai_llm_provider} 表的
 * CRUD 操作能力。</p>
 *
 * @author haowl
 * @since 2024
 */
@Mapper
public interface LlmProviderMapper extends BaseMapper<LlmProvider> {
}
