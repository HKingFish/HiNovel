package cn.haowl.hinovel.novel.interfaces.dto;

import lombok.Data;

/**
 * 批注 AI 改写请求。
 *
 * @author haowl
 * @date 2026-11-12
 * @desc 承载批注改写所需的上下文与改写要求
 */
@Data
public class AnnotationRewriteRequest {

    /**
     * 批注 ID
     */
    private Long annotationId;

    /**
     * 章节 ID
     */
    private Long chapterId;

    /**
     * 整章正文内容（保证改写连贯性）
     */
    private String fullChapterContent;

    /**
     * 被批注的原文片段
     */
    private String originalText;

    /**
     * 改写要求（用户输入的提示词）
     */
    private String annotationContent;

    /**
     * 小说 ID（快捷改写模式使用，不关联批注时需要传入）
     */
    private Long novelId;
}
