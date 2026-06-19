package cn.haowl.hinovel.novel.infrastructure.mapper;

import cn.haowl.hinovel.novel.domain.entity.NovelSettings;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 小说配置 Mapper。
 *
 * <p>基于 MyBatis-Plus 实现小说配置的数据访问操作。</p>
 *
 * @author haowl
 */
@Mapper
public interface NovelSettingsMapper extends BaseMapper<NovelSettings> {

    /**
     * 根据小说 ID 查询小说级别配置。
     *
     * @param novelId 小说 ID
     * @return 小说配置，不存在时返回 null
     */
    default NovelSettings selectByNovelId(Long novelId) {
        return selectOne(new LambdaQueryWrapper<NovelSettings>()
                .eq(NovelSettings::getNovelId, novelId));
    }

    /**
     * 根据用户 ID 查询用户级别默认配置（novel_id = 0 表示用户默认配置）。
     *
     * @param userId 用户 ID
     * @return 用户默认配置，不存在时返回 null
     */
    default NovelSettings selectUserDefault(Long userId) {
        return selectOne(new LambdaQueryWrapper<NovelSettings>()
                .eq(NovelSettings::getNovelId, 0L)
                .eq(NovelSettings::getUserId, userId));
    }
}
