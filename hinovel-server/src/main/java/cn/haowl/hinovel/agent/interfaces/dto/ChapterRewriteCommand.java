package cn.haowl.hinovel.agent.interfaces.dto;

import cn.haowl.hinovel.novel.domain.entity.ChapterOutline;
import cn.haowl.hinovel.novel.domain.entity.NovelChapter;
import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * @author : haowl
 * @date : 2026/11/17 21:09
 * @desc : 章节改写所需的正文、大纲和前情
 */
@Value
@Builder
public class ChapterRewriteCommand {

    /**
     * 待改写章节。
     */
    NovelChapter chapter;

    /**
     * 改写时参考的章节大纲。
     */
    ChapterOutline outline;

    /**
     * 前情章节，缺省时为空列表。
     */
    @Builder.Default
    List<NovelChapter> previousChapters = List.of();

    /**
     * 可选的全文大纲。
     */
    String novelOutlineContent;

    /**
     * 可选的用户改写要求。
     */
    String userRequirement;
}
