package cn.haowl.hinovel.agent.infrastructure.agent;

import cn.haowl.hinovel.novel.domain.entity.ChapterOutline;
import cn.haowl.hinovel.novel.domain.entity.NovelChapter;

import java.util.List;

/**
 * Agent 提示词模板统一管理类。
 *
 * <p>集中管理所有 Agent 的系统提示词（System Prompt）和用户提示词构建逻辑，
 * 避免硬编码散落在各 Service 中，便于统一维护和调优。</p>
 *
 * <p>使用方式：各 Service 通过静态常量引用系统提示词，
 * 通过静态方法构建用户提示词。</p>
 *
 * @author haowl
 */
public final class AgentPromptTemplates {

    /**
     * 私有构造方法，禁止实例化
     */
    private AgentPromptTemplates() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }

    // ========== 系统提示词（System Prompt） ==========

    /**
     * 作者 Agent 默认系统提示词
     */
    public static final String AUTHOR_SYSTEM_PROMPT = """
            你是一位专业的小说作者助手，擅长根据大纲和要求改写、续写小说章节内容。
            请严格按照提供的大纲、剧情要点、情感基调和场景设置来生成内容。
            输出纯文本格式的章节内容，不要包含标题、编号等元信息。""";

    /**
     * 编辑 Agent 默认系统提示词（审核）
     */
    public static final String EDITOR_AUDIT_SYSTEM_PROMPT = """
            你是一位专业的小说编辑，擅长审核小说章节内容的质量。
            你需要从以下维度审核章节内容：
            1. 一致性（consistency）：章节内容是否与全文大纲和章节大纲一致
            2. 逻辑性（logic）：情节发展是否合理，有无逻辑漏洞
            3. 人物（character）：人物行为是否符合人设，对话是否自然
            4. 剧情（plot）：剧情推进是否流畅，节奏是否合理
            
            你必须以 JSON 格式返回审核结果，格式如下：
            {
              "overallAssessment": "pass/warning/fail",
              "issues": [
                {
                  "originalText": "有问题的原文片段",
                  "issueType": "consistency/logic/character/plot",
                  "reason": "问题原因说明",
                  "severity": "low/medium/high",
                  "suggestion": "修改建议"
                }
              ],
              "summary": "审核总结"
            }
            
            注意：只返回 JSON，不要包含其他文字或 Markdown 标记。""";

    /**
     * AI 总结大纲系统提示词
     */
    public static final String SUMMARIZE_OUTLINE_SYSTEM_PROMPT = """
            你是一位专业的小说编辑，擅长总结和提炼章节内容。
            请根据提供的章节内容，生成一份简洁精炼的章节大纲，包含：
            1. 本章核心剧情概要
            2. 关键情节转折点
            3. 涉及的主要人物及其行为
            4. 本章对整体剧情的推进作用
            
            要求：语言简洁，重点突出，不超过 500 字。直接输出大纲内容，不要包含标题或编号前缀。""";

    /**
     * 问答 Agent 默认系统提示词
     */
    public static final String QA_SYSTEM_PROMPT = """
            你是一位专业的小说助手，熟悉当前小说的所有内容和设定。
            你可以通过检索工具查询小说已发布章节的内容和大纲，帮助作者回答关于小说的各种问题。
            你还具备创作能力，可以帮助作者生成全文大纲、章节大纲，以及创作和修改章节正文。
            你还可以管理小说的人物图谱，包括查询、新增、修改人物信息和人物关系。
            
            你的职责包括：
            1. 回答关于小说剧情、人物、设定等方面的问题
            2. 帮助作者回顾已有内容，避免前后矛盾
            3. 提供创作建议和灵感
            4. 分析人物关系和剧情走向
            5. 生成或修改全文大纲（调用 saveNovelOutline 工具）
            6. 为指定章节生成或修改大纲（调用 saveChapterOutline 工具）
            7. 创建新章节并写入正文内容（调用 createChapterWithContent 工具）
            8. 修改已有章节的正文内容（调用 updateChapterContent 工具）
            9. 查询、新增、修改人物信息（调用 listCharacters、createCharacter、updateCharacterBasicInfo、updateCharacterProfile 工具）
            10. 管理人物关系（调用 listCharacterRelations、createCharacterRelation 工具）
            
            创作工具使用规范：
            - 操作章节前，先调用 listChapters 了解当前章节结构
            - 生成或修改大纲前，先调用 getNovelOutline 了解已有大纲
            - 创作正文时，结合全文大纲和章节大纲保持剧情连贯
            - 每次创作完成后，向用户反馈操作结果
            
            人物图谱工具使用规范：
            - 新增或修改人物前，先调用 listCharacters 了解当前人物图谱
            - 修改人物信息前，先调用 getCharacterDetail 了解人物当前设定
            - 创建人物关系前，先调用 listCharacterRelations 了解已有关系
            - 人物关系具有方向性，注意主体和客体的区分
            
            回答要求：
            - 基于检索到的实际内容回答，不要编造不存在的情节
            - 如果检索不到相关内容，如实告知作者
            - 回答简洁明了，重点突出
            - 使用中文回答""";

    // ========== 用户提示词构建方法 ==========

    /**
     * 构建大纲改写用户 Prompt。
     *
     * @param chapter             章节信息
     * @param outline             章节大纲
     * @param previousChapters    前几章内容（用于前情提要，可为空列表）
     * @param novelOutlineContent 全文大纲内容（可为 null）
     * @param userRequirement     用户临时需求（可为 null）
     * @return 用户 Prompt
     */
    public static String buildRewritePrompt(NovelChapter chapter, ChapterOutline outline,
                                            List<NovelChapter> previousChapters,
                                            String novelOutlineContent,
                                            String userRequirement) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请根据以下信息改写章节内容：\n\n");

        // 全文大纲（如果配置启用）
        if (novelOutlineContent != null && !novelOutlineContent.isBlank()) {
            prompt.append("【全文大纲】\n").append(novelOutlineContent).append("\n\n");
        }

        // 前情提要（如果配置启用且有前几章内容）
        if (previousChapters != null && !previousChapters.isEmpty()) {
            prompt.append("【前情提要】\n");
            for (NovelChapter prevChapter : previousChapters) {
                prompt.append("第").append(prevChapter.getChapterNumber()).append("章 ")
                        .append(prevChapter.getTitle()).append("：\n");
                prompt.append(prevChapter.getContent()).append("\n\n");
            }
        }

        prompt.append("【章节标题】").append(chapter.getTitle()).append("\n");
        prompt.append("【章节大纲】").append(outline.getOutlineContent()).append("\n");

        appendOutlineDetails(prompt, outline);

        if (outline.getChapterContent() != null && !outline.getChapterContent().isBlank()) {
            prompt.append("\n【原始内容（供参考）】\n").append(outline.getChapterContent()).append("\n");
        }

        // 用户临时需求（用户对改写结果不满意时补充的具体要求）
        if (userRequirement != null && !userRequirement.isBlank()) {
            prompt.append("\n【用户额外要求】\n").append(userRequirement).append("\n");
        }

        prompt.append("\n请根据以上信息生成完整的章节内容，保持文风统一，情节连贯。");
        return prompt.toString();
    }

    /**
     * 构建基于建议的重写用户 Prompt。
     *
     * @param chapter     章节信息
     * @param outline     章节大纲
     * @param suggestions 修改建议
     * @return 用户 Prompt
     */
    public static String buildSuggestionRewritePrompt(NovelChapter chapter,
                                                      ChapterOutline outline,
                                                      List<String> suggestions) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请根据编辑建议修改以下章节内容：\n\n");
        prompt.append("【章节标题】").append(chapter.getTitle()).append("\n");
        prompt.append("【章节大纲】").append(outline.getOutlineContent()).append("\n");
        prompt.append("【原始内容】\n").append(chapter.getContent()).append("\n\n");
        prompt.append("【编辑建议】\n");
        for (int i = 0; i < suggestions.size(); i++) {
            prompt.append(i + 1).append(". ").append(suggestions.get(i)).append("\n");
        }
        prompt.append("\n请根据以上建议修改章节内容，保留原文优秀部分，针对性改进不足之处。");
        return prompt.toString();
    }

    /**
     * 构建审核用户 Prompt。
     *
     * @param chapter      章节信息
     * @param outline      章节大纲
     * @param novelOutline 全文大纲内容（可为 null）
     * @return 用户 Prompt
     */
    public static String buildAuditPrompt(NovelChapter chapter, ChapterOutline outline,
                                          String novelOutline) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请审核以下章节内容：\n\n");

        if (novelOutline != null && !novelOutline.isBlank()) {
            prompt.append("【全文大纲】\n").append(novelOutline).append("\n\n");
        }

        if (outline != null) {
            prompt.append("【章节大纲】\n").append(outline.getOutlineContent()).append("\n");
            appendOutlineDetails(prompt, outline);
            prompt.append("\n");
        }

        prompt.append("【章节标题】").append(chapter.getTitle()).append("\n");
        prompt.append("【章节内容】\n").append(chapter.getContent()).append("\n\n");
        prompt.append("请从一致性、逻辑性、人物、剧情四个维度进行审核，以 JSON 格式返回结果。");

        return prompt.toString();
    }

    /**
     * 构建 AI 总结大纲的用户 Prompt。
     *
     * @param chapter             章节信息
     * @param novelOutlineContent 全文大纲内容（可为 null，作为参考）
     * @return 用户 Prompt
     */
    public static String buildSummarizeOutlinePrompt(NovelChapter chapter,
                                                     String novelOutlineContent) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请总结以下章节的大纲：\n\n");
        prompt.append("【章节标题】").append(chapter.getTitle()).append("\n");
        prompt.append("【章节内容】\n").append(chapter.getContent()).append("\n\n");
        prompt.append("【前情提要】\n").append(chapter.getContent()).append("\n\n");
        if (novelOutlineContent != null && !novelOutlineContent.isBlank()) {
            prompt.append("【全文大纲（参考）】\n").append(novelOutlineContent).append("\n\n");
        }

        prompt.append("请根据章节内容生成简洁精炼的大纲总结");
        return prompt.toString();
    }

    /**
     * 构建问答用户 Prompt。
     *
     * <p>将用户问题、历史对话上下文和全文大纲组合为完整的用户 Prompt，
     * 供问答 Agent 结合向量检索工具进行回答。</p>
     *
     * @param userQuestion        用户问题
     * @param chatHistory         历史对话记录（可为空列表）
     * @param novelOutlineContent 全文大纲内容（可为 null）
     * @return 用户 Prompt
     */
    public static String buildQaPrompt(String userQuestion,
                                       List<String> chatHistory,
                                       String novelOutlineContent) {
        StringBuilder prompt = new StringBuilder();

        // 全文大纲作为背景参考
        if (novelOutlineContent != null && !novelOutlineContent.isBlank()) {
            prompt.append("【小说全文大纲（参考）】\n").append(novelOutlineContent).append("\n\n");
        }

        // 历史对话上下文
        if (chatHistory != null && !chatHistory.isEmpty()) {
            prompt.append("【历史对话记录】\n");
            for (String history : chatHistory) {
                prompt.append(history).append("\n");
            }
            prompt.append("\n");
        }

        prompt.append("【用户问题】\n").append(userQuestion).append("\n\n");
        prompt.append("请根据用户的问题选择合适的方式回应：");
        prompt.append("如果是内容查询类问题，使用检索工具查询后回答；");
        prompt.append("如果是创作类需求（生成大纲、创作章节等），使用对应的创作工具完成操作；");
        prompt.append("如果是人物相关需求（新增、修改人物或关系），使用人物图谱工具完成操作。");
        prompt.append("如果检索不到相关内容，请如实告知。");

        return prompt.toString();
    }

    // ========== 内部辅助方法 ==========

    /**
     * 追加大纲详细信息（剧情要点、情感基调、场景设置）。
     *
     * @param prompt  StringBuilder 实例
     * @param outline 章节大纲
     */
    private static void appendOutlineDetails(StringBuilder prompt, ChapterOutline outline) {
        if (outline.getPlotPoints() != null && !outline.getPlotPoints().isBlank()) {
            prompt.append("【剧情要点】").append(outline.getPlotPoints()).append("\n");
        }
        if (outline.getEmotionTone() != null && !outline.getEmotionTone().isBlank()) {
            prompt.append("【情感基调】").append(outline.getEmotionTone()).append("\n");
        }
        if (outline.getSceneSetting() != null && !outline.getSceneSetting().isBlank()) {
            prompt.append("【场景设置】").append(outline.getSceneSetting()).append("\n");
        }
    }
}
