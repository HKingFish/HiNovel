package cn.haowl.hinovel.ai.interfaces.dto;

import lombok.Data;

import java.util.List;

/**
 * LLM 提供方请求 DTO。
 *
 * 创建与更新共用同一个请求体，apiKey 传明文，传空或脱敏值时保持原密钥不变。
 *
 * @author : haowl
 * @date : 2026/11/12 15:10
 * @desc : LLM 提供方的创建与更新入参
 */
@Data
public class LlmProviderRequest {

    /**
     * 提供方名称。
     */
    private String name;

    /**
     * 提供方类型（OPENAI / QWEN / DEEPSEEK）。
     */
    private String providerType;

    /**
     * API 基础 URL。
     */
    private String baseUrl;

    /**
     * API 密钥（明文）。
     */
    private String apiKey;

    /**
     * 支持的模型列表。
     */
    private List<String> models;
}
