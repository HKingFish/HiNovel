package cn.haowl.hinovel.ai.domain.repository;

import cn.haowl.hinovel.ai.domain.entity.LlmProvider;

import java.util.List;
import java.util.Optional;

/**
 * LLM 提供方仓储接口。
 *
 * <p>定义 LLM 提供方实体的存储和查询操作。</p>
 *
 * @Author : haowl
 * @Date : 2026/3/12 23:30
 */
public interface LlmProviderRepository {

    /**
     * 保存 LLM 提供方。
     *
     * @param provider LLM 提供方实体
     * @return 保存后的实体
     */
    LlmProvider save(LlmProvider provider);

    /**
     * 根据 ID 查询 LLM 提供方。
     *
     * @param id 提供方 ID
     * @return LLM 提供方实体
     */
    Optional<LlmProvider> findById(Long id);

    /**
     * 查询所有激活的 LLM 提供方。
     *
     * @return 激活的提供方列表
     */
    List<LlmProvider> findActiveProviders();

    /**
     * 查询指定用户的所有激活 LLM 提供方。
     *
     * @param userId 用户 ID
     * @return 激活的提供方列表
     */
    List<LlmProvider> findActiveProvidersByUserId(Long userId);

    /**
     * 根据类型查询 LLM 提供方。
     *
     * @param providerType 提供方类型
     * @return 提供方列表
     */
    List<LlmProvider> findByProviderType(String providerType);

    /**
     * 查询所有 LLM 提供方。
     *
     * @return 所有提供方列表
     */
    List<LlmProvider> findAll();

    /**
     * 查询指定用户的所有 LLM 提供方。
     *
     * @param userId 用户 ID
     * @return 提供方列表
     */
    List<LlmProvider> findAllByUserId(Long userId);

    /**
     * 删除 LLM 提供方。
     *
     * @param id 提供方 ID
     */
    void deleteById(Long id);

    /**
     * 判断名称是否存在。
     *
     * @param name 名称
     * @return 是否存在
     */
    boolean existsByName(String name);

    /**
     * 判断指定用户下名称是否已存在。
     *
     * @param name   名称
     * @param userId 用户 ID
     * @return 是否存在
     */
    boolean existsByNameAndUserId(String name, Long userId);
}
