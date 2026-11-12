package cn.haowl.hinovel.novel.interfaces.dto;

import lombok.Data;

/**
 * 创建批注请求。
 *
 * @author haowl
 * @date 2026-11-12
 * @desc 承载批注的定位与内容
 */
@Data
public class CreateAnnotationRequest {

    /**
     * 章节 ID
     */
    private Long chapterId;

    /**
     * 小说 ID
     */
    private Long novelId;

    /**
     * 批注起始字符偏移量
     */
    private Integer startOffset;

    /**
     * 批注结束字符偏移量
     */
    private Integer endOffset;

    /**
     * 被批注的原文内容
     */
    private String originalText;

    /**
     * 批注内容
     */
    private String annotationContent;
}
