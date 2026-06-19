package cn.haowl.hinovel.novel.domain.repository;

import cn.haowl.hinovel.novel.domain.entity.Novel;

import java.util.List;
import java.util.Optional;

/**
 * 小说仓储接口。
 *
 * <p>定义小说实体的存储和查询操作。</p>
 *
 * @Author : haowl
 * @Date : 2026/3/12 23:30
 */
public interface NovelRepository {

    /**
     * 保存小说。
     *
     * @param novel 小说实体
     * @return 保存后的小说
     */
    Novel save(Novel novel);

    /**
     * 根据ID查询小说。
     *
     * @param id 小说ID
     * @return 小说实体
     */
    Optional<Novel> findById(Long id);

    /**
     * 根据用户ID查询小说列表。
     *
     * @param userId 用户ID
     * @return 小说列表
     */
    List<Novel> findByUserId(Long userId);

    /**
     * 根据用户ID和状态查询小说列表。
     *
     * @param userId 用户ID
     * @param status 状态
     * @return 小说列表
     */
    List<Novel> findByUserIdAndStatus(Long userId, String status);

    /**
     * 删除小说。
     *
     * @param id 小说ID
     */
    void deleteById(Long id);

    /**
     * 判断小说是否属于用户。
     *
     * @param novelId 小说ID
     * @param userId  用户ID
     * @return 是否属于用户
     */
    boolean existsByIdAndUserId(Long novelId, Long userId);
}
