package cn.haowl.hinovel.novel.interfaces.dto;

import lombok.Data;

import java.util.List;

/**
 * 批量排序请求。
 *
 * @author haowl
 * @date 2026-11-12
 * @desc 承载拖拽排序后的章节 ID 顺序
 */
@Data
public class BatchSortRequest {

    /**
     * 按期望顺序排列的章节 ID 列表
     */
    private List<Long> chapterIds;
}
