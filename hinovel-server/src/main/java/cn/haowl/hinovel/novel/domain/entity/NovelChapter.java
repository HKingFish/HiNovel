package cn.haowl.hinovel.novel.domain.entity;

import cn.haowl.hinovel.common.constant.CommonConstants;
import cn.haowl.hinovel.common.entity.BaseEntity;
import cn.haowl.hinovel.common.exception.BusinessException;
import cn.haowl.hinovel.common.response.ErrorCode;
import cn.haowl.hinovel.novel.constant.NovelChapterStatus;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 小说章节实体。
 *
 * <p>小说聚合内部实体，由 Novel 聚合根管理。</p>
 *
 * @author haowl
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("novel_chapter")
public class NovelChapter extends BaseEntity {

    public static final String CONTENT_FIELD = "content";

    /**
     * 小说 ID。
     */
    private Long novelId;

    /**
     * 章节数（第几章）。
     */
    private Integer chapterNumber;

    /**
     * 章节标题。
     */
    private String title;

    /**
     * 章节内容。
     */
    private String content;

    /**
     * 字数。
     */
    private Integer wordCount;

    /**
     * 排序序号。
     */
    private Integer sortOrder;

    /**
     * 状态：DRAFT-草稿，PUBLISHED-已发布。
     *
     * @see NovelChapterStatus
     */
    private NovelChapterStatus status;

    /**
     * 发布后内容是否有改动，需要重新发布。0-无需重新发布，1-需要重新发布。
     */
    private Integer needRepublish;

    /**
     * 是否已完成向量数据库存储。0-未存储，1-已存储。
     */
    private Integer vectorStored;

    /**
     * 创建草稿章节。
     *
     * @param novelId       小说 ID
     * @param chapterNumber 章节号
     * @param title         标题
     * @return 章节实体
     */
    public static NovelChapter createDraft(Long novelId, Integer chapterNumber, String title) {
        if (novelId == null || chapterNumber == null || chapterNumber < 1) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        NovelChapter chapter = new NovelChapter();
        chapter.setNovelId(novelId);
        chapter.setChapterNumber(chapterNumber);
        chapter.setTitle(title);
        chapter.setContent("");
        chapter.setWordCount(0);
        chapter.setSortOrder(chapterNumber);
        chapter.setStatus(NovelChapterStatus.DRAFT);
        chapter.setNeedRepublish(CommonConstants.DISABLED);
        chapter.setVectorStored(CommonConstants.DISABLED);
        chapter.setDeleted(CommonConstants.DELETED_FALSE);
        chapter.setVersion(0);
        LocalDateTime now = LocalDateTime.now();
        chapter.setCreateTime(now);
        chapter.setUpdateTime(now);
        return chapter;
    }

    /**
     * 发布章节，重置改动标记。
     */
    public void publish() {
        if (this.content == null || this.content.isBlank()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        this.status = NovelChapterStatus.PUBLISHED;
        this.needRepublish = CommonConstants.DISABLED;
        this.setUpdateTime(LocalDateTime.now());
    }

    /**
     * 更新内容，若已发布则标记需要重新发布，同时重置向量入库状态。
     *
     * @param content 内容
     */
    public void updateContent(String content) {
        this.content = content;
        this.wordCount = content == null ? 0 : content.length();
        this.setUpdateTime(LocalDateTime.now());
        this.vectorStored = CommonConstants.DISABLED;
        if (NovelChapterStatus.PUBLISHED.equals(this.status)) {
            this.needRepublish = CommonConstants.ENABLED;
        }
    }

    /**
     * 更新标题。
     *
     * @param title 标题
     */
    public void updateTitle(String title) {
        this.title = title;
        this.setUpdateTime(LocalDateTime.now());
    }

    /**
     * 判断是否已发布。
     */
    public boolean isPublished() {
        return NovelChapterStatus.PUBLISHED.equals(this.status);
    }

    /**
     * 判断是否草稿。
     */
    public boolean isDraft() {
        return NovelChapterStatus.DRAFT.equals(this.status);
    }

    /**
     * 判断是否属于小说。
     */
    public boolean belongsToNovel(Long novelId) {
        return Objects.equals(this.novelId, novelId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        NovelChapter that = (NovelChapter) obj;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
