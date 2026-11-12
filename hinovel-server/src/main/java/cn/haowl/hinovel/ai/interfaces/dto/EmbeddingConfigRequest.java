package cn.haowl.hinovel.ai.interfaces.dto;

import lombok.Data;

/**
 * 嵌入式模型配置请求 DTO。
 *
 * 创建与更新共用同一个请求体，apiKey 传明文，传空或脱敏值时保持原密钥不变。
 *
 * @author : wylon
 * @date : 2026/11/12 15:10
 * @desc : 嵌入式模型配置的创建与更新入参
 */
@Data
public class EmbeddingConfigRequest {

    /**
     * 配置名称。
     */
    private String name;

    /**
     * 模型类型（openai/onnx）。
     */
    private String modelType;

    /**
     * API 基础地址。
     */
    private String baseUrl;

    /**
     * API 密钥（明文）。
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
}
