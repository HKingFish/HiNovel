package cn.haowl.hinovel.agent.interfaces.dto;

import lombok.Data;

import java.util.List;

/**
 * @author : haowl
 * @date : 2026/11/12 18:39
 * @desc : AI 改写与重新生成的请求参数
 */
@Data
public class AIAuthorRequest {

    /**
     * 章节 ID。
     */
    private Long chapterId;

    /**
     * 章节正文。
     */
    private String content;

    /**
     * 章节大纲。
     */
    private String outline;

    /**
     * 剧情要点。
     */
    private String plotPoints;

    /**
     * 关联人物 ID 列表。
     */
    private List<Long> characters;

    /**
     * 情感基调。
     */
    private String emotionTone;

    /**
     * 场景设置。
     */
    private String sceneSetting;

    /**
     * 用户临时需求（用户对改写结果不满意时，可补充具体要求）。
     */
    private String userRequirement;
}
