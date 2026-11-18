package cn.haowl.hinovel.novel.constant;

/**
 * 小说模块常量类。
 *
 * 定义小说模块相关的常量配置，从 AgentConstants 中拆分出小说相关部分。
 *
 * @author haowl
 * @since 2024
 */
public final class NovelConstants {

    /**
     * 未提供标题时使用的章节标题。
     */
    public static final String DEFAULT_CHAPTER_TITLE = "未命名章节";

    /**
     * 防止实例化常量类。
     */
    private NovelConstants() {
    }

    /**
     * 默认版本查询限制。
     */
    public static final int DEFAULT_VERSION_LIMIT = 50;

    /**
     * 每章保留的历史版本上限。
     */
    public static final int MAX_CHAPTER_VERSION_KEEP_COUNT = 50;

    /**
     * 章节列表默认每页条数。
     */
    public static final int DEFAULT_CHAPTER_PAGE_SIZE = 50;

    /**
     * AI 改写时默认携带的前几章数量。
     */
    public static final int DEFAULT_REWRITE_CONTEXT_CHAPTERS = 5;

    /**
     * AI 问答时默认携带的历史消息条数。
     */
    public static final int DEFAULT_QA_CONTEXT_LENGTH = 20;
}
