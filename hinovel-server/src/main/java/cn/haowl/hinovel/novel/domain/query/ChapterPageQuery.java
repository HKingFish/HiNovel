package cn.haowl.hinovel.novel.domain.query;

import cn.haowl.hinovel.common.constant.CommonConstants;
import cn.haowl.hinovel.novel.constant.NovelConstants;
import lombok.Data;

/**
 * @author : haowl
 * @date : 2026/11/17 21:06
 * @desc : 章节分页与筛选条件
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
    private int pageNum = CommonConstants.DEFAULT_PAGE_NUM;

    /**
     * 每页大小
     */
    private int pageSize = NovelConstants.DEFAULT_CHAPTER_PAGE_SIZE;

    /**
     * 发布状态筛选（可选，0=草稿，1=已发布）。
     *
     * @see cn.haowl.hinovel.novel.constant.NovelChapterStatusEnum
     */
    private Integer status;

    /**
     * 入库状态筛选（可选，0=未入库，1=已入库）
     *
     * @see cn.haowl.hinovel.common.constant.CommonConstants#DISABLED
     * @see cn.haowl.hinovel.common.constant.CommonConstants#ENABLED
     */
    private Integer vectorStored;

    /**
     * 标题关键词筛选（可选）
     */
    private String title;
}
