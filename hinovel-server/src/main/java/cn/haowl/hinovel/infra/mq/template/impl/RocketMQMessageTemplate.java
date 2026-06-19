//package com.haowl.cn.mq.template.impl;
//
//import com.haowl.cn.mq.constant.MqConstants;
//import com.haowl.cn.mq.event.DomainEvent;
//import com.haowl.cn.mq.template.MessageTemplate;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.rocketmq.client.producer.SendCallback;
//import org.apache.rocketmq.client.producer.SendResult;
//import org.apache.rocketmq.spring.core.RocketMQTemplate;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.messaging.Message;
//import org.springframework.messaging.support.MessageBuilder;
//import org.springframework.stereotype.Component;
//
//import java.util.concurrent.CompletableFuture;
//
/// **
// * RocketMQ 消息模板实现
// * 使用 RocketMQ 作为消息队列中间件
// * 适用于生产环境的分布式系统
// */
//@Slf4j
//@Component
//@RequiredArgsConstructor
//@ConditionalOnClass(RocketMQTemplate.class)
//@ConditionalOnProperty(name = "rocketmq.name-server")
//public class RocketMQMessageTemplate implements MessageTemplate {
//
//    private final RocketMQTemplate rocketMQTemplate;
//
//    @Override
//    public void send(Object event) {
//        String topic = resolveTopic(event);
//        try {
//            rocketMQTemplate.syncSend(topic, event);
//            logEvent("同步消息发送成功", event);
//        } catch (Exception e) {
//            log.error("同步消息发送失败: topic={}, event={}, error={}",
//                    topic, event.getClass().getSimpleName(), e.getMessage(), e);
//            throw e;
//        }
//    }
//
//    @Override
//    public void sendAsync(Object event) {
//        String topic = resolveTopic(event);
//        try {
//            rocketMQTemplate.asyncSend(topic, event, new SendCallback() {
//                @Override
//                public void onSuccess(SendResult sendResult) {
//                    logEvent("异步消息发送成功", event);
//                }
//
//                @Override
//                public void onException(Throwable e) {
//                    log.error("异步消息发送失败: topic={}, event={}, error={}",
//                            topic, event.getClass().getSimpleName(), e.getMessage(), e);
//                }
//            });
//        } catch (Exception e) {
//            log.error("异步消息发送异常: topic={}, event={}, error={}",
//                    topic, event.getClass().getSimpleName(), e.getMessage(), e);
//        }
//    }
//
//    @Override
//    public CompletableFuture<Void> sendAsyncWithFuture(Object event) {
//        String topic = resolveTopic(event);
//        CompletableFuture<Void> future = new CompletableFuture<>();
//        try {
//            rocketMQTemplate.asyncSend(topic, event, new SendCallback() {
//                @Override
//                public void onSuccess(SendResult sendResult) {
//                    logEvent("异步Future消息发送成功", event);
//                    future.complete(null);
//                }
//
//                @Override
//                public void onException(Throwable e) {
//                    log.error("异步Future消息发送失败: topic={}, event={}, error={}",
//                            topic, event.getClass().getSimpleName(), e.getMessage(), e);
//                    future.completeExceptionally(e);
//                }
//            });
//        } catch (Exception e) {
//            future.completeExceptionally(e);
//        }
//        return future;
//    }
//
//    @Override
//    public void sendDelayed(Object event, long delayMillis) {
//        String topic = resolveTopic(event);
//        try {
//            int delayLevel = convertToDelayLevel(delayMillis);
//            Message<Object> message = MessageBuilder.withPayload(event).build();
//            rocketMQTemplate.syncSend(topic, message, MqConstants.DEFAULT_SEND_TIMEOUT_MS, delayLevel);
//            logEvent("延迟消息发送成功", event, delayMillis);
//        } catch (Exception e) {
//            log.error("延迟消息发送失败: topic={}, event={}, delay={}ms, error={}",
//                    topic, event.getClass().getSimpleName(), delayMillis, e.getMessage(), e);
//        }
//    }
//
//    @Override
//    public void sendOrdered(Object event, String key) {
//        String topic = resolveTopic(event);
//        try {
//            rocketMQTemplate.syncSendOrderly(topic, event, key);
//            logEvent("顺序消息发送成功", event, key);
//        } catch (Exception e) {
//            log.error("顺序消息发送失败: topic={}, event={}, key={}, error={}",
//                    topic, event.getClass().getSimpleName(), key, e.getMessage(), e);
//        }
//    }
//
//    @Override
//    public void sendInTransaction(Object event, String transactionId) {
//        String topic = resolveTopic(event);
//        try {
//            Message<Object> message = MessageBuilder.withPayload(event).build();
//            rocketMQTemplate.sendMessageInTransaction(topic, message, transactionId);
//            logEvent("事务消息发送成功", event, transactionId);
//        } catch (Exception e) {
//            log.error("事务消息发送失败: topic={}, event={}, txId={}, error={}",
//                    topic, event.getClass().getSimpleName(), transactionId, e.getMessage(), e);
//        }
//    }
//
//    @Override
//    public String getMqType() {
//        return MqConstants.MQ_TYPE_ROCKETMQ;
//    }
//
//    private String resolveTopic(Object event) {
//        if (event instanceof DomainEvent domainEvent) {
//            String topic = domainEvent.getTopic();
//            String tag = domainEvent.getTag();
//            if (tag != null && !tag.isEmpty()) {
//                return topic + ":" + tag;
//            }
//            return topic;
//        }
//        return event.getClass().getSimpleName();
//    }
//
//    private int convertToDelayLevel(long delayMillis) {
//        if (delayMillis <= MqConstants.DELAY_THRESHOLD_1S) return MqConstants.DELAY_LEVEL_1S;
//        if (delayMillis <= MqConstants.DELAY_THRESHOLD_5S) return MqConstants.DELAY_LEVEL_5S;
//        if (delayMillis <= MqConstants.DELAY_THRESHOLD_10S) return MqConstants.DELAY_LEVEL_10S;
//        if (delayMillis <= MqConstants.DELAY_THRESHOLD_30S) return MqConstants.DELAY_LEVEL_30S;
//        if (delayMillis <= MqConstants.DELAY_THRESHOLD_1M) return MqConstants.DELAY_LEVEL_1M;
//        if (delayMillis <= MqConstants.DELAY_THRESHOLD_2M) return MqConstants.DELAY_LEVEL_2M;
//        if (delayMillis <= MqConstants.DELAY_THRESHOLD_3M) return MqConstants.DELAY_LEVEL_3M;
//        if (delayMillis <= MqConstants.DELAY_THRESHOLD_4M) return MqConstants.DELAY_LEVEL_4M;
//        if (delayMillis <= MqConstants.DELAY_THRESHOLD_5M) return MqConstants.DELAY_LEVEL_5M;
//        if (delayMillis <= MqConstants.DELAY_THRESHOLD_6M) return MqConstants.DELAY_LEVEL_6M;
//        if (delayMillis <= MqConstants.DELAY_THRESHOLD_7M) return MqConstants.DELAY_LEVEL_7M;
//        if (delayMillis <= MqConstants.DELAY_THRESHOLD_8M) return MqConstants.DELAY_LEVEL_8M;
//        if (delayMillis <= MqConstants.DELAY_THRESHOLD_9M) return MqConstants.DELAY_LEVEL_9M;
//        if (delayMillis <= MqConstants.DELAY_THRESHOLD_10M) return MqConstants.DELAY_LEVEL_10M;
//        if (delayMillis <= MqConstants.DELAY_THRESHOLD_20M) return MqConstants.DELAY_LEVEL_20M;
//        if (delayMillis <= MqConstants.DELAY_THRESHOLD_30M) return MqConstants.DELAY_LEVEL_30M;
//        if (delayMillis <= MqConstants.DELAY_THRESHOLD_1H) return MqConstants.DELAY_LEVEL_1H;
//        if (delayMillis <= MqConstants.DELAY_THRESHOLD_2H) return MqConstants.DELAY_LEVEL_2H;
//        return MqConstants.DELAY_LEVEL_2H;
//    }
//
//    private void logEvent(String message, Object event) {
//        if (event instanceof DomainEvent domainEvent) {
//            log.debug("{}: type={}, topic={}, eventId={}",
//                    message, domainEvent.getEventType(), domainEvent.getTopic(), domainEvent.getEventId());
//        } else {
//            log.debug("{}: type={}", message, event.getClass().getSimpleName());
//        }
//    }
//
//    private void logEvent(String message, Object event, long delayMillis) {
//        if (event instanceof DomainEvent domainEvent) {
//            log.debug("{}: type={}, topic={}, eventId={}, delay={}ms",
//                    message, domainEvent.getEventType(), domainEvent.getTopic(),
//                    domainEvent.getEventId(), delayMillis);
//        } else {
//            log.debug("{}: type={}, delay={}ms", message, event.getClass().getSimpleName(), delayMillis);
//        }
//    }
//
//    private void logEvent(String message, Object event, String extra) {
//        if (event instanceof DomainEvent domainEvent) {
//            log.debug("{}: type={}, topic={}, eventId={}, extra={}",
//                    message, domainEvent.getEventType(), domainEvent.getTopic(),
//                    domainEvent.getEventId(), extra);
//        } else {
//            log.debug("{}: type={}, extra={}", message, event.getClass().getSimpleName(), extra);
//        }
//    }
//}
