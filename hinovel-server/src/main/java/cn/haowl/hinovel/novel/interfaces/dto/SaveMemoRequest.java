package cn.haowl.hinovel.novel.interfaces.dto;

import lombok.Data;

/**
 * 保存手记请求。
 *
 * @author haowl
 * @date 2026-11-12
 * @desc 承载创作手记内容
 */
@Data
public class SaveMemoRequest {

    /**
     * 手记内容（Markdown 格式）
     */
    private String content;
}
