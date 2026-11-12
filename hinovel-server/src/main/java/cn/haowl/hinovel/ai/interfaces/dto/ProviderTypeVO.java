package cn.haowl.hinovel.ai.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * LLM 提供方类型 VO。
 *
 * 返回给前端的提供方类型编码、显示名称与官方 API 地址。
 *
 * @author : haowl
 * @date : 2026/11/12 15:10
 * @desc : 提供方类型列表的出参
 */
@Data
@AllArgsConstructor
public class ProviderTypeVO {

    /**
     * 类型编码。
     */
    private String code;

    /**
     * 显示名称。
     */
    private String displayName;

    /**
     * 官方 API 地址。
     */
    private String officialBaseUrl;
}
