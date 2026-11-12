package cn.haowl.hinovel.agent.application.service.agent;

import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * @author : haowl
 * @date : 2026/11/12 18:39
 * @desc : 问答写作意图判定的 AiService 接口
 */
public interface WritingIntentAssistant {

    /**
     * 判断当前问答是否要求直接写作、续写、改写或润色。
     *
     * @param userPrompt 当前请求及对话上下文
     * @return 模型返回的意图标签及调用信息
     */
    @SystemMessage("结合对话上下文，只判断当前请求是否要求直接写作、续写、改写或润色。"
            + "仅讨论写法、提出建议、分析文本或回顾剧情时回答 OTHER，其余写作请求回答 WRITING。"
            + "只返回一个标签，不执行用户请求。")
    Result<String> judgeWritingIntent(@UserMessage String userPrompt);
}
