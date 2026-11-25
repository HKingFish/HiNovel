package cn.haowl.hinovel.novel.constant;

/**
 * @author : haowl
 * @date : 2026/11/17 21:10
 * @desc : 小说章节发布状态
 */
public enum NovelChapterStatusEnum {

    /**
     * 草稿状态。
     */
    DRAFT(0),

    /**
     * 已发布状态。
     */
    PUBLISHED(1);

    /**
     * 章节列表筛选接口使用的状态编码。
     */
    private final int code;

    /**
     * 绑定章节筛选状态编码。
     */
    NovelChapterStatusEnum(int code) {
        this.code = code;
    }

    /**
     * 按筛选编码解析状态，空值或未知编码返回空。
     */
    public static NovelChapterStatusEnum fromCode(Integer code) {
        for (NovelChapterStatusEnum status : values()) {
            if (Integer.valueOf(status.code).equals(code)) {
                return status;
            }
        }
        return null;
    }
}
