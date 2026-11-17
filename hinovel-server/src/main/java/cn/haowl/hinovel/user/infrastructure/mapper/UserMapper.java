package cn.haowl.hinovel.user.infrastructure.mapper;

import cn.haowl.hinovel.user.domain.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper。
 *
 * 提供用户数据的 CRUD 操作，继承 MyBatis-Plus BaseMapper。
 *
 * @author haowl
 * @since 2024
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
