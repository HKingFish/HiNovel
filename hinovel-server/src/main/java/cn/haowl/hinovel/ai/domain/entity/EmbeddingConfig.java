package cn.haowl.hinovel.ai.domain.entity;

import cn.haowl.hinovel.ai.constant.EmbeddingModelType;
import cn.haowl.hinovel.common.constant.CommonConstants;
import cn.haowl.hinovel.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 嵌入式模型配置实体。
 *
 * <p>存储用户自定义的 Embedding 模型配置，支持用户隔离，
 * 每个用户可配置独立的 Embedding 模型（OpenAI 兼容接口）。</p>
 *
 * @author wylon
 * @date 2026/3/23 14:56
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("ai_embedding_config")
public class EmbeddingConfig extends BaseEntity {

    /**
     * 所属用户 ID（数据隔离字段）。
     */
    private Long userId;

    /**
     * 配置名称。
     */
    private String name;

    /**
     * 模型类型。
     *
     * @see EmbeddingModelType
     */
    private String modelType;

    /**
     * API 基础地址。
     */
    private String baseUrl;

    /**
     * API 密钥（加密存储）。
     */
    private String apiKey;

    /**
     * 模型名称。
     */
    private String modelName;

    /**
     * 向量维度。
     */
    private Integer dimensions;

    /**
     * 是否激活。
     *
     * @see CommonConstants#ENABLED
     * @see CommonConstants#DISABLED
     */
    private Integer isActive;

    /**
     * 创建嵌入式模型配置。
     *
     * @param userId     所属用户 ID
     * @param name       配置名称
     * @param modelType  模型类型
     * @param baseUrl    API 基础地址
     * @param apiKey     API 密钥（明文，调用方负责加密）
     * @param modelName  模型名称
     * @param dimensions 向量维度
     * @return 嵌入式模型配置实体
     */
    public static EmbeddingConfig create(Long userId, String name, String modelType,
                                         String baseUrl, String apiKey,
                                         String modelName, Integer dimensions) {
        EmbeddingConfig config = new EmbeddingConfig();
        config.setUserId(userId);
        config.setName(name);
        config.setModelType(modelType != null ? modelType : EmbeddingModelType.OPENAI.getValue());
        config.setBaseUrl(baseUrl);
        config.setApiKey(apiKey);
        config.setModelName(modelName);
        config.setDimensions(dimensions);
        config.setIsActive(CommonConstants.ENABLED);
        return config;
    }

    /**
     * 激活配置。
     */
    public void activate() {
        this.isActive = CommonConstants.ENABLED;
    }

    /**
     * 停用配置。
     */
    public void deactivate() {
        this.isActive = CommonConstants.DISABLED;
    }

    /**
     * 判断是否激活。
     *
     * @return 是否激活
     */
    public boolean isActive() {
        return Integer.valueOf(CommonConstants.ENABLED).equals(this.isActive);
    }
}
