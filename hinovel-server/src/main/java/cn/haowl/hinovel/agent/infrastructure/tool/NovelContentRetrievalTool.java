package cn.haowl.hinovel.agent.infrastructure.tool;

import cn.haowl.hinovel.agent.application.service.ChapterVectorService;
import cn.haowl.hinovel.ai.application.vector.VectorStoreFactory;
import cn.haowl.hinovel.ai.application.vector.VectorStorePort;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 小说内容检索工具。
 *
 * <p>供其他 Agent 调用，从 Qdrant 向量数据库中检索与查询语义相关的小说内容。
 * 使用 LangChain4J {@link Tool} 注解，可被 AiService 自动发现和调用。</p>
 *
 * <p>该工具不注册为 Spring Bean，而是在需要时通过构造方法注入对应小说的向量存储实例，
 * 以支持不同小说使用独立的向量集合。</p>
 *
 * @author haowl
 */
@Slf4j
public class NovelContentRetrievalTool {

    private final VectorStorePort vectorStore;
    private final Long novelId;

    /**
     * 默认返回最相似结果数量
     */
    private static final int DEFAULT_TOP_K = 5;

    /**
     * 构造检索工具实例。
     *
     * @param vectorStore 向量存储端口（对应某本小说的集合）
     * @param novelId     小说 ID
     */
    public NovelContentRetrievalTool(VectorStorePort vectorStore, Long novelId) {
        this.vectorStore = vectorStore;
        this.novelId = novelId;
    }

    /**
     * 根据工厂、小说 ID 和用户 ID 创建检索工具实例。
     *
     * <p>使用用户在数据库中配置的 Embedding 模型。若用户未配置 Embedding 模型，
     * 返回 null 表示不启用 RAG 功能，调用方应据此跳过工具挂载。</p>
     *
     * @param vectorStoreFactory 向量存储工厂
     * @param novelId            小说 ID
     * @param userId             用户 ID
     * @return 检索工具实例；用户未配置 Embedding 模型时返回 null
     */
    public static NovelContentRetrievalTool create(VectorStoreFactory vectorStoreFactory,
                                                   Long novelId, Long userId) {
        String collectionName = "novel_" + novelId;
        VectorStorePort vectorStore = vectorStoreFactory.getByCollection(collectionName, userId);
        if (vectorStore == null) {
            return null;
        }
        return new NovelContentRetrievalTool(vectorStore, novelId);
    }

    /**
     * 从小说向量数据库中检索与查询内容语义相关的片段。
     *
     * <p>该方法会返回最相似的文本片段，包含章节标题、内容类型等元信息，
     * 帮助 Agent 了解小说已有的剧情、人物、场景等信息。</p>
     *
     * @param query 查询内容（如"主角的性格特点"、"第三章发生了什么"等）
     * @return 检索到的相关内容，包含来源章节信息
     */
    @Tool("从小说已发布的章节内容和大纲中检索与查询语义相关的信息。" +
            "可用于查找剧情细节、人物描写、场景设定等。" +
            "返回最相关的文本片段及其来源章节信息。")
    public String retrieveNovelContent(String query) {
        log.info("检索小说内容，小说ID={}，查询={}", novelId, query);

        try {
            EmbeddingSearchResult<TextSegment> searchResult = vectorStore.search(query, DEFAULT_TOP_K);
            List<EmbeddingMatch<TextSegment>> matches = searchResult.matches();

            if (matches.isEmpty()) {
                return "未找到与查询相关的内容。";
            }

            StringBuilder result = new StringBuilder();
            result.append("检索到 ").append(matches.size()).append(" 条相关内容：\n\n");

            for (int i = 0; i < matches.size(); i++) {
                EmbeddingMatch<TextSegment> match = matches.get(i);
                TextSegment segment = match.embedded();

                String chapterTitle = segment.metadata().getString("chapterTitle");
                String contentType = segment.metadata().getString("contentType");
                String chapterId = segment.metadata().getString("chapterId");
                double score = match.score();

                result.append("【").append(i + 1).append("】");
                result.append("来源：第").append(chapterId).append("章");
                if (chapterTitle != null) {
                    result.append("「").append(chapterTitle).append("」");
                }
                result.append(" | 类型：").append(ChapterVectorService.CONTENT_TYPE_OUTLINE.equals(contentType) ? "大纲" : "正文");
                result.append(String.format(" | 相似度：%.2f", score));
                result.append("\n");
                result.append(segment.text());
                result.append("\n\n");
            }

            log.info("检索完成，小说ID={}，匹配数={}", novelId, matches.size());
            return result.toString();
        } catch (Exception e) {
            log.error("检索小说内容失败，小说ID={}，查询={}，异常信息：{}",
                    novelId, query, e.getMessage(), e);
            return "检索失败：" + e.getMessage();
        }
    }
}
