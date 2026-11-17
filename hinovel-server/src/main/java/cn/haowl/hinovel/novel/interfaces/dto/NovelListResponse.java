package cn.haowl.hinovel.novel.interfaces.dto;

import cn.haowl.hinovel.novel.constant.NovelStatusEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 小说列表响应DTO
 * 包含小说基本信息和Agent配置
 */
@Data
public class NovelListResponse {

    /**
     * 小说 ID。
     */
    private Long id;
    /**
     * 小说作者 ID。
     */
    private Long userId;
    /**
     * 标题。
     */
    private String title;
    /**
     * 小说简介。
     */
    private String description;
    /**
     * 小说封面 URL。
     */
    private String coverUrl;
    /**
     * 状态编码。
     *
     * @see NovelStatusEnum
     */
    private String status;
    /**
     * 累计字数。
     */
    private Long wordCount;
    /**
     * 章节数。
     */
    private Integer chapterCount;
    /**
     * 创建时间。
     */
    private LocalDateTime createTime;
    /**
     * 更新时间。
     */
    private LocalDateTime updateTime;

    // Agent配置信息
    /**
     * 作者 Agent ID。
     */
    private Long authorAgentId;
    /**
     * 作者 Agent 名称。
     */
    private String authorAgentName;
    /**
     * 编辑 Agent ID。
     */
    private Long editorAgentId;
    /**
     * 编辑 Agent 名称。
     */
    private String editorAgentName;
    /**
     * 问答 Agent ID。
     */
    private Long qaAgentId;
    /**
     * 问答 Agent 名称。
     */
    private String qaAgentName;
}
