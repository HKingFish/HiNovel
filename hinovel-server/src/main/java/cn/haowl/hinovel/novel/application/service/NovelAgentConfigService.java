package cn.haowl.hinovel.novel.application.service;

import cn.haowl.hinovel.agent.domain.repository.AgentRepository;
import cn.haowl.hinovel.common.constant.CommonConstants;
import cn.haowl.hinovel.novel.domain.entity.NovelAgentConfig;
import cn.haowl.hinovel.novel.domain.repository.NovelAgentConfigRepository;
import cn.haowl.hinovel.novel.domain.service.NovelDomainService;
import cn.haowl.hinovel.novel.interfaces.dto.NovelAgentConfigRequest;
import cn.haowl.hinovel.novel.interfaces.dto.NovelAgentConfigResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static cn.haowl.hinovel.agent.enums.AgentErrorCodeConstants.AGENT_NOT_OWNED;
import static cn.haowl.hinovel.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 小说 Agent 配置服务。
 *
 * 提供小说 Agent 配置的查询、保存和初始化功能。
 */
@Slf4j
@Service
public class NovelAgentConfigService {

    /**
     * 小说 Agent 配置数据访问。
     */
    @Autowired
    private NovelAgentConfigRepository configRepository;

    /**
     * Agent 数据访问。
     */
    @Autowired
    private AgentRepository agentRepository;

    /**
     * 小说归属校验。
     */
    @Autowired
    private NovelDomainService novelDomainService;

    /**
     * 获取小说的 Agent 配置
     *
     * @param novelId 小说 ID
     * @return Agent 配置响应
     */
    public NovelAgentConfigResponse getConfig(Long novelId) {
        NovelAgentConfig config = configRepository.findByNovelId(novelId).orElse(null);
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
     * @param userId 当前用户 ID
     * @return 更新后的配置
     */
    @Transactional(rollbackFor = Exception.class)
    public NovelAgentConfigResponse saveOrUpdateConfig(Long novelId, NovelAgentConfigRequest request, Long userId) {
        novelDomainService.validateOwnership(novelId, userId);
        for (Long agentId : Stream.of(request.getAuthorAgentId(), request.getEditorAgentId(), request.getQaAgentId())
                .filter(java.util.Objects::nonNull).distinct().toList()) {
            agentRepository.findById(agentId)
                .filter(agent -> agent.isAvailableTo(userId))
                .orElseThrow(() -> exception(AGENT_NOT_OWNED));
        }
        NovelAgentConfig config = configRepository.findByNovelId(novelId).orElse(null);

        if (config == null) {
            config = new NovelAgentConfig();
            config.setNovelId(novelId);
            config.setCreateTime(LocalDateTime.now());
            config.setDeleted(CommonConstants.DELETED_FALSE);
        }
        config.configureAuthorAgent(request.getAuthorAgentId());
        config.configureEditorAgent(request.getEditorAgentId());
        config.configureQaAgent(request.getQaAgentId());
        config.setUpdateTime(LocalDateTime.now());

        return convertToResponse(configRepository.save(config));
    }

    /**
     * 初始化小说的 Agent 配置（创建小说时调用）
     *
     * @param novelId 小说 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void initConfig(Long novelId) {
        NovelAgentConfig config = configRepository.findByNovelId(novelId).orElse(null);
        if (config == null) {
            config = new NovelAgentConfig();
            config.setNovelId(novelId);
            config.setCreateTime(LocalDateTime.now());
            config.setUpdateTime(LocalDateTime.now());
            config.setDeleted(CommonConstants.DELETED_FALSE);
            configRepository.save(config);
            log.info("[initConfig][已初始化小说 Agent 配置，novelId={}]", novelId);
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
            agentRepository.findById(config.getAuthorAgentId())
                .ifPresent(agent -> response.setAuthorAgentName(agent.getName()));
        }
        if (config.getEditorAgentId() != null) {
            agentRepository.findById(config.getEditorAgentId())
                .ifPresent(agent -> response.setEditorAgentName(agent.getName()));
        }
        if (config.getQaAgentId() != null) {
            agentRepository.findById(config.getQaAgentId())
                .ifPresent(agent -> response.setQaAgentName(agent.getName()));
        }

        return response;
    }
}
