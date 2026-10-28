package com.finsight.common.event;

import lombok.Data;

import java.io.Serializable;

/**
 * Generic event envelope for all asynchronous Kafka events in FinSight AI.
 * Guarantees standard event structure across microservices.
 *
 * @param <T> Payload domain type
 */
@Data
public class BaseEvent<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private EventHeader header;
    private T data;

    public BaseEvent() {
    }

    public BaseEvent(EventHeader header, T data) {
        this.header = header;
        this.data = data;
    }

    public String getEventId() {
        return header != null ? header.getEventId() : null;
    }

    public String getEventType() {
        return header != null ? header.getEventType() : null;
    }
}
