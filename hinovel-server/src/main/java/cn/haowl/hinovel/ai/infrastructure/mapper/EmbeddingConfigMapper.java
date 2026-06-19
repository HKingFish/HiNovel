package cn.haowl.hinovel.ai.infrastructure.mapper;

import cn.haowl.hinovel.ai.domain.entity.EmbeddingConfig;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 嵌入式模型配置 Mapper。
 *
 * @author wylon
 * @date 2026/3/23 14:56
 */
@Mapper
public interface EmbeddingConfigMapper extends BaseMapper<EmbeddingConfig> {
}
