package cn.haowl.hinovel.ai.application.llm;

import cn.dev33.satoken.stp.StpUtil;
import cn.haowl.hinovel.ai.domain.entity.LlmProvider;
import cn.haowl.hinovel.ai.domain.repository.LlmProviderRepository;
import cn.haowl.hinovel.ai.interfaces.dto.LlmProviderRequest;
import cn.haowl.hinovel.common.service.ApiKeyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static cn.haowl.hinovel.common.exception.enums.GlobalErrorCodeConstants.NOT_FOUND;
import static cn.haowl.hinovel.common.exception.enums.GlobalErrorCodeConstants.PARAM_ERROR;
import static cn.haowl.hinovel.common.exception.util.ServiceExceptionUtil.exception;

/**
 * LLM 提供方应用服务。
 *
 * <p>处理 LLM 提供方的增删改查、激活与停用，密钥加密落库，面向展示的返回值做脱敏。</p>
 *
 * @author haowl
 * @date 2026/11/12 15:10
 */
@Slf4j
@Service
public class LlmProviderService {

    /** LLM 提供方仓储。 */
    @Autowired
    private LlmProviderRepository llmProviderRepository;

    /** API 密钥加解密服务。 */
    @Autowired
    private ApiKeyService apiKeyService;

    /**
     * 创建 LLM 提供方。
     *
     * @param userId  所属用户 ID
     * @param request 创建请求（apiKey 为明文）
     * @return LLM 提供方实体（apiKey 已脱敏）
     */
    public LlmProvider create(Long userId, LlmProviderRequest request) {
        log.info("[create][创建 LLM 提供方，名称：{}，userId={}]", request.getName(), userId);
        if (llmProviderRepository.existsByNameAndUserId(request.getName(), userId)) {
            throw exception(PARAM_ERROR);
        }
        // 加密 API 密钥后再存储
        String encryptedApiKey = apiKeyService.encrypt(request.getApiKey());
        LlmProvider provider = LlmProvider.create(
                request.getName(), request.getProviderType(), request.getBaseUrl(),
                encryptedApiKey, request.getModels(), userId);
        return maskApiKey(llmProviderRepository.save(provider));
    }

    /**
     * 查询指定用户的所有提供方（apiKey 已脱敏，用于前端展示）。
     *
     * @param userId 用户 ID
     * @return 提供方列表
     */
    public List<LlmProvider> listByUserId(Long userId) {
        List<LlmProvider> providers = llmProviderRepository.findAllByUserId(userId);
        providers.forEach(this::maskApiKey);
        return providers;
    }

    /**
     * 获取 LLM 提供方。
     *
     * @param id 提供方ID
     * @return LLM 提供方实体
     */
    public LlmProvider getById(Long id) {
        return llmProviderRepository.findById(id)
            .orElseThrow(() -> exception(NOT_FOUND));
    }

    /**
     * 获取 LLM 提供方详情（apiKey 已脱敏，用于前端展示）。
     *
     * @param id 提供方ID
     * @return 提供方实体
     */
    public LlmProvider getByIdForView(Long id) {
        return maskApiKey(getById(id));
    }

    /**
     * 更新 LLM 提供方。
     *
     * <p>前端未修改 apiKey（传入空值或脱敏值）时保留原有加密密钥不变。</p>
     *
     * @param id      提供方 ID
     * @param request 更新请求
     * @return 更新后的提供方实体（apiKey 已脱敏）
     */
    public LlmProvider update(Long id, LlmProviderRequest request) {
        log.info("[update][更新 LLM 提供方，id={}，名称：{}]", id, request.getName());
        LlmProvider provider = getById(id);
        provider.setName(request.getName());
        provider.setProviderType(request.getProviderType());
        provider.updateBaseUrl(request.getBaseUrl());
        provider.updateModels(request.getModels());
        // 仅当前端传入了新的明文 apiKey 时才重新加密
        if (apiKeyService.isNewApiKey(request.getApiKey())) {
            provider.updateApiKey(apiKeyService.encrypt(request.getApiKey()));
        }
        return maskApiKey(llmProviderRepository.save(provider));
    }

    /**
     * 获取激活的 LLM 提供方列表。
     *
     * @return 激活的提供方列表
     */
    public List<LlmProvider> getActiveProviders() {
        return llmProviderRepository.findActiveProvidersByUserId(StpUtil.getLoginIdAsLong());
    }

    /**
     * 激活提供方。
     *
     * @param id 提供方ID
     */
    public void activate(Long id) {
        log.info("[activate][激活 LLM 提供方，id={}]", id);
        LlmProvider provider = getById(id);
        provider.activate();
        llmProviderRepository.save(provider);
    }

    /**
     * 停用提供方。
     *
     * @param id 提供方ID
     */
    public void deactivate(Long id) {
        log.info("[deactivate][停用 LLM 提供方，id={}]", id);
        LlmProvider provider = getById(id);
        provider.deactivate();
        llmProviderRepository.save(provider);
    }

    /**
     * 更新 API 密钥。
     *
     * @param id     提供方ID
     * @param apiKey 新的API密钥（明文，将自动加密存储）
     */
    public void updateApiKey(Long id, String apiKey) {
        LlmProvider provider = getById(id);
        // 加密后存储
        provider.updateApiKey(apiKeyService.encrypt(apiKey));
        llmProviderRepository.save(provider);
    }

    /**
     * 删除提供方。
     *
     * @param id 提供方ID
     */
    public void delete(Long id) {
        log.info("[delete][删除 LLM 提供方，id={}]", id);
        llmProviderRepository.deleteById(id);
    }

    /**
     * 对提供方的 apiKey 脱敏，避免前端拿到完整密钥。
     *
     * @param provider 提供方实体
     * @return 脱敏后的同一实例
     */
    private LlmProvider maskApiKey(LlmProvider provider) {
        if (provider != null && provider.getApiKey() != null) {
            provider.setApiKey(apiKeyService.decryptAndMask(provider.getApiKey()));
        }
        return provider;
    }
}
