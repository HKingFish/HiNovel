package cn.haowl.hinovel.novel.domain.entity;

import cn.haowl.hinovel.common.constant.CommonConstants;
import cn.haowl.hinovel.common.entity.BaseEntity;
import cn.haowl.hinovel.novel.constant.NovelChapterStatusEnum;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Objects;

import static cn.haowl.hinovel.common.exception.enums.GlobalErrorCodeConstants.PARAM_ERROR;
import static cn.haowl.hinovel.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 小说章节实体。
 *
 * 小说聚合内部实体，由 Novel 聚合根管理。
 *
 * @author haowl
 */
@Data
@TableName("novel_chapter")
public class NovelChapter extends BaseEntity {

    /**
     * 存储章节正文的数据库列名。
     */
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
     * @see NovelChapterStatusEnum
     */
    private NovelChapterStatusEnum status;

    /**
     * 发布后内容是否有改动，需要重新发布。0-无需重新发布，1-需要重新发布。
     *
     * @see CommonConstants#DISABLED
     * @see CommonConstants#ENABLED
     */
    private Integer needRepublish;

    /**
     * 是否已完成向量数据库存储。0-未存储，1-已存储。
     *
     * @see CommonConstants#DISABLED
     * @see CommonConstants#ENABLED
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
            throw exception(PARAM_ERROR);
        }
        NovelChapter chapter = new NovelChapter();
        chapter.setNovelId(novelId);
        chapter.setChapterNumber(chapterNumber);
        chapter.setTitle(title);
        chapter.setContent("");
        chapter.setWordCount(0);
        chapter.setSortOrder(chapterNumber);
        chapter.setStatus(NovelChapterStatusEnum.DRAFT);
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
            throw exception(PARAM_ERROR);
        }
        this.status = NovelChapterStatusEnum.PUBLISHED;
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
        this.wordCount = countContentCharacters(content);
        this.setUpdateTime(LocalDateTime.now());
        this.vectorStored = CommonConstants.DISABLED;
        if (NovelChapterStatusEnum.PUBLISHED == this.status) {
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
     * 统计正文中除空白字符外的字数。
     */
    public static int countContentCharacters(String content) {
        if (content == null || content.isBlank()) {
            return 0;
        }
        return content.replaceAll("(?U)\\s+", "").length();
    }

    /**
     * 更新编辑时提供的排序号和状态。
     *
     * @param sortOrder 新排序号，为 null 时保留原值
     * @param status 新状态，为 null 时保留原值
     * @return 是否提供了至少一个更新字段
     */
    public boolean updateMetadata(Integer sortOrder, NovelChapterStatusEnum status) {
        if (sortOrder == null && status == null) {
            return false;
        }
        if (sortOrder != null) {
            this.sortOrder = sortOrder;
        }
        if (status != null) {
            this.status = status;
        }
        return true;
    }

    /**
     * 更新章节在列表中的排序号和章节号。
     *
     * @param order 新排序号
     * @param number 新章节号
     * @return 是否有字段发生变化
     */
    public boolean applySortOrder(Integer order, Integer number) {
        if (Objects.equals(order, sortOrder) && Objects.equals(number, chapterNumber)) {
            return false;
        }
        sortOrder = order;
        chapterNumber = number;
        return true;
    }

    /**
     * 判断是否已发布。
     */
    public boolean isPublished() {
        return NovelChapterStatusEnum.PUBLISHED == this.status;
    }

    /**
     * 判断是否草稿。
     */
    public boolean isDraft() {
        return NovelChapterStatusEnum.DRAFT == this.status;
    }

    /**
     * 判断是否属于小说。
     */
    public boolean belongsToNovel(Long novelId) {
        return Objects.equals(this.novelId, novelId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        NovelChapter that = (NovelChapter) obj;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
