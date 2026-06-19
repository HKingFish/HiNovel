package cn.haowl.hinovel.agent.infrastructure.tool;

import cn.haowl.hinovel.novel.application.service.ChapterOutlineService;
import cn.haowl.hinovel.novel.application.service.NovelChapterService;
import cn.haowl.hinovel.novel.domain.entity.ChapterOutline;
import cn.haowl.hinovel.novel.domain.entity.NovelChapter;
import cn.haowl.hinovel.novel.domain.entity.NovelOutline;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 小说创作工具。
 *
 * <p>供问答 Agent 调用，提供全文大纲生成/更新、章节大纲生成/更新、
 * 章节创建及正文写入等创作能力。使用 LangChain4J {@link Tool} 注解，
 * 可被 AiService 自动发现和调用。</p>
 *
 * <p>该工具不注册为 Spring Bean，而是在需要时通过构造方法注入依赖，
 * 以支持不同小说使用独立的上下文。</p>
 *
 * @author haowl
 */
@Slf4j
public class NovelCreationTool {

    private final Long novelId;
    private final NovelChapterService novelChapterService;
    private final ChapterOutlineService chapterOutlineService;

    /**
     * 构造创作工具实例。
     *
     * @param novelId               小说 ID
     * @param novelChapterService   章节应用服务
     * @param chapterOutlineService 章节大纲应用服务
     */
    public NovelCreationTool(Long novelId,
                             NovelChapterService novelChapterService,
                             ChapterOutlineService chapterOutlineService) {
        this.novelId = novelId;
        this.novelChapterService = novelChapterService;
        this.chapterOutlineService = chapterOutlineService;
    }

    /**
     * 创建工具实例的工厂方法。
     *
     * @param novelId               小说 ID
     * @param novelChapterService   章节应用服务
     * @param chapterOutlineService 章节大纲应用服务
     * @return 创作工具实例
     */
    public static NovelCreationTool create(Long novelId,
                                           NovelChapterService novelChapterService,
                                           ChapterOutlineService chapterOutlineService) {
        return new NovelCreationTool(novelId, novelChapterService, chapterOutlineService);
    }

    // ==================== 全文大纲工具 ====================

    /**
     * 保存或更新小说全文大纲。
     *
     * @param outlineContent 大纲内容
     * @return 操作结果描述
     */
    @Tool("保存或更新小说的全文大纲。当用户要求生成、修改全文大纲时调用此工具。" +
            "传入完整的大纲内容，系统会自动创建或覆盖已有大纲。")
    public String saveNovelOutline(@P("完整的全文大纲内容") String outlineContent) {
        log.info("保存全文大纲，小说ID={}", novelId);
        try {
            NovelOutline outline = new NovelOutline();
            outline.setNovelId(novelId);
            outline.setOutlineContent(outlineContent);
            novelChapterService.saveNovelOutline(outline);
            return "全文大纲保存成功，内容字数：" + outlineContent.length();
        } catch (Exception e) {
            log.error("保存全文大纲失败，小说ID={}，异常信息：{}", novelId, e.getMessage(), e);
            return "保存全文大纲失败：" + e.getMessage();
        }
    }

    /**
     * 获取小说当前的全文大纲。
     *
     * @return 全文大纲内容
     */
    @Tool("获取小说当前的全文大纲内容。在生成或修改大纲前，" +
            "先调用此工具了解已有大纲，避免覆盖用户已有内容。")
    public String getNovelOutline() {
        log.info("获取全文大纲，小说ID={}", novelId);
        try {
            NovelOutline outline = novelChapterService.getNovelOutline(novelId);
            if (outline == null || outline.isEmpty()) {
                return "当前小说尚未设置全文大纲。";
            }
            return "【全文大纲】\n" + outline.getOutlineContent();
        } catch (Exception e) {
            log.error("获取全文大纲失败，小说ID={}，异常信息：{}", novelId, e.getMessage(), e);
            return "获取全文大纲失败：" + e.getMessage();
        }
    }

    // ==================== 章节大纲工具 ====================

    /**
     * 保存或更新指定章节的大纲。
     *
     * @param chapterId      章节 ID
     * @param outlineContent 章节大纲内容
     * @return 操作结果描述
     */
    @Tool("保存或更新指定章节的大纲。当用户要求为某个章节生成或修改大纲时调用此工具。" +
            "需要先通过 listChapters 获取章节列表，确认章节 ID 后再调用。")
    public String saveChapterOutline(
            @P("章节 ID") Long chapterId,
            @P("章节大纲内容") String outlineContent) {
        log.info("保存章节大纲，小说ID={}，章节ID={}", novelId, chapterId);
        try {
            ChapterOutline outline = ChapterOutline.builder()
                    .novelId(novelId)
                    .chapterId(chapterId)
                    .outlineContent(outlineContent)
                    .build();
            chapterOutlineService.saveOrUpdateOutline(outline);
            return "章节大纲保存成功，章节ID：" + chapterId + "，内容字数：" + outlineContent.length();
        } catch (Exception e) {
            log.error("保存章节大纲失败，小说ID={}，章节ID={}，异常信息：{}",
                    novelId, chapterId, e.getMessage(), e);
            return "保存章节大纲失败：" + e.getMessage();
        }
    }

    // ==================== 章节管理工具 ====================

    /**
     * 获取小说的所有章节列表。
     *
     * @return 章节列表信息
     */
    @Tool("获取小说的所有章节列表，包含章节 ID、章节号、标题、字数和状态。" +
            "在创建新章节或操作已有章节前，先调用此工具了解当前章节结构。")
    public String listChapters() {
        log.info("获取章节列表，小说ID={}", novelId);
        try {
            List<NovelChapter> chapters = novelChapterService.getNovelChapters(novelId);
            if (chapters.isEmpty()) {
                return "当前小说尚无章节。";
            }
            StringBuilder result = new StringBuilder();
            result.append("共 ").append(chapters.size()).append(" 个章节：\n\n");
            for (NovelChapter chapter : chapters) {
                result.append("- 章节ID：").append(chapter.getId())
                        .append(" | 第").append(chapter.getChapterNumber()).append("章")
                        .append(" | 标题：").append(chapter.getTitle())
                        .append(" | 字数：").append(chapter.getWordCount())
                        .append(" | 状态：").append(chapter.isPublished() ? "已发布" : "草稿")
                        .append("\n");
            }
            return result.toString();
        } catch (Exception e) {
            log.error("获取章节列表失败，小说ID={}，异常信息：{}", novelId, e.getMessage(), e);
            return "获取章节列表失败：" + e.getMessage();
        }
    }

    /**
     * 创建新章节并写入正文内容。
     *
     * @param title   章节标题
     * @param content 章节正文内容
     * @return 操作结果描述
     */
    @Tool("创建一个新章节并写入正文内容。当用户要求创作新章节时调用此工具。" +
            "系统会自动分配章节号和排序。")
    public String createChapterWithContent(
            @P("章节标题") String title,
            @P("章节正文内容") String content) {
        log.info("创建新章节，小说ID={}，标题={}", novelId, title);
        try {
            NovelChapter chapter = new NovelChapter();
            chapter.setNovelId(novelId);
            chapter.setTitle(title);
            chapter.setContent(content);
            NovelChapter created = novelChapterService.createChapter(chapter);
            return "章节创建成功，章节ID：" + created.getId()
                    + "，第" + created.getChapterNumber() + "章"
                    + "，标题：" + created.getTitle()
                    + "，字数：" + created.getWordCount();
        } catch (Exception e) {
            log.error("创建章节失败，小说ID={}，标题={}，异常信息：{}",
                    novelId, title, e.getMessage(), e);
            return "创建章节失败：" + e.getMessage();
        }
    }

    /**
     * 更新已有章节的正文内容。
     *
     * @param chapterId 章节 ID
     * @param content   新的正文内容
     * @return 操作结果描述
     */
    @Tool("更新已有章节的正文内容。当用户要求修改某个章节的内容时调用此工具。" +
            "需要先通过 listChapters 获取章节列表，确认章节 ID 后再调用。")
    public String updateChapterContent(
            @P("章节 ID") Long chapterId,
            @P("新的章节正文内容") String content) {
        log.info("更新章节内容，小说ID={}，章节ID={}", novelId, chapterId);
        try {
            NovelChapter chapter = novelChapterService.getChapter(chapterId);
            if (chapter == null) {
                return "章节不存在，章节ID：" + chapterId;
            }
            if (!chapter.belongsToNovel(novelId)) {
                return "该章节不属于当前小说，无法操作。";
            }
            chapter.setContent(content);
            novelChapterService.updateChapter(chapter);
            return "章节内容更新成功，章节ID：" + chapterId
                    + "，标题：" + chapter.getTitle()
                    + "，更新后字数：" + content.length();
        } catch (Exception e) {
            log.error("更新章节内容失败，小说ID={}，章节ID={}，异常信息：{}",
                    novelId, chapterId, e.getMessage(), e);
            return "更新章节内容失败：" + e.getMessage();
        }
    }
}
