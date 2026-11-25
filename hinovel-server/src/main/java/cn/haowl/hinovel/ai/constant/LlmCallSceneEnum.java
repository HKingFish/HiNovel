package cn.haowl.hinovel.ai.constant;

/**
 * @author : haowl
 * @date : 2026/11/17 21:23
 * @desc : LLM 调用记录的场景
 */
public enum LlmCallSceneEnum {

    /**
     * 按大纲改写章节。
     */
    REWRITE,

    /**
     * 按审核建议重写章节。
     */
    REGENERATE,

    /**
     * 按批注改写片段。
     */
    ANNOTATION_REWRITE,

    /**
     * 审核章节。
     */
    AUDIT,

    /**
     * 小说问答。
     */
    QA,

    /**
     * 识别问答中的写作意图。
     */
    QA_WRITING_INTENT,

    /**
     * 从章节正文总结大纲。
     */
    SUMMARIZE_OUTLINE
}
