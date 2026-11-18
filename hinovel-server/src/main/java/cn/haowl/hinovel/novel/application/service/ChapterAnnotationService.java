package cn.haowl.hinovel.novel.application.service;

import cn.haowl.hinovel.novel.domain.command.CreateAnnotationRequest;
import cn.haowl.hinovel.novel.domain.entity.ChapterAnnotation;
import cn.haowl.hinovel.novel.domain.entity.NovelChapter;
import cn.haowl.hinovel.novel.domain.service.ChapterDomainService;
import cn.haowl.hinovel.novel.infrastructure.mapper.ChapterAnnotationMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static cn.haowl.hinovel.common.constant.CommonConstants.HTTP_STATUS_NOT_FOUND;
import static cn.haowl.hinovel.common.exception.enums.GlobalErrorCodeConstants.FORBIDDEN;
import static cn.haowl.hinovel.common.exception.util.ServiceExceptionUtil.exception;
import static cn.haowl.hinovel.common.exception.util.ServiceExceptionUtil.exception0;

/**
 * 章节批注应用服务。
 *
 * 提供批注的增删改查及状态流转操作。
 *
 * @author haowl
 */
@Slf4j
@Service
public class ChapterAnnotationService {

    /**
     * 批注数据访问。
     */
    @Autowired
    private ChapterAnnotationMapper annotationMapper;

    /**
     * 批注所属章节的归属校验。
     */
    @Autowired
    private ChapterDomainService chapterDomainService;

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
    @Transactional(rollbackFor = Exception.class)
    public ChapterAnnotation createSelfAnnotation(CreateAnnotationRequest request, Long userId) {
        NovelChapter chapter = chapterDomainService.getOwnedChapter(request.getChapterId(), userId);
        if (!chapter.belongsToNovel(request.getNovelId())) {
            throw exception(FORBIDDEN);
        }
        return createAnnotation(ChapterAnnotation.createSelfAnnotation(request, userId));
    }

    /**
     * 获取章节的所有批注。
     *
     * @param chapterId 章节 ID
     * @param userId 操作者 ID
     * @return 批注列表
     */
    public List<ChapterAnnotation> getAnnotationsByChapterId(Long chapterId, Long userId) {
        chapterDomainService.getOwnedChapter(chapterId, userId);
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
     * @param userId 操作者 ID
     * @return 批注实体，不存在时返回 null
     */
    public ChapterAnnotation getAnnotationById(Long annotationId, Long userId) {
        ChapterAnnotation annotation = annotationMapper.selectById(annotationId);
        if (annotation != null) {
            NovelChapter chapter = chapterDomainService.getOwnedChapter(annotation.getChapterId(), userId);
            if (!chapter.belongsToNovel(annotation.getNovelId())) {
                throw exception(FORBIDDEN);
            }
        }
        return annotation;
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
     * @param userId 操作者 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateAnnotationContent(Long annotationId, String content, Long userId) {
        ChapterAnnotation annotation = getAnnotationById(annotationId, userId);
        if (annotation == null) {
            throw exception0(HTTP_STATUS_NOT_FOUND, "批注不存在");
        }
        annotation.setAnnotationContent(content);
        annotationMapper.updateById(annotation);
    }

    /**
     * 保存 AI 改写结果到批注。
     *
     * @param annotationId  批注 ID
     * @param rewriteResult AI 改写结果
     * @param userId 操作者 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveAiRewriteResult(Long annotationId, String rewriteResult, Long userId) {
        ChapterAnnotation annotation = getAnnotationById(annotationId, userId);
        if (annotation == null) {
            log.warn("[saveAiRewriteResult][批注不存在，annotationId={}]", annotationId);
            return;
        }
        annotation.saveAiRewriteResult(rewriteResult);
        annotationMapper.updateById(annotation);
    }

    /**
     * 采纳批注。
     *
     * @param annotationId 批注 ID
     * @param userId 操作者 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void acceptAnnotation(Long annotationId, Long userId) {
        ChapterAnnotation annotation = getAnnotationById(annotationId, userId);
        if (annotation == null) {
            log.warn("[acceptAnnotation][批注不存在，annotationId={}]", annotationId);
            return;
        }
        annotation.accept();
        annotationMapper.updateById(annotation);
    }

    /**
     * 删除批注。
     *
     * @param annotationId 批注 ID
     * @param userId 操作者 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteAnnotation(Long annotationId, Long userId) {
        if (getAnnotationById(annotationId, userId) != null) {
            annotationMapper.deleteById(annotationId);
        }
    }

    /**
     * 标记批注为已查看。
     *
     * @param annotationId 批注 ID
     * @param userId 操作者 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void markAsViewed(Long annotationId, Long userId) {
        ChapterAnnotation annotation = getAnnotationById(annotationId, userId);
        if (annotation == null) {
            log.warn("[markAsViewed][批注不存在，annotationId={}]", annotationId);
            return;
        }
        annotation.markAsViewed();
        annotationMapper.updateById(annotation);
    }

    /**
     * 标记批注为已处理。
     *
     * @param annotationId 批注 ID
     * @param userId 操作者 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void resolveAnnotation(Long annotationId, Long userId) {
        ChapterAnnotation annotation = getAnnotationById(annotationId, userId);
        if (annotation == null) {
            log.warn("[resolveAnnotation][批注不存在，annotationId={}]", annotationId);
            return;
        }
        annotation.resolve();
        annotationMapper.updateById(annotation);
    }
}
