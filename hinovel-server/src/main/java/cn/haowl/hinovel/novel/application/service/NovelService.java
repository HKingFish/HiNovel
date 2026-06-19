package cn.haowl.hinovel.novel.application.service;

import cn.haowl.hinovel.novel.domain.entity.Novel;
import cn.haowl.hinovel.novel.domain.entity.NovelOutline;
import cn.haowl.hinovel.novel.domain.service.NovelDomainService;
import cn.haowl.hinovel.novel.interfaces.dto.NovelCreateRequest;
import cn.haowl.hinovel.novel.interfaces.dto.NovelUpdateRequest;
import cn.haowl.hinovel.novel.interfaces.dto.OutlineUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 小说应用服务。
 *
 * <p>负责小说相关用例的流程编排，协调领域服务完成业务操作。
 * 不包含业务逻辑，业务规则由 {@link NovelDomainService} 和领域实体承担。</p>
 */
@Service
@RequiredArgsConstructor
public class NovelService {

    private final NovelDomainService novelDomainService;
    private final NovelAgentConfigService novelAgentConfigService;

    /**
     * 创建小说。
     *
     * <p>编排流程：创建小说 → 初始化空白大纲 → 初始化 Agent 配置。</p>
     *
     * @param userId  用户 ID
     * @param request 创建请求
     * @return 创建的小说
     */
    @Transactional(rollbackFor = Exception.class)
    public Novel createNovel(Long userId, NovelCreateRequest request) {
        // 通过领域服务创建小说（内部调用 Novel.create() 充血方法）
        Novel novel = novelDomainService.createNovel(
                userId, request.getTitle(), request.getDescription());

        // 补充封面信息（封面不属于核心创建逻辑，由应用层处理）
        if (request.getCoverUrl() != null) {
            novelDomainService.updateNovelInfo(novel, null, null, request.getCoverUrl());
        }

        // 初始化空白大纲
        novelDomainService.getOrCreateOutline(novel.getId());

        // 初始化小说 Agent 配置
        novelAgentConfigService.initConfig(novel.getId());

        return novel;
    }

    /**
     * 更新小说信息。
     *
     * @param userId  用户 ID
     * @param novelId 小说 ID
     * @param request 更新请求
     * @return 更新后的小说
     */
    @Transactional(rollbackFor = Exception.class)
    public Novel updateNovel(Long userId, Long novelId, NovelUpdateRequest request) {
        Novel novel = novelDomainService.validateOwnership(novelId, userId);

        // 通过领域服务更新基本信息（内部调用实体充血方法）
        novel = novelDomainService.updateNovelInfo(
                novel, request.getTitle(), request.getDescription(), request.getCoverUrl());

        // 状态变更单独处理（状态转换有独立的领域规则）
        if (request.getStatus() != null) {
            novel = novelDomainService.changeStatus(novel, request.getStatus());
        }

        return novel;
    }

    /**
     * 删除小说。
     *
     * @param userId  用户 ID
     * @param novelId 小说 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteNovel(Long userId, Long novelId) {
        novelDomainService.deleteNovel(novelId, userId);
    }

    /**
     * 获取小说详情（含归属校验）。
     *
     * @param userId  用户 ID
     * @param novelId 小说 ID
     * @return 小说实体
     */
    public Novel getNovelById(Long userId, Long novelId) {
        return novelDomainService.validateOwnership(novelId, userId);
    }

    /**
     * 获取用户的小说列表。
     *
     * @param userId 用户 ID
     * @return 小说列表
     */
    public List<Novel> listUserNovels(Long userId) {
        return novelDomainService.listUserNovels(userId);
    }

    /**
     * 获取小说大纲。
     *
     * @param userId  用户 ID
     * @param novelId 小说 ID
     * @return 小说大纲
     */
    public NovelOutline getOutline(Long userId, Long novelId) {
        novelDomainService.validateOwnership(novelId, userId);
        return novelDomainService.getOrCreateOutline(novelId);
    }

    /**
     * 更新大纲。
     *
     * @param userId  用户 ID
     * @param request 更新请求
     * @return 更新后的大纲
     */
    @Transactional(rollbackFor = Exception.class)
    public NovelOutline updateOutline(Long userId, OutlineUpdateRequest request) {
        novelDomainService.validateOwnership(request.getNovelId(), userId);
        return novelDomainService.updateOutline(request.getNovelId(), request.getContent());
    }

    /**
     * 刷新小说统计信息（供其他应用服务调用）。
     *
     * @param novelId 小说 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void refreshNovelStats(Long novelId) {
        novelDomainService.refreshNovelStats(novelId);
    }
}
