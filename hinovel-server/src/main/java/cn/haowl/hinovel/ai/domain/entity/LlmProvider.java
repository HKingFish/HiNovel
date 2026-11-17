package cn.haowl.hinovel.ai.domain.entity;

import cn.haowl.hinovel.ai.constant.LlmProviderTypeEnum;
import cn.haowl.hinovel.common.constant.CommonConstants;
import cn.haowl.hinovel.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.EnumUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static cn.haowl.hinovel.common.exception.enums.GlobalErrorCodeConstants.PARAM_ERROR;
import static cn.haowl.hinovel.common.exception.util.ServiceExceptionUtil.exception;

/**
 * LLM 提供方实体。
 *
 * 存储不同 LLM 提供商的配置信息，包括 OpenAI、DeepSeek 等第三方 AI 服务的 API 配置。
 *
 * @Author : haowl
 * @Date : 2026/3/12 23:30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("ai_llm_provider")
public class LlmProvider extends BaseEntity {

    /**
     * 旧版模型列表的空值编码。
     */
    private static final String EMPTY_MODELS = "[]";

    /**
     * 提供方名称。
     */
    private String name;

    /**
     * 提供方类型编码。
     *
     * @see LlmProviderTypeEnum
     */
    private String providerType;

    /**
     * API 基础 URL。
     */
    private String baseUrl;

    /**
     * API 密钥。
     */
    private String apiKey;

    /**
     * 支持的模型列表，按逗号分隔并兼容旧版空值。
     */
    private String models;

    /**
     * 所属用户 ID（数据隔离字段）。
     */
    private Long userId;

    /**
     * 是否激活。
     *
     * @see CommonConstants#ENABLED
     * @see CommonConstants#DISABLED
     */
    private Integer isActive;

    /**
     * 创建 LLM 提供方。
     *
     * @param name         名称
     * @param providerType 类型
     * @param baseUrl      基础URL
     * @param apiKey       API密钥
     * @param models       模型列表
     * @param userId       所属用户 ID
     * @return LLM 提供方实体
     */
    public static LlmProvider create(
        String name, String providerType, String baseUrl, String apiKey, List<String> models, Long userId) {
        if (name == null || name.isBlank()) {
            throw exception(PARAM_ERROR);
        }
        if (providerType == null || providerType.isBlank()) {
            throw exception(PARAM_ERROR);
        }
        LlmProvider provider = new LlmProvider();
        provider.setName(name);
        provider.setProviderType(providerType);
        provider.setBaseUrl(baseUrl);
        provider.setApiKey(apiKey);
        provider.updateModels(models);
        provider.setIsActive(CommonConstants.ENABLED);
        provider.setUserId(userId);
        return provider;
    }

    /**
     * 激活提供方。
     */
    public void activate() {
        this.isActive = CommonConstants.ENABLED;
    }

    /**
     * 停用提供方。
     */
    public void deactivate() {
        this.isActive = CommonConstants.DISABLED;
    }

    /**
     * 更新 API 密钥。
     *
     * @param apiKey 新的 API 密钥
     */
    public void updateApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * 更新基础 URL。
     *
     * @param baseUrl 新的基础 URL
     */
    public void updateBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * 更新支持的模型列表。
     *
     * @param models 模型列表
     */
    public void updateModels(List<String> models) {
        this.models = models == null ? EMPTY_MODELS : String.join(",", models);
    }

    /**
     * 获取模型列表。
     *
     * @return 模型列表
     */
    public List<String> getModelList() {
        if (this.models == null || this.models.isBlank() || EMPTY_MODELS.equals(this.models)) {
            return List.of();
        }
        return Arrays.asList(this.models.split(","));
    }

    /**
     * 判断是否支持指定模型。
     *
     * @param modelName 模型名称
     * @return 是否支持
     */
    public boolean supportsModel(String modelName) {
        if (modelName == null || modelName.isBlank()) {
            return false;
        }
        return getModelList().stream()
                .anyMatch(m -> m.trim().equalsIgnoreCase(modelName.trim()));
    }

    /**
     * 判断是否激活。
     *
     * @return 是否激活
     */
    public boolean isActive() {
        return Integer.valueOf(CommonConstants.ENABLED).equals(this.isActive);
    }

    /**
     * 判断是否为 OpenAI 类型。
     *
     * @return 是否为 OpenAI
     */
    public boolean isOpenAi() {
        return LlmProviderTypeEnum.OPENAI == EnumUtils.getEnum(LlmProviderTypeEnum.class, this.providerType);
    }

    /**
     * 判断是否为 DeepSeek 类型。
     *
     * @return 是否为 DeepSeek
     */
    public boolean isDeepSeek() {
        return LlmProviderTypeEnum.DEEPSEEK == EnumUtils.getEnum(LlmProviderTypeEnum.class, this.providerType);
    }

    /**
     * 判断是否为通义千问类型。
     *
     * @return 是否为通义千问
     */
    public boolean isQwen() {
        return LlmProviderTypeEnum.QWEN == EnumUtils.getEnum(LlmProviderTypeEnum.class, this.providerType);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        LlmProvider that = (LlmProvider) obj;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
