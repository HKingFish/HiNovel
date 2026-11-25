package cn.haowl.hinovel.novel.constant;

/**
 * @author : haowl
 * @date : 2026/11/17 21:10
 * @desc : 区分作者自批注和审阅者批注
 */
public enum AnnotationTypeEnum {

    /**
     * 作者自批注（可触发 AI 改写）。
     */
    SELF,

    /**
     * 审阅者批注（仅供参考，不可直接触发 AI 改写）。
     */
    REVIEW
}
