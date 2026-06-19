package cn.haowl.hinovel.novel.interfaces.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 小说 Agent 配置响应 DTO
 */
@Data
public class NovelAgentConfigResponse {

    private Long id;
    private Long novelId;
    private Long authorAgentId;
    private String authorAgentName;
    private Long editorAgentId;
    private String editorAgentName;
    private Long qaAgentId;
    private String qaAgentName;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
