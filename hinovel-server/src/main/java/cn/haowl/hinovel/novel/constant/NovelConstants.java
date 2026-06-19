package cn.haowl.hinovel.novel.constant;

/**
 * 小说模块常量类。
 *
 * <p>定义小说模块相关的常量配置，从 AgentConstants 中拆分出小说相关部分。</p>
 *
 * @author haowl
 * @since 2024
 */
public final class NovelConstants {

    private NovelConstants() {
    }

    /**
     * 默认版本查询限制。
     */
    public static final int DEFAULT_VERSION_LIMIT = 50;

    /**
     * AI 改写时默认携带的前几章数量。
     */
    public static final int DEFAULT_REWRITE_CONTEXT_CHAPTERS = 2;

    /**
     * AI 问答时默认携带的历史消息条数。
     */
    public static final int DEFAULT_QA_CONTEXT_LENGTH = 10;
}
