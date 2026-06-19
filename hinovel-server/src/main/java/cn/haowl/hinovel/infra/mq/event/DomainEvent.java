package cn.haowl.hinovel.infra.mq.event;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 领域事件基类。
 *
 * <p>所有领域事件的基类，包含事件 ID、类型、时间戳等通用字段。</p>
 *
 * @author haowl
 * @since 2024
 */
@Data
public abstract class DomainEvent {

    /**
     * 事件 ID。
     */
    private String eventId;

    /**
     * 事件类型。
     */
    private String eventType;

    /**
     * 聚合根 ID。
     */
    private Long aggregateId;

    /**
     * 聚合根类型。
     */
    private String aggregateType;

    /**
     * 事件创建时间。
     */
    private LocalDateTime occurredOn;

    /**
     * 事件创建者 ID。
     */
    private Long createdBy;

    /**
     * 构造领域事件。
     */
    protected DomainEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.occurredOn = LocalDateTime.now();
    }

    /**
     * 构造领域事件。
     *
     * @param aggregateId   聚合根 ID
     * @param aggregateType 聚合根类型
     */
    protected DomainEvent(Long aggregateId, String aggregateType) {
        this();
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
    }
}
