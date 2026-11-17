package cn.haowl.hinovel.agent.interfaces.dto;

import cn.haowl.hinovel.novel.domain.entity.ChapterOutline;
import cn.haowl.hinovel.novel.domain.entity.NovelChapter;
import lombok.Builder;
import lombok.Value;

/**
 * @author : haowl
 * @date : 2026/11/17 19:23
 * @desc : 章节审核命令
 */
@Value
@Builder
public class ChapterAuditCommand {
    /**
     * 待审核章节，正文可来自编辑器。
     */
    NovelChapter chapter;

    /**
     * 章节大纲，未编写时为空。
     */
    ChapterOutline outline;

    /**
     * 小说全文大纲，未纳入审核时为空。
     */
    String novelOutline;

    /**
     * 发起审核的用户 ID。
     */
    Long userId;
}
