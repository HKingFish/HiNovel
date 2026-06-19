package cn.haowl.hinovel.novel.infrastructure.mapper;

import cn.haowl.hinovel.novel.domain.entity.NovelAgentConfig;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 小说 Agent 配置 Mapper。
 *
 * @author haowl
 */
@Mapper
public interface NovelAgentConfigMapper extends BaseMapper<NovelAgentConfig> {

    /**
     * 根据小说 ID 查询配置。
     *
     * @param novelId 小说 ID
     * @return Agent 配置，不存在时返回 null
     */
    default NovelAgentConfig selectByNovelId(Long novelId) {
        return selectOne(new LambdaQueryWrapper<NovelAgentConfig>()
                .eq(NovelAgentConfig::getNovelId, novelId));
    }
}
