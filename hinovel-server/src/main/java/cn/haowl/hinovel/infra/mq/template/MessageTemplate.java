package cn.haowl.hinovel.infra.mq.template;

import cn.haowl.hinovel.infra.mq.event.DomainEvent;

/**
 * 消息发送模板接口。
 *
 * <p>定义消息发送的统一接口，支持同步/异步发送、延迟消息等。</p>
 *
 * @author haowl
 * @since 2024
 */
public interface MessageTemplate {

    /**
     * 同步发送领域事件。
     *
     * @param event 领域事件
     */
    void send(DomainEvent event);

    /**
     * 同步发送领域事件到指定队列。
     *
     * @param event     领域事件
     * @param queueName 队列名称
     */
    void send(DomainEvent event, String queueName);

    /**
     * 异步发送领域事件。
     *
     * @param event 领域事件
     */
    void sendAsync(DomainEvent event);

    /**
     * 发送延迟消息。
     *
     * @param event         领域事件
     * @param delayMillis   延迟时间（毫秒）
     */
    void sendDelay(DomainEvent event, long delayMillis);

    /**
     * 发送延迟消息到指定队列。
     *
     * @param event         领域事件
     * @param queueName     队列名称
     * @param delayMillis   延迟时间（毫秒）
     */
    void sendDelay(DomainEvent event, String queueName, long delayMillis);
}
