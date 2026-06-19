package cn.haowl.hinovel.agent.infrastructure.mapper;

import cn.haowl.hinovel.agent.domain.entity.Agent;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * Agent Mapper
 */
@Mapper
public interface AgentMapper extends BaseMapper<Agent> {
}
