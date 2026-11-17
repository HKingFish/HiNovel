package cn.haowl.hinovel.novel.domain.entity;

import cn.haowl.hinovel.common.constant.CommonConstants;
import cn.haowl.hinovel.common.entity.BaseEntity;
import cn.haowl.hinovel.novel.constant.AnnotationStatusEnum;
import cn.haowl.hinovel.novel.constant.AnnotationTypeEnum;
import cn.haowl.hinovel.novel.domain.command.CreateAnnotationRequest;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import org.apache.commons.lang3.EnumUtils;

import java.util.Objects;

/**
 * 章节批注实体。
 *
 * 存储针对章节某段文字的批注信息，支持作者自批注和审阅者批注两种类型。
 * 作者自批注可触发 AI 改写，审阅者批注仅供参考。
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
     * @see AnnotationTypeEnum
     */
    private String annotationType;

    /**
     * 批注状态。
     *
     * @see AnnotationStatusEnum
     */
    private String status;

    /**
     * 是否已查看（0-未查看，1-已查看）。
     *
     * @see CommonConstants#DISABLED
     * @see CommonConstants#ENABLED
     */
    private Integer viewed;

    // ==================== 工厂方法 ====================

    /**
     * 创建作者自批注。
     *
     * @param request 批注定位与内容
     * @param userId  批注人 ID
     * @return 批注实体
     */
    public static ChapterAnnotation createSelfAnnotation(CreateAnnotationRequest request, Long userId) {
        Objects.requireNonNull(request.getChapterId(), "章节 ID 不能为空");
        Objects.requireNonNull(request.getNovelId(), "小说 ID 不能为空");
        Objects.requireNonNull(userId, "用户 ID 不能为空");

        return ChapterAnnotation.builder()
            .chapterId(request.getChapterId())
            .novelId(request.getNovelId())
                .userId(userId)
            .startOffset(request.getStartOffset())
            .endOffset(request.getEndOffset())
            .originalText(request.getOriginalText())
            .annotationContent(request.getAnnotationContent())
            .annotationType(AnnotationTypeEnum.SELF.name())
            .status(AnnotationStatusEnum.PENDING.name())
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
        this.status = AnnotationStatusEnum.ACCEPTED.name();
    }

    /**
     * 标记为已处理。
     */
    public void resolve() {
        this.status = AnnotationStatusEnum.RESOLVED.name();
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
        return AnnotationTypeEnum.SELF == EnumUtils.getEnum(AnnotationTypeEnum.class, this.annotationType);
    }

    /**
     * 是否待处理。
     */
    public boolean isPending() {
        return AnnotationStatusEnum.PENDING == EnumUtils.getEnum(AnnotationStatusEnum.class, this.status);
    }

    /**
     * 是否有 AI 改写结果。
     */
    public boolean hasAiRewriteResult() {
        return aiRewriteResult != null && !aiRewriteResult.isBlank();
    }
}
