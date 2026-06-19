package cn.haowl.hinovel.infra.mq.config;

import cn.haowl.hinovel.infra.mq.template.MessageTemplate;
import cn.haowl.hinovel.infra.mq.template.impl.SpringEventMessageTemplate;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 消息模板自动配置类
 * 当没有自定义 MessageTemplate 实现时，默认使用 Spring Event 版本
 */
@AutoConfiguration
@EnableAsync
public class MessageTemplateAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(MessageTemplate.class)
    public MessageTemplate messageTemplate(ApplicationEventPublisher eventPublisher) {
        return new SpringEventMessageTemplate(eventPublisher);
    }
}
