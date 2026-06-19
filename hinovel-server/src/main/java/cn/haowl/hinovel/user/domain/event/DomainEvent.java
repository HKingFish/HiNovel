package cn.haowl.hinovel.user.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 领域事件基类。
 *
 * <p>所有领域事件都应继承此类。</p>
 *
 * @Author : haowl
 * @Date : 2026/3/12 22:30
 */
public abstract class DomainEvent {

    private final String eventId;
    private final LocalDateTime occurredOn;
    private final String eventType;

    protected DomainEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.occurredOn = LocalDateTime.now();
        this.eventType = this.getClass().getSimpleName();
    }

    public String getEventId() {
        return eventId;
    }

    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }

    public String getEventType() {
        return eventType;
    }
}
