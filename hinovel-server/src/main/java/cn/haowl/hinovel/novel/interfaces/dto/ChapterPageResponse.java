package cn.haowl.hinovel.novel.interfaces.dto;

import cn.haowl.hinovel.novel.domain.entity.NovelChapter;
import lombok.Data;

import java.util.List;

/**
 * 章节分页响应。
 *
 * @author haowl
 * @date 2026-11-12
 * @desc 返回章节分页列表及分页信息
 */
@Data
public class ChapterPageResponse {

    /**
     * 章节列表
     */
    private List<NovelChapter> list;

    /**
     * 章节总数
     */
    private int total;

    /**
     * 当前页码
     */
    private int pageNum;

    /**
     * 每页大小
     */
    private int pageSize;

    /**
     * 是否还有更多
     */
    private boolean hasMore;
}
