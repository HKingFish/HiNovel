package cn.haowl.hinovel.novel.domain.command;

import lombok.Builder;
import lombok.Value;

/**
 * @author : haowl
 * @date : 2026/11/17 21:27
 * @desc : 创建草稿章节所需的信息
 */
@Value
@Builder
public class CreateChapterCommand {

    /**
     * 所属小说 ID。
     */
    Long novelId;

    /**
     * 标题，缺省时使用默认标题。
     */
    String title;

    /**
     * 可选的初始正文。
     */
    String content;

    /**
     * 排序号，缺省时追加到当前章节列表末尾。
     */
    Integer sortOrder;
}
