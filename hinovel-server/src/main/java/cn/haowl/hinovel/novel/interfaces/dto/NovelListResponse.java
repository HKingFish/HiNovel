package cn.haowl.hinovel.novel.interfaces.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 小说列表响应DTO
 * 包含小说基本信息和Agent配置
 */
@Data
public class NovelListResponse {

    private Long id;
    private Long userId;
    private String title;
    private String description;
    private String coverUrl;
    private String status;
    private Long wordCount;
    private Integer chapterCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // Agent配置信息
    private Long authorAgentId;
    private String authorAgentName;
    private Long editorAgentId;
    private String editorAgentName;
    private Long qaAgentId;
    private String qaAgentName;
}
