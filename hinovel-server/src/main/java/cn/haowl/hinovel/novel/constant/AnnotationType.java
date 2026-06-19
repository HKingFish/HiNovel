package cn.haowl.hinovel.novel.constant;

/**
 * 批注类型枚举。
 *
 * <p>区分批注来源，用于控制不同角色的操作权限。</p>
 *
 * @author haowl
 */
public enum AnnotationType {

    /**
     * 作者自批注（可触发 AI 改写）。
     */
    SELF,

    /**
     * 审阅者批注（仅供参考，不可直接触发 AI 改写）。
     */
    REVIEW
}
