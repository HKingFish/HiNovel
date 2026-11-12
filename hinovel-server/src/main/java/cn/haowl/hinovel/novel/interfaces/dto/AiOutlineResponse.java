package cn.haowl.hinovel.novel.interfaces.dto;

import lombok.Data;

/**
 * AI 大纲响应。
 *
 * @author haowl
 * @date 2026-11-12
 * @desc 返回章节 AI 生成的大纲内容
 */
@Data
public class AiOutlineResponse {

    /**
     * AI 生成的大纲内容
     */
    private String aiOutlineContent;

    /**
     * 是否存在 AI 大纲
     */
    private boolean hasAiOutline;
}
