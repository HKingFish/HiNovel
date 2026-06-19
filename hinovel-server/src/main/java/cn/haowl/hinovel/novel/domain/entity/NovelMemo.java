package cn.haowl.hinovel.novel.domain.entity;

import cn.haowl.hinovel.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 小说创作手记实体。
 *
 * <p>用于记录小说创作过程中的笔记内容，支持 Markdown 格式，
 * 仅供作者个人阅读，不参与 AI 流程。</p>
 *
 * @author haowl
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("novel_memo")
public class NovelMemo extends BaseEntity {

    /**
     * 小说 ID。
     */
    private Long novelId;

    /**
     * 用户 ID。
     */
    private Long userId;

    /**
     * 手记内容（Markdown 格式）。
     */
    private String content;
}
