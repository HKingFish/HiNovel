package cn.haowl.hinovel.novel.domain.entity;

import cn.haowl.hinovel.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.util.Objects;

import static cn.haowl.hinovel.common.exception.enums.GlobalErrorCodeConstants.PARAM_ERROR;
import static cn.haowl.hinovel.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 小说实体。
 *
 * <p>小说聚合根，管理小说基本信息和章节。</p>
 *
 * @Author : haowl
 * @Date : 2026/3/12 23:30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("novel")
public class Novel extends BaseEntity {

    /**
     * 作者用户ID。
     */
    private Long userId;

    /**
     * 小说标题。
     */
    private String title;

    /**
     * 小说简介/描述。
     */
    private String description;

    /**
     * 封面图片URL。
     */
    private String coverUrl;

    /**
     * 状态：ONGOING-连载中，COMPLETED-已完结，PAUSED-暂停。
     */
    private String status;

    /**
     * 总字数（冗余字段，实时统计）。
     */
    private Long wordCount;

    /**
     * 总章节数（冗余字段）。
     */
    private Integer chapterCount;

    /**
     * 创建新小说。
     *
     * @param userId      用户ID
     * @param title       标题
     * @param description 简介
     * @return 小说实体
     */
    public static Novel create(Long userId, String title, String description) {
        if (title == null || title.isBlank()) {
            throw exception(PARAM_ERROR);
        }
        Novel novel = new Novel();
        novel.setUserId(userId);
        novel.setTitle(title);
        novel.setDescription(description);
        novel.setStatus(NovelStatus.ONGOING.name());
        novel.setWordCount(0L);
        novel.setChapterCount(0);
        return novel;
    }

    /**
     * 更新标题。
     *
     * @param title 新标题
     */
    public void updateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw exception(PARAM_ERROR);
        }
        this.title = title;
    }

    /**
     * 更新简介。
     *
     * @param description 新简介
     */
    public void updateDescription(String description) {
        this.description = description;
    }

    /**
     * 更新封面。
     *
     * @param coverUrl 封面URL
     */
    public void updateCover(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    /**
     * 完结小说。
     */
    public void complete() {
        if (NovelStatus.COMPLETED.name().equals(this.status)) {
            return;
        }
        this.status = NovelStatus.COMPLETED.name();
    }

    /**
     * 暂停连载。
     */
    public void pause() {
        if (NovelStatus.PAUSED.name().equals(this.status)) {
            return;
        }
        this.status = NovelStatus.PAUSED.name();
    }

    /**
     * 恢复连载。
     */
    public void resume() {
        if (NovelStatus.ONGOING.name().equals(this.status)) {
            return;
        }
        this.status = NovelStatus.ONGOING.name();
    }

    /**
     * 增加字数。
     *
     * @param words 字数
     */
    public void addWordCount(int words) {
        if (this.wordCount == null) {
            this.wordCount = 0L;
        }
        this.wordCount += words;
    }

    /**
     * 减少字数。
     *
     * @param words 字数
     */
    public void subtractWordCount(int words) {
        if (this.wordCount == null) {
            this.wordCount = 0L;
        }
        this.wordCount = Math.max(0, this.wordCount - words);
    }

    /**
     * 增加章节数。
     */
    public void incrementChapterCount() {
        if (this.chapterCount == null) {
            this.chapterCount = 0;
        }
        this.chapterCount++;
    }

    /**
     * 减少章节数。
     */
    public void decrementChapterCount() {
        if (this.chapterCount == null) {
            this.chapterCount = 0;
        }
        this.chapterCount = Math.max(0, this.chapterCount - 1);
    }

    /**
     * 判断是否属于用户。
     *
     * @param userId 用户ID
     * @return 是否属于用户
     */
    public boolean belongsTo(Long userId) {
        return Objects.equals(this.userId, userId);
    }

    /**
     * 判断是否连载中。
     *
     * @return 是否连载中
     */
    public boolean isOngoing() {
        return NovelStatus.ONGOING.name().equals(this.status);
    }

    /**
     * 判断是否已完结。
     *
     * @return 是否已完结
     */
    public boolean isCompleted() {
        return NovelStatus.COMPLETED.name().equals(this.status);
    }

    /**
     * 判断是否暂停。
     *
     * @return 是否暂停
     */
    public boolean isPaused() {
        return NovelStatus.PAUSED.name().equals(this.status);
    }

    /**
     * 小说状态枚举。
     */
    public enum NovelStatus {
        ONGOING,
        COMPLETED,
        PAUSED
    }
}
