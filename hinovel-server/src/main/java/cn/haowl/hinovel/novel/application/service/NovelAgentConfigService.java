package cn.haowl.hinovel.novel.application.service;

import cn.haowl.hinovel.agent.domain.entity.Agent;
import cn.haowl.hinovel.agent.infrastructure.mapper.AgentMapper;
import cn.haowl.hinovel.common.constant.CommonConstants;
import cn.haowl.hinovel.novel.domain.entity.NovelAgentConfig;
import cn.haowl.hinovel.novel.infrastructure.mapper.NovelAgentConfigMapper;
import cn.haowl.hinovel.novel.interfaces.dto.NovelAgentConfigRequest;
import cn.haowl.hinovel.novel.interfaces.dto.NovelAgentConfigResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 小说 Agent 配置服务。
 *
 * <p>提供小说 Agent 配置的查询、保存和初始化功能。
 * 跨模块依赖 AgentMapper 查询 Agent 名称（只读）。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NovelAgentConfigService {

    private final NovelAgentConfigMapper novelAgentConfigMapper;
    private final AgentMapper agentMapper;

    /**
     * 获取小说的 Agent 配置
     *
     * @param novelId 小说 ID
     * @return Agent 配置响应
     */
    public NovelAgentConfigResponse getConfig(Long novelId) {
        NovelAgentConfig config = novelAgentConfigMapper.selectByNovelId(novelId);
        if (config == null) {
            NovelAgentConfigResponse response = new NovelAgentConfigResponse();
            response.setNovelId(novelId);
            return response;
        }
        return convertToResponse(config);
    }

    /**
     * 保存或更新小说的 Agent 配置
     *
     * @param novelId 小说 ID
     * @param request 配置请求
     * @return 更新后的配置
     */
    @Transactional(rollbackFor = Exception.class)
    public NovelAgentConfigResponse saveOrUpdateConfig(Long novelId, NovelAgentConfigRequest request) {
        NovelAgentConfig config = novelAgentConfigMapper.selectByNovelId(novelId);

        if (config == null) {
            config = new NovelAgentConfig();
            config.setNovelId(novelId);
            config.setAuthorAgentId(request.getAuthorAgentId());
            config.setEditorAgentId(request.getEditorAgentId());
            config.setQaAgentId(request.getQaAgentId());
            config.setCreateTime(LocalDateTime.now());
            config.setUpdateTime(LocalDateTime.now());
            config.setDeleted(CommonConstants.DELETED_FALSE);
            novelAgentConfigMapper.insert(config);
        } else {
            config.setAuthorAgentId(request.getAuthorAgentId());
            config.setEditorAgentId(request.getEditorAgentId());
            config.setQaAgentId(request.getQaAgentId());
            config.setUpdateTime(LocalDateTime.now());
            novelAgentConfigMapper.updateById(config);
        }

        return convertToResponse(config);
    }

    /**
     * 初始化小说的 Agent 配置（创建小说时调用）
     *
     * @param novelId 小说 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void initConfig(Long novelId) {
        NovelAgentConfig config = novelAgentConfigMapper.selectByNovelId(novelId);
        if (config == null) {
            config = new NovelAgentConfig();
            config.setNovelId(novelId);
            config.setCreateTime(LocalDateTime.now());
            config.setUpdateTime(LocalDateTime.now());
            config.setDeleted(CommonConstants.DELETED_FALSE);
            novelAgentConfigMapper.insert(config);
            log.info("初始化小说 Agent 配置成功，novelId: {}", novelId);
        }
    }

    /**
     * 转换为响应 DTO
     */
    private NovelAgentConfigResponse convertToResponse(NovelAgentConfig config) {
        NovelAgentConfigResponse response = new NovelAgentConfigResponse();
        response.setId(config.getId());
        response.setNovelId(config.getNovelId());
        response.setAuthorAgentId(config.getAuthorAgentId());
        response.setEditorAgentId(config.getEditorAgentId());
        response.setQaAgentId(config.getQaAgentId());
        response.setCreateTime(config.getCreateTime());
        response.setUpdateTime(config.getUpdateTime());

        if (config.getAuthorAgentId() != null) {
            Agent authorAgent = agentMapper.selectById(config.getAuthorAgentId());
            if (authorAgent != null) {
                response.setAuthorAgentName(authorAgent.getName());
            }
        }
        if (config.getEditorAgentId() != null) {
            Agent editorAgent = agentMapper.selectById(config.getEditorAgentId());
            if (editorAgent != null) {
                response.setEditorAgentName(editorAgent.getName());
            }
        }
        if (config.getQaAgentId() != null) {
            Agent qaAgent = agentMapper.selectById(config.getQaAgentId());
            if (qaAgent != null) {
                response.setQaAgentName(qaAgent.getName());
            }
        }

        return response;
    }
}
