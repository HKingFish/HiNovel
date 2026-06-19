package cn.haowl.hinovel.agent.infrastructure.config;

import cn.haowl.hinovel.ai.application.llm.LlmProviderFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * Agent 模块配置类。
 *
 * <p>模型实例由 {@link LlmProviderFactory}
 * 动态管理，通过 {@link dev.langchain4j.service.AiServices} 在运行时按需构建 AiService 实例。</p>
 *
 * @author haowl
 */
@Slf4j
@Configuration
public class AgentConfig {
}
