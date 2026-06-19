package cn.haowl.hinovel.novel.domain.entity;

import cn.haowl.hinovel.common.constant.CommonConstants;
import cn.haowl.hinovel.common.entity.BaseEntity;
import cn.haowl.hinovel.novel.constant.AnnotationStatus;
import cn.haowl.hinovel.novel.constant.AnnotationType;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.util.Objects;

/**
 * 章节批注实体。
 *
 * <p>存储针对章节某段文字的批注信息，支持作者自批注和审阅者批注两种类型。
 * 作者自批注可触发 AI 改写，审阅者批注仅供参考。</p>
 *
 * @author haowl
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("novel_chapter_annotation")
public class ChapterAnnotation extends BaseEntity {

    /**
     * 章节 ID。
     */
    private Long chapterId;

    /**
     * 小说 ID。
     */
    private Long novelId;

    /**
     * 批注人 ID。
     */
    private Long userId;

    /**
     * 批注起始字符偏移量。
     */
    private Integer startOffset;

    /**
     * 批注结束字符偏移量。
     */
    private Integer endOffset;

    /**
     * 被批注的原文内容（冗余存储，防止偏移量失效）。
     */
    private String originalText;

    /**
     * 批注内容。
     */
    private String annotationContent;

    /**
     * AI 改写结果（用户确认前暂存）。
     */
    private String aiRewriteResult;

    /**
     * 批注类型。
     *
     * @see AnnotationType
     */
    private String annotationType;

    /**
     * 批注状态。
     *
     * @see AnnotationStatus
     */
    private String status;

    /**
     * 是否已查看（0-未查看，1-已查看）。
     */
    private Integer viewed;

    // ==================== 工厂方法 ====================

    /**
     * 创建作者自批注。
     *
     * @param chapterId         章节 ID
     * @param novelId           小说 ID
     * @param userId            用户 ID
     * @param startOffset       起始偏移量
     * @param endOffset         结束偏移量
     * @param originalText      被批注的原文
     * @param annotationContent 批注内容
     * @return 批注实体
     */
    public static ChapterAnnotation createSelfAnnotation(
            Long chapterId, Long novelId, Long userId,
            Integer startOffset, Integer endOffset,
            String originalText, String annotationContent) {
        Objects.requireNonNull(chapterId, "章节 ID 不能为空");
        Objects.requireNonNull(novelId, "小说 ID 不能为空");
        Objects.requireNonNull(userId, "用户 ID 不能为空");

        return ChapterAnnotation.builder()
                .chapterId(chapterId)
                .novelId(novelId)
                .userId(userId)
                .startOffset(startOffset)
                .endOffset(endOffset)
                .originalText(originalText)
                .annotationContent(annotationContent)
                .annotationType(AnnotationType.SELF.name())
                .status(AnnotationStatus.PENDING.name())
                .build();
    }

    // ==================== 业务方法 ====================

    /**
     * 保存 AI 改写结果。
     *
     * @param rewriteResult AI 改写后的文本
     */
    public void saveAiRewriteResult(String rewriteResult) {
        this.aiRewriteResult = rewriteResult;
    }

    /**
     * 采纳批注（将状态标记为已采纳）。
     */
    public void accept() {
        this.status = AnnotationStatus.ACCEPTED.name();
    }

    /**
     * 标记为已处理。
     */
    public void resolve() {
        this.status = AnnotationStatus.RESOLVED.name();
    }

    /**
     * 标记为已查看。
     */
    public void markAsViewed() {
        this.viewed = CommonConstants.ENABLED;
    }

    // ==================== 状态判断 ====================

    /**
     * 是否为作者自批注。
     */
    public boolean isSelfAnnotation() {
        return AnnotationType.SELF.name().equals(this.annotationType);
    }

    /**
     * 是否待处理。
     */
    public boolean isPending() {
        return AnnotationStatus.PENDING.name().equals(this.status);
    }

    /**
     * 是否有 AI 改写结果。
     */
    public boolean hasAiRewriteResult() {
        return aiRewriteResult != null && !aiRewriteResult.isBlank();
    }
}
