package cn.haowl.hinovel.novel.application.service;

import cn.haowl.hinovel.novel.domain.entity.ChapterAnnotation;
import cn.haowl.hinovel.novel.infrastructure.mapper.ChapterAnnotationMapper;
import cn.haowl.hinovel.novel.interfaces.dto.CreateAnnotationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static cn.haowl.hinovel.common.exception.util.ServiceExceptionUtil.exception0;

/**
 * 章节批注应用服务。
 *
 * <p>提供批注的增删改查及状态流转操作。</p>
 *
 * @author haowl
 */
@Slf4j
@Service
public class ChapterAnnotationService {

    /** 批注数据访问。 */
    @Autowired
    private ChapterAnnotationMapper annotationMapper;

    /**
     * 创建批注。
     *
     * @param annotation 批注实体
     * @return 创建后的批注（含 ID）
     */
    public ChapterAnnotation createAnnotation(ChapterAnnotation annotation) {
        annotationMapper.insert(annotation);
        return annotation;
    }

    /**
     * 创建用户自建批注。
     *
     * @param request 创建批注请求
     * @param userId  用户 ID
     * @return 创建后的批注
     */
    public ChapterAnnotation createSelfAnnotation(CreateAnnotationRequest request, Long userId) {
        ChapterAnnotation annotation = ChapterAnnotation.createSelfAnnotation(
                request.getChapterId(),
                request.getNovelId(),
                userId,
                request.getStartOffset(),
                request.getEndOffset(),
                request.getOriginalText(),
                request.getAnnotationContent()
        );
        return createAnnotation(annotation);
    }

    /**
     * 获取章节的所有批注。
     *
     * @param chapterId 章节 ID
     * @return 批注列表
     */
    public List<ChapterAnnotation> getAnnotationsByChapterId(Long chapterId) {
        return annotationMapper.selectByChapterId(chapterId);
    }

    /**
     * 获取章节的待处理批注。
     *
     * @param chapterId 章节 ID
     * @return 待处理批注列表
     */
    public List<ChapterAnnotation> getPendingAnnotations(Long chapterId) {
        return annotationMapper.selectPendingByChapterId(chapterId);
    }

    /**
     * 根据 ID 获取批注。
     *
     * @param annotationId 批注 ID
     * @return 批注实体，不存在时返回 null
     */
    public ChapterAnnotation getAnnotationById(Long annotationId) {
        return annotationMapper.selectById(annotationId);
    }

    /**
     * 更新批注内容。
     *
     * @param annotation 批注实体
     */
    public void updateAnnotation(ChapterAnnotation annotation) {
        annotationMapper.updateById(annotation);
    }

    /**
     * 按 ID 更新批注内容。
     *
     * @param annotationId 批注 ID
     * @param content      新的批注内容
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateAnnotationContent(Long annotationId, String content) {
        ChapterAnnotation annotation = annotationMapper.selectById(annotationId);
        if (annotation == null) {
            throw exception0(404, "批注不存在");
        }
        annotation.setAnnotationContent(content);
        annotationMapper.updateById(annotation);
    }

    /**
     * 保存 AI 改写结果到批注。
     *
     * @param annotationId  批注 ID
     * @param rewriteResult AI 改写结果
     */
    public void saveAiRewriteResult(Long annotationId, String rewriteResult) {
        ChapterAnnotation annotation = annotationMapper.selectById(annotationId);
        if (annotation == null) {
            log.warn("批注不存在，ID：{}", annotationId);
            return;
        }
        annotation.saveAiRewriteResult(rewriteResult);
        annotationMapper.updateById(annotation);
    }

    /**
     * 采纳批注。
     *
     * @param annotationId 批注 ID
     */
    public void acceptAnnotation(Long annotationId) {
        ChapterAnnotation annotation = annotationMapper.selectById(annotationId);
        if (annotation == null) {
            log.warn("批注不存在，ID：{}", annotationId);
            return;
        }
        annotation.accept();
        annotationMapper.updateById(annotation);
    }

    /**
     * 删除批注。
     *
     * @param annotationId 批注 ID
     */
    public void deleteAnnotation(Long annotationId) {
        annotationMapper.deleteById(annotationId);
    }

    /**
     * 标记批注为已查看。
     *
     * @param annotationId 批注 ID
     */
    public void markAsViewed(Long annotationId) {
        ChapterAnnotation annotation = annotationMapper.selectById(annotationId);
        if (annotation == null) {
            log.warn("批注不存在，ID：{}", annotationId);
            return;
        }
        annotation.markAsViewed();
        annotationMapper.updateById(annotation);
    }

    /**
     * 标记批注为已处理。
     *
     * @param annotationId 批注 ID
     */
    public void resolveAnnotation(Long annotationId) {
        ChapterAnnotation annotation = annotationMapper.selectById(annotationId);
        if (annotation == null) {
            log.warn("批注不存在，ID：{}", annotationId);
            return;
        }
        annotation.resolve();
        annotationMapper.updateById(annotation);
    }
}
