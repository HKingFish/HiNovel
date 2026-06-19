package cn.haowl.hinovel.agent.domain.repository;

import cn.haowl.hinovel.agent.domain.entity.Agent;

import java.util.List;
import java.util.Optional;

/**
 * Agent 仓储接口。
 *
 * <p>定义 Agent 实体的存储和查询操作。</p>
 *
 * @author haowl
 */
public interface AgentRepository {

    /**
     * 保存 Agent。
     *
     * @param agent Agent 实体
     * @return 保存后的 Agent
     */
    Agent save(Agent agent);

    /**
     * 根据 ID 查询 Agent。
     *
     * @param id Agent ID
     * @return Agent 实体
     */
    Optional<Agent> findById(Long id);

    /**
     * 根据用户 ID 查询 Agent 列表。
     *
     * @param userId 用户 ID
     * @return Agent 列表
     */
    List<Agent> findByUserId(Long userId);

    /**
     * 查询所有内置 Agent。
     *
     * @return 内置 Agent 列表
     */
    List<Agent> findBuiltinAgents();

    /**
     * 删除 Agent。
     *
     * @param id Agent ID
     */
    void deleteById(Long id);

    /**
     * 判断 Agent 是否属于用户。
     *
     * @param agentId Agent ID
     * @param userId  用户 ID
     * @return 是否属于用户
     */
    boolean existsByIdAndUserId(Long agentId, Long userId);

    /**
     * 查询用户可用的所有 Agent（自建 + 内置）。
     *
     * @param userId 用户 ID
     * @return Agent 列表
     */
    List<Agent> findAvailableByUserId(Long userId);

    /**
     * 根据名称查询内置 Agent。
     *
     * @param name Agent 名称
     * @return Agent 实体
     */
    Optional<Agent> findBuiltinByName(String name);
}
