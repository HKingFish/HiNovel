package cn.haowl.hinovel.ai.infrastructure.rag.router;

import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.rag.query.router.QueryRouter;

import java.util.Collection;
import java.util.List;

/**
 * @Author : haowl
 * @Date : 2026/6/29 21:41
 * @Desc : 查询路由
 */
public class CommonQueryRouter implements QueryRouter {

    @Override
    public Collection<ContentRetriever> route(Query query) {
        // TODO :
        return List.of();
    }
}
