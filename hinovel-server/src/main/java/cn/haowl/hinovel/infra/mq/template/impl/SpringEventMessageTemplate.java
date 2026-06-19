package cn.haowl.hinovel.infra.mq.template.impl;

import cn.haowl.hinovel.infra.mq.event.DomainEvent;
import cn.haowl.hinovel.infra.mq.template.MessageTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;

/**
 * Spring 事件消息模板实现。
 *
 * <p>基于 Spring ApplicationEventPublisher 实现的本地消息发送，适用于单体应用。</p>
 *
 * @author haowl
 * @since 2024
 */
@Slf4j
@RequiredArgsConstructor
public class SpringEventMessageTemplate implements MessageTemplate {

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void send(DomainEvent event) {
        log.debug("发送领域事件: eventId={}, eventType={}", event.getEventId(), event.getEventType());
        eventPublisher.publishEvent(event);
    }

    @Override
    public void send(DomainEvent event, String queueName) {
        log.debug("发送领域事件到队列: eventId={}, queueName={}", event.getEventId(), queueName);
        eventPublisher.publishEvent(event);
    }

    @Override
    @Async
    public void sendAsync(DomainEvent event) {
        log.debug("异步发送领域事件: eventId={}", event.getEventId());
        eventPublisher.publishEvent(event);
    }

    @Override
    public void sendDelay(DomainEvent event, long delayMillis) {
        log.warn("Spring 事件模板不支持延迟消息，将立即发送: eventId={}", event.getEventId());
        send(event);
    }

    @Override
    public void sendDelay(DomainEvent event, String queueName, long delayMillis) {
        log.warn("Spring 事件模板不支持延迟消息，将立即发送: eventId={}, queueName={}", event.getEventId(), queueName);
        send(event, queueName);
    }
}
