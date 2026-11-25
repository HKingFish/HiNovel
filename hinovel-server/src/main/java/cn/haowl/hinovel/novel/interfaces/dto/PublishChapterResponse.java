package cn.haowl.hinovel.novel.interfaces.dto;

import lombok.Data;

/**
 * 发布章节响应。
 *
 * @author haowl
 * @date 2026-11-12
 * @desc 返回章节发布结果
 */
@Data
public class PublishChapterResponse {

    /**
     * 是否发布成功
     */
    private boolean published;

    /**
     * 结果消息
     */
    private String message;
}
