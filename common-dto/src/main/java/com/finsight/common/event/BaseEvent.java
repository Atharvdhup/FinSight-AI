package com.finsight.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Generic event envelope for all asynchronous Kafka events in FinSight AI.
 * Guarantees standard event structure across microservices.
 *
 * @param <T> Payload domain type
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseEvent<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private EventHeader header;
    private T data;

    public String getEventId() {
        return header != null ? header.getEventId() : null;
    }

    public String getEventType() {
        return header != null ? header.getEventType() : null;
    }
}