package cn.haowl.hinovel.user.domain.repository;

import cn.haowl.hinovel.user.domain.entity.User;

import java.util.Optional;

/**
 * 用户仓储接口。
 *
 * <p>定义用户实体的存储和查询操作，遵循 DDD 仓储模式。</p>
 *
 * @Author : haowl
 * @Date : 2026/3/12 22:30
 */
public interface UserRepository {

    /**
     * 保存用户。
     *
     * @param user 用户实体
     * @return 保存后的用户
     */
    User save(User user);

    /**
     * 根据ID查询用户。
     *
     * @param id 用户ID
     * @return 用户实体
     */
    Optional<User> findById(Long id);

    /**
     * 根据用户名查询用户。
     *
     * @param username 用户名
     * @return 用户实体
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据邮箱查询用户。
     *
     * @param email 邮箱
     * @return 用户实体
     */
    Optional<User> findByEmail(String email);

    /**
     * 根据用户名或邮箱查询用户。
     *
     * @param account 用户名或邮箱
     * @return 用户实体
     */
    Optional<User> findByUsernameOrEmail(String account);

    /**
     * 判断用户名是否存在。
     *
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 判断邮箱是否存在。
     *
     * @param email 邮箱
     * @return 是否存在
     */
    boolean existsByEmail(String email);

    /**
     * 根据ID删除用户。
     *
     * @param id 用户ID
     */
    void deleteById(Long id);
}
