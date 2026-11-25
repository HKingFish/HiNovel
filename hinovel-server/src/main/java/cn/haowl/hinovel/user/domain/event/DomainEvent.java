package cn.haowl.hinovel.user.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 领域事件基类。
 *
 * 所有领域事件都应继承此类。
 *
 * @Author : haowl
 * @Date : 2026/3/12 22:30
 */
public abstract class DomainEvent {

    /**
     * 事件唯一标识。
     */
    private final String eventId;
    /**
     * 事件发生时间。
     */
    private final LocalDateTime occurredOn;
    /**
     * 事件类型。
     */
    private final String eventType;

    /**
     * 生成事件标识并记录时间。
     */
    protected DomainEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.occurredOn = LocalDateTime.now();
        this.eventType = this.getClass().getSimpleName();
    }

    /**
     * 返回事件唯一标识。
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * 返回事件发生时间。
     */
    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }

    /**
     * 返回事件类型。
     */
    public String getEventType() {
        return eventType;
    }
}
