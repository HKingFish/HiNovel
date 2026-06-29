package cn.haowl.hinovel.ai.infrastructure.rag.tool;

import dev.langchain4j.service.tool.ToolProvider;
import dev.langchain4j.service.tool.ToolProviderRequest;
import dev.langchain4j.service.tool.ToolProviderResult;

/**
 * @Author : haowl
 * @Date : 2026/6/29 20:58
 * @Desc :
 */
public class DynamicToolProvider implements ToolProvider {

    @Override
    public ToolProviderResult provideTools(ToolProviderRequest request) {
        // TODO : 动态加载 DB 中的 tool 和 skills
        return null;
    }
}
