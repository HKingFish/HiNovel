package cn.haowl.hinovel.ai.infrastructure.mapper;

import cn.haowl.hinovel.ai.domain.entity.LlmProvider;
import cn.haowl.hinovel.common.constant.CommonConstants;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * LLM Provider 数据访问映射器接口。
 *
 * 继承自 MyBatis-Plus 的 {@link BaseMapper}，提供对 {@code ai_llm_provider} 表的
 * CRUD 操作能力。
 *
 * @author haowl
 * @since 2024
 */
@Mapper
public interface LlmProviderMapper extends BaseMapper<LlmProvider> {

    /**
     * 查询可作为故障切换候选的提供方。
     */
    default List<LlmProvider> selectActiveProviders() {
        return selectList(new LambdaQueryWrapper<LlmProvider>()
            .eq(LlmProvider::getIsActive, CommonConstants.ENABLED));
    }
}
