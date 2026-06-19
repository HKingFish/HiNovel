package cn.haowl.hinovel.novel.domain.entity;

import cn.haowl.hinovel.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 小说大纲实体。
 *
 * <p>属于小说聚合的值对象，管理小说的全文大纲内容。</p>
 *
 * @author haowl
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("novel_outline")
public class NovelOutline extends BaseEntity {

    /**
     * 小说 ID。
     */
    private Long novelId;

    /**
     * 大纲内容。
     */
    private String outlineContent;

    /**
     * 创建空白大纲。
     *
     * @param novelId 小说 ID
     * @return 空白大纲实体
     */
    public static NovelOutline createEmpty(Long novelId) {
        NovelOutline outline = new NovelOutline();
        outline.setNovelId(novelId);
        outline.setOutlineContent("");
        return outline;
    }

    /**
     * 更新大纲内容。
     *
     * @param content 新的大纲内容
     */
    public void updateContent(String content) {
        this.outlineContent = content;
        this.setUpdateTime(LocalDateTime.now());
    }

    /**
     * 判断大纲内容是否为空。
     *
     * @return 是否为空
     */
    public boolean isEmpty() {
        return outlineContent == null || outlineContent.isBlank();
    }
}
