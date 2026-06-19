package cn.haowl.hinovel.novel.constant;

/**
 * 批注状态枚举。
 *
 * <p>定义批注的生命周期状态。</p>
 *
 * @author haowl
 */
public enum AnnotationStatus {

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
