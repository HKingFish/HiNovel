package cn.haowl.hinovel.novel.interfaces.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 小说 Agent 配置响应 DTO
 */
@Data
public class NovelAgentConfigResponse {

    /**
     * 配置 ID。
     */
    private Long id;
    /**
     * 小说 ID。
     */
    private Long novelId;
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
    /**
     * 创建时间。
     */
    private LocalDateTime createTime;
    /**
     * 更新时间。
     */
    private LocalDateTime updateTime;
}
