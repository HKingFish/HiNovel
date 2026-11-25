package cn.haowl.hinovel.novel.interfaces.dto;

import lombok.Data;

/**
 * 修改章节号请求。
 *
 * @author haowl
 * @date 2026-11-12
 * @desc 承载新的章节号
 */
@Data
public class UpdateChapterNumberRequest {

    /**
     * 新章节号
     */
    private Integer chapterNumber;
}
