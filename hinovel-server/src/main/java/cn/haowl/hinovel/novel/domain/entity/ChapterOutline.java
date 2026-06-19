package cn.haowl.hinovel.novel.domain.entity;

import cn.haowl.hinovel.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.util.Objects;

/**
 * 章节大纲实体。
 *
 * <p>存储每章的大纲内容、剧情要点、人物关联、情感基调等信息，
 * 支持用户手动编写和 AI 自动生成两种大纲来源。</p>
 *
 * @author haowl
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("novel_chapter_outline")
public class ChapterOutline extends BaseEntity {

    /**
     * 所属小说 ID。
     */
    private Long novelId;

    /**
     * 章节 ID。
     */
    private Long chapterId;

    /**
     * 用户编写的大纲内容。
     */
    private String outlineContent;

    /**
     * 章节内容（用于 AI 分析时的上下文，不持久化到数据库）。
     */
    @TableField(exist = false)
    private String chapterContent;

    /**
     * 剧情要点（JSON 格式存储关键剧情点）。
     */
    private String plotPoints;

    /**
     * 涉及人物（JSON 格式存储人物 ID 列表）。
     */
    private String involvedCharacters;

    /**
     * 情感基调。
     */
    private String emotionTone;

    /**
     * 场景设置。
     */
    private String sceneSetting;

    /**
     * AI 生成的大纲内容（与用户自己的 outlineContent 不冲突）。
     */
    private String aiOutlineContent;

    // ==================== 工厂方法 ====================

    /**
     * 创建空白章节大纲。
     *
     * @param novelId   小说 ID
     * @param chapterId 章节 ID
     * @return 空白大纲实体
     */
    public static ChapterOutline createEmpty(Long novelId, Long chapterId) {
        Objects.requireNonNull(novelId, "小说 ID 不能为空");
        Objects.requireNonNull(chapterId, "章节 ID 不能为空");

        return ChapterOutline.builder()
                .novelId(novelId)
                .chapterId(chapterId)
                .build();
    }

    // ==================== 业务方法 ====================

    /**
     * 更新用户编写的大纲内容。
     *
     * @param content 新的大纲内容
     */
    public void updateOutlineContent(String content) {
        this.outlineContent = content;
    }

    /**
     * 更新 AI 生成的大纲内容。
     *
     * @param aiContent AI 生成的大纲
     */
    public void updateAiOutlineContent(String aiContent) {
        this.aiOutlineContent = aiContent;
    }

    /**
     * 更新章节内容快照（用于 AI 分析上下文）。
     *
     * @param chapterContent 章节正文内容
     */
    public void updateChapterContent(String chapterContent) {
        this.chapterContent = chapterContent;
    }

    /**
     * 更新剧情分析信息。
     *
     * @param plotPoints         剧情要点（JSON）
     * @param involvedCharacters 涉及人物（JSON）
     * @param emotionTone        情感基调
     * @param sceneSetting       场景设置
     */
    public void updatePlotAnalysis(String plotPoints, String involvedCharacters,
                                   String emotionTone, String sceneSetting) {
        this.plotPoints = plotPoints;
        this.involvedCharacters = involvedCharacters;
        this.emotionTone = emotionTone;
        this.sceneSetting = sceneSetting;
    }

    // ==================== 状态判断 ====================

    /**
     * 判断是否有用户编写的大纲内容。
     *
     * @return 是否有内容
     */
    public boolean hasOutlineContent() {
        return outlineContent != null && !outlineContent.isBlank();
    }

    /**
     * 判断是否有 AI 生成的大纲内容。
     *
     * @return 是否有 AI 大纲
     */
    public boolean hasAiOutlineContent() {
        return aiOutlineContent != null && !aiOutlineContent.isBlank();
    }

    /**
     * 判断大纲是否属于指定章节。
     *
     * @param chapterId 章节 ID
     * @return 是否属于该章节
     */
    public boolean belongsToChapter(Long chapterId) {
        return Objects.equals(this.chapterId, chapterId);
    }

    /**
     * 获取有效的大纲内容（优先返回用户编写的，其次返回 AI 生成的）。
     *
     * @return 有效大纲内容，均无时返回空字符串
     */
    public String getEffectiveOutline() {
        if (hasOutlineContent()) {
            return outlineContent;
        }
        if (hasAiOutlineContent()) {
            return aiOutlineContent;
        }
        return "";
    }
}
