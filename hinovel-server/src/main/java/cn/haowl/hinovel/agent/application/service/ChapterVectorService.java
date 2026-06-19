package cn.haowl.hinovel.agent.application.service;

import cn.haowl.hinovel.ai.application.vector.VectorStoreFactory;
import cn.haowl.hinovel.ai.application.vector.VectorStorePort;
import cn.haowl.hinovel.common.constant.CommonConstants;
import cn.haowl.hinovel.novel.domain.repository.NovelChapterRepository;
import dev.langchain4j.data.segment.TextSegment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 章节向量存储服务。
 *
 * <p>负责将章节内容和大纲切片后存入 Qdrant 向量数据库，
 * 每本小说对应独立的向量集合（collection），实现数据隔离。</p>
 *
 * @author haowl
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChapterVectorService {

    private final VectorStoreFactory vectorStoreFactory;
    private final NovelChapterRepository chapterRepository;

    /**
     * 向量集合名称前缀
     */
    private static final String COLLECTION_PREFIX = "novel_";

    /**
     * 内容类型：章节正文
     */
    public static final String CONTENT_TYPE_CHAPTER = "CHAPTER";

    /**
     * 内容类型：章节大纲
     */
    public static final String CONTENT_TYPE_OUTLINE = "OUTLINE";

    /**
     * Embedding 接口单次批量上限
     */
    private static final int EMBEDDING_BATCH_SIZE = 10;

    /**
     * 文本切片最大长度（字符数）
     */
    private static final int MAX_SEGMENT_LENGTH = 500;

    /**
     * 切片重叠长度（字符数），保证上下文连贯
     */
    private static final int SEGMENT_OVERLAP = 50;

    /**
     * 异步存储章节内容和大纲到向量数据库。
     *
     * @param novelId        小说 ID
     * @param chapterId      章节 ID
     * @param chapterTitle   章节标题
     * @param chapterContent 章节正文内容
     * @param outlineContent 章节大纲内容（可为空）
     * @param userId         用户 ID（用于选择 Embedding 模型）
     */
    @Async
    public void storeChapterVectors(Long novelId, Long chapterId,
                                    String chapterTitle, String chapterContent,
                                    String outlineContent, Long userId) {
        String collectionName = COLLECTION_PREFIX + novelId;
        log.info("开始存储章节向量，小说ID={}，章节ID={}，集合={}", novelId, chapterId, collectionName);

        try {
            VectorStorePort vectorStore = vectorStoreFactory.getByCollection(collectionName, userId);
            List<TextSegment> segments = new ArrayList<>();

            // 切片并存储章节正文
            if (chapterContent != null && !chapterContent.isBlank()) {
                List<TextSegment> contentSegments = splitAndCreateSegments(
                        chapterContent, novelId, chapterId, chapterTitle, CONTENT_TYPE_CHAPTER);
                segments.addAll(contentSegments);
            }

            // 切片并存储章节大纲
            if (outlineContent != null && !outlineContent.isBlank()) {
                List<TextSegment> outlineSegments = splitAndCreateSegments(
                        outlineContent, novelId, chapterId, chapterTitle, CONTENT_TYPE_OUTLINE);
                segments.addAll(outlineSegments);
            }

            if (!segments.isEmpty()) {
                // 先删除该章节已有的向量数据（根据 chapterId 筛选）
                vectorStore.deleteByMetadata("chapterId", String.valueOf(chapterId));
                log.info("已删除章节旧向量数据，小说ID={}，章节ID={}", novelId, chapterId);

                // 分批存储向量数据（Embedding 接口单次批量上限为 10 条）
                storeBatch(vectorStore, segments);

                // 向量存储成功后，更新章节的向量存储标记
                markVectorStored(chapterId);
                log.info("章节向量存储完成，小说ID={}，章节ID={}，切片数={}",
                        novelId, chapterId, segments.size());
            }
        } catch (Exception e) {
            log.error("章节向量存储失败，小说ID={}，章节ID={}，异常信息：{}",
                    novelId, chapterId, e.getMessage(), e);
        }
    }

    /**
     * 将文本按固定长度切片，并为每个切片附加 metadata。
     *
     * @param text         原始文本
     * @param novelId      小说 ID
     * @param chapterId    章节 ID
     * @param chapterTitle 章节标题
     * @param contentType  内容类型（CHAPTER / OUTLINE）
     * @return 文本切片列表
     */
    private List<TextSegment> splitAndCreateSegments(String text, Long novelId,
                                                     Long chapterId, String chapterTitle,
                                                     String contentType) {
        List<TextSegment> segments = new ArrayList<>();
        List<String> chunks = splitText(text);

        for (int i = 0; i < chunks.size(); i++) {
            TextSegment segment = TextSegment.from(
                    chunks.get(i),
                    dev.langchain4j.data.document.Metadata.from("novelId", String.valueOf(novelId))
                            .put("chapterId", String.valueOf(chapterId))
                            .put("chapterTitle", chapterTitle)
                            .put("contentType", contentType)
                            .put("segmentIndex", String.valueOf(i))
            );
            segments.add(segment);
        }
        return segments;
    }

    /**
     * 按固定窗口大小切分文本，带重叠区域保证上下文连贯。
     *
     * @param text 原始文本
     * @return 切片列表
     */
    private List<String> splitText(String text) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return chunks;
        }

        int length = text.length();
        int start = 0;
        while (start < length) {
            int end = Math.min(start + MAX_SEGMENT_LENGTH, length);
            chunks.add(text.substring(start, end));
            // 已到达文本末尾，无需继续切片
            if (end >= length) {
                break;
            }
            // 下一个切片起始位置带重叠，保证上下文连贯
            int nextStart = end - SEGMENT_OVERLAP;
            // 确保 start 严格前进，避免死循环
            start = Math.max(nextStart, start + 1);
        }
        return chunks;
    }

    /**
     * 分批存储向量数据，每批不超过 Embedding 接口的批量上限。
     *
     * @param vectorStore 向量存储实例
     * @param segments    全部文本切片
     */
    private void storeBatch(VectorStorePort vectorStore, List<TextSegment> segments) {
        for (int i = 0; i < segments.size(); i += EMBEDDING_BATCH_SIZE) {
            int end = Math.min(i + EMBEDDING_BATCH_SIZE, segments.size());
            List<TextSegment> batch = segments.subList(i, end);
            vectorStore.store(batch);
        }
    }

    /**
     * 更新章节的向量存储标记为已存储。
     *
     * @param chapterId 章节 ID
     */
    private void markVectorStored(Long chapterId) {
        try {
            chapterRepository.findById(chapterId).ifPresent(chapter -> {
                chapter.setVectorStored(CommonConstants.ENABLED);
                chapterRepository.save(chapter);
            });
        } catch (Exception e) {
            log.error("更新章节向量存储标记失败，章节ID={}，异常信息：{}", chapterId, e.getMessage(), e);
        }
    }

    /**
     * 获取小说对应的向量集合名称。
     *
     * @param novelId 小说 ID
     * @return 集合名称
     */
    public String getCollectionName(Long novelId) {
        return COLLECTION_PREFIX + novelId;
    }
}
