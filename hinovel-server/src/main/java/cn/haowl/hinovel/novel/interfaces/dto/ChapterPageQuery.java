package cn.haowl.hinovel.novel.interfaces.dto;

import lombok.Data;

/**
 * 章节分页查询参数。
 *
 * @author haowl
 * @date 2026-11-12
 * @desc 收敛小说章节分页查询的筛选条件，配合 GET 参数绑定使用
 */
@Data
public class ChapterPageQuery {

    /**
     * 小说 ID
     */
    private Long novelId;

    /**
     * 页码（从 1 开始）
     */
    private int pageNum = 1;

    /**
     * 每页大小
     */
    private int pageSize = 50;

    /**
     * 发布状态筛选（可选，0=草稿，1=已发布）
     */
    private Integer status;

    /**
     * 入库状态筛选（可选，0=未入库，1=已入库）
     */
    private Integer vectorStored;

    /**
     * 标题关键词筛选（可选）
     */
    private String title;
}
