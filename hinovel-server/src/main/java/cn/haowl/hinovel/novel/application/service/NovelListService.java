package cn.haowl.hinovel.novel.application.service;

import cn.haowl.hinovel.novel.domain.entity.Novel;
import cn.haowl.hinovel.novel.interfaces.dto.NovelAgentConfigResponse;
import cn.haowl.hinovel.novel.interfaces.dto.NovelListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 小说列表服务。
 *
 * <p>提供小说列表查询功能，包含 Agent 配置信息。</p>
 *
 * @author haowl
 * @since 2024
 */
@Service
@RequiredArgsConstructor
public class NovelListService {

    private final NovelService novelService;
    private final NovelAgentConfigService novelAgentConfigService;

    /**
     * 获取用户小说列表（含 Agent 配置信息）。
     *
     * @param userId 用户 ID
     * @return 小说列表响应
     */
    public List<NovelListResponse> listUserNovelsWithAgentConfig(Long userId) {
        List<Novel> novels = novelService.listUserNovels(userId);
        return novels.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    /**
     * 转换小说实体为响应 DTO。
     *
     * @param novel 小说实体
     * @return 响应 DTO
     */
    private NovelListResponse convertToResponse(Novel novel) {
        NovelListResponse response = new NovelListResponse();
        response.setId(novel.getId());
        response.setUserId(novel.getUserId());
        response.setTitle(novel.getTitle());
        response.setDescription(novel.getDescription());
        response.setCoverUrl(novel.getCoverUrl());
        response.setStatus(novel.getStatus());
        response.setWordCount(novel.getWordCount());
        response.setChapterCount(novel.getChapterCount());
        response.setCreateTime(novel.getCreateTime());
        response.setUpdateTime(novel.getUpdateTime());

        NovelAgentConfigResponse config = novelAgentConfigService.getConfig(novel.getId());
        if (config != null) {
            response.setAuthorAgentId(config.getAuthorAgentId());
            response.setAuthorAgentName(config.getAuthorAgentName());
            response.setEditorAgentId(config.getEditorAgentId());
            response.setEditorAgentName(config.getEditorAgentName());
            response.setQaAgentId(config.getQaAgentId());
            response.setQaAgentName(config.getQaAgentName());
        }

        return response;
    }
}
