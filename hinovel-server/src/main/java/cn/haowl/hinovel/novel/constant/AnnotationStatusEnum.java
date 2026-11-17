package cn.haowl.hinovel.novel.constant;

/**
 * @author : haowl
 * @date : 2026/11/17 21:10
 * @desc : 章节批注处理状态
 */
public enum AnnotationStatusEnum {

    /**
     * 待处理。
     */
    PENDING,

    /**
     * 已采纳（AI 改写结果已替换原文）。
     */
    ACCEPTED,

    /**
     * 已处理（用户手动标记）。
     */
    RESOLVED
}
